package com.easyinsight.datafeeds.freshbooks;

import com.easyinsight.analysis.*;
import com.easyinsight.core.Key;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.*;
import com.easyinsight.datafeeds.composite.ChildConnection;
import com.easyinsight.datafeeds.composite.CompositeServerDataSource;
import com.easyinsight.users.Account;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: jamesboe
 * Date: Jul 28, 2010
 * Time: 6:46:43 PM
 */
public class FreshbooksCompositeSource extends CompositeServerDataSource {

    public static final String CONSUMER_KEY = "easyinsight";
    public static final String CONSUMER_SECRET = "3gKm7ivgkPCeQZChh7ig9CDMBGratLg6yS";

    public FreshbooksCompositeSource() {
        setFeedName("FreshBooks");
    }

    private boolean liveDataSource;

    private String pin;

    public boolean isLiveDataSource() {
        return liveDataSource;
    }

    public void setLiveDataSource(boolean liveDataSource) {
        this.liveDataSource = liveDataSource;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    private String tokenKey;
    private String tokenSecretKey;

    public String getTokenKey() {
        return tokenKey;
    }

    public void setTokenKey(String tokenKey) {
        this.tokenKey = tokenKey;
    }

    public String getTokenSecretKey() {
        return tokenSecretKey;
    }

    public void setTokenSecretKey(String tokenSecretKey) {
        this.tokenSecretKey = tokenSecretKey;
    }

    @Override
    public String validateCredentials() {
        Pattern p = Pattern.compile("(http(s?)://)?([A-Za-z0-9]|\\-)+(\\.(freshbooks)\\.com)?");
        Matcher m = p.matcher(url);
        String result = null;
        if(!m.matches()) {
            result = "Invalid url. Please input a proper URL.";
        }
        return result;
    }

    private String url = "";

    public String getUrl() {
        if (url == null || "".equals(url)) {
            return url;
        }
        String freshbooksURL = ((url.startsWith("http://") || url.startsWith("https://")) ? "" : "https://") + url;
        if(freshbooksURL.endsWith("/")) {
            freshbooksURL = freshbooksURL.substring(0, freshbooksURL.length() - 1);
        }
        if(!(freshbooksURL.endsWith(".freshbooks.com")))
            freshbooksURL = freshbooksURL + ".freshbooks.com";
        return freshbooksURL;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDataSourceType() {
        if (liveDataSource) {
            return DataSourceInfo.LIVE;
        } else {
            return DataSourceInfo.COMPOSITE_PULL;
        }
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.FRESHBOOKS_COMPOSITE;
    }

    @Override
    public int getRequiredAccountTier() {
        return Account.BASIC;
    }

    @Override
    public void exchangeTokens(EIConnection conn, HttpServletRequest request, String externalPin) throws Exception {
        try {
            if (externalPin != null) {
                pin = externalPin;
            }
            if (pin != null && !"".equals(pin)) {
                OAuthConsumer consumer = (OAuthConsumer) request.getSession().getAttribute("oauthConsumer");
                OAuthProvider provider = (OAuthProvider) request.getSession().getAttribute("oauthProvider");
                provider.retrieveAccessToken(consumer, pin.trim());
                tokenKey = consumer.getToken();
                tokenSecretKey = consumer.getTokenSecret();
                pin = null;
            }
        } catch (OAuthCommunicationException oe) {
            throw new UserMessageException(oe, "The specified verifier token was rejected. Please try to authorize access again.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void customStorage(Connection conn) throws SQLException {
        super.customStorage(conn);
        PreparedStatement clearStmt = conn.prepareStatement("DELETE FROM FRESHBOOKS WHERE DATA_SOURCE_ID = ?");
        clearStmt.setLong(1, getDataFeedID());
        clearStmt.executeUpdate();
        clearStmt.close();
        PreparedStatement basecampStmt = conn.prepareStatement("INSERT INTO FRESHBOOKS (DATA_SOURCE_ID, TOKEN_KEY, TOKEN_SECRET_KEY, URL, LIVE_DATA_SOURCE) VALUES (?, ?, ?, ?, ?)");
        basecampStmt.setLong(1, getDataFeedID());
        basecampStmt.setString(2, tokenKey);
        basecampStmt.setString(3, tokenSecretKey);
        basecampStmt.setString(4, getUrl());
        basecampStmt.setBoolean(5, liveDataSource);
        basecampStmt.execute();
        basecampStmt.close();
    }

    public void customLoad(Connection conn) throws SQLException {
        super.customLoad(conn);
        PreparedStatement loadStmt = conn.prepareStatement("SELECT URL, TOKEN_KEY, TOKEN_SECRET_KEY, LIVE_DATA_SOURCE FROM FRESHBOOKS WHERE DATA_SOURCE_ID = ?");
        loadStmt.setLong(1, getDataFeedID());
        ResultSet rs = loadStmt.executeQuery();
        if (rs.next()) {
            this.setUrl(rs.getString(1));
            setTokenKey(rs.getString(2));
            setTokenSecretKey(rs.getString(3));
            setLiveDataSource(rs.getBoolean(4));
        }
        loadStmt.close();
    }

    @Override
    protected Set<FeedType> getFeedTypes() {
        return new HashSet<FeedType>(Arrays.asList(FeedType.FRESHBOOKS_INVOICE, FeedType.FRESHBOOKS_CLIENTS,
                FeedType.FRESHBOOKS_EXPENSES, FeedType.FRESHBOOKS_CATEGORIES, FeedType.FRESHBOOKS_STAFF,
                FeedType.FRESHBOOKS_PAYMENTS, FeedType.FRESHBOOKS_TASKS, FeedType.FRESHBOOKS_TIME_ENTRIES,
                FeedType.FRESHBOOKS_PROJECTS, FeedType.FRESHBOOKS_ESTIMATES, FeedType.FRESHBOOKS_LINE_ITEMS));
    }

    @Override
    protected Collection<ChildConnection> getChildConnections() {
        return new ArrayList<ChildConnection>();
    }

    @Override
    protected Collection<ChildConnection> getLiveChildConnections() {
        return Arrays.asList(new ChildConnection(FeedType.FRESHBOOKS_INVOICE, FeedType.FRESHBOOKS_CLIENTS, FreshbooksInvoiceSource.CLIENT_ID, FreshbooksClientSource.CLIENT_ID),
                new ChildConnection(FeedType.FRESHBOOKS_EXPENSES, FeedType.FRESHBOOKS_CATEGORIES, FreshbooksExpenseSource.CATEGORY_ID, FreshbooksCategorySource.CATEGORY_ID),
                new ChildConnection(FeedType.FRESHBOOKS_EXPENSES, FeedType.FRESHBOOKS_STAFF, FreshbooksExpenseSource.STAFF_ID, FreshbooksStaffSource.STAFF_ID),
                new ChildConnection(FeedType.FRESHBOOKS_EXPENSES, FeedType.FRESHBOOKS_PROJECTS, FreshbooksExpenseSource.PROJECT_ID, FreshbooksProjectSource.PROJECT_ID),
                new ChildConnection(FeedType.FRESHBOOKS_EXPENSES, FeedType.FRESHBOOKS_CLIENTS, FreshbooksExpenseSource.CLIENT_ID, FreshbooksClientSource.CLIENT_ID),
                new ChildConnection(FeedType.FRESHBOOKS_ESTIMATES, FeedType.FRESHBOOKS_CLIENTS, FreshbooksEstimateSource.CLIENT_ID, FreshbooksClientSource.CLIENT_ID),
                new ChildConnection(FeedType.FRESHBOOKS_PAYMENTS, FeedType.FRESHBOOKS_CLIENTS, FreshbooksPaymentSource.CLIENT_ID, FreshbooksClientSource.CLIENT_ID),
                new ChildConnection(FeedType.FRESHBOOKS_PAYMENTS, FeedType.FRESHBOOKS_INVOICE, FreshbooksPaymentSource.INVOICE_ID, FreshbooksInvoiceSource.INVOICE_ID),
                new ChildConnection(FeedType.FRESHBOOKS_PROJECTS, FeedType.FRESHBOOKS_CLIENTS, FreshbooksProjectSource.CLIENT_ID, FreshbooksClientSource.CLIENT_ID),
                new ChildConnection(FeedType.FRESHBOOKS_TIME_ENTRIES, FeedType.FRESHBOOKS_PROJECTS, FreshbooksTimeEntrySource.PROJECT_ID, FreshbooksProjectSource.PROJECT_ID),
                new ChildConnection(FeedType.FRESHBOOKS_TIME_ENTRIES, FeedType.FRESHBOOKS_STAFF, FreshbooksTimeEntrySource.STAFF_ID, FreshbooksStaffSource.STAFF_ID),
                new ChildConnection(FeedType.FRESHBOOKS_TASKS, FeedType.FRESHBOOKS_TIME_ENTRIES, FreshbooksTaskSource.TASK_ID, FreshbooksTimeEntrySource.TASK_ID),
                new ChildConnection(FeedType.FRESHBOOKS_LINE_ITEMS, FeedType.FRESHBOOKS_INVOICE, FreshbooksInvoiceLineSource.INVOICE_ID, FreshbooksInvoiceSource.INVOICE_ID),
                new ChildConnection(FeedType.FRESHBOOKS_INVOICE, FeedType.FRESHBOOKS_STAFF, FreshbooksInvoiceSource.INVOICE_CREATED_BY, FreshbooksStaffSource.STAFF_ID));
    }

    private transient Map<String, String> timeEntryToInvoiceLineItem;

    protected List<IServerDataSourceDefinition> sortSources(List<IServerDataSourceDefinition> children) {
        Collections.sort(children, new Comparator<IServerDataSourceDefinition>() {

            public int compare(IServerDataSourceDefinition feedDefinition, IServerDataSourceDefinition feedDefinition1) {
                if (feedDefinition.getFeedType().getType() == FeedType.FRESHBOOKS_LINE_ITEMS.getType()) {
                    return -1;
                }
                if (feedDefinition1.getFeedType().getType() == FeedType.FRESHBOOKS_LINE_ITEMS.getType()) {
                    return 1;
                }
                return 0;
            }
        });
        return null;
    }

    public Map<String, String> timeEntryToInvoiceLines() {
        if (timeEntryToInvoiceLineItem == null) {
            timeEntryToInvoiceLineItem = new HashMap<String, String>();
        }
        return timeEntryToInvoiceLineItem;
    }

    @Override
    protected void refreshDone() {
        super.refreshDone();
        timeEntryToInvoiceLineItem = null;
    }

    @Override
    public FeedDefinition clone(Connection conn) throws CloneNotSupportedException, SQLException {
        FreshbooksCompositeSource dataSource = (FreshbooksCompositeSource) super.clone(conn);
        dataSource.setUrl("");
        dataSource.setTokenKey(null);
        dataSource.setTokenSecretKey(null);
        dataSource.setPin(null);
        return dataSource;
    }

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public List<DataSourceMigration> getMigrations() {
        return Arrays.asList((DataSourceMigration) new FreshbooksComposite1To2(this));
    }

    @Override
    public boolean checkDateTime(String name, Key key) {
        return false;
    }
}
