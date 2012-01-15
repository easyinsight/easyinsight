package com.easyinsight.audit;

import com.easyinsight.dashboard.DashboardDescriptor;

import javax.persistence.*;
import java.util.Date;

/**
 * User: jamesboe
 * Date: 5/13/11
 * Time: 7:50 PM
 */
@Entity
@Table(name="action_dashboard_log")
@PrimaryKeyJoinColumn(name="action_log_id")
public class ActionDashboardLog extends ActionLog {

    public static final int EDIT = 1;
    public static final int VIEW = 2;
    public static final int EXPORT_XLS = 3;
    public static final int EXPORT_PDF = 4;
    public static final int EXPORT_PNG = 5;

    @Column(name="dashboard_id")
    private long dashboardID;

    @Transient
    private DashboardDescriptor dashboardDescriptor;

    public ActionDashboardLog() {
    }

    public ActionDashboardLog(long userID, int actionType, long dashboardID) {
        super(userID, actionType);
        this.dashboardID = dashboardID;
    }

    public ActionDashboardLog(DashboardDescriptor dashboardDescriptor, int actionType, Date actionDate) {
        this.dashboardDescriptor = dashboardDescriptor;
        dashboardID = dashboardDescriptor.getId();
        setActionType(actionType);
        setActionDate(actionDate);
    }

    public DashboardDescriptor getDashboardDescriptor() {
        return dashboardDescriptor;
    }

    public void setDashboardDescriptor(DashboardDescriptor dashboardDescriptor) {
        this.dashboardDescriptor = dashboardDescriptor;
    }

    public long getDashboardID() {
        return dashboardID;
    }

    public void setDashboardID(long dashboardID) {
        this.dashboardID = dashboardID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ActionDashboardLog that = (ActionDashboardLog) o;

        if (dashboardID != that.dashboardID) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (dashboardID ^ (dashboardID >>> 32));
        return result;
    }
}

