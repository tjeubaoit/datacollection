package com.datacollection.collect.filter;

import com.datacollection.collect.model.GraphModel;
import com.datacollection.collect.model.Profile;
import com.datacollection.common.utils.Strings;

import java.util.LinkedList;
import java.util.List;

/**
 * Bộ lọc của Collcector dùng để loại bỏ các bài viết được xác
 * định là spam như bài viết về đăng bán sim, bài viết chứa quá
 * nhiều số điện thoại...
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class DetectSpamFilter implements CollectFilter {

    private static final String[] KEYWORDS = {
            "số đẹp", "sim", "số tứ quý", "số năm sinh",
            "so dep", "so tu quy", "so nam sinh"
    };

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean accept(GraphModel gm) {
        for (Profile profile : gm.profiles()) {
            List<String> phones = new LinkedList<>();
            for (Profile.EntityRelationship e : profile.untrustedEntities()) {
                if (!"phone".equals(e.entity.label())) continue;
                phones.add(e.entity.id());
            }
            String content = profile.log().content();

            if (phones.size() > 2 && Strings.containsOnce(content.toLowerCase(), KEYWORDS)) return false;
            if (phones.size() > 4) return false;
        }

        return true;
    }
}
