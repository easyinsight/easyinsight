package com.easyinsight.users;

import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

import org.jetbrains.annotations.Nullable;

/**
 * User: jamesboe
 * Date: Aug 24, 2009
 * Time: 9:12:26 AM
 */
public class TokenStorage {
    public static final int GOOGLE_DOCS_TOKEN = 1;
    public static final int GOOGLE_ANALYTICS_TOKEN = 2;

    public void saveToken(Token token) {
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT token_id FROM TOKEN where user_id = ? AND token_type = ?");
            queryStmt.setLong(1, token.getUserID());
            queryStmt.setInt(2, token.getTokenType());
            ResultSet rs = queryStmt.executeQuery();
            if (rs.next()) {
                PreparedStatement updateStmt = conn.prepareStatement("UPDATE TOKEN set token_value = ? WHERE token_id = ?");
                updateStmt.setString(1, token.getTokenValue());
                updateStmt.setLong(2, rs.getLong(1));
                updateStmt.executeUpdate();
            } else {
                PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO TOKEN (token_type, user_id, token_value) VALUES (?, ?, ?)");
                insertStmt.setInt(1, token.getTokenType());
                insertStmt.setLong(2, token.getUserID());
                insertStmt.setString(3, token.getTokenValue());
                insertStmt.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public void deleteToken(Token token) {
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM TOKEN WHERE TOKEN_TYPE = ? AND USER_ID = ?");
            deleteStmt.setInt(1, token.getTokenType());
            deleteStmt.setLong(2, token.getUserID());
            deleteStmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    @Nullable
    public Token getToken(long userID, int type) {
        Token token = null;
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT token_value FROM TOKEN where user_id = ? AND token_type = ?");
            queryStmt.setLong(1, userID);
            queryStmt.setInt(2, type);
            ResultSet rs = queryStmt.executeQuery();
            if (rs.next()) {
                String tokenValue = rs.getString(1);
                token = new Token();
                token.setUserID(userID);
                token.setTokenType(type);
                token.setTokenValue(tokenValue);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
        return token;
    }
}
