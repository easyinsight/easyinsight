package com.easyinsight.datafeeds.infusionsoft;

import com.easyinsight.analysis.*;
import com.easyinsight.core.DateValue;
import com.easyinsight.core.Key;
import com.easyinsight.core.NamedKey;
import com.easyinsight.core.StringValue;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.datafeeds.FeedStorage;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.datafeeds.ServerDataSourceDefinition;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.logging.LogClass;
import com.easyinsight.storage.IDataStorage;
import com.easyinsight.userupload.DataTypeGuesser;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: jamesboe
 * Date: 9/12/14
 * Time: 1:32 PM
 */
public class InfusionsoftReportSource extends ServerDataSourceDefinition {
    private String reportID;
    private String userID;

    public InfusionsoftReportSource() {

    }

    public String getReportID() {
        return reportID;
    }

    public void setReportID(String reportID) {
        this.reportID = reportID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    protected void createFields(FieldBuilder fieldBuilder, Connection conn, FeedDefinition parentDefinition) {
        try {
            InfusionsoftCompositeSource infusionsoftCompositeSource = (InfusionsoftCompositeSource) new FeedStorage().getFeedDefinitionData(getParentSourceID(), conn);
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            String url = infusionsoftCompositeSource.getUrl() + ":443/api/xmlrpc";
            config.setServerURL(new URL(url));
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);
            List parameters = new ArrayList();
            parameters.add(infusionsoftCompositeSource.getInfusionApiKey());
            parameters.add(reportID);
            parameters.add(1);
            Map<String, String> result = (Map<String, String>) client.execute("SearchService.getAllReportColumns", parameters);
            if (getFields().size() == 0) {
                Set<String> keys = new HashSet<>();
                for (Map.Entry<String, String> entry : result.entrySet()) {
                    keys.add(entry.getKey());
                }
                List params = new ArrayList();
                params.add(infusionsoftCompositeSource.getInfusionApiKey());
                params.add(reportID);
                params.add(1);
                params.add(0);
                params.add(new ArrayList<>(keys));
                DataTypeGuesser guesser = new DataTypeGuesser(new String[] { "MM-dd-yy" }, new String[] { "EEE MMM dd HH:mm:ss zzz yyyy" });
                Object[] results = (Object[]) client.execute("SearchService.getSavedSearchResults", params);
                for (Object o : results) {
                    Map resultMap = (Map) o;
                    for (String key : keys) {
                        StringValue stringValue = new StringValue(resultMap.get(key).toString());
                        guesser.addValue(new NamedKey(key), stringValue);
                    }
                }
                List<AnalysisItem> items = guesser.createFeedItems();
                for (AnalysisItem item : items) {
                    item.setDisplayName(getFeedName() + " - " + item.getKey().toKeyString());
                    fieldBuilder.addField(item.getKey().toKeyString(), item);
                }
            } else {
                for (Map.Entry<String, String> entry : result.entrySet()) {
                    fieldBuilder.addField(entry.getKey(), new AnalysisDimension(getFeedName() + " - " + entry.getKey()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.INFUSIONSOFT_REPORT;
    }

    @Override
    public int getDataSourceType() {
        return DataSourceInfo.STORED_PULL;
    }

    @Override
    public DataSet getDataSet(Map<String, Key> keys, Date now, FeedDefinition parentDefinition, IDataStorage IDataStorage, EIConnection conn, String callDataID, Date lastRefreshDate) throws ReportException {
        try {
            InfusionsoftCompositeSource infusionsoftCompositeSource = (InfusionsoftCompositeSource) new FeedStorage().getFeedDefinitionData(getParentSourceID(), conn);
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            String url = infusionsoftCompositeSource.getUrl() + ":443/api/xmlrpc";
            config.setServerURL(new URL(url));
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);
            DataSet dataSet = new DataSet();

            //The secure encryption key
            boolean hasMoreResults;
            int page = 0;
            do {
                int count = 0;
                List parameters = new ArrayList();
                parameters.add(infusionsoftCompositeSource.getInfusionApiKey());
                parameters.add(reportID);
                parameters.add(1);
                parameters.add(page);
                parameters.add(new ArrayList<>(keys.keySet()));
                Object[] results = (Object[]) client.execute("SearchService.getSavedSearchResults", parameters);
                for (Object result : results) {
                    IRow row = dataSet.createRow();
                    Map resultMap = (Map) result;
                    for (Map.Entry<String, Key> entry : keys.entrySet()) {
                        Object value = resultMap.get(entry.getKey());
                        if (value != null) {
                            String string = value.toString();
                            row.addValue(entry.getValue(), string);
                        }
                    }
                    count++;
                }

                hasMoreResults = count == 1000;
                page++;
            } while (hasMoreResults);
            return dataSet;
        } catch (Exception e) {
            LogClass.error(e);
            return new DataSet();
        }
    }

    @Override
    public void customStorage(Connection conn) throws SQLException {
        super.customStorage(conn);
        PreparedStatement clearStmt = conn.prepareStatement("DELETE FROM INFUSIONSOFT_REPORT_SOURCE WHERE DATA_SOURCE_ID = ?");
        clearStmt.setLong(1, getDataFeedID());
        clearStmt.executeUpdate();
        clearStmt.close();
        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO INFUSIONSOFT_REPORT_SOURCE (DATA_SOURCE_ID, REPORT_ID, USER_ID) VALUES (?, ?, ?)");
        insertStmt.setLong(1, getDataFeedID());
        insertStmt.setString(2, reportID);
        insertStmt.setString(3, userID);
        insertStmt.execute();
        insertStmt.close();
    }

    @Override
    public void customLoad(Connection conn) throws SQLException {
        super.customLoad(conn);
        PreparedStatement queryStmt = conn.prepareStatement("SELECT REPORT_ID, USER_ID FROM INFUSIONSOFT_REPORT_SOURCE WHERE DATA_SOURCE_ID = ?");
        queryStmt.setLong(1, getDataFeedID());
        ResultSet rs = queryStmt.executeQuery();
        if (rs.next()) {
            setReportID(rs.getString(1));
            setUserID(rs.getString(2));
        }
        queryStmt.close();
    }
}
