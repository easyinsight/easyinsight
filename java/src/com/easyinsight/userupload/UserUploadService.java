package com.easyinsight.userupload;

import com.easyinsight.config.ConfigLoader;
import com.easyinsight.core.DataSourceDescriptor;
import com.easyinsight.core.EIDescriptor;
import com.easyinsight.dashboard.DashboardDescriptor;
import com.easyinsight.dashboard.DashboardService;
import com.easyinsight.dashboard.DashboardStorage;
import com.easyinsight.datafeeds.basecampnext.BasecampNextAccount;
import com.easyinsight.datafeeds.basecampnext.BasecampNextCompositeSource;
import com.easyinsight.datafeeds.file.FileBasedFeedDefinition;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.etl.LookupTableDescriptor;
import com.easyinsight.scorecard.DataSourceRefreshEvent;
import com.easyinsight.scorecard.ScorecardDescriptor;
import com.easyinsight.scorecard.ScorecardInternalService;
import com.easyinsight.scorecard.ScorecardService;
import com.easyinsight.storage.DataStorage;
import com.easyinsight.storage.StorageLimitException;
import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.*;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.core.InsightDescriptor;
import com.easyinsight.logging.LogClass;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.security.Roles;
import com.easyinsight.users.*;
import com.easyinsight.analysis.*;
import com.easyinsight.PasswordStorage;
import com.easyinsight.scheduler.*;
import com.easyinsight.solutions.SolutionInstallInfo;

import java.io.*;
import java.util.*;
import java.util.Date;
import java.sql.*;

import com.easyinsight.util.ServiceUtil;
import com.xerox.amazonws.sqs2.Message;
import com.xerox.amazonws.sqs2.MessageQueue;
import com.xerox.amazonws.sqs2.SQSUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;

/**
 * User: James Boe
 * Date: Jan 26, 2008
 * Time: 9:17:37 PM
 */
public class UserUploadService {


    private static FeedStorage feedStorage = new FeedStorage();
    private static Map<Long, RawUploadData> rawDataMap = new WeakHashMap<Long, RawUploadData>();
    private static final long TEN_MEGABYTES = 10485760;

    public UserUploadService() {
    }

    public void deleteReports(List<EIDescriptor> descriptors) {

        try {
            for (EIDescriptor descriptor : descriptors) {
                if (descriptor.getType() == EIDescriptor.REPORT) {
                    new AnalysisService().deleteAnalysisDefinition(descriptor.getId());
                } else if (descriptor.getType() == EIDescriptor.SCORECARD) {
                    new ScorecardService().deleteScorecard(descriptor.getId());
                } else if (descriptor.getType() == EIDescriptor.DASHBOARD) {
                    new DashboardService().deleteDashboard(descriptor.getId());
                } else if (descriptor.getType() == EIDescriptor.LOOKUP_TABLE) {
                    new FeedService().deleteLookupTable(descriptor.getId());
                }
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public long newFolder(String name, long dataSourceID) {
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement saveFolderStmt = conn.prepareStatement("INSERT INTO REPORT_FOLDER (ACCOUNT_ID, FOLDER_NAME, FOLDER_SEQUENCE, DATA_SOURCE_ID) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            saveFolderStmt.setLong(1, SecurityUtil.getAccountID());
            saveFolderStmt.setString(2, name);
            saveFolderStmt.setInt(3, 1);
            saveFolderStmt.setLong(4, dataSourceID);
            saveFolderStmt.execute();
            long id = Database.instance().getAutoGenKey(saveFolderStmt);
            saveFolderStmt.close();
            return id;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public void move(EIDescriptor descriptor, int targetFolder) {
        EIConnection conn = Database.instance().getConnection();
        try {
            if (descriptor.getType() == EIDescriptor.REPORT) {
                SecurityUtil.authorizeInsight(descriptor.getId());
                InsightDescriptor insightDescriptor = (InsightDescriptor) descriptor;
                new AnalysisStorage().clearCache(descriptor.getId(), insightDescriptor.getDataFeedID());
                PreparedStatement updateStmt = conn.prepareStatement("UPDATE ANALYSIS SET FOLDER = ? WHERE ANALYSIS_ID = ?");
                updateStmt.setInt(1, targetFolder);
                updateStmt.setLong(2, descriptor.getId());
                updateStmt.executeUpdate();
                updateStmt.close();
            } else if (descriptor.getType() == EIDescriptor.DASHBOARD) {
                SecurityUtil.authorizeDashboard(descriptor.getId());
                PreparedStatement updateStmt = conn.prepareStatement("UPDATE DASHBOARD SET FOLDER = ? WHERE DASHBOARD_ID = ?");
                updateStmt.setInt(1, targetFolder);
                updateStmt.setLong(2, descriptor.getId());
                updateStmt.executeUpdate();
                updateStmt.close();
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public List<SolutionInstallInfo> copyDataSource(long dataSourceID, String newName, boolean copyData, boolean includeChildren) {
        SecurityUtil.authorizeFeed(dataSourceID, Roles.OWNER);
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            FeedDefinition existingDef = feedStorage.getFeedDefinitionData(dataSourceID, conn);
            List<SolutionInstallInfo> results = DataSourceCopyUtils.installFeed(SecurityUtil.getUserID(), conn, copyData, dataSourceID, existingDef, newName, 0,
                    SecurityUtil.getAccountID(), SecurityUtil.getUserName());
            conn.commit();
            return results;
        } catch (Throwable e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
    }

    private boolean keep(EIDescriptor descriptor, boolean onlyMyData) {
        return !onlyMyData || descriptor.getRole() == Roles.OWNER;
    }

    private long getDataSourceID(EIDescriptor descriptor) {
        if (descriptor.getType() == EIDescriptor.DASHBOARD) {
            return ((DashboardDescriptor) descriptor).getDataSourceID();
        } else if (descriptor.getType() == EIDescriptor.REPORT) {
            return ((InsightDescriptor) descriptor).getDataFeedID();
        } else if (descriptor.getType() == EIDescriptor.SCORECARD) {
            return ((ScorecardDescriptor) descriptor).getDataSourceID();
        } else {
            throw new RuntimeException();
        }
    }



    public MyDataTree getFeedAnalysisTree(boolean onlyMyData) {
        return getFeedAnalysisTree(onlyMyData, 0);
    }

    public List<EIDescriptor> getFeedAnalysisTreeForDataSource(DataSourceDescriptor dataSourceDescriptor) {
        SecurityUtil.authorizeFeedAccess(dataSourceDescriptor.getId());
        long userID = SecurityUtil.getUserID();
        long accountID = SecurityUtil.getAccountID();
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            List<EIDescriptor> objects = new ArrayList<EIDescriptor>();
            List<EIDescriptor> results = new ArrayList<EIDescriptor>();

            AnalysisStorage analysisStorage = new AnalysisStorage();

            objects.addAll(new DashboardStorage().getDashboardsForDataSource(userID, accountID, conn, dataSourceDescriptor.getId()).values());
            objects.addAll(analysisStorage.getInsightDescriptorsForDataSource(userID, accountID, dataSourceDescriptor.getId(), conn));
            objects.addAll(new ScorecardInternalService().getScorecards(userID, accountID, conn).values());

            Iterator<EIDescriptor> iter = objects.iterator();
            while (iter.hasNext()) {
                EIDescriptor descriptor = iter.next();
                if (!keep(descriptor, false)) {
                    iter.remove();
                }
            }

            iter = objects.iterator();
            while (iter.hasNext()) {
                EIDescriptor descriptor = iter.next();
                long dataSourceID = getDataSourceID(descriptor);
                if (dataSourceID == dataSourceDescriptor.getId()) {
                    dataSourceDescriptor.getChildren().add(descriptor);
                }
                iter.remove();
            }

            for (LookupTableDescriptor lookupTableDescriptor : feedStorage.getLookupTableDescriptors(conn)) {
                if (lookupTableDescriptor.getDataSourceID() == dataSourceDescriptor.getId()) {
                    lookupTableDescriptor.setRole(dataSourceDescriptor.getRole());
                    dataSourceDescriptor.getChildren().add(lookupTableDescriptor);
                }
            }

            for (EIDescriptor descriptor : results) {
                if (descriptor.getType() == EIDescriptor.DATA_SOURCE) {
                    Collections.sort(dataSourceDescriptor.getChildren(), new Comparator<EIDescriptor>() {

                        public int compare(EIDescriptor eiDescriptor, EIDescriptor eiDescriptor1) {
                            String name1 = eiDescriptor.getName() != null ? eiDescriptor.getName() : "";
                            String name2 = eiDescriptor1.getName() != null ? eiDescriptor1.getName() : "";
                            return name1.compareTo(name2);
                        }
                    });
                }
            }

            Collections.sort(results, new Comparator<EIDescriptor>() {

                public int compare(EIDescriptor eiDescriptor, EIDescriptor eiDescriptor1) {
                    String name1 = eiDescriptor.getName() != null ? eiDescriptor.getName() : "";
                    String name2 = eiDescriptor1.getName() != null ? eiDescriptor1.getName() : "";
                    return name1.compareTo(name2);
                }
            });
            conn.commit();
            return dataSourceDescriptor.getChildren();
        } catch (Throwable e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(false);
            Database.closeConnection(conn);
        }
    }

    public void renameFolder(long folderID, String name) {
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement updateStmt = conn.prepareStatement("UPDATE REPORT_FOLDER SET FOLDER_NAME = ? WHERE REPORT_FOLDER_ID = ? AND ACCOUNT_ID = ?");
            updateStmt.setString(1, name);
            updateStmt.setLong(2, folderID);
            updateStmt.setLong(3, SecurityUtil.getAccountID());
            updateStmt.executeUpdate();
            updateStmt.close();
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public void deleteFolder(long folderID) {
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement updateStmt = conn.prepareStatement("UPDATE ANALYSIS SET FOLDER = ? WHERE FOLDER = ?");
            updateStmt.setLong(1, 1);
            updateStmt.setLong(2, folderID);
            updateStmt.executeUpdate();
            updateStmt.close();
            PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM REPORT_FOLDER WHERE REPORT_FOLDER_ID = ? AND ACCOUNT_ID = ?");
            deleteStmt.setLong(1, folderID);
            deleteStmt.setLong(2, SecurityUtil.getAccountID());
            deleteStmt.executeUpdate();
            deleteStmt.close();
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public List<CustomFolder> getCustomFolders(long dataSourceID) {
        List<CustomFolder> folders = new ArrayList<CustomFolder>();
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement getFoldersStmt = conn.prepareStatement("SELECT REPORT_FOLDER_ID, FOLDER_NAME, DATA_SOURCE_ID FROM REPORT_FOLDER WHERE ACCOUNT_ID = ? AND " +
                    "DATA_SOURCE_ID = ?");
            getFoldersStmt.setLong(1, SecurityUtil.getAccountID());
            getFoldersStmt.setLong(2, dataSourceID);
            ResultSet folderRS = getFoldersStmt.executeQuery();
            while (folderRS.next()) {
                long id = folderRS.getLong(1);
                String name = folderRS.getString(2);
                CustomFolder customFolder = new CustomFolder();
                customFolder.setName(name);
                customFolder.setId(id);
                folders.add(customFolder);
            }
            getFoldersStmt.close();
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
        return folders;
    }

    public MyDataTree getFeedAnalysisTree(boolean onlyMyData, long groupID) {
        onlyMyData = false;
        long userID = SecurityUtil.getUserID();
        long accountID = SecurityUtil.getAccountID();
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            List<EIDescriptor> objects = new ArrayList<EIDescriptor>();
            List<EIDescriptor> results = new ArrayList<EIDescriptor>();
            List<DataSourceDescriptor> dataSources;
            if (groupID == 0) {
                dataSources = feedStorage.getDataSources(userID, accountID, conn);
            } else {
                dataSources = feedStorage.getDataSourcesForGroup(userID, groupID, conn);
            }



            Iterator<DataSourceDescriptor> dataSourceIter = dataSources.iterator();
            while (dataSourceIter.hasNext()) {
                DataSourceDescriptor dataSource = dataSourceIter.next();
                if (!keep(dataSource, onlyMyData)) {
                    dataSourceIter.remove();
                }
            }

            AnalysisStorage analysisStorage = new AnalysisStorage();

            if (groupID == 0) {
                objects.addAll(new DashboardStorage().getDashboards(userID, accountID, conn).values());
                objects.addAll(analysisStorage.getReports(userID, accountID, conn).values());
                objects.addAll(new ScorecardInternalService().getScorecards(userID, accountID, conn).values());
            } else {
                objects.addAll(analysisStorage.getReportsForGroup(groupID, conn).values());
                objects.addAll(new DashboardStorage().getDashboardForGroup(groupID, conn).values());
                objects.addAll(new ScorecardInternalService().getScorecardsForGroup(groupID, conn).values());
            }

            Iterator<EIDescriptor> iter = objects.iterator();
            while (iter.hasNext()) {
                EIDescriptor descriptor = iter.next();
                if (!keep(descriptor, onlyMyData)) {
                    iter.remove();
                }
            }

            Map<Long, DataSourceDescriptor> descriptorMap = new HashMap<Long, DataSourceDescriptor>();
            for (DataSourceDescriptor dataSource : dataSources) {
                descriptorMap.put(dataSource.getId(), dataSource);
            }

            PreparedStatement getFoldersStmt = conn.prepareStatement("SELECT REPORT_FOLDER_ID, FOLDER_NAME, DATA_SOURCE_ID FROM REPORT_FOLDER WHERE ACCOUNT_ID = ?");
            getFoldersStmt.setLong(1, SecurityUtil.getAccountID());

            ResultSet folderRS = getFoldersStmt.executeQuery();
            while (folderRS.next()) {
                long id = folderRS.getLong(1);
                String name = folderRS.getString(2);
                long dataSourceID = folderRS.getLong(3);
                CustomFolder customFolder = new CustomFolder();
                customFolder.setName(name);
                customFolder.setId(id);
                DataSourceDescriptor dataSourceDescriptor = descriptorMap.get(dataSourceID);
                if (dataSourceDescriptor != null) {
                    dataSourceDescriptor.getCustomFolders().add(customFolder);
                }
            }
            getFoldersStmt.close();

            if (groupID != 0) {
                int role = SecurityUtil.authorizeGroup(groupID, Roles.SUBSCRIBER);
                for (EIDescriptor descriptor : objects) {
                    descriptor.setRole(role);
                }
            }

            PreparedStatement stmt = conn.prepareStatement("SELECT DATA_FEED.FEED_NAME, DATA_FEED.FEED_TYPE, FEED_PERSISTENCE_METADATA.LAST_DATA_TIME, refresh_behavior FROM DATA_FEED LEFT JOIN FEED_PERSISTENCE_METADATA ON DATA_FEED.DATA_FEED_ID = FEED_PERSISTENCE_METADATA.FEED_ID WHERE " +
                    "DATA_FEED_ID = ?");
            PreparedStatement findOwnerStmt = conn.prepareStatement("SELECT FIRST_NAME, NAME FROM USER, UPLOAD_POLICY_USERS WHERE UPLOAD_POLICY_USERS.USER_ID = USER.USER_ID AND " +
                "UPLOAD_POLICY_USERS.FEED_ID = ?");

            DataSourceDescriptor placeholder = new DataSourceDescriptor("Not Tied to a Data Source", 0, 0, false, 0);
            descriptorMap.put(0L, placeholder);
            iter = objects.iterator();
            while (iter.hasNext()) {
                EIDescriptor descriptor = iter.next();
                long dataSourceID = getDataSourceID(descriptor);
                DataSourceDescriptor dataSource = descriptorMap.get(dataSourceID);
                if (dataSource != null) {
                    dataSource.getChildren().add(descriptor);
                } else {
                    if (dataSourceID == 0) {
                        placeholder.getChildren().add(dataSource);
                    } else {
                        stmt.setLong(1, dataSourceID);
                        ResultSet dsRS = stmt.executeQuery();
                        if (dsRS.next()) {
                            findOwnerStmt.setLong(1, dataSourceID);
                            ResultSet ownerRS = findOwnerStmt.executeQuery();
                            String name;
                            if (ownerRS.next()) {
                                String firstName = ownerRS.getString(1);
                                String lastName = ownerRS.getString(2);
                                name = firstName != null ? firstName + " " + lastName : lastName;
                            } else {
                                name = "";
                            }
                            Timestamp lastTime = dsRS.getTimestamp(3);
                            Date lastDataTime = null;
                            if (lastTime != null) {
                                lastDataTime = new Date(lastTime.getTime());
                            }
                            DataSourceDescriptor stub = new DataSourceDescriptor(dsRS.getString(1), dataSourceID, dsRS.getInt(2), false, dsRS.getInt(4));
                            stub.setAuthor(name);
                            stub.setLastDataTime(lastDataTime);
                            dataSources.add(stub);
                            descriptorMap.put(dataSourceID, stub);
                            stub.getChildren().add(descriptor);
                        }
                    }
                }
                iter.remove();
            }

            if (!placeholder.getChildren().isEmpty()) {
                dataSources.add(placeholder);
            }

            //results.addAll(objects);
            PreparedStatement analystStmt = conn.prepareStatement("SELECT ANALYST FROM USER WHERE USER_ID = ?");
            analystStmt.setLong(1, SecurityUtil.getUserID());
            ResultSet analystRS = analystStmt.executeQuery();
            analystRS.next();
            boolean analyst = analystRS.getBoolean(1);
            analystStmt.close();
            if (groupID == 0 && analyst) {
                List<LookupTableDescriptor> lookupTables = feedStorage.getLookupTableDescriptors(conn);
                for (LookupTableDescriptor lookupTableDescriptor : lookupTables) {
                    DataSourceDescriptor feedDescriptor = descriptorMap.get(lookupTableDescriptor.getDataSourceID());
                    if (feedDescriptor != null) {
                        lookupTableDescriptor.setRole(feedDescriptor.getRole());
                        feedDescriptor.getChildren().add(lookupTableDescriptor);
                    }
                }
            }
            results.addAll(dataSources);

            if (groupID != 0) {
                int role = SecurityUtil.authorizeGroup(groupID, Roles.SUBSCRIBER);
                for (EIDescriptor descriptor : results) {
                    descriptor.setRole(role);
                }
            }

            for (EIDescriptor descriptor : results) {
                if (descriptor.getType() == EIDescriptor.DATA_SOURCE) {
                    DataSourceDescriptor dataSourceDescriptor = (DataSourceDescriptor) descriptor;
                    Collections.sort(dataSourceDescriptor.getChildren(), new Comparator<EIDescriptor>() {

                        public int compare(EIDescriptor eiDescriptor, EIDescriptor eiDescriptor1) {
                            String name1 = eiDescriptor.getName() != null ? eiDescriptor.getName() : "";
                            String name2 = eiDescriptor1.getName() != null ? eiDescriptor1.getName() : "";
                            return name1.compareToIgnoreCase(name2);
                        }
                    });
                }
            }

            Collections.sort(results, new Comparator<EIDescriptor>() {

                public int compare(EIDescriptor eiDescriptor, EIDescriptor eiDescriptor1) {
                    String name1 = eiDescriptor.getName() != null ? eiDescriptor.getName() : "";
                    String name2 = eiDescriptor1.getName() != null ? eiDescriptor1.getName() : "";
                    return name1.compareToIgnoreCase(name2);
                }
            });

            int dataSourceCount = 0;
            int reportCount = 0;
            int dashboardCount = 0;
            MyDataTree myDataTree = new MyDataTree(results, onlyMyData);
            myDataTree.setDashboardCount(dashboardCount);
            myDataTree.setDataSourceCount(dataSourceCount);
            myDataTree.setReportCount(reportCount);
            conn.commit();
            return myDataTree;
        } catch (Throwable e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(false);
            Database.closeConnection(conn);
        }
    }

    public FeedDefinition getDataFeedConfiguration(long dataFeedID) {
        SecurityUtil.authorizeFeed(dataFeedID, Roles.SUBSCRIBER);
        try {
            return getFeedDefinition(dataFeedID);
        } catch (Throwable e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public static FeedDefinition getFeedDefinition(long dataFeedID) {
        try {
            return feedStorage.getFeedDefinitionData(dataFeedID);
        } catch (Throwable e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public void updateFeedDefinition(FeedDefinition feedDefinition) {
        try {
            SecurityUtil.authorizeFeed(feedDefinition.getDataFeedID(), Roles.OWNER);
            feedStorage.updateDataFeedConfiguration(feedDefinition);
        } catch (Throwable e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public long addRawUploadData(long userID, String fileName, byte[] rawData) {

        // get size of data on that user
        Connection conn = Database.instance().getConnection();
        try {
            long uploadID;
            PreparedStatement anythingExistingStmt = conn.prepareStatement("SELECT USER_UPLOAD_ID FROM USER_UPLOAD WHERE " +
                    "ACCOUNT_ID = ? AND DATA_NAME = ?");
            anythingExistingStmt.setLong(1, userID);
            anythingExistingStmt.setString(2, fileName);
            ResultSet existingRS = anythingExistingStmt.executeQuery();
            if (existingRS.next()) {
                uploadID = existingRS.getLong(1);
                PreparedStatement updateStmt = conn.prepareStatement("UPDATE USER_UPLOAD SET USER_DATA = ? WHERE " +
                        "USER_UPLOAD_ID = ?");
                ByteArrayInputStream bais = new ByteArrayInputStream(rawData);
                updateStmt.setBinaryStream(1, bais, rawData.length);
                updateStmt.setLong(2, uploadID);
                updateStmt.executeUpdate();
            } else {
                PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO USER_UPLOAD (ACCOUNT_ID, DATA_NAME, " +
                        "USER_DATA) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                insertStmt.setLong(1, userID);
                insertStmt.setString(2, fileName);
                ByteArrayInputStream bais = new ByteArrayInputStream(rawData);
                insertStmt.setBinaryStream(3, bais, rawData.length);
                insertStmt.execute();
                uploadID = Database.instance().getAutoGenKey(insertStmt);
            }
            rawDataMap.put(uploadID, new RawUploadData(userID, fileName, rawData));
            return uploadID;
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public long createNewDefaultFeed(String name) {
        Connection conn = Database.instance().getConnection();
        DataStorage tableDef = null;
        try {
            conn.setAutoCommit(false);
            FeedDefinition feedDefinition = new FeedDefinition();
            feedDefinition.setFeedName(name);
            feedDefinition.setOwnerName(retrieveUser(conn).getUserName());
            UploadPolicy uploadPolicy = new UploadPolicy(SecurityUtil.getUserID(), SecurityUtil.getAccountID());
            feedDefinition.setUploadPolicy(uploadPolicy);
            feedDefinition.setFields(new ArrayList<AnalysisItem>());
            FeedCreationResult result = new FeedCreation().createFeed(feedDefinition, conn, new DataSet(), uploadPolicy);
            tableDef = result.getTableDefinitionMetadata();
            tableDef.commit();
            conn.commit();
            return result.getFeedID();
        } catch (Throwable e) {
            LogClass.error(e);
            if (tableDef != null) {
                tableDef.rollback();
            }
            try {
                conn.rollback();
            } catch (SQLException e1) {
                LogClass.error(e1);
            }
            throw new RuntimeException(e);
        } finally {
            if (tableDef != null) {
                tableDef.closeConnection();
            }
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LogClass.error(e);
            }
            Database.closeConnection(conn);
        }
    }

    // three contexts here
    // excel/csv upload
    // google documents
    // unchecked API

    public UploadResponse analyzeUpload(UploadContext uploadContext) {
        UploadResponse uploadResponse;
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);

            String validation = uploadContext.validateUpload(conn);

            if (validation != null) {
                uploadResponse = new UploadResponse(validation);
            } else {
                List<AnalysisItem> fields = uploadContext.guessFields(conn);
                List<FieldUploadInfo> fieldInfos = new ArrayList<FieldUploadInfo>();
                for (AnalysisItem field : fields) {
                    FieldUploadInfo fieldUploadInfo = new FieldUploadInfo();
                    fieldUploadInfo.setGuessedItem(field);
                    fieldUploadInfo.setSampleValues(uploadContext.getSampleValues(field.getKey()));
                    fieldInfos.add(fieldUploadInfo);
                }
                uploadResponse = new UploadResponse();
                uploadResponse.setSuccessful(true);
                uploadResponse.setInfos(fieldInfos);
            }
            conn.commit();
        } catch (Throwable e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
        return uploadResponse;
    }

    public UploadResponse createDataSource(String name, UploadContext uploadContext, List<AnalysisItem> analysisItems, boolean accountVisible) {
        UploadResponse uploadResponse;
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            long dataSourceID = uploadContext.createDataSource(name, analysisItems, conn, accountVisible);
            uploadResponse = new UploadResponse(dataSourceID);
            conn.commit();
            return uploadResponse;
        } catch (ReportException re) {
            if (re.getReportFault() instanceof StorageLimitFault) {
                conn.rollback();
                uploadResponse = new UploadResponse("You have reached your account storage limit. You need to reduce the size of the data, clean up other data sources on the account, or upgrade to a higher account tier.");
            } else {
                conn.rollback();
                uploadResponse = new UploadResponse(re.getMessage());
            }
        } catch (StorageLimitException se) {
            conn.rollback();
            uploadResponse = new UploadResponse("You have reached your account storage limit.");
        } catch (Throwable e) {
            LogClass.error(e);
            conn.rollback();
            uploadResponse = new UploadResponse("Something caused an internal error in the processing of the uploaded file.");
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
        return uploadResponse;
    }

    public void defineAccountReports(List<EIDescriptor> descriptors) {
        SecurityUtil.authorizeAccountAdmin();
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement clearStmt = conn.prepareStatement("DELETE FROM ACCOUNT_TO_REPORT WHERE ACCOUNT_ID = ?");
            clearStmt.setLong(1, SecurityUtil.getAccountID());
            clearStmt.executeUpdate();
            clearStmt.close();
            PreparedStatement clearDashboardStmt = conn.prepareStatement("DELETE FROM ACCOUNT_TO_DASHBOARD WHERE ACCOUNT_ID = ?");
            clearDashboardStmt.setLong(1, SecurityUtil.getAccountID());
            clearDashboardStmt.executeUpdate();
            clearDashboardStmt.close();
            PreparedStatement saveReportStmt = conn.prepareStatement("INSERT INTO ACCOUNT_TO_REPORT (ACCOUNT_ID, REPORT_ID) VALUES (?, ?)");
            PreparedStatement saveDashboardStmt = conn.prepareStatement("INSERT INTO ACCOUNT_TO_DASHBOARD (ACCOUNT_ID, DASHBOARD_ID) VALUES (?, ?)");
            for (EIDescriptor descriptor : descriptors) {
                if (descriptor.getType() == EIDescriptor.REPORT) {
                    saveReportStmt.setLong(1, SecurityUtil.getAccountID());
                    saveReportStmt.setLong(2, descriptor.getId());
                    saveReportStmt.execute();
                } else if (descriptor.getType() == EIDescriptor.DASHBOARD) {
                    saveDashboardStmt.setLong(1, SecurityUtil.getAccountID());
                    saveDashboardStmt.setLong(2, descriptor.getId());
                    saveDashboardStmt.execute();
                }
            }
            saveReportStmt.close();
            saveDashboardStmt.close();
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public List<EIDescriptor> getAccountReports() {
        EIConnection conn = Database.instance().getConnection();
        try {
            List<EIDescriptor> reports = new ArrayList<EIDescriptor>();
            PreparedStatement getReportStmt = conn.prepareStatement("SELECT ANALYSIS.TITLE, ANALYSIS.ANALYSIS_ID, ANALYSIS.DATA_FEED_ID, ANALYSIS.REPORT_TYPE," +
                    "ANALYSIS.URL_KEY, ANALYSIS.DESCRIPTION FROM " +
                    "ANALYSIS, ACCOUNT_TO_REPORT WHERE ACCOUNT_TO_REPORT.ACCOUNT_ID = ? AND ACCOUNT_TO_REPORT.REPORT_ID = ANALYSIS.ANALYSIS_ID");
            getReportStmt.setLong(1, SecurityUtil.getAccountID());
            ResultSet reportRS = getReportStmt.executeQuery();
            while (reportRS.next()) {
                String title = reportRS.getString(1);
                long reportID = reportRS.getLong(2);
                long dataSourceID = reportRS.getLong(3);
                int reportType = reportRS.getInt(4);
                String urlKey = reportRS.getString(5);
                InsightDescriptor id = new InsightDescriptor(reportID, title, dataSourceID, reportType, urlKey, Roles.OWNER, true);
                id.setDescription(reportRS.getString(6));
                reports.add(id);
            }
            getReportStmt.close();
            PreparedStatement getDashboardStmt = conn.prepareStatement("SELECT DASHBOARD.DASHBOARD_NAME, DASHBOARD.DASHBOARD_ID, DASHBOARD.DATA_SOURCE_ID, " +
                    "DASHBOARD.URL_KEY, DASHBOARD.DESCRIPTION FROM " +
                    "DASHBOARD, ACCOUNT_TO_DASHBOARD WHERE ACCOUNT_TO_DASHBOARD.ACCOUNT_ID = ? AND ACCOUNT_TO_DASHBOARD.DASHBOARD_ID = DASHBOARD.DASHBOARD_ID");
            getDashboardStmt.setLong(1, SecurityUtil.getAccountID());
            ResultSet dashboardRS = getDashboardStmt.executeQuery();
            while (dashboardRS.next()) {
                String title = dashboardRS.getString(1);
                long reportID = dashboardRS.getLong(2);
                long dataSourceID = dashboardRS.getLong(3);
                String urlKey = dashboardRS.getString(4);
                DashboardDescriptor dd = new DashboardDescriptor(title, reportID, urlKey, dataSourceID, Roles.OWNER, "", true);
                dd.setDescription(dashboardRS.getString(5));
                reports.add(dd);
            }
            getDashboardStmt.close();
            return reports;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public static RawUploadData retrieveRawData(long uploadID) {
        Connection conn = Database.instance().getConnection();
        RawUploadData result = null;
        try {
            conn.setAutoCommit(false);
            result = retrieveRawData(uploadID, conn);
        }
        catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
        finally {
            Database.closeConnection(conn);
        }
        return result;
    }

    public static RawUploadData retrieveRawData(long uploadID, Connection conn) throws SQLException {
        RawUploadData rawUploadData = rawDataMap.get(uploadID);
        if (rawUploadData == null) {
                PreparedStatement rawDataStmt = conn.prepareStatement("SELECT ACCOUNT_ID, DATA_NAME, USER_DATA FROM " +
                        "USER_UPLOAD WHERE USER_UPLOAD_ID = ?");
                rawDataStmt.setLong(1, uploadID);
                ResultSet dataRS = rawDataStmt.executeQuery();
                if (dataRS.next()) {
                    long accountID = dataRS.getLong(1);
                    String dataName = dataRS.getString(2);
                    byte[] userData = dataRS.getBytes(3);
                    rawUploadData = new RawUploadData(accountID, dataName, userData);
                } else {
                    throw new RuntimeException("Couldn't find upload info");
                }

        }
        return rawUploadData;
    }

    public static class RawUploadData {

        private RawUploadData(long accountID, String dataName, byte[] userData) {
            this.accountID = accountID;
            this.dataName = dataName;
            this.userData = userData;
        }

        public byte[] getUserData() {
            return userData;
        }

        public void setUserData(byte[] userData) {
            this.userData = userData;
        }

        public String getDataName() {
            return dataName;
        }

        public void setDataName(String dataName) {
            this.dataName = dataName;
        }

        public long getAccountID() {
            return accountID;
        }

        public void setAccountID(long accountID) {
            this.accountID = accountID;
        }

        long accountID;
        String dataName;
        byte[] userData;
    }

    public void deleteUserUpload(long dataFeedID) {
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            deleteUserUpload(dataFeedID, conn);
            conn.commit();
        } catch (Throwable e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
    }

    private void deleteUserUpload(long dataFeedID, EIConnection conn) throws SQLException {
        int role = SecurityUtil.getUserRoleToFeed(dataFeedID);
        if (role <= Roles.SHARER) {
            FeedDefinition feedDefinition = feedStorage.getFeedDefinitionData(dataFeedID, conn);
            feedDefinition.setVisible(false);
            PreparedStatement updateReportStmt = conn.prepareStatement("UPDATE ANALYSIS SET TEMPORARY_REPORT = ? WHERE DATA_FEED_ID = ?");
            try {
                feedDefinition.delete(conn);
            } catch (HibernateException e) {
                LogClass.error(e);
                PreparedStatement manualDeleteStmt = conn.prepareStatement("DELETE FROM DATA_FEED WHERE DATA_FEED_ID = ?");
                manualDeleteStmt.setLong(1, dataFeedID);
                manualDeleteStmt.executeUpdate();
            }
        } else if (role == Roles.SUBSCRIBER) {
        } else {
            throw new SecurityException();
        }
    }

    public long createAnalysisBasedFeed(AnalysisBasedFeedDefinition definition) {
        long userID = SecurityUtil.getUserID();
        SecurityUtil.authorizeInsight(definition.getReportID());
        try {
            long feedID = feedStorage.addFeedDefinitionData(definition);
            new UserUploadInternalService().createUserFeedLink(userID, feedID, Roles.OWNER);
            return feedID;
        } catch (Throwable e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public void subscribe(long dataFeedID) {
        SecurityUtil.authorizeFeedAccess(dataFeedID);
        long userID = SecurityUtil.getUserID();
        try {
            new UserUploadInternalService().createUserFeedLink(userID, dataFeedID, Roles.SUBSCRIBER);
        } catch (Throwable e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public int getRole(long userID, long feedID) {
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement existingLinkQuery = conn.prepareStatement("SELECT USER_ROLE FROM USER_TO_FEED WHERE " +
                    "USER_ID = ? AND DATA_FEED_ID = ?");
            existingLinkQuery.setLong(1, userID);
            existingLinkQuery.setLong(2, feedID);
            ResultSet rs = existingLinkQuery.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new RuntimeException();
            }
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    private void deleteUserFeedLink(long accountID, long feedID, Connection conn) throws SQLException {
        PreparedStatement existingLinkQuery = conn.prepareStatement("DELETE FROM UPLOAD_POLICY_USERS WHERE " +
                "USER_ID = ? AND FEED_ID = ?");
        existingLinkQuery.setLong(1, accountID);
        existingLinkQuery.setLong(2, feedID);
        existingLinkQuery.executeUpdate();
    }

    public CredentialsResponse refreshData(long feedID) {
        SecurityUtil.authorizeFeed(feedID, Roles.SUBSCRIBER);
        try {
            CredentialsResponse credentialsResponse;
            //final IServerDataSourceDefinition dataSource = (IServerDataSourceDefinition)
            /*if (SecurityUtil.getAccountTier() < dataSource.getRequiredAccountTier()) {
                return new CredentialsResponse(false, "Your account level is no longer valid for this data source connection.", feedID);
            }*/
            final FeedDefinition feedDefinition = feedStorage.getFeedDefinitionData(feedID);
            if ((feedDefinition.getDataSourceType() != DataSourceInfo.LIVE)) {
                if (DataSourceMutex.mutex().lock(feedDefinition.getDataFeedID())) {
                    final String callID = ServiceUtil.instance().longRunningCall(feedDefinition.getDataFeedID());
                    credentialsResponse = new CredentialsResponse(true, feedDefinition.getDataFeedID());
                    credentialsResponse.setCallDataID(callID);
                    final String userName = SecurityUtil.getUserName();
                    final long userID = SecurityUtil.getUserID();
                    final long accountID = SecurityUtil.getAccountID();
                    final int accountType = SecurityUtil.getAccountTier();
                    final boolean accountAdmin = SecurityUtil.isAccountAdmin();
                    final int firstDayOfWeek = SecurityUtil.getFirstDayOfWeek();
                    final String personaName = SecurityUtil.getPersonaName();
                    new Thread(new Runnable() {

                        public void run() {
                            SecurityUtil.populateThreadLocal(userName, userID, accountID, accountType, accountAdmin, firstDayOfWeek, personaName);
                            EIConnection conn = Database.instance().getConnection();
                            try {
                                List<ReportFault> warnings = new ArrayList<ReportFault>();
                                conn.setAutoCommit(false);
                                Date now = new Date();
                                List<FeedDefinition> sourcesToRefresh = new ArrayList<FeedDefinition>();
                                if (feedDefinition.getFeedType().getType() == FeedType.COMPOSITE.getType()) {
                                    CompositeFeedDefinition compositeFeedDefinition = (CompositeFeedDefinition) feedDefinition;
                                    for (CompositeFeedNode node : compositeFeedDefinition.getCompositeFeedNodes()) {
                                        FeedDefinition child = feedStorage.getFeedDefinitionData(node.getDataFeedID(), conn);
                                        sourcesToRefresh.add(child);
                                    }
                                } else {
                                    sourcesToRefresh.add(feedDefinition);
                                }

                                boolean changed = false;
                                for (FeedDefinition sourceToRefresh : sourcesToRefresh) {
                                    if (sourceToRefresh instanceof IServerDataSourceDefinition && (sourceToRefresh.getDataSourceType() == DataSourceInfo.STORED_PULL ||
                                            sourceToRefresh.getDataSourceType() == DataSourceInfo.COMPOSITE_PULL)) {
                                        IServerDataSourceDefinition refreshable = (IServerDataSourceDefinition) sourceToRefresh;
                                        DataSourceRefreshEvent info = new DataSourceRefreshEvent();
                                        info.setDataSourceName("Synchronizing with " + refreshable.getFeedName());
                                        ServiceUtil.instance().updateStatus(callID, ServiceUtil.RUNNING, info);
                                        changed = changed || new DataSourceFactory().createSource(conn, warnings, now, sourceToRefresh, refreshable, callID).invoke();
                                        PreparedStatement stmt = conn.prepareStatement("INSERT INTO DATA_SOURCE_REFRESH_LOG (REFRESH_TIME, DATA_SOURCE_ID) VALUES (?, ?)");
                                        stmt.setTimestamp(1, new Timestamp(now.getTime()));
                                        stmt.setLong(2, sourceToRefresh.getDataFeedID());
                                        stmt.execute();
                                        stmt.close();
                                    }
                                }

                                ReportFault warning = null;
                                if (!warnings.isEmpty()) {
                                    warning = warnings.get(warnings.size() - 1);
                                }
                                DataSourceRefreshResult result = new DataSourceRefreshResult();
                                result.setDate(now);
                                result.setWarning(warning);
                                result.setNewFields(sourcesToRefresh.size() == 1 && sourcesToRefresh.get(0).rebuildFieldWindow() && changed);
                                ServiceUtil.instance().updateStatus(callID, ServiceUtil.DONE, result);

                                conn.commit();
                            } catch (ReportException re) {
                                if (!conn.getAutoCommit()) {
                                    conn.rollback();
                                }
                                ServiceUtil.instance().updateStatus(callID, ServiceUtil.FAILED, re.getReportFault());
                            } catch (Exception e) {
                                LogClass.error(e);
                                if (!conn.getAutoCommit()) {
                                    conn.rollback();
                                }
                                ServiceUtil.instance().updateStatus(callID, ServiceUtil.FAILED, e.getMessage());
                            } finally {
                                conn.setAutoCommit(true);
                                Database.closeConnection(conn);
                                DataSourceMutex.mutex().unlock(feedDefinition.getDataFeedID());
                                SecurityUtil.clearThreadLocal();
                            }
                        }
                    }).start();
                } else {
                    credentialsResponse = new CredentialsResponse(true, feedDefinition.getDataFeedID());
                }
            } else {
                feedDefinition.setVisible(true);
                feedStorage.updateDataFeedConfiguration(feedDefinition);
                credentialsResponse = new CredentialsResponse(true, feedID);
            }
            return credentialsResponse;
        } catch (Throwable e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public Collection<FieldUploadInfo> analyzeUpdate(long feedID, byte[] bytes) {
        SecurityUtil.authorizeFeed(feedID, Roles.SUBSCRIBER);
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            FileBasedFeedDefinition dataSource = (FileBasedFeedDefinition) feedStorage.getFeedDefinitionData(feedID, conn);
            UserUploadAnalysis analysis = dataSource.getUploadFormat().analyze(bytes);
            Map<String, AnalysisItem> fieldMap = new HashMap<String, AnalysisItem>();
            for (AnalysisItem field : dataSource.getFields()) {
                fieldMap.put(field.getKey().toBaseKey().toKeyString(), field);
            }
            Collection<FieldUploadInfo> fieldInfos = new ArrayList<FieldUploadInfo>();
            for (AnalysisItem field : analysis.getFields()) {
                if (!fieldMap.containsKey(field.getKey().toBaseKey().toKeyString())) {
                    FieldUploadInfo fieldUploadInfo = new FieldUploadInfo();
                    fieldUploadInfo.setGuessedItem(field);
                    fieldUploadInfo.setSampleValues(new ArrayList<String>(analysis.getSampleMap().get(field.getKey())));
                    fieldInfos.add(fieldUploadInfo);
                }
            }
            conn.commit();
            return fieldInfos;
        } catch (Throwable e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
    }

    public void updateData(long feedID, byte[] bytes, boolean update, List<AnalysisItem> newFields) {
        SecurityUtil.authorizeFeed(feedID, Roles.SUBSCRIBER);
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            FileProcessUpdateScheduledTask task = new FileProcessUpdateScheduledTask();
            task.setFeedID(feedID);
            task.setNewFields(newFields);
            task.setUpdate(update);
            task.setUserID(SecurityUtil.getUserID());
            task.setAccountID(SecurityUtil.getAccountID());
            task.updateData(feedID, update, conn, bytes);
            conn.commit();
        } catch (Throwable e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
    }

    public void updateData(long feedID, byte[] bytes, boolean update) {
        updateData(feedID, bytes, update, null);
    }

    public long uploadPNG(byte[] bytes) {
        try {
            Long userID = SecurityUtil.getUserID(false);
            if (userID == 0) {
                userID = null;
            }
            return new PNGExportOperation().write(bytes, userID);
        } catch (Throwable e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public CredentialsResponse validateCredentials(FeedDefinition feedDefinition) {
        try {
            String failureMessage = feedDefinition.validateCredentials();
            CredentialsResponse credentialsResponse;
            if (failureMessage == null) {
                credentialsResponse = new CredentialsResponse(true, feedDefinition.getDataFeedID());
            } else {
                credentialsResponse = new CredentialsResponse(false, failureMessage, feedDefinition.getDataFeedID());
            }
            return credentialsResponse;
        } catch (ReportException re) {
            return new CredentialsResponse(false, re.getReportFault());
        } catch (Throwable e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }


    public long newExternalDataSource(FeedDefinition feedDefinition) {
        if (SecurityUtil.getAccountTier() < feedDefinition.getRequiredAccountTier()) {
            throw new RuntimeException("You are not allowed to create data sources of this type with your account.");
        }
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            IServerDataSourceDefinition serverDataSourceDefinition = (IServerDataSourceDefinition) feedDefinition;
            long id = serverDataSourceDefinition.create(conn, null, null);
            conn.commit();
            return id;
        } catch (Throwable e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
    }
    public static User retrieveUser(Connection conn) {
        long userID = SecurityUtil.getUserID();
        return retrieveUser(conn, userID);
    }

    public static User retrieveUser(Connection conn, long userID) {
        try {
            User user = null;
            Session session = Database.instance().createSession(conn);
            List results;
            try {
                session.beginTransaction();
                results = session.createQuery("from User where userID = ?").setLong(0, userID).list();
                session.getTransaction().commit();
            } catch (Throwable e) {
                session.getTransaction().rollback();
                throw new RuntimeException(e);
            } finally {
                session.close();
            }
            if (results.size() > 0) {
                user = (User) results.get(0);
            }
            return user;
        } catch (Throwable e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public Credentials encryptCredentials(Credentials creds) {
        Credentials c = new Credentials();
        c.setUserName(PasswordStorage.encryptString(creds.getUserName() + ":" + SecurityUtil.getUserName()));
        c.setPassword(PasswordStorage.encryptString(creds.getPassword() + ":" + SecurityUtil.getUserName()));
        c.setEncrypted(true);
        return c;
    }

    private Credentials decryptCredentials(Credentials creds) throws MalformedCredentialsException {
        Credentials c = new Credentials();
        String s = PasswordStorage.decryptString(creds.getUserName());
        int i = s.lastIndexOf(":" + SecurityUtil.getUserName());
        if(i == -1) {
            throw new MalformedCredentialsException();
        }
        c.setUserName(s.substring(0, i));
        s = PasswordStorage.decryptString(creds.getPassword());
        i = s.lastIndexOf(":" + SecurityUtil.getUserName());
        if(i == -1)
            throw new MalformedCredentialsException();
        c.setPassword(s.substring(0, i));
        return c;
    }

    public Collection<BasecampNextAccount> getBasecampAccounts(BasecampNextCompositeSource dataSource) {
        try {
            return dataSource.getBasecampAccounts();
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public CredentialsResponse completeInstallation(final FeedDefinition dataSource) {
        SecurityUtil.authorizeFeed(dataSource.getDataFeedID(), Roles.OWNER);
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            final IServerDataSourceDefinition serverDataSourceDefinition = (IServerDataSourceDefinition) dataSource;
            serverDataSourceDefinition.create(conn, null, null);
            CredentialsResponse credentialsResponse = null;
            if (SecurityUtil.getAccountTier() < dataSource.getRequiredAccountTier()) {
                return new CredentialsResponse(false, "Your account level is no longer valid for this data source connection.", dataSource.getDataFeedID());
            }
            if ((dataSource.getDataSourceType() != DataSourceInfo.LIVE)) {
                if (DataSourceMutex.mutex().lock(dataSource.getDataFeedID())) {

                } else {
                    credentialsResponse = new CredentialsResponse(true, dataSource.getDataFeedID());
                }
            } else {
                dataSource.setVisible(true);
                feedStorage.updateDataFeedConfiguration(dataSource, conn);
                credentialsResponse = new CredentialsResponse(true, dataSource.getDataFeedID());
            }
            conn.commit();
            if (credentialsResponse == null) {
                final String callID = ServiceUtil.instance().longRunningCall(dataSource.getDataFeedID());
                credentialsResponse = new CredentialsResponse(true, dataSource.getDataFeedID());
                credentialsResponse.setCallDataID(callID);
                final String userName = SecurityUtil.getUserName();
                final long userID = SecurityUtil.getUserID();
                final long accountID = SecurityUtil.getAccountID();
                final int accountType = SecurityUtil.getAccountTier();
                final boolean accountAdmin = SecurityUtil.isAccountAdmin();
                final int firstDayOfWeek = SecurityUtil.getFirstDayOfWeek();
                final String personaName = SecurityUtil.getPersonaName();
                new Thread(new Runnable() {

                    public void run() {
                        SecurityUtil.populateThreadLocal(userName, userID, accountID, accountType, accountAdmin, firstDayOfWeek, personaName);
                        EIConnection conn = Database.instance().getConnection();
                        try {
                            conn.setAutoCommit(false);
                            Date now = new Date();
                            boolean changed = new DataSourceFactory().createSource(conn, new ArrayList<ReportFault>(), now, dataSource, serverDataSourceDefinition, callID).invoke();
                            conn.commit();
                            DataSourceRefreshResult result = new DataSourceRefreshResult();
                            result.setDate(now);
                            result.setNewFields(changed && dataSource.rebuildFieldWindow());
                            ServiceUtil.instance().updateStatus(callID, ServiceUtil.DONE, result);
                        } catch (ReportException re) {
                            if (!conn.getAutoCommit()) {
                                conn.rollback();
                            }
                            ServiceUtil.instance().updateStatus(callID, ServiceUtil.FAILED, re.getReportFault());
                        } catch (Exception e) {
                            LogClass.error(e);
                            if (!conn.getAutoCommit()) {
                                conn.rollback();
                            }
                            ServiceUtil.instance().updateStatus(callID, ServiceUtil.FAILED, e.getMessage());
                        } finally {
                            conn.setAutoCommit(true);
                            Database.closeConnection(conn);
                            DataSourceMutex.mutex().unlock(dataSource.getDataFeedID());
                            SecurityUtil.clearThreadLocal();
                        }
                    }
                }).start();
            }
            return credentialsResponse;
        } catch (ReportException re) {
            return new CredentialsResponse(false, re.getReportFault().toString(), dataSource.getDataFeedID());
        } catch (Exception e) {
            LogClass.error(e);
            if (!conn.getAutoCommit()) {
                conn.rollback();
            }
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
    }

    public boolean flatFileUpload(long dataSourceID) {
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT ENDPOINT FROM FILE_BASED_DATA_SOURCE WHERE DATA_SOURCE_ID = ?");
            queryStmt.setLong(1, dataSourceID);
            ResultSet rs = queryStmt.executeQuery();
            if (rs.next()) {
                String endpoint = rs.getString(1);
                if (endpoint == null || "".equals(endpoint.trim())) {
                    return true;
                }
            } else {
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public static class DataSourceFactory {
        public IUploadDataSource createSource(EIConnection conn, List<ReportFault> warnings, Date now, FeedDefinition sourceToRefresh, IServerDataSourceDefinition refreshable, String callID) {
            if (sourceToRefresh.getFeedType().getType() == FeedType.SERVER_MYSQL.getType() ||
                    sourceToRefresh.getFeedType().getType() == FeedType.SERVER_SQL_SERVER.getType() ||
                    sourceToRefresh.getFeedType().getType() == FeedType.ORACLE.getType()) {
                return new SQSUploadDataSource(sourceToRefresh.getDataFeedID(), sourceToRefresh);
            } else {
                return new UploadDataSource(conn, warnings, now, sourceToRefresh, refreshable, callID);
            }
        }
    }

    public static interface IUploadDataSource {
        public boolean invoke() throws Exception;
    }

    public static class SQSUploadDataSource implements IUploadDataSource {

        private long dataSourceID;
        private FeedDefinition dataSource;

        private SQSUploadDataSource(long dataSourceID, FeedDefinition dataSource) {
            this.dataSourceID = dataSourceID;
            this.dataSource = dataSource;
        }

        public boolean invoke() throws Exception {
            MessageQueue msgQueue = SQSUtils.connectToQueue(ConfigLoader.instance().getDatabaseRequestQueue(), "0AWCBQ78TJR8QCY8ABG2", "bTUPJqHHeC15+g59BQP8ackadCZj/TsSucNwPwuI");
            MessageQueue responseQueue = SQSUtils.connectToQueue(ConfigLoader.instance().getDatabaseResponseQueue(), "0AWCBQ78TJR8QCY8ABG2", "bTUPJqHHeC15+g59BQP8ackadCZj/TsSucNwPwuI");
            msgQueue.sendMessage(dataSourceID + "|" + System.currentTimeMillis());
            boolean responded = false;
            boolean changed = false;
            int i = 0;
            while (!responded && i < 60) {
                Message message = responseQueue.receiveMessage();
                if (message == null) {
                    i++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        // ignore
                    }
                } else {
                    String body = message.getMessageBody();
                    String[] parts = body.split("\\|");
                    long sourceID = Long.parseLong(parts[0]);
                    System.out.println("got response with id = " + sourceID);
                    if (sourceID == dataSourceID) {
                        responseQueue.deleteMessage(message);
                        boolean successful = Boolean.parseBoolean(parts[1]);
                        if (successful) {
                            changed = Boolean.parseBoolean(parts[2]);
                            System.out.println("matched!");
                            responded = true;
                        } else {
                            String error = parts[2];
                            throw new ReportException(new DataSourceConnectivityReportFault(error, dataSource));
                        }
                    }
                }
            }
            return changed;
        }
    }

    public static class UploadDataSource implements IUploadDataSource {
        private EIConnection conn;
        private List<ReportFault> warnings;
        private Date now;
        private FeedDefinition sourceToRefresh;
        private IServerDataSourceDefinition refreshable;
        private String callID;

        public UploadDataSource(EIConnection conn, List<ReportFault> warnings, Date now, FeedDefinition sourceToRefresh, IServerDataSourceDefinition refreshable, String callID) {
            this.conn = conn;
            this.warnings = warnings;
            this.now = now;
            this.sourceToRefresh = sourceToRefresh;
            this.refreshable = refreshable;
            this.callID = callID;
        }

        public boolean invoke() throws Exception {
            boolean changed = refreshable.refreshData(SecurityUtil.getAccountID(), new Date(), conn, null, callID, sourceToRefresh.getLastRefreshStart(), false, warnings);
            sourceToRefresh.setVisible(true);
            sourceToRefresh.setLastRefreshStart(now);
            if (changed) {
                new DataSourceInternalService().updateFeedDefinition(sourceToRefresh, conn, true, true);
            } else {
                feedStorage.updateDataFeedConfiguration(sourceToRefresh, conn);
            }
            return changed;
        }
    }
}
