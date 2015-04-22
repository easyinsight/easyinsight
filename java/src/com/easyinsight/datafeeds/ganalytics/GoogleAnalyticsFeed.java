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
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.gdata.client.GoogleService;
import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthSigner;
import com.google.gdata.data.analytics.*;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.InvalidEntryException;
import com.google.gdata.util.ServiceException;
import com.google.gdata.util.ServiceForbiddenException;
import org.apache.amber.oauth2.client.OAuthClient;
import org.apache.amber.oauth2.client.URLConnectionClient;
import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.amber.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.types.GrantType;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.concurrent.Semaphore;

/**
 * User: James Boe
 * Date: Jun 11, 2009
 * Time: 1:59:16 PM
 */
public class GoogleAnalyticsFeed extends Feed {

    private transient AnalyticsService as;

    private static DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    private static DateFormat outboundDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public GoogleAnalyticsFeed(String oauthToken, String oauthTokenSecret, String refreshToken, String accessToken) {
        this.oauthToken = oauthToken;
        this.oauthTokenSecret = oauthTokenSecret;
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    public AnalysisItemResultMetadata getMetadata(AnalysisItem analysisItem, InsightRequestMetadata insightRequestMetadata, EIConnection conn, WSAnalysisDefinition report, List<FilterDefinition> otherFilters, FilterDefinition requester) throws ReportException {
        AnalysisItemResultMetadata metadata = analysisItem.createResultMetadata();
        try {
            semaphore.acquire();
            AnalysisItem queryItem;
            if (analysisItem.hasType(AnalysisItemTypes.HIERARCHY)) {
                AnalysisHierarchyItem analysisHierarchyItem = (AnalysisHierarchyItem) analysisItem;
                queryItem = analysisHierarchyItem.getHierarchyLevel().getAnalysisItem();
            } else {
                queryItem = analysisItem;
            }
            AnalyticsService as = getAnalyticsService();

            if ("title".equals(queryItem.getKey().toKeyString())) {
                URL queryURL = new URL("https://www.googleapis.com/analytics/v2.4/management/accounts");
                ManagementFeed accountsFeed = as.getFeed(queryURL, ManagementFeed.class);
                for (ManagementEntry accountEntry : accountsFeed.getEntries()) {
                    String accountID = accountEntry.getProperty("ga:accountId");
                    ManagementFeed webPropertyFeed = as.getFeed(new URL("https://www.googleapis.com/analytics/v2.4/management/accounts/" + accountID + "/webproperties"), ManagementFeed.class);
                    for (ManagementEntry webPropertyEntry : webPropertyFeed.getEntries()) {

                        String webPropertyID = webPropertyEntry.getProperty("ga:WebPropertyId");
                        ManagementFeed profilesFeed = as.getFeed(new URL("https://www.googleapis.com/analytics/v2.4/management/accounts/" + accountID + "/webproperties/" + webPropertyID + "/profiles"), ManagementFeed.class);
                        for (ManagementEntry profileEntry : profilesFeed.getEntries()) {
                            metadata.addValue(analysisItem, new StringValue(profileEntry.getProperty("ga:profileName")), insightRequestMetadata);
                        }
                    }
                }
            } else {
                URL queryURL = new URL("https://www.googleapis.com/analytics/v2.4/management/accounts");
                ManagementFeed accountsFeed = as.getFeed(queryURL, ManagementFeed.class);
                for (ManagementEntry accountEntry : accountsFeed.getEntries()) {
                    String accountID = accountEntry.getProperty("ga:accountId");
                    ManagementFeed webPropertyFeed = as.getFeed(new URL("https://www.googleapis.com/analytics/v2.4/management/accounts/" + accountID + "/webproperties"), ManagementFeed.class);
                    for (ManagementEntry webPropertyEntry : webPropertyFeed.getEntries()) {

                        String webPropertyID = webPropertyEntry.getProperty("ga:WebPropertyId");
                        ManagementFeed profilesFeed = as.getFeed(new URL("https://www.googleapis.com/analytics/v2.4/management/accounts/" + accountID + "/webproperties/" + webPropertyID + "/profiles"), ManagementFeed.class);
                        for (ManagementEntry profileEntry : profilesFeed.getEntries()) {
                            String ids = profileEntry.getProperty("dxp:tableId");
                            //String ids = "";
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
                            DataFeed feed = null;
                            int retries = 0;
                            Exception sfe = null;
                            do {
                                try {
                                    feed = as.getFeed(reportUrl, DataFeed.class);
                                } catch (ServiceForbiddenException e) {
                                    sfe = e;
                                    if (e.getMessage().contains("usageLimits")) {
                                        Thread.sleep(1000);
                                        System.out.println("retrying...");
                                        retries++;
                                    }
                                } catch (AuthenticationException se1) {
                                    sfe = se1;
                                    if (se1.getMessage().contains("usageLimits")) {
                                        Thread.sleep(1000);
                                        System.out.println("retrying...");
                                        retries++;
                                    }
                                }
                            } while (feed == null && retries < 5);
                            if (feed == null) {
                                throw sfe;
                            }
                            for (DataEntry entry : feed.getEntries()) {
                                metadata.addValue(queryItem, getValue(queryItem, entry), insightRequestMetadata);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
        metadata.calculateCaches();
        return metadata;
    }

    private String token;

    private String oauthToken;
    private String oauthTokenSecret;
    private String refreshToken;
    private String accessToken;

    private AnalyticsService getAnalyticsService() throws AuthenticationException, ReportException, OAuthException {
        if (as == null) {
            as = new AnalyticsService("easyinsight_eianalytics_v1.0");

            if (accessToken != null && !"".equals(accessToken)) {
                as.useSsl();
                GoogleCredential credential = new GoogleCredential();
                credential.setAccessToken(accessToken);
                credential.setRefreshToken(refreshToken);
                as.setOAuth2Credentials(credential);
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

    private static Semaphore semaphore = new Semaphore(1);

    public DataSet getAggregateDataSet(Set<AnalysisItem> analysisItems, Collection<FilterDefinition> filters, InsightRequestMetadata insightRequestMetadata, List<AnalysisItem> allAnalysisItems, boolean adminMode, EIConnection conn) throws ReportException {
        try {
            return createDataSet(analysisItems, filters, insightRequestMetadata);
        } catch (GoogleService.SessionExpiredException gse) {
            try {
                OAuthClientRequest.TokenRequestBuilder tokenRequestBuilder = OAuthClientRequest.tokenLocation("https://www.googleapis.com/oauth2/v3/token").
                        setGrantType(GrantType.REFRESH_TOKEN).setClientId("196763839405.apps.googleusercontent.com").
                        setClientSecret("bRmYcsSJcp0CBehRRIcxl1hK").setRefreshToken(refreshToken).setRedirectURI("https://easy-insight.com/app/oauth");
                //tokenRequestBuilder.setParameter("type", "refresh_token");
                OAuthClient client = new OAuthClient(new URLConnectionClient());
                OAuthClientRequest request = tokenRequestBuilder.buildBodyMessage();
                OAuthJSONAccessTokenResponse response = client.accessToken(request);
                accessToken = response.getAccessToken();
                System.out.println("got new access token");
                try {
                    as = null;
                    return createDataSet(analysisItems, filters, insightRequestMetadata);
                } catch (Exception e1) {
                    as = null;
                    throw new RuntimeException(e1);
                }
            } catch (Exception e) {
                LogClass.error(e);
                throw new RuntimeException(e);
            }
        } catch (AuthenticationException ae) {
            ae.printStackTrace();
            throw new ReportException(new DataSourceConnectivityReportFault("You need to reauthorize Easy Insight to access your Google data.", getDataSource()));
        } catch (InvalidEntryException iee) {
            iee.printStackTrace();
            throw new ReportException(new GenericReportFault(iee.getMessage()));
        } catch (ServiceException se) {
            se.printStackTrace();
            throw new ReportException(new GenericReportFault(se.getMessage()));
        } catch (ReportException tme) {
            throw tme;
        } catch (Exception e) {
            as = null;
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }

    protected DataSet createDataSet(Set<AnalysisItem> analysisItems, Collection<FilterDefinition> filters, InsightRequestMetadata insightRequestMetadata) throws Exception {
        semaphore.acquire();
        Collection<AnalysisDimension> dimensions = new HashSet<AnalysisDimension>();
        Collection<AnalysisMeasure> measures = new HashSet<AnalysisMeasure>();
        List<AnalysisItem> convertedItems = new ArrayList<AnalysisItem>();
        for (AnalysisItem analysisItem : analysisItems) {
            for (AnalysisItem field : getFields()) {
                if (field.isDerived()) {
                    continue;
                }
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

        // https://localhost:4443/app/html/embeddedReport/YexERMtXvYiwSqndQxGX?showToolbar=1&showFilters=1&embedKey=gzQlrQUkhxkr

        Set<String> titleFilters = new HashSet<String>();

        for (FilterDefinition filterDefinition : filters) {
            if (filterDefinition.getField() == null) {
                continue;
            }
            if (filterDefinition.getField().getKey().toKeyString().equals(GoogleAnalyticsDataSource.DATE) ||
                    filterDefinition.getField().toDisplay().equals("Date")) {
                if (filterDefinition instanceof FilterDateRangeDefinition) {
                    FilterDateRangeDefinition dateRange = (FilterDateRangeDefinition) filterDefinition;
                    startDate = dateRange.getStartDate();
                    endDate = dateRange.getEndDate();
                } else if (filterDefinition instanceof RollingFilterDefinition) {
                    RollingFilterDefinition rollingFilterDefinition = (RollingFilterDefinition) filterDefinition;

                    startDate = rollingFilterDefinition.startTime(insightRequestMetadata);
                    if (rollingFilterDefinition.getEndDate() != null) {
                        endDate = rollingFilterDefinition.endTime(insightRequestMetadata);
                    } else {
                        endDate = insightRequestMetadata.getNow();
                    }
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

        if (startDate == null && endDate == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, -1);
            endDate = new Date();
            startDate = cal.getTime();
        }


        String startDateString = outboundDateFormat.format(startDate);
        String endDateString = outboundDateFormat.format(endDate);

        if (startDateString.equals(endDateString)) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, -1);
            endDate = new Date();
            startDate = cal.getTime();
            startDateString = outboundDateFormat.format(startDate);
            endDateString = outboundDateFormat.format(endDate);
        }
        //String baseUrl = "https://www.googleapis.com/analytics/v2.4/data";
        URL queryURL = new URL("https://www.googleapis.com/analytics/v2.4/management/accounts");
        ManagementFeed accountsFeed = as.getFeed(queryURL, ManagementFeed.class);
        //AccountFeed accountFeed = as.getFeed(new URL(baseUrl), AccountFeed.class);

        for (ManagementEntry accountEntry : accountsFeed.getEntries()) {
            /* String title = accountEntry.getTitle().getPlainText();
            if (!titleFilters.isEmpty() && !titleFilters.contains(title)) {
                continue;
            }*/

            //String ids = accountEntry.getTableId().getValue();
            String accountID = accountEntry.getProperty("ga:accountId");
            ManagementFeed webPropertyFeed = as.getFeed(new URL("https://www.googleapis.com/analytics/v2.4/management/accounts/" + accountID + "/webproperties"), ManagementFeed.class);
            for (ManagementEntry webPropertyEntry : webPropertyFeed.getEntries()) {

                String webPropertyID = webPropertyEntry.getProperty("ga:WebPropertyId");
                ManagementFeed profilesFeed = as.getFeed(new URL("https://www.googleapis.com/analytics/v2.4/management/accounts/" + accountID + "/webproperties/" + webPropertyID + "/profiles"), ManagementFeed.class);
                for (ManagementEntry profileEntry : profilesFeed.getEntries()) {
                    String ids = profileEntry.getProperty("dxp:tableId");
                    String title = profileEntry.getProperty("ga:profileName");
                    if (!titleFilters.isEmpty() && !titleFilters.contains(title)) {
                        continue;
                    }
                    //String ids = "";
                    StringBuilder urlBuilder = new StringBuilder("https://www.google.com/analytics/feeds/data?ids=");
                    urlBuilder.append(ids);

                    urlBuilder.append("&dimensions=");
                    StringBuilder dimBuilder = new StringBuilder();
                    if (dimensions.size() > 0) {
                        Iterator<AnalysisDimension> dimIter = dimensions.iterator();
                        while (dimIter.hasNext()) {
                            AnalysisDimension analysisDimension = dimIter.next();
                            if ("title".equals(analysisDimension.getKey().toKeyString())) {
                                continue;
                            }
                            dimBuilder.append(analysisDimension.getKey().toKeyString());
                            if (dimIter.hasNext()) {
                                dimBuilder.append(",");
                            }
                        }
                    }
                    if (!dimBuilder.toString().contains("ga:date")) {
                        if (dimBuilder.length() > 0) {
                            dimBuilder.append("&");
                        }
                        dimBuilder.append("ga:date");
                    }
                    if (dimBuilder.length() > 0 && dimBuilder.charAt(dimBuilder.length() - 1) == ',') {
                        dimBuilder.deleteCharAt(dimBuilder.length() - 1);
                    }
                    urlBuilder.append(dimBuilder);
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
                    String next = urlBuilder.toString();

                    while (next != null) {
                        URL reportUrl = new URL(next);
                        System.out.println("next url = " + next);
                        DataFeed feed = null;
                        int retries = 0;
                        Exception sfe = null;
                        do {
                            try {
                                feed = as.getFeed(reportUrl, DataFeed.class);
                            } catch (ServiceForbiddenException e) {
                                sfe = e;
                                if (e.getMessage().contains("usageLimits")) {
                                    System.out.println("retrying...");
                                    Thread.sleep(1000);
                                    retries++;
                                }
                            } catch (AuthenticationException se1) {
                                sfe = se1;
                                if (se1.getMessage().contains("usageLimits")) {
                                    System.out.println("retrying...");
                                    Thread.sleep(1000);
                                    retries++;
                                }
                            }
                        } while (feed == null && retries < 5);
                        if (feed == null) {
                            throw sfe;
                        }

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
            }
        }
        return dataSet;
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
            try {
                if (date != null) {
                    Date dateValue = dateFormat.parse(date);
                    value = new DateValue(dateValue);
                } else {
                    value = new DateValue(new Date());
                }
            } catch (Exception e) {
                System.out.println(e.getMessage() + " on " + date);
                value = new EmptyValue();
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
