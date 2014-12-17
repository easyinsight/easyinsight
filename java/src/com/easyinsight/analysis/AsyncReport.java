package com.easyinsight.analysis;

import com.easyinsight.analysis.definitions.WSCompareYearsDefinition;
import com.easyinsight.analysis.definitions.WSKPIDefinition;
import com.easyinsight.analysis.definitions.WSYTDDefinition;
import com.easyinsight.cache.MemCachedManager;
import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.logging.LogClass;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.userupload.DataSourceThreadPool;
import com.mysql.jdbc.Statement;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * User: jamesboe
 * Date: 12/16/14
 * Time: 11:16 AM
 */
public class AsyncReport {

    public void claimAndRun() throws InterruptedException {
        Long requestID = null;
        byte[] reportBytes = null;
        byte[] metadataBytes = null;
        int requestType = 0;
        long userID = 0;
        do {
            EIConnection conn = Database.instance().getConnection();
            try {
                conn.setAutoCommit(false);
                PreparedStatement q = conn.prepareStatement("SELECT async_report_request_id, report, metadata, request_type, user_id FROM async_report_request WHERE claimed = ?");
                q.setBoolean(1, false);
                ResultSet rs = q.executeQuery();
                if (rs.next()) {
                    requestID = rs.getLong(1);
                    reportBytes = rs.getBytes(2);
                    metadataBytes = rs.getBytes(3);
                    requestType = rs.getInt(4);
                    userID = rs.getLong(5);
                    PreparedStatement u = conn.prepareStatement("UPDATE async_report_request SET claimed = ? WHERE async_report_request_id = ?");
                    u.setBoolean(1, true);
                    u.setLong(2, requestID);
                    u.executeUpdate();
                    u.close();
                }
                q.close();
                conn.commit();
            } catch (Exception e) {
                LogClass.error(e);
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
                Database.closeConnection(conn);
            }
            if (requestID == null) {
                Thread.sleep(100);
            }
        } while (requestID == null);
        final Long frequestID = requestID;
        final byte[] freportBytes = reportBytes;
        final byte[] fmetadataBytes = metadataBytes;
        final int frequestType = requestType;
        final long fuserID = userID;


        DataSourceThreadPool.instance().addActivity(() -> {
            try {
                try {
                    EIConnection conn = Database.instance().getConnection();
                    try {
                        PreparedStatement queryStmt = conn.prepareStatement("SELECT USERNAME, ACCOUNT.ACCOUNT_TYPE, USER.account_admin," +
                                "ACCOUNT.FIRST_DAY_OF_WEEK, USER.first_name, USER.name, USER.email, USER.ACCOUNT_ID, USER.PERSONA_ID, USER.TEST_ACCOUNT_VISIBLE FROM USER, ACCOUNT " +
                                "WHERE USER.USER_ID = ?");
                        queryStmt.setLong(1, fuserID);
                        ResultSet queryRS = queryStmt.executeQuery();
                        queryRS.next();
                        String userName = queryRS.getString(1);
                        int accountType = queryRS.getInt(2);
                        boolean accountAdmin = queryRS.getBoolean(3);
                        int firstDayOfWeek = queryRS.getInt(4);
                        long accountID = queryRS.getLong(8);
                        long userPersonaID = queryRS.getLong("USER.persona_ID");
                        String personaName = null;
                        if (userPersonaID > 0) {
                            PreparedStatement personaNameStmt = conn.prepareStatement("SELECT persona.persona_name FROM persona WHERE persona_id = ?");
                            personaNameStmt.setLong(1, userPersonaID);
                            ResultSet personaRS = personaNameStmt.executeQuery();
                            if (personaRS.next()) {
                                personaName = personaRS.getString(1);
                            }
                            personaNameStmt.close();
                        }
                        SecurityUtil.populateThreadLocal(userName, fuserID, accountID, accountType, accountAdmin, firstDayOfWeek, personaName);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        Database.closeConnection(conn);
                    }

                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(freportBytes));
                    WSAnalysisDefinition report = (WSAnalysisDefinition) ois.readObject();
                    ois = new ObjectInputStream(new ByteArrayInputStream(fmetadataBytes));
                    InsightRequestMetadata insightRequestMetadata = (InsightRequestMetadata) ois.readObject();
                    InsightRequestMetadata localMetadata = new InsightRequestMetadata();
                    localMetadata.setUtcOffset(insightRequestMetadata.getUtcOffset());
                    localMetadata.setNoAsync(true);

                    System.out.println("Asynchronous execution of " + report.getName());

                    if (frequestType == REPORT_EDITOR) {
                        DataResults results;
                        if (report instanceof WSCrosstabDefinition) {
                            results = new DataService().getCrosstabDataResults((WSCrosstabDefinition) report, localMetadata);
                        } else if (report instanceof WSKPIDefinition) {
                            results = new DataService().getTrendDataResults((WSKPIDefinition) report, localMetadata);
                        } else if (report instanceof WSTreeDefinition) {
                            results = new DataService().getTreeDataResults((WSTreeDefinition) report, localMetadata);
                        } else if (report instanceof WSYTDDefinition) {
                            results = new DataService().getYTDResults(report, localMetadata);
                        } else if (report instanceof WSCompareYearsDefinition) {
                            results = new DataService().getCompareYearsResults(report, localMetadata);
                        } else {
                            results = new DataService().list(report, localMetadata);
                        }
                        ResultData rh = new ResultData();
                        rh.dataResults = results;
                        rh.report = report;
                        MemCachedManager.instance().add("async" + frequestID, 100, rh);
                    } else if (frequestType == REPORT_END_USER) {
                        EmbeddedResults results;
                        /*if (report instanceof WSCrosstabDefinition) {

                            results = getCrosstabDataResults((WSCrosstabDefinition) report, localMetadata);
                        } else if (report instanceof WSKPIDefinition) {
                            results = getTrendDataResults((WSKPIDefinition) report, localMetadata);
                        } else if (report instanceof WSTreeDefinition) {
                            results = getTreeDataResults((WSTreeDefinition) report, localMetadata);
                        } else if (report instanceof WSYTDDefinition) {
                            results = getYTDResults(report, localMetadata);
                        } else if (report instanceof WSCompareYearsDefinition) {
                            results = getCompareYearsResults(report, localMetadata);
                        } else {*/
                        conn = Database.instance().getConnection();
                        try {
                            results = new DataService().getEmbeddedResultsForReport(report, new ArrayList<>(), localMetadata, new ArrayList<>(), conn);
                        } finally {
                            Database.closeConnection(conn);
                        }
                        //}
                        ResultData rh = new ResultData();
                        rh.results = results;
                        rh.report = report;
                        MemCachedManager.instance().add("async" + frequestID, 100, rh);
                    } else if (frequestType == REPORT_DATA_SET) {
                        conn = Database.instance().getConnection();
                        try {
                            ResultData rh = new ResultData();
                            try {
                                DataSet dataSet = DataService.listDataSet(report, localMetadata, conn);
                                dataSet.setAsyncSavedReport(report);
                                rh.dataSet = dataSet;
                                rh.report = report;
                                MemCachedManager.instance().add("async" + frequestID, 100, rh);
                            } catch (Exception e) {
                                rh.exception = e;
                                MemCachedManager.instance().add("async" + frequestID, 100, rh);
                            }
                        } finally {
                            Database.closeConnection(conn);
                        }
                    }
                } finally {
                    SecurityUtil.clearThreadLocal();
                }

            } catch (Exception e) {
                LogClass.error(e);
            }
        });

    }

    public static final int REPORT_EDITOR = 1;
    public static final int REPORT_END_USER = 2;
    public static final int REPORT_DATA_SET = 3;

    public static DataResults asyncDataResults(final WSAnalysisDefinition analysisDefinition, final InsightRequestMetadata insightRequestMetadata, EIConnection conn)  {
        try {
            long reqID = createAsyncRequestID(analysisDefinition, insightRequestMetadata, conn, REPORT_EDITOR);

            ResultData dataResults = null;
            while (dataResults == null) {
                dataResults = (ResultData) MemCachedManager.instance().get("async" + reqID);
                if (dataResults == null) {
                    Thread.sleep(100);
                }
            }
            dataResults.dataResults.setReport(dataResults.report);
            return dataResults.dataResults;
        } catch (Exception e) {
            LogClass.error(e);
            ListDataResults embeddedDataResults = new ListDataResults();
            embeddedDataResults.setReportFault(new ServerError("Something went wrong in running the report."));
            return embeddedDataResults;
        }
    }

    private static long createAsyncRequestID(WSAnalysisDefinition analysisDefinition, InsightRequestMetadata insightRequestMetadata, EIConnection conn, int requestType) throws SQLException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(analysisDefinition);
        oos.flush();
        byte[] reportBytes = baos.toByteArray();
        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.writeObject(insightRequestMetadata);
        oos.flush();
        byte[] metadata = baos.toByteArray();
        PreparedStatement reqStmt = conn.prepareStatement("INSERT INTO async_report_request (claimed, report, metadata, request_created, request_type, user_id) VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        reqStmt.setBoolean(1, false);
        reqStmt.setBytes(2, reportBytes);
        reqStmt.setBytes(3, metadata);
        reqStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
        reqStmt.setInt(5, requestType);
        reqStmt.setLong(6, SecurityUtil.getUserID());
        reqStmt.execute();
        long requestID = Database.instance().getAutoGenKey(reqStmt);
        reqStmt.close();
        return requestID;
    }

    public static DataResults asyncDataResults(final WSAnalysisDefinition analysisDefinition, final InsightRequestMetadata insightRequestMetadata)  {
        try {
            long reqID;
            EIConnection conn = Database.instance().getConnection();
            try {
                reqID = createAsyncRequestID(analysisDefinition, insightRequestMetadata, conn, REPORT_EDITOR);
            } finally {
                Database.closeConnection(conn);
            }

            ResultData dataResults = null;
            while (dataResults == null) {
                dataResults = (ResultData) MemCachedManager.instance().get("async" + reqID);
                if (dataResults == null) {
                    Thread.sleep(100);
                }
            }
            dataResults.dataResults.setReport(dataResults.report);
            return dataResults.dataResults;
        } catch (Exception e) {
            LogClass.error(e);
            ListDataResults embeddedDataResults = new ListDataResults();
            embeddedDataResults.setReportFault(new ServerError("Something went wrong in running the report."));
            return embeddedDataResults;
        }
    }

    public static ResultData asyncDataSet(final WSAnalysisDefinition analysisDefinition, final InsightRequestMetadata insightRequestMetadata)  {
        try {
            long reqID;
            EIConnection conn = Database.instance().getConnection();
            try {
                reqID = createAsyncRequestID(analysisDefinition, insightRequestMetadata, conn, REPORT_DATA_SET);
            } finally {
                Database.closeConnection(conn);
            }

            ResultData dataResults = null;
            while (dataResults == null) {
                dataResults = (ResultData) MemCachedManager.instance().get("async" + reqID);
                if (dataResults == null) {
                    Thread.sleep(100);
                }
            }
            if (dataResults.exception != null) {
                throw dataResults.exception;
            }
            return dataResults;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public static ResultData asyncEndUserResults(final WSAnalysisDefinition analysisDefinition, final InsightRequestMetadata insightRequestMetadata)  {
        try {
            long reqID;
            EIConnection conn = Database.instance().getConnection();
            try {
                reqID = createAsyncRequestID(analysisDefinition, insightRequestMetadata, conn, REPORT_END_USER);
            } finally {
                Database.closeConnection(conn);
            }

            ResultData dataResults = null;
            while (dataResults == null) {
                dataResults = (ResultData) MemCachedManager.instance().get("async" + reqID);
                if (dataResults == null) {
                    Thread.sleep(100);
                }
            }
            return dataResults;
        } catch (Exception e) {
            LogClass.error(e);
            EmbeddedResults embeddedDataResults = new EmbeddedResults();
            embeddedDataResults.setReportFault(new ServerError("Something went wrong in running the report."));
            //return embeddedDataResults;
            throw new RuntimeException(e);
        }
    }
}
