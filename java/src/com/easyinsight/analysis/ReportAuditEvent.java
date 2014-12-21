package com.easyinsight.analysis;

import java.io.Serializable;

/**
 * User: jamesboe
 * Date: 10/8/13
 * Time: 12:28 PM
 */
public class ReportAuditEvent implements Serializable {

    public static final String JOIN_FILTER = "Join Filter";
    public static final String JOIN = "Join";
    public static final String QUERY = "Query";
    public static final String WARNING = "Warning";
    public static final String FIELD = "Field";
    public static final String FILTER = "Filter";

    private String auditType;
    private String eventLabel;


    public ReportAuditEvent() {
    }

    public ReportAuditEvent(String auditType, String eventLabel) {
        this.auditType = auditType;
        this.eventLabel = eventLabel;
    }

    public String getAuditType() {
        return auditType;
    }

    public void setAuditType(String auditType) {
        this.auditType = auditType;
    }

    public String getEventLabel() {
        return eventLabel;
    }

    public void setEventLabel(String eventLabel) {
        this.eventLabel = eventLabel;
    }
}
