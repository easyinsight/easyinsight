package com.easyinsight.analysis;

import java.io.Serializable;
import java.util.Date;

/**
 * User: jamesboe
 * Date: 11/12/12
 * Time: 3:24 PM
 */
public class DataSourceRefreshResult implements Serializable {
    private Date date;
    private ReportFault warning;
    private boolean newFields;
    private boolean discoveredNewFields;

    public boolean isDiscoveredNewFields() {
        return discoveredNewFields;
    }

    public void setDiscoveredNewFields(boolean discoveredNewFields) {
        this.discoveredNewFields = discoveredNewFields;
    }

    public boolean isNewFields() {
        return newFields;
    }

    public void setNewFields(boolean newFields) {
        this.newFields = newFields;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ReportFault getWarning() {
        return warning;
    }

    public void setWarning(ReportFault warning) {
        this.warning = warning;
    }
}
