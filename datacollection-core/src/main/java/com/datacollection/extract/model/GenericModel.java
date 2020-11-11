package com.datacollection.extract.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GenericModel {

    public static final String TYPE_FB_GROUP_POST = "fb.group.post";
    public static final String TYPE_FB_GROUP_COMMENT = "fb.group.cmt";
    public static final String TYPE_FB_FANPAGE_POST = "fb.page.post";
    public static final String TYPE_FB_FANPAGE_COMMENT = "fb.page.cmt";
    public static final String TYPE_FB_PROFILE_POST = "fb.profile.post";
    public static final String TYPE_FB_PROFILE_COMMENT = "fb.profile.cmt";
    public static final String TYPE_FB_PROFILE = "fb.profile";
    public static final String TYPE_FB_PROFILE_NEW = "fb.profile.new";
    public static final String TYPE_FB_PROFILE_NEW2 = "fb.profile.new2";
    
    public static final String TYPE_ORG = "org";
    public static final String TYPE_API_VIETID = "api.vietid";
    public static final String TYPE_API_RB = "api.rb";
    public static final String TYPE_ECOMMERCE = "ecom";
    public static final String TYPE_NEWSDB = "news";
    public static final String TYPE_DMP = "dmp";
    public static final String TYPE_EXCEL = "excel";

    public static final String TYPE_ZAMBA = "zb";
    public static final String TYPE_ZAMBA_CHATBOT="fbzb";
    public static final String TYPE_FORUM_ARTICLE = "fr.art";
    public static final String TYPE_FORUM_COMMENT = "fr.cmt";

    public static final String TYPE_LINKEDIN = "linkedin";
    public static final String TYPE_ADCHATBOT = "log.adchatbot";
    public static final String TYPE_TRACKFORM = "log.trackform";

    private String id;
    private String type;
    private Map<String, Object> post = new LinkedHashMap<>();

    public GenericModel() {
    }

    public GenericModel(String id, String type, AbstractPost post) {
        this(id, type, post.toMap());
    }

    public GenericModel(String id, String type, Map<String, Object> postMap) {
        this.id = id;
        this.type = type;
        this.post.putAll(postMap);
    }

    @Override
    public String toString() {
        return "{id=" + id + ",type=" + type + "}";
    }

    public String getId() {
        return id;
    }

    public GenericModel setId(String id) {
        this.id = id;
        return this;
    }

    public String getType() {
        return type;
    }

    public GenericModel setType(String type) {
        this.type = type;
        return this;
    }

    public Map<String, Object> getPost() {
        return post;
    }

    public GenericModel setPost(Map<String, Object> post) {
        this.post = post;
        return this;
    }
}
