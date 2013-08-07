package com.easyinsight.datafeeds;

import com.easyinsight.analysis.ReplacementMap;
import com.easyinsight.core.DataSourceDescriptor;
import com.easyinsight.core.Key;
import com.easyinsight.core.DerivedKey;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.DataSourceInfo;
import com.easyinsight.database.EIConnection;
import com.easyinsight.logging.LogClass;
import com.easyinsight.database.Database;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.solutions.SolutionInstallInfo;

import java.sql.*;
import java.util.*;

import org.hibernate.Session;

/**
 * User: James Boe
 * Date: May 31, 2008
 * Time: 11:55:05 PM
 */
public class CompositeFeedDefinition extends FeedDefinition {
    // if (([Todo Completed At] > 0 && [Todo Completed At] <= [Todo Due At]) || nowdate() <= [Todo Due At], "On Time", "Late")
    private List<CompositeFeedNode> compositeFeedNodes = new ArrayList<CompositeFeedNode>();
    private List<CompositeFeedConnection> connections = new ArrayList<CompositeFeedConnection>();

    private Map<Long, AnalysisItem> uniqueFields = new HashMap<Long, AnalysisItem>();

    public Map<Long, AnalysisItem> getUniqueFields() {
        return uniqueFields;
    }

    public void setUniqueFields(Map<Long, AnalysisItem> uniqueFields) {
        this.uniqueFields = uniqueFields;
    }

    public int getDataSourceType() {
        return DataSourceInfo.COMPOSITE;
    }

    public List<CompositeFeedNode> getCompositeFeedNodes() {
        return compositeFeedNodes;
    }

    public void setCompositeFeedNodes(List<CompositeFeedNode> compositeFeedNodes) {
        this.compositeFeedNodes = compositeFeedNodes;
    }

    public List<CompositeFeedConnection> getConnections() {
        return connections;
    }

    public void setConnections(List<CompositeFeedConnection> connections) {
        this.connections = connections;
    }

    public FeedType getFeedType() {
        return FeedType.COMPOSITE;
    }

    @Override
    public boolean checkDateTime(String name, Key key) {
        if (key instanceof DerivedKey) {
            DerivedKey derivedKey = (DerivedKey) key;
            long childDataSourceID = derivedKey.getFeedID();
            try {
                return new FeedStorage().getFeedDefinitionData(childDataSourceID).checkDateTime(name, derivedKey.getParentKey());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return super.checkDateTime(name, key);
    }

    public void beforeSave(EIConnection conn) throws Exception {
        super.beforeSave(conn);
        populateFields(conn);    
    }

    public List<CompositeFeedConnection> obtainChildConnections() throws SQLException {
        return connections;
    }

    public boolean customJoinsAllowed(EIConnection conn) {
        return true;
    }

    public void customStorage(Connection conn) throws SQLException {
        PreparedStatement clearStmt = conn.prepareStatement("DELETE FROM COMPOSITE_FEED WHERE DATA_FEED_ID = ?");
        clearStmt.setLong(1, getDataFeedID());
        clearStmt.executeUpdate();
        clearStmt.close();
        PreparedStatement nodeStmt = conn.prepareStatement("INSERT INTO COMPOSITE_FEED (DATA_FEED_ID) " +
                "VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        nodeStmt.setLong(1, getDataFeedID());
        nodeStmt.execute();
        long compositeFeedID = Database.instance().getAutoGenKey(nodeStmt);
        nodeStmt.close();
        for (CompositeFeedNode node : compositeFeedNodes) {
            node.store(conn, compositeFeedID);
        }
        for (CompositeFeedConnection connection : connections) {
            connection.store(conn, compositeFeedID);
        }
        nodeStmt.close();
    }

    public void customLoad(Connection conn) throws SQLException {
        PreparedStatement getCustomFeedIDStmt = conn.prepareStatement("SELECT COMPOSITE_FEED_ID FROM COMPOSITE_FEED WHERE " +
                "DATA_FEED_ID = ?");
        getCustomFeedIDStmt.setLong(1, getDataFeedID());
        ResultSet rs = getCustomFeedIDStmt.executeQuery();
        if (rs.next()) {
            long compositeFeedID = rs.getLong(1);
            getCustomFeedIDStmt.close();
            PreparedStatement queryStmt = conn.prepareStatement("SELECT DATA_FEED.DATA_FEED_ID, X, Y, DATA_FEED.FEED_TYPE, DATA_FEED.FEED_NAME, REFRESH_BEHAVIOR FROM " +
                    "COMPOSITE_NODE, DATA_FEED WHERE COMPOSITE_FEED_ID = ? AND COMPOSITE_NODE.DATA_FEED_ID = DATA_FEED.DATA_FEED_ID");
            queryStmt.setLong(1, compositeFeedID);
            ResultSet nodeRS = queryStmt.executeQuery();
            List<CompositeFeedNode> nodes = new ArrayList<CompositeFeedNode>();
            while (nodeRS.next()) {
                long feedID = nodeRS.getLong(1);
                nodes.add(new CompositeFeedNode(feedID, nodeRS.getInt(2), nodeRS.getInt(3), nodeRS.getString(5), nodeRS.getInt(4), nodeRS.getInt(6)));
            }
            queryStmt.close();
            PreparedStatement queryConnStmt = conn.prepareStatement("SELECT SOURCE_FEED_NODE_ID, TARGET_FEED_NODE_ID," +
                    "SOURCE_JOIN, TARGET_JOIN, SOURCE_ITEM_ID, TARGET_ITEM_ID, left_join, right_join, left_join_on_original, right_join_on_original, marmot_script, SOURCE_REPORT_ID, TARGET_REPORT_ID " +
                    " FROM COMPOSITE_CONNECTION WHERE COMPOSITE_FEED_ID = ?");
            PreparedStatement nameStmt = conn.prepareStatement("SELECT FEED_NAME FROM DATA_FEED WHERE DATA_FEED_ID = ?");
            PreparedStatement reportNameStmt = conn.prepareStatement("SELECT TITLE FROM ANALYSIS WHERE ANALYSIS_ID = ?");
            queryConnStmt.setLong(1, compositeFeedID);
            List<CompositeFeedConnection> edges = new ArrayList<CompositeFeedConnection>();
            ResultSet connectionRS = queryConnStmt.executeQuery();
            while (connectionRS.next()) {
                long sourceID = connectionRS.getLong(1);
                long targetID = connectionRS.getLong(2);
                long sourceReportID = connectionRS.getLong(12);
                long targetReportID = connectionRS.getLong(13);
                String sourceName;
                String targetName;
                if (sourceID > 0) {
                    nameStmt.setLong(1, sourceID);
                    ResultSet nameRS = nameStmt.executeQuery();
                    nameRS.next();

                    sourceName = nameRS.getString(1);
                } else {
                    reportNameStmt.setLong(1, sourceReportID);
                    ResultSet nameRS = reportNameStmt.executeQuery();
                    nameRS.next();
                    sourceName = nameRS.getString(1);
                }

                if (targetID > 0) {
                    nameStmt.setLong(1, targetID);
                    ResultSet nameRS = nameStmt.executeQuery();
                    nameRS.next();

                    targetName = nameRS.getString(1);
                } else {
                    reportNameStmt.setLong(1, targetReportID);
                    ResultSet nameRS = reportNameStmt.executeQuery();
                    nameRS.next();
                    targetName = nameRS.getString(1);
                }

                long sourceKeyID = connectionRS.getLong(3);
                if (connectionRS.wasNull()) {
                    AnalysisItem sourceItem = getItem(conn, connectionRS.getLong(5));
                    AnalysisItem targetItem = getItem(conn, connectionRS.getLong(6));
                    boolean sourceJoin = connectionRS.getBoolean(7);
                    boolean targetJoin = connectionRS.getBoolean(8);
                    boolean sourceJoinOnOriginal = connectionRS.getBoolean(9);
                    boolean targetJoinOnOriginal = connectionRS.getBoolean(10);
                    String marmotScript = connectionRS.getString(11);
                    CompositeFeedConnection compositeFeedConnection = new CompositeFeedConnection(sourceID, targetID, sourceItem, targetItem, sourceName, targetName,
                            sourceJoin, targetJoin, sourceJoinOnOriginal, targetJoinOnOriginal, marmotScript);
                    compositeFeedConnection.setSourceReportID(sourceReportID);
                    compositeFeedConnection.setTargetReportID(targetReportID);
                    edges.add(compositeFeedConnection);
                } else {
                    Key sourceKey = getKey(conn, sourceKeyID);
                    Key targetKey = getKey(conn, connectionRS.getLong(4));
                    boolean sourceJoin = connectionRS.getBoolean(7);
                    boolean targetJoin = connectionRS.getBoolean(8);
                    boolean sourceJoinOnOriginal = connectionRS.getBoolean(9);
                    boolean targetJoinOnOriginal = connectionRS.getBoolean(10);
                    String marmotScript = connectionRS.getString(11);
                    CompositeFeedConnection compositeFeedConnection = new CompositeFeedConnection(sourceID, targetID, sourceKey, targetKey, sourceName, targetName,
                            sourceJoin, targetJoin, sourceJoinOnOriginal, targetJoinOnOriginal, marmotScript);
                    compositeFeedConnection.setSourceReportID(sourceReportID);
                    compositeFeedConnection.setTargetReportID(targetReportID);
                    edges.add(compositeFeedConnection);
                }

            }
            this.compositeFeedNodes = nodes;
            this.connections = edges;
            nameStmt.close();
            queryConnStmt.close();
        } else {
            getCustomFeedIDStmt.close();
        }
    }

    protected void loadKPIs(List<AnalysisItem> kpis, EIConnection conn) {
        if (!getFeedType().equals(FeedType.COMPOSITE)) {
            super.loadKPIs(kpis, conn);
            return;
        }
        Session session = Database.instance().createSession(conn);
        try {
            for (CompositeFeedNode node : compositeFeedNodes) {
                PreparedStatement query = conn.prepareStatement("SELECT ANALYSIS_ITEM.ANALYSIS_ITEM_ID FROM ANALYSIS_ITEM, FEED_TO_ANALYSIS_ITEM, DATA_FEED WHERE " +
                        "DATA_FEED.FEED_TYPE = ? AND ANALYSIS_ITEM.KPI = ? AND DATA_FEED.DATA_FEED_ID = FEED_TO_ANALYSIS_ITEM.FEED_ID AND " +
                        "FEED_TO_ANALYSIS_ITEM.ANALYSIS_ITEM_ID = ANALYSIS_ITEM.ANALYSIS_ITEM_ID");
                query.setInt(1, node.getDataSourceType());
                query.setBoolean(2, true);
                ResultSet kpiRS = query.executeQuery();
                while (kpiRS.next()) {
                    long fieldID = kpiRS.getLong(1);
                    AnalysisItem kpi = (AnalysisItem) session.createQuery("from AnalysisItem where analysisItemID = ?").setLong(0, fieldID).list().get(0);
                    kpi.setDataSourceID(node.getDataFeedID());
                    kpi.afterLoad();
                    kpis.add(kpi);
                }
                query.close();
            }
        } catch (SQLException e) {
            LogClass.error(e);
        } finally {
            session.close();
        }
    }

    protected void addKPIsToFolder(FeedFolder clonedFolder, List<AnalysisItem> kpis, Map<Long, AnalysisItem> replacementMap) {
        if (!getFeedType().equals(FeedType.COMPOSITE)) {
            super.addKPIsToFolder(clonedFolder, kpis, replacementMap);
            return;
        }
        long dataSourceID = 0;
        for (CompositeFeedNode node : compositeFeedNodes) {
            if (node.getDataSourceName().equals(clonedFolder.getName())) {
                dataSourceID = node.getDataFeedID();
                break;
            }
        }
        if (dataSourceID != 0) {
            FeedFolder kpiFolder = new FeedFolder();
            kpiFolder.setName("KPIs");
            int size = 0;
            for (AnalysisItem kpi : kpis) {
                if (kpi.getDataSourceID() == dataSourceID) {
                    size++;
                    AnalysisItem targetKPI = replacementMap.remove(kpi.getAnalysisItemID());
                    targetKPI.setKpi(false);
                    kpiFolder.addAnalysisItem(targetKPI);
                }
            }
            if (size > 0) {
                clonedFolder.getChildFolders().add(kpiFolder);
            }
        }
    }

    protected void addKPIs(List<AnalysisItem> kpis, Map<Long, AnalysisItem> replacementMap, List<FeedNode> feedNodes) {
        if (!getFeedType().equals(FeedType.COMPOSITE)) {
            super.addKPIs(kpis, replacementMap, feedNodes);
        }
    }

    protected boolean compositeSourceFolder(FeedFolder clonedFolder) {
        if (!getFeedType().equals(FeedType.COMPOSITE)) {
            return super.compositeSourceFolder(clonedFolder);
        }
        for (CompositeFeedNode node : compositeFeedNodes) {
            if (node.getDataSourceName().equals(clonedFolder.getName())) {
                return true;
            }
        }
        return false;
    }

    public Feed createFeedObject(FeedDefinition parent) {
        try {
            return new CompositeFeed(compositeFeedNodes, obtainChildConnections(), getAddonReports());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    private Key getKey(Connection conn, long targetJoinID) {
        Session session = Database.instance().createSession(conn);
        Key key = null;
        try {
            List results = session.createQuery("from Key where keyID = ?").setLong(0, targetJoinID).list();
            key = (Key) results.get(0);
            key.afterLoad();
        } finally {
            session.close();
        }
        return key;
    }

    private AnalysisItem getItem(Connection conn, long itemID) {
        Session session = Database.instance().createSession(conn);
        AnalysisItem analysisItem = null;
        try {
            List results = session.createQuery("from AnalysisItem where analysisItemID = ?").setLong(0, itemID).list();
            analysisItem = (AnalysisItem) results.get(0);
            analysisItem.afterLoad();
        } finally {
            session.close();
        }
        return analysisItem;
    }

    public void populateFields(Connection conn) {
        // get fields from the composite feed nodes...
        try {
            /*GatherItemVisitor gatherItemVisitor = new GatherItemVisitor(conn);
            gatherItemVisitor.visit(this);*/

            AnalysisItemVisitor analysisItemVisitor = new AnalysisItemVisitor(conn);
            analysisItemVisitor.visit(this);

            // have to reconcile what was returned by the visitor with existing fields

            Map<Long, AnalysisItem> replacementMap = new HashMap<Long, AnalysisItem>();

            Map<Key, AnalysisItem> childToParentMap = new HashMap<Key, AnalysisItem>();

            for (AnalysisItem field : analysisItemVisitor.fields) {
                childToParentMap.put(field.createAggregateKey(), field);
            }

            List<AnalysisItem> newFields = new ArrayList<AnalysisItem>();
            for (AnalysisItem field : getFields()) {
                if (field.getAnalysisItemID() != 0) {
                    AnalysisItem item = childToParentMap.get(field.createAggregateKey());
                    if (item != null) {
                        replacementMap.put(field.getAnalysisItemID(), item);
                    } else {
                        replacementMap.put(field.getAnalysisItemID(), field);
                    }
                } else {
                    // ????
                    newFields.add(field);
                }
            }

            List<AnalysisItem> fields = getFields() == null ? new ArrayList<AnalysisItem>() : getFields();

            // Clear any existing fields from child data sources, since those may have changed


            
            // define folder for each child
            
            Map<String, AnalysisItem> keyMap = new HashMap<String, AnalysisItem>();
            Map<String, List<AnalysisItem>> duplicateNameMap = new HashMap<String, List<AnalysisItem>>();
            for (AnalysisItem analysisItem : analysisItemVisitor.fields) {
                String displayName = analysisItem.toDisplay();
                AnalysisItem existing = keyMap.get(displayName);
                if (existing == null) {
                    keyMap.put(displayName, analysisItem);
                } else {
                    List<AnalysisItem> analysisItems = duplicateNameMap.get(displayName);
                    if (analysisItems == null) {
                        analysisItems = new ArrayList<AnalysisItem>();
                        duplicateNameMap.put(displayName, analysisItems);
                        analysisItems.add(existing);
                    }
                    analysisItems.add(analysisItem);
                }
            }

            for (Map.Entry<String, List<AnalysisItem>> entry : duplicateNameMap.entrySet()) {
                for (AnalysisItem analysisItem : entry.getValue()) {
                    DerivedKey derivedKey = (DerivedKey) analysisItem.getKey();
                    String name = getCompositeFeedName(derivedKey.getFeedID(), conn);
                    analysisItem.setDisplayName(name + " - " + entry.getKey());
                }
            }

            Iterator<AnalysisItem> iter = fields.iterator();
            Set<String> fieldSet = new HashSet<String>();
            for (AnalysisItem gatherItem : analysisItemVisitor.fields) {
                fieldSet.add(gatherItem.toDisplay());
            }
            while (iter.hasNext()) {
                AnalysisItem analysisItem = iter.next();
                if (fieldSet.contains(analysisItem.toDisplay())) {
                    iter.remove();
                }
            }

            Map<Long, FeedFolder> folderMap = new HashMap<Long, FeedFolder>();
            for (CompositeFeedNode feed : getCompositeFeedNodes()) {
                String name = getCompositeFeedName(feed.getDataFeedID(), conn);
                folderMap.put(feed.getDataFeedID(), defineFolder(name));
            }

            ReplacementMap replacements = ReplacementMap.fromMap(replacementMap);

            for (AnalysisItem analysisItem : newFields) {
                replacements.addField(analysisItem, false);
            }

            for (AnalysisItem analysisItem : getFields()) {
                analysisItem.updateIDs(replacements);
            }

            for (AnalysisItem analysisItem : analysisItemVisitor.fields) {
                fields.add(analysisItem);
            }

            for (Map.Entry<Long, List<FeedNode>> entry : analysisItemVisitor.nodeMap.entrySet()) {
                long dataSourceID = entry.getKey();
                FeedFolder dataSourceFolder = folderMap.get(dataSourceID);
                for (FeedNode feedNode : entry.getValue()) {
                    FeedFolder folder = createFeedFolder(feedNode);
                    dataSourceFolder.getChildFolders().add(folder);
                }
            }
            for (Map.Entry<Long, List<AnalysisItem>> entry : analysisItemVisitor.rootLevelFields.entrySet()) {
                long dataSourceID = entry.getKey();
                FeedFolder dataSourceFolder = folderMap.get(dataSourceID);
                for (AnalysisItem item : entry.getValue()) {
                    dataSourceFolder.getChildItems().add(item);
                }
            }
            setFields(fields);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    private FeedFolder createFeedFolder(FeedNode feedNode) {
        FolderNode folderNode = (FolderNode) feedNode;
        FeedFolder folder = new FeedFolder();
        folder.setName(folderNode.getFolder().getName());
        for (FeedNode child : folderNode.getChildren()) {
            if (child instanceof FolderNode) {
                folder.getChildFolders().add(createFeedFolder(child));
            } else {
                AnalysisItemNode childItem = (AnalysisItemNode) child;
                folder.getChildItems().add(childItem.getAnalysisItem());
            }
        }
        return folder;
    }

    private String getCompositeFeedName(long feedID, Connection conn) {
        try {
            PreparedStatement nameStmt = conn.prepareStatement("SELECT FEED_NAME FROM DATA_FEED WHERE DATA_FEED_ID = ?");
            nameStmt.setLong(1, feedID);
            ResultSet rs = nameStmt.executeQuery();
            rs.next();
            String name = rs.getString(1);
            nameStmt.close();
            return name;
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    private class AnalysisItemVisitor extends CompositeFeedNodeShallowVisitor {

        private Map<Long, List<FeedNode>> nodeMap = new LinkedHashMap<Long, List<FeedNode>>();
        private Map<Long, List<AnalysisItem>> rootLevelFields = new HashMap<Long, List<AnalysisItem>>();
        private List<AnalysisItem> fields = new ArrayList<AnalysisItem>();
        private Connection conn;

        private AnalysisItemVisitor(Connection conn) {
            this.conn = conn;
        }

        protected void accept(CompositeFeedNode compositeFeedNode) throws SQLException, CloneNotSupportedException {
            Map<Long, AnalysisItem> replacementMap = new HashMap<Long, AnalysisItem>();
            List<AnalysisItem> analysisItemList = retrieveFields(compositeFeedNode.getDataFeedID(), conn);
            List<FeedFolder> folders = new FeedStorage().getFolders(compositeFeedNode.getDataFeedID(), analysisItemList, conn);

            List<AnalysisItem> localFields = new ArrayList<AnalysisItem>();
            for (AnalysisItem analysisItem : analysisItemList) {
                AnalysisItem clonedItem = analysisItem.clone();
                Key key = clonedItem.getKey();
                DerivedKey derivedKey = new DerivedKey();
                derivedKey.setFeedID(compositeFeedNode.getDataFeedID());
                derivedKey.setParentKey(key);
                clonedItem.setKey(derivedKey);
                clonedItem.setAnalysisItemID(0);
                localFields.add(clonedItem);
                fields.add(clonedItem);
                replacementMap.put(analysisItem.getAnalysisItemID(), clonedItem);
            }

            ReplacementMap replacements = ReplacementMap.fromMap(replacementMap);

            for (Map.Entry<Long, AnalysisItem> replEntry : replacementMap.entrySet()) {
                replEntry.getValue().updateIDs(replacements);
            }


            Set<AnalysisItem> set = new HashSet<AnalysisItem>(localFields);

            List<FeedNode> feedNodes = new ArrayList<FeedNode>();
            for (FeedFolder feedFolder : folders) {
                try {
                    feedNodes.add(cloneFolder(feedFolder, set, replacementMap, true));
                } catch (CloneNotSupportedException e) {
                    LogClass.error(e);
                }
            }

            rootLevelFields.put(compositeFeedNode.getDataFeedID(), new ArrayList<AnalysisItem>(set));

            nodeMap.put(compositeFeedNode.getDataFeedID(), feedNodes);
        }
    }

    private FeedNode cloneFolder(FeedFolder feedFolder, Set<AnalysisItem> set, Map<Long, AnalysisItem> replacementMap, boolean first) throws CloneNotSupportedException {
        FeedFolder clonedFolder = feedFolder.clone();
        if (first) {
            clonedFolder.updateIDs(replacementMap);
        }
        FeedNode feedNode = clonedFolder.toFeedNode();
        for (AnalysisItem item : clonedFolder.getChildItems()) {
            set.remove(item);
        }
        Iterator<FeedNode> nodeIter = feedNode.getChildren().iterator();
        while (nodeIter.hasNext()) {
            FeedNode child = nodeIter.next();
            if (child instanceof FolderNode) {
                nodeIter.remove();
            }
        }
        for (FeedFolder childFolder : clonedFolder.getChildFolders()) {
            FeedNode childNode = cloneFolder(childFolder, set, replacementMap, false);
            feedNode.getChildren().add(childNode);
        }
        return feedNode;
    }

    private List<AnalysisItem> retrieveFields(long feedID, Connection conn) throws SQLException {
        return new FeedStorage().retrieveFields(feedID, conn);
    }

    @Override
    public Map<Long, SolutionInstallInfo> cloneDataSource(Connection conn) throws Exception {
        Map<Long, SolutionInstallInfo> infos = super.cloneDataSource(conn);
        CompositeFeedDefinition feedDefinition = (CompositeFeedDefinition) infos.get(getDataFeedID()).getNewDataSource();

        // need clean model here
        // clone the core information
        // there's a "generate keys" phase, effectively
        // 

        Map<Long, SolutionInstallInfo> replacementMap = new HashMap<Long, SolutionInstallInfo>();
        List<CompositeFeedNode> newChildren = new ArrayList<CompositeFeedNode>();
        for (CompositeFeedNode child : getCompositeFeedNodes()) {
            FeedDefinition childDefinition = new FeedStorage().getFeedDefinitionData(child.getDataFeedID(), conn);
            infos.putAll(DataSourceCopyUtils.installFeed(SecurityUtil.getUserID(), conn, false, childDefinition, childDefinition.getFeedName(), 0, SecurityUtil.getAccountID(), SecurityUtil.getUserName()));
            FeedDefinition clonedDefinition = infos.get(child.getDataFeedID()).getNewDataSource();
            replacementMap.putAll(infos);
            newChildren.add(new CompositeFeedNode(clonedDefinition.getDataFeedID(), child.getX(), child.getY(), child.getDataSourceName(), child.getDataSourceType(),
                    child.getRefreshBehavior()));
        }

        feedDefinition.setCompositeFeedNodes(newChildren);
        List<CompositeFeedConnection> newConnections = new ArrayList<CompositeFeedConnection>();
        for (CompositeFeedConnection connection : feedDefinition.getConnections()) {
            CompositeFeedConnection newConnection = new CompositeFeedConnection();
            newConnection.setSourceJoin(replacementMap.get(connection.getSourceFeedID()).getNewDataSource().getField(connection.getSourceJoin().toKeyString()));
            newConnection.setTargetJoin(replacementMap.get(connection.getTargetFeedID()).getNewDataSource().getField(connection.getTargetJoin().toKeyString()));
            newConnection.setSourceFeedID(replacementMap.get(connection.getSourceFeedID()).getNewDataSource().getDataFeedID());
            newConnection.setTargetFeedID(replacementMap.get(connection.getTargetFeedID()).getNewDataSource().getDataFeedID());
            newConnections.add(newConnection);
        }
        feedDefinition.setConnections(newConnections);

        // at this point, we're referencing the cloned keys on our internal fields
        for (AnalysisItem analysisItem : feedDefinition.getFields()) {
            Key key = analysisItem.getKey();
            if (key instanceof DerivedKey) {
                DerivedKey derivedKey = (DerivedKey) key;
                SolutionInstallInfo result = replacementMap.get(derivedKey.getFeedID());
                derivedKey.setFeedID(result.getNewDataSource().getDataFeedID());
                derivedKey.setParentKey(result.getKeyReplacementMap().get(derivedKey.getParentKey()));
            }
        }
        return infos;
    }

    protected void cloneFields() {

    }

    public void postClone(Connection conn) throws Exception {
        FeedStorage feedStorage = new FeedStorage();
        for (CompositeFeedNode child : getCompositeFeedNodes()) {
            FeedDefinition feedDefinition = feedStorage.getFeedDefinitionData(child.getDataFeedID(), conn);
            feedDefinition.setParentSourceID(getDataFeedID());
            feedStorage.updateDataFeedConfiguration(feedDefinition, conn);
        }
    }

    @Override
    public void delete(Connection conn) throws SQLException {
        super.delete(conn);
        for (CompositeFeedNode node : getCompositeFeedNodes()) {
            FeedDefinition feedDefinition = new FeedStorage().getFeedDefinitionData(node.getDataFeedID(), conn);
            if (feedDefinition != null && !feedDefinition.isVisible()) {
                feedDefinition.delete(conn);
            }
        }
    }

    @Override
    protected void onDelete(Connection conn) throws SQLException {

    }

    public Collection<DataSourceDescriptor> getDataSources(EIConnection conn) throws SQLException {
        Collection<DataSourceDescriptor> sources = super.getDataSources(conn);
        for (CompositeFeedNode node : getCompositeFeedNodes()) {
            FeedDefinition feedDefinition = new FeedStorage().getFeedDefinitionData(node.getDataFeedID(), conn);
            if (feedDefinition.isVisible()) {
                sources.addAll(feedDefinition.getDataSources(conn));
            }
        }
        return sources;
    }
}