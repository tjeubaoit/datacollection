package com.datacollection.core.transform;

import com.datacollection.core.collect.model.Entity;
import com.datacollection.core.collect.model.GraphModel;
import com.datacollection.core.collect.model.Log;
import com.datacollection.core.collect.model.Organization;
import com.datacollection.core.collect.model.Profile;
import com.datacollection.core.collect.model.Relationship;
import com.datacollection.common.ProfileRegexHelper;
import com.datacollection.common.utils.Strings;
import com.datacollection.core.extract.model.GenericModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class OrgTransformer implements DataTransformer {

    private final ProfileRegexHelper regexHelper = ProfileRegexHelper.getDefault();

    @Override
    public GraphModel transform(GenericModel generic) {
        Map<String, Object> post = generic.getPost();
        GraphModel graphModel = new GraphModel();
        String title = post.get("Title").toString().toLowerCase();
        Organization orgProfile = new Organization(title);

        this.addTrustEntity(orgProfile, post);
        this.addAnonymousEntity(orgProfile, post, Arrays.asList("Address", "Fax"));
        this.addBranch(orgProfile, post);
        this.addProductOrServices(orgProfile, post);

        Log log = new Log(generic.getType(), post.get("Domain").toString(), post.get("Url").toString());
        log.putProperties(post);

        orgProfile.setLog(log);
        graphModel.addProfile(orgProfile);

        return graphModel;
    }

    public void addTrustEntity(Profile orgProfile, Map<String, Object> post) {
        if (!Strings.isNullOrStringEmpty(post.get("Phone"))) {
            Collection<String> phones = regexHelper.extractPhones(post.get("Phone").toString());
            phones.forEach(phone -> {
                Entity entity = new Entity(phone, "phone");
                orgProfile.addTrustedEntity(entity);
            });
        }

        if (!Strings.isNullOrStringEmpty(post.get("Email"))) {
            Collection<String> emails = regexHelper.extractEmails(post.get("Email").toString());
            emails.forEach(email -> {
                Entity entity = new Entity(email, "email");
                orgProfile.addTrustedEntity(entity);
            });
        }

        if (!Strings.isNullOrStringEmpty(post.get("TaxCode"))) {
            Entity taxEntity = new Entity(post.get("TaxCode").toString(), "taxcode");
            orgProfile.addTrustedEntity(taxEntity);
        }

        Map<String, Object> propertise = new HashMap<>();
        if (!Strings.isNullOrStringEmpty(post.get("PermitDate"))) {
            propertise.put("permitdate", post.get("PermitDate"));
        }

        if (!Strings.isNullOrStringEmpty(post.get("ActivateDate"))) {
            propertise.put("activatedate", post.get("ActivateDate").toString().split("\\(")[0]);
        }

        if (!Strings.isNullOrStringEmpty(post.get("Manager"))) {
            propertise.put("manager", post.get("Manager"));
        }

        orgProfile.putProperties(propertise);
    }

    public void addAnonymousEntity(Profile orgProfile, Map<String, Object> post, List<String> keys) {
        keys.forEach(key -> {
            if (!Strings.isNullOrStringEmpty(post.get(key))) {
                Entity entity = new Entity(post.get(key).toString(), key.toLowerCase());
                orgProfile.addAnonymousEntity(entity);
            }
        });
    }

    public void addBranch(Profile orgProfile, Map<String, Object> post) {
        if (!Strings.isNullOrStringEmpty(post.get("BranchMain"))) {
            Entity entity = new Entity(post.get("BranchMain").toString(), "branch");
            if (!Strings.isNullOrStringEmpty(post.get("BranchCode")))
                entity.putProperties(Collections.singletonMap("code", post.get("BranchCode").toString()));

            orgProfile.addAnonymousEntity(Relationship.forName("branchmain"), entity);
        }

        if (!Strings.isNullOrStringEmpty(post.get("BranchList"))) {
            List<Map<String, String>> branchList = (List<Map<String, String>>) post.get("BranchList");
            branchList.forEach(branch -> {
                if (!Strings.isNullOrStringEmpty(branch.get("Name"))) {
                    Entity entity = new Entity(branch.get("Name"), "branch");
                    if (!Strings.isNullOrStringEmpty(branch.get("Code"))) {
                        entity.putProperties(Collections.singletonMap("code", branch.get("Code")));
                    }

                    orgProfile.addAnonymousEntity(entity);
                }
            });
        }
    }

    public void addProductOrServices(Profile orgProfile, Map<String, Object> post){
        if (post.get("ProductOrServices")==null) return;
        List<Map<String, String>> productOrServices = (List<Map<String, String>>)post.get("ProductOrServices");
        productOrServices.forEach(productOrService -> {
            Entity entity = new Entity(productOrService.get("name"), "productservice");
            orgProfile.addAnonymousEntity(Relationship.forName("service"), entity);
        });
    }
}
