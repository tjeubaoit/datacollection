package com.datacollection.core.transform;

import com.google.common.base.Preconditions;
import com.datacollection.core.collect.Constants;
import com.datacollection.core.collect.OrgSearcher;
import com.datacollection.core.collect.model.Entity;
import com.datacollection.core.collect.model.GraphModel;
import com.datacollection.core.collect.model.Log;
import com.datacollection.core.collect.model.Organization;
import com.datacollection.core.collect.model.Profile;
import com.datacollection.core.collect.model.Relationship;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.TextUtils;
import com.datacollection.common.utils.Pair;
import com.datacollection.common.utils.Strings;
import com.datacollection.common.utils.Utils;
import com.datacollection.core.extract.model.GenericModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EcommerceTransformer implements DataTransformer {

    private final ProfileRegexHelper regexHelper = ProfileRegexHelper.getDefault();
    private final OrgSearcher orgSearcher = OrgSearcher.getOurInstance();

    @Override
    public GraphModel transform(GenericModel generic) {
        Map<String, Object> post = generic.getPost();
        Preconditions.checkNotNull(post);
        String fullName = post.getOrDefault("full_name", "").toString();

        String source = convertSource(post.get("source_name").toString());
        Log log = new Log(generic.getType(), source, post.get("url").toString());
        log.putProperties(post);

        Profile profile = new Profile(Profile.TYPE_PERSON);
        profile.setLog(log);

        if ("vietid".equals(source)) {
            Entity entity = new Entity(post.get("id").toString(), "vietid");
            profile.addTrustedEntity(Relationship.forName(Constants.ACCOUNT), entity);
        } else if ("enbac.com".equals(source) && post.containsKey("user_name")) {
            String username = post.get("user_name").toString().trim();
            if (TextUtils.detectNicknameEcom(username)) {
                Entity entity = new Entity(username, source);
                profile.addTrustedEntity(Relationship.forName(Constants.ACCOUNT), entity);
            } else {
                String id = post.getOrDefault("id", "").toString();
                Set<String> phones = new HashSet<>();
                phones.addAll(regexHelper.extractPhones(id));
                phones.addAll(regexHelper.extractPhones(username));
                phones.addAll(regexHelper.extractPhones(fullName));

                phones.forEach(phone -> profile.addUntrustedEntity(new Entity(phone, "phone")));
            }

            post.remove("user_name");
            post.remove("id");
        }

        post.remove("url");
        post.remove("id");
        post.remove("source_name");

        List<Organization> orgProfiles = new ArrayList<>();

        for (Map.Entry<String, Object> item : post.entrySet()) {
            String key = item.getKey().toLowerCase();

            if (item.getValue() instanceof ArrayList) {
                for (Object ob : (ArrayList) item.getValue()) {
                    String value = ob.toString();
                    if (Strings.isNonEmpty(value)) {
                        handleKeyValue(orgProfiles, profile, key, value);
                    }
                }
            } else if (item.getValue() instanceof String) {
                String value = item.getValue().toString();
                if (Strings.isNonEmpty(value)) {
                    handleKeyValue(orgProfiles, profile, key, value);
                }
            }
        }

        GraphModel gm = new GraphModel();
        TransformUtils.predictFacebookAccount(gm, profile);
        gm.addProfile(profile);

        orgProfiles.forEach(orgProfile -> {
            orgProfile.setLog(log);
            gm.addProfile(orgProfile);
        });

        return gm;
    }

    private void handleKeyValue(List<Organization> orgProfiles, Profile profile, String key, String value) {
        if ("0".equals(value) && Utils.notEquals(key, "gender")) return;
        switch (key) {
            case "emails":
                Collection<String> emails = regexHelper.extractEmails(value);
                emails.forEach(email -> {
                    Entity entity = new Entity(email, "email");
                    profile.addTrustedEntity(entity);
                });

                break;
            case "phones":
                Collection<String> phones = regexHelper.extractPhones(value);
                phones.forEach(phone -> {
                    Entity entity = new Entity(phone, "phone");
                    profile.addUntrustedEntity(entity);
                });
                break;
            case "website": {
                if (TextUtils.isWebsite(value)) {
                    Entity entity = new Entity(value, "website");
                    profile.addAnonymousEntity(entity);
                }
                break;
            }
            case "birth_day": {
                if (Utils.notEquals("0000-00-00", value)) {
                    Entity entity = new Entity(value, "birthday");
                    profile.addAnonymousEntity(entity);
                }
                break;
            }
            case "yahoo_id":
            case "sky_id": {
                if (TextUtils.detectNicknameEcom(value)) {
                    Entity entity = new Entity(value, normalizeKey(key));
                    profile.addAnonymousEntity(Relationship.forName("chat"), entity);
                }
                break;
            }
            case "company": {
                if (Strings.isNonEmpty(value) && profile.type().equals(Profile.TYPE_PERSON)) {
                    Organization orgAnonymous = new Organization(Profile.TYPE_ORG_UNTRUST, value);
                    profile.addAnonymousEntity(Relationship.forName("job"), orgAnonymous);

                    Collection<Pair<String, String>> orgNames = orgSearcher.matchOrg(value);
                    if (!orgNames.isEmpty()) {
                        for (Pair<String, String> name : orgNames) {
                            if (name.getKey().equals(orgAnonymous)) {
                                orgAnonymous.setLabel(Profile.TYPE_ORG);
                                break;
                            }
                            Organization orgStandard = new Organization(name.getKey());
                            Relationship orgRelationship = Relationship.forName("looklike");
                            orgRelationship.putProperties(Collections.singletonMap("score", name.getValue()));
                            orgAnonymous.addAnonymousEntity(orgRelationship, orgStandard);
                        }
                        orgProfiles.add(orgAnonymous);
                    }
                }
                break;
            }
            case "user_name":
            case "full_name": {
                Entity entity = new Entity(value, "fullname");
                profile.addAnonymousEntity(entity);
                break;
            }
            default:
                break;
        }
    }

    private static String convertSource(String source) {
        switch (source) {
            case "enbac":
                return "enbac.com";
            case "rongbay":
                return "rongbay.com";
            case "muachung":
                return "muachung.vn";
            case "vietid":
                return "vietid";
            default:
                throw new IllegalArgumentException("Invalid source");
        }
    }

    private static String normalizeKey(String key) {
        switch (key) {
            case "sky_id":
                return "skype";
            case "yahoo_id":
                return "yahoo";
            default:
                return key.replace("_", "");
        }
    }
}