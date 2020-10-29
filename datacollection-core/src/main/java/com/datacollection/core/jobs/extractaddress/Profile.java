package com.datacollection.core.jobs.extractaddress;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by kumin on 12/09/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Profile {

    private long uid;
    private Set<String> emails = new LinkedHashSet<>();
    private Set<String> phones = new LinkedHashSet<>();
    private Set<Long> uids = new LinkedHashSet<>();

    @JsonProperty("vietids")
    private Set<String> vietId = new LinkedHashSet<>();

    @JsonProperty("adsids")
    private Set<String> adsId = new LinkedHashSet<>();

    private Map<String, String> facebooks = new LinkedHashMap<>();
    private Map<String, String> forums = new LinkedHashMap<>();

    private String name;
    private String address;
    private String location;
    private String job;
    private String birthday;
    private Byte gender;
    private Map<String, String> extras = new LinkedHashMap<>();

    private Date updateTime;
    private List<History> histories = new LinkedList<>();

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public Set<String> getEmails() {
        return emails;
    }

    public void setEmails(Set<String> emails) {
        this.emails = emails;
    }

    public Set<String> getPhones() {
        return phones;
    }

    public void setPhones(Set<String> phones) {
        this.phones = phones;
    }

    public Set<Long> getUids() {
        return uids;
    }

    public void setUids(Set<Long> uids) {
        this.uids = uids;
    }

    public Set<String> getVietId() {
        return vietId;
    }

    public void setVietId(Set<String> vietId) {
        this.vietId = vietId;
    }

    public Set<String> getAdsId() {
        return adsId;
    }

    public void setAdsId(Set<String> adsId) {
        this.adsId = adsId;
    }

    public Map<String, String> getFacebooks() {
        return facebooks;
    }

    public void setFacebooks(Map<String, String> facebooks) {
        this.facebooks = facebooks;
    }

    public Map<String, String> getForums() {
        return forums;
    }

    public void setForums(Map<String, String> forums) {
        this.forums = forums;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public Byte getGender() {
        return gender;
    }

    public void setGender(Byte gender) {
        this.gender = gender;
    }

    public Map<String, String> getExtras() {
        return extras;
    }

    public void setExtras(Map<String, String> extras) {
        this.extras = extras;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public List<History> getHistories() {
        return histories;
    }

    public void setHistories(List<History> histories) {
        this.histories = histories;
    }
}
