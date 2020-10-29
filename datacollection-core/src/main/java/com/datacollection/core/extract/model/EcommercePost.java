package com.datacollection.core.extract.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by kumin on 07/02/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EcommercePost extends AbstractPost {

    public String full_name;
    public String user_name;
    public String sky_id;
    public String address;
    public String fax;
    public String birth_day;
    public String gender;
    public String company;
    public String source_name;
}
