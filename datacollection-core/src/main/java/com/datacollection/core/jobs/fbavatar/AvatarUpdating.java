package com.datacollection.core.jobs.fbavatar;

import com.datacollection.core.collect.fbavt.FbAvatarService;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.config.Properties;
import com.datacollection.common.lifecycle.AbstractLifeCycle;
import com.datacollection.common.tasks.TaskManager;
import com.datacollection.common.utils.ThreadPool;
import com.datacollection.common.utils.Threads;
import com.datacollection.graphdb.Direction;
import com.datacollection.graphdb.GraphDatabase;
import com.datacollection.graphdb.GraphSession;
import com.datacollection.graphdb.Vertex;
import com.datacollection.graphdb.VertexSet;
import com.datacollection.core.metric.Counter;
import com.datacollection.core.metric.CounterMetrics;
import com.datacollection.core.metric.Sl4jPublisher;
import com.datacollection.core.platform.elastic.ElasticClientProvider;
import com.datacollection.core.platform.elastic.ElasticConfig;
import com.datacollection.core.service.remoteconfig.RemoteConfiguration;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

public class AvatarUpdating extends AbstractLifeCycle {

    private static final String VERSION_KEY = "_v@avatarupdate";
    private GraphSession session;
    private FbAvatarService fbAvatarService;
    private Counter counter;
    private CounterMetrics counterMetrics;
    private int version;

    private Client elasticClient;
    private String elasticIndex;

    private TaskManager taskManager;

    private Properties props;

    public AvatarUpdating(Properties props) {
        this.props = props;
    }

    @Override
    protected void onInitialize() {
        session = GraphDatabase.open(props);
        fbAvatarService = new FbAvatarService(props);
        counter = new Counter();
        counterMetrics = new CounterMetrics(new Sl4jPublisher(), "default-metric-group",
                "update avatar", counter, 1000);
        RemoteConfiguration remoteConfig = RemoteConfiguration.create(props);
        version = remoteConfig.getIntProperty(VERSION_KEY, 1);

        ElasticConfig elasticConfig = new ElasticConfig(props);
        elasticClient = ElasticClientProvider.getDefault(elasticConfig);
        elasticIndex = elasticConfig.getElasticIndex();
        int nThread = props.getIntProperty("nthread", Runtime.getRuntime().availableProcessors());
        System.out.println(nThread);
        ExecutorService executor = ThreadPool.builder()
                .setCoreSize(nThread)
                .setQueueSize(10)
                .setDaemon(true)
                .setNamePrefix("update-avatar")
                .build();
        taskManager = new TaskManager(props, executor);
    }

    @Override
    protected void onStart() {
        counterMetrics.start();
    }

    @Override
    protected void onProcess() {
        updateAvatarByElastic();
//        updateAvatarByGraph();
    }

    public void updateAvatarByGraph() {
        VertexSet profiles = session.vertices("profile");
        for (Vertex profile : profiles) {
            counter.inc();
            //get all fbids per app
            VertexSet facebooks = session.verticesByAdjVertexLabels(profile, Direction.OUT, "fb.com");
            List<String> fbIds = new ArrayList<>();
            for (Vertex facebook : facebooks) {
                fbIds.add(facebook.id());
            }
            while (isNotCanceled()) {
                try {
                    taskManager.submit(new UpdateHandlerGraph(fbIds, profile, version, session, fbAvatarService));
                    break;
                } catch (RejectedExecutionException e) {
                    Threads.sleep(1);
                }
            }
        }
    }

    public void updateAvatarByElastic() {
        SearchResponse sr = elasticClient.prepareSearch(elasticIndex)
                .setTypes("profiles")
                .setQuery(QueryBuilders.termQuery("photo.domain", "fb.com"))
                .setScroll(new TimeValue(6000000))
                .setSize(1000)
                .execute()
                .actionGet();
        while (isNotCanceled()) {
            for (SearchHit hit : sr.getHits()) {
                counter.inc();
                Map<String, Object> source = hit.getSource();
                String profile = hit.getId();
                Vertex profileVertex = Vertex.create(profile, "profile");
                List<Map<String, String>> accounts = (List<Map<String, String>>) source.get("account");
                List<String> fbIds = new ArrayList<>();
                for (Map<String, String> acc : accounts) {
                    if (!acc.get("type").equals("fb.com")) continue;
                    fbIds.add(acc.get("id"));
                }
                while (isNotCanceled()) {
                    try {
                        taskManager.submit(new UpdateHandlerElastic(fbIds,
                                profileVertex,
                                version,
                                elasticClient,
                                elasticIndex,
                                session,
                                fbAvatarService));
                        break;
                    } catch (RejectedExecutionException e) {
                        Threads.sleep(1);
                    }
                }
            }
            sr = elasticClient.prepareSearchScroll(sr.getScrollId()).setScroll(new TimeValue(6000000)).get();
            if (sr.getHits().getHits().length == 0) break;
        }
    }

    @Override
    protected void onStop() {
        counterMetrics.stop();
        session.close();
    }

    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        AvatarUpdating au = new AvatarUpdating(configuration.toSubProperties("update_avatar"));
        au.start();
    }
}
