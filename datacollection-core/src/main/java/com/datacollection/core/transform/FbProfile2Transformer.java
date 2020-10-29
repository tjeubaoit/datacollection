package com.datacollection.core.transform;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.datacollection.core.collect.Constants;
import com.datacollection.core.collect.OrgSearcher;
import com.datacollection.core.collect.model.Entity;
import com.datacollection.core.collect.model.Gender;
import com.datacollection.core.collect.model.GraphModel;
import com.datacollection.core.collect.model.Log;
import com.datacollection.core.collect.model.Organization;
import com.datacollection.core.collect.model.Profile;
import com.datacollection.core.collect.model.Relationship;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.utils.NullProtector;
import com.datacollection.common.utils.Pair;
import com.datacollection.core.extract.model.GenericModel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by kumin on 24/11/2017.
 */
public class FbProfile2Transformer implements DataTransformer {

    private final ProfileRegexHelper regexHelper = ProfileRegexHelper.getDefault();
    private final OrgSearcher orgSearcher = OrgSearcher.getOurInstance();

    @Override
    public GraphModel transform(GenericModel generic) {
        GraphModel graphModel = new GraphModel();
        try {
            Map<String, Object> post = generic.getPost();
            Log log = new Log(generic.getType(), Constants.FACEBOOK, post.get("baseUrl").toString());
            log.putProperties(post);

            Profile userProfile = new Profile(Profile.TYPE_PERSON);
            userProfile.setLog(Constants.POST, log);

            if (post.get("ContactInfo") != null) {
                getPhoneAndEmail(userProfile, post);
                getBasicInfo(userProfile, post);
            }
            if (post.get("Living") != null) {
                getAddress(userProfile, post);
            }

            Entity fbEntity = new Entity(post.get("userFbId").toString(), "fb.com");
            fbEntity.putProperty("name", post.getOrDefault("name", ""));
            if (post.get("Bio") != null) {
                Map<String, Collection<String>> bio = this.jsonToMap(post.get("Bio").toString());
                Collection<String> nicknames = bio.get("Other Names");

                if (nicknames != null) {
                    String nicknameStr = "";
                    for (String nickname : nicknames) {
                        nickname = splitSmart(nickname, ";", 0);
                        if (nickname==null) continue;
                        nicknameStr += nickname + ";";
                    }
                    fbEntity.putProperties(Collections.singletonMap("nickname", nicknameStr));
                }
            }
            userProfile.addTrustedEntity(new Relationship("account"), fbEntity);

            if (post.get("Education") != null) {
                List<Organization> companyOrOrgProfiles = new ArrayList<>();
                List<Organization> schoolProfiles = new ArrayList<>();
                getJobAndEducation(userProfile, companyOrOrgProfiles, schoolProfiles, post);

                companyOrOrgProfiles.forEach(organization -> {
                    organization.setLog(log);
                    graphModel.addProfile(organization);
                });
                schoolProfiles.forEach(organization -> {
                    organization.setLog(log);
                    graphModel.addProfile(organization);
                });
            }
            graphModel.addProfile(userProfile);
        } catch (JsonParseException e) {
            throw new TransformException(e);
        }

        return graphModel;
    }

    private void getPhoneAndEmail(Profile userProfile, Map<String, Object> post) {
        Collection<String> phones = regexHelper.extractPhones(post.get("ContactInfo").toString());
        Collection<String> emails = regexHelper.extractEmails(post.get("ContactInfo").toString());

        phones.forEach(phone -> {
            Entity entity = new Entity(phone, "phone");
            userProfile.addTrustedEntity(entity);
        });

        emails.forEach(email -> {
            Entity entity = new Entity(email, "email");
            userProfile.addTrustedEntity(entity);
        });
    }

    private void getBasicInfo(Profile userProfile, Map<String, Object> post) {
        Map<String, Collection<String>> contactInfo = this.jsonToMap(post.get("ContactInfo").toString());
        Collection<String> basicInformatics = contactInfo.get("Basic Information");
        if (basicInformatics != null)
            basicInformatics.forEach(basicInformation -> {
                String entityId;
                if (basicInformation.contains("Birthday") &&
                        (entityId = splitSmart(basicInformation, ";", 1)) != null) {
                    Entity bday = new Entity(transformBirthday(entityId), "birthday");
                    userProfile.addAnonymousEntity(bday);

                } else if (basicInformation.contains("Gender") &&
                        (entityId = splitSmart(basicInformation, ";", 1)) != null) {
                    String gender = transformGender(entityId);
                    if (gender != null)
                        userProfile.addAnonymousEntity(new Gender(gender));

                } else if (basicInformation.contains("Interested In") &&
                        (entityId = splitSmart(basicInformation, ";", 1)) != null) {
                    Entity interested = new Entity(entityId, "interested_in");
                    userProfile.addAnonymousEntity(interested);
                }
            });
    }

    private void getAddress(Profile userProfile, Map<String, Object> post) {
        Map<String, Collection<String>> living = this.jsonToMap(post.get("Living").toString());
        Collection<String> cityAndHometown = living.get("Current City and Hometown");
        if (cityAndHometown != null)
            cityAndHometown.forEach(addr -> {
                String entityId;
                if (addr.contains("Current city") &&
                        (entityId = splitSmart(addr, ";", 1)) != null) {
                    Entity currentCity = new Entity(entityId, "location");
                    userProfile.addAnonymousEntity(new Relationship("current_city"), currentCity);
                } else if (addr.contains("Hometown") &&
                        (entityId = splitSmart(addr, ";", 1)) != null) {
                    Entity hometown = new Entity(entityId, "location");
                    userProfile.addAnonymousEntity(new Relationship("hometown"), hometown);
                }
            });
    }

    private void getJobAndEducation(Profile userProfile, List<Organization> companyOrOrgProfiles,
                                    List<Organization> schoolProfiles, Map<String, Object> post) {
        Map<String, Collection<String>> educationAndWork = this.jsonToMap(post.get("Education").toString());
        Collection<String> works = educationAndWork.get("Work");
        if (works != null)
            works.forEach(work -> {
                String orgId = splitSmart(work, ";", 0);
                if (orgId != null) {
                    Collection<Pair<String, String>> orgNames = orgSearcher.matchOrg(orgId);
                    Organization orgCrawl = new Organization(Profile.TYPE_ORG_UNTRUST, orgId);
                    userProfile.addAnonymousEntity(new Relationship("job", "position",
                            NullProtector.get(work.split(";"), 1).orElse("")), orgCrawl);

                    if (!orgNames.isEmpty()) {
                        companyOrOrgProfiles.add(orgCrawl);
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
                }
            });

        Collection<String> schools = educationAndWork.get("Education");
        if (schools != null)
            schools.forEach(school -> {
                String schoolId = splitSmart(school, ";", 0);
                if (schoolId != null) {
                    Collection<Pair<String, String>> schoolNames = orgSearcher.matchOrg(schoolId);
                    Organization schoolCrawl = new Organization(Profile.TYPE_ORG_UNTRUST, schoolId);
                    userProfile.addAnonymousEntity(Relationship.forName("school"), schoolCrawl);

                    if (!schoolNames.isEmpty()) {
                        schoolProfiles.add(schoolCrawl);
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
                }
            });
    }

    private Map<String, Collection<String>> jsonToMap(String json) {
        Map<String, Collection<String>> outputMap = new HashMap<>();
        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(json).getAsJsonArray();

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            String key = jsonObject.get("Title").getAsString();
            JsonArray jsonValues = jsonObject.get("Data").getAsJsonArray();

            Collection<String> values = new HashSet<>();
            for (int j = 0; j < jsonValues.size(); j++) {
                values.add(jsonValues.get(j).getAsString());
            }
            outputMap.put(key, values);
        }
        return outputMap;
    }

    private String transformGender(String gender) {
        if (gender.equals("Male")) return Gender.MALE;
        else if (gender.equals("Female")) return Gender.FEMALE;

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

    public String splitSmart(String toSplit, String delimiter, int index) {
        String[] split = toSplit.split(delimiter);
        if (split.length > index) return split[index];
        return null;
    }
}