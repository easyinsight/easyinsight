package com.easyinsight.datafeeds;

/**
 * User: James Boe
 * Date: Apr 27, 2008
 * Time: 3:43:23 PM
 */
public class FeedType {
    public static final FeedType GOOGLE = new FeedType(1);
    public static final FeedType STATIC = new FeedType(2);
    public static final FeedType COMPOSITE = new FeedType(3);
    public static final FeedType SALESFORCE = new FeedType(4);
    public static final FeedType ANALYSIS_BASED = new FeedType(5);
    public static final FeedType SALESFORCE_SUB = new FeedType(6);
    public static final FeedType WESABE = new FeedType(7);
    public static final FeedType DEFAULT = new FeedType(8);
    public static final FeedType JIRA = new FeedType(9);
    public static final FeedType BASECAMP = new FeedType(10);
    public static final FeedType ADMIN_STATS = new FeedType(11);
    public static final FeedType GNIP = new FeedType(12);

    private int type;

    public FeedType() {
    }

    public void setType(int type) {
        this.type = type;
    }

    public FeedType(int type) {
        this.type = type;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeedType that = (FeedType) o;

        return type == that.type;

    }

    public int getType() {
        return type;
    }

    public static FeedType valueOf(int type) {
        return new FeedType(type);
    }       

    public int hashCode() {
        return type;
    }
}
