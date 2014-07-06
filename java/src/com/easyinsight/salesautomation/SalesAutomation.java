package com.easyinsight.salesautomation;

import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.email.SendGridEmail;
import com.easyinsight.logging.LogClass;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: jamesboe
 * Date: 6/27/14
 * Time: 11:39 AM
 */
public class SalesAutomation {

    public static final int NO_DATA = 1;
    public static final int SMALL_BIZ = 2;
    public static final int ENTERPRISE = 3;

    public void sendEmailsToSelected(List<Long> accountIDs, String rep, String repEmail) {
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement accountStmt = conn.prepareStatement("SELECT user.first_name, user.email, user.user_id FROM user WHERE user.account_id = ? AND user.account_admin = ? AND " +
                    "user.opt_in_email = ?");
            PreparedStatement updateEmailsStmt = conn.prepareStatement("UPDATE emails_to_send SET email_sender = ?, email_sent = ?, email_sent_at = ? WHERE account_id = ?");
            PreparedStatement findSourceStmt = conn.prepareStatement("SELECT data_feed.feed_type FROM data_feed, upload_policy_users WHERE " +
                    "data_feed.data_feed_id = upload_policy_users.feed_id AND upload_policy_users.user_id = ? AND data_feed.visible = ? AND data_feed.parent_source_id is null");
            for (Long accountID : accountIDs) {
                accountStmt.setLong(1, accountID);
                accountStmt.setBoolean(2, true);
                accountStmt.setBoolean(3, true);
                ResultSet rs = accountStmt.executeQuery();
                if (rs.next()) {
                    String firstName = rs.getString(1);
                    String email = rs.getString(2);
                    long userID = rs.getLong(3);

                    findSourceStmt.setLong(1, userID);
                    findSourceStmt.setBoolean(2, true);
                    ResultSet dataSourceRS = findSourceStmt.executeQuery();

                    int emailToSend = NO_DATA;
                    while (dataSourceRS.next()) {
                        int type = dataSourceRS.getInt(1);
                        if (type == FeedType.STATIC.getType() || type == FeedType.DEFAULT.getType() || type == FeedType.SERVER_MYSQL.getType() ||
                                type == FeedType.SALESFORCE.getType() || type == FeedType.SERVER_ORACLE.getType() || type == FeedType.SERVER_POSTGRES.getType() ||
                                type == FeedType.SERVER_SQL_SERVER.getType()) {
                            emailToSend = ENTERPRISE;
                        } else if (emailToSend == NO_DATA){
                            emailToSend = SMALL_BIZ;
                        }
                    }


                    // send email here
                    String smallBizBody = "Hi {0},\r\n\r\nI wanted to reach out to you to make sure you are able to get up and running quickly with Easy Insight. If you have any questions I am happy to jump on a quick call and/or screenshare for a basic tutorial to help you get familiar with Easy Insight, use our prebuilt reports and dashboards or to get you on track for creating your own reports that suit your specific reporting needs.\r\n\r\nThanks for your interest in Easy Insight!\r\n\r\n{1}\r\n{2}\r\nhttp://www.easy-insight.com/";
                    String enterpriseBody = "Hi {0},\r\n\r\nI wanted to reach out to you to make sure you are able to get up and running quickly with Easy Insight. If you have any questions, need help structuring your data or if you're interested in our client services I am happy to jump on a quick call to work out the details.\r\n\r\nThanks for your interest in Easy Insight!\r\n\r\n{1}\r\n{2}\r\nhttp://www.easy-insight.com/";
                    String body;
                    if (emailToSend == NO_DATA || emailToSend == SMALL_BIZ) {
                        body = smallBizBody;
                    } else {
                        body = enterpriseBody;
                    }
                    String formatted = MessageFormat.format(body, firstName, rep, "1-720-316-8174");
                    new SendGridEmail().sendEmail(email, "Welcoming you to Easy Insight", formatted, repEmail, false, rep);

                    updateEmailsStmt.setString(1, rep);
                    updateEmailsStmt.setBoolean(2, true);
                    updateEmailsStmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    updateEmailsStmt.setLong(4, accountID);
                    updateEmailsStmt.executeUpdate();

                }
            }
            updateEmailsStmt.close();
            accountStmt.close();
        } catch (Exception e) {
            LogClass.error(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public static void main(String[] args) {
        String body = "Hi {0},\n\nI wanted to reach out with a quick email to make sure you were able to get up and going with Easy Insight. If you have any questions, encountered any issues, or just need a quick start, I''d be happy to jump on a call and/or screenshare with you to help make sure you''re able to get the reports and dashboards you need.\n\nThanks for your interest in Easy Insight!\n\n{1}\n{2}\nhttp://www.easy-insight.com/";
        String formatted = MessageFormat.format(body, "Jim", "James Boe", "1-720-316-8174");
        System.out.println(formatted);
    }

    public void noEmails(List<Long> accountIDs) {
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement updateEmailsStmt = conn.prepareStatement("UPDATE emails_to_send SET email_sent = ? WHERE account_id = ?");
            for (Long accountID : accountIDs) {
                updateEmailsStmt.setBoolean(1, true);
                updateEmailsStmt.setLong(2, accountID);
                updateEmailsStmt.executeUpdate();
            }
            updateEmailsStmt.close();
        } catch (Exception e) {
            LogClass.error(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public List<SalesAccount> automate() {
        List<SalesAccount> salesAccounts = new ArrayList<>();
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement query = conn.prepareStatement("SELECT account.account_id, user.first_name, user.name, user.email, account.creation_date from " +
                    "emails_to_send, account, user " +
                    "where email_sent = ? AND " +
                    "emails_to_send.account_id = account.account_id AND account.account_id = user.account_id AND user.account_admin = ? AND account.account_state = ? AND " +
                    "user.opt_in_email = ?");
            query.setBoolean(1, false);
            query.setBoolean(2, true);
            query.setInt(3, com.easyinsight.users.Account.TRIAL);
            query.setBoolean(4, true);
            ResultSet rs = query.executeQuery();
            while (rs.next()) {
                long accountID = rs.getLong(1);
                String firstName = rs.getString(2);
                String lastName = rs.getString(3);
                String email = rs.getString(4);
                Date date = new java.util.Date(rs.getTimestamp(5).getTime());
                salesAccounts.add(new SalesAccount(email, firstName, lastName, accountID, date));
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
        return salesAccounts;
    }

}
