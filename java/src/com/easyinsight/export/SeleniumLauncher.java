package com.easyinsight.export;

import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.logging.LogClass;
import com.easyinsight.util.RandomTextGenerator;
import com.xerox.amazonws.sqs2.MessageQueue;
import com.xerox.amazonws.sqs2.SQSUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

/**
 * User: jamesboe
 * Date: Sep 23, 2010
 * Time: 11:55:38 AM
 */
public class SeleniumLauncher {

    public static final String OUTBOUND_QUEUE = "EISelenium";

    private static final String URL = "/app/selenium/selenium.jsp?userName={0}&password={1}&dataSourceID={3}&reportID={4}&reportType={5}&width={6}&height={7}&seleniumID={2}";
    private static final String DASHBOARD_URL = "/app/selenium/seleniumDashboard.jsp?userName={0}&password={1}&seleniumID={2}&dashboardID={3}&width={4}&height={5}";

    public long requestSeleniumDrawForReport(long reportID, long accountActivityID, long userID, long accountID, EIConnection conn,
                                             int actionType, DeliveryExtension deliveryExtension) throws SQLException {
        EmailSeleniumPostProcessor processor = new EmailSeleniumPostProcessor(accountActivityID, actionType);
        long id = processor.save(conn);
        String userName = RandomTextGenerator.generateText(50);
        String password = RandomTextGenerator.generateText(50);
        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO SELENIUM_REQUEST (SELENIUM_PROCESSOR_ID, " +
                "USERNAME, PASSWORD, REQUEST_TIME, USER_ID, ACCOUNT_ID) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        insertStmt.setLong(1, id);
        insertStmt.setString(2, userName);
        insertStmt.setString(3, password);
        insertStmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
        insertStmt.setLong(5, userID);
        insertStmt.setLong(6, accountID);
        insertStmt.execute();
        String seleniumID = String.valueOf(Database.instance().getAutoGenKey(insertStmt));
        PreparedStatement queryStmt = conn.prepareStatement("SELECT ANALYSIS.title, ANALYSIS.data_feed_id, ANALYSIS.report_type, ANALYSIS.url_key FROM " +
                "ANALYSIS WHERE ANALYSIS.analysis_id = ?");
        queryStmt.setLong(1, reportID);
        ResultSet rs = queryStmt.executeQuery();
        rs.next();
        String dataSourceID = String.valueOf(rs.getLong(2));
        String reportType = String.valueOf(rs.getInt(3));
        String urlKey = rs.getString(4);
        String url = MessageFormat.format(URL, userName, password, seleniumID, dataSourceID, urlKey, reportType, "1000", "800");
        if (deliveryExtension != null) {
            url += deliveryExtension.toURL();
        }
        queryStmt.close();
        System.out.println("https://localhost:4443" + url);
        launchRequest(url);
        return id;
    }

    public long requestSeleniumDrawForDashboard(long dashboardID, long accountActivityID, long userID, long accountID, EIConnection conn,
                                             int actionType, DeliveryExtension deliveryExtension) throws SQLException {
        EmailSeleniumPostProcessor processor = new EmailSeleniumPostProcessor(accountActivityID, actionType);
        long id = processor.save(conn);
        String userName = RandomTextGenerator.generateText(50);
        String password = RandomTextGenerator.generateText(50);
        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO SELENIUM_REQUEST (SELENIUM_PROCESSOR_ID, " +
                "USERNAME, PASSWORD, REQUEST_TIME, USER_ID, ACCOUNT_ID) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        insertStmt.setLong(1, id);
        insertStmt.setString(2, userName);
        insertStmt.setString(3, password);
        insertStmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
        insertStmt.setLong(5, userID);
        insertStmt.setLong(6, accountID);
        insertStmt.execute();
        String seleniumID = String.valueOf(Database.instance().getAutoGenKey(insertStmt));
        PreparedStatement queryStmt = conn.prepareStatement("SELECT DASHBOARD.url_key FROM " +
                "DASHBOARD WHERE dashboard.dashboard_id = ?");
        queryStmt.setLong(1, dashboardID);
        ResultSet rs = queryStmt.executeQuery();
        rs.next();
        String urlKey = rs.getString(1);
        String url = MessageFormat.format(DASHBOARD_URL, userName, password, seleniumID, String.valueOf(dashboardID), "1000", "800");
        if (deliveryExtension != null) {
            url += deliveryExtension.toURL();
        }
        System.out.println("https://localhost:4443" + url);
        queryStmt.close();
        launchRequest(url);
        return id;
    }

    public long requestSeleniumDrawForMobile(long reportID, long userID, long accountID, EIConnection conn, int width, int height) throws SQLException {
        HtmlSeleniumPostProcessor processor = new HtmlSeleniumPostProcessor(reportID);
        long id = processor.save(conn);
        String userName = RandomTextGenerator.generateText(50);
        String password = RandomTextGenerator.generateText(50);
        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO SELENIUM_REQUEST (SELENIUM_PROCESSOR_ID, " +
                "USERNAME, PASSWORD, REQUEST_TIME, USER_ID, ACCOUNT_ID) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        insertStmt.setLong(1, id);
        insertStmt.setString(2, userName);
        insertStmt.setString(3, password);
        insertStmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
        insertStmt.setLong(5, userID);
        insertStmt.setLong(6, accountID);
        insertStmt.execute();
        long requestID = Database.instance().getAutoGenKey(insertStmt);
        String seleniumID = String.valueOf(requestID);
        PreparedStatement queryStmt = conn.prepareStatement("SELECT ANALYSIS.title, ANALYSIS.data_feed_id, ANALYSIS.report_type, ANALYSIS.url_key FROM " +
                "ANALYSIS WHERE ANALYSIS.analysis_id = ?");
        queryStmt.setLong(1, reportID);
        ResultSet rs = queryStmt.executeQuery();
        rs.next();
        String dataSourceID = String.valueOf(rs.getLong(2));
        String reportType = String.valueOf(rs.getInt(3));
        String urlKey = rs.getString(4);
        String url = MessageFormat.format(URL, userName, password, seleniumID, dataSourceID, urlKey, reportType, String.valueOf(width), String.valueOf(height));
        queryStmt.close();
        launchRequest(url);
        return requestID;
    }

    private void launchRequest(String url) {
        // send an SQS request
        try {
            MessageQueue msgQueue = SQSUtils.connectToQueue(OUTBOUND_QUEUE, "0AWCBQ78TJR8QCY8ABG2", "bTUPJqHHeC15+g59BQP8ackadCZj/TsSucNwPwuI");
            msgQueue.sendMessage(url);
        } catch (Exception e) {
            LogClass.error(e);
        }
    }
}
