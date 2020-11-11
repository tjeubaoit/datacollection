package com.datacollection.collect.transform;

import com.datacollection.collect.Constants;
import com.datacollection.collect.model.Entity;
import com.datacollection.collect.model.GraphModel;
import com.datacollection.collect.model.Log;
import com.datacollection.collect.model.Profile;
import com.datacollection.collect.model.Relationship;
import com.datacollection.common.FacebookClient;
import com.datacollection.common.TextUtils;
import com.datacollection.common.utils.Pair;
import com.datacollection.common.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class TransformUtils {

    private static final Logger logger = LoggerFactory.getLogger(TransformUtils.class);

    public static void predictFacebookAccount(GraphModel gm, Profile srcProfile) {
        for (Profile.EntityRelationship e : srcProfile.trustedEntities()) {
            String type = e.entity.label();
            String nickname;
            if ("email".equals(type)) {
                nickname = e.entity.id().split("@")[0];
            } else if (TextUtils.isWebsite(type)) {
                nickname = e.entity.id();
            } else continue;

            try {
                String fbId = FacebookClient.fetchUserIdFromUsername_v2(nickname);
                if (fbId == null) continue;

                Pair<String, String> fbInfo = FacebookClient.fetchProfileInfo(fbId);
                if (fbInfo == null || !"user".equals(fbInfo.getKey())) continue;

                String fbName = fbInfo.getValue();
                addDuplicateFbAccount(gm, srcProfile, fbId, fbName, nickname);
            } catch (IOException ex) {
                logger.warn(ex.getMessage(), ex);
            }
        }
    }

    public static void addDuplicateFbAccount(GraphModel gm, Profile srcProfile,
                                             String fbId, String fbName, String nickname) {
        String score = Strings.format("%03f", Math.min(nickname.length(), 15) / 15F * 0.25F);
        Entity fbAcc = new Entity(fbId, Constants.FACEBOOK);
        fbAcc.putProperty("name", fbName);

        Profile fbProfile = new Profile(Profile.TYPE_PERSON);
        fbProfile.addTrustedEntity(Relationship.forName("account"), fbAcc);
        fbProfile.addAnonymousEntity(new Relationship("_duplicate", "score", score), srcProfile);

        srcProfile.addAnonymousEntity(new Relationship("_duplicate", "score", score), fbProfile);
        srcProfile.addAnonymousEntity(new Relationship("_account", "score", score), fbAcc);

        Log log = new Log("internal", "internal", "#");
        fbProfile.setLog(log);
        gm.addProfile(fbProfile);
    }
}
