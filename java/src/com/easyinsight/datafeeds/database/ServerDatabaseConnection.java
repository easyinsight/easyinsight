package com.easyinsight.datafeeds.database;

import com.easyinsight.analysis.*;
import com.easyinsight.core.EmptyValue;
import com.easyinsight.core.Key;
import com.easyinsight.core.NamedKey;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.datafeeds.ServerDataSourceDefinition;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.logging.LogClass;
import com.easyinsight.storage.IDataStorage;
import com.easyinsight.users.Account;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * User: jamesboe
 * Date: 11/27/12
 * Time: 3:01 PM
 */
public abstract class ServerDatabaseConnection extends ServerDataSourceDefinition {

    private String query;
    private int timeout = 5;

    private boolean rebuildFields = true;
    private long copyingFromSource;

    public long getCopyingFromSource() {
        return copyingFromSource;
    }

    public void setCopyingFromSource(long copyingFromSource) {
        this.copyingFromSource = copyingFromSource;
    }

    public String validateCredentials() {
        return null;
    }

    @Override
    public int getRequiredAccountTier() {
        return Account.BASIC;
    }

    @Override
    public boolean isMigrateRequired() {
        return false;
    }

    @NotNull
    @Override
    protected List<String> getKeys(FeedDefinition parentDefinition) {
        return new ArrayList<String>();
    }

    private List<AnalysisItem> cacheFields = new ArrayList<AnalysisItem>();

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    protected void beforeRefresh(EIConnection conn) {
        if (rebuildFields) {
            if (getDataFeedID() != 0) {
                List<AnalysisItem> fields = new ArrayList<AnalysisItem>();
                conn.setAutoCommit(true);
                Map<String, Key> keys = newDataSourceFields(null);
                Map<String, AnalysisItem> map = new HashMap<String, AnalysisItem>();
                for (AnalysisItem analysisItem : getFields()) {
                    fields.add(analysisItem);
                    map.put(analysisItem.getKey().toKeyString(), analysisItem);
                }
                boolean newField = false;
                if (getQuery() != null) {
                    try {
                        //Class.forName(serverDatabaseConnection.getDriver());
                        Connection connection = createConnection();
                        Statement statement = connection.createStatement();
                        ResultSet rs = statement.executeQuery(query);
                        rs.next();
                        int columnCount = rs.getMetaData().getColumnCount();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = rs.getMetaData().getColumnName(i);

                            AnalysisItem analysisItem = map.get(columnName);
                            if (analysisItem == null) {
                                newField = true;
                                if (columnName.endsWith("id") || columnName.endsWith("fk")) {
                                    analysisItem = new AnalysisDimension(new NamedKey(columnName), true);
                                } else {
                                    switch (rs.getMetaData().getColumnType(i)) {
                                        case Types.BIGINT:
                                        case Types.TINYINT:
                                        case Types.SMALLINT:
                                        case Types.INTEGER:
                                        case Types.NUMERIC:
                                        case Types.FLOAT:
                                        case Types.DOUBLE:
                                        case Types.DECIMAL:
                                        case Types.REAL:
                                            analysisItem = new AnalysisMeasure(new NamedKey(columnName), AggregationTypes.SUM);
                                            break;
                                        case Types.BOOLEAN:
                                        case Types.BIT:
                                        case Types.CHAR:
                                        case Types.NCHAR:
                                        case Types.NVARCHAR:
                                        case Types.VARCHAR:
                                        case Types.LONGVARCHAR:
                                            analysisItem = new AnalysisDimension(new NamedKey(columnName), true);
                                            break;
                                        case Types.DATE:
                                            analysisItem = new AnalysisDateDimension(new NamedKey(columnName), true, AnalysisDateDimension.DAY_LEVEL);
                                            ((AnalysisDateDimension) analysisItem).setDateOnlyField(true);
                                            break;
                                        case Types.TIME:
                                        case Types.TIMESTAMP:
                                            analysisItem = new AnalysisDateDimension(new NamedKey(columnName), true, AnalysisDateDimension.DAY_LEVEL);
                                            break;
                                        default:
                                            throw new RuntimeException("This data type (" + rs.getMetaData().getColumnTypeName(i) + ") is not supported in Easy Insight. Type value: " + rs.getMetaData().getColumnType(i));
                                    }
                                }
                            }
                            fields.add(analysisItem);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                rebuildFields = newField;
                cacheFields = fields;
                conn.setAutoCommit(false);
            }
        }
    }

    public boolean isRebuildFields() {
        return rebuildFields;
    }

    public void setRebuildFields(boolean rebuildFields) {
        this.rebuildFields = rebuildFields;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<AnalysisItem> createAnalysisItems(Map<String, Key> keys, Connection conn, FeedDefinition parentDefinition) {
        List<AnalysisItem> fields = new ArrayList<AnalysisItem>();

        if (getDataFeedID() == 0) {
            return fields;
        }

        return cacheFields;
    }

    protected boolean otherwiseChanged() {
        boolean blah = rebuildFields;
        rebuildFields = false;
        return blah;
    }



    protected abstract Connection createConnection() throws SQLException;

    @Override
    public DataSet getDataSet(Map<String, Key> keys, Date now, FeedDefinition parentDefinition, IDataStorage IDataStorage, EIConnection conn, String callDataID, Date lastRefreshDate) throws ReportException {
        Map<String, AnalysisItem> map = new HashMap<String, AnalysisItem>();
        for (AnalysisItem field : getFields()) {
            map.put(field.getKey().toKeyString(), field);
        }
        try {
            DataSet dataSet = new DataSet();
            Connection connection = createConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            int ct = 0;
            while (rs.next()) {
                IRow row = dataSet.createRow();
                int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    AnalysisItem analysisItem = map.get(columnName);
                    if (analysisItem == null) {
                        continue;
                    }
                    switch (rs.getMetaData().getColumnType(i)) {
                        case Types.BIGINT:
                        case Types.TINYINT:
                        case Types.SMALLINT:
                        case Types.INTEGER:
                        case Types.NUMERIC:
                        case Types.FLOAT:
                        case Types.DOUBLE:
                        case Types.DECIMAL:
                        case Types.REAL:
                            double number = rs.getDouble(i);
                            if (analysisItem.hasType(AnalysisItemTypes.DIMENSION)) {
                                row.addValue(analysisItem.getKey(), String.valueOf((int) number));
                            } else {
                                row.addValue(analysisItem.getKey(), number);
                            }
                            break;

                        case Types.BOOLEAN:
                        case Types.BIT:
                        case Types.CHAR:
                        case Types.NCHAR:
                        case Types.NVARCHAR:
                        case Types.VARCHAR:
                        case Types.LONGVARCHAR:
                            String string = rs.getString(i);
                            row.addValue(analysisItem.getKey(), string);
                            break;

                        case Types.DATE:
                            try {
                                Date d = rs.getDate(i);
                                if(!rs.wasNull()) {
                                    row.addValue(analysisItem.getKey(), d);
                                }
                            } catch (SQLException e) {
                                if (e.getMessage() != null && e.getMessage().contains("can not be represented as java.sql.Date")) {
                                    row.addValue(analysisItem.getKey(), new EmptyValue());
                                } else {
                                    LogClass.debug(e.getMessage());
                                }
                            }
                            break;
                        case Types.TIME:
                            Time t = rs.getTime(i);
                            if(!rs.wasNull()) {
                                row.addValue(analysisItem.getKey(), new Date(t.getTime()));
                            }
                            break;
                        case Types.TIMESTAMP:
                            try {
                                Timestamp timestamp = rs.getTimestamp(i);
                                if (!rs.wasNull()) {
                                    java.sql.Date date = new java.sql.Date(timestamp.getTime());
                                    row.addValue(analysisItem.getKey(), date);
                                }
                            } catch (SQLException e) {
                                // ignore
                            }
                            break;
                        default:
                            throw new RuntimeException("This data type (" + rs.getMetaData().getColumnTypeName(i) + ") is not supported in Easy Insight. Type value: " + rs.getMetaData().getColumnType(i));
                    }
                }
                ct++;
                if (ct == 1000) {
                    IDataStorage.insertData(dataSet);
                    dataSet = new DataSet();
                    ct = 0;
                }
            }
            IDataStorage.insertData(dataSet);
        } catch (Exception e) {
            LogClass.error(e);
        }
        return null;
    }

    /*protected boolean noDataProcessing() {
        return true;
    }

    @Override
    protected boolean clearsData(FeedDefinition parentSource) {
        return false;
    }*/

    public boolean rebuildFieldWindow() {
        return true;
    }

    public List<SchemaTable> exploreSchema() throws SQLException {
        Connection conn = createConnection();
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet tableRS = databaseMetaData.getTables(null, null, null, null);
            List<SchemaTable> schemaTables = new ArrayList<SchemaTable>();
            while (tableRS.next()) {
                String tableName = tableRS.getString("TABLE_NAME");
                SchemaTable schemaTable = new SchemaTable();
                schemaTable.setName(tableName);
                schemaTables.add(schemaTable);
                List<SchemaColumn> schemaColumns = new ArrayList<SchemaColumn>();
                ResultSet columnRS = databaseMetaData.getColumns(null, null, tableName, null);
                while (columnRS.next()) {
                    String columnName = columnRS.getString("COLUMN_NAME");
                    SchemaColumn schemaColumn = new SchemaColumn();
                    schemaColumn.setName(columnName);
                    schemaColumns.add(schemaColumn);
                }
                schemaTable.setSchemaColumns(schemaColumns);
            }
            return schemaTables;
        } finally {
            conn.close();
        }
    }
}
