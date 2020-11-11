package com.datacollection.collect.model;

import com.datacollection.common.utils.Hashings;
import com.datacollection.common.utils.Maps;

import java.util.Collections;
import java.util.Map;

/**
 * Biểu diễn cho một đối tượng Log, Log không được lưư trữ trong
 * đồ thị mà sẽ được lưu trữ ở một nơi riêng quản lý bởi LogStorage
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class Log extends BaseEntity {

    public static final String TYPE = "_type";
    public static final String SOURCE = "_src";
    public static final String URL = "_url";

    /**
     * Khởi tạo đối tượng Log
     *
     * @param type   kiểu của dữ liệu, thông thường chính là type của raw data từ trong
     *               GenericModel. Type cho biết dữ liệu được <b>lấy về</b> theo cách
     *               nào (nguồn crawler, hoặc provider...). Ví dụ type=fb.group.cmt cho
     *               biết đây là dữ liệu fbgroup comment được lấy bởi graphApiFetcher,
     *               type=fr.cmt cho biết đây là dữ liệu comment trên forum được lấy bởi
     *               forum crawlers...
     * @param source nguồn dữ liệu, cho biết nơi mà dữ liệu được <b>tạo ra</b>. Khác với
     *               type cho biết cách mà dữ liệu được lấy về theo cách nào, source cho
     *               biết nơi mà dữ liệu được sinh ra. Ví dụ về các nguồn là: ecommerce,
     *               vietid, fb.com, tinhte.vn, excel... Source đôi khi có thể trùng với
     *               type nhưng phần lớn thì sẽ khác, đặc biệt với dữ liệu forum đều có
     *               chung type là fr.cmt hoặc fr.art nhưng source sẽ là domain của các forum
     * @param url    URL để định danh dữ liệu, có thể bỏ qua
     */
    public Log(String type, String source, String url) {
        this(type, source, url, Collections.emptyMap());
    }

    /**
     * Khởi tạo đối tượng Log
     *
     * @param type      kiểu của dữ liệu, thông thường chính là type của raw data từ trong
     *                  GenericModel. Type cho biết dữ liệu được <b>lấy về</b> theo cách
     *                  nào (nguồn crawler, hoặc provider...). Ví dụ type=fb.group.cmt cho
     *                  biết đây là dữ liệu fbgroup comment được lấy bởi graphApiFetcher,
     *                  type=fr.cmt cho biết đây là dữ liệu comment trên forum được lấy bởi
     *                  forum crawlers...
     * @param source    nguồn dữ liệu, cho biết nơi mà dữ liệu được <b>tạo ra</b>. Khác với
     *                  type cho biết cách mà dữ liệu được lấy về theo cách nào, source cho
     *                  biết nơi mà dữ liệu được sinh ra. Ví dụ về các nguồn là: ecommerce,
     *                  vietid, fb.com, tinhte.vn, excel... Source đôi khi có thể trùng với
     *                  type nhưng phần lớn thì sẽ khác, đặc biệt với dữ liệu forum đều có
     *                  chung type là fr.cmt hoặc fr.art nhưng source sẽ là domain của các forum
     * @param url       URL để định danh dữ liệu, có thể bỏ qua
     * @param keyValues các thuộc tính bổ sung cho đối tượng Log, dưới dạng key-values theo
     *                  quy tắc key_1, value_1, key_2, value_2... key_n, value_n
     */
    public Log(String type, String source, String url, Object... keyValues) {
        this(type, source, url, Maps.initFromKeyValues(keyValues));
    }

    /**
     * Khởi tạo đối tượng Log
     *
     * @param type       kiểu của dữ liệu, thông thường chính là type của raw data từ trong
     *                   GenericModel. Type cho biết dữ liệu được <b>lấy về</b> theo cách
     *                   nào (nguồn crawler, hoặc provider...). Ví dụ type=fb.group.cmt cho
     *                   biết đây là dữ liệu fbgroup comment được lấy bởi graphApiFetcher,
     *                   type=fr.cmt cho biết đây là dữ liệu comment trên forum được lấy bởi
     *                   forum crawlers...
     * @param source     nguồn dữ liệu, cho biết nơi mà dữ liệu được <b>tạo ra</b>. Khác với
     *                   type cho biết cách mà dữ liệu được lấy về theo cách nào, source cho
     *                   biết nơi mà dữ liệu được sinh ra. Ví dụ về các nguồn là: ecommerce,
     *                   vietid, fb.com, tinhte.vn, excel... Source đôi khi có thể trùng với
     *                   type nhưng phần lớn thì sẽ khác, đặc biệt với dữ liệu forum đều có
     *                   chung type là fr.cmt hoặc fr.art nhưng source sẽ là domain của các forum
     * @param url        URL để định danh dữ liệu, có thể bỏ qua
     * @param properties object Map chứa các thuộc tính bổ sung cho đối tượng Log
     */
    public Log(String type, String source, String url, Map<String, Object> properties) {
        super(Hashings.sha1AsBase64(url, false), null, properties);
        this.putProperty(TYPE, type);
        this.putProperty(SOURCE, source);
        this.putProperty(URL, url);
    }

    /**
     * @return Kiểu của object Log
     */
    public String type() {
        return this.property(TYPE).toString();
    }

    /**
     * @return Nguồn dữ liệu của object Log
     */
    public String source() {
        return this.property(SOURCE).toString();
    }

    /**
     * @return URL định danh cho Log
     */
    public String url() {
        return this.property(URL).toString();
    }

    /**
     * @return Giá trị của thuộc tính với key="content" của Log
     */
    public String content() {
        return this.properties().getOrDefault("content", "").toString();
    }
}
