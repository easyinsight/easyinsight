package com.easyinsight.salesautomation;

import com.easyinsight.admin.ConstantContactSync;
import com.easyinsight.database.EIConnection;
import com.easyinsight.email.SendGridEmail;
import com.easyinsight.logging.LogClass;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.users.Account;
import com.easyinsight.users.User;
import com.easyinsight.util.RandomTextGenerator;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * User: jamesboe
 * Date: Jan 8, 2010
 * Time: 4:45:03 PM
 */
public class SalesEmail implements Runnable {

    public static final int ONE_DAY = 1;
    public static final int ONE_WEEK = 2;
    public static final int TWO_WEEKS = 3;
    public static final int THREE_WEEKS = 4;
    public static final int END_OF_TRIAL = 5;
    public static final int ONE_DAY_BACK = 6;
    public static final int ONE_WEEK_BACK = 7;
    public static final int TWO_WEEKS_BACK = 8;

    private static String newAccountNotification =
            "New account created:\r\n\r\n" +
            "Account Type: {0}\r\n" +
            "User Name: {1}\r\n" +
            "First Name: {2}\r\n" +
            "Last Name: {3}\r\n" +
            "Email Address: {4}";

    private Account account;
    private User user;

    public SalesEmail(Account account, User user) {
        this.account = account;
        this.user = user;
    }

    public void run() {
        String accountType;
        if (account.getAccountType() == Account.PERSONAL) {
            accountType = "Personal";
        } else if (account.getAccountType() == Account.BASIC) {
            accountType = "Basic";
        }  else if (account.getAccountType() == Account.PLUS) {
            accountType = "Plus";
        } else if (account.getAccountType() == Account.PROFESSIONAL) {
            accountType = "Professional";
        } else if (account.getAccountType() == Account.PREMIUM) {
            accountType = "Premium";
        } else {
            accountType = "Enterprise";
        }
        String body = MessageFormat.format(newAccountNotification, accountType, user.getUserName(), user.getFirstName(), user.getName(), user.getEmail());
        String subject = "New " + accountType + " Account Created";
        try {
            new SendGridEmail().sendEmail("sales@easy-insight.com", subject, body, "donotreply@easy-insight.com", false, "Easy Insight");
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public static void leadNurture(long userID, int number, EIConnection conn) throws SQLException, MessagingException, UnsupportedEncodingException {

        // does the user have any KPIs defined?

        PreparedStatement userStmt = conn.prepareStatement("SELECT USER.email, USER.first_name, USER.name, ACCOUNT.account_type, ACCOUNT.account_state FROM USER, ACCOUNT WHERE USER.USER_ID = ? AND " +
                "USER.account_id = ACCOUNT.account_id and user.opt_in_email = ?");
        userStmt.setLong(1, userID);
        userStmt.setBoolean(2, true);
        ResultSet rs = userStmt.executeQuery();
        rs.next();

        String email = rs.getString(1);
        String firstName = rs.getString(2);
        String lastName = rs.getString(3);
        int accountState = rs.getInt(5);

        if (accountState != Account.TRIAL) return;

        String subject = null;

        // Let's quickly walk through how you can build a report in Easy Insight. Once you've created a data source,
        // you can click on the "New Report" button to jump into the report editor. In the report editor, you'll see
        // a set of fields down the left hand side of the

        // basic report creation
        // prebuilt reports and dashboards
        // creating filters

        // delivering reports via email
        // exporting to excel and pdf
        // custom fields

        // push connecting to more systems
        // more advanced calculations
        // more advanced filters

        // dashboards
        // combining data sources
        // styling/branding

        // end of trial

        String string = null;
        if (number == ONE_DAY) {
            // nothing yet
            return;
        } else if (number == ONE_WEEK) {
            subject = "Tips for the best Easy Insight experience";
            /*emailBlocks.add(new ParagraphEmailBlock("You're one week into your trial with Easy Insight, so here's a few tips to help make sure you're getting the most out of Easy Insight:"));
            emailBlocks.add(new ParagraphEmailBlock("Want up to date reports on your business waiting in your inbox every morning? You can set up any report inside of Easy Insight to export and deliver over email on a scheduled basis. We offer a number of export formats to meet your preferences--Excel, PDF, PNG, and inline HTML tables. You can set up report delivery in the Scheduling page, from the Export tab of the report editor, or as part of the report save process."));
            emailBlocks.add(new ParagraphEmailBlock("Need a little help getting reports up and running? Check out the Exchange page of Easy Insight to find prebuilt reports and dashboards. You can apply these reports to your own data with a single click. As you build your own useful reports, we encourage you to publish them to the Exchange as well to help improve the experience of all Easy Insight users."));
            emailBlocks.add(new ParagraphEmailBlock("Find yourself wanting to pull up key metrics or reports while you're away from your computer? Easy Insight's mobile support gives you the ability to quickly pull up simplified forms of reports on any mobile device. You can access Easy Insight Mobile by clicking on the Customer Login link at www.easy-insight.com or by navigating directly to https://www.easy-insight.com/app/htmlreport."));
            emailBlocks.add(new ParagraphEmailBlock("Have any questions or comments? Please don't hesitate to contact us at sales@easy-insight.com or support@easy-insight.com!"));
            emailBlocks.add(new ParagraphEmailBlock("The Team at Easy Insight", true));*/
            string = ConstantContactSync.getContent(ConstantContactSync.ONE_WEEK);
        } else if (number == TWO_WEEKS) {
            subject = "More tips for the best Easy Insight experience";
            /*emailBlocks.add(new ParagraphEmailBlock("You're halfway through your trial with Easy Insight, so here's a few suggestions on how to take advantage of the wide variety of functionality we offer:"));
            emailBlocks.add(new ParagraphEmailBlock("You can greatly expand your capabilities for reporting on any particular SaaS system with custom fields. Your CRM system doesn't have an Expected Close Date or Target Revenue? You can add custom fields in Easy Insight through multiple mechanisms. Check out our documentation at http://www.easy-insight.com/documentation/customfields.html for further information about these capabilities."));
            emailBlocks.add(new ParagraphEmailBlock("Want to style Easy Insight to meet your corporate branding or just to look a little more like your desktop? You can define styling at a user or account level from the Account Settings page. Whether you just want to change the application background to a particular favorite image or overhaul the entire color scheme, check out the options offered under Account and User Skins to make the look of Easy Insight match your preferences."));
            emailBlocks.add(new ParagraphEmailBlock("Once you have a handful of reports created on one of your data sources, you can start building out full dashboards to display many reports in a single screen. Cick on Create Dashboard on the My Data page and you can start assembling interactive dashboards with multiple reports in a single screen view."));
            emailBlocks.add(new ParagraphEmailBlock("Have any questions or comments? Please don't hesitate to contact us at sales@easy-insight.com or support@easy-insight.com!"));
            emailBlocks.add(new ParagraphEmailBlock("The Team at Easy Insight", true));*/
            string = ConstantContactSync.getContent(ConstantContactSync.TWO_WEEKS);
        } else if (number == THREE_WEEKS) {
            // nothing yet
            subject = "More tips for the best Easy Insight experience";
            /*emailBlocks.add(new ParagraphEmailBlock("You're three weeks into your trial with Easy Insight, so here's a few more suggestions on how to take advantage of the wide variety of functionality we offer:"));
            emailBlocks.add(new ParagraphEmailBlock("Have multiple Highrise systems you want to combine into a single view? Want to join data from your Constant Contact system with your Salesforce system? Easy Insight has tremendous capabilities around joining data from disparate data sources."));
            emailBlocks.add(new ParagraphEmailBlock("Want to style Easy Insight to meet your corporate branding or just to look a little more like your desktop? You can define styling at a user or account level from the Account Settings page. Whether you just want to change the application background to a particular favorite image or overhaul the entire color scheme, check out the options offered under Account and User Skins to make the look of Easy Insight match your preferences."));
            emailBlocks.add(new ParagraphEmailBlock("Once you have a handful of reports created on one of your data sources, you can start building out full dashboards to display many reports in a single screen. Cick on Create Dashboard on the My Data page and you can start assembling interactive dashboards with multiple reports in a single screen view."));
            emailBlocks.add(new ParagraphEmailBlock("Have any questions or comments? Please don't hesitate to contact us at sales@easy-insight.com or support@easy-insight.com!"));
            emailBlocks.add(new ParagraphEmailBlock("The Team at Easy Insight", true));*/
            string = ConstantContactSync.getContent(ConstantContactSync.THREE_WEEKS);
        } else if (number == END_OF_TRIAL) {
            subject = "Your Easy Insight trial is almost over";
            /*emailBlocks.add(new ParagraphEmailBlock("Your trial with Easy Insight is coming to an end soon. Pricing information is available at https://www.easy-insight.com/app/newaccount. We'd love to have your business, so if you have any questions or concerns, please contact us at sales@easy-insight.com."));
            emailBlocks.add(new ParagraphEmailBlock("The Team at Easy Insight", true));*/
            string = ConstantContactSync.getContent(ConstantContactSync.FOUR_WEEKS);
        }

        if (string != null) {
            PreparedStatement queryUnsubscribeStmt = conn.prepareStatement("SELECT unsubscribe_key from user_unsubscribe_key WHERE USER_ID = ?");
            PreparedStatement insertKeyStmt = conn.prepareStatement("INSERT INTO USER_UNSUBSCRIBE_KEY (USER_ID, UNSUBSCRIBE_KEY) VALUES (?, ?)");
            queryUnsubscribeStmt.setLong(1, SecurityUtil.getUserID());
            ResultSet unsubscribeRS = queryUnsubscribeStmt.executeQuery();
            String unsubscribeKey;
            if (unsubscribeRS.next()) {
                unsubscribeKey = unsubscribeRS.getString(1);
            } else {
                unsubscribeKey = RandomTextGenerator.generateText(25);
                insertKeyStmt.setLong(1, SecurityUtil.getUserID());
                insertKeyStmt.setString(2, unsubscribeKey);
                insertKeyStmt.execute();
            }
            String emailBody = string.replace("{0}", "https://www.easy-insight.com/app/unsubscribe?user=" + unsubscribeKey);
            new SendGridEmail().sendEmail(email, subject, emailBody, "sales@easy-insight.com", true, "Easy Insight Marketing");
        }

        /*if (emailBlocks.size() > 1) {
            StringBuilder sb = new StringBuilder();
            for (EmailBlock emailBlock : emailBlocks) {
                sb.append(emailBlock.toText());
            }

        }*/
    }
}
