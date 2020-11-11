package com.datacollection.collect.transform;

import com.datacollection.collect.Constants;
import com.datacollection.collect.model.Entity;
import com.datacollection.collect.model.FbPage;
import com.datacollection.collect.model.GraphModel;
import com.datacollection.collect.model.Log;
import com.datacollection.collect.model.Profile;
import com.datacollection.collect.model.Relationship;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.utils.Strings;
import com.datacollection.extract.model.GenericModel;

import java.util.Map;
import java.util.Set;

public class HistoryTransformer implements DataTransformer {

    private ProfileRegexHelper regexHelper;

    public HistoryTransformer(){
        regexHelper = ProfileRegexHelper.getDefault();
    }
    @Override
    public GraphModel transform(GenericModel generic) {

        GraphModel graphModel = new GraphModel();
        //just do with facebook group or fanpage
        if (checkSkip(generic.getType())) return graphModel;

        Profile userProfile = new Profile(Profile.TYPE_PERSON);

        Map<String, Object> post = generic.getPost();
        Log log = new Log(generic.getType()+".history", Constants.FACEBOOK, post.get("Url").toString());
        log.putProperties(post);
        userProfile.setLog(log);


        if (generic.getType().equals("fb.group.post")) getFbAccountFromGroup(post, userProfile, Constants.POST);
        if (generic.getType().equals("fb.group.cmt")) getFbAccountFromGroup(post, userProfile, Constants.COMMENT);
        if (generic.getType().equals("fb.page.post")) getFbAccountFromPage(post, userProfile, Constants.POST);
        if (generic.getType().equals("fb.page.cmt")) getFbAccountFromPage(post, userProfile, Constants.COMMENT);

        getPhoneAndEmail(post, userProfile);

        graphModel.addProfile(userProfile);
        return graphModel;
    }

    public void getFbAccountFromGroup(Map<String, Object> post, Profile userProfile, String commentOrPost){

        if(Strings.isNullOrStringEmpty(post.get("user_id"))) return;

        Entity account = new Entity(post.get("user_id").toString(), Constants.FACEBOOK);
        userProfile.addTrustedEntity(Relationship.forName(Constants.ACCOUNT), account);
        if (!Strings.isNullOrStringEmpty(post.get("page_fbid"))) {
            userProfile.addAnonymousEntity(Relationship.forName(commentOrPost),
                    new Entity(post.get("page_fbid").toString(), "fbgroup"));
        }
    }

    public void getFbAccountFromPage(Map<String, Object> post, Profile userProfile, String commentOrPost){

        if (Strings.isNullOrStringEmpty(post.get("user_id"))) return;
        if (Strings.isNullOrStringEmpty(post.get("page_fbid"))) return;

        String userId = post.get("user_id").toString();
        String pageId = post.get("page_fbid").toString();

        boolean postByAdmin = userId.equalsIgnoreCase(pageId);

        userProfile = postByAdmin ? new FbPage(pageId) : userProfile;
        if (postByAdmin) {
            userProfile.putProperty("name", post.get("page_fbname"));
        } else {
            Entity account = new Entity(userId, Constants.FACEBOOK);
            userProfile.addTrustedEntity(Relationship.forName("account"), account);

            FbPage fbpage = new FbPage(pageId, "name", post.get("page_fbname"));
            userProfile.addAnonymousEntity(Relationship.forName(commentOrPost), fbpage);
        }
    }

    public void getPhoneAndEmail(Map<String, Object> post, Profile userProfile){
        if(!Strings.isNullOrStringEmpty(post.get("Content"))){
            String content = post.get("Content").toString();
            Set<String> phones = regexHelper.extractPhones(content);
            Set<String> emails = regexHelper.extractEmails(content);

            phones.forEach(phone -> {
                Entity entity = new Entity(phone, "phone");
                userProfile.addUntrustedEntity(entity);
            });

            emails.forEach(email ->{
                Entity entity = new Entity(email, "email");
                userProfile.addUntrustedEntity(entity);
            });
        }
    }

    public boolean checkSkip(String type){
        if (type.startsWith("fr")) return true;
        return  (type.startsWith("fb.profile"));
    }
}
