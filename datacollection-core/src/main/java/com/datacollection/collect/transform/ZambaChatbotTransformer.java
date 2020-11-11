package com.datacollection.collect.transform;

import com.datacollection.collect.Constants;
import com.datacollection.collect.model.Entity;
import com.datacollection.collect.model.FbPage;
import com.datacollection.collect.model.Gender;
import com.datacollection.collect.model.GraphModel;
import com.datacollection.collect.model.Log;
import com.datacollection.collect.model.Profile;
import com.datacollection.collect.model.Relationship;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.utils.Strings;
import com.datacollection.extract.model.GenericModel;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class ZambaChatbotTransformer implements DataTransformer {
    private ProfileRegexHelper regexHelper;

    public ZambaChatbotTransformer() {
        regexHelper = ProfileRegexHelper.getDefault();
    }

    @Override
    public GraphModel transform(GenericModel generic) {
        GraphModel graphModel = new GraphModel();

        Profile userProfile = new Profile(Profile.TYPE_PERSON);
        Map<String, Object> post = generic.getPost();

        getPhoneAndEmail(post, userProfile);
        getAccount(post, userProfile);
        getGender(post, userProfile);

        Log log = new Log(generic.getType(), Constants.FACEBOOK, "#");
        log.putProperties(post);
        userProfile.setLog(log);

        graphModel.addProfile(userProfile);
        return graphModel;
    }

    public void getPhoneAndEmail(Map<String, Object> post, Profile userProfile) {

        if (!Strings.isNullOrStringEmpty(post.get("phone"))) {
            Collection<String> phones = regexHelper.extractPhones(post.get("phone").toString());
            phones.forEach(phone -> {
                Entity entity = new Entity(phone, "phone");
                userProfile.addUntrustedEntity(entity);
            });
        }

        if (!Strings.isNullOrStringEmpty(post.get("email"))) {
            Collection<String> emails = regexHelper.extractEmails(post.get("email").toString());
            emails.forEach(email -> {
                Entity entity = new Entity(email, "email");
                userProfile.addUntrustedEntity(entity);
            });
        }
    }

    public void getAccount(Map<String, Object> post, Profile userProfile) {
        if (!Strings.isNullOrStringEmpty(post.get("facebook_id"))) {
            Entity fbEntity = new Entity(post.get("facebook_id").toString(), "fb.com");
            fbEntity.putProperty("name", post.get("name"));
            fbEntity.putProperty("cover", post.get("cover"));

            userProfile.addTrustedEntity(Relationship.forName("account"), fbEntity);
        }
//        else if (!Strings.isNullOrStringEmpty(post.get("facebook_id_fp"))){
//            Entity fbEntity = new Entity(post.get("facebook_id_fp").toString(), "fb.com");
//            fbEntity.putProperty("name", post.get("name"));
//            fbEntity.putProperty("cover", post.get("cover"));
//        }

        if (!Strings.isNullOrStringEmpty(post.get("fanpage_id"))){
            FbPage fbPage = new FbPage(post.get("fanpage_id").toString());
            userProfile.addAnonymousEntity(Relationship.forName("like"), fbPage);
        }
    }

    public void getGender(Map<String, Object> post, Profile userProfile) {
        String field = "gender";
        if (!Strings.isNullOrStringEmpty(post.get(field))) {
            String fieldValue = transformGender(post.get(field).toString());
            if (fieldValue != null) {
                Entity entity = new Entity(fieldValue, field);
                userProfile.addAnonymousEntity(entity);
            }
        }
    }

    @Nullable
    private String transformGender(String gender) {
        if (gender.equals("male")) return Gender.MALE;
        else if (gender.equals("female")) return Gender.FEMALE;

        return null;
    }
}