package com.datacollection.core.transform;

import com.datacollection.core.collect.Constants;
import com.datacollection.core.collect.model.Entity;
import com.datacollection.core.collect.model.GraphModel;
import com.datacollection.core.collect.model.Log;
import com.datacollection.core.collect.model.Profile;
import com.datacollection.core.collect.model.Relationship;
import com.datacollection.common.TextUtils;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.utils.Hashings;
import com.datacollection.core.extract.model.GenericModel;

import java.util.Map;
import java.util.Set;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ForumArtTransformer implements DataTransformer {

    private static final ProfileRegexHelper regexHelper = ProfileRegexHelper.getDefault();

    @Override
    public GraphModel transform(GenericModel generic) {
        Map<String, Object> post = generic.getPost();

        String author = post.getOrDefault("Author", "").toString();
        String url = post.get("Url").toString();
        String domain = post.get("Domain").toString();
        String authorId = TextUtils.parseForumId(post.getOrDefault("AuthorLink", "").toString());
        String content = post.getOrDefault("Content", "").toString();

        Set<String> emails = regexHelper.extractEmails(content);
        Set<String> phones = regexHelper.extractPhones(content);

        Profile profile = new Profile(Profile.TYPE_PERSON);
        if (authorId != null) {
            profile.addTrustedEntity(Relationship.forName(Constants.ACCOUNT),
                    new Entity(authorId, domain, "name", author));

            String avatar = post.getOrDefault("PathAvatar", "").toString();
            if (!avatar.isEmpty()) {
                profile.addTrustedEntity(new Entity(avatar, Constants.PHOTO, "domain", domain));
            }
        } else if (TextUtils.detectNicknameForum(author)) {
            profile.addTrustedEntity(Relationship.forName(Constants.ACCOUNT), 
                new Entity(author, domain, "name", author));
        } else {
            if (phones.isEmpty() && emails.isEmpty())
                throw new TransformException("Invalid doc, no user_id or phone/email was extracted");

            // add anonymous authorId from url
            String id = "unknown_" + Hashings.sha1AsHex(url);
            profile.addTrustedEntity(Relationship.forName(Constants.ACCOUNT),
                    new Entity(id, domain, "name", author));
            profile.setType(Profile.TYPE_FORUM_UNKNOWN);
            profile.setId(id);
        }

        phones.forEach(phone -> profile.addUntrustedEntity(new Entity(phone, "phone")));
        emails.forEach(email -> profile.addUntrustedEntity(new Entity(email, "email")));

        Log log = new Log(generic.getType(), domain, url);
        log.putProperty("content", content);
        log.putProperty("title", post.get("Title"));
        log.putProperty("category", post.get("Category"));
        log.putProperty("pdt", post.get("PostDate"));
        log.putProperty("author", authorId != null ? authorId : author);
        log.putProperty("dbid", post.get("_id"));

        profile.setLog(Constants.POST, log);

        return new GraphModel().addProfile(profile);
    }
}
