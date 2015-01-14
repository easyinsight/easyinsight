package com.easyinsight.userupload;

import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.core.Key;
import com.easyinsight.database.EIConnection;
import com.easyinsight.scheduler.FileProcessCreateScheduledTask;

import com.easyinsight.scheduler.FileProcessOptimizedCreateScheduledTask;
import com.easyinsight.scheduler.RedshiftFileCreate;
import com.easyinsight.security.SecurityUtil;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * User: jamesboe
 * Date: Mar 27, 2010
 * Time: 3:21:36 PM
 */
public class FlatFileUploadContext extends UploadContext implements Serializable  {
    private String uploadKey;
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUploadKey() {
        return uploadKey;
    }

    public void setUploadKey(String uploadKey) {
        this.uploadKey = uploadKey;
    }

    private transient UploadFormat uploadFormat;

    @Override
    public String validateUpload(EIConnection conn) throws SQLException {
        if (fileName.endsWith(".xlsx")) {
            uploadFormat = new XSSFExcelUploadFormat();
        } else if (fileName.endsWith(".xls")) {
            uploadFormat = new ExcelUploadFormat();
        } else {
            uploadFormat = new CsvFileUploadFormat();
        }
        return null;
    }

    private Map<Key, Set<String>> sampleMap;

    @Override
    public List<AnalysisItem> guessFields(EIConnection conn, byte[] bytes) throws Exception {
        UserUploadAnalysis userUploadAnalysis = uploadFormat.analyze(bytes);
        sampleMap = userUploadAnalysis.getSampleMap();
        return userUploadAnalysis.getFields();
    }

    public long createDataSource(String name, List<AnalysisItem> analysisItems, EIConnection conn, boolean accountVisible, byte[] bytes) throws Exception {
        UploadFormat uploadFormat = new UploadFormatTester().determineFormat(bytes);
        PreparedStatement dbStmt = conn.prepareStatement("SELECT special_storage FROM account WHERE account_id = ?");
        dbStmt.setLong(1, SecurityUtil.getAccountID());
        ResultSet rs = dbStmt.executeQuery();
        rs.next();
        String specialStorage = rs.getString(1);
        dbStmt.close();
        if (uploadFormat instanceof CsvFileUploadFormat) {
            if (specialStorage != null) {
                System.out.println("Using Redshift file creation...");
                RedshiftFileCreate task = new RedshiftFileCreate();
                task.setName(name);
                task.setUserID(SecurityUtil.getUserID());
                task.setAccountID(SecurityUtil.getAccountID());
                task.createFeed(conn, bytes, uploadFormat, analysisItems, accountVisible);
                return task.getFeedID();
            } else {
                System.out.println("Using legacy file creation...");
                FileProcessOptimizedCreateScheduledTask task = new FileProcessOptimizedCreateScheduledTask();
                task.setName(name);
                task.setUserID(SecurityUtil.getUserID());
                task.setAccountID(SecurityUtil.getAccountID());
                task.createFeed(conn, bytes, uploadFormat, analysisItems, accountVisible);
                return task.getFeedID();
            }
        } else {
            FileProcessCreateScheduledTask task = new FileProcessCreateScheduledTask();
            task.setName(name);
            task.setUserID(SecurityUtil.getUserID());
            task.setAccountID(SecurityUtil.getAccountID());
            task.createFeed(conn, bytes, uploadFormat, analysisItems, accountVisible);
            return task.getFeedID();
        }
    }

    @Override
    public List<String> getSampleValues(Key key) {
        return new ArrayList<String>(sampleMap.get(key));
    }
}
