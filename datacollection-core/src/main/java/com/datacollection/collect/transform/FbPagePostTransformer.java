package com.datacollection.collect.transform;

import com.google.common.base.Preconditions;
import com.datacollection.collect.Constants;
import com.datacollection.collect.model.Entity;
import com.datacollection.collect.model.FbPage;
import com.datacollection.collect.model.GraphModel;
import com.datacollection.collect.model.Log;
import com.datacollection.collect.model.Profile;
import com.datacollection.collect.model.Relationship;
import com.datacollection.common.FacebookHelper;
import com.datacollection.common.TextUtils;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.utils.Strings;
import com.datacollection.extract.model.GenericModel;

import java.util.Map;
import java.util.Set;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class FbPagePostTransformer implements DataTransformer {

    private static final ProfileRegexHelper regexHelper = ProfileRegexHelper.getDefault();

    @Override
    public GraphModel transform(GenericModel generic) {
        Map<String, Object> post = generic.getPost();
        String content = post.get("Content").toString();

        String pageId = TextUtils.parsePageId(post.get("PostFbId").toString());
        String fromId = post.getOrDefault("FromId", pageId).toString();

        Preconditions.checkNotNull(fromId, "user_id must not be null");
        Preconditions.checkNotNull(pageId, "page_id must not be null");

        boolean postByAdmin = fromId.equalsIgnoreCase(pageId);
        Set<String> emails = regexHelper.extractEmails(content);
        Set<String> phones = regexHelper.extractPhones(content);

        // ignore posts that posted by page admin and does not contains any phones or emails
        if (emails.isEmpty() && phones.isEmpty() && postByAdmin) {
            throw new TransformException("Invalid doc, require at least one trusted entity");
        }

        String fromName = post.getOrDefault("FromName", "").toString();
        if (fromName.length() > 150) fromName = Strings.firstCharacters(fromName, 150);

        Profile profile = postByAdmin ? new FbPage(fromId) : new Profile(Profile.TYPE_PERSON);
        if (postByAdmin) {
            profile.putProperty("name", fromName);
        } else {
            Entity account = new Entity(fromId, Constants.FACEBOOK, "name", fromName);
            profile.addTrustedEntity(Relationship.forName("account"), account);

            FbPage fbpage = new FbPage(pageId, "name", post.get("ToName"));
            profile.addAnonymousEntity(Relationship.forName(Constants.POST), fbpage);
        }

        // add email/phone as untrusted info
        phones.forEach(phone -> profile.addUntrustedEntity(new Entity(phone, "phone")));
        emails.forEach(email -> profile.addUntrustedEntity(new Entity(email, "email")));

        // add log
        Log log = new Log(generic.getType(), Constants.FACEBOOK,
                FacebookHelper.FACEBOOK_ENDPOINT + post.get("PostFbId").toString());
        log.putProperty("pdt", post.get("CreateTime"));
        log.putProperty("content", content);
        log.putProperty("dbid", generic.getId());
        log.putProperty("author", fromId);
        log.putProperty("fbpid", pageId);
        log.putProperty("fbpname", postByAdmin ? post.get("FromName") : post.get("ToName"));

        profile.setLog(Constants.POST, log);

        return new GraphModel().addProfile(profile);
    }
}
