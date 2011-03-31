package com.easyinsight.datafeeds.file;

import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.userupload.*;

import java.sql.*;

/**
 * User: James Boe
 * Date: Jul 13, 2008
 * Time: 10:26:47 AM
 */
public class FileBasedFeedDefinition extends FeedDefinition {

    public static final int HSSF_MODEL = 1;
    public static final int XSSF_MODEL = 2;

    private UploadFormat uploadFormat;

    public UploadFormat getUploadFormat() {
        return uploadFormat;
    }

    public void setUploadFormat(UploadFormat uploadFormat) {
        this.uploadFormat = uploadFormat;
    }

    public void customStorage(Connection conn) throws SQLException {
        super.customStorage(conn);
        uploadFormat.persist(conn, getDataFeedID());
    }

    public FeedType getFeedType() {
        return FeedType.STATIC;
    }

    public void customLoad(Connection conn) throws SQLException {
        super.customLoad(conn);
        UploadFormat uploadFormat;
        PreparedStatement excelFormatStmt = conn.prepareStatement("SELECT EXCEL_MODEL FROM EXCEL_UPLOAD_FORMAT WHERE " +
                "FEED_ID = ?");
        excelFormatStmt.setLong(1, getDataFeedID());
        ResultSet rs = excelFormatStmt.executeQuery();
        if (rs.next()) {
            int model = rs.getInt(1);
            if (model == HSSF_MODEL)
                uploadFormat = new ExcelUploadFormat();
            else
                uploadFormat = new XSSFExcelUploadFormat();
        } else {
            PreparedStatement delimitedFormatStmt = conn.prepareStatement("SELECT DELIMITER_PATTERN, DELIMITER_ESCAPE FROM " +
                    "FLAT_FILE_UPLOAD_FORMAT WHERE FEED_ID = ?");
            delimitedFormatStmt.setLong(1, getDataFeedID());
            rs = delimitedFormatStmt.executeQuery();
            if (rs.next()) {
                String pattern = rs.getString(1);
                String escape = rs.getString(2);
                if (rs.wasNull()) {
                    escape = null;
                }
                if(!pattern.equals(","))
                    uploadFormat = new FlatFileUploadFormat(pattern, escape);
                else
                    uploadFormat = new CsvFileUploadFormat();
            } else {
                throw new RuntimeException("Could not match feed to an upload format");
            }
            delimitedFormatStmt.close();
        }
        excelFormatStmt.close();
        this.uploadFormat = uploadFormat;
    }
}
