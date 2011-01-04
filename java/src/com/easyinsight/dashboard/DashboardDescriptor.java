package com.easyinsight.dashboard;

import com.easyinsight.core.EIDescriptor;

/**
 * User: jamesboe
 * Date: Nov 26, 2010
 * Time: 1:24:45 PM
 */
public class DashboardDescriptor extends EIDescriptor {

    private long dataSourceID;

    public DashboardDescriptor() {
    }

    /*public DashboardDescriptor(String name, long id) {
        super(name, id);
    }*/

    public DashboardDescriptor(String name, long id, String urlKey, long dataSourceID) {
        super(name, id, urlKey);
        this.dataSourceID = dataSourceID;
    }

    public long getDataSourceID() {
        return dataSourceID;
    }

    public void setDataSourceID(long dataSourceID) {
        this.dataSourceID = dataSourceID;
    }

    @Override
    public int getType() {
        return EIDescriptor.DASHBOARD;
    }
}
