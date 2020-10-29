package com.datacollection.core.collect.filter;

import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import com.datacollection.core.collect.model.GraphModel;
import com.datacollection.core.collect.model.Profile;
import com.datacollection.common.utils.Strings;
import com.datacollection.common.utils.Utils;

import java.io.IOException;
import java.util.List;

/**
 * Bộ lọc của Collcector dùng để loại bỏ các bài viết không phải
 * ngôn ngữ Tiếng Việt. Hàm accept() trả về true cho các bài viết
 * là tiếng Việt hoặc không thể xác định được ngôn ngữ và false
 * nếu xác định được là một ngôn ngữ khác.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class DetectLanguageFilter implements CollectFilter {

    private final LanguageDetector languageDetector;

    public DetectLanguageFilter() {
        try {
            //load all languages:
            List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();

            //build language detector:
            languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                    .withProfiles(languageProfiles)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean accept(GraphModel gm) {
        for (Profile profile : gm.profiles()) {
            String content = profile.log().content();
            if (Strings.isNullOrEmpty(content)) continue;

            //create a text object factory
            TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();

            //query:
            TextObject textObject = textObjectFactory.forText(content);
            LdLocale ldLocale = languageDetector.detect(textObject).orNull();

            if (ldLocale != null && Utils.notEquals(ldLocale.getLanguage(), "vi")) return false;
        }
        return true;
    }
}
