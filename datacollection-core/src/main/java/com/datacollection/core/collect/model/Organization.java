package com.datacollection.core.collect.model;

import com.datacollection.common.utils.Maps;
import com.datacollection.common.utils.Strings;

import java.util.Collections;
import java.util.Map;

/**
 * Biểu diễn cho các đối tượng công ty, trường học, tổ chức trong đồ thị
 * Organization thực tế vẫn là một Profile của các đối tương không phải
 * là cá nhân (có type khác Person).
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class Organization extends Profile {

    /**
     * Tạo một đối tương Organization với type là TYPE_ORG
     *
     * @param name tên của Organization, sẽ được sử dụng làm ID trong graph
     */
    public Organization(String name) {
        this(predictTypeByName(name, Profile.TYPE_ORG), name);
    }

    /**
     * Tạo một đối tương Organization
     *
     * @param type kiểu của Organization, sẽ được sử dụng làm type của Profile
     * @param name tên của Organization, sẽ được sử dụng làm ID của Pròile
     */
    public Organization(String type, String name) {
        this(type, name, Collections.emptyMap());
    }

    /**
     * Tạo một đối tương Organization
     *
     * @param type      kiểu của Organization, sẽ được sử dụng làm type của Profile
     * @param name      tên của Organization, sẽ được sử dụng làm ID của Pròile
     * @param keyValues mảng các thuộc tính của Organization, mảng phải có độ dài là một
     *                  số chẵn và tuân theo quy tắc <i>key_1</i>, <i>value_1</i>,
     *                  <i>key_2</i>, <i>value_2</i>... <i>key_n</i>, <i>value_n</i>
     */
    public Organization(String type, String name, Object... keyValues) {
        this(type, name, Maps.initFromKeyValues(keyValues));
    }

    /**
     * Tạo một đối tương Organization
     *
     * @param type       kiểu của Organization, sẽ được sử dụng làm type của Profile
     * @param name       tên của Organization, sẽ được sử dụng làm ID của Pròile
     * @param properties Map object represent properties of Organization
     */
    public Organization(String type, String name, Map<String, Object> properties) {
        super(type, name, properties);
    }

    /**
     * Dự doán kiểu của Organization từ tên
     *
     * @param name    tên của Organization
     * @param defType kiểu mặc định của Organization, dùng để trả về kết quả
     *                nếu không đủ cơ sở để đưa ra dự đoán từ tên
     * @return kiểu của Organization dự đoán được hoặc defType nếu không đủ
     * cơ sở để dự đoán
     */
    public static String predictTypeByName(String name, String defType) {
        if (Strings.containsOnce(name, "tập đoàn", "tap doan", "công ty", "cty",
                "c.ty", "cong ty", "dntn", "tnhh", "cổ phần", "doanh nghiệp", "doanh nghiep"))
            return TYPE_COMPANY;
        else if (Strings.containsOnce(name, "trường mầm non", "trường tiểu học",
                "trường thcs", "trường thpt", "đại học", "dai hoc"))
            return TYPE_SCHOOL;
        return defType;
    }
}
