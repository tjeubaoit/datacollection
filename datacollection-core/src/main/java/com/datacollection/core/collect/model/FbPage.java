package com.datacollection.core.collect.model;

import com.datacollection.common.utils.Maps;

import java.util.Collections;
import java.util.Map;

/**
 * Biểu diễn cho một đối tượng FbPage trong đồ thị
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class FbPage extends Profile {

    /**
     * Tạo một đối tượng FbPage mới từ ID
     *
     * @param id ID thật của Fanpage trên Facebook
     */
    public FbPage(String id) {
        this(id, Collections.emptyMap());
    }

    /**
     * Tạo một đối tượng FbPage mới từ ID và các thuộc tính
     *
     * @param id        ID thật của Fanpage trên Facebook
     * @param keyValues mảng các thuộc tính của FbPage, mảng phải có độ dài là một
     *                  số chẵn và tuân theo quy tắc <i>key_1</i>, <i>value_1</i>,
     *                  <i>key_2</i>, <i>value_2</i>... <i>key_n</i>, <i>value_n</i>
     */
    public FbPage(String id, Object... keyValues) {
        this(id, Maps.initFromKeyValues(keyValues));
    }

    /**
     * Tạo một đối tượng FbPage mới từ ID và các thuộc tính
     *
     * @param id         ID thật của Fanpage trên Facebook
     * @param properties Map object represent properties of FbPage
     */
    public FbPage(String id, Map<String, Object> properties) {
        super(TYPE_FBPAGE, id, properties);
    }

    @Override
    public String id() {
        // id gốc được prefix với fbpage_ để phân biệt
        return "fbpage_" + super.id();
    }
}
