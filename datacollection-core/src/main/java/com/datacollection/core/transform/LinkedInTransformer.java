package com.datacollection.core.transform;

import com.datacollection.core.collect.OrgSearcher;
import com.datacollection.core.collect.Constants;
import com.datacollection.core.collect.model.Entity;
import com.datacollection.core.collect.model.GraphModel;
import com.datacollection.core.collect.model.Log;
import com.datacollection.core.collect.model.Organization;
import com.datacollection.core.collect.model.Profile;
import com.datacollection.core.collect.model.Relationship;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.utils.Pair;
import com.datacollection.common.utils.Strings;
import com.datacollection.core.extract.Extractor;
import com.datacollection.core.extract.model.GenericModel;
import com.datacollection.core.extract.mongo.LinkedInExtractor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LinkedInTransformer implements DataTransformer {

    private OrgSearcher orgSearcher;
    private final ProfileRegexHelper regexHelper = ProfileRegexHelper.getDefault();

    public LinkedInTransformer() {
        orgSearcher = OrgSearcher.getOurInstance();
    }

    @Override
    public GraphModel transform(GenericModel generic) {
        GraphModel graphModel = new GraphModel();
        Map<String, Object> post = generic.getPost();

        Profile person = new Profile(Profile.TYPE_PERSON);
        Log log = new Log(generic.getType(), "linkedin", post.get("Url").toString());
        log.putProperties(post);
        person.setLog(Constants.POST, log);

        String[] splitStr = post.get("Url").toString().split("/");

        person.addTrustedEntity(new Entity(splitStr[4], "account"));

        if (!Strings.isNullOrStringEmpty(post.get("Phone"))){
            regexHelper.extractPhones(post.get("Phone").toString()).forEach(phone->
                    person.addTrustedEntity(new Entity(phone, "phone")));
        }

        if (!Strings.isNullOrStringEmpty(post.get("Email"))){
            regexHelper.extractEmails(post.get("Email").toString()).forEach(email->
                    person.addTrustedEntity(new Entity(email, "email")));
        }

        if (!Strings.isNullOrStringEmpty(post.get("Skype"))){
            person.addAnonymousEntity(new Entity(post.get("Skype").toString(), "chat"));
        }

        if (!Strings.isNullOrStringEmpty(post.get("Name"))) {
            person.addAnonymousEntity(new Entity(post.get("Name").toString(), "name"));
        }

        if (!Strings.isNullOrStringEmpty(post.get("Experience"))) {
            List<Map<String, Object>> mExperience = (List<Map<String, Object>>) post.get("Experience");
            for (Map<String, Object> mE : mExperience) {
                Relationship relationship = new Relationship("job");
                relationship.putProperty("position", mE.get("Position"));
                relationship.putProperty("duration", mE.get("Duration"));

                String[] time = mE.get("Times").toString().split("–");
                if (time.length == 1){
                    relationship.putProperty("from", time[0].trim());
                }
                if(time.length == 2){
                    relationship.putProperty("from", time[0].trim());
                    relationship.putProperty("to", time[1].trim());
                }


                Organization companyCrawl = new Organization(Profile.TYPE_ORG_UNTRUST, mE.get("Company").toString());
                person.addAnonymousEntity(relationship, companyCrawl);
                Collection<Pair<String, String>> companyNames = orgSearcher.matchOrg(mE.get("Company").toString());

                if (!companyNames.isEmpty()) {
                    companyCrawl.setLog(log);
                    graphModel.addProfile(companyCrawl);
                    for (Pair<String, String> name : companyNames) {
                        if (name.getKey().equals(mE.get("Company").toString())){
                            companyCrawl.setLabel(Profile.TYPE_ORG);
                            break;
                        }
                        Organization companyStandard = new Organization(name.getKey());
                        Relationship companyRelationship = Relationship.forName("looklike");
                        companyRelationship.putProperties(Collections.singletonMap("score", name.getValue()));
                        companyCrawl.addAnonymousEntity(companyRelationship, companyStandard);
                    }
                }
            }
        }

        if (!Strings.isNullOrStringEmpty(post.get("Education"))) {
            List<Map<String, Object>> mEducation = (List<Map<String, Object>>) post.get("Education");
            for (Map<String, Object> mEdu : mEducation) {
                Relationship relationship = new Relationship("school");
                relationship.putProperty("times", mEdu.get("Times"));
                relationship.putProperty("degreeName", mEdu.get("DegreeName"));
                relationship.putProperty("fieldofstudy", mEdu.get("FieldOfStudy"));

                Organization schoolCrawl = new Organization(Profile.TYPE_ORG_UNTRUST, mEdu.get("SchoolName").toString());
                person.addAnonymousEntity(relationship, schoolCrawl);
                Collection<Pair<String, String>> schoolNames = orgSearcher.matchOrg(mEdu.get("SchoolName").toString());

                if(!schoolNames.isEmpty()){
                    schoolCrawl.setLog(log);
                    graphModel.addProfile(schoolCrawl);
                    for (Pair<String, String> name: schoolNames){
                        if(name.getKey().equals(mEdu.get("SchoolName").toString())){
                            schoolCrawl.setLabel(Profile.TYPE_ORG);
                            break;
                        }
                        Organization schoolStandard = new Organization(name.getKey());
                        Relationship schoolRelationship = Relationship.forName("looklike");
                        schoolRelationship.putProperties(Collections.singletonMap("score", name.getValue()));
                        schoolCrawl.addAnonymousEntity(schoolRelationship, schoolStandard);
                    }
                }
            }
        }

        if (!Strings.isNullOrStringEmpty(post.get("FeaturedSkills_Endorsements"))) {
            List<Map<String, Object>> linkedInSkill = (List<Map<String, Object>>) post.get("FeaturedSkills_Endorsements");
            for (Map<String, Object> linkedIn : linkedInSkill) {
                if (!Strings.isNullOrStringEmpty(linkedIn.get("Name"))) {
                    person.addAnonymousEntity(new Entity(linkedIn.get("Name").toString(), "linkedinskill"));
                }
            }
        }

        if (!Strings.isNullOrStringEmpty(post.get("Interests"))) {
            List<Map<String, Object>> linkedInInterests = (List<Map<String, Object>>) post.get("Interests");
            for (Map<String, Object> linkedIn : linkedInInterests) {
                if (!Strings.isNullOrStringEmpty(linkedIn.get("Name"))) {
                    person.addAnonymousEntity(new Entity(linkedIn.get("Name").toString(), "interests"));
                }
            }
        }

        graphModel.addProfile(person);
        return graphModel;
    }

    public static void main(String[] args) {
        Extractor extractor = new LinkedInExtractor(new Configuration());
        TransformTester transformTester = new TransformTester(new LinkedInTransformer());
        extractor.setMsgBrokerFactory(transformTester);
        extractor.start();
    }
}
