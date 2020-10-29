package com.datacollection.core.collect;

/**
 * Chứa một vài các hằng số dùng trong quá trình collect dữ liệu
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface Constants {

    String ERROR_LOG_FILE = "WriteError.log";

    /**
     * Prefix cho một số tên cột của HBase ở mức low-level
     */
    String SYSTEM_PREFIX = "__";
    /**
     * Prefix cho một số tên thuộc tính trong Graph được coi là ẩn ở mức logic
     */
    String HIDDEN_PREFIX = "_";
    /**
     * Dùng để phân chia các phần trong trường hợp tên thuộc tính, ID của các
     * thành phần trong Graph là sự kết hợp của nhiều thành phần khác nhau
     */
    String PART_DELIMITER = "-";

    /**
     * Action mặc định của log khi lưu trữ
     */
    String LOG = "_log";
    /**
     * Mối quan hệ giữa một profile và facebook page/group khi profile có một
     * bài post trên fanpage/group đó
     */
    String POST = "_post";
    /**
     * Mối quan hệ giữa một profile và facebook page/group khi profile có một
     * comment trên fanpage/group đó
     */
    String COMMENT = "_cmt";

    String PROFILE = "profile";
    String ACCOUNT = "account";
    String FACEBOOK = "fb.com";
    String PHOTO = "photo";
    String FB_ZOMBIE = "fb.zombie";
    String AVATAR = "avatar";
}
