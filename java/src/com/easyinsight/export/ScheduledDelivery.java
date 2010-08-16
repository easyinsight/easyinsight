package com.easyinsight.export;

import com.easyinsight.database.EIConnection;
import com.easyinsight.email.UserStub;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: jamesboe
 * Date: Jun 6, 2010
 * Time: 1:55:39 PM
 */
public abstract class ScheduledDelivery extends ScheduledActivity {

    private List<UserStub> users = new ArrayList<UserStub>();
    private List<String> emails = new ArrayList<String>();

    public List<UserStub> getUsers() {
        return users;
    }

    public void setUsers(List<UserStub> users) {
        this.users = users;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    protected void customSave(EIConnection conn, int utcOffset) throws SQLException {
        PreparedStatement clearStmt = conn.prepareStatement("DELETE FROM delivery_to_user WHERE SCHEDULED_ACCOUNT_ACTIVITY_ID = ?");
        clearStmt.setLong(1, getScheduledActivityID());
        clearStmt.executeUpdate();
        clearStmt.close();
        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO delivery_to_user (user_id, SCHEDULED_ACCOUNT_ACTIVITY_ID) VALUES (?, ?)");
        for (UserStub userStub : users) {
            insertStmt.setLong(1, userStub.getUserID());
            insertStmt.setLong(2, getScheduledActivityID());
            insertStmt.execute();
        }
        insertStmt.close();
        PreparedStatement clearEmailStmt = conn.prepareStatement("DELETE FROM delivery_to_email WHERE SCHEDULED_ACCOUNT_ACTIVITY_ID = ?");
        clearEmailStmt.setLong(1, getScheduledActivityID());
        clearEmailStmt.executeUpdate();
        clearEmailStmt.close();
        PreparedStatement insertEmailStmt = conn.prepareStatement("INSERT INTO delivery_to_email (email_address, SCHEDULED_ACCOUNT_ACTIVITY_ID) VALUES (?, ?)");
        for (String email : emails) {
            insertEmailStmt.setString(1, email);
            insertEmailStmt.setLong(2, getScheduledActivityID());
            insertEmailStmt.execute();
        }
        insertEmailStmt.close();
    }

    protected void customLoad(EIConnection conn) throws SQLException {
        PreparedStatement queryStmt = conn.prepareStatement("SELECT USER.user_id, USER.username, USER.email, USER.name, USER.account_id, USER.first_name FROM " +
                "delivery_to_user, user WHERE " +
                "SCHEDULED_ACCOUNT_ACTIVITY_ID = ? AND delivery_to_user.user_id = USER.user_id");
        queryStmt.setLong(1, getScheduledActivityID());
        ResultSet rs = queryStmt.executeQuery();

        List<UserStub> users = new ArrayList<UserStub>();
        while (rs.next()) {
            UserStub userStub = new UserStub(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getLong(5), rs.getString(6));
            users.add(userStub);
        }
        queryStmt.close();
        PreparedStatement queryEmailStmt = conn.prepareStatement("SELECT EMAIL_ADDRESS FROM delivery_to_email where SCHEDULED_ACCOUNT_ACTIVITY_ID = ?");
        queryEmailStmt.setLong(1, getScheduledActivityID());
        List<String> emails = new ArrayList<String>();
        ResultSet emailRS = queryEmailStmt.executeQuery();
        while (emailRS.next()) {
            String email = emailRS.getString(1);
            emails.add(email);
        }
        setUsers(users);
        setEmails(emails);
    }
}
