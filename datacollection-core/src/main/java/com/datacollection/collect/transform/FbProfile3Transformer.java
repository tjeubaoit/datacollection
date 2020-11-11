package com.datacollection.collect.transform;

import com.facebook.presto.jdbc.internal.jackson.databind.ObjectMapper;
import com.datacollection.collect.Constants;
import com.datacollection.collect.OrgSearcher;
import com.datacollection.collect.model.*;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.utils.Pair;
import com.datacollection.common.utils.Strings;
import com.datacollection.extract.model.GenericModel;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FbProfile3Transformer implements DataTransformer {

    private final ProfileRegexHelper regexHelper = ProfileRegexHelper.getDefault();
    private final OrgSearcher orgSearcher = OrgSearcher.getOurInstance();

    @Override
    public GraphModel transform(GenericModel generic) {

        GraphModel graphModel = new GraphModel();

        Map<String, Object> post = generic.getPost();
        Log log = new Log(generic.getType(), Constants.FACEBOOK, post.get("_id").toString());
        log.putProperties(post);

        Profile userProfile = new Profile(Profile.TYPE_PERSON);
        userProfile.setLog(Constants.POST, log);

        Entity fbEntity = new Entity(post.get("_id").toString(), "fb.com");
        fbEntity.putProperty("name", post.getOrDefault("profileName", ""));
        userProfile.addTrustedEntity(new Relationship("account"), fbEntity);

        if(!Strings.isNullOrStringEmpty(post.get("email"))){
            Collection<String> emails = regexHelper.extractPhones(post.get("email").toString());
            emails.forEach(email -> {
                Entity entity = new Entity(email, "email");
                userProfile.addTrustedEntity(entity);
            });
        }

        if(!Strings.isNullOrStringEmpty(post.get("phone"))){
            Collection<String> phones = regexHelper.extractPhones(post.get("phone").toString());
            phones.forEach(phone -> {
                Entity entity = new Entity(phone, "phone");
                userProfile.addTrustedEntity(entity);
            });
        }

        if(!Strings.isNullOrStringEmpty(post.get("birthday"))){
            Entity entity = new Entity(transformBirthday(post.get("birthday").toString()), "birthday");
            userProfile.addAnonymousEntity(entity);
        }

        if(!Strings.isNullOrStringEmpty(post.get("gender"))){
            userProfile.addAnonymousEntity(new Gender(transformGender(post.get("gender").toString())));
        }

        if(!Strings.isNullOrStringEmpty(post.get("interestedIn"))){
            //????
//            addAnonymousEntity
        }

        if(!Strings.isNullOrStringEmpty(post.get("hometown"))){
            Map<String,Object> map = (Map<String, Object>) post.get("hometown");
            Entity hometown = new Entity(map.get("name").toString(), "location");
            userProfile.addAnonymousEntity(new Relationship("hometown"), hometown);
        }

        if(!Strings.isNullOrStringEmpty(post.get("location"))){
            Entity currentCity = new Entity(post.get("location").toString(), "location");
            userProfile.addAnonymousEntity(new Relationship("current_city"), currentCity);
        }

        if(!Strings.isNullOrStringEmpty(post.get("education"))){
            List<Map<String,Object>> listEducation = (List<Map<String, Object>>) post.get("education");
            for (Map<String,Object> education : listEducation){
                Map<String,Object> school = (Map<String, Object>) education.get("school");


                if(school.get("id") != null){
                    String schoolId = school.get("id").toString();
                    Collection<Pair<String, String>> schoolNames = orgSearcher.matchOrg(schoolId);

                    Organization schoolCrawl = new Organization(Profile.TYPE_ORG_UNTRUST, schoolId);

                    userProfile.addAnonymousEntity(Relationship.forName("school"), schoolCrawl);

                    if (!schoolNames.isEmpty()) {
                        for (Pair<String, String> name : schoolNames) {
                            if (name.getKey().equals(schoolId)){
                                schoolCrawl.setLabel(Profile.TYPE_ORG);
                                break;
                            }
                            Organization orgStandard = new Organization(name.getKey());
                            Relationship orgRelationship = Relationship.forName("looklike");
                            orgRelationship.putProperties(Collections.singletonMap("score", name.getValue()));
                            schoolCrawl.addAnonymousEntity(orgRelationship, orgStandard);
                        }
                    }
                    schoolCrawl.setLog(log);
                    graphModel.addProfile(schoolCrawl);

                }
            }
        }

        if(!Strings.isNullOrStringEmpty(post.get("work"))){
            List<Map<String,Object>> listWork = (List<Map<String, Object>>) post.get("work");
            for (Map<String,Object> work : listWork){
                Map<String,Object> employer = (Map<String, Object>) work.get("employer");
                Map<String,Object> position = (Map<String, Object>) work.get("position");

                String rsPosition = "";

                if(position.get("name") != null){
                    rsPosition = position.get("name").toString();
                }

                if(employer.get("name") != null){
                    String orgId = employer.get("name").toString();
                    Collection<Pair<String, String>> orgNames = orgSearcher.matchOrg(orgId);
                    Organization orgCrawl = new Organization(Profile.TYPE_ORG_UNTRUST, orgId);
                    userProfile.addAnonymousEntity(new Relationship("job", "position",
                            rsPosition), orgCrawl);

                    if (!orgNames.isEmpty()) {
                        for (Pair<String, String> org: orgNames){
                            if (org.getKey().equals(orgId)){
                                orgCrawl.setLabel(Profile.TYPE_ORG);
                                break;
                            }
                            Organization orgStandard = new Organization(org.getKey());
                            Relationship orgRelationship = Relationship.forName("looklike");
                            orgRelationship.putProperties(Collections.singletonMap("score", org.getValue()));
                            orgCrawl.addAnonymousEntity(orgRelationship, orgStandard);
                        }
                    }

                    orgCrawl.setLog(log);
                    graphModel.addProfile(orgCrawl);
                }
            }
        }
        return graphModel;
    }

    private String transformGender(String gender) {
        if (gender.toLowerCase().equals("male")) return Gender.MALE;
        else if (gender.toLowerCase().equals("female")) return Gender.FEMALE;

        return null;
    }

    private String transformBirthday(String birthday) {
        DateFormat df1 = new SimpleDateFormat("MMMMMMMMMM dd, yyyy");
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date1 = df1.parse(birthday);
            return df2.format(date1);
        } catch (ParseException e) {
            return birthday;
        }
    }



    public static void main(String[] args) {
//        Extractor extractor = new FbProfile3Extractor(new Configuration());
//        TransformTester transformTester = new TransformTester(new FbProfile3Transformer());

        FbProfile3Transformer fbProfile3Transformer = new FbProfile3Transformer();
        String jsonSource = "{\"_id\":\"100005760886636\",\"profileId\":\"100005760886636\",\"link\":\"https://www.facebook.com/victorvu.x3\",\"profileName\":\"Victor Vũ\",\"picture\":\"https://scontent.xx.fbcdn.net/v/t1.0-0/p480x480/28166820_810246295844059_8058380985950823406_n.jpg?_nc_cat=0&_nc_ad=z-m&_nc_cid=0&oh=bf58fe10abb26adc00082a32e04d465e&oe=5B98DB3C\",\"pictureId\":\"810246295844059\",\"email\":\"\",\"mobilePhone\":\"\",\"gender\":\"male\",\"locale\":\"vi_VN\",\"education\":[{\"id\":\"316775241857836\",\"type\":\"High School\",\"school\":{\"id\":\"Trường THPT Lý Nhân\",\"name\":\"150005148441825\"}}],\"work\":[{\"id\":\"490828261119199\",\"employer\":{\"id\":\"291555710971943\",\"name\":\"FIFA\"},\"location\":{\"id\":\"102160693158562\",\"name\":\"Zürich, Switzerland\"}}],\"installed\":false,\"installType\":\"UNKNOWN\",\"isVerified\":false,\"thirdPartyId\":\"Yn33SX7tYIrMwOEWHNe3_trzTlY\",\"timezone\":0.0,\"viewerCanSendGift\":false,\"fetchFriends\":false,\"fetchSubscribers\":false,\"fetchSubscribedto\":false,\"totalFriends\":0,\"totalSubscribedto\":0,\"totalSubscribers\":0,\"dateCreated\":1524842912778,\"modifiedTime\":1524842912778}";
        Map<String,Object> map = null;
        try {
            map = new ObjectMapper().readValue(jsonSource,HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fbProfile3Transformer.transform(new GenericModel("1522122649913_danh_ba_cqtchai_quan.xlsx_0_256","excel",map));


    }
}
