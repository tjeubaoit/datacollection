package com.datacollection.collect.model;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for profiles
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class GraphModel {

    private final List<Profile> profiles = new ArrayList<>();

    /**
     * Thêm một Profile vào GraphModel
     *
     * @param profile Profile object cần thêm
     * @return object GraphModel hiện tại, dùng để add thêm Profile mới nhanh hơn
     */
    public GraphModel addProfile(Profile profile) {
        // Profile thêm vào GraphModel cần phải đảm bảo các điều kiện là có log
        // và có ít nhất một trusted entity hoặc có ID được khởi tạo trước
        Preconditions.checkNotNull(profile.logAction(), "Profile must have log");
        if (Strings.isNullOrEmpty(profile.id())) {
            Preconditions.checkArgument(profile.trustedEntities().size() > 0,
                    "Profile must have at least one trusted entity");
        }

        this.profiles.add(profile);
        return this;
    }

    /**
     * @return Danh sách các Profiles đã được thêm vào GraphModel
     */
    public List<Profile> profiles() {
        return this.profiles;
    }
}
