package com.easyinsight.datafeeds.basecamp;

import com.easyinsight.analysis.*;
import com.easyinsight.datafeeds.*;
import com.easyinsight.datafeeds.composite.CompositeServerDataSource;
import com.easyinsight.datafeeds.composite.ChildConnection;
import com.easyinsight.kpi.KPI;
import com.easyinsight.kpi.KPIUtil;
import com.easyinsight.users.Account;

import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.auth.AuthScope;
import nu.xom.Builder;
import nu.xom.Document;

/**
 * User: James Boe
 * Date: Jun 16, 2009
 * Time: 9:36:35 PM
 */
public class BaseCampCompositeSource extends CompositeServerDataSource {

    private String url;

    public int getDataSourceType() {
        return DataSourceInfo.COMPOSITE_PULL;
    }

    @Override
    protected Map<String, String> createProperties() {
        Map<String, String> properties = super.createProperties();
        properties.put("basecamp.url", url);
        return properties;
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.BASECAMP_MASTER;
    }

    protected Set<FeedType> getFeedTypes() {
        Set<FeedType> feedTypes = new HashSet<FeedType>();
        feedTypes.add(FeedType.BASECAMP);
        feedTypes.add(FeedType.BASECAMP_TIME);
        return feedTypes;
    }

    public int getCredentialsDefinition() {
        return CredentialsDefinition.STANDARD_USERNAME_PW;
    }

    public boolean isConfigured() {
        return url != null && !url.isEmpty();
    }

    private static HttpClient getHttpClient(String username, String password) {
        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);
        Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
        client.getState().setCredentials(new AuthScope(AuthScope.ANY), defaultcreds);
        return client;
    }

    private Document runRestRequest(String path, HttpClient client, Builder builder) throws BaseCampLoginException {
        HttpMethod restMethod = new GetMethod(getUrl() + path);
        restMethod.setRequestHeader("Accept", "application/xml");
        restMethod.setRequestHeader("Content-Type", "application/xml");
        Document doc;
        try {
            client.executeMethod(restMethod);
            doc = builder.build(restMethod.getResponseBodyAsStream());
        } catch (UnknownHostException uhe) {
            throw new RuntimeException("Could not recognize host " + getUrl() + " ");
        }
        catch (nu.xom.ParsingException e) {
            String statusLine = restMethod.getStatusLine().toString();
            if ("HTTP/1.1 404 Not Found".equals(statusLine)) {
                throw new BaseCampLoginException("Could not locate a Basecamp instance at " + getUrl());
            } else {
                throw new BaseCampLoginException("Invalid Basecamp authentication token--you can find the token under your the My Info link in the upper right corner on your Basecamp page.");
            }

        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return doc;
    }

    public String validateCredentials(com.easyinsight.users.Credentials credentials) {
        HttpClient client = getHttpClient(credentials.getUserName(), credentials.getPassword());
        Pattern p = Pattern.compile("(http(s?)://)?([A-Za-z0-9]|\\-)+(\\.(basecamphq|projectpath|seework|clientsection|grouphub|updatelog)\\.com)?");
        Matcher m = p.matcher(url);
        String result = null;
        if(!m.matches()) {
            result = "Invalid url. Please input a proper URL.";
        }
        else {
            try {
                runRestRequest("/projects.xml", client, new Builder());
            } catch (BaseCampLoginException e) {
               result = e.getMessage();
            }
        }
        return result;
    }

    @Override
    public int getRequiredAccountTier() {
        return Account.BASIC;
    }

    protected Collection<ChildConnection> getChildConnections() {
        return Arrays.asList(new ChildConnection(FeedType.BASECAMP,  FeedType.BASECAMP_TIME, BaseCampTodoSource.ITEMID,  BaseCampTimeSource.TODOID));
    }

    protected IServerDataSourceDefinition createForFeedType(FeedType feedType) {
        if (feedType.equals(FeedType.BASECAMP)) {
            return new BaseCampTodoSource();
        } else if (feedType.equals(FeedType.BASECAMP_TIME)) {
            return new BaseCampTimeSource();
        }
        throw new RuntimeException();
    }

    public void customStorage(Connection conn) throws SQLException {
        super.customStorage(conn);
        PreparedStatement clearStmt = conn.prepareStatement("DELETE FROM BASECAMP WHERE DATA_FEED_ID = ?");
        clearStmt.setLong(1, getDataFeedID());
        clearStmt.executeUpdate();
        PreparedStatement basecampStmt = conn.prepareStatement("INSERT INTO BASECAMP (DATA_FEED_ID, URL) VALUES (?, ?)");
        basecampStmt.setLong(1, getDataFeedID());
        basecampStmt.setString(2, getUrl());
        basecampStmt.execute();
    }

    public void customLoad(Connection conn) throws SQLException {
        super.customLoad(conn);
        PreparedStatement loadStmt = conn.prepareStatement("SELECT URL FROM BASECAMP WHERE DATA_FEED_ID = ?");
        loadStmt.setLong(1, getDataFeedID());
        ResultSet rs = loadStmt.executeQuery();
        if (rs.next()) {
            this.setUrl(rs.getString(1));
        }
    }

    public String getUrl() {
        String basecampUrl = ((url.startsWith("http://") || url.startsWith("https://")) ? "" : "http://") + url;
        if(basecampUrl.endsWith("/")) {
            basecampUrl = basecampUrl.substring(0, basecampUrl.length() - 1);
        }
        if(!(basecampUrl.endsWith(".basecamphq.com") || basecampUrl.endsWith(".projectpath.com") || basecampUrl.endsWith(".seework.com")
        || basecampUrl.endsWith(".clientsection.com") || basecampUrl.endsWith(".grouphub.com") || basecampUrl.endsWith(".updatelog.com")))
            basecampUrl = basecampUrl + ".basecamphq.com";
        return basecampUrl;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public FeedDefinition clone(Connection conn) throws CloneNotSupportedException, SQLException {
        BaseCampCompositeSource dataSource = (BaseCampCompositeSource) super.clone(conn);
        dataSource.setUrl("");      
        return dataSource;
    }

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public List<DataSourceMigration> getMigrations() {
        return Arrays.asList((DataSourceMigration) new BaseCamp1To2(this));
    }

    @Override
    public List<KPI> createKPIs() {
        List<KPI> kpis = new ArrayList<KPI>();
        FilterValueDefinition openFilter = new FilterValueDefinition();
        openFilter.setField(findAnalysisItem(BaseCampTodoSource.COMPLETED));
        openFilter.setFilteredValues(Arrays.asList((Object) "false"));
        openFilter.setInclusive(true);
        kpis.add(KPIUtil.createKPIWithFilters("Total Open Todo Items", "inbox.png", (AnalysisMeasure) findAnalysisItem(BaseCampTodoSource.COUNT),
                Arrays.asList((FilterDefinition) openFilter), KPI.BAD));
        FilterValueDefinition closedFilter = new FilterValueDefinition();
        closedFilter.setField(findAnalysisItem(BaseCampTodoSource.COMPLETED));
        closedFilter.setInclusive(true);
        closedFilter.setFilteredValues(Arrays.asList((Object) "false"));
        kpis.add(KPIUtil.createKPIForDateFilter("Todo Items Closed in the Last Seven Days", "inbox.png", (AnalysisMeasure) findAnalysisItem(BaseCampTodoSource.COUNT),
                (AnalysisDimension) findAnalysisItem(BaseCampTodoSource.COMPLETEDDATE), MaterializedRollingFilterDefinition.WEEK,
                Arrays.asList((FilterDefinition) closedFilter), KPI.GOOD));
        kpis.add(KPIUtil.createKPIForDateFilter("Hours Worked Month to Date", "clock.png", (AnalysisMeasure) findAnalysisItem(BaseCampTimeSource.HOURS),
                (AnalysisDimension) findAnalysisItem(BaseCampTimeSource.DATE), MaterializedRollingFilterDefinition.MONTH_TO_NOW,
                null, KPI.GOOD));
        return kpis;
    }
}
