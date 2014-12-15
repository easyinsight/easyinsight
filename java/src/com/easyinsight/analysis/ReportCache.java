package com.easyinsight.analysis;

import com.easyinsight.cache.MemCachedManager;
import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.logging.LogClass;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * User: jamesboe
 * Date: Jun 21, 2010
 * Time: 4:10:36 PM
 */
public class ReportCache {
    private static ReportCache instance;

    public static void initialize() {
        instance = new ReportCache();
    }

    public static ReportCache instance() {
        return instance;
    }

    @Nullable
    public EmbeddedResults getResults(long dataSourceID, CacheKey cacheKey, int cacheTime) {
        String cacheKeyString = "reportResults" + cacheKey.toString();
        EmbeddedResults results = null;
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT REPORT_CACHE_ID FROM REPORT_CACHE WHERE report_id = ? AND cache_key = ?");
            ps.setLong(1, cacheKey.getReportID());
            ps.setString(2, cacheKeyString);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                long reportCacheID = rs.getLong(1);
                results = (EmbeddedResults) MemCachedManager.get("reportResults" + reportCacheID);
            }
            ps.close();
        } catch (Exception e) {
            LogClass.error(e);
        } finally {
            Database.closeConnection(conn);
        }
        return results;
    }

    public void storeReport(long dataSourceID, CacheKey cacheKey, EmbeddedResults results, int cacheTime) {
        String cacheKeyString = "reportResults" + cacheKey.toString();
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO REPORT_CACHE (cache_key, data_source_id, report_id) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, cacheKeyString);
            ps.setLong(2, dataSourceID);
            ps.setLong(3, cacheKey.getReportID());
            ps.execute();
            long id = Database.instance().getAutoGenKey(ps);
            ps.close();
            MemCachedManager.add("reportResults" + id, cacheTime > 0 ? cacheTime * 60 : 10000, results);
        } catch (Exception e) {
            LogClass.error(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    @Nullable
    public DataSet getAddonResults(long dataSourceID, CacheKey cacheKey, int cacheTime) {
        String cacheKeyString = "addonResults" + cacheKey.toString();
        DataSet results = null;
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT REPORT_CACHE_ID FROM REPORT_CACHE WHERE report_id = ? AND cache_key = ?");
            ps.setLong(1, cacheKey.getReportID());
            ps.setString(2, cacheKeyString);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                long reportCacheID = rs.getLong(1);
                results = (DataSet) MemCachedManager.get("addonResults" + reportCacheID);
            }
            ps.close();
        } catch (Exception e) {
            LogClass.error(e);
        } finally {
            Database.closeConnection(conn);
        }
        return results;
    }

    public void storeAddonReport(long dataSourceID, CacheKey cacheKey, DataSet results, int cacheTime) {

        String cacheKeyString = "addonResults" + cacheKey.toString();
        if (cacheKeyString.length() > 10000) {
            System.out.println("Cache key on " + cacheKey.getReportID() + " too long");
            return;
        }
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO REPORT_CACHE (cache_key, data_source_id, report_id) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, cacheKeyString);
            ps.setLong(2, dataSourceID);
            ps.setLong(3, cacheKey.getReportID());
            ps.execute();
            long id = Database.instance().getAutoGenKey(ps);
            ps.close();
            MemCachedManager.add("addonResults" + id, cacheTime > 0 ? cacheTime * 60 : 10000, results);
        } catch (Exception e) {
            LogClass.error(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public void flushResults(long dataSourceID, EIConnection conn) {

        try {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT CACHE_KEY FROM REPORT_CACHE WHERE DATA_SOURCE_ID = ?");
            queryStmt.setLong(1, dataSourceID);
            ResultSet rs = queryStmt.executeQuery();
            while (rs.next()) {
                String cacheID = rs.getString(1);
                MemCachedManager.delete(cacheID);
            }
            queryStmt.close();
            PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM REPORT_CACHE WHERE DATA_SOURCE_ID = ?");
            deleteStmt.setLong(1, dataSourceID);
            deleteStmt.executeUpdate();
            deleteStmt.close();
        } catch (Exception e) {
            LogClass.error(e);
        }
    }

    public void flushResults(long dataSourceID) {
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT CACHE_KEY FROM REPORT_CACHE WHERE DATA_SOURCE_ID = ?");
            queryStmt.setLong(1, dataSourceID);
            ResultSet rs = queryStmt.executeQuery();
            while (rs.next()) {
                String cacheID = rs.getString(1);
                MemCachedManager.delete(cacheID);
            }
            queryStmt.close();
            PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM REPORT_CACHE WHERE DATA_SOURCE_ID = ?");
            deleteStmt.setLong(1, dataSourceID);
            deleteStmt.executeUpdate();
            deleteStmt.close();
        } catch (Exception e) {
            LogClass.error(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public void flushResultsForReport(long reportID) {
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT CACHE_KEY FROM REPORT_CACHE WHERE REPORT_ID = ?");
            queryStmt.setLong(1, reportID);
            ResultSet rs = queryStmt.executeQuery();
            while (rs.next()) {
                String cacheID = rs.getString(1);
                MemCachedManager.delete(cacheID);
            }
            queryStmt.close();
            PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM REPORT_CACHE WHERE REPORT_ID = ?");
            deleteStmt.setLong(1, reportID);
            deleteStmt.executeUpdate();
            deleteStmt.close();
        } catch (Exception e) {
            LogClass.error(e);
        } finally {
            Database.closeConnection(conn);
        }
    }
}
