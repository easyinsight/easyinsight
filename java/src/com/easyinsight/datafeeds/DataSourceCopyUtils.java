package com.easyinsight.datafeeds;

import com.easyinsight.storage.DataStorage;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.analysis.*;
import com.easyinsight.logging.LogClass;
import com.easyinsight.userupload.UploadPolicy;
import com.easyinsight.userupload.UserUploadInternalService;
import com.easyinsight.database.Database;
import com.easyinsight.api.APIService;
import com.easyinsight.api.dynamic.DynamicServiceDefinition;
import com.easyinsight.api.dynamic.ConfiguredMethod;
import com.easyinsight.solutions.SolutionInstallInfo;
import com.easyinsight.security.Roles;
import com.easyinsight.notifications.ConfigureDataFeedTodo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import org.hibernate.Session;

/**
 * User: James Boe
 * Date: May 27, 2009
 * Time: 4:25:09 PM
 */
public class DataSourceCopyUtils {

    public static List<SolutionInstallInfo> installFeed(long userID, Connection conn, boolean copyData, long feedID, FeedDefinition feedDefinition, boolean includeChildren, String newDataSourceName) throws CloneNotSupportedException, SQLException {
        FeedStorage feedStorage = new FeedStorage();
        AnalysisStorage analysisStorage = new AnalysisStorage();
        List<SolutionInstallInfo> infos = new ArrayList<SolutionInstallInfo>();
        FeedDefinition clonedFeedDefinition = cloneFeed(userID, conn, feedDefinition);
        if (newDataSourceName != null) {
            clonedFeedDefinition.setFeedName(newDataSourceName);
        }
        feedStorage.updateDataFeedConfiguration(clonedFeedDefinition, conn);
        buildClonedDataStores(copyData, feedDefinition, clonedFeedDefinition, conn);
        new UserUploadInternalService().createUserFeedLink(userID, clonedFeedDefinition.getDataFeedID(), Roles.OWNER, conn);
        if (includeChildren) {
            List<AnalysisDefinition> insights = getInsightsFromFeed(feedID, conn);
            for (AnalysisDefinition insight : insights) {
                if (insight.isRootDefinition()) {
                    continue;
                }
                AnalysisDefinition clonedInsight = insight.clone();
                clonedInsight.setAnalysisPolicy(AnalysisPolicy.PRIVATE);
                clonedInsight.setDataFeedID(clonedFeedDefinition.getDataFeedID());
                clonedInsight.setUserBindings(Arrays.asList(new UserToAnalysisBinding(userID, UserPermission.OWNER)));
                analysisStorage.saveAnalysis(clonedInsight, conn);
                infos.add(new SolutionInstallInfo(insight.getAnalysisID(), clonedInsight.getAnalysisID(), SolutionInstallInfo.INSIGHT, null, false));
                List<FeedDefinition> insightFeeds = getFeedsFromInsight(clonedInsight.getAnalysisID(), conn);
                for (FeedDefinition insightFeed : insightFeeds) {
                    infos.addAll(installFeed(userID, conn, copyData, insightFeed.getDataFeedID(), insightFeed, true, null));
                }
            }
        }

        ConfigureDataFeedTodo todo = null;        
        if (feedDefinition instanceof ServerDataSourceDefinition) {
            Session session = Database.instance().createSession(conn);

            todo = new ConfigureDataFeedTodo();
            todo.setFeedID(clonedFeedDefinition.getDataFeedID());
            todo.setUserID(userID);
            session.save(todo);
            session.flush();
        }
        infos.add(new SolutionInstallInfo(feedDefinition.getDataFeedID(), clonedFeedDefinition.getDataFeedID(), SolutionInstallInfo.DATA_SOURCE, todo, clonedFeedDefinition.getFeedName(), todo != null));


        
        return infos;
    }

    private static void buildClonedDataStores(boolean copyData, FeedDefinition feedDefinition, FeedDefinition clonedFeedDefinition, Connection conn) throws SQLException {
        if (copyData) {
            DataStorage sourceTable = DataStorage.writeConnection(feedDefinition, conn);
            DataSet dataSet;
            try {
                Set<AnalysisItem> validQueryItems = new HashSet<AnalysisItem>();
                for (AnalysisItem analysisItem : feedDefinition.getFields()) {
                    if (!analysisItem.isDerived()) {
                        validQueryItems.add(analysisItem);
                    }
                }
                dataSet = sourceTable.retrieveData(validQueryItems, null, null, null, null);
            } finally {
                sourceTable.closeConnection();
            }
            DataStorage clonedTable = DataStorage.writeConnection(clonedFeedDefinition, conn);
            try {
                clonedTable.createTable();
                clonedTable.insertData(dataSet);
                clonedTable.commit();
            } catch (SQLException e) {
                LogClass.error(e);
                clonedTable.rollback();
                throw new RuntimeException(e);
            } finally {
                clonedTable.closeConnection();
            }
        } else {
            DataStorage clonedTable = DataStorage.writeConnection(clonedFeedDefinition, conn);
            try {
                clonedTable.createTable();
                clonedTable.commit();
            } catch (SQLException e) {
                LogClass.error(e);
                clonedTable.rollback();
                throw new RuntimeException(e);
            } finally {
                clonedTable.closeConnection();
            }
        }
    }

    public static FeedDefinition cloneFeed(long userID, Connection conn, FeedDefinition feedDefinition) throws CloneNotSupportedException, SQLException {
        FeedStorage feedStorage = new FeedStorage();
        AnalysisStorage analysisStorage = new AnalysisStorage();
        FeedDefinition clonedFeedDefinition = feedDefinition.clone();
        clonedFeedDefinition.setUploadPolicy(new UploadPolicy(userID));
        feedStorage.addFeedDefinitionData(clonedFeedDefinition, conn);
        AnalysisDefinition clonedRootInsight = analysisStorage.cloneReport(feedDefinition.getAnalysisDefinitionID(), conn);
        clonedRootInsight.setUserBindings(Arrays.asList(new UserToAnalysisBinding(userID, UserPermission.OWNER)));
        analysisStorage.saveAnalysis(clonedRootInsight, conn);
        clonedFeedDefinition.setAnalysisDefinitionID(clonedRootInsight.getAnalysisID());
        if (clonedFeedDefinition.getDynamicServiceDefinitionID() > 0) {
            cloneAPIs(conn, feedDefinition, clonedFeedDefinition);
        }
        return clonedFeedDefinition;
    }

    private static void cloneAPIs(Connection conn, FeedDefinition feedDefinition, FeedDefinition clonedFeedDefinition) {
        Session session = Database.instance().createSession(conn);
        try {
            APIService apiService = new APIService();
            DynamicServiceDefinition dynamicServiceDefinition = apiService.getDynamicServiceDefinition(feedDefinition.getDataFeedID(), conn, session);
            List<ConfiguredMethod> clonedConfiguredMethods = new ArrayList<ConfiguredMethod>();
            for (ConfiguredMethod configuredMethod : dynamicServiceDefinition.getConfiguredMethods()) {
                List<AnalysisItem> clonedMethodItems = new ArrayList<AnalysisItem>();
                for (AnalysisItem keyItem : configuredMethod.getKeys()) {
                    // find that item in the cloned definition...
                    AnalysisItem matchedItem = null;
                    for (AnalysisItem clonedItem : clonedFeedDefinition.getFields()) {
                        if (clonedItem.equals(keyItem)) {
                            matchedItem = clonedItem;
                        }
                    }
                    clonedMethodItems.add(matchedItem);
                }
                ConfiguredMethod clonedMethod = configuredMethod.clone();
                clonedMethod.setKeys(clonedMethodItems);
                clonedConfiguredMethods.add(clonedMethod);
            }
            DynamicServiceDefinition clonedDefinition = dynamicServiceDefinition.clone();
            clonedDefinition.setConfiguredMethods(clonedConfiguredMethods);
            apiService.addDynamicServiceDefinition(clonedDefinition, conn);
            clonedFeedDefinition.setDynamicServiceDefinitionID(clonedDefinition.getServiceID());
            session.flush();
        } finally {
            session.close();
        }
    }

    private static List<AnalysisDefinition> getInsightsFromFeed(long feedID, Connection conn) throws SQLException {
        AnalysisStorage analysisStorage = new AnalysisStorage();
        List<AnalysisDefinition> analyses = new ArrayList<AnalysisDefinition>();
        PreparedStatement queryStmt = conn.prepareStatement("SELECT ANALYSIS_ID FROM ANALYSIS WHERE DATA_FEED_ID = ?");
        queryStmt.setLong(1, feedID);
        ResultSet rs = queryStmt.executeQuery();
        while (rs.next()) {
            analyses.add(AnalysisDefinitionFactory.fromWSDefinition(analysisStorage.getAnalysisDefinition(rs.getLong(1), conn)));
        }
        return analyses;
    }

    private static List<FeedDefinition> getFeedsFromInsight(long insightID, Connection conn) throws SQLException {
        FeedStorage feedStorage = new FeedStorage();
        List<FeedDefinition> feeds = new ArrayList<FeedDefinition>();
        PreparedStatement queryStmt = conn.prepareStatement("SELECT DATA_FEED_ID FROM ANALYSIS_BASED_FEED WHERE analysis_id = ?");
        queryStmt.setLong(1, insightID);
        ResultSet rs = queryStmt.executeQuery();
        while (rs.next()) {
            long feedID = rs.getLong(1);
            feeds.add(feedStorage.getFeedDefinitionData(feedID, conn));
        }
        return feeds;
    }
}
