package com.datacollection.collect.model;

import com.datacollection.collect.Constants;
import com.datacollection.common.FacebookClient;
import com.datacollection.common.utils.Hashings;
import com.datacollection.common.utils.Maps;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Biểu diễn cho một đối tượng Photo trong đồ thị
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class Photo extends BaseEntity {

    /**
     * Tạo mới đối tượng Photo từ URL
     *
     * @param url URL của photo
     */
    public Photo(String url) {
        this(url, Collections.emptyMap());
    }

    /**
     * Tạo mới đối tượng Photo từ URL và các thuộc tính
     *
     * @param url       URL của photo
     * @param keyValues mảng các thuộc tính của Photo, mảng phải có độ dài là một
     *                  số chẵn và tuân theo quy tắc <i>key_1</i>, <i>value_1</i>,
     *                  <i>key_2</i>, <i>value_2</i>... <i>key_n</i>, <i>value_n</i>
     */
    public Photo(String url, Object... keyValues) {
        this(url, Maps.initFromKeyValues(keyValues));
    }

    /**
     * @param url        URL của photo
     * @param properties Map object represent properties of Photo
     */
    public Photo(String url, Map<String, Object> properties) {
        super("", Constants.PHOTO, properties);
        this.putProperty("url", url);
        this.putProperty("_ts", System.currentTimeMillis());

        String domain = properties.get("domain").toString();
        String id = Constants.FACEBOOK.equals(domain) ? extractIdentifiedInfo(url) : url;
        this.setId(Hashings.sha1AsBase64(id, false));
    }

    /**
     * <p>Các URL tới từ Facebook thông thường chứa một vài giá trị thường xuyên
     * thay đổi và không đại diện bức ảnh (vd như date params...) nên chỉ cần lấy
     * phần thông tin có giá trị để làm ID của đối tượng ảnh.</p>
     *
     * @param originUrl URL gốc của bức ảnh trên Facebook
     * @return tên hoặc phần thông tin không thay đổi định danh bức ảnh
     */
    private static String extractIdentifiedInfo(String originUrl) {
        try {
            int end = originUrl.indexOf("?");
            String shortUrl = originUrl.substring(0, end);
            return shortUrl.substring(shortUrl.lastIndexOf("/") + 1, end);
        } catch (Exception e) {
            return originUrl;
        }
    }

    public static void main(String[] args) throws IOException {
        int[] size = new int[]{24, 32, 40, 60, 80, 100, 160, 200, 240, 480, 720, 7200};
        for (int s : size) {
            String url = FacebookClient.fetchAvatarUrl("1299092363551733", s, s);
            System.out.println(s + ": " + extractIdentifiedInfo(url));
        }
    }
}
