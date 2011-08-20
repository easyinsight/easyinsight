package com.easyinsight.datafeeds.migration;

import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.core.Key;
import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.database.migration.MigrateJoinOverrides;
import com.easyinsight.datafeeds.*;
import com.easyinsight.logging.LogClass;
import com.easyinsight.storage.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jamesboe
 * Date: Oct 30, 2009
 * Time: 9:46:21 PM
 */
public class MigrationManager {

    public static final String MIGRATION = "migration";

    private DataSourceTypeRegistry dataSourceTypeRegistry;

    private static class MigrationInfo {
        private int version;
        private long dataSourceID;

        private MigrationInfo(int version, long dataSourceID) {
            this.version = version;
            this.dataSourceID = dataSourceID;
        }
    }

    private void migrateJoins() {
        new MigrateJoinOverrides().migrate();
    }

    public void migrate() {
        if (obtainLock()) {
            try {
                migrateJoins();
            } catch (Exception e) {
                LogClass.error(e);
            }
            try {
                EIConnection conn = Database.instance().getConnection();
                try {
                    conn.setAutoCommit(false);
                    PreparedStatement queryStmt = conn.prepareStatement("SELECT VERSION, DATA_FEED_ID FROM DATA_FEED WHERE FEED_TYPE = ? AND VERSION < ?");
                    for (Map.Entry<FeedType, Class> entry : dataSourceTypeRegistry.getDataSourceMap().entrySet()) {
                        FeedDefinition feedDefinition = dataSourceTypeRegistry.createDataSource(entry.getKey());
                        List<MigrationInfo> infos = new ArrayList<MigrationInfo>();
                        queryStmt.setInt(1, entry.getKey().getType());
                        queryStmt.setInt(2, feedDefinition.getVersion());                        
                        ResultSet migrationTargetRS = queryStmt.executeQuery();
                        while (migrationTargetRS.next()) {

                            int currentVersion = migrationTargetRS.getInt(1);
                            long dataSourceID = migrationTargetRS.getLong(2);
                            infos.add(new MigrationInfo(currentVersion, dataSourceID));
                        }
                        for (MigrationInfo migrationInfo : infos) {
                            long dataSourceID = migrationInfo.dataSourceID;
                            int currentVersion = migrationInfo.version;
                            try {    
                                FeedDefinition migrateSource = new FeedStorage().getFeedDefinitionData(dataSourceID, conn);
                                Map<String, Key> keyMap = new HashMap<String, Key>();
                                for (AnalysisItem analysisItem : migrateSource.getFields()) {
                                    keyMap.put(analysisItem.getKey().toKeyString(), analysisItem.getKey());
                                }
                                List<DataSourceMigration> migrations = migrateSource.getMigrations();
                                for (DataSourceMigration migration : migrations) {
                                    if (migration.fromVersion() >= currentVersion && migration.toVersion() != currentVersion) {
                                        migration.migrate(keyMap, conn);
                                    }
                                }
                                new DataSourceInternalService().updateFeedDefinition(migrateSource, conn, true, true);
                                conn.commit();
                            } catch (Throwable e) {
                                LogClass.error(e);
                                LogClass.error("While saving data source " + dataSourceID);
                                conn.rollback();
                            }
                        }
                    }
                } catch (Throwable e) {
                    LogClass.error(e);
                } finally {
                    conn.setAutoCommit(true);
                    Database.closeConnection(conn);
                }
            } finally {
                releaseLock();
            }
        }
    }

    private void releaseLock() {
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement lockStmt = conn.prepareStatement("DELETE FROM DISTRIBUTED_LOCK WHERE LOCK_NAME = ?");
            lockStmt.setString(1, MIGRATION);
            lockStmt.executeUpdate();
            lockStmt.close();
        } catch (SQLException e) {
            LogClass.error(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    private boolean obtainLock() {
        boolean locked = false;
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement lockStmt = conn.prepareStatement("INSERT INTO DISTRIBUTED_LOCK (LOCK_NAME) VALUES (?)");
            lockStmt.setString(1, MIGRATION);
            lockStmt.execute();
            locked = true;
            lockStmt.close();
        } catch (SQLException e) {
            LogClass.debug("Failed to obtain distributed lock, assuming another app server has it.");
        } finally {
            Database.closeConnection(conn);
        }
        return locked;
    }

    public void setDataSourceTypeRegistry(DataSourceTypeRegistry dataSourceTypeRegistry) {
        this.dataSourceTypeRegistry = dataSourceTypeRegistry;
    }

    private void migrateStorage() throws Exception {
        migrateDatabase(Database.instance());
    }

    private void migrateDatabase(Database database) throws SQLException {
        EIConnection conn = database.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SHOW TABLE STATUS");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String tableName = rs.getString("Name");
                String collation = rs.getString("Collation");
                if (!collation.startsWith("utf8")) {
                    System.out.println("Migrating " + tableName);
                    PreparedStatement alterStmt = conn.prepareStatement("ALTER TABLE " + tableName + " CONVERT TO CHARACTER SET UTF8");
                    alterStmt.execute();
                    alterStmt.close();
                }
            }
        } finally {
            Database.closeConnection(conn);
        }
    }
}
