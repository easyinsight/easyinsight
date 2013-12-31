package com.easyinsight.datafeeds;

import java.io.Serializable;

/**
 * User: James Boe
 * Date: Apr 27, 2008
 * Time: 3:43:23 PM
 */
public class FeedType implements Serializable {
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
    public static final FeedType HIGHRISE_CONTACT_NOTES = new FeedType(55);
    public static final FeedType WHOLE_FOODS = new FeedType(56);
    public static final FeedType RALLY_COMPOSITE = new FeedType(57);
    public static final FeedType RALLY_DEFECT = new FeedType(58);
    public static final FeedType RALLY_STORY = new FeedType(59);
    public static final FeedType HIGHRISE_DEAL_NOTES = new FeedType(60);
    public static final FeedType HIGHRISE_COMPANY_NOTES = new FeedType(61);
    public static final FeedType HIGHRISE_CASE_NOTES = new FeedType(62);
    public static final FeedType CONSTANT_CONTACT = new FeedType(63);
    public static final FeedType CONSTANT_CONTACT_CAMPAIGN = new FeedType(64);
    public static final FeedType CONSTANT_CONTACT_CAMPAIGN_RESULTS = new FeedType(65);
    public static final FeedType CONSTANT_CONTACT_CONTACTS = new FeedType(66);
    public static final FeedType CONSTANT_CONTACT_CONTACT_LISTS = new FeedType(67);
    public static final FeedType CONSTANT_CONTACT_CONTACT_TO_CONTACT_LIST = new FeedType(68);
    public static final FeedType BASECAMP_PROJECTS = new FeedType(69);
    public static final FeedType BASECAMP_TODO_COMMENTS = new FeedType(70);
    public static final FeedType QUICKBASE_CHILD = new FeedType(71);
    public static final FeedType QUICKBASE_COMPOSITE = new FeedType(72);
    public static final FeedType FRESHBOOKS_ITEMS = new FeedType(73);
    public static final FeedType FRESHBOOKS_LINE_ITEMS = new FeedType(74);
    public static final FeedType CAMPAIGN_MONITOR_COMPOSITE = new FeedType(75);
    public static final FeedType CAMPAIGN_MONITOR_CLIENTS = new FeedType(76);
    public static final FeedType CAMPAIGN_MONITOR_CAMPAIGNS = new FeedType(77);
    public static final FeedType CAMPAIGN_MONITOR_LISTS = new FeedType(78);
    public static final FeedType CAMPAIGN_MONITOR_CAMPAIGN_RESULTS = new FeedType(79);
    public static final FeedType CLEARDB_COMPOSITE = new FeedType(80);
    public static final FeedType CLEARDB_CHILD = new FeedType(81);
    public static final FeedType BATCHBOOK_COMPOSITE = new FeedType(82);
    public static final FeedType BATCHBOOK_DEALS = new FeedType(83);
    public static final FeedType BATCHBOOK_PEOPLE = new FeedType(84);
    public static final FeedType BATCHBOOK_COMPANIES = new FeedType(85);
    public static final FeedType BATCHBOOK_COMMUNICATIONS = new FeedType(86);
    public static final FeedType BATCHBOOK_USERS = new FeedType(87);
    public static final FeedType BATCHBOOK_TODOS = new FeedType(88);
    public static final FeedType FEDERATED = new FeedType(89);
    public static final FeedType MYSQL = new FeedType(90);
    public static final FeedType SQL_SERVER = new FeedType(91);
    public static final FeedType ORACLE = new FeedType(92);
    public static final FeedType HARVEST_COMPOSITE = new FeedType(93);
    public static final FeedType HARVEST_CLIENT = new FeedType(94);
    public static final FeedType HARVEST_PROJECT = new FeedType(95);
    public static final FeedType HARVEST_TIME = new FeedType(96);
    public static final FeedType ZENDESK_COMPOSITE = new FeedType(97);
    public static final FeedType ZENDESK_ORGANIZATION = new FeedType(98);
    public static final FeedType ZENDESK_USER = new FeedType(99);
    public static final FeedType ZENDESK_TICKET = new FeedType(100);
    public static final FeedType ZENDESK_GROUP = new FeedType(101);
    public static final FeedType ZENDESK_GROUP_TO_USER = new FeedType(102);
    public static final FeedType HARVEST_CONTACTS = new FeedType(103);
    public static final FeedType HARVEST_TASKS = new FeedType(104);
    public static final FeedType HARVEST_TASK_ASSIGNMENTS = new FeedType(105);

    public static final FeedType HARVEST_USERS = new FeedType(106);
    public static final FeedType AMAZON_EC2 = new FeedType(107);
    public static final FeedType AMAZON_EBS = new FeedType(108);
    public static final FeedType HARVEST_EXPENSES = new FeedType(109);
    public static final FeedType HARVEST_EXPENSE_CATEGORIES = new FeedType(110);
    public static final FeedType AMAZON_SQS = new FeedType(111);
    public static final FeedType AMAZON_RDS = new FeedType(112);
    public static final FeedType HARVEST_INVOICES = new FeedType(113);
    public static final FeedType AMAZON_S3 = new FeedType(114);
    public static final FeedType SAMPLE_COMPOSITE = new FeedType(115);
    public static final FeedType SAMPLE_SALES = new FeedType(116);
    public static final FeedType SAMPLE_CUSTOMER = new FeedType(117);
    public static final FeedType SAMPLE_PRODUCT = new FeedType(118);
    public static final FeedType HIGHRISE_CASE_JOIN = new FeedType(119);

    public static final FeedType KASHOO_COMPOSITE = new FeedType(120);
    public static final FeedType KASHOO_BUSINESSES = new FeedType(121);

    public static final FeedType SOLVE360_COMPOSITE = new FeedType(122);
    public static final FeedType SOLVE360_CONTACTS = new FeedType(123);
    public static final FeedType SOLVE360_OPPORTUNITIES = new FeedType(124);
    public static final FeedType SOLVE360_ACTIVITIES = new FeedType(125);

    public static final FeedType XERO_COMPOSITE = new FeedType(126);
    public static final FeedType XERO_ACCOUNTS = new FeedType(127);
    public static final FeedType XERO_BANK_TRANSACTIONS = new FeedType(128);
    public static final FeedType XERO_INVOICES = new FeedType(129);

    public static final FeedType HARVEST_USER_ASSIGNMENTS = new FeedType(130);
    
    public static final FeedType CUSTOM_REST_LIVE = new FeedType(131);
    public static final FeedType TWILIO_COMPOSITE = new FeedType(132);
    public static final FeedType TWILIO_SOMETHING = new FeedType(133);
    public static final FeedType BATCHBOOK_COMMUNICATION_PARTIES = new FeedType(134);

    public static final FeedType BASECAMP_NEXT_COMPOSITE = new FeedType(135);
    public static final FeedType BASECAMP_NEXT_PROJECTS = new FeedType(136);
    public static final FeedType BASECAMP_NEXT_TODOS = new FeedType(137);
    public static final FeedType BASECAMP_NEXT_CALENDAR = new FeedType(138);
    public static final FeedType BASECAMP_NEXT_PEOPLE = new FeedType(139);
    public static final FeedType BATCHBOOK_SUPER_TAG = new FeedType(140);
    public static final FeedType JSON = new FeedType(141);
    public static final FeedType HIGHRISE_ACTIVITIES = new FeedType(142);

    public static final FeedType BATCHBOOK2_PEOPLE = new FeedType(143);
    public static final FeedType BATCHBOOK2_COMPANIES = new FeedType(144);
    public static final FeedType BATCHBOOK2_COMPOSITE= new FeedType(145);
    public static final FeedType BATCHBOOK2_PHONES = new FeedType(146);
    public static final FeedType BATCHBOOK2_WEBSITES = new FeedType(147);
    public static final FeedType BATCHBOOK2_ADDRESSES = new FeedType(148);
    public static final FeedType BATCHBOOK2_EMAILS = new FeedType(149);
    public static final FeedType BATCHBOOK2_CUSTOM = new FeedType(150);
    public static final FeedType WUFOO_COMPOSITE = new FeedType(151);

    public static final FeedType TREASURE_DATA = new FeedType(152);

    public static final FeedType DATABASE_CONNECTION = new FeedType(153);

    private int type;
    public static final FeedType QUICKBASE_USER_CHILD = new FeedType(154);

    public static final FeedType YOUTRACK_COMPOSITE = new FeedType(155);
    public static final FeedType YOUTRACK_PROJECTS = new FeedType(156);
    public static final FeedType YOUTRACK_TASKS = new FeedType(157);
    public static final FeedType ZENDESK_COMMENTS = new FeedType(158);

    public static final FeedType WUFOO_FORM = new FeedType(159);
    public static final FeedType INSIGHTLY_COMPOSITE = new FeedType(160);
    public static final FeedType INSIGHTLY_CONTACTS = new FeedType(161);
    public static final FeedType INSIGHTLY_ORGANIZATIONS = new FeedType(162);
    public static final FeedType INSIGHTLY_OPPORTUNITIES = new FeedType(163);
    public static final FeedType INSIGHTLY_TASKS = new FeedType(164);
    public static final FeedType INSIGHTLY_PROJECTS = new FeedType(165);

    public static final FeedType GOOGLE_PROVISIONING = new FeedType(166);
    public static final FeedType SERVER_MYSQL = new FeedType(167);
    public static final FeedType SERVER_SQL_SERVER = new FeedType(168);
    public static final FeedType SERVER_ORACLE = new FeedType(169);

    public static final FeedType SERVER_POSTGRES = new FeedType(170);

    public static final FeedType TRELLO_COMPOSITE = new FeedType(171);
    public static final FeedType TRELLO_BOARD = new FeedType(172);
    public static final FeedType TRELLO_LIST = new FeedType(173);
    public static final FeedType TRELLO_CARD = new FeedType(174);
    public static final FeedType TRELLO_CARD_HISTORY = new FeedType(175);

    public static final FeedType CONSTANT_CONTACT_LINKS = new FeedType(176);
    public static final FeedType INFUSIONSOFT_COMPOSITE = new FeedType(177);
    public static final FeedType INFUSIONSOFT_LEAD = new FeedType(178);
    public static final FeedType INFUSIONSOFT_STAGE = new FeedType(179);
    public static final FeedType INFUSIONSOFT_STAGE_HISTORY = new FeedType(180);
    public static final FeedType INFUSIONSOFT_COMPANIES = new FeedType(181);
    public static final FeedType INFUSIONSOFT_CONTACTS = new FeedType(182);
    public static final FeedType INFUSIONSOFT_AFFILIATES = new FeedType(183);
    public static final FeedType INFUSIONSOFT_JOBS = new FeedType(184);
    public static final FeedType INFUSIONSOFT_SUBSCRIPTIONS = new FeedType(185);
    public static final FeedType INFUSIONSOFT_PRODUCTS = new FeedType(186);
    public static final FeedType INFUSIONSOFT_PRODUCT_INTEREST = new FeedType(187);
    public static final FeedType INFUSIONSOFT_CONTACT_ACTION = new FeedType(188);
    public static final FeedType INFUSIONSOFT_CAMPAIGNS = new FeedType(189);
    public static final FeedType INFUSIONSOFT_RECURRING_ORDERS = new FeedType(190);
    public static final FeedType INFUSIONSOFT_ORDER_ITEM = new FeedType(191);
    public static final FeedType CACHED_ADDON = new FeedType(192);
    public static final FeedType DISTINCT_CACHED_ADDON = new FeedType(193);
    public static final FeedType HARVEST_PAYMENT = new FeedType(194);
    public static final FeedType INFUSIONSOFT_PAYMENT = new FeedType(195);
    public static final FeedType INFUSIONSOFT_INVOICES = new FeedType(196);
    public static final FeedType INFUSIONSOFT_INVOICE_ITEM = new FeedType(197);
    public static final FeedType INFUSIONSOFT_INVOICE_PAYMENT = new FeedType(198);
    public static final FeedType INFUSIONSOFT_LEAD_SOURCE = new FeedType(199);
    public static final FeedType INFUSIONSOFT_PAY_PLAN = new FeedType(200);
    public static final FeedType INFUSIONSOFT_EXPENSES = new FeedType(201);
    public static final FeedType INFUSIONSOFT_JOB_RECURRING_INSTANCE = new FeedType(202);

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
