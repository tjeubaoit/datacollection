package com.datacollection.core.transform;

import com.datacollection.core.collect.Constants;
import com.datacollection.core.collect.model.Entity;
import com.datacollection.core.collect.model.GraphModel;
import com.datacollection.core.collect.model.Log;
import com.datacollection.core.collect.model.Profile;
import com.datacollection.core.collect.model.Relationship;
import com.datacollection.common.FacebookHelper;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.utils.NullProtector;
import com.datacollection.common.utils.Strings;
import com.datacollection.common.utils.Utils;
import com.datacollection.core.extract.model.GenericModel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ZambaTransformer implements DataTransformer {

    public final ProfileRegexHelper regexHelper = ProfileRegexHelper.getDefault();

    @Override
    public GraphModel transform(GenericModel generic) {
        Map<String, Object> post = generic.getPost();

        String domain = post.get("domain").toString();
        String userId = NullProtector.getOrNull(post, "user_id");
        GraphModel graphModel = new GraphModel();
        Profile userProfile = new Profile(Profile.TYPE_PERSON);

        this.getPhoneEmailEntity(userProfile, post);
        this.getUserInfoEntity(userProfile, post,
                Arrays.asList("name", "address", "location", "gender"), domain, userId);

        Log log = this.getLog(generic);
        if (log != null) {
            userProfile.setLog(Constants.POST, log);
        }
        graphModel.addProfile(userProfile);

        return graphModel;
    }

    private void getPhoneEmailEntity(Profile userProfile, Map<String, Object> post) {
        if (post.get("phone") != null) {
            this.extractPhones(post.get("phone").toString(), ",").forEach(phone -> {
                Entity entity = new Entity(phone, "phone");
                userProfile.addUntrustedEntity(entity);
            });
        }
        if (post.get("email") != null) {
            regexHelper.extractEmails(post.get("email").toString()).forEach(email -> {
                Entity entity = new Entity(email, "email");
                userProfile.addUntrustedEntity(entity);
            });
        }

        if (post.get("content") != null) {
            regexHelper.extractPhones(post.get("content").toString()).forEach(phone -> {
                Entity entity = new Entity(phone, "phone");
                userProfile.addUntrustedEntity(entity);
            });
            regexHelper.extractEmails(post.get("content").toString()).forEach(email -> {
                Entity entity = new Entity(email, "email");
                userProfile.addUntrustedEntity(entity);
            });
        }
    }

    private void getUserInfoEntity(Profile userProfile, Map<String, Object> post, List<String> keys,
                                   String domain, String userId) {

        for (String key : keys) {
            if (!Strings.isNullOrStringEmpty(post.get(key))) {
                Entity name = new Entity(post.get(key).toString(), key);
                userProfile.addAnonymousEntity(name);
            }
        }

        if (!Strings.isNullOrStringEmpty(post.get("facebook"))) {
            Entity facebook = new Entity(FacebookHelper.parseURLProfileToId(post.get("facebook").toString())
                    , Constants.FACEBOOK);
            userProfile.addTrustedEntity(new Relationship("account"), facebook);
        }

        if (!Strings.isNullOrStringEmpty(userId)) {
            Entity entity = new Entity(userId, domain);
            userProfile.addTrustedEntity(new Relationship("account"), entity);
        }

        if (!Strings.isNullOrStringEmpty(post.get("profile_url"))) {
            Entity entity = new Entity(post.get("profile_url").toString(), domain);
            userProfile.addTrustedEntity(new Relationship("account"), entity);
        }

        if (!Strings.isNullOrStringEmpty(post.get("birthday"))) {
            String bd = transformBirthday(post.get("birthday").toString());
            if (Utils.notEquals("0000-00-00", bd)) {
                Entity entity = new Entity(bd, "birthday");
                userProfile.addAnonymousEntity(entity);
            }
        }
    }

    private String transformBirthday(String birthday) {
        DateFormat df1 = new SimpleDateFormat("yyyy/MM/dd");
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date1 = df1.parse(birthday);
            return df2.format(date1);
        } catch (ParseException e) {
            return birthday;
        }
    }

    private Log getLog(GenericModel generic) {
        Map<String, Object> post = generic.getPost();
        if (post.get("url") == null || post.get("domain") == null) return null;

        Map<String, Object> props = new HashMap<>();
        props.put("content", post.get("content"));
        props.put("title", post.get("subject"));
        props.put("pdt", post.get("post_time"));
        props.put("author", post.get("user_id"));
        props.put("dbid", post.get(generic.getId()));

        return new Log(generic.getType(), post.get("domain").toString(), post.get("url").toString(), props);
    }

    private Set<String> extractPhones(String content, String splitCharacter) {
        String[] contentSplit = content.split(splitCharacter);
        Set<String> phones = new HashSet<>();
        for (String phone : contentSplit) {
            phone = phone.trim().replaceAll("[^0-9]", "");
            phone = phone.startsWith("0") ? phone : "0" + phone;
            phones.addAll(regexHelper.extractPhones(phone));
        }
        return phones;
    }
}
