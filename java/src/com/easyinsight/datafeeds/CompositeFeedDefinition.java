package com.easyinsight.datafeeds;

import com.easyinsight.analysis.AggregateKey;
import com.easyinsight.core.Key;
import com.easyinsight.core.DerivedKey;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.DataSourceInfo;
import com.easyinsight.database.EIConnection;
import com.easyinsight.logging.LogClass;
import com.easyinsight.database.Database;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.security.Roles;
import com.easyinsight.userupload.UserUploadInternalService;

import java.sql.*;
import java.util.*;

import org.hibernate.Session;

/**
 * User: James Boe
 * Date: May 31, 2008
 * Time: 11:55:05 PM
 */
public class CompositeFeedDefinition extends FeedDefinition {
    private List<CompositeFeedNode> compositeFeedNodes = new ArrayList<CompositeFeedNode>();
    private List<CompositeFeedConnection> connections = new ArrayList<CompositeFeedConnection>();

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

    public void beforeSave(EIConnection conn) {
        populateFields(conn);    
    }

    public List<CompositeFeedConnection> obtainChildConnections() throws SQLException {
        return connections;
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
        rs.next();
        long compositeFeedID = rs.getLong(1);
        getCustomFeedIDStmt.close();
        PreparedStatement queryStmt = conn.prepareStatement("SELECT DATA_FEED_ID, X, Y FROM COMPOSITE_NODE WHERE COMPOSITE_FEED_ID = ?");
        queryStmt.setLong(1, compositeFeedID);
        ResultSet nodeRS = queryStmt.executeQuery();
        List<CompositeFeedNode> nodes = new ArrayList<CompositeFeedNode>();
        while (nodeRS.next()) {
            long feedID = nodeRS.getLong(1);
            nodes.add(new CompositeFeedNode(feedID, nodeRS.getInt(2), nodeRS.getInt(3)));
        }
        queryStmt.close();
        PreparedStatement queryConnStmt = conn.prepareStatement("SELECT SOURCE_FEED_NODE_ID, TARGET_FEED_NODE_ID," +
                "SOURCE_JOIN, TARGET_JOIN FROM COMPOSITE_CONNECTION WHERE COMPOSITE_FEED_ID = ?");
        queryConnStmt.setLong(1, compositeFeedID);
        List<CompositeFeedConnection> edges = new ArrayList<CompositeFeedConnection>();
        ResultSet connectionRS = queryConnStmt.executeQuery();
        while (connectionRS.next()) {
            long sourceID = connectionRS.getLong(1);
            long targetID = connectionRS.getLong(2);
            Key sourceKey = getKey(conn, connectionRS.getLong(3));
            Key targetKey = getKey(conn, connectionRS.getLong(4));
            edges.add(new CompositeFeedConnection(sourceID, targetID, sourceKey, targetKey));
        }
        this.compositeFeedNodes = nodes;
        this.connections = edges;
        queryConnStmt.close();
    }

    public Feed createFeedObject(FeedDefinition parent) {
        try {
            return new CompositeFeed(compositeFeedNodes, obtainChildConnections());
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
        } finally {
            session.close();
        }
        return key;
    }

    public void populateFields(Connection conn) {
        // get fields from the composite feed nodes...
        try {
            AnalysisItemVisitor analysisItemVisitor = new AnalysisItemVisitor(conn);
            analysisItemVisitor.visit(this);

            // have to reconcile what was returned by the visitor with existing fields

            

            List<AnalysisItem> fields = getFields() == null ? new ArrayList<AnalysisItem>() : getFields();
            
            // define folder for each child
            Map<String, AnalysisItem> keyMap = new HashMap<String, AnalysisItem>();
            Map<String, List<AnalysisItem>> duplicateNameMap = new HashMap<String, List<AnalysisItem>>();
            for (AnalysisItem analysisItem : analysisItemVisitor.derivedItems) {
                String displayName = analysisItem.getDisplayName() != null ? analysisItem.getDisplayName() : analysisItem.getKey().toKeyString();
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

            Map<Long, FeedFolder> folderMap = new HashMap<Long, FeedFolder>();
            for (CompositeFeedNode feed : getCompositeFeedNodes()) {
                String name = getCompositeFeedName(feed.getDataFeedID(), conn);
                folderMap.put(feed.getDataFeedID(), defineFolder(name));
            }



            for (Map.Entry<Long, List<FeedNode>> entry : analysisItemVisitor.nodeMap.entrySet()) {
                long dataSourceID = entry.getKey();
                FeedFolder dataSourceFolder = folderMap.get(dataSourceID);
                for (FeedNode feedNode : entry.getValue()) {
                    addFeedNode(feedNode, dataSourceFolder);
                }
            }
            Map<AggregateKey, AnalysisItem> existingItems = new HashMap<AggregateKey, AnalysisItem>();
            if (getFields() != null) {
                for (AnalysisItem analysisItem : getFields()) {
                    existingItems.put(analysisItem.createAggregateKey(), analysisItem);
                }
            }
            for (AnalysisItem analysisItem : analysisItemVisitor.derivedItems) {
                // TODO: is the analysis item already here?
                // is the item already in a folder?
                if (existingItems.get(analysisItem.createAggregateKey()) == null) {
                    fields.add(analysisItem);
                }
            }

            setFields(fields);
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    private void addFeedNode(FeedNode feedNode, FeedFolder parentFolder) {
        if (feedNode instanceof FolderNode) {
            FolderNode folderNode = (FolderNode) feedNode;
            FeedFolder feedFolder = parentFolder.getFolderByName(folderNode.getFolder().getName());
            if (feedFolder == null) {
                feedFolder = new FeedFolder();
                feedFolder.setName(folderNode.getFolder().getName());
                parentFolder.getChildFolders().add(feedFolder);
            }
            for (FeedNode childNode : folderNode.getChildren()) {
                addFeedNode(childNode, feedFolder);
            }
        } else {
            AnalysisItemNode itemNode = (AnalysisItemNode) feedNode;
            // TODO: is the analysis item already in this folder?
            if (parentFolder.getAnalysisItem(itemNode.getAnalysisItem().getKey().toKeyString()) == null) {
                parentFolder.addAnalysisItem(itemNode.getAnalysisItem());    
            }
        }
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

        private Map<Long, List<FeedNode>> nodeMap = new HashMap<Long, List<FeedNode>>();
        private List<AnalysisItem> derivedItems = new ArrayList<AnalysisItem>();
        private Connection conn;

        private AnalysisItemVisitor(Connection conn) {
            this.conn = conn;
        }

        protected void accept(CompositeFeedNode compositeFeedNode) throws SQLException {
            Map<Long, AnalysisItem> replacementMap = new HashMap<Long, AnalysisItem>();
            List<AnalysisItem> analysisItemList = retrieveFields(compositeFeedNode.getDataFeedID(), conn);
            List<FeedFolder> folders = new FeedStorage().getFolders(compositeFeedNode.getDataFeedID(), analysisItemList, conn);


            for (AnalysisItem analysisItem : analysisItemList) {
                AnalysisItem clonedItem;
                try {
                    clonedItem = analysisItem.clone();
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }
                Key key = analysisItem.getKey();
                DerivedKey derivedKey = new DerivedKey();
                derivedKey.setFeedID(compositeFeedNode.getDataFeedID());
                derivedKey.setParentKey(key);
                clonedItem.setKey(derivedKey);
                clonedItem.setAnalysisItemID(0);
                derivedItems.add(clonedItem);
                replacementMap.put(analysisItem.getAnalysisItemID(), clonedItem);
            }
            for (Map.Entry<Long, AnalysisItem> replEntry : replacementMap.entrySet()) {
                replEntry.getValue().updateIDs(replacementMap);
            }

            List<FeedNode> feedNodes = new ArrayList<FeedNode>();
            for (FeedFolder feedFolder : folders) {
                try {
                    FeedFolder clonedFolder = feedFolder.clone();
                    clonedFolder.updateIDs(replacementMap);
                    feedNodes.add(clonedFolder.toFeedNode());
                } catch (CloneNotSupportedException e) {
                    LogClass.error(e);
                }
            }
            for (AnalysisItem analysisItem : replacementMap.values()) {
                if (!analysisItem.isHidden()) {
                    feedNodes.add(analysisItem.toFeedNode());
                }
            }

            nodeMap.put(compositeFeedNode.getDataFeedID(), feedNodes);
        }
    }

    private List<AnalysisItem> retrieveFields(long feedID, Connection conn) throws SQLException {
        return new FeedStorage().retrieveFields(feedID, conn);
    }

    @Override
    public DataSourceCloneResult cloneDataSource(Connection conn) throws CloneNotSupportedException, SQLException {
        DataSourceCloneResult dataSourceCloneResult = super.cloneDataSource(conn);
        CompositeFeedDefinition feedDefinition = (CompositeFeedDefinition) dataSourceCloneResult.getFeedDefinition();

        // need clean model here
        // clone the core information
        // there's a "generate keys" phase, effectively
        // 

        Map<Long, DataSourceCloneResult> replacementMap = new HashMap<Long, DataSourceCloneResult>();
        List<CompositeFeedNode> newChildren = new ArrayList<CompositeFeedNode>();
        for (CompositeFeedNode child : getCompositeFeedNodes()) {
            FeedDefinition childDefinition = new FeedStorage().getFeedDefinitionData(child.getDataFeedID(), conn);
            DataSourceCloneResult result = DataSourceCopyUtils.cloneFeed(SecurityUtil.getUserID(), conn, childDefinition, false, SecurityUtil.getAccountID(), SecurityUtil.getUserName());
            FeedDefinition clonedDefinition = result.getFeedDefinition();
            DataSourceCopyUtils.buildClonedDataStores(false, feedDefinition, clonedDefinition, conn);
            new UserUploadInternalService().createUserFeedLink(SecurityUtil.getUserID(), clonedDefinition.getDataFeedID(), Roles.OWNER, conn);
            replacementMap.put(child.getDataFeedID(), result);
            newChildren.add(new CompositeFeedNode(clonedDefinition.getDataFeedID(), child.getX(), child.getY()));
        }

        // 

        feedDefinition.setCompositeFeedNodes(newChildren);
        List<CompositeFeedConnection> newConnections = new ArrayList<CompositeFeedConnection>();
        for (CompositeFeedConnection connection : feedDefinition.getConnections()) {
            CompositeFeedConnection newConnection = new CompositeFeedConnection();
            newConnection.setSourceJoin(replacementMap.get(connection.getSourceFeedID()).getFeedDefinition().getField(connection.getSourceJoin().toKeyString()));
            newConnection.setTargetJoin(replacementMap.get(connection.getTargetFeedID()).getFeedDefinition().getField(connection.getTargetJoin().toKeyString()));
            newConnection.setSourceFeedID(replacementMap.get(connection.getSourceFeedID()).getFeedDefinition().getDataFeedID());
            newConnection.setTargetFeedID(replacementMap.get(connection.getTargetFeedID()).getFeedDefinition().getDataFeedID());
            newConnections.add(newConnection);
        }
        feedDefinition.setConnections(newConnections);

        // at this point, we're referencing the cloned keys on our internal fields
        for (AnalysisItem analysisItem : feedDefinition.getFields()) {
            Key key = analysisItem.getKey();
            if (key instanceof DerivedKey) {
                DerivedKey derivedKey = (DerivedKey) key;
                DataSourceCloneResult result = replacementMap.get(derivedKey.getFeedID());
                derivedKey.setFeedID(result.getFeedDefinition().getDataFeedID());
                derivedKey.setParentKey(result.getKeyReplacementMap().get(derivedKey.getParentKey()));
            }
        }
        return dataSourceCloneResult;
    }

    protected void cloneFields() {

    }

    public void postClone(Connection conn) throws SQLException {
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
}
