package com.easyinsight.datafeeds;

import com.easyinsight.datafeeds.admin.AdminStatsDataSource;
import com.easyinsight.datafeeds.basecamp.BaseCampCompositeSource;
import com.easyinsight.datafeeds.basecamp.BaseCampTimeSource;
import com.easyinsight.datafeeds.basecamp.BaseCampTodoSource;
import com.easyinsight.datafeeds.cloudwatch.CloudWatchDataSource;
import com.easyinsight.datafeeds.file.FileBasedFeedDefinition;
import com.easyinsight.datafeeds.ganalytics.GoogleAnalyticsDataSource;
import com.easyinsight.datafeeds.gnip.GnipDataSource;
import com.easyinsight.datafeeds.google.GoogleFeedDefinition;
import com.easyinsight.datafeeds.highrise.HighRiseCompanySource;
import com.easyinsight.datafeeds.highrise.HighRiseCompositeSource;
import com.easyinsight.datafeeds.highrise.HighRiseDealSource;
import com.easyinsight.datafeeds.jira.JiraDataSource;
import com.easyinsight.datafeeds.salesforce.SalesforceBaseDataSource;
import com.easyinsight.datafeeds.test.TestAlphaDataSource;
import com.easyinsight.datafeeds.test.TestBetaDataSource;
import com.easyinsight.datafeeds.test.TestGammaDataSource;
import com.easyinsight.datafeeds.wesabe.WesabeAccountDataSource;
import com.easyinsight.datafeeds.wesabe.WesabeDataSource;
import com.easyinsight.datafeeds.wesabe.WesabeTransactionDataSource;

import java.util.HashMap;
import java.util.Map;

/**
 * User: jamesboe
 * Date: Oct 30, 2009
 * Time: 10:07:19 PM
 */
public class DataSourceTypeRegistry {

    private Map<FeedType, Class> dataSourceMap = new HashMap<FeedType, Class>();

    public DataSourceTypeRegistry() {
        registerTypes();
    }

    private void registerTypes() {
        registerType(FeedType.STATIC, FileBasedFeedDefinition.class);
        registerType(FeedType.ANALYSIS_BASED, AnalysisBasedFeedDefinition.class);
        registerType(FeedType.GOOGLE, GoogleFeedDefinition.class);
        registerType(FeedType.COMPOSITE, CompositeFeedDefinition.class);
        registerType(FeedType.SALESFORCE, SalesforceBaseDataSource.class);
        registerType(FeedType.DEFAULT, FeedDefinition.class);
        registerType(FeedType.BASECAMP_MASTER, BaseCampCompositeSource.class);
        registerType(FeedType.ADMIN_STATS, AdminStatsDataSource.class);
        registerType(FeedType.GNIP, GnipDataSource.class);
        registerType(FeedType.GOOGLE_ANALYTICS, GoogleAnalyticsDataSource.class);
        registerType(FeedType.TEST_ALPHA, TestAlphaDataSource.class);
        registerType(FeedType.TEST_BETA, TestBetaDataSource.class);
        registerType(FeedType.TEST_GAMMA, TestGammaDataSource.class);
        registerType(FeedType.BASECAMP, BaseCampTodoSource.class);
        registerType(FeedType.BASECAMP_TIME, BaseCampTimeSource.class);
        registerType(FeedType.WESABE, WesabeDataSource.class);
        registerType(FeedType.WESABE_ACCOUNTS, WesabeAccountDataSource.class);
        registerType(FeedType.WESABE_TRANSACTIONS, WesabeTransactionDataSource.class);
        registerType(FeedType.CLOUD_WATCH, CloudWatchDataSource.class);
        registerType(FeedType.HIGHRISE_COMPOSITE, HighRiseCompositeSource.class);
        registerType(FeedType.HIGHRISE_COMPANY, HighRiseCompanySource.class);
        registerType(FeedType.HIGHRISE_DEAL, HighRiseDealSource.class);
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
