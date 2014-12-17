package com.easyinsight.admin;

import com.easyinsight.cache.MemCachedManager;
import com.easyinsight.config.ConfigLoader;
import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.logging.LogClass;
import com.easyinsight.storage.DatabaseManager;

import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;

/**
 * User: jamesboe
 * Date: 1/26/11
 * Time: 10:58 AM
 */
public class HealthListener implements Runnable {

    public static final String SUCCESS = "Success";
    public static final String FAILURE = "Failure";

    private boolean running = true;

    public void run() {
        initialSetup();

        while (running) {
            try {

                String response = "All Good";
                String code = SUCCESS;
                {
                    EIConnection conn = Database.instance().getConnection();
                    try {
                        Statement stmt = conn.createStatement();
                        stmt.executeQuery("SELECT 1");
                    } catch (Exception e) {
                        response = e.getMessage() + " on query of core database pool";
                        code = FAILURE;
                    } finally {
                        Database.closeConnection(conn);
                    }

                }
                for (Map.Entry<String, Database> storageEntry : DatabaseManager.instance().getDbMap().entrySet()) {
                    Database database = storageEntry.getValue();

                    EIConnection conn = database.getConnection();
                    try {
                        Statement stmt = conn.createStatement();
                        stmt.executeQuery("SELECT 1");
                    } catch (Exception e) {
                        response = e.getMessage() + " on query of " + storageEntry.getKey() + " database pool";
                        code = FAILURE;
                    } finally {
                        Database.closeConnection(conn);
                    }

                }
                for (Map.Entry<String, Database> storageEntry : DatabaseManager.instance().getAdditionalDatabases().entrySet()) {
                    Database database = storageEntry.getValue();

                    EIConnection conn = database.getConnection();
                    try {
                        Statement stmt = conn.createStatement();
                        stmt.executeQuery("SELECT 1");
                    } catch (Exception e) {
                        response = e.getMessage() + " on query of " + storageEntry.getKey() + " database pool";
                        code = FAILURE;
                    } finally {
                        Database.closeConnection(conn);
                    }

                }
                Status status = new Status();
                status.setTime(System.currentTimeMillis());
                status.setCode(code);
                status.setMessage(response);
                status.setHealthInfo(new AdminService().getHealthInfo());
                MemCachedManager.add("servers" + InetAddress.getLocalHost().getHostName(), 120, status);
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    // ignore
                }
            } catch (Exception e) {
                LogClass.error(e);
            }
        }
    }

    private void initialSetup() {
        EIConnection conn = Database.instance().getConnection();
        try {
            String host = InetAddress.getLocalHost().getHostName();
            PreparedStatement stmt = conn.prepareStatement("SELECT SERVER_ID FROM SERVER WHERE SERVER_HOST = ?");
            stmt.setString(1, host);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO SERVER (server_host, enabled, database_listener, report_listener," +
                        "data_source_listener, healthy, last_healthy_time) VALUES (?, ?, ?, ?, ?, ?, ?)");
                insertStmt.setString(1, host);
                insertStmt.setBoolean(2, true);
                insertStmt.setBoolean(3, ConfigLoader.instance().isDatabaseListener());
                insertStmt.setBoolean(4, ConfigLoader.instance().isTaskRunner());
                insertStmt.setBoolean(5, ConfigLoader.instance().isTaskRunner());
                insertStmt.setBoolean(6, true);
                insertStmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
                insertStmt.execute();
                insertStmt.close();
            } else {
                PreparedStatement updateStmt = conn.prepareStatement("UPDATE SERVER SET database_listener = ?, report_listener = ?," +
                        "data_source_listener = ?, healthy = ?, last_healthy_time = ? WHERE server_host = ?");
                updateStmt.setBoolean(1, ConfigLoader.instance().isDatabaseListener());
                updateStmt.setBoolean(2, ConfigLoader.instance().isTaskRunner());
                updateStmt.setBoolean(3, ConfigLoader.instance().isTaskRunner());
                updateStmt.setBoolean(4, true);
                updateStmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                updateStmt.setString(6, host);
                updateStmt.execute();
                updateStmt.close();
            }
            stmt.close();
        } catch (Exception e) {
            LogClass.error(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public void stop() {
        running = false;
    }
}
