package com.easyinsight.dashboard;

/**
 * User: jamesboe
 * Date: 11/14/13
 * Time: 11:29 AM
 */
public class FilterPositionKey {

    public static int DASHBOARD = 1;
    public static int REPORT = 2;
    public static int DASHBOARD_REPORT = 3;
    public static int DASHBOARD_STACK = 4;

    private int scope;

    private long filterID;

    private String scopeURLKey;

    public FilterPositionKey(int scope, long filterID, String scopeURLKey) {
        this.scope = scope;
        this.filterID = filterID;
        this.scopeURLKey = scopeURLKey;
    }

    public static FilterPositionKey fromString(String string) {
        String[] tokens = string.split("\\|");
        int scope = Integer.parseInt(tokens[0]);
        long filterID;
        String urlKey = null;
        if (tokens.length == 2) {
            filterID = Long.parseLong(tokens[1]);
        } else {
            filterID = Long.parseLong(tokens[1]);
            urlKey = tokens[2];

        }
        return new FilterPositionKey(scope, filterID, urlKey);
    }

    public String createURLKey() {
        if (scope == REPORT) {
            return scope + "|" + filterID;
        } else if (scope == DASHBOARD) {
            return scope + "|" + filterID;
        } else {
            return scope + "|" + filterID + "|" + scopeURLKey;
        }
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public long getFilterID() {
        return filterID;
    }

    public void setFilterID(long filterID) {
        this.filterID = filterID;
    }

    public String getScopeURLKey() {
        return scopeURLKey;
    }

    public void setScopeURLKey(String scopeURLKey) {
        this.scopeURLKey = scopeURLKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterPositionKey that = (FilterPositionKey) o;

        if (filterID != that.filterID) return false;
        if (scope != that.scope) return false;
        if (scopeURLKey != null ? !scopeURLKey.equals(that.scopeURLKey) : that.scopeURLKey != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = scope;
        result = 31 * result + (int) (filterID ^ (filterID >>> 32));
        result = 31 * result + (scopeURLKey != null ? scopeURLKey.hashCode() : 0);
        return result;
    }
}
