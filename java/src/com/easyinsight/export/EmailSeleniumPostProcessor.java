package com.easyinsight.export;

import com.easyinsight.config.ConfigLoader;
import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.logging.LogClass;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.util.RandomTextGenerator;
import com.xerox.amazonws.sqs2.MessageQueue;
import com.xerox.amazonws.sqs2.SQSUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.sql.*;

/**
 * User: jamesboe
 * Date: Sep 15, 2010
 * Time: 4:30:37 PM
 */
public class EmailSeleniumPostProcessor extends SeleniumPostProcessor {

    public static final int SEND_ON_RESPONSE = 0;
    public static final int PART_OF_MULTIPLE = 2;

    private long accountActivityID;
    private int actionType;

    public EmailSeleniumPostProcessor(long accountActivityID, int actionType) {
        this.accountActivityID = accountActivityID;
        this.actionType = actionType;
    }

    public long save(EIConnection conn) throws SQLException {
        long id = super.save(conn);
        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO email_selenium_processor (scheduled_account_activity_id, " +
                "selenium_processor_id, action_type) values (?, ?, ?)");
        insertStmt.setLong(1, accountActivityID);
        insertStmt.setLong(2, id);
        insertStmt.setInt(3, actionType);
        insertStmt.execute();
        return id;
    }

    @Override
    public int getProcessorType() {
        return SeleniumPostProcessor.EMAIL;
    }

    @Override
    public void process(byte[] bytes, EIConnection conn, long accountID, long requestID, long processorID) {
        try {
            PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO PNG_EXPORT (USER_ID, PNG_IMAGE, REPORT_NAME, ANONYMOUS_ID) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            System.out.println("on request " + requestID);
            if (actionType == ReportDelivery.PDF) {
                byte[] pdf = new ExportService().toImagePDF(bytes, 770, 523);
                ByteArrayInputStream bais = new ByteArrayInputStream(pdf);
                BufferedInputStream bis = new BufferedInputStream(bais, 1024);
                long userID = SecurityUtil.getUserID(false);
                if (userID == 0) {
                    insertStmt.setNull(1, Types.BIGINT);
                } else {
                    insertStmt.setLong(1, SecurityUtil.getUserID());
                }
                insertStmt.setBinaryStream(2, bis, pdf.length);
                insertStmt.setString(3, "export");
                String anonID = RandomTextGenerator.generateText(20);
                insertStmt.setString(4, anonID);
                insertStmt.execute();
                long id = Database.instance().getAutoGenKey(insertStmt);
                System.out.println("sending PDF of " + requestID + " to queue");
                MessageQueue msgQueue = SQSUtils.connectToQueue(ConfigLoader.instance().getReportDeliveryQueue(), "0AWCBQ78TJR8QCY8ABG2", "bTUPJqHHeC15+g59BQP8ackadCZj/TsSucNwPwuI");
                msgQueue.sendMessage(processorID + "|" + id);
            } else if (actionType == ReportDelivery.PNG) {
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                BufferedInputStream bis = new BufferedInputStream(bais, 1024);
                long userID = SecurityUtil.getUserID(false);
                if (userID == 0) {
                    insertStmt.setNull(1, Types.BIGINT);
                } else {
                    insertStmt.setLong(1, SecurityUtil.getUserID());
                }
                insertStmt.setBinaryStream(2, bis, bytes.length);
                insertStmt.setString(3, "export");
                String anonID = RandomTextGenerator.generateText(20);
                insertStmt.setString(4, anonID);
                insertStmt.execute();
                long id = Database.instance().getAutoGenKey(insertStmt);
                System.out.println("sending PNG of " + id + " to queue");
                MessageQueue msgQueue = SQSUtils.connectToQueue(ConfigLoader.instance().getReportDeliveryQueue(), "0AWCBQ78TJR8QCY8ABG2", "bTUPJqHHeC15+g59BQP8ackadCZj/TsSucNwPwuI");
                msgQueue.sendMessage(processorID + "|" + id);
            }
        } catch (Exception e) {
            LogClass.error(e);
        }
    }

    public static SeleniumPostProcessor load(long processorID, EIConnection conn) throws SQLException {
        PreparedStatement queryStmt = conn.prepareStatement("SELECT scheduled_account_activity_id, action_type FROM EMAIL_SELENIUM_PROCESSOR WHERE " +
                "SELENIUM_PROCESSOR_ID = ?");
        queryStmt.setLong(1, processorID);
        ResultSet rs = queryStmt.executeQuery();
        rs.next();
        long reportDeliveryID = rs.getLong(1);
        int actionType = rs.getInt(2);
        return new EmailSeleniumPostProcessor(reportDeliveryID, actionType);
    }
}
