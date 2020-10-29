package com.datacollection.core.collect.model;

import com.google.common.base.Preconditions;

/**
 * Biểu diễn cho một đối tượng Gender trong đồ thị. Cho phép validate
 * để bảo đảm Gender chỉ chứa 2 gía trị chuẩn là m (male) và f (female).
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class Gender extends Entity {

    public static final String MALE = "m";
    public static final String FEMALE = "f";

    /**
     * Tạo một đối tượng Gender mới
     *
     * @param id phải là m (male) hoặc f (female)
     */
    public Gender(String id) {
        super(null, "gender");
        Preconditions.checkArgument(MALE.equals(id) || FEMALE.equals(id),
                "Gender must only be m (male) or f (female)");
        setId(id);
    }
}
