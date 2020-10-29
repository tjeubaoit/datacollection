package com.datacollection.core.transform;

import com.datacollection.core.collect.model.Entity;
import com.datacollection.core.collect.model.GraphModel;
import com.datacollection.core.collect.model.Log;
import com.datacollection.core.collect.model.Profile;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.core.extract.model.GenericModel;

import java.util.Map;

public class TrackFormTransformer implements DataTransformer{

    private final ProfileRegexHelper regexHelper = ProfileRegexHelper.getDefault();

    @Override
    public GraphModel transform(GenericModel generic) {
        GraphModel graphModel = new GraphModel();

        Map<String, Object> post = generic.getPost();

        Log log = new Log(generic.getType(),"ad-track-form",generic.getId());

        Profile profile = new Profile(Profile.TYPE_PERSON);

        if(post.get("log") != null){
            regexHelper.extractPhones(post.get("log").toString()).forEach(phone->{
                System.out.println();
                Entity entity = new Entity(phone, "phone");
                profile.addTrustedEntity(entity);
            });

            regexHelper.extractEmails(post.get("log").toString()).forEach(email->{
                Entity entity = new Entity(email, "email");
                profile.addTrustedEntity(entity);
            });
        }

        log.putProperties(post);
        profile.setLog(log);
        graphModel.addProfile(profile);
        return graphModel;
    }
}
