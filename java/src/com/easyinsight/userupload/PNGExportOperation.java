package com.easyinsight.userupload;

import com.easyinsight.database.Database;
import com.easyinsight.logging.LogClass;

import java.sql.*;

/**
 * User: James Boe
 * Date: Oct 11, 2008
 * Time: 1:54:31 PM
 */
public class PNGExportOperation implements FileOperation {
    public byte[] retrieve(long fileID, long userID) {
        byte[] bytes;
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT PNG_IMAGE, USER_ID FROM PNG_EXPORT WHERE PNG_EXPORT_ID = ?");
            queryStmt.setLong(1, fileID);
            ResultSet rs = queryStmt.executeQuery();
            if (rs.next()) {
                long dbUserID = rs.getLong(2);
                if (dbUserID != userID) {
                    throw new SecurityException();
                } else {
                    bytes = rs.getBytes(1);
                }
            } else {
                throw new RuntimeException("No data found for that file ID.");
            }
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.instance().closeConnection(conn);
        }
        return bytes;
    }

    public long write(byte[] file, long userID) {
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO PNG_EXPORT (PNG_IMAGE, USER_ID) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            insertStmt.setBytes(1, file);
            insertStmt.setLong(2, userID);
            insertStmt.execute();
            return Database.instance().getAutoGenKey(insertStmt);
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.instance().closeConnection(conn);
        }
    }
}
