package com.datacollection.core.transform;

import com.datacollection.core.collect.model.Entity;
import com.datacollection.core.collect.model.GraphModel;
import com.datacollection.core.collect.model.Log;
import com.datacollection.core.collect.model.Profile;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.utils.Strings;
import com.datacollection.core.extract.model.GenericModel;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class RbTransformer implements DataTransformer {

    private final ProfileRegexHelper regexHelper = ProfileRegexHelper.getDefault();

    @Override
    public GraphModel transform(GenericModel generic) {
        GraphModel graphModel = new GraphModel();
        Map<String, Object> post = generic.getPost();
        if (post != null) {
            Log log = new Log(generic.getType(), "rongbay.com", post.get("ad_id").toString());
            log.putProperties(post);
            Profile profile = new Profile(Profile.TYPE_PERSON);
            profile.setLog(log);

            String[] listKey = {"ad_description", "ad_is_mobile", "ad_mobile", "job_contact_fullname",
                    "job_contact_address", "job_contact_email", "fullname", "birthday"};

            for (String key : listKey) {
                if (Strings.isNullOrStringEmpty(post.get(key))) continue;
                if (post.get(key).toString().equals("0")) continue;

                if (key.equals("ad_description")) {
                    String ad_description = post.get("ad_description").toString();
                    Collection<String> phones = regexHelper.extractPhones(ad_description);
                    Collection<String> emails = regexHelper.extractEmails(ad_description);

                    phones.forEach(phone -> {
                        Entity entity = new Entity(phone, "phone");
                        profile.addUntrustedEntity(entity);
                    });

                    emails.forEach(email -> {
                        Entity entity = new Entity(email, "email");
                        profile.addUntrustedEntity(entity);
                    });
                } else {
                    switch (key) {
                        case "ad_is_mobile":
                        case "ad_mobile": {
                            String val = "0" + post.get(key).toString();
                            regexHelper.extractPhones(val).forEach(phone -> {
                                Entity entity = new Entity(val, "phone");
                                profile.addUntrustedEntity(entity);
                            });
                            break;
                        }
                        case "job_contact_fullname":
                        case "fullname": {
                            Entity entity = new Entity(post.get(key).toString(), "fullname");
                            profile.addAnonymousEntity(entity);
                            break;
                        }
                        case "job_contact_address": {
                            Entity entity = new Entity(post.get(key).toString(), "address");
                            profile.addAnonymousEntity(entity);
                            break;
                        }
                        case "job_contact_email": {
                            String val = post.get(key).toString();
                            regexHelper.extractEmails(val).forEach(email -> {
                                Entity entity = new Entity(post.get(key).toString(), "email");
                                profile.addUntrustedEntity(entity);
                            });
                            break;
                        }
                        case "birthday": {
                            try {
                                long ms = Long.parseLong(post.get(key).toString());
                                Entity entity = new Entity(convertDate(ms), "birthday");
                                profile.addAnonymousEntity(entity);
                            } catch (RuntimeException ignored) {
                            }
                            break;
                        }
                        default: {
                            Entity entity = new Entity(post.get(key).toString(), key);
                            profile.addAnonymousEntity(entity);
                            break;
                        }
                    }
                }
            }

            graphModel.addProfile(profile);
        }

        return graphModel;
    }

    public String convertDate(long l) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date(l));
    }
}
