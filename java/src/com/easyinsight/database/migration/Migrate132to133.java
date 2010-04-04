package com.easyinsight.database.migration;

import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.logging.LogClass;
import com.easyinsight.util.RandomTextGenerator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User: jamesboe
 * Date: Apr 2, 2010
 * Time: 11:17:34 AM
 */
public class Migrate132to133 implements Migration {

    public boolean needToRun() {
        return true;
    }

    public void migrate() {
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            PreparedStatement reportKeyStmt = conn.prepareStatement("UPDATE ANALYSIS SET URL_KEY = ? WHERE ANALYSIS_ID = ?");
            PreparedStatement findReportsStmt = conn.prepareStatement("SELECT ANALYSIS_ID FROM ANALYSIS WHERE " +
                    "URL_KEY IS NULL");
            ResultSet rs = findReportsStmt.executeQuery();
            while (rs.next()) {
                long reportID = rs.getLong(1);
                reportKeyStmt.setString(1, RandomTextGenerator.generateText(20));
                reportKeyStmt.setLong(2, reportID);
                reportKeyStmt.executeUpdate();
            }

            PreparedStatement packageKeyStmt = conn.prepareStatement("UPDATE REPORT_PACKAGE SET URL_KEY = ? WHERE " +
                    "REPORT_PACKAGE_ID = ?");
            PreparedStatement findPackageStmt = conn.prepareStatement("SELECT REPORT_PACKAGE_ID FROM REPORT_PACKAGE WHERE " +
                    "URL_KEY IS NULL");
            ResultSet packageRS = findPackageStmt.executeQuery();
            while (packageRS.next()) {
                long packageID = packageRS.getLong(1);
                packageKeyStmt.setString(1, RandomTextGenerator.generateText(20));
                packageKeyStmt.setLong(2, packageID);
                packageKeyStmt.executeUpdate();
            }

            PreparedStatement kpiTreeKeyStmt = conn.prepareStatement("UPDATE GOAL_TREE SET URL_KEY = ? WHERE GOAL_TREE_ID = ?");
            PreparedStatement findKPITReeStmt = conn.prepareStatement("SELECT GOAL_TREE_ID FROM GOAL_TREE WHERE " +
                    "URL_KEY IS NULL");
            ResultSet kpiTreeRS = findKPITReeStmt.executeQuery();
            while (kpiTreeRS.next()) {
                long kpiTreeID = kpiTreeRS.getLong(1);
                kpiTreeKeyStmt.setString(1, RandomTextGenerator.generateText(20));
                kpiTreeKeyStmt.setLong(2, kpiTreeID);
                kpiTreeKeyStmt.executeUpdate();
            }

            PreparedStatement dataSourceKeyStmt = conn.prepareStatement("UPDATE DATA_FEED SET API_KEY = ? WHERE DATA_FEED_ID = ?");
            PreparedStatement findDSStmt = conn.prepareStatement("SELECT DATA_FEED_ID FROM DATA_FEED WHERE " +
                    "API_KEY IS NULL");
            ResultSet dsRS = findDSStmt.executeQuery();
            while (dsRS.next()) {
                long reportID = dsRS.getLong(1);
                dataSourceKeyStmt.setString(1, RandomTextGenerator.generateText(20));
                dataSourceKeyStmt.setLong(2, reportID);
                dataSourceKeyStmt.executeUpdate();
            }

            PreparedStatement groupStmt = conn.prepareStatement("UPDATE COMMUNITY_GROUP SET URL_KEY = ? WHERE COMMUNITY_GROUP_ID = ?");
            PreparedStatement findGroupStmt = conn.prepareStatement("SELECT COMMUNITY_GROUP_ID FROM COMMUNITY_GROUP WHERE " +
                    "URL_KEY IS NULL");
            ResultSet groupRS = findGroupStmt.executeQuery();
            while (groupRS.next()) {
                long groupID = groupRS.getLong(1);
                groupStmt.setString(1, RandomTextGenerator.generateText(20));
                groupStmt.setLong(2, groupID);
                groupStmt.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            LogClass.error(e);
            conn.rollback();
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
    }
}
