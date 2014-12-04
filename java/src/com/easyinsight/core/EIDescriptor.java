package com.easyinsight.core;

import com.easyinsight.dashboard.DashboardDescriptor;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.export.ExportMetadata;
import com.easyinsight.security.Roles;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

/**
 * User: James Boe
 * Date: Mar 28, 2009
 * Time: 8:30:25 AM
 */
public abstract class EIDescriptor implements Serializable {

    public static final int DATA_SOURCE = 1;
    public static final int REPORT = 2;
    public static final int GROUP = 3;
    public static final int GOAL_TREE = 4;
    public static final int SOLUTION = 5;
    public static final int PACKAGE = 8;
    public static final int SCORECARD = 9;
    public static final int LOOKUP_TABLE = 10;
    public static final int DASHBOARD = 11;
    public static final int ANALYSIS_ITEM = 12;
    public static final int FILTER = 13;
    public static final int KEY = 14;
    public static final int FOLDER = 15;
    public static final int FILTER_SET = 16;

    public static final int MAIN_VIEW = 1;
    public static final int OTHER_VIEW = 2;

    private String name;
    private String urlKey;
    private String author;
    private Date creationDate;
    private Date modifiedDate;
    private boolean accountVisible;
    private long id;
    private int role = Roles.VIEWER;
    private int folder = MAIN_VIEW;
    private String description;

    public abstract int getType();

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAccountVisible() {
        return accountVisible;
    }

    public void setAccountVisible(boolean accountVisible) {
        this.accountVisible = accountVisible;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFolder() {
        return folder;
    }

    public void setFolder(int folder) {
        this.folder = folder;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public EIDescriptor() {
    }

    public EIDescriptor(String name, long id, boolean accountVisible) {
        this.name = name;
        this.id = id;
        this.accountVisible = accountVisible;
    }

    protected EIDescriptor(String name, long id, String urlKey, boolean accountVisible) {
        this.name = name;
        this.id = id;
        this.urlKey = urlKey;
        this.accountVisible = accountVisible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EIDescriptor that = (EIDescriptor) o;

        if (id != that.id) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (int) (id ^ (id >>> 32));
        return result;
    }

    public String getUrlKey() {
        return urlKey;
    }

    public void setUrlKey(String urlKey) {
        this.urlKey = urlKey;
    }

    public JSONObject toJSON(ExportMetadata dateFormat) throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("name", getName());
        jo.put("url_key", getUrlKey());
        jo.put("description", getDescription());
        jo.put("folder", getFolder());
        jo.put("delete_enabled", role == Roles.OWNER);
        jo.put("id", getId());
        jo.put("href", getUrl());
        return jo;
    }

    public abstract String getUrl();

    public boolean contains(String value) {
        String v = value.toLowerCase().trim();
        return (getName() != null && getName().toLowerCase().contains(v)) || (getDescription() != null && getDescription().contains(v));
    }
}
