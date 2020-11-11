package com.datacollection.collect.transform;

import com.datacollection.collect.model.Entity;
import com.datacollection.collect.model.GraphModel;
import com.datacollection.collect.model.Log;
import com.datacollection.collect.model.Profile;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.utils.Strings;
import com.datacollection.extract.model.GenericModel;

import java.util.List;
import java.util.Map;

public class ChatBotTransformer implements DataTransformer {

    private final ProfileRegexHelper regexHelper = ProfileRegexHelper.getDefault();

    @Override
    public GraphModel transform(GenericModel generic) {
        GraphModel graphModel = new GraphModel();

        Map<String, Object> post = generic.getPost();

        Log log = new Log(generic.getType(),"adchatbot",generic.getId());


        if (!Strings.isNullOrStringEmpty(post.get("api"))) {
            List<Map<String,Object>> lst = (List<Map<String, Object>>) post.get("api");
            for (Map<String,Object> m : lst){
                Profile profile = new Profile(Profile.TYPE_PERSON);

                Map<String,Object> contact = (Map<String, Object>) m.get("contact");
                String telephone = contact.get("telephone").toString();

                regexHelper.extractPhones(telephone).forEach(phone->{
                    Entity entity = new Entity(phone, "phone");
                    profile.addTrustedEntity(entity);
                });


                if(!Strings.isNullOrStringEmpty(m.get("name"))){
                    Entity entity = new Entity(m.get("name").toString(), "name");
                    profile.addAnonymousEntity(entity);
                }

                if(!Strings.isNullOrStringEmpty(m.get("facebook_id"))){
                    Entity entity = new Entity(m.get("facebook_id").toString(), "facebook_id");
                    profile.addAnonymousEntity(entity);
                }

                log.putProperties(post);
                profile.setLog(log);
                graphModel.addProfile(profile);
            }
        }
        return graphModel;
    }

}
