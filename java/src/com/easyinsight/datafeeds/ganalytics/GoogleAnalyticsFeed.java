package com.easyinsight.datafeeds.ganalytics;

import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.Feed;
import com.easyinsight.analysis.*;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.core.*;
import com.easyinsight.users.Token;
import com.easyinsight.users.TokenStorage;
import com.easyinsight.users.Utility;
import com.easyinsight.logging.LogClass;
import com.easyinsight.security.SecurityUtil;
import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthSigner;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.AccountEntry;
import com.google.gdata.data.analytics.AccountFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.InvalidEntryException;

import java.util.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;

/**
 * User: James Boe
 * Date: Jun 11, 2009
 * Time: 1:59:16 PM
 */
public class GoogleAnalyticsFeed extends Feed {

    private transient AnalyticsService as;

    private static DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    private static DateFormat outboundDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public GoogleAnalyticsFeed(String oauthToken, String oauthTokenSecret) {
        this.oauthToken = oauthToken;
        this.oauthTokenSecret = oauthTokenSecret;
    }

    public AnalysisItemResultMetadata getMetadata(AnalysisItem analysisItem, InsightRequestMetadata insightRequestMetadata, EIConnection conn) throws ReportException {
        AnalysisItemResultMetadata metadata = analysisItem.createResultMetadata();
        try {
            AnalysisItem queryItem;
            if (analysisItem.hasType(AnalysisItemTypes.HIERARCHY)) {
                AnalysisHierarchyItem analysisHierarchyItem = (AnalysisHierarchyItem) analysisItem;
                queryItem = analysisHierarchyItem.getHierarchyLevel().getAnalysisItem();
            } else {
                queryItem = analysisItem;
            }
            AnalyticsService as = getAnalyticsService();

            if ("title".equals(queryItem.getKey().toKeyString())) {
                String baseUrl = "https://www.google.com/analytics/feeds/accounts/default";
                AccountFeed accountFeed = as.getFeed(new URL(baseUrl), AccountFeed.class);
                for (AccountEntry accountEntry : accountFeed.getEntries()) {
                    String title = accountEntry.getTitle().getPlainText();
                    metadata.addValue(analysisItem, new StringValue(title), insightRequestMetadata);
                }
            } else {
                String baseUrl = "https://www.google.com/analytics/feeds/accounts/default";
                AccountFeed accountFeed = as.getFeed(new URL(baseUrl), AccountFeed.class);
                for (AccountEntry accountEntry : accountFeed.getEntries()) {
                    String ids = accountEntry.getTableId().getValue();
                    StringBuilder urlBuilder = new StringBuilder("https://www.google.com/analytics/feeds/data?ids=");
                    urlBuilder.append(ids);

                    if (analysisItem.hasType(AnalysisItemTypes.DIMENSION)) {
                        urlBuilder.append("&dimensions=");
                        urlBuilder.append(queryItem.getKey().toKeyString());
                        urlBuilder.append("&metrics=");
                        String measure = GoogleAnalyticsDataSource.getMeasure(queryItem.getKey().toKeyString());
                        if (measure == null) {
                            throw new RuntimeException("Could not locate measure for dimension " + queryItem.getKey().toKeyString());
                        }
                        urlBuilder.append(measure);
                    } else {
                        urlBuilder.append("&metrics=");
                        urlBuilder.append(queryItem.getKey().toKeyString());
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.YEAR, -1);
                    String endDateString = outboundDateFormat.format(new Date());
                    String startDateString = outboundDateFormat.format(cal.getTime());
                    urlBuilder.append("&start-date=").append(startDateString).append("&end-date=").append(endDateString);
                    URL reportUrl = new URL(urlBuilder.toString());
                    DataFeed feed = as.getFeed(reportUrl, DataFeed.class);

                    for (DataEntry entry : feed.getEntries()) {
                        metadata.addValue(queryItem, getValue(queryItem, entry), insightRequestMetadata);
                    }
                }
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }

        return metadata;
    }

    private String token;

    private String oauthToken;
    private String oauthTokenSecret;

    private String getToken() throws ReportException {
        if (token == null) {
            Token tokenObject = new TokenStorage().getToken(SecurityUtil.getUserID(false), TokenStorage.GOOGLE_ANALYTICS_TOKEN, getFeedID(), true);
            if (tokenObject == null) {
                throw new ReportException(new DataSourceConnectivityReportFault("You need to reset your connection to Google Analytics.", getDataSource()));
            }
            token = tokenObject.getTokenValue();
        }
        return token;
    }

    private AnalyticsService getAnalyticsService() throws AuthenticationException, ReportException, OAuthException {
        if (as == null) {
            as = new AnalyticsService("easyinsight_eianalytics_v1.0");
            if (oauthToken == null) {
                String token = getToken();
                as.setAuthSubToken(token, Utility.getPrivateKey());
            } else {
                GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
                as.useSsl();
                oauthParameters.setOAuthConsumerKey("www.easy-insight.com");
                oauthParameters.setOAuthConsumerSecret("OG0zlkZFPIe7JdHfLB8qXXYv");
                oauthParameters.setOAuthToken(oauthToken);
                oauthParameters.setOAuthTokenSecret(oauthTokenSecret);
                oauthParameters.setScope("https://www.google.com/analytics/feeds/");
                OAuthSigner signer = new OAuthHmacSha1Signer();
                as.setOAuthCredentials(oauthParameters, signer);
            }
        }
        return as;
    }

    public List<FilterDefinition> getIntrinsicFilters(EIConnection conn) {
        RollingFilterDefinition rollingFilterDefinition = new RollingFilterDefinition();
        AnalysisItem dateField = null;
        for (AnalysisItem analysisItem : getFields()) {
            if (analysisItem.getKey().toKeyString().equals(GoogleAnalyticsDataSource.DATE)) {
                dateField = analysisItem;
            }
        }
        rollingFilterDefinition.setField(dateField);
        rollingFilterDefinition.setIntrinsic(true);
        rollingFilterDefinition.setInterval(MaterializedRollingFilterDefinition.WEEK);
        rollingFilterDefinition.setApplyBeforeAggregation(true);
        return Arrays.asList((FilterDefinition) rollingFilterDefinition);
    }

    public DataSet getAggregateDataSet(Set<AnalysisItem> analysisItems, Collection<FilterDefinition> filters, InsightRequestMetadata insightRequestMetadata, List<AnalysisItem> allAnalysisItems, boolean adminMode, EIConnection conn) throws ReportException {
        try {
            Collection<AnalysisDimension> dimensions = new HashSet<AnalysisDimension>();
            Collection<AnalysisMeasure> measures = new HashSet<AnalysisMeasure>();
            List<AnalysisItem> convertedItems = new ArrayList<AnalysisItem>();
            for (AnalysisItem analysisItem : analysisItems) {
                for (AnalysisItem field : getFields()) {
                    if (field.getKey().toBaseKey().equals(analysisItem.getKey().toBaseKey())) {
                        if (field.hasType(AnalysisItemTypes.DIMENSION) && analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
                            convertedItems.add(field);
                        } else if (field.hasType(AnalysisItemTypes.MEASURE) && analysisItem.hasType(AnalysisItemTypes.DIMENSION)) {
                            convertedItems.add(field);
                        } else {
                            convertedItems.add(analysisItem);
                        }
                    }
                }
            }
            for (AnalysisItem analysisItem : convertedItems) {
                if (analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
                    measures.add((AnalysisMeasure) analysisItem);
                } else {
                    dimensions.add((AnalysisDimension) analysisItem);
                }
            }
            if (measures.size() == 0 && dimensions.size() > 0) {
                measures.add(getDefaultMeasure(dimensions));
            }
            if (measures.size() == 0 && dimensions.size() == 0) {
                return new DataSet();
            }

            DataSet dataSet = new DataSet();

            AnalyticsService as = getAnalyticsService();

            Date startDate = null;
            Date endDate = null;

            Set<String> titleFilters = new HashSet<String>();

            for (FilterDefinition filterDefinition : filters) {
                if (filterDefinition.getField().getKey().toKeyString().equals(GoogleAnalyticsDataSource.DATE)) {
                    if (filterDefinition instanceof FilterDateRangeDefinition) {
                        FilterDateRangeDefinition dateRange = (FilterDateRangeDefinition) filterDefinition;
                        startDate = dateRange.getStartDate();
                        endDate = dateRange.getEndDate();
                    } else if (filterDefinition instanceof RollingFilterDefinition) {
                        RollingFilterDefinition rollingFilterDefinition = (RollingFilterDefinition) filterDefinition;
                        endDate = insightRequestMetadata.getNow();
                        startDate = new Date(MaterializedRollingFilterDefinition.findStartDate(rollingFilterDefinition, endDate));
                    }
                } else if (filterDefinition instanceof FilterValueDefinition) {
                    if (filterDefinition.getField().getKey().toKeyString().equals(GoogleAnalyticsDataSource.TITLE)) {
                        FilterValueDefinition filterValueDefinition = (FilterValueDefinition) filterDefinition;
                        List<Object> values = filterValueDefinition.getFilteredValues();
                        for (Object value : values) {
                            titleFilters.add(value.toString());
                        }
                    }
                }
            }


            String startDateString = outboundDateFormat.format(startDate);
            String endDateString = outboundDateFormat.format(endDate);
            String baseUrl = "https://www.google.com/analytics/feeds/accounts/default";
            AccountFeed accountFeed = as.getFeed(new URL(baseUrl), AccountFeed.class);
            for (AccountEntry accountEntry : accountFeed.getEntries()) {
                String title = accountEntry.getTitle().getPlainText();
                if (!titleFilters.isEmpty() && !titleFilters.contains(title)) {
                    continue;
                }
                String ids = accountEntry.getTableId().getValue();
                StringBuilder urlBuilder = new StringBuilder("https://www.google.com/analytics/feeds/data?ids=");
                urlBuilder.append(ids);

                urlBuilder.append("&dimensions=");
                if (dimensions.size() > 0) {
                    Iterator<AnalysisDimension> dimIter = dimensions.iterator();
                    while (dimIter.hasNext()) {
                        AnalysisDimension analysisDimension = dimIter.next();
                        if ("title".equals(analysisDimension.getKey().toKeyString())) {
                            continue;
                        }
                        urlBuilder.append(analysisDimension.getKey().toKeyString());
                        if (dimIter.hasNext()) {
                            urlBuilder.append(",");
                        }
                    }
                    if (urlBuilder.charAt(urlBuilder.length() - 1) == ',') {
                        urlBuilder.deleteCharAt(urlBuilder.length() - 1);
                    }
                }
                urlBuilder.append("&metrics=");
                Iterator<AnalysisMeasure> measureIter = measures.iterator();
                while (measureIter.hasNext()) {
                    AnalysisMeasure analysisMeasure = measureIter.next();
                    urlBuilder.append(analysisMeasure.getKey().toKeyString());
                    if (measureIter.hasNext()) {
                        urlBuilder.append(",");
                    }
                }
                urlBuilder.append("&start-date=").append(startDateString).append("&end-date=").append(endDateString);
                /*String url = "https://www.google.com/analytics/feeds/data?ids=" + ids + "&dimensions=ga:browser,ga:city,ga:date,ga:visitorType,ga:latitude,ga:longitude" +
                "&metrics=ga:pageviews,ga:bounces,ga:timeOnPage,ga:visitors,ga:visits,ga:timeOnSite" +
                "&start-date=2009-06-09&end-date=2009-06-11";*/
                String next = urlBuilder.toString();
                
                while (next != null) {
                    URL reportUrl = new URL(next);
                    DataFeed feed = as.getFeed(reportUrl, DataFeed.class);
                    
                    for (DataEntry entry : feed.getEntries()) {
                        IRow row = dataSet.createRow();

                        for (AnalysisItem analysisItem : analysisItems) {
                            if ("title".equals(analysisItem.getKey().toKeyString())) {
                                row.addValue(analysisItem.createAggregateKey(), title);
                            } else {
                                row.addValue(analysisItem.createAggregateKey(), getValue(analysisItem, entry));
                            }
                        }

                    }
                    String nextLink = feed.getNextLink() == null ? null : feed.getNextLink().getHref();
                    if (!next.equals(nextLink)) {
                        next = nextLink;
                    } else {
                        next = null;
                    }
                }
            }
            //String ids = "ga:16750246";
            return dataSet;
        } catch (AuthenticationException ae) {
            throw new ReportException(new DataSourceConnectivityReportFault("You need to reauthorize Easy Insight to access your Google data.", getDataSource()));
        } catch (InvalidEntryException iee) {
            throw new ReportException(new GenericReportFault(iee.getMessage()));
        } catch (ReportException tme) {
            throw tme;
        } catch (Exception e) {
            as = null;
            throw new RuntimeException(e);
        }
    }

    private Value getValue(AnalysisItem analysisItem, DataEntry entry) throws ParseException {
        Value value;
        if (analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
            Double doubleValue = entry.doubleValueOf(analysisItem.getKey().toKeyString());
            if (doubleValue == null) {
                doubleValue = (double) 0;
            }
            value = new NumericValue(doubleValue);
        } else if (analysisItem.hasType(AnalysisItemTypes.DATE_DIMENSION)) {
            String key = analysisItem.getKey().toKeyString();
            String date = entry.stringValueOf(key);
            if (date != null) {
                Date dateValue = dateFormat.parse(date);
                value = new DateValue(dateValue);
            } else {
                value = new DateValue(new Date());
            }
        } else {
            value = new StringValue(entry.stringValueOf(analysisItem.getKey().toKeyString()));
        }
        return value;
    }

    public AnalysisMeasure getDefaultMeasure(Collection<AnalysisDimension> dimensions) {
        AnalysisMeasure analysisMeasure = null;
        AnalysisDimension dimension = null;
        Iterator<AnalysisDimension> iter = dimensions.iterator();
        while (dimension == null && iter.hasNext()) {
            AnalysisDimension testDim = iter.next();
            if (!testDim.getKey().toKeyString().equals(GoogleAnalyticsDataSource.DATE) &&
                    !testDim.getKey().toKeyString().equals(GoogleAnalyticsDataSource.SOURCE)) {
                dimension = testDim;
            }
        }
        if (dimension == null) {
            iter = dimensions.iterator();
            while (dimension == null && iter.hasNext()) {
                AnalysisDimension testDim = iter.next();
                if (!testDim.getKey().toKeyString().equals(GoogleAnalyticsDataSource.DATE)) {
                    dimension = testDim;
                }
            }
        }
        String measureName;
        if (dimension == null) {
            measureName = GoogleAnalyticsDataSource.VISITS;
        } else {
            measureName = GoogleAnalyticsDataSource.getMeasure(dimension.getKey().toKeyString());
        }

        for (AnalysisItem analysisItem : getFields()) {
            if (measureName.equals(analysisItem.getKey().toKeyString())) {
                analysisMeasure = (AnalysisMeasure) analysisItem;
            }
        }
        return analysisMeasure;
    }
}
