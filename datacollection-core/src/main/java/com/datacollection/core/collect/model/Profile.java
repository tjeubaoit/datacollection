package com.datacollection.core.collect.model;

import com.datacollection.core.collect.Constants;
import com.datacollection.common.utils.Maps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Đại diện cho đối tượng Profile trong Graph, đây là đối tượng trung
 * tâm chứa các mối quan hệ tới các entity, profile khác. Profile có thể
 * là person (người/cá nhân) hoặc company (công ty), school (trường học),
 * org (tổ chức chung), facebook fanpage...
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class Profile extends Entity {

    /**
     * Kiểu của Profile quy định một đối tượng là dạng cá nhân/người
     */
    public static final String TYPE_PERSON = "person";
    /**
     * Kiểu của Profile quy định một đối tượng là công ty
     */
    public static final String TYPE_COMPANY = "company";
    /**
     * Kiểu của Profile quy định một đối tượng là trường học
     */
    public static final String TYPE_SCHOOL = "school";
    /**
     * Kiểu của Profile quy định một đối tượng là tổ chức (bao gồm cả
     * công ty và trường học nhưng không thể xác định rõ ràng
     */
    public static final String TYPE_ORG = "org";
    /**
     * Kiểu của Profile quy định một đối tượng là một tổ chức dạng
     * không rõ ràng. Đây là các tổ chức lấy được từ các nguồn không chính
     * thống và không đáng tin như khai báo của user trên Facebook, LinkedIn...
     */
    public static final String TYPE_ORG_UNTRUST = "_org";
    /**
     * Kiểu của Profile quy định một đối tượng là một fanpage trên Facebook
     */
    public static final String TYPE_FBPAGE = "fbpage";
    /**
     * Kiểu của Profile quy định một đối tượng là dạng cá nhân/tổ chức sở
     * hữu một tài khoản trên Forum nào đó. Tuy nhiên đây là dạng tài khoản
     * không xác định được chính xác ID (ví dụ do crawler chỉ lấy được
     * tên hiển thị). Xem thêm các transformer của forum (ForumArtTransformer
     * và ForumCmtTransformer) để biết thêm chi tiết.
     */
    public static final String TYPE_FORUM_UNKNOWN = "frzb";
    /**
     * Kiểu của Profile quy định một đối tượng là một tài khoản Facebook
     * gắn với một per-app Facebook ID đã không còn hiệu lực (do ứng dụng
     * tương ứng với ID này trên Facebook đã bị xóa/vô hiệu) hoặc là tài
     * khoản không thể lấy được avatar nên không có cách nào để map nó về
     * tài khoản Facebook thực vì thế được gọi là dạng zombie.
     */
    public static final String TYPE_FB_ZOMBIE = "fbzb";

    private final List<EntityRelationship> untrusted = new ArrayList<>();
    private final List<EntityRelationship> trusted = new ArrayList<>();
    private final List<EntityRelationship> anonymous = new ArrayList<>();
    private String type;
    private LogAction logAction;

    /**
     * Tạo ra một object Profile mới với ID null và không có thuộc tính.
     * Profile sẽ được gán một ID mới (auto-gen) bởi Collector.
     *
     * @param type kiểu của Profile, được dịnh nghĩa bởi các constants phía trên
     */
    public Profile(String type) {
        this(type, null, Collections.emptyMap());
    }

    /**
     * Tạo ra một object Profile mới với ID được khởi tạo trước và không có
     * thuộc tính. Profile có ID được khởi tạo trước (vd company, school, org...)
     * sẽ không cần phải gen ID bởi Collector.
     *
     * @param type kiểu của Profile, được dịnh nghĩa bởi các constants phía trên
     * @param id   ID của Profile
     */
    public Profile(String type, String id) {
        this(type, id, Collections.emptyMap());
    }

    /**
     * Tạo ra một object Profile mới với ID và các thuộc tính được khởi tạo
     * trước. Profile có ID xác định trước (vd company, school, org...) sẽ
     * không cần phải gen ID bởi Collector.
     *
     * @param type      kiểu của Profile, được dịnh nghĩa bởi các constants phía trên
     * @param id        ID của Profile
     * @param keyValues các thuộc tính của Profile dưới dạng mảng có độ dài là số chẵn
     *                  và các giá trị theo quy tắc <i>key_1</i>, <i>value_1</i>,
     *                  <i>key_2</i>, <i>value_2</i>... <i>key_n</i>, <i>value_n</i>
     */
    public Profile(String type, String id, Object... keyValues) {
        this(type, id, Maps.initFromKeyValues(keyValues));
    }

    /**
     * Tạo ra một object Profile mới với ID và các thuộc tính được khởi tạo
     * trước. Profile có ID xác định trước (vd company, school, org...) sẽ
     * không cần phải gen ID bởi Collector.
     *
     * @param type       kiểu của Profile, được dịnh nghĩa bởi các constants phía trên
     * @param id         ID của Profile
     * @param properties các thuộc tính của Profile dưới dạng Map
     */
    public Profile(String type, String id, Map<String, Object> properties) {
        super(id, Constants.PROFILE, properties);
        this.setType(type);
        this.putProperty("tag", type);
        this.putProperty("_ts", System.currentTimeMillis());
    }

    /**
     * Thêm entity dạng untrusted (không tin tương) vào Profile
     *
     * @param relationship mối quan hệ giữa Profile và Entity
     * @param entity       Entity có quan hệ với Profile
     * @return Profile hiện tại, dùng để gọi tiếp các method của Profile nhanh hơn
     */
    public Profile addUntrustedEntity(Relationship relationship, BaseEntity entity) {
        untrusted.add(new EntityRelationship(relationship, entity));
        return this;
    }

    /**
     * Thêm entity dạng untrusted (không tin tương) vào Profile
     * với relationship được khởi tạo tự động từ Entity
     *
     * @param entity Entity có quan hệ với Profile
     * @return Profile hiện tại, dùng để gọi tiếp các method của Profile nhanh hơn
     */
    public Profile addUntrustedEntity(BaseEntity entity) {
        return addUntrustedEntity(new Relationship(entity.label()), entity);
    }

    /**
     * Thêm entity dạng trusted (tin tưởng) vào Profile
     *
     * @param relationship mối quan hệ giữa Profile và Entity
     * @param entity       Entity có quan hệ với Profile
     * @return Profile hiện tại, dùng để gọi tiếp các method của Profile nhanh hơn
     */
    public Profile addTrustedEntity(Relationship relationship, BaseEntity entity) {
        trusted.add(new EntityRelationship(relationship, entity));
        return this;
    }

    /**
     * Thêm entity dạng trusted (tin tưởng) vào Profile
     * với relationship được khởi tạo tự động từ Entity
     *
     * @param entity Entity có quan hệ với Profile
     * @return Profile hiện tại, dùng để gọi tiếp các method của Profile nhanh hơn
     */
    public Profile addTrustedEntity(BaseEntity entity) {
        return addTrustedEntity(new Relationship(entity.label()), entity);
    }

    /**
     * Thêm entity dạng anonymous vào Profile. Anonymous entity là các entity
     * không đủ để xác định duy nhất một Profile, tham khảo thêm trong wiki.
     *
     * @param relationship mối quan hệ giữa Profile và Entity
     * @param entity       Entity có quan hệ với Profile
     * @return Profile hiện tại, dùng để gọi tiếp các method của Profile nhanh hơn
     */
    public Profile addAnonymousEntity(Relationship relationship, BaseEntity entity) {
        anonymous.add(new EntityRelationship(relationship, entity));
        return this;
    }

    /**
     * Thêm entity dạng anonymous vào Profile với relationship được khởi tạo
     * tự động từ Entity. Anonymous entity là các entity không đủ để xác
     * định duy nhất một Profile, tham khảo thêm trong wiki.
     *
     * @param entity Entity có quan hệ với Profile
     * @return Profile hiện tại, dùng để gọi tiếp các method của Profile nhanh hơn
     */
    public Profile addAnonymousEntity(BaseEntity entity) {
        return addAnonymousEntity(new Relationship(entity.label()), entity);
    }

    /**
     * @return danh sách các entity dạng untrusted và mối quan hệ của chúng với Profile
     */
    public List<EntityRelationship> untrustedEntities() {
        return untrusted;
    }

    /**
     * @return danh sách các entity dạng trusted và mối quan hệ của chúng với Profile
     */
    public List<EntityRelationship> trustedEntities() {
        return trusted;
    }

    /**
     * @return danh sách các entity dạng anonymous và mối quan hệ của chúng với Profile
     */
    public List<EntityRelationship> anonymousEntities() {
        return anonymous;
    }

    /**
     * @return Kiểu của profile
     */
    public String type() {
        return type;
    }

    /**
     * Set kiểu của profile
     *
     * @param type kiểu của Profile cần set
     * @return Profile hiện tại, dùng để gọi tiếp các method của Profile nhanh hơn
     */
    public Profile setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Set log cho một Profile với action cho log mặc định là <i>_log</i>
     *
     * @param log object Log của Profile
     * @return Profile hiện tại, dùng để gọi tiếp các method của Profile nhanh hơn
     */
    public Profile setLog(Log log) {
        return setLog(Constants.LOG, log);
    }

    /**
     * Set log cho một Profile
     *
     * @param action action của log, chỉ ra mối quan hệ giữa Profile và Log,
     *               có thể là _log (mặc định) hay _post (log post bài của
     *               Profile), _view (log view bài viết)...
     * @param log    object Log của Profile
     * @return Profile hiện tại, dùng để gọi tiếp các method của Profile nhanh hơn
     */
    public Profile setLog(String action, Log log) {
        this.logAction = new LogAction(action, log);
        return this;
    }

    /**
     * @return Log and Action of Profile
     */
    public LogAction logAction() {
        return this.logAction;
    }

    /**
     * @return Log of Profile (without action)
     */
    public Log log() {
        return logAction != null ? logAction.log : null;
    }

    /**
     * Container class contains Log and Action
     */
    public static class LogAction {
        public final String action;
        public final Log log;

        LogAction(String action, Log log) {
            this.action = action;
            this.log = log;
        }
    }

    /**
     * Container class contains Entity and Relationship
     */
    public static class EntityRelationship {
        public final BaseEntity entity;
        public final Relationship relationship;

        EntityRelationship(Relationship relationship, BaseEntity entity) {
            this.entity = entity;
            this.relationship = relationship;
        }
    }
}
