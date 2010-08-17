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
    public static final FeedType GOOGLE_ANALYTICS = new FeedType(13);
    public static final FeedType TEST_ALPHA = new FeedType(14);
    public static final FeedType TEST_BETA = new FeedType(15);
    public static final FeedType TEST_GAMMA = new FeedType(16);
    public static final FeedType BASECAMP_TIME = new FeedType(17);
    public static final FeedType BASECAMP_MASTER = new FeedType(18);
    public static final FeedType WESABE_ACCOUNTS = new FeedType(19);
    public static final FeedType WESABE_TRANSACTIONS = new FeedType(20);
    public static final FeedType CLOUD_WATCH = new FeedType(21);
    public static final FeedType CLARITY = new FeedType(22);
    public static final FeedType HIGHRISE_COMPOSITE = new FeedType(23);
    public static final FeedType HIGHRISE_COMPANY = new FeedType(24);
    public static final FeedType HIGHRISE_DEAL = new FeedType(25);
    public static final FeedType TWITTER = new FeedType(26);
    public static final FeedType CUSTOM = new FeedType(27);
    public static final FeedType BASECAMP_COMPANY = new FeedType(28);
    public static final FeedType BASECAMP_COMPANY_PROJECT_JOIN = new FeedType(29);
    public static final FeedType HIGHRISE_CONTACTS = new FeedType(30);
    public static final FeedType MARKETO = new FeedType(31);
    public static final FeedType HIGHRISE_CASES = new FeedType(32);
    public static final FeedType HIGHRISE_TASKS = new FeedType(33);
    public static final FeedType PIVOTAL_TRACKER = new FeedType(34);
    public static final FeedType SENDGRID = new FeedType(35);
    public static final FeedType MAILCHIMP = new FeedType(36);
    public static final FeedType TEAMWORKPM = new FeedType(37);
    public static final FeedType MEETUP = new FeedType(38);
    public static final FeedType LINKEDIN = new FeedType(39);
    public static final FeedType HIGHRISE_EMAILS = new FeedType(40);
    public static final FeedType FRESHBOOKS_INVOICE = new FeedType(41);
    public static final FeedType FRESHBOOKS_CLIENTS = new FeedType(42);
    public static final FeedType FRESHBOOKS_EXPENSES = new FeedType(43);
    public static final FeedType FRESHBOOKS_STAFF = new FeedType(44);
    public static final FeedType FRESHBOOKS_PROJECTS = new FeedType(45);
    public static final FeedType FRESHBOOKS_TASKS = new FeedType(46);
    public static final FeedType FRESHBOOKS_TIME_ENTRIES = new FeedType(47);
    public static final FeedType FRESHBOOKS_PAYMENTS = new FeedType(48);
    public static final FeedType FRESHBOOKS_COMPOSITE = new FeedType(49);
    public static final FeedType FRESHBOOKS_CATEGORIES = new FeedType(50);
    public static final FeedType FRESHBOOKS_ESTIMATES = new FeedType(51);
    public static final FeedType FRESHBOOKS_RECURRING = new FeedType(52);
    public static final FeedType BASECAMP_COMMENTS = new FeedType(53);
    public static final FeedType REDIRECT = new FeedType(54);

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
