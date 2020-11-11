package com.datacollection.collect.transform;

import com.google.common.base.Preconditions;
import com.datacollection.collect.model.Entity;
import com.datacollection.collect.model.GraphModel;
import com.datacollection.collect.model.Log;
import com.datacollection.collect.model.Profile;
import com.datacollection.collect.model.Relationship;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.utils.Strings;
import com.datacollection.extract.model.GenericModel;

import java.util.Collection;
import java.util.Map;

public class VietIdTransformer implements DataTransformer {

    private final ProfileRegexHelper regexHelper = ProfileRegexHelper.getDefault();

    @Override
    public GraphModel transform(GenericModel generic) {
        Map<String, Object> post = generic.getPost();
        Preconditions.checkNotNull(post);

        Log log = new Log(generic.getType(), "vietid", post.get("url").toString());
        log.putProperties(post);

        post.remove("status");
        post.remove("url");

        Profile profile = new Profile(Profile.TYPE_PERSON);
        profile.setLog(log);

        for (Map.Entry<String, Object> entry : post.entrySet()) {
            if (Strings.isNullOrStringEmpty(entry.getValue()) || "status".equals(entry.getValue())) continue;

            switch (entry.getKey()) {
                case "id": {
                    Entity entity = new Entity(entry.getValue().toString(), "vietid");
                    String username = post.getOrDefault("username", "").toString();
                    if (Strings.isNonEmpty(username)) {
                        entity.putProperty("username", username);
                    }
                    profile.addTrustedEntity(Relationship.forName("account"), entity);
                    break;
                }
                case "email": {
                    Collection<String> emails = regexHelper.extractEmails(entry.getValue().toString());
                    emails.forEach(email -> {
                        Entity entity = new Entity(email, "email");
                        profile.addTrustedEntity(entity);
                    });
                    break;
                }
                case "mobile": {
                    Collection<String> phones = regexHelper.extractPhones(entry.getValue().toString());
                    phones.forEach(phone -> {
                        Entity entity = new Entity(phone, "phone");
                        profile.addUntrustedEntity(entity);
                    });
                    break;
                }
                case "fullname": {
                    Entity entity = new Entity(entry.getValue().toString(), entry.getKey());
                    profile.addAnonymousEntity(entity);
                    break;
                }
                default: {
                    Entity entity = new Entity(entry.getValue().toString(), entry.getKey());
                    profile.addAnonymousEntity(entity);
                    break;
                }
            }
        }

        return new GraphModel().addProfile(profile);
    }
}
