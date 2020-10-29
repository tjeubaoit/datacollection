package com.datacollection.core.collect.model;

import com.datacollection.common.utils.Maps;
import com.datacollection.common.utils.Strings;

import java.util.Collections;
import java.util.Map;

/**
 * A BaseEntity with normalized id
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class Entity extends BaseEntity {

    /**
     * Tạo một Entity từ ID và nhãn (label)
     *
     * @param id    id of Entity
     * @param label label of Label
     */
    public Entity(String id, String label) {
        this(id, label, Collections.emptyMap());
    }

    /**
     * Tạo một Entity từ ID, label và các thuộc tính
     *
     * @param id        id of Entity
     * @param label     label of Label
     * @param keyValues mảng các thuộc tính của Entity, mảng phải có độ dài là một
     *                  số chẵn và tuân theo quy tắc <i>key_1</i>, <i>value_1</i>,
     *                  <i>key_2</i>, <i>value_2</i>... <i>key_n</i>, <i>value_n</i>
     */
    public Entity(String id, String label, Object... keyValues) {
        this(id, label, Maps.initFromKeyValues(keyValues));
    }

    /**
     * Tạo một Entity từ ID, label và các thuộc tính
     *
     * @param id         id of Entity
     * @param label      label of Label
     * @param properties Map object represent properties of Entity
     */
    public Entity(String id, String label, Map<String, Object> properties) {
        super(id, label, properties);
    }

    @Override
    public BaseEntity setId(String id) {
        if (Strings.isNonEmpty(id)) {
            // normalize id bằng cách loại bỏ các khoảng trắng, đưa về dạng viết
            // thường (lowercase) và loại bỏ kí tự "|" là kí tự dùng để phân tách
            // các thành phần khi lưu trữ graph trong HBase
            return super.setId(id.trim().toLowerCase().replace("|", ""));
        }
        return super.setId(id);
    }
}
