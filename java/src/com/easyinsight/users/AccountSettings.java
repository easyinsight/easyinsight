package com.easyinsight.users;

/**
 * User: James Boe
 * Date: Apr 13, 2009
 * Time: 9:38:01 AM
 */
public class AccountSettings {
    private boolean apiEnabled;
    private boolean publicData;
    private boolean marketplace;
    private boolean reportSharing;
    private long groupID;

    public AccountSettings() {
    }

    public boolean isPublicData() {
        return publicData;
    }

    public void setPublicData(boolean publicData) {
        this.publicData = publicData;
    }

    public boolean isMarketplace() {
        return marketplace;
    }

    public void setMarketplace(boolean marketplace) {
        this.marketplace = marketplace;
    }

    public boolean isReportSharing() {
        return reportSharing;
    }

    public void setReportSharing(boolean reportSharing) {
        this.reportSharing = reportSharing;
    }

    public long getGroupID() {
        return groupID;
    }

    public void setGroupID(long groupID) {
        this.groupID = groupID;
    }

    public boolean isApiEnabled() {
        return apiEnabled;
    }

    public void setApiEnabled(boolean apiEnabled) {
        this.apiEnabled = apiEnabled;
    }
}