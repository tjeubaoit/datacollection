package com.datacollection.core.collect;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.datacollection.core.collect.fbavt.FbAvatarService;
import com.datacollection.core.collect.filter.CollectFilter;
import com.datacollection.core.collect.filter.DetectLanguageFilter;
import com.datacollection.core.collect.filter.DetectSpamFilter;
import com.datacollection.core.collect.idgen.RemoteIdGenerator;
import com.datacollection.core.collect.log.LogStorage;
import com.datacollection.core.collect.model.BaseEntity;
import com.datacollection.core.collect.model.GraphModel;
import com.datacollection.core.collect.model.Log;
import com.datacollection.core.collect.model.Photo;
import com.datacollection.core.collect.model.Profile;
import com.datacollection.core.transform.DataTransformer;
import com.datacollection.core.transform.HistoryTransformer;
import com.datacollection.core.transform.TransformException;
import com.datacollection.common.concurrency.AllInOneFuture;
import com.datacollection.common.concurrency.FutureAdapter;
import com.datacollection.common.config.Properties;
import com.datacollection.common.types.IdGenerator;
import com.datacollection.common.types.RandomIdGenerator;
import com.datacollection.common.utils.Strings;
import com.datacollection.common.utils.TimeKey;
import com.datacollection.common.utils.Utils;
import com.datacollection.core.extract.model.GenericModel;
import com.datacollection.graphdb.Direction;
import com.datacollection.graphdb.Edge;
import com.datacollection.graphdb.GraphDatabase;
import com.datacollection.graphdb.GraphSession;
import com.datacollection.graphdb.Versions;
import com.datacollection.graphdb.Vertex;
import com.datacollection.core.platform.hystrix.SyncCommand;
import com.datacollection.core.service.notification.Message;
import com.datacollection.core.service.notification.NotificationService;
import com.datacollection.core.service.remoteconfig.RemoteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Cài đặt của CollectService xử lý và lưu trữ dữ liệu dưới dạng Graph
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class GraphCollectService implements CollectService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Collection<CollectFilter> filters = new ArrayList<>();
    private final FbAvatarService fbAvatarService;
    private final NotificationService notificationService;

    private final GraphSession session;
    private final LogStorage logStorage;
    private final Map<String, Integer> dataVersionMapping = new HashMap<>();

    private final IdGenerator idGenerator;
    private final RemoteIdGenerator remoteIdGenerator;

    /**
     *
     * @param props
     */
    public GraphCollectService(Properties props) {
        this(props, Arrays.asList(new DetectLanguageFilter(), new DetectSpamFilter()));
    }

    /**
     *
     * @param props
     * @param filters
     */
    public GraphCollectService(Properties props, Collection<CollectFilter> filters) {
        this.filters.addAll(filters);

        this.session = GraphDatabase.open(props);
        this.logStorage = LogStorage.create(props);
        this.fbAvatarService = new FbAvatarService(props);
        this.notificationService = NotificationService.create(props);

        this.idGenerator = new RandomIdGenerator();
        this.remoteIdGenerator = RemoteIdGenerator.create(props);

        RemoteConfiguration remoteConfig = RemoteConfiguration.create(props);
        for (Map.Entry<String, String> e : remoteConfig.getPropertiesByPrefix("_v").entrySet()) {
            String type = e.getKey().split(Versions.DELIMITER)[1];
            dataVersionMapping.put(type, Integer.parseInt(e.getValue()));
        }
    }

    @Override
    public ListenableFuture<?> collect(GenericModel generic) {
        GraphModel gm = transform(generic);
        return gm != null && isValid(gm)
                ? collect(gm)
                : Futures.immediateFuture(0);
    }

    @Override
    public void close() {
        this.session.close();
        this.notificationService.close();
    }

    /**
     *
     * @param gm
     * @return
     */
    public ListenableFuture<?> collect(GraphModel gm) {
        List<ListenableFuture<?>> futures = new LinkedList<>();

        for (Profile profile : gm.profiles()) {
            // Bảo đảm tất cả profile đều có id trước khi lưu vào graph
            if (Strings.isNullOrEmpty(profile.id())) {
                // Method check và sinh mới ID được gọi qua Hystrix để monitor
                SyncCommand<String> cmd = new SyncCommand<>("collector", "FindOrCreateUid",
                        () -> findOrCreateUid(profile));
                String uid = cmd.execute();
                if (uid == null) return Futures.immediateFailedFuture(cmd.getExecutionException());
                profile.setId(uid);
            }
        }

        for (Profile profile : gm.profiles()) {
            saveProfileAndEntities(futures, profile);
            saveRelationships(futures, profile);
            saveUntrustedDataForMatching(futures, profile);

            // Các profile ko chứa các untrusted entities sẽ được add vào Notification Center
            // để sync sang ElasticSearch (sync mới hàng ngày). Các profile có untrusted entities
            // cần được matching trước sau đó mới sync để tránh sync nhiều lần
            if (profile.untrustedEntities().isEmpty()) {
                Message msg = new Message("profile_" + TimeKey.currentTimeKey(), profile.id());
                msg.putProperty("uid", profile.id());
                futures.add(notificationService.addMessage(msg));
            }

            // save log
            futures.add(logStorage.addLog(profile.id(), profile.log()));
        }

        // Sử dụng Future để chờ cho tất cả async operation hoàn th
        Future<?> fut = AllInOneFuture.from(futures);
        return FutureAdapter.from(fut, list -> list);
    }

    /**
     * Lưu tất cả các profiles và entities thành các đỉnh trong graph
     *
     * @param futures lưu danh sách các Futures dùng để biểu diễn
     *                kết quả của thao tác lưu các đỉnh
     * @param profile profile cần lưu
     */
    private void saveProfileAndEntities(List<ListenableFuture<?>> futures, Profile profile) {
        Log log = profile.log();
        List<Vertex> vertices = new LinkedList<>();

        // add profile entities as vertex
        profile.trustedEntities().forEach(e -> vertices.add(createVertex(e.entity)));
        profile.anonymousEntities().forEach(e -> vertices.add(createVertex(e.entity)));

        profile.untrustedEntities().forEach(e -> {
            // Để phục vụ cho matching thì thông tin về log sẽ được lưu thành các thuộc tính
            // của các untrusted entities, mỗi log là một thuộc t
            Vertex vertex = createVertex(e.entity);
            String entityLog = Strings.join(Constants.PART_DELIMITER,
                    Constants.LOG, log.source(), log.id());
            vertex.putProperty(entityLog, profile.id());
            vertices.add(vertex);
        });

        // add profile vertex
        Vertex vProfile = createVertex(profile);
        Versions.setVersion(log.type(), latestVersionByType(log.type()), vProfile);
        vertices.add(vProfile);

        // save all vertices in one batch
        futures.add(session.addVertices(vertices));
    }

    /**
     *
     * @param futures
     * @param profile
     */
    private void saveRelationships(List<ListenableFuture<?>> futures, Profile profile) {
        Log log = profile.log();
        List<Edge> edges = new LinkedList<>();
        Vertex vProfile = Vertex.create(profile.id(), Constants.PROFILE);

        profile.trustedEntities().forEach(e -> {
            Vertex vEntity = createVertex(e.entity);

            Edge edge = Edge.create(e.relationship.name(), vProfile, vEntity, e.relationship.properties());
            Versions.setVersion(log.type(), latestVersionByType(log.type()), edge);
            edges.add(edge);

            Edge reverseEdge = Edge.create(Constants.PROFILE, vEntity, vProfile, e.relationship.properties());
            Versions.setVersion(log.type(), latestVersionByType(log.type()), reverseEdge);
            edges.add(reverseEdge);
        });

        profile.anonymousEntities().forEach(e -> {
            Vertex vEntity = createVertex(e.entity);
            Edge edge = Edge.create(e.relationship.name(), vProfile, vEntity, e.relationship.properties());
            Versions.setVersion(log.type(), latestVersionByType(log.type()), edge);
            edges.add(edge);
        });

        profile.untrustedEntities().forEach(e -> {
            Vertex vEntity = createVertex(e.entity);
            String lb = Constants.HIDDEN_PREFIX + e.relationship.name();

            Edge edge = Edge.create(lb, vProfile, vEntity, e.relationship.properties());
            Versions.setVersion(log.type(), latestVersionByType(log.type()), edge);

            // add relationship log as extra edge properties to tell where entity appears
            String relLog = Strings.join(Constants.PART_DELIMITER, Constants.LOG,
                    log.source(), log.id());
            edge.putProperty(relLog, "");
            edges.add(edge);
        });

        // save all edges in one batch
        futures.add(session.addEdges(edges));
    }

    /**
     *
     * @param futures
     * @param profile
     */
    private void saveUntrustedDataForMatching(List<ListenableFuture<?>> futures, Profile profile) {
        Log log = profile.log();

        for (Profile.EntityRelationship e : profile.untrustedEntities()) {
            BaseEntity entity = e.entity;

            // add trace log to trigger has new anonymous data need to be matched
            Message msg = new Message(entity.label(), log.source(), entity.id(), profile.id());
            msg.putProperty("source", log.source());
            msg.putProperty("value", entity.id());
            msg.putProperty("uid", profile.id());
            futures.add(notificationService.addMessage(msg));
        }
    }

    /**
     *
     * @param profile
     * @return
     */
    private String findOrCreateUid(Profile profile) {
        List<Photo> photos = new ArrayList<>();
        for (Profile.EntityRelationship e : profile.trustedEntities()) {
            BaseEntity entity = e.entity;
            Vertex vEntity = createVertex(entity);

            // find adjacency vertex link by edge with label profile
            Edge edge = session.edges(vEntity, Direction.OUT, Constants.PROFILE).first();
            if (edge != null && !Versions.checkElementOutOfDate(edge, dataVersionMapping)) {
                return edge.inVertex().id();
            }

            // if entity is fb account, continue find by fb avatar
            if (!Constants.FACEBOOK.equals(entity.label())) continue;

            String url = fbAvatarService.getAvatarUrl(entity.id());
            if (!url.isEmpty()) {
                Photo photo = new Photo(url, "domain", Constants.FACEBOOK);
                Vertex vPhoto = createVertex(photo);

                edge = session.edges(vPhoto, Direction.OUT, Constants.PROFILE).first();
                if (edge != null && !Versions.checkElementOutOfDate(edge, dataVersionMapping)) {
                    return edge.inVertex().id();
                }

                // vProfile != null nghĩa là photo node này chưa được gán với bất kì profile
                // nào (chưa tồn tại trong graph). Lưu lại photo này như là một trusted entity
                photos.add(photo);
            } else {
                // Url empty nghĩa là fb id collect được từ object GenericModel hiện tại là
                // ko hợp lệ (điều này có thể xảy ra do id này thuộc về một app đã bị xóa
                // hoặc trong quá trình crawl việc extract, save dữ liệu có lỗi).
                // Với trường hợp này vẫn lưu lại fbid nhưng coi nó là một dạng đặc biệt (zombie)
                profile.setType(Profile.TYPE_FB_ZOMBIE);
                return "fbzb_" + entity.id();
            }
        }

        // save new photos as trusted entities
        photos.forEach(profile::addTrustedEntity);

        return String.valueOf(generateNewId(profile));
    }

    /**
     *
     * @param profile
     * @return
     */
    private long generateNewId(Profile profile) {
        long defVal = idGenerator.generate();
        if (profile.trustedEntities().isEmpty()) return defVal;

        List<String> seeds = new ArrayList<>();
        profile.trustedEntities().forEach(e -> seeds.add(e.entity.toString()));

        SyncCommand<Long> cmd = new SyncCommand<>("collector", "RemoteIdGenerator",
                () -> remoteIdGenerator.generate(seeds, defVal), -1L);
        long id = cmd.execute();
        if (id == -1L) throw cmd.getCheckedException();
        return id;
    }

    /**
     *
     * @param type
     * @return
     */
    private int latestVersionByType(String type) {
        return dataVersionMapping.getOrDefault(type, Versions.MIN_VERSION);
    }

    private static Vertex createVertex(BaseEntity entity) {
        return Vertex.create(entity.id(), entity.label(), entity.properties());
    }

    /**
     *
     * @param generic
     * @return
     */
    private GraphModel transform(GenericModel generic) {
        try {
            DataTransformer transformer = isHistory(generic) ?
                    new HistoryTransformer() : DataTransformer.create(generic.getType());

            return transformer.transform(generic);
        } catch (TransformException e) {
//            logger.info("Record ignored, message [" + e.getMessage() + "] " + generic);
        } catch (RuntimeException e) {
            logger.warn("Transform failed: " + Utils.toJson(generic), e);
        }
        return null;
    }

    /**
     *
     * @param genericModel
     * @return
     */
    private boolean isHistory(GenericModel genericModel) {
        try {
            Long.parseLong(genericModel.getId());
            return genericModel.getPost().get("source_id") != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private boolean isValid(GraphModel gm) {
        for (CollectFilter filter : filters) {
            if (!filter.accept(gm)) return false;
        }
        return true;
    }
}
