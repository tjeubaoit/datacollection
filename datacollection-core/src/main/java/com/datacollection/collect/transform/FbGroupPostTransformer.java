package com.datacollection.collect.transform;

import com.google.common.base.Preconditions;
import com.datacollection.collect.Constants;
import com.datacollection.collect.model.Entity;
import com.datacollection.collect.model.GraphModel;
import com.datacollection.collect.model.Log;
import com.datacollection.collect.model.Profile;
import com.datacollection.collect.model.Relationship;
import com.datacollection.common.FacebookHelper;
import com.datacollection.common.TextUtils;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.utils.Maps;
import com.datacollection.common.utils.Strings;
import com.datacollection.extract.model.GenericModel;

import java.util.Map;
import java.util.Set;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class FbGroupPostTransformer implements DataTransformer {

    private static final ProfileRegexHelper regexHelper = ProfileRegexHelper.getDefault();

    @Override
    public GraphModel transform(GenericModel generic) {
        Map<String, Object> post = generic.getPost();
        String content = post.get("Content").toString();

        Set<String> emails = regexHelper.extractEmails(content);
        Set<String> phones = regexHelper.extractPhones(content);

        String fromId = Maps.getOrNull(post, "FromId");
        String pageId = post.containsKey("PostFbId")
                ? TextUtils.parsePageId(post.get("PostFbId").toString()) : null;

        Preconditions.checkNotNull(fromId, "user_id must not be null");

        String name = post.getOrDefault("FromName", "").toString();
        if (name.length() > 500) name = Strings.firstCharacters(name, 500);

        Profile profile = new Profile(Profile.TYPE_PERSON);

        Entity account = new Entity(fromId, Constants.FACEBOOK, "name", name);
        profile.addTrustedEntity(Relationship.forName(Constants.ACCOUNT), account);
        if (pageId != null) {
            profile.addAnonymousEntity(Relationship.forName(Constants.POST),
                    new Entity(pageId, "fbgroup"));
        }

        // add email/phone as untrusted info
        phones.forEach(phone -> profile.addUntrustedEntity(new Entity(phone, "phone")));
        emails.forEach(email -> profile.addUntrustedEntity(new Entity(email, "email")));

        Log log = new Log(generic.getType(), Constants.FACEBOOK,
                FacebookHelper.FACEBOOK_ENDPOINT + post.get("PostFbId").toString());
        log.putProperty("pdt", post.get("CreateTime"));
        log.putProperty("content", content);
        log.putProperty("dbid", generic.getId());
        log.putProperty("author", fromId);
        log.putProperty("fbpid", pageId);
        log.putProperty("fbpname", Maps.getOrNull(post, "ToName"));

        profile.setLog(Constants.POST, log);

        return new GraphModel().addProfile(profile);
    }
}
