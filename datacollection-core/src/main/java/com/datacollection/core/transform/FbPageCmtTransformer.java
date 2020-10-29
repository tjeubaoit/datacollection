package com.datacollection.core.transform;

import com.google.common.base.Preconditions;
import com.datacollection.core.collect.Constants;
import com.datacollection.core.collect.model.Entity;
import com.datacollection.core.collect.model.FbPage;
import com.datacollection.core.collect.model.GraphModel;
import com.datacollection.core.collect.model.Log;
import com.datacollection.core.collect.model.Profile;
import com.datacollection.core.collect.model.Relationship;
import com.datacollection.common.FacebookHelper;
import com.datacollection.common.TextUtils;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.utils.Maps;
import com.datacollection.common.utils.Strings;
import com.datacollection.core.extract.model.GenericModel;

import java.util.Map;
import java.util.Set;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class FbPageCmtTransformer implements DataTransformer {

    private static final ProfileRegexHelper regexHelper = ProfileRegexHelper.getDefault();

    @Override
    public GraphModel transform(GenericModel generic) {
        Map<String, Object> post = generic.getPost();
        String content = post.get("Content").toString();

        String pageId = TextUtils.parsePageId(post.get("PostFbId").toString());
        String userId = Maps.getOrNull(post, "UserId");

        Preconditions.checkNotNull(userId, "user_id must not be null");
        Preconditions.checkNotNull(pageId, "page_id must not be null");

        boolean commentByAdmin = userId.equals(pageId);
        Set<String> emails = regexHelper.extractEmails(content);
        Set<String> phones = regexHelper.extractPhones(content);

        // ignore comments that posted by page admin and does not contains any phones or emails
        if (emails.isEmpty() && phones.isEmpty() && commentByAdmin) {
            throw new TransformException("Invalid doc, require at least one trusted entity");
        }

        String username = post.getOrDefault("Username", "").toString();
        if (username.length() > 150) username = Strings.firstCharacters(username, 150);

        Profile profile = commentByAdmin ? new FbPage(pageId) : new Profile(Profile.TYPE_PERSON);
        if (commentByAdmin) {
            profile.putProperty("name", username);
        } else {
            Entity account = new Entity(userId, Constants.FACEBOOK, "name", username);
            profile.addTrustedEntity(Relationship.forName("account"), account);

            FbPage fbpage = new FbPage(pageId);
            profile.addAnonymousEntity(Relationship.forName(Constants.COMMENT), fbpage);
        }

        // add email/phone as untrusted info
        phones.forEach(phone -> profile.addUntrustedEntity(new Entity(phone, "phone")));
        emails.forEach(email -> profile.addUntrustedEntity(new Entity(email, "email")));

        // add log
        Log log = new Log(generic.getType(), Constants.FACEBOOK,
                FacebookHelper.FACEBOOK_ENDPOINT + post.get("CommentFbId").toString());
        log.putProperty("pdt", post.get("CreateTime"));
        log.putProperty("content", content);
        log.putProperty("dbid", generic.getId());
        log.putProperty("author", userId);
        log.putProperty("postid", post.get("PostFbId"));
        log.putProperty("fbpid", pageId);

        profile.setLog(Constants.POST, log);

        return new GraphModel().addProfile(profile);
    }
}
