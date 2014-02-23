package com.easyinsight.datafeeds;

import com.easyinsight.datafeeds.admin.AdminStatsDataSource;
import com.easyinsight.datafeeds.basecamp.*;
import com.easyinsight.datafeeds.basecampnext.*;
import com.easyinsight.datafeeds.batchbook.*;
import com.easyinsight.datafeeds.batchbook2.*;
import com.easyinsight.datafeeds.blank.BlankDataSource;
import com.easyinsight.datafeeds.campaignmonitor.CMClientSource;
import com.easyinsight.datafeeds.campaignmonitor.CampaignMonitorDataSource;
import com.easyinsight.datafeeds.cleardb.ClearDBCompositeSource;
import com.easyinsight.datafeeds.cleardb.ClearDBDataSource;
import com.easyinsight.datafeeds.cloudwatch.*;
import com.easyinsight.datafeeds.composite.FederatedDataSource;
import com.easyinsight.datafeeds.constantcontact.*;
import com.easyinsight.datafeeds.database.*;
import com.easyinsight.datafeeds.file.FileBasedFeedDefinition;
import com.easyinsight.datafeeds.freshbooks.*;
import com.easyinsight.datafeeds.freshdesk.FreshdeskCompositeSource;
import com.easyinsight.datafeeds.freshdesk.FreshdeskTicketSource;
import com.easyinsight.datafeeds.ganalytics.GoogleAnalyticsDataSource;
import com.easyinsight.datafeeds.github.GithubCompositeSource;
import com.easyinsight.datafeeds.github.GithubRepositorySource;
import com.easyinsight.datafeeds.google.GoogleFeedDefinition;
import com.easyinsight.datafeeds.harvest.*;
import com.easyinsight.datafeeds.highrise.*;
/*import com.easyinsight.datafeeds.kashoo.KashooBusinessSource;
import com.easyinsight.datafeeds.kashoo.KashooCompositeSource;*/
import com.easyinsight.datafeeds.infusionsoft.*;
import com.easyinsight.datafeeds.insightly.*;
import com.easyinsight.datafeeds.json.JSONDataSource;
import com.easyinsight.datafeeds.kashoo.KashooAccountSource;
import com.easyinsight.datafeeds.kashoo.KashooBusinessSource;
import com.easyinsight.datafeeds.kashoo.KashooCompositeSource;
import com.easyinsight.datafeeds.kashoo.KashooRecordSource;
import com.easyinsight.datafeeds.linkedin.LinkedInDataSource;
import com.easyinsight.datafeeds.meetup.MeetupDataSource;
import com.easyinsight.datafeeds.pivotaltracker.PivotalTrackerBaseSource;
import com.easyinsight.datafeeds.pivotaltrackerv5.*;
import com.easyinsight.datafeeds.quickbase.QuickbaseCompositeSource;
import com.easyinsight.datafeeds.quickbase.QuickbaseDatabaseSource;
import com.easyinsight.datafeeds.quickbase.QuickbaseUserSource;
import com.easyinsight.datafeeds.redbooth.*;
import com.easyinsight.datafeeds.redirect.RedirectDataSource;
import com.easyinsight.datafeeds.salesforce.SalesforceBaseDataSource;
import com.easyinsight.datafeeds.salesforce.SalesforceSObjectSource;
import com.easyinsight.datafeeds.sample.SampleCustomerDataSource;
import com.easyinsight.datafeeds.sample.SampleDataSource;
import com.easyinsight.datafeeds.sample.SampleProductDataSource;
import com.easyinsight.datafeeds.sample.SampleSalesDataSource;
import com.easyinsight.datafeeds.sendgrid.SendGridDataSource;
/*import com.easyinsight.datafeeds.solve360.Solve360ActivitiesSource;
import com.easyinsight.datafeeds.solve360.Solve360CompositeSource;
import com.easyinsight.datafeeds.solve360.Solve360ContactsSource;
import com.easyinsight.datafeeds.solve360.Solve360OpportunitiesSource;*/
import com.easyinsight.datafeeds.smartsheet.SmartsheetTableSource;
import com.easyinsight.datafeeds.solve360.*;
import com.easyinsight.datafeeds.test.TestAlphaDataSource;
import com.easyinsight.datafeeds.test.TestBetaDataSource;
import com.easyinsight.datafeeds.test.TestGammaDataSource;
/*import com.easyinsight.datafeeds.twilio.TwilioCompositeSource;
import com.easyinsight.datafeeds.twilio.TwilioSomethingSource;*/

import com.easyinsight.datafeeds.treasuredata.TreasureDataQuerySource;
import com.easyinsight.datafeeds.trello.*;
import com.easyinsight.datafeeds.wholefoods.WholeFoodsSource;
/*import com.easyinsight.datafeeds.xero.XeroAccountSource;
import com.easyinsight.datafeeds.xero.XeroBankTransactionSource;
import com.easyinsight.datafeeds.xero.XeroCompositeSource;*/
import com.easyinsight.datafeeds.wufoo.WufooCompositeSource;
import com.easyinsight.datafeeds.wufoo.WufooFormSource;
import com.easyinsight.datafeeds.youtrack.YouTrackCompositeSource;
import com.easyinsight.datafeeds.youtrack.YouTrackIssueSource;
import com.easyinsight.datafeeds.youtrack.YouTrackProjectSource;
import com.easyinsight.datafeeds.zendesk.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: jamesboe
 * Date: Oct 30, 2009
 * Time: 10:07:19 PM
 */
public class DataSourceTypeRegistry {

    private Map<FeedType, Class> dataSourceMap = new HashMap<FeedType, Class>();
    private Set<Integer> exchangeTypes = new HashSet<Integer>();
    private Map<FeedType, Integer> connectionBillingInfoMap = new HashMap<FeedType, Integer>();

    public DataSourceTypeRegistry() {
        registerTypes();
        registerExchangeTypes();
        registerConnectionBillingTypes();
    }

    public boolean isExchangeType(int type) {
        return exchangeTypes.contains(type);
    }

    private void registerConnectionBillingTypes() {
        connectionBillingInfoMap.put(FeedType.QUICKBASE_COMPOSITE, ConnectionBillingType.QUICKBASE);

        connectionBillingInfoMap.put(FeedType.BASECAMP_MASTER, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.BASECAMP_NEXT_COMPOSITE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.BATCHBOOK2_COMPOSITE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.BATCHBOOK_COMPOSITE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.CONSTANT_CONTACT, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.FRESHBOOKS_COMPOSITE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.FRESHDESK_COMPOSITE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.GITHUB_COMPOSITE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.GOOGLE_ANALYTICS, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.HARVEST_COMPOSITE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.HIGHRISE_COMPOSITE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.INFUSIONSOFT_COMPOSITE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.INSIGHTLY_COMPOSITE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.KASHOO_COMPOSITE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.LINKEDIN, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.PIVOTAL_TRACKER, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.PIVOTAL_V5_COMPOSITE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.SALESFORCE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.SENDGRID, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.SOLVE360_COMPOSITE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.TRELLO_COMPOSITE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.WUFOO_COMPOSITE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.XERO_COMPOSITE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.YOUTRACK_COMPOSITE, ConnectionBillingType.SMALL_BIZ);
        connectionBillingInfoMap.put(FeedType.ZENDESK_COMPOSITE, ConnectionBillingType.SMALL_BIZ);

        connectionBillingInfoMap.put(FeedType.CLEARDB_COMPOSITE, ConnectionBillingType.CUSTOM_DATA);
        connectionBillingInfoMap.put(FeedType.DATABASE_CONNECTION, ConnectionBillingType.CUSTOM_DATA);
        connectionBillingInfoMap.put(FeedType.DEFAULT, ConnectionBillingType.CUSTOM_DATA);
        connectionBillingInfoMap.put(FeedType.JSON, ConnectionBillingType.CUSTOM_DATA);
        connectionBillingInfoMap.put(FeedType.SERVER_MYSQL, ConnectionBillingType.CUSTOM_DATA);
        connectionBillingInfoMap.put(FeedType.SERVER_SQL_SERVER, ConnectionBillingType.CUSTOM_DATA);
        connectionBillingInfoMap.put(FeedType.SERVER_POSTGRES, ConnectionBillingType.CUSTOM_DATA);
        connectionBillingInfoMap.put(FeedType.SERVER_ORACLE, ConnectionBillingType.CUSTOM_DATA);
        connectionBillingInfoMap.put(FeedType.SMARTSHEET_TABLE, ConnectionBillingType.CUSTOM_DATA);
        connectionBillingInfoMap.put(FeedType.STATIC, ConnectionBillingType.CUSTOM_DATA);
        connectionBillingInfoMap.put(FeedType.TREASURE_DATA, ConnectionBillingType.CUSTOM_DATA);
    }

    public int billingInfoForType(FeedType feedType) {
        Integer type = connectionBillingInfoMap.get(feedType);
        if (type == null) {
            return 0;
        }
        return type;
    }

    private void registerExchangeTypes() {
        exchangeTypes.add(FeedType.SALESFORCE.getType());
        exchangeTypes.add(FeedType.BASECAMP_MASTER.getType());
        exchangeTypes.add(FeedType.GOOGLE_ANALYTICS.getType());
        exchangeTypes.add(FeedType.CLOUD_WATCH.getType());
        exchangeTypes.add(FeedType.HIGHRISE_COMPOSITE.getType());
        exchangeTypes.add(FeedType.PIVOTAL_TRACKER.getType());
        exchangeTypes.add(FeedType.SENDGRID.getType());
        exchangeTypes.add(FeedType.LINKEDIN.getType());
        exchangeTypes.add(FeedType.FRESHBOOKS_COMPOSITE.getType());
        exchangeTypes.add(FeedType.CONSTANT_CONTACT.getType());
        exchangeTypes.add(FeedType.BATCHBOOK_COMPOSITE.getType());
        exchangeTypes.add(FeedType.ZENDESK_COMPOSITE.getType());
        exchangeTypes.add(FeedType.HARVEST_COMPOSITE.getType());
        exchangeTypes.add(FeedType.BASECAMP_NEXT_COMPOSITE.getType());
        exchangeTypes.add(FeedType.YOUTRACK_COMPOSITE.getType());
        exchangeTypes.add(FeedType.INSIGHTLY_COMPOSITE.getType());
        exchangeTypes.add(FeedType.BATCHBOOK2_COMPOSITE.getType());
    }

    public Set<Integer> getExchangeTypes() {
        return exchangeTypes;
    }

    private void registerTypes() {
        registerType(FeedType.STATIC, FileBasedFeedDefinition.class);
        registerType(FeedType.ANALYSIS_BASED, AnalysisBasedFeedDefinition.class);
        registerType(FeedType.GOOGLE, GoogleFeedDefinition.class);
        registerType(FeedType.COMPOSITE, CompositeFeedDefinition.class);
        registerType(FeedType.SALESFORCE, SalesforceBaseDataSource.class);
        registerType(FeedType.SALESFORCE_SUB, SalesforceSObjectSource.class);
        registerType(FeedType.DEFAULT, FeedDefinition.class);
        registerType(FeedType.BASECAMP_MASTER, BaseCampCompositeSource.class);
        registerType(FeedType.ADMIN_STATS, AdminStatsDataSource.class);
        registerType(FeedType.GOOGLE_ANALYTICS, GoogleAnalyticsDataSource.class);
        registerType(FeedType.TEST_ALPHA, TestAlphaDataSource.class);
        registerType(FeedType.TEST_BETA, TestBetaDataSource.class);
        registerType(FeedType.TEST_GAMMA, TestGammaDataSource.class);
        registerType(FeedType.BASECAMP, BaseCampTodoSource.class);
        registerType(FeedType.BASECAMP_TIME, BaseCampTimeSource.class);
        registerType(FeedType.BASECAMP_COMPANY, BaseCampCompanySource.class);
        registerType(FeedType.BASECAMP_COMPANY_PROJECT_JOIN, BaseCampCompanyProjectJoinSource.class);
        registerType(FeedType.CLOUD_WATCH, CloudWatchDataSource.class);
        registerType(FeedType.HIGHRISE_COMPOSITE, HighRiseCompositeSource.class);
        registerType(FeedType.HIGHRISE_COMPANY, HighRiseCompanySource.class);
        registerType(FeedType.HIGHRISE_DEAL, HighRiseDealSource.class);
        registerType(FeedType.HIGHRISE_CONTACTS, HighRiseContactSource.class);
        registerType(FeedType.PIVOTAL_TRACKER, PivotalTrackerBaseSource.class);
        registerType(FeedType.SENDGRID, SendGridDataSource.class);
        registerType(FeedType.MEETUP, MeetupDataSource.class);
        registerType(FeedType.LINKEDIN, LinkedInDataSource.class);
        registerType(FeedType.HIGHRISE_CASES, HighRiseCaseSource.class);
        registerType(FeedType.HIGHRISE_TASKS, HighRiseTaskSource.class);
        registerType(FeedType.HIGHRISE_EMAILS, HighRiseEmailSource.class);
        registerType(FeedType.FRESHBOOKS_INVOICE, FreshbooksInvoiceSource.class);
        registerType(FeedType.FRESHBOOKS_CLIENTS, FreshbooksClientSource.class);
        registerType(FeedType.FRESHBOOKS_EXPENSES, FreshbooksExpenseSource.class);
        registerType(FeedType.FRESHBOOKS_PAYMENTS, FreshbooksPaymentSource.class);
        registerType(FeedType.FRESHBOOKS_STAFF, FreshbooksStaffSource.class);
        registerType(FeedType.FRESHBOOKS_TASKS, FreshbooksTaskSource.class);
        registerType(FeedType.FRESHBOOKS_TIME_ENTRIES, FreshbooksTimeEntrySource.class);
        registerType(FeedType.FRESHBOOKS_COMPOSITE, FreshbooksCompositeSource.class);
        registerType(FeedType.FRESHBOOKS_CATEGORIES, FreshbooksCategorySource.class);
        registerType(FeedType.FRESHBOOKS_ESTIMATES, FreshbooksEstimateSource.class);
        registerType(FeedType.FRESHBOOKS_PROJECTS, FreshbooksProjectSource.class);
        registerType(FeedType.FRESHBOOKS_LINE_ITEMS, FreshbooksInvoiceLineSource.class);
        registerType(FeedType.BASECAMP_COMMENTS, BaseCampCommentsSource.class);
        registerType(FeedType.REDIRECT, RedirectDataSource.class);
        registerType(FeedType.WHOLE_FOODS, WholeFoodsSource.class);
        registerType(FeedType.HIGHRISE_CASE_NOTES, HighRiseCaseNotesSource.class);
        registerType(FeedType.HIGHRISE_COMPANY_NOTES, HighRiseCompanyNotesSource.class);
        registerType(FeedType.HIGHRISE_CONTACT_NOTES, HighRiseContactNotesSource.class);
        registerType(FeedType.HIGHRISE_DEAL_NOTES, HighRiseDealNotesSource.class);
        registerType(FeedType.HIGHRISE_CASE_JOIN, HighRiseCaseJoinSource.class);
        registerType(FeedType.CONSTANT_CONTACT, ConstantContactCompositeSource.class);
        registerType(FeedType.CONSTANT_CONTACT_CONTACT_LISTS, CCContactListSource.class);
        registerType(FeedType.CONSTANT_CONTACT_CONTACTS, CCContactSource.class);
        registerType(FeedType.CONSTANT_CONTACT_CONTACT_TO_CONTACT_LIST, CCContactToContactListSource.class);
        registerType(FeedType.CONSTANT_CONTACT_CAMPAIGN, CCCampaignSource.class);
        registerType(FeedType.CONSTANT_CONTACT_CAMPAIGN_RESULTS, CCCampaignResultsSource.class);
        registerType(FeedType.BASECAMP_TODO_COMMENTS, BaseCampTodoCommentsSource.class);
        registerType(FeedType.QUICKBASE_COMPOSITE, QuickbaseCompositeSource.class);
        registerType(FeedType.QUICKBASE_CHILD, QuickbaseDatabaseSource.class);
        registerType(FeedType.CLEARDB_COMPOSITE, ClearDBCompositeSource.class);
        registerType(FeedType.CLEARDB_CHILD, ClearDBDataSource.class);
        registerType(FeedType.BATCHBOOK_COMPOSITE, BatchbookCompositeSource.class);
        registerType(FeedType.BATCHBOOK_DEALS, BatchbookDealSource.class);
        registerType(FeedType.BATCHBOOK_PEOPLE, BatchbookPeopleSource.class);
        registerType(FeedType.BATCHBOOK_COMPANIES, BatchbookCompanySource.class);
        registerType(FeedType.BATCHBOOK_COMMUNICATIONS, BatchbookCommunicationsSource.class);
        registerType(FeedType.BATCHBOOK_TODOS, BatchbookTodoSource.class);
        registerType(FeedType.BATCHBOOK_USERS, BatchbookUserSource.class);
        registerType(FeedType.BATCHBOOK_COMMUNICATION_PARTIES, BatchbookCommunicationsPartySource.class);
        registerType(FeedType.CAMPAIGN_MONITOR_COMPOSITE, CampaignMonitorDataSource.class);
        registerType(FeedType.CAMPAIGN_MONITOR_CLIENTS, CMClientSource.class);
        registerType(FeedType.FEDERATED, FederatedDataSource.class);
        registerType(FeedType.HARVEST_COMPOSITE, HarvestCompositeSource.class);
        registerType(FeedType.HARVEST_CLIENT, HarvestClientSource.class);
        registerType(FeedType.HARVEST_PROJECT, HarvestProjectSource.class);
        registerType(FeedType.HARVEST_TIME, HarvestTimeSource.class);
        registerType(FeedType.HARVEST_CONTACTS, HarvestClientContactSource.class);
        registerType(FeedType.HARVEST_TASKS, HarvestTaskSource.class);
        registerType(FeedType.HARVEST_TASK_ASSIGNMENTS, HarvestTaskAssignmentSource.class);
        registerType(FeedType.HARVEST_USER_ASSIGNMENTS, HarvestUserAssignmentSource.class);
        registerType(FeedType.HARVEST_USERS, HarvestUserSource.class);
        registerType(FeedType.HARVEST_EXPENSES, HarvestExpenseSource.class);
        registerType(FeedType.HARVEST_EXPENSE_CATEGORIES, HarvestExpenseCategoriesSource.class);
        registerType(FeedType.HARVEST_INVOICES, HarvestInvoiceSource.class);
        registerType(FeedType.ZENDESK_COMPOSITE, ZendeskCompositeSource.class);
        registerType(FeedType.ZENDESK_GROUP, ZendeskGroupSource.class);
        registerType(FeedType.ZENDESK_USER, ZendeskUserSource.class);
        registerType(FeedType.ZENDESK_ORGANIZATION, ZendeskOrganizationSource.class);
        registerType(FeedType.ZENDESK_TICKET, ZendeskTicketSource.class);
        registerType(FeedType.ZENDESK_GROUP_TO_USER, ZendeskGroupToUserJoinSource.class);
        registerType(FeedType.AMAZON_EC2, AmazonEC2Source.class);
        registerType(FeedType.AMAZON_EBS, AmazonEBSSource.class);
        registerType(FeedType.AMAZON_SQS, AmazonSQSSource.class);
        registerType(FeedType.AMAZON_RDS, AmazonRDSSource.class);
        registerType(FeedType.SAMPLE_COMPOSITE, SampleDataSource.class);
        registerType(FeedType.SAMPLE_CUSTOMER, SampleCustomerDataSource.class);
        registerType(FeedType.SAMPLE_PRODUCT, SampleProductDataSource.class);
        registerType(FeedType.SAMPLE_SALES, SampleSalesDataSource.class);
        /*registerType(FeedType.KASHOO_COMPOSITE, KashooCompositeSource.class);
        registerType(FeedType.KASHOO_BUSINESSES, KashooBusinessSource.class);
        registerType(FeedType.SOLVE360_COMPOSITE, Solve360CompositeSource.class);
        registerType(FeedType.SOLVE360_CONTACTS, Solve360ContactsSource.class);
        registerType(FeedType.SOLVE360_OPPORTUNITIES, Solve360OpportunitiesSource.class);
        registerType(FeedType.SOLVE360_ACTIVITIES, Solve360ActivitiesSource.class);
        registerType(FeedType.XERO_COMPOSITE, XeroCompositeSource.class);
        registerType(FeedType.XERO_ACCOUNTS, XeroAccountSource.class);
        registerType(FeedType.XERO_BANK_TRANSACTIONS, XeroBankTransactionSource.class);
        registerType(FeedType.CUSTOM_REST_LIVE, CustomRestSource.class);
        registerType(FeedType.TWILIO_COMPOSITE, TwilioCompositeSource.class);
        registerType(FeedType.TWILIO_SOMETHING, TwilioSomethingSource.class);*/
        registerType(FeedType.BASECAMP_NEXT_COMPOSITE, BasecampNextCompositeSource.class);
        registerType(FeedType.BASECAMP_NEXT_PROJECTS, BasecampNextProjectSource.class);
        registerType(FeedType.BASECAMP_NEXT_TODOS, BasecampNextTodoSource.class);
        registerType(FeedType.BASECAMP_NEXT_CALENDAR, BasecampNextCalendarSource.class);
        registerType(FeedType.BASECAMP_NEXT_PEOPLE, BasecampNextPeopleSource.class);
        registerType(FeedType.BATCHBOOK_SUPER_TAG, BatchbookSuperTagSource.class);
        registerType(FeedType.JSON, JSONDataSource.class);
        registerType(FeedType.HIGHRISE_ACTIVITIES, HighRiseActivitySource.class);
        registerType(FeedType.BATCHBOOK2_COMPOSITE, Batchbook2CompositeSource.class);
        registerType(FeedType.BATCHBOOK2_PEOPLE, Batchbook2PeopleSource.class);
        registerType(FeedType.BATCHBOOK2_ADDRESSES, Batchbook2AddressSource.class);
        registerType(FeedType.BATCHBOOK2_EMAILS, Batchbook2EmailSource.class);
        registerType(FeedType.BATCHBOOK2_PHONES, Batchbook2PhoneSource.class);
        registerType(FeedType.BATCHBOOK2_WEBSITES, Batchbook2WebsiteSource.class);
        registerType(FeedType.BATCHBOOK2_COMPANIES, Batchbook2CompanySource.class);
        registerType(FeedType.BATCHBOOK2_CUSTOM, Batchbook2CustomFieldSource.class);
        registerType(FeedType.TREASURE_DATA, TreasureDataQuerySource.class);
        registerType(FeedType.DATABASE_CONNECTION, DatabaseConnection.class);
        registerType(FeedType.QUICKBASE_USER_CHILD, QuickbaseUserSource.class);
        registerType(FeedType.YOUTRACK_COMPOSITE, YouTrackCompositeSource.class);
        registerType(FeedType.YOUTRACK_PROJECTS, YouTrackProjectSource.class);
        registerType(FeedType.YOUTRACK_TASKS, YouTrackIssueSource.class);
        registerType(FeedType.ZENDESK_COMMENTS, ZendeskCommentSource.class);
        registerType(FeedType.WUFOO_COMPOSITE, WufooCompositeSource.class);
        registerType(FeedType.WUFOO_FORM, WufooFormSource.class);
        registerType(FeedType.INSIGHTLY_COMPOSITE, InsightlyCompositeSource.class);
        registerType(FeedType.INSIGHTLY_CONTACTS, InsightlyContactSource.class);
        registerType(FeedType.INSIGHTLY_ORGANIZATIONS, InsightlyOrganisationSource.class);
        registerType(FeedType.INSIGHTLY_OPPORTUNITIES, InsightlyOpportunitySource.class);
        registerType(FeedType.INSIGHTLY_PROJECTS, InsightlyProjectSource.class);
        registerType(FeedType.INSIGHTLY_TASKS, InsightlyTaskSource.class);
        registerType(FeedType.SERVER_MYSQL, MySQLDatabaseConnection.class);
        registerType(FeedType.SERVER_SQL_SERVER, SQLServerDatabaseConnection.class);
        registerType(FeedType.SERVER_ORACLE, OracleDatabaseConnection.class);
        registerType(FeedType.SERVER_POSTGRES, PostgresDatabaseConnection.class);
        registerType(FeedType.TRELLO_COMPOSITE, TrelloCompositeSource.class);
        registerType(FeedType.TRELLO_CARD, TrelloCardSource.class);
        registerType(FeedType.TRELLO_BOARD, TrelloBoardSource.class);
        registerType(FeedType.TRELLO_LIST, TrelloListSource.class);
        registerType(FeedType.TRELLO_CARD_HISTORY, TrelloCardHistorySource.class);
        registerType(FeedType.CONSTANT_CONTACT_LINKS, CCCampaignLinkSource.class);
        registerType(FeedType.INFUSIONSOFT_COMPOSITE, InfusionsoftCompositeSource.class);
        registerType(FeedType.INFUSIONSOFT_LEAD, InfusionsoftLeadSource.class);
        registerType(FeedType.INFUSIONSOFT_STAGE, InfusionsoftStageSource.class);
        registerType(FeedType.INFUSIONSOFT_STAGE_HISTORY, InfusionsoftStageMoveSource.class);
        registerType(FeedType.INFUSIONSOFT_AFFILIATES, InfusionsoftAffiliateSource.class);
        registerType(FeedType.INFUSIONSOFT_COMPANIES, InfusionsoftCompanySource.class);
        registerType(FeedType.INFUSIONSOFT_CONTACTS, InfusionsoftContactSource.class);
        registerType(FeedType.INFUSIONSOFT_JOBS, InfusionsoftJobSource.class);
        registerType(FeedType.INFUSIONSOFT_SUBSCRIPTIONS, InfusionsoftSubscriptionSource.class);
        registerType(FeedType.INFUSIONSOFT_PRODUCTS, InfusionsoftProductSource.class);
        registerType(FeedType.INFUSIONSOFT_PRODUCT_INTEREST, InfusionsoftProductInterestSource.class);
        registerType(FeedType.INFUSIONSOFT_CAMPAIGNS, InfusionsoftCampaignSource.class);
        registerType(FeedType.INFUSIONSOFT_CONTACT_ACTION, InfusionsoftContactActionSource.class);
        registerType(FeedType.INFUSIONSOFT_RECURRING_ORDERS, InfusionsoftRecurringOrderSource.class);
        registerType(FeedType.INFUSIONSOFT_ORDER_ITEM, InfusionsoftOrderItemSource.class);
        registerType(FeedType.CACHED_ADDON, CachedAddonDataSource.class);
        registerType(FeedType.DISTINCT_CACHED_ADDON, DistinctCachedSource.class);
        registerType(FeedType.HARVEST_PAYMENT, HarvestPaymentSource.class);
        registerType(FeedType.INFUSIONSOFT_PAYMENT, InfusionsoftPaymentSource.class);
        registerType(FeedType.INFUSIONSOFT_INVOICES, InfusionsoftInvoiceSource.class);
        registerType(FeedType.INFUSIONSOFT_INVOICE_ITEM, InfusionsoftInvoiceItemSource.class);
        registerType(FeedType.INFUSIONSOFT_INVOICE_PAYMENT, InfusionsoftInvoicePaymentSource.class);
        registerType(FeedType.INFUSIONSOFT_LEAD_SOURCE, InfusionsoftLeadSourceSource.class);
        registerType(FeedType.INFUSIONSOFT_PAY_PLAN, InfusionsoftPayPlanSource.class);
        registerType(FeedType.INFUSIONSOFT_EXPENSES, InfusionsoftExpenseSource.class);
        registerType(FeedType.INFUSIONSOFT_JOB_RECURRING_INSTANCE, InfusionsoftJobRecurringInstanceSource.class);
        registerType(FeedType.INFUSIONSOFT_EXPENSES, InfusionsoftExpenseSource.class);
        registerType(FeedType.GITHUB_REPOSITORY, GithubRepositorySource.class);
        registerType(FeedType.GITHUB_COMPOSITE, GithubCompositeSource.class);
        registerType(FeedType.INFUSIONSOFT_TAG, InfusionsoftTagSource.class);
        registerType(FeedType.INFUSIONSOFT_CONTACT_TO_TAG, InfusionsoftContactToTag.class);
        registerType(FeedType.FRESHDESK_COMPOSITE, FreshdeskCompositeSource.class);
        registerType(FeedType.FRESHDESK_TICKET, FreshdeskTicketSource.class);
        registerType(FeedType.INFUSIONSOFT_REFERRAL, InfusionsoftReferralSource.class);
        registerType(FeedType.INFUSIONSOFT_CAMPAIGNEE, InfusionsoftCampaigneeSource.class);
        registerType(FeedType.INFUSIONSOFT_CAMPAIGN_STEP, InfusionsoftCampaignStep.class);
        registerType(FeedType.INFUSIONSOFT_TAG_GROUP, InfusionsoftContactGroupCategorySource.class);
        registerType(FeedType.KASHOO_COMPOSITE, KashooCompositeSource.class);
        registerType(FeedType.KASHOO_BUSINESSES, KashooBusinessSource.class);
        registerType(FeedType.KASHOO_RECORDS, KashooRecordSource.class);
        registerType(FeedType.BLANK, BlankDataSource.class);
        registerType(FeedType.KASHOO_ACCOUNTS, KashooAccountSource.class);
        registerType(FeedType.SOLVE360_COMPOSITE, Solve360CompositeSource.class);
        registerType(FeedType.SOLVE360_CONTACTS, Solve360ContactsSource.class);
        registerType(FeedType.SOLVE360_OPPORTUNITIES, Solve360OpportunitiesSource.class);
        registerType(FeedType.SOLVE360_ACTIVITIES, Solve360ActivitiesSource.class);
        registerType(FeedType.SMARTSHEET_TABLE, SmartsheetTableSource.class);
        registerType(FeedType.SOLVE360_COMPANIES, Solve360CompanySource.class);
        registerType(FeedType.PIVOTAL_V5_COMPOSITE, PivotalTrackerV5CompositeSource.class);
        registerType(FeedType.PIVOTAL_V5_PROJECT, PivotalTrackerV5ProjectSource.class);
        registerType(FeedType.PIVOTAL_V5_EPIC, PivotalTrackerV5EpicSource.class);
        registerType(FeedType.PIVOTAL_V5_STORY, PivotalTrackerV5StorySource.class);
        registerType(FeedType.PIVOTAL_V5_LABEL, PivotalTrackerV5LabelSource.class);
        registerType(FeedType.PIVOTAL_V5_ITERATION, PivotalTrackerV5IterationSource.class);
        registerType(FeedType.PIVOTAL_V5_STORY_TO_LABEL, PivotalTrackerV5StoryToLabelSource.class);
        registerType(FeedType.INSIGHTLY_NOTES, InsightlyNoteSource.class);
        registerType(FeedType.INSIGHTLY_NOTE_LINKS, InsightlyNoteLinkSource.class);
        registerType(FeedType.REDBOOTH_COMPOSITE, RedboothCompositeSource.class);
        registerType(FeedType.REDBOOTH_ORGANIZATION, RedboothOrganizationSource.class);
        registerType(FeedType.REDBOOTH_PROJECT, RedboothProjectSource.class);
        registerType(FeedType.REDBOOTH_TASK_LIST, RedboothTaskListSource.class);
        registerType(FeedType.REDBOOTH_TASK, RedboothTaskSource.class);
    }

    public Map<FeedType, Class> getDataSourceMap() {
        return dataSourceMap;
    }

    private void registerType(FeedType feedType, Class dataSourceClass) {
        dataSourceMap.put(feedType, dataSourceClass);
    }

    public FeedDefinition createDataSource(FeedType feedType) {
        try {
            Class clazz = dataSourceMap.get(feedType);
            return (FeedDefinition) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}