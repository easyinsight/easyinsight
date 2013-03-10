package com.easyinsight.datafeeds.basecampnext;

import com.easyinsight.analysis.DataSourceConnectivityReportFault;
import com.easyinsight.analysis.DataSourceInfo;
import com.easyinsight.analysis.ReportException;
import com.easyinsight.config.ConfigLoader;
import com.easyinsight.core.Key;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.datafeeds.composite.ChildConnection;
import com.easyinsight.datafeeds.composite.CompositeServerDataSource;
import com.easyinsight.users.Account;
import net.smartam.leeloo.client.OAuthClient;
import net.smartam.leeloo.client.URLConnectionClient;
import net.smartam.leeloo.client.request.OAuthClientRequest;
import net.smartam.leeloo.client.response.OAuthJSONAccessTokenResponse;
import net.smartam.leeloo.common.exception.OAuthProblemException;
import net.smartam.leeloo.common.exception.OAuthSystemException;
import net.smartam.leeloo.common.message.types.GrantType;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * User: jamesboe
 * Date: 3/26/12
 * Time: 11:03 AM
 */
public class BasecampNextCompositeSource extends CompositeServerDataSource {

    public static final String CLIENT_ID = "e6630db11f381ce469305018e1e773b6ad4a6a14";
    public static final String CLIENT_SECRET = "f845c2a78ca4df6a19cd23515deda0ce826ff8d0";

    private String accessToken;
    private String refreshToken;
    private String endpoint;

    @Override
    protected void beforeRefresh(Date lastRefreshTime) {
        super.beforeRefresh(lastRefreshTime);

        if (lastRefreshTime != null) {
            try {
                refreshTokenInfo();
            } catch (ReportException re) {
                throw re;
            } catch (Exception e) {
                throw new ReportException(new DataSourceConnectivityReportFault(e.getMessage(), this));
            }
        }
    }

    public void refreshTokenInfo() throws OAuthSystemException, OAuthProblemException {
        try {
            OAuthClientRequest.TokenRequestBuilder tokenRequestBuilder = OAuthClientRequest.tokenLocation("https://launchpad.37signals.com/authorization/token").
                    setGrantType(GrantType.REFRESH_TOKEN).setClientId(CLIENT_ID).
                    setClientSecret(CLIENT_SECRET).setRefreshToken(refreshToken).setRedirectURI("https://easy-insight.com/app/oauth");
            tokenRequestBuilder.setParameter("type", "refresh");
            OAuthClient client = new OAuthClient(new URLConnectionClient());
            OAuthClientRequest request = tokenRequestBuilder.buildBodyMessage();
            OAuthJSONAccessTokenResponse response = client.accessToken(request);
            accessToken = response.getAccessToken();
        } catch (Exception e) {
            throw new ReportException(new DataSourceConnectivityReportFault("You need to reauthorize access to Basecamp.", this));
        }
    }

    @Override
    public void customStorage(Connection conn) throws SQLException {
        super.customStorage(conn);
        PreparedStatement deleteStatement = conn.prepareStatement("delete from basecamp_next where data_source_id = ?");
        deleteStatement.setLong(1, this.getDataFeedID());
        deleteStatement.execute();
        PreparedStatement statement = conn.prepareStatement("insert into basecamp_next (data_source_id, endpoint, access_token, refresh_token) VALUES (?, ?, ?, ?)");
        statement.setLong(1, this.getDataFeedID());
        statement.setString(2, getEndpoint());
        statement.setString(3, getAccessToken());
        statement.setString(4, getRefreshToken());
        statement.execute();
        statement.close();
        deleteStatement.close();
    }

    @Override
    public void customLoad(Connection conn) throws SQLException {
        super.customLoad(conn);
        PreparedStatement statement = conn.prepareStatement("select endpoint, access_token, refresh_token from basecamp_next where data_source_id = ?");
        statement.setLong(1, getDataFeedID());
        ResultSet rs = statement.executeQuery();
        if(rs.next()) {
            this.setEndpoint(rs.getString(1));
            setAccessToken(rs.getString(2));
            setRefreshToken(rs.getString(3));
        }
        statement.close();
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public BasecampNextCompositeSource() {
        setFeedName("Basecamp");
    }

    @Override
    public String validateCredentials() {
        return null;
    }

    @Override
    public boolean checkDateTime(String name, Key key) {
        if (BasecampNextTodoSource.TODO_COMPLETED_AT.equals(name)) {
            return false;
        }
        return true;
    }

    @Override
    public void exchangeTokens(EIConnection conn, HttpServletRequest httpRequest, String externalPin) throws Exception {
        try {
            if (httpRequest != null) {
                String code = httpRequest.getParameter("code");
                if (code != null) {
                    OAuthClientRequest request;
                    if (ConfigLoader.instance().isProduction()) {
                        OAuthClientRequest.TokenRequestBuilder tokenRequestBuilder = OAuthClientRequest.tokenLocation("https://launchpad.37signals.com/authorization/token").
                                setGrantType(GrantType.AUTHORIZATION_CODE).setClientId(CLIENT_ID).
                                setClientSecret(CLIENT_SECRET).
                                setRedirectURI("https://www.easy-insight.com/app/oauth").
                                setCode(code);
                        tokenRequestBuilder.setParameter("type", "web_server");
                        request = tokenRequestBuilder.buildBodyMessage();
                    } else {
                        OAuthClientRequest.TokenRequestBuilder tokenRequestBuilder = OAuthClientRequest.tokenLocation("https://launchpad.37signals.com/authorization/token").
                                setGrantType(GrantType.AUTHORIZATION_CODE).setClientId(CLIENT_ID).
                                setClientSecret(CLIENT_SECRET).
                                setRedirectURI("https://www.easy-insight.com/app/oauth").
                                setCode(code);
                        tokenRequestBuilder.setParameter("type", "web_server");
                        request = tokenRequestBuilder.buildBodyMessage();
                    }
                    OAuthClient client = new OAuthClient(new URLConnectionClient());
                    OAuthJSONAccessTokenResponse response = client.accessToken(request);
                    accessToken = response.getAccessToken();
                    refreshToken = response.getRefreshToken();
                    /*try {
                        this.endpoint = new InitRetrieval().blah(this);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }*/
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getDataSourceType() {
        return DataSourceInfo.COMPOSITE_PULL;
    }

    @Override
    protected Set<FeedType> getFeedTypes() {
        Set<FeedType> feedTypes = new HashSet<FeedType>();
        feedTypes.add(FeedType.BASECAMP_NEXT_PROJECTS);
        feedTypes.add(FeedType.BASECAMP_NEXT_TODOS);
        feedTypes.add(FeedType.BASECAMP_NEXT_CALENDAR);
        feedTypes.add(FeedType.BASECAMP_NEXT_PEOPLE);
        return feedTypes;
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.BASECAMP_NEXT_COMPOSITE;
    }

    @Override
    public int getRequiredAccountTier() {
        return Account.BASIC;
    }

    @Override
    protected Collection<ChildConnection> getChildConnections() {
        return new ArrayList<ChildConnection>();
    }

    @Override
    protected Collection<ChildConnection> getLiveChildConnections() {
        return Arrays.asList(new ChildConnection(FeedType.BASECAMP_NEXT_PROJECTS, FeedType.BASECAMP_NEXT_TODOS, BasecampNextProjectSource.PROJECT_ID, BasecampNextTodoSource.TODO_LIST_PROJECT_ID),
                new ChildConnection(FeedType.BASECAMP_NEXT_PROJECTS, FeedType.BASECAMP_NEXT_CALENDAR, BasecampNextProjectSource.PROJECT_ID, BasecampNextCalendarSource.CALENDAR_EVENT_PROJECT_ID));
    }

    private transient ProjectCache projectCache;

    public ProjectCache getOrCreateProjectCache() throws JSONException {
        if (projectCache == null) {
            projectCache = new ProjectCache();
            projectCache.populate(this);
        }
        return projectCache;
    }

    @Override
    protected void refreshDone() {
        super.refreshDone();
        projectCache = null;
    }

    public Collection<BasecampNextAccount> getBasecampAccounts() {
        try {
            return new InitRetrieval().getAccounts(this);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
