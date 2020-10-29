package com.datacollection.core.transform;

import com.datacollection.core.collect.model.Entity;
import com.datacollection.core.collect.model.GraphModel;
import com.datacollection.core.collect.model.Log;
import com.datacollection.core.collect.model.Profile;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.core.extract.model.GenericModel;

import java.util.List;
import java.util.Map;

public class ExcelTransformer implements DataTransformer {

    private final ProfileRegexHelper regexHelper = ProfileRegexHelper.getDefault();

    @Override
    public GraphModel transform(GenericModel generic) {

        GraphModel graphModel = new GraphModel();

        Map<String, Object> post = generic.getPost();

        Profile profile = new Profile(Profile.TYPE_PERSON);

        Log log = new Log(generic.getType(),"excel",generic.getId());

        for (Map.Entry<String,Object> map : post.entrySet()){
            String key = map.getKey();
            List<String> lstValue = (List<String>) map.getValue();

            if(key.equals("email")) {
                lstValue.forEach(value -> {
                    if (regexHelper.isEmail(value)) {
                        Entity entity = new Entity(value, "email");
                        profile.addTrustedEntity(entity);
                    }
                });
            }else if (key.equals("phone")){
                lstValue.forEach(value -> {
                    if (regexHelper.isPhone(value)){
                        Entity entity = new Entity(value, "phone");
                        profile.addTrustedEntity(entity);
                    }
                });
            }else {
                lstValue.forEach(value -> {
                    Entity entity = new Entity(value, key);
                    profile.addAnonymousEntity(entity);
                });
            }

        }

        log.putProperties(post);
        profile.setLog(log);
        graphModel.addProfile(profile);

        return graphModel;
    }
}
