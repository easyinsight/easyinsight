package com.easyinsight.datafeeds.database;

import com.easyinsight.PasswordStorage;
import com.easyinsight.analysis.DataSourceInfo;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.FeedStorage;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.datafeeds.HTMLConnectionFactory;
import com.easyinsight.datafeeds.HTMLConnectionProperty;
import com.easyinsight.logging.LogClass;

import java.sql.*;
import java.text.MessageFormat;

/**
 * User: jamesboe
 * Date: 11/27/12
 * Time: 10:24 PM
 */
public class MySQLDatabaseConnection extends ServerDatabaseConnection {

    private String host;
    private boolean useSSL = true;
    private int port = 3306;
    private String databaseName;
    private String dbUserName;
    private String dbPassword;

    public void configureFactory(HTMLConnectionFactory factory) {
        factory.addField("Host", "host");
        factory.addFieldWithDefault("Port", "port", HTMLConnectionProperty.INTEGER, "3306");
        factory.addField("Database Name", "databaseName");
        factory.addField("Database User Name", "dbUserName");
        factory.addPassword("Database Password", "dbPassword", true);
        factory.addField("Query", "query", HTMLConnectionProperty.TEXT);
        factory.addField("Use SSL", "useSSL", HTMLConnectionProperty.CHECKBOX);
        factory.addFieldWithDefault("Connection Timeout (minutes)", "timeout", HTMLConnectionProperty.INTEGER, "20");
        factory.type(HTMLConnectionFactory.TYPE_BASIC_AUTH);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDbUserName() {
        return dbUserName;
    }

    public void setDbUserName(String dbUserName) {
        this.dbUserName = dbUserName;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    @Override
    protected Connection createConnection() throws SQLException {
        String url;
        if (useSSL) {
            url = MessageFormat.format("jdbc:mysql://{0}:{1}/{2}?useSSL=true&requireSSL=true&reconnect=true", host, String.valueOf(port), databaseName);
        } else {
            url = MessageFormat.format("jdbc:mysql://{0}:{1}/{2}", host, String.valueOf(port), databaseName);
        }
        return DriverManager.getConnection(url, dbUserName, dbPassword);
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.SERVER_MYSQL;
    }

    public boolean isUseSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    @Override
    public void beforeSave(EIConnection conn) throws Exception {
        super.beforeSave(conn);
        PreparedStatement queryStmt = conn.prepareStatement("SELECT mysql_database_connection.query_string FROM mysql_database_connection WHERE DATA_SOURCE_ID = ?");
        queryStmt.setLong(1, getDataFeedID());
        ResultSet existing = queryStmt.executeQuery();
        if (existing.next()) {
            String existingQueryString = existing.getString(1);
            if (existingQueryString == null || !existingQueryString.equals(getQuery())) {
                setRebuildFields(true);
            }
        } else {
            setRebuildFields(true);
        }
        queryStmt.close();
    }

    @Override
    public void customStorage(Connection conn) throws SQLException {
        super.customStorage(conn);
        if (getCopyingFromSource() > 0) {
            MySQLDatabaseConnection dataSource = (MySQLDatabaseConnection) new FeedStorage().getFeedDefinitionData(getCopyingFromSource(), conn);
            setDbPassword(dataSource.getDbPassword());
        }
        PreparedStatement findPasswordStmt = conn.prepareStatement("SELECT DATABASE_PASSWORD FROM MYSQL_DATABASE_CONNECTION WHERE DATA_SOURCE_ID = ?");
        findPasswordStmt.setLong(1, getDataFeedID());
        ResultSet passwordRS = findPasswordStmt.executeQuery();
        String existingPassword = null;
        if (passwordRS.next()) {
            existingPassword = passwordRS.getString(1);
        }
        findPasswordStmt.close();
        PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM mysql_database_connection WHERE DATA_SOURCE_ID = ?");
        deleteStmt.setLong(1, getDataFeedID());
        deleteStmt.executeUpdate();
        deleteStmt.close();
        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO mysql_database_connection (data_source_id, host_name, server_port, database_name," +
                "database_username, database_password, query_string, rebuild_fields, timeout, use_ssl) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        insertStmt.setLong(1, getDataFeedID());
        insertStmt.setString(2, host);
        insertStmt.setInt(3, port);
        insertStmt.setString(4, databaseName);
        insertStmt.setString(5, dbUserName);
        if (dbPassword == null || "".equals(dbPassword)) {
            if (existingPassword == null) {
                insertStmt.setNull(6, Types.VARCHAR);
            } else {
                insertStmt.setString(6, existingPassword);
            }
        } else {
            insertStmt.setString(6, PasswordStorage.encryptString(dbPassword));
        }
        insertStmt.setString(7, getQuery());
        insertStmt.setBoolean(8, isRebuildFields());
        insertStmt.setInt(9, getTimeout());
        insertStmt.setBoolean(10, isUseSSL());
        insertStmt.execute();
        insertStmt.close();
    }

    @Override
    public void customLoad(Connection conn) throws SQLException {
        super.customLoad(conn);
        PreparedStatement loadStmt = conn.prepareStatement("SELECT host_name, server_port, database_name, database_username," +
                "database_password, query_string, rebuild_fields, timeout, use_ssl FROM mysql_database_connection WHERE data_source_id = ?");
        loadStmt.setLong(1, getDataFeedID());
        ResultSet rs = loadStmt.executeQuery();
        if (rs.next()) {
            host = rs.getString(1);
            port = rs.getInt(2);
            databaseName = rs.getString(3);
            dbUserName = rs.getString(4);
            dbPassword = rs.getString(5);
            if (dbPassword != null && !"".equals(dbPassword)) {
                try {
                    dbPassword = PasswordStorage.decryptString(dbPassword);
                } catch (Exception e) {
                    LogClass.error(e);
                }
            }
            setQuery(rs.getString(6));
            setRebuildFields(rs.getBoolean(7));
            setTimeout(rs.getInt(8));
            setUseSSL(rs.getBoolean(9));
        }
        loadStmt.close();
    }

    @Override
    public int getDataSourceType() {
        return DataSourceInfo.STORED_PULL;
    }
}
