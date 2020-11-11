package com.datacollection.extract.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.avro.reflect.Nullable;

import java.io.Serializable;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FacebookPost extends AbstractPost implements Serializable {

    @JsonProperty("user_name")
    @Nullable
    public String userName;

    @JsonProperty("user_id")
    @Nullable
    public String userId;

    @Nullable
    @JsonProperty("post_time")
    public long postTime;

    @JsonProperty("page_id")
    @Nullable
    public String pageId;

    @JsonProperty("page_name")
    @Nullable
    public String pageName;

    @JsonProperty("post_type")
    @Nullable
    public String postType;

    public String url;

    @JsonProperty("post_id")
    public String postId;

    @JsonProperty("comment_id")
    @Nullable
    public String commentId;
}
