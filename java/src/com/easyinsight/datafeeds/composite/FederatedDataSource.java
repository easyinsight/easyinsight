package com.easyinsight.datafeeds.composite;

import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.DataSourceInfo;
import com.easyinsight.core.Key;
import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.*;
import com.easyinsight.logging.LogClass;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jamesboe
 * Date: 3/5/11
 * Time: 6:29 PM
 */
public class FederatedDataSource extends FeedDefinition {
    private List<FederationSource> sources = new ArrayList<FederationSource>();
    private AnalysisItem analysisItem;

    @Override
    public int getDataSourceType() {
        return DataSourceInfo.COMPOSITE;
    }

    public List<FederationSource> getSources() {
        return sources;
    }

    public void setSources(List<FederationSource> sources) {
        this.sources = sources;
    }

    public AnalysisItem getAnalysisItem() {
        return analysisItem;
    }

    public void setAnalysisItem(AnalysisItem analysisItem) {
        this.analysisItem = analysisItem;
    }

    @Override
    public boolean checkDateTime(String name, Key key) {
        FederationSource source1 = sources.get(0);
        FeedDefinition child;
        try {
            child = new FeedStorage().getFeedDefinitionData(source1.getDataSourceID());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return child.checkDateTime(name, key);
    }

    @Override
    public boolean customJoinsAllowed(EIConnection conn) throws SQLException {
        FederationSource source1 = sources.get(0);
        FeedDefinition child = new FeedStorage().getFeedDefinitionData(source1.getDataSourceID(), conn);
        return child.customJoinsAllowed(conn);
    }

    @Override
    public void customStorage(Connection conn) throws SQLException {
        super.customStorage(conn);
        PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM FEDERATED_DATA_SOURCE WHERE data_source_id = ?");
        deleteStmt.setLong(1, getDataFeedID());
        deleteStmt.executeUpdate();
        PreparedStatement saveStmt = conn.prepareStatement("INSERT INTO FEDERATED_DATA_SOURCE (DATA_SOURCE_ID, ANALYSIS_ITEM_ID) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        saveStmt.setLong(1, getDataFeedID());
        if (analysisItem == null) {
            saveStmt.setNull(2, Types.BIGINT);
        } else {

        }
        saveStmt.execute();
        long id = Database.instance().getAutoGenKey(saveStmt);
        saveStmt.close();
        PreparedStatement clearMappingsStmt = conn.prepareStatement("DELETE FROM FEDERATED_FIELD_MAPPING WHERE FEDERATED_DATA_SOURCE_ID = ?");
        clearMappingsStmt.setLong(1, getDataFeedID());
        clearMappingsStmt.executeUpdate();
        clearMappingsStmt.close();
        PreparedStatement saveJoinStmt = conn.prepareStatement("INSERT INTO FEDERATED_DATA_SOURCE_TO_DATA_SOURCE (FEDERATED_DATA_SOURCE_ID, DATA_SOURCE_ID) VALUES (?, ?)");
        PreparedStatement saveMappingsStmt = conn.prepareStatement("INSERT INTO FEDERATED_FIELD_MAPPING (federated_key, SOURCE_KEY, federated_data_source_id, data_source_id) VALUES (?, ?, ?, ?)");
        for (FederationSource source : sources) {
            saveJoinStmt.setLong(1, id);
            saveJoinStmt.setLong(2, source.getDataSourceID());
            saveJoinStmt.execute();
            for (FieldMapping fieldMapping : source.getFieldMappings()) {
                saveMappingsStmt.setString(1, fieldMapping.getFederatedKey());
                saveMappingsStmt.setString(2, fieldMapping.getSourceKey());
                saveMappingsStmt.setLong(3, getDataFeedID());
                saveMappingsStmt.setLong(4, source.getDataSourceID());
                saveMappingsStmt.execute();
            }
        }
    }

    @Override
    public Feed createFeedObject(FeedDefinition parent) {
        return new FederatedFeed(sources);
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.FEDERATED;
    }

    @Override
    public void customLoad(Connection conn) throws SQLException {
        super.customLoad(conn);
        PreparedStatement getStmt = conn.prepareStatement("SELECT FEDERATED_DATA_SOURCE_ID FROM FEDERATED_DATA_SOURCE WHERE data_source_id = ?");
        getStmt.setLong(1, getDataFeedID());
        ResultSet rs = getStmt.executeQuery();
        List<FederationSource> sources = new ArrayList<FederationSource>();
        if (rs.next()) {
            long id = rs.getLong(1);
            PreparedStatement stmt = conn.prepareStatement("SELECT DATA_SOURCE_ID, FEED_NAME, FEED_TYPE FROM FEDERATED_DATA_SOURCE_TO_DATA_SOURCE, DATA_FEED WHERE FEDERATED_DATA_SOURCE_ID = ? AND " +
                    "DATA_SOURCE_ID = DATA_FEED_ID");
            PreparedStatement mappingStmt = conn.prepareStatement("SELECT FEDERATED_KEY, SOURCE_KEY FROM federated_field_mapping WHERE federated_data_source_id = ? AND data_source_id = ?");
            stmt.setLong(1, id);
            ResultSet fedRS = stmt.executeQuery();
            while (fedRS.next()) {
                FederationSource federationSource = new FederationSource();
                federationSource.setDataSourceID(fedRS.getLong(1));
                federationSource.setName(fedRS.getString(2));
                federationSource.setDataSourceType(fedRS.getInt(3));
                sources.add(federationSource);
                mappingStmt.setLong(1, getDataFeedID());
                mappingStmt.setLong(2, federationSource.getDataSourceID());
                ResultSet mappingRS = mappingStmt.executeQuery();
                List<FieldMapping> mappings = new ArrayList<FieldMapping>();
                while (mappingRS.next()) {
                    FieldMapping fieldMapping = new FieldMapping();
                    fieldMapping.setFederatedKey(mappingRS.getString(1));
                    fieldMapping.setSourceKey(mappingRS.getString(2));
                    mappings.add(fieldMapping);
                }
                federationSource.setFieldMappings(mappings);
            }
        }
        this.sources = sources;
    }

    public void populateFields(EIConnection conn) throws SQLException, CloneNotSupportedException {
        FederationSource source = sources.get(0);
        FeedDefinition child = new FeedStorage().getFeedDefinitionData(source.getDataSourceID(), conn);
        List<AnalysisItem> newFields = new ArrayList<AnalysisItem>();
        Map<Long, AnalysisItem> replacementMap = new HashMap<Long, AnalysisItem>();
        for (AnalysisItem analysisItem : child.getFields()) {
            AnalysisItem clonedItem = analysisItem.clone();
            clonedItem.setAnalysisItemID(0);
            Key clonedKey = clonedItem.getKey().clone();
            clonedItem.setKey(clonedKey);
            newFields.add(clonedItem);
            replacementMap.put(analysisItem.getAnalysisItemID(), clonedItem);
        }
        for (Map.Entry<Long, AnalysisItem> replEntry : replacementMap.entrySet()) {
            replEntry.getValue().updateIDs(replacementMap);
        }
        setFields(newFields);
        List<FeedFolder> clonedFolders = new ArrayList<FeedFolder>();
        for (FeedFolder feedFolder : child.getFolders()) {
            try {
                FeedFolder clonedFolder = feedFolder.clone();
                clonedFolder.updateIDs(replacementMap);
                clonedFolders.add(clonedFolder);
            } catch (CloneNotSupportedException e) {
                LogClass.error(e);
            }
        }
        setFolders(clonedFolders);
    }
}
