package com.easyinsight.datafeeds.infusionsoft;

import com.easyinsight.analysis.DataSourceInfo;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.datafeeds.composite.ChildConnection;
import com.easyinsight.datafeeds.composite.CompositeServerDataSource;
import com.easyinsight.users.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User: jamesboe
 * Date: 4/24/13
 * Time: 11:18 AM
 */
public class InfusionsoftCompositeSource extends CompositeServerDataSource {

    private String infusionApiKey;
    private String url;

    public InfusionsoftCompositeSource() {
        setFeedName("Infusionsoft");
    }

    public String getInfusionApiKey() {
        return infusionApiKey;
    }

    public void setInfusionApiKey(String infusionApiKey) {
        this.infusionApiKey = infusionApiKey;
    }

    @Override
    public int getDataSourceType() {
        return DataSourceInfo.COMPOSITE_PULL;
    }

    @Override
    public int getRequiredAccountTier() {
        return Account.BASIC;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.INFUSIONSOFT_COMPOSITE;
    }

    @Override
    protected Set<FeedType> getFeedTypes() {
        Set<FeedType> types = new HashSet<FeedType>();
        types.add(FeedType.INFUSIONSOFT_LEAD);
        types.add(FeedType.INFUSIONSOFT_STAGE);
        types.add(FeedType.INFUSIONSOFT_STAGE_HISTORY);
        types.add(FeedType.INFUSIONSOFT_AFFILIATES);
        types.add(FeedType.INFUSIONSOFT_COMPANIES);
        types.add(FeedType.INFUSIONSOFT_CONTACTS);
        types.add(FeedType.INFUSIONSOFT_JOBS);
        return types;
    }

    @Override
    public String validateCredentials() {
        return null;
    }

    @Override
    public void customStorage(Connection conn) throws SQLException {
        super.customStorage(conn);
        PreparedStatement clearStmt = conn.prepareStatement("DELETE FROM INFUSIONSOFT WHERE DATA_SOURCE_ID = ?");
        clearStmt.setLong(1, getDataFeedID());
        clearStmt.executeUpdate();
        clearStmt.close();
        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO INFUSIONSOFT (DATA_SOURCE_ID, URL, API_KEY) VALUES (?, ?, ?)");
        insertStmt.setLong(1, getDataFeedID());
        insertStmt.setString(2, url);
        insertStmt.setString(3, infusionApiKey);
        insertStmt.execute();
        insertStmt.close();
    }

    @Override
    public void customLoad(Connection conn) throws SQLException {
        super.customLoad(conn);
        PreparedStatement queryStmt = conn.prepareStatement("SELECT URL, API_KEY FROM INFUSIONSOFT WHERE DATA_SOURCE_ID = ?");
        queryStmt.setLong(1, getDataFeedID());
        ResultSet rs = queryStmt.executeQuery();
        if (rs.next()) {
            setUrl(rs.getString(1));
            setInfusionApiKey(rs.getString(2));
        }
        queryStmt.close();
    }

    @Override
    protected Collection<ChildConnection> getChildConnections() {
        return new ArrayList<ChildConnection>();
    }
}
