package com.easyinsight.storage;

import com.easyinsight.analysis.*;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.security.Roles;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.logging.LogClass;
import com.easyinsight.database.Database;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.datafeeds.FeedPersistenceMetadata;
import com.easyinsight.core.*;
import com.easyinsight.cache.Cache;
import com.easyinsight.users.Account;

import java.text.NumberFormat;
import java.util.*;
import java.util.Date;
import java.sql.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

/**
 * User: James Boe
 * Date: Nov 8, 2008
 * Time: 4:08:06 PM
 */
public class DataStorage implements IDataStorage {
    private Map<Key, KeyMetadata> keys;
    private long feedID;
    private long accountID;
    private int version;
    private boolean systemUpdate;
    private Database database;
    private Connection storageConn;
    private Connection coreDBConn;
    private boolean committed = false;
    private FeedPersistenceMetadata metadata;
    private int dataSourceType;
    private static DateDimCache dateDimCache = new DateDimCache();

    private JCS reportCache = Cache.getCache(Cache.EMBEDDED_REPORTS);
                               
    /**
     * Creates a read only connection for retrieving data.
     *
     * @param fields the analysis items you want to retrieve
     * @param feedID the ID of the data source
     * @return a DataStorage object for making read calls
     */

    public static DataStorage readConnection(List<AnalysisItem> fields, long feedID) {
        DataStorage dataStorage = new DataStorage();
        Map<Key, KeyMetadata> keyMetadatas = new HashMap<Key, KeyMetadata>();
        for (AnalysisItem analysisItem : fields) {
            if (analysisItem.isDerived()) {
                continue;
            }
            Key key = analysisItem.createAggregateKey(false);
            if (analysisItem.hasType(AnalysisItemTypes.DATE_DIMENSION)) {
                keyMetadatas.put(key, new KeyMetadata(key, Value.DATE, analysisItem));
            } else if (analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
                keyMetadatas.put(key, new KeyMetadata(key, Value.NUMBER, analysisItem));
            } else if (analysisItem.hasType(AnalysisItemTypes.TEXT)) {
                keyMetadatas.put(key, new KeyMetadata(key, Value.TEXT, analysisItem));
            } else {
                keyMetadatas.put(key, new KeyMetadata(key, Value.STRING, analysisItem));
            }
        }
        Connection conn = Database.instance().getConnection();
        try {
            dataStorage.metadata = getMetadata(feedID, conn);
            if (dataStorage.metadata == null) {
                dataStorage.metadata = createDefaultMetadata(conn);
            }
        } finally {
            Database.closeConnection(conn);
        }
        dataStorage.keys = keyMetadatas;
        dataStorage.feedID = feedID;
        dataStorage.version = dataStorage.metadata.getVersion();
        dataStorage.database = DatabaseManager.instance().getDatabase(dataStorage.metadata.getDatabase());
        dataStorage.storageConn = dataStorage.database.getConnection();
        try {
            dataStorage.storageConn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dataStorage;
    }

    /**
     * Creates a DataStorage object for write purposes.
     *
     * @param feedDefinition the definition of the data source
     * @param conn           a connection with an existing transaction open
     * @return the new DataStorage object for writing
     * @throws java.sql.SQLException if something goes wrong
     */

    public static DataStorage writeConnection(FeedDefinition feedDefinition, Connection conn) throws SQLException {
        return writeConnection(feedDefinition, conn, SecurityUtil.getAccountID(), false);
    }

    public static IDataStorage writeConnection(FeedDefinition feedDefinition, Connection conn, boolean systemUpdate) throws SQLException {
        return writeConnection(feedDefinition, conn, 0, systemUpdate);
    }

    public static DataStorage writeConnection(FeedDefinition feedDefinition, Connection conn, long accountID) throws SQLException {
        return writeConnection(feedDefinition, conn, accountID, false);
    }

    public static TempStorage tempConnection(FeedDefinition feedDefinition, Connection conn) {
        Map<Key, KeyMetadata> keyMetadatas = new HashMap<Key, KeyMetadata>();
        for (AnalysisItem analysisItem : feedDefinition.getFields()) {
            if (analysisItem.isDerived() || !analysisItem.isConcrete()) {
                continue;
            }
            Key key = analysisItem.getKey();
            if (analysisItem.hasType(AnalysisItemTypes.DATE_DIMENSION)) {
                keyMetadatas.put(key, new KeyMetadata(key, Value.DATE, analysisItem));
            } else if (analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
                keyMetadatas.put(key, new KeyMetadata(key, Value.NUMBER, analysisItem));
            } else if (analysisItem.hasType(AnalysisItemTypes.TEXT)) {
                keyMetadatas.put(key, new KeyMetadata(key, Value.TEXT, analysisItem));
            } else {
                keyMetadatas.put(key, new KeyMetadata(key, Value.STRING, analysisItem));
            }
        }
        Database database = DatabaseManager.instance().getDatabase(getMetadata(feedDefinition.getDataFeedID(), conn).getDatabase());
        return new TempStorage(feedDefinition.getDataFeedID(), keyMetadatas, database);
    }

    public static DataStorage writeConnection(FeedDefinition feedDefinition, Connection conn, long accountID, boolean systemUpdate) throws SQLException {
        DataStorage dataStorage = new DataStorage();
        dataStorage.accountID = accountID;
        dataStorage.systemUpdate = systemUpdate;
        Map<Key, KeyMetadata> keyMetadatas = new HashMap<Key, KeyMetadata>();
        for (AnalysisItem analysisItem : feedDefinition.getFields()) {
            if (analysisItem.isDerived() || !analysisItem.isConcrete()) {
                continue;
            }
            Key key = analysisItem.getKey();
            if (analysisItem.hasType(AnalysisItemTypes.DATE_DIMENSION)) {
                keyMetadatas.put(key, new KeyMetadata(key, Value.DATE, analysisItem));
            } else if (analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
                keyMetadatas.put(key, new KeyMetadata(key, Value.NUMBER, analysisItem));
            } else if (analysisItem.hasType(AnalysisItemTypes.TEXT)) {
                keyMetadatas.put(key, new KeyMetadata(key, Value.TEXT, analysisItem));
            } else {
                keyMetadatas.put(key, new KeyMetadata(key, Value.STRING, analysisItem));
            }
        }
        dataStorage.metadata = getMetadata(feedDefinition.getDataFeedID(), conn);
        dataStorage.tableDefined = dataStorage.metadata != null;
        if (dataStorage.metadata == null) {
            dataStorage.metadata = createDefaultMetadata(conn);
        }
        dataStorage.keys = keyMetadatas;
        dataStorage.dataSourceType = feedDefinition.getFeedType().getType();
        dataStorage.feedID = feedDefinition.getDataFeedID();
        dataStorage.version = dataStorage.metadata.getVersion();
        dataStorage.coreDBConn = conn;
        dataStorage.database = DatabaseManager.instance().getDatabase(dataStorage.metadata.getDatabase());
        if (dataStorage.database == null) {
            throw new DatabaseShardException();
        }
        dataStorage.storageConn = dataStorage.database.getConnection();
        dataStorage.storageConn.setAutoCommit(false);
        return dataStorage;
    }

    private boolean tableDefined;

    public boolean isTableDefined() {
        return tableDefined;
    }

    public FeedPersistenceMetadata getMetadata() {
        return metadata;
    }

    private void validateSpace(Connection conn) throws SQLException, StorageLimitException {
        if (systemUpdate) return;
        PreparedStatement dsStmt = conn.prepareStatement("SELECT data_feed.data_feed_id, data_feed.parent_source_id FROM data_feed, upload_policy_users, user WHERE " +
                "data_feed.data_feed_id = upload_policy_users.feed_id and data_feed.visible = ? and upload_policy_users.user_id = user.user_id and " +
                "user.account_id = ? and upload_policy_users.role = ?");
        dsStmt.setBoolean(1, true);
        dsStmt.setLong(2, accountID);
        dsStmt.setInt(3, Roles.OWNER);
        ResultSet dsRS = dsStmt.executeQuery();
        PreparedStatement sizeStmt = conn.prepareStatement("SELECT size FROM feed_persistence_metadata WHERE feed_id = ?");
        long size = 0;
        while (dsRS.next()) {
            sizeStmt.setLong(1, dsRS.getLong(1));
            ResultSet sizeRS = sizeStmt.executeQuery();
            if (sizeRS.next()) {
                size += sizeRS.getLong(1);
            }
        }
        dsStmt.close();
        sizeStmt.close();
        PreparedStatement spaceAllowed = conn.prepareStatement("SELECT ACCOUNT_TYPE FROM ACCOUNT WHERE account_id = ?");
        spaceAllowed.setLong(1, accountID);
        ResultSet spaceRS = spaceAllowed.executeQuery();
        spaceRS.next();
        int accountType = spaceRS.getInt(1);
        long allowed;
        if (accountType == Account.ENTERPRISE) {
            allowed = Account.ENTERPRISE_MAX;
        } else if (accountType == Account.PREMIUM) {
            allowed = Account.PREMIUM_MAX;
        } else if (accountType == Account.BASIC) {
            allowed = Account.BASIC_MAX;
        } else if (accountType == Account.PLUS) {
            allowed = Account.PLUS_MAX;
        } else if (accountType == Account.PERSONAL) {
            allowed = Account.PERSONAL_MAX;
        } else if (accountType == Account.PROFESSIONAL) {
            allowed = Account.PROFESSIONAL_MAX;
        } else if (accountType == Account.ADMINISTRATOR) {
            allowed = Account.ADMINISTRATOR_MAX;           
        } else {
            throw new RuntimeException("Unknown account type");
        }
        if (size > allowed) {
            long mb = allowed / 1000000L;
            throw new ReportException(new StorageLimitFault("Retrieval of data for this data source has exceeded your account's storage limit of " + mb + " mb. You need to reduce the size of the data, clean up other data sources on the account, or upgrade to a higher account tier."));
        }
        spaceAllowed.close();
    }

    public int getVersion() {
        return version;
    }

    public static void delete(long feedID, Connection conn) throws SQLException {
        FeedPersistenceMetadata metadata = getMetadata(feedID, conn);
        if (metadata != null) {
            deleteMetadata(feedID, conn);
            if (DatabaseManager.instance().getDatabase(metadata.getDatabase()) != null) {
                String dropSQL = "DROP TABLE " + "df" + feedID + "v" + metadata.getVersion();
                Connection storageConn = DatabaseManager.instance().getDatabase(metadata.getDatabase()).getConnection();
                try {
                    PreparedStatement dropTableStmt = storageConn.prepareStatement(dropSQL);
                    dropTableStmt.execute();
                } catch (SQLException se) {
                    if (se.getMessage().contains("Unknown table")) {
                        LogClass.error("Data source " + feedID + " did not have a storage table. Continuing with delete, screwed up data.");
                    } else {
                        throw se;
                    }
                } finally {
                    storageConn.close();
                }
            }
        }
    }

    public long calculateSize() throws SQLException {
        PreparedStatement countStmt = storageConn.prepareStatement("SHOW TABLE STATUS LIKE ?");
        countStmt.setString(1, getTableName());
        ResultSet countRS = countStmt.executeQuery();
        if (countRS.next()) {
            long dataLength = countRS.getLong("Data_length");
            long indexLength = countRS.getLong("Index_length");
            if (dataSourceType == FeedType.CONSTANT_CONTACT_CONTACTS.getType() || dataSourceType == FeedType.HIGHRISE_CONTACTS.getType()
                    || dataSourceType == FeedType.HIGHRISE_CONTACT_NOTES.getType() || dataSourceType == FeedType.HIGHRISE_CASE_NOTES.getType() ||
                    dataSourceType == FeedType.HIGHRISE_CONTACT_NOTES.getType() || dataSourceType == FeedType.HIGHRISE_DEAL_NOTES.getType() ||
                    dataSourceType == FeedType.HIGHRISE_EMAILS.getType() || dataSourceType == FeedType.BASECAMP_TIME.getType() ||
                    dataSourceType == FeedType.BASECAMP.getType() || dataSourceType == FeedType.CONSTANT_CONTACT_CONTACT_TO_CONTACT_LIST.getType() ||
                    dataSourceType == FeedType.CONSTANT_CONTACT_CAMPAIGN_RESULTS.getType()) {
                return dataLength;
            }
            return dataLength + indexLength;
        } else {
            return 0;
        }
    }

    public void createTable() throws SQLException {
        ResultSet existsRS = storageConn.getMetaData().getTables(null, null, getTableName(), null);
        if (existsRS.next()) {
            storageConn.prepareStatement("DROP TABLE " + getTableName()).execute();
        }
        try {
            String sql = defineTableSQL(false);
            PreparedStatement createSQL = storageConn.prepareStatement(sql);
            createSQL.execute();
        } catch (SQLException e) {
            if (e.getMessage().contains("Row size too large")) {
                String sql = defineTableSQL(true);
                PreparedStatement createSQL = storageConn.prepareStatement(sql);
                createSQL.execute();
            }
        }
    }

    public void commit() throws SQLException {
        long size = calculateSize();
        metadata.setSize(size);
        metadata.setLastData(new Date());
        if (coreDBConn == null) {
            Connection conn = Database.instance().getConnection();
            try {
                addOrUpdateMetadata(feedID, metadata, conn);
            } finally {
                Database.closeConnection(conn);
            }
        } else {
            addOrUpdateMetadata(feedID, metadata, coreDBConn);
            validateSpace(coreDBConn);
        }
        storageConn.commit();
        try {
            if (reportCache != null) reportCache.remove(feedID);
        } catch (CacheException e) {
            LogClass.error(e);
        }
        committed = true;
    }

    public Connection getStorageConn() {
        return storageConn;
    }

    public void rollback() {
        try {
            if (!committed) {
                storageConn.rollback();
            }
        } catch (SQLException e) {
            LogClass.error(e);
        }
    }

    public void closeConnection() {
        try {
            storageConn.setAutoCommit(true);
        } catch (SQLException e) {
            LogClass.error(e);
        }
        Database.closeConnection(storageConn);
        storageConn = null;
    }

    public void insertFromSelect(String tempTable) throws SQLException {
        StringBuilder columnBuilder = new StringBuilder();
        StringBuilder selectBuilder = new StringBuilder();
        Iterator<KeyMetadata> keyIter = keys.values().iterator();
        while (keyIter.hasNext()) {
            KeyMetadata keyMetadata = keyIter.next();
            columnBuilder.append(keyMetadata.createInsertClause());
            selectBuilder.append(keyMetadata.createInsertClause());
            if (keyIter.hasNext()) {
                columnBuilder.append(",");
                selectBuilder.append(",");
            }
        }
        String columns = columnBuilder.toString();
        String insertSQL = "INSERT INTO " + getTableName() + " (" + columns + ") SELECT " + selectBuilder.toString() + " FROM " + tempTable;
        PreparedStatement insertStmt = storageConn.prepareStatement(insertSQL);
        insertStmt.execute();
        insertStmt.close();
        PreparedStatement dropStmt = storageConn.prepareStatement("DROP TABLE " + tempTable);
        dropStmt.execute();
    }

    public void updateFromTemp(String tempTable, Key updateKey) throws SQLException {
        StringBuilder columnBuilder = new StringBuilder();
        StringBuilder selectBuilder = new StringBuilder();
        Iterator<KeyMetadata> keyIter = keys.values().iterator();
        while (keyIter.hasNext()) {
            KeyMetadata keyMetadata = keyIter.next();
            columnBuilder.append(keyMetadata.createInsertClause());
            selectBuilder.append(keyMetadata.createInsertClause());
            if (keyIter.hasNext()) {
                columnBuilder.append(",");
                selectBuilder.append(",");
            }
        }
        String columns = columnBuilder.toString();
        String insertSQL = "INSERT INTO " + getTableName() + " (" + columns + ") SELECT " + selectBuilder.toString() + " FROM " + tempTable + " WHERE update_key_field = ?";
        PreparedStatement insertStmt = storageConn.prepareStatement(insertSQL);
        PreparedStatement getKeysStmt = storageConn.prepareStatement("SELECT DISTINCT UPDATE_KEY_FIELD FROM " + tempTable);
        ResultSet keyRS = getKeysStmt.executeQuery();
        String updateSQL = "DELETE FROM " + getTableName() + " WHERE " + updateKey.toSQL() + " = ?";
        PreparedStatement updateStmt = storageConn.prepareStatement(updateSQL);
        while (keyRS.next()) {
            String key = keyRS.getString(1);
            updateStmt.setString(1, key);
            updateStmt.executeUpdate();
            insertStmt.setString(1, key);
            insertStmt.execute();
        }
        PreparedStatement dropStmt = storageConn.prepareStatement("DROP TABLE " + tempTable);
        dropStmt.execute();
    }


    private static class FieldMigration {
        // private int previousType;
        private int newType;
        private Key key;

        public FieldMigration(AnalysisItem newItem) {
            key = newItem.getKey();
            /*if (previousItem.hasType(AnalysisItemTypes.DATE_DIMENSION)) {
                previousType = Value.DATE;
            } else if (previousItem.hasType(AnalysisItemTypes.MEASURE)) {
                previousType = Value.NUMBER;
            } else {
                previousType = Value.STRING;
            } */
            if (newItem.hasType(AnalysisItemTypes.DATE_DIMENSION)) {
                newType = Value.DATE;
            } else if (newItem.hasType(AnalysisItemTypes.MEASURE)) {
                newType = Value.NUMBER;
            } else {
                newType = Value.STRING;
            }
        }
    }

    public int migrate(List<AnalysisItem> previousItems, List<AnalysisItem> newItems) throws Exception {
        return migrate(previousItems, newItems, true);
    }

    public int migrate(List<AnalysisItem> previousItems, List<AnalysisItem> newItems, final boolean migrateData) throws Exception {
        // did any items change in a way that requires us to migrate...
        List<FieldMigration> fieldMigrations = new ArrayList<FieldMigration>();
        boolean newFieldsFound = false;
        for (AnalysisItem newItem : newItems) {
            if (newItem.isDerived() || !newItem.isConcrete()) {
                continue;
            }
            boolean newKey = true;
            for (AnalysisItem previousItem : previousItems) {
                if (newItem.getKey().equals(previousItem.getKey())) {
                    // matched the item...
                    newKey = false;
                    if ((newItem.hasType(AnalysisItemTypes.DATE_DIMENSION) && !previousItem.hasType(AnalysisItemTypes.DATE_DIMENSION)) ||
                            (newItem.hasType(AnalysisItemTypes.MEASURE) && !previousItem.hasType(AnalysisItemTypes.MEASURE)) ||
                            (newItem.hasType(AnalysisItemTypes.DIMENSION) && previousItem.hasType(AnalysisItemTypes.MEASURE)) ||
                            (newItem.hasType(AnalysisItemTypes.TEXT) && !previousItem.hasType(AnalysisItemTypes.TEXT))) {
                        fieldMigrations.add(new FieldMigration(newItem));
                    }
                }
            }
            if (newKey) {
                newFieldsFound = true;
            }
        }



        if (newFieldsFound || !fieldMigrations.isEmpty()) {

            int previousVersion = metadata.getVersion();
            this.version = previousVersion + 1;
            this.metadata.setVersion(this.version);
            List<AnalysisItem> previousKeys = new ArrayList<AnalysisItem>();
            for (AnalysisItem previousItem : previousItems) {
                if (!previousItem.isDerived() && previousItem.isConcrete()) {
                    previousKeys.add(previousItem);
                }
            }
            DataSet existing = null;
            Map<Key, KeyMetadata> keyMetadatas;
            keyMetadatas = new HashMap<Key, KeyMetadata>();
            Map<Key, Key> keyMap = new HashMap<Key, Key>();
            for (AnalysisItem analysisItem : previousItems) {
                if (analysisItem.isDerived()) {
                    continue;
                }
                Key key = analysisItem.createAggregateKey(false);
                keyMap.put(key.toBaseKey(), key);
                if (analysisItem.hasType(AnalysisItemTypes.DATE_DIMENSION)) {
                    keyMetadatas.put(key, new KeyMetadata(key, Value.DATE, analysisItem));
                } else if (analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
                    keyMetadatas.put(key, new KeyMetadata(key, Value.NUMBER, analysisItem));
                } else if (analysisItem.hasType(AnalysisItemTypes.TEXT)) {
                    keyMetadatas.put(key, new KeyMetadata(key, Value.TEXT, analysisItem));
                } else {
                    keyMetadatas.put(key, new KeyMetadata(key, Value.STRING, analysisItem));
                }
            }
            if (migrateData) {
                if (previousKeys.isEmpty()) {
                    existing = new DataSet();
                } else {


                    InsightRequestMetadata insightRequestMetadata = new InsightRequestMetadata();
                    insightRequestMetadata.setNow(new Date());
                    insightRequestMetadata.setAggregateQuery(false);
                    try {
                        existing = retrieveData(previousKeys, null, 0, keyMetadatas, previousVersion, insightRequestMetadata);
                    } catch (SQLException e) {
                        LogClass.error(e);
                        existing = new DataSet();
                    }
                    //existing = new DataSet();
                }
            }

            ResultSet existsRS = storageConn.getMetaData().getTables(null, null, getTableName(), null);
            if (existsRS.next()) {
                storageConn.prepareStatement("DROP TABLE " + getTableName()).execute();
            }
            String sql = defineTableSQL(false);
            LogClass.info("Creating new storage table in migration with sql " + sql);
            PreparedStatement createSQL = storageConn.prepareStatement(sql);
            createSQL.execute();

            if (migrateData) {
                for (FieldMigration fieldMigration : fieldMigrations) {
                    for (IRow row : existing.getRows()) {
                        Key key = keyMap.get(fieldMigration.key);
                        Value existingValue = row.getValue(key);
                        String string = existingValue.toString();
                        if (fieldMigration.newType == Value.DATE) {
                        } else if (fieldMigration.newType == Value.NUMBER) {
                            double doubleValue = NumericValue.produceDoubleValue(string);
                            row.addValue(fieldMigration.key, new NumericValue(doubleValue));
                        } else {
                            if (existingValue.type() == Value.NUMBER) {
                                string = ((Long) ((NumericValue) existingValue).getValue().longValue()).toString();
                            }
                            row.addValue(fieldMigration.key, new StringValue(string));
                        }
                    }
                }
                DataSet mirror = new DataSet();
                for (IRow row : existing.getRows()) {
                    IRow mirrorRow = mirror.createRow();
                    for (Key key : row.getKeys()) {
                        mirrorRow.addValue(key.toBaseKey(), row.getValue(key));
                    }
                }
                insertData(mirror);
            }

            try {
                String dropSQL = "DROP TABLE " + "df" + feedID + "v" + previousVersion;
                PreparedStatement dropTableStmt = storageConn.prepareStatement(dropSQL);
                dropTableStmt.execute();
            } catch (SQLException e) {
                LogClass.error(e);
            }
        }
        return this.version;
    }

    /**
     * Clears out all data in the data source.
     *
     * @throws java.sql.SQLException if something goes wrong
     */

    public void truncate() throws SQLException {
        PreparedStatement truncateStmt = storageConn.prepareStatement("TRUNCATE " + getTableName());
        truncateStmt.execute();
    }

    /**
     * Retrieves the requested data set from the database.
     *
     * @param reportItems    the analysis items you're looking to retrieve
     * @param filters        any filter definitions you want to constrain data by
     * @param limit          optional limit on result set
     * @return the created data set
     * @throws java.sql.SQLException if something goes wrong
     */

    public DataSet retrieveData(@NotNull Collection<AnalysisItem> reportItems, @Nullable Collection<FilterDefinition> filters, @Nullable Integer limit) throws SQLException {
        return retrieveData(reportItems, filters, limit, null, version, null);
    }

    /**
     * Retrieves the requested data set from the database.
     *
     * @param reportItems            the analysis items you're looking to retrieve
     * @param filters                any filter definitions you want to constrain data by
     * @param limit                  optional limit on result set
     * @param insightRequestMetadata the request metadata
     * @return the created data set
     * @throws java.sql.SQLException if something goes wrong
     */

    public DataSet retrieveData(Collection<AnalysisItem> reportItems, Collection<FilterDefinition> filters, Integer limit,
                                InsightRequestMetadata insightRequestMetadata) throws SQLException {
        return retrieveData(reportItems, filters, limit, null, version, insightRequestMetadata);
    }

    private DataSet retrieveData(@NotNull Collection<AnalysisItem> reportItems, @Nullable Collection<FilterDefinition> filters, @Nullable Integer limit,
                                 @Nullable Map<Key, KeyMetadata> keys, int version, @Nullable InsightRequestMetadata insightRequestMetadata) throws SQLException {
        if (insightRequestMetadata == null) {
            insightRequestMetadata = new InsightRequestMetadata();
            insightRequestMetadata.setNow(new Date());
        }
        reportItems = new ArrayList<AnalysisItem>(reportItems);
        boolean countDistinct = false;
        Set<String> keyStrings = new HashSet<String>();

        for (Key key : this.keys.keySet()) {
            keyStrings.add(key.toSQL());
        }

        
        if (keys == null) {
            keys = new HashMap<Key, KeyMetadata>();
            Iterator<AnalysisItem> iter = reportItems.iterator();
            while (iter.hasNext()) {
                AnalysisItem analysisItem = iter.next();
                if (analysisItem.isDerived()) {
                    iter.remove();
                    continue;
                }
                Key key = analysisItem.createAggregateKey(false);
                if (!keyStrings.contains(key.toSQL())) {
                    iter.remove();
                    continue;
                }
                if (analysisItem.hasType(AnalysisItemTypes.DATE_DIMENSION)) {
                    keys.put(key, new KeyMetadata(key, Value.DATE, analysisItem));
                } else if (analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
                    AnalysisMeasure analysisMeasure = (AnalysisMeasure) analysisItem;
                    if (analysisMeasure.getAggregation() == AggregationTypes.COUNT_DISTINCT) {
                        countDistinct = true;
                        keys.put(key, new KeyMetadata(key, Value.STRING, analysisItem));
                    } else {
                        keys.put(key, new KeyMetadata(key, Value.NUMBER, analysisItem));
                    }
                } else if (analysisItem.hasType(AnalysisItemTypes.TEXT)) {
                    keys.put(key, new KeyMetadata(key, Value.TEXT, analysisItem));
                } else {
                    if (analysisItem.hasType(AnalysisItemTypes.DIMENSION)) {
                        AnalysisDimension analysisDimension = (AnalysisDimension) analysisItem;
                        countDistinct = countDistinct || !analysisDimension.isGroup();
                    }
                    keys.put(key, new KeyMetadata(key, Value.STRING, analysisItem));
                }
            }
        }
        if (reportItems.isEmpty()) {
            return new DataSet();
        }
        filters = eligibleFilters(filters, keyStrings);
        StringBuilder queryBuilder = new StringBuilder();
        StringBuilder selectBuilder = new StringBuilder();
        StringBuilder fromBuilder = new StringBuilder();
        StringBuilder whereBuilder = new StringBuilder();
        StringBuilder groupByBuilder = new StringBuilder();
        Collection<Key> groupByItems = new HashSet<Key>();
        boolean aggregateQuery = insightRequestMetadata.isAggregateQuery() && !countDistinct;
        createSelectClause(reportItems, selectBuilder, groupByBuilder, aggregateQuery, insightRequestMetadata.isOptimized());
        selectBuilder = selectBuilder.deleteCharAt(selectBuilder.length() - 1);
        createFromClause(version, fromBuilder);
        createWhereClause(filters, whereBuilder);
        createSQL(filters, limit, queryBuilder, selectBuilder, fromBuilder, whereBuilder, groupByBuilder, groupByItems);
        PreparedStatement queryStmt = storageConn.prepareStatement(queryBuilder.toString());
        populateParameters(filters, keys, queryStmt, insightRequestMetadata);
        DataSet dataSet = new DataSet();
        ResultSet dataRS = queryStmt.executeQuery();
        processQueryResults(reportItems, keys, dataSet, dataRS, aggregateQuery, insightRequestMetadata.isOptimized());
        dataSet.setLastTime(metadata.getLastData());
        return dataSet;
    }

    @NotNull
    private Collection<FilterDefinition> eligibleFilters(@Nullable Collection<FilterDefinition> filters, Set<String> keyStrings) {
        Collection<FilterDefinition> eligibleFilters = new ArrayList<FilterDefinition>();
        if (filters != null) {
            for (FilterDefinition filterDefinition : filters) {
                if (filterDefinition.isApplyBeforeAggregation() && filterDefinition.validForQuery()) {
                    if (filterDefinition.getField() != null) {
                        if (!keyStrings.contains(filterDefinition.getField().getKey().toSQL())) {
                            continue;
                        }
                    }
                    eligibleFilters.add(filterDefinition);
                }
            }
        }
        return eligibleFilters;
    }

    private void processQueryResults(@NotNull Collection<AnalysisItem> reportItems, @NotNull Map<Key, KeyMetadata> keys, @NotNull DataSet dataSet, @NotNull ResultSet dataRS,
                                     boolean aggregateQuery, boolean optimized) throws SQLException {
        while (dataRS.next()) {
            IRow row = dataSet.createRow();
            int i = 1;
            for (AnalysisItem analysisItem : reportItems) {
                Key key = analysisItem.createAggregateKey(false);
                AggregateKey aggregateKey = analysisItem.createAggregateKey();
                KeyMetadata keyMetadata = keys.get(key);
                if (keyMetadata != null) {
                    if (keyMetadata.getType() == Value.DATE) {
                        AnalysisDateDimension date = (AnalysisDateDimension) analysisItem;
                        if (optimized && (date.getDateLevel() == AnalysisDateDimension.MONTH_FLAT || date.getDateLevel() == AnalysisDateDimension.MONTH_LEVEL)) {
                            int month = dataRS.getInt(i++);
                            int year = dataRS.getInt(i++);
                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.DAY_OF_MONTH, 2);
                            cal.set(Calendar.MONTH, month - 1);
                            cal.set(Calendar.YEAR, year);
                            row.addValue(aggregateKey, new DateValue(cal.getTime()));
                        } else {
                            try {
                                Timestamp time = dataRS.getTimestamp(i++);
                                if (dataRS.wasNull()) {
                                    row.addValue(aggregateKey, new EmptyValue());
                                } else {
                                    long milliseconds = time.getTime();
                                    row.addValue(aggregateKey, new DateValue(new Date(milliseconds)));
                                }
                            } catch (SQLException e) {
                                row.addValue(aggregateKey, new EmptyValue());
                            }
                        }
                    } else if (keyMetadata.getType() == Value.NUMBER) {
                        try {
                            double value = dataRS.getDouble(i++);
                            if (dataRS.wasNull()) {
                                row.addValue(aggregateKey, new EmptyValue());
                            } else {
                                NumericValue numericValue = new NumericValue(value);
                                if (aggregateQuery && analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
                                    AnalysisMeasure analysisMeasure = (AnalysisMeasure) analysisItem;
                                    if (analysisMeasure.getAggregation() == AggregationTypes.COUNT) {
                                        CountAggregation countAggregation = new CountAggregation();
                                        countAggregation.setCount(value);
                                        numericValue.setAggregation(countAggregation);
                                    }
                                }
                                row.addValue(aggregateKey, numericValue);
                            }
                        } catch (SQLException e) {
                            row.addValue(aggregateKey, new EmptyValue());
                        }
                    } else {
                        try {
                            String value = dataRS.getString(i++);
                            if (dataRS.wasNull()) {
                                row.addValue(aggregateKey, new EmptyValue());
                            } else {
                                row.addValue(aggregateKey, new StringValue(value));
                            }
                        } catch (SQLException e) {
                            row.addValue(aggregateKey, new EmptyValue());
                        }
                    }
                }
            }
        }
    }

    private void populateParameters(@NotNull Collection<FilterDefinition> filters, @NotNull Map<Key, KeyMetadata> keys, @NotNull PreparedStatement queryStmt, @NotNull InsightRequestMetadata insightRequestMetadata) throws SQLException {
        if (filters.size() > 0) {
            int i = 1;
            for (FilterDefinition filterDefinition : filters) {
                KeyMetadata keyMetadata = keys.get(filterDefinition.getField().createAggregateKey(false));
                if (keyMetadata != null) {
                    int type = keyMetadata.getType();
                    i = filterDefinition.populatePreparedStatement(queryStmt, i, type, insightRequestMetadata);
                } else {
                    int type = Value.STRING;
                    i = filterDefinition.populatePreparedStatement(queryStmt, i, type, insightRequestMetadata);
                }
            }
        }
    }

    private void createSQL(@NotNull Collection<FilterDefinition> filters, @Nullable Integer limit, @NotNull StringBuilder queryBuilder, @NotNull StringBuilder selectBuilder,
                           @NotNull StringBuilder fromBuilder, @NotNull StringBuilder whereBuilder, @NotNull StringBuilder groupByBuilder, @NotNull Collection<Key> groupByItems) {
        queryBuilder.append("SELECT ");
        queryBuilder.append(selectBuilder.toString());
        queryBuilder.append(" FROM ");
        queryBuilder.append(fromBuilder.toString());
        if (filters.size() > 0) {
            queryBuilder.append(" WHERE ");
            queryBuilder.append(whereBuilder.toString());
        }
        if (groupByBuilder.length() > 0) {
            queryBuilder.append(" GROUP BY ");
            queryBuilder.append(groupByBuilder.toString());
        }
        if (limit != null && limit > 0) {
            queryBuilder.append(" LIMIT ").append(limit);
        }
    }

    private StringBuilder createGroupByClause(StringBuilder groupByBuilder, Collection<Key> groupByItems) {
        if (groupByItems.size() > 0) {
            for (Key key : groupByItems) {
                String columnName = key.toSQL();
                groupByBuilder.append("binary(" + columnName + ")");
                groupByBuilder.append(",");
            }
            groupByBuilder = groupByBuilder.deleteCharAt(groupByBuilder.length() - 1);
        }
        return groupByBuilder;
    }

    private void createWhereClause(Collection<FilterDefinition> filters, StringBuilder whereBuilder) {
        if (filters != null && filters.size() > 0) {
            Iterator<FilterDefinition> filterIter = filters.iterator();
            while (filterIter.hasNext()) {
                FilterDefinition filterDefinition = filterIter.next();
                whereBuilder.append(filterDefinition.toQuerySQL(getTableName()));
                if (filterIter.hasNext()) {
                    whereBuilder.append(" AND ");
                }
            }
        }
    }

    private void createFromClause(int version, StringBuilder fromBuilder) {
        String tableName = "df" + feedID + "v" + version;
        fromBuilder.append(tableName);
    }

    private void addAdditionalKeysToSelect(Collection<Key> additionalKeys, StringBuilder selectBuilder, Collection<Key> groupByItems) {
        if (additionalKeys != null) {
            for (Key key : additionalKeys) {
                String columnName = key.toSQL();
                selectBuilder.append(columnName);
                selectBuilder.append(",");
                groupByItems.add(key);
            }
        }
    }

    private void createSelectClause(Collection<AnalysisItem> reportItems, StringBuilder selectBuilder, StringBuilder groupByBuilder, boolean aggregateQuery, boolean optimized) {
        for (AnalysisItem analysisItem : reportItems) {
            if (analysisItem.isDerived()) {
                throw new RuntimeException("Attempt made to query derived analysis item " + analysisItem.toDisplay() + " of class " + analysisItem.getClass().getName());
            }
            
            String columnName = analysisItem.toKeySQL();
            if (analysisItem.hasType(AnalysisItemTypes.MEASURE) && aggregateQuery) {
                AnalysisMeasure analysisMeasure = (AnalysisMeasure) analysisItem;
                int aggregation = analysisMeasure.getQueryAggregation();
                if (aggregation == AggregationTypes.SUM) {
                    columnName = "SUM(" + columnName + ")";
                } else if (aggregation == AggregationTypes.AVERAGE) {
                    columnName = "AVG(" + columnName + ")";
                } else if (aggregation == AggregationTypes.COUNT) {
                    columnName = "COUNT(DISTINCT " + columnName + ")";
                } else if (aggregation == AggregationTypes.MAX) {
                    columnName = "MAX(" + columnName + ")";
                } else if (aggregation == AggregationTypes.MIN) {
                    columnName = "MIN(" + columnName + ")";
                } else {
                    groupByBuilder.append("binary(" + columnName + ")");
                    groupByBuilder.append(",");
                }
                selectBuilder.append(columnName);
                selectBuilder.append(",");
            } else if (analysisItem.hasType(AnalysisItemTypes.DATE_DIMENSION) && aggregateQuery) {
                AnalysisDateDimension date = (AnalysisDateDimension) analysisItem;
                if (optimized && (date.getDateLevel() == AnalysisDateDimension.MONTH_FLAT || date.getDateLevel() == AnalysisDateDimension.MONTH_LEVEL)) {
                    selectBuilder.append("month(" + columnName + ") as month" + columnName + ", year(" + columnName + ") as year" + columnName + ",");
                    groupByBuilder.append("month" + columnName + ", year" + columnName + ",");
                } else {
                    selectBuilder.append(columnName);
                    selectBuilder.append(",");
                    groupByBuilder.append("binary(" + columnName + ")");
                    groupByBuilder.append(",");
                }
            } else {
                if (aggregateQuery) {
                    groupByBuilder.append("binary(" + columnName + ")");
                    groupByBuilder.append(",");
                }
                selectBuilder.append(columnName);
                selectBuilder.append(",");
            }

        }
        if (groupByBuilder.length() > 0) {
            groupByBuilder.deleteCharAt(groupByBuilder.length() - 1);
        }
    }

    public void insertData(DataSet dataSet) throws Exception {
        insertData(dataSet, keys);
    }

    public void insertData(DataSet dataSet, Map<Key, KeyMetadata> keys) throws Exception {
        StringBuilder columnBuilder = new StringBuilder();
        StringBuilder paramBuilder = new StringBuilder();
        Iterator<KeyMetadata> keyIter = keys.values().iterator();
        while (keyIter.hasNext()) {
            KeyMetadata keyMetadata = keyIter.next();
            columnBuilder.append(keyMetadata.createInsertClause());
            //columnBuilder.append("k").append(keyMetadata.key.getKeyID());
            //paramBuilder.append("?");
            paramBuilder.append(keyMetadata.createInsertQuestionMarks());
            if (keyIter.hasNext()) {
                columnBuilder.append(",");
                paramBuilder.append(",");
            }
        }
        String columns = columnBuilder.toString();
        String parameters = paramBuilder.toString();
        String insertSQL = "INSERT INTO " + getTableName() + " (" + columns + ") VALUES (" + parameters + ")";
        PreparedStatement insertStmt = storageConn.prepareStatement(insertSQL);
        try {
            for (IRow row : dataSet.getRows()) {
                int i = 1;
                for (KeyMetadata keyMetadata : keys.values()) {
                    i = setValue(insertStmt, row, i, keyMetadata);
                }
                insertStmt.execute();
            }
            insertStmt.close();
        } catch (Exception e) {
            LogClass.error("Failure on persistence where SQL = " + insertSQL + ", database = " + database.getID() + ", data set = " + dataSet);
            throw e;
        }
    }

    public void insertData(IRow row, Map<Key, KeyMetadata> keys) throws Exception {
        StringBuilder columnBuilder = new StringBuilder();
        StringBuilder paramBuilder = new StringBuilder();
        Iterator<KeyMetadata> keyIter = keys.values().iterator();
        while (keyIter.hasNext()) {
            KeyMetadata keyMetadata = keyIter.next();
            columnBuilder.append(keyMetadata.createInsertClause());
            //columnBuilder.append("k").append(keyMetadata.key.getKeyID());
            //paramBuilder.append("?");
            paramBuilder.append(keyMetadata.createInsertQuestionMarks());
            if (keyIter.hasNext()) {
                columnBuilder.append(",");
                paramBuilder.append(",");
            }
        }
        String columns = columnBuilder.toString();
        String parameters = paramBuilder.toString();
        String insertSQL = "INSERT INTO " + getTableName() + " (" + columns + ") VALUES (" + parameters + ")";
        PreparedStatement insertStmt = storageConn.prepareStatement(insertSQL);
        try {

            int i = 1;
            for (KeyMetadata keyMetadata : keys.values()) {
                i = setValue(insertStmt, row, i, keyMetadata);
            }
            insertStmt.execute();

            insertStmt.close();
        } catch (Exception e) {
            LogClass.error("Failure on persistence where SQL = " + insertSQL + ", database = " + database.getID());
            throw e;
        }
    }

    public void updateData(DataSet dataSet, List<IWhere> wheres) throws Exception {
        StringBuilder fieldBuilder = new StringBuilder();
        List<KeyMetadata> updateKeys = new ArrayList<KeyMetadata>();
        for (KeyMetadata keyMetadata : keys.values()) {
            //boolean inWhereClause = false;
            for (IWhere where : wheres) {
                if (where.getKey().equals(keyMetadata.getKey())) {
                    where.getKey().setKeyID(keyMetadata.getKey().getKeyID());
                    //inWhereClause = true;
                }
            }
            //if (!inWhereClause) {
            updateKeys.add(keyMetadata);
            //}
        }
        Iterator<KeyMetadata> keyIter = updateKeys.iterator();
        while (keyIter.hasNext()) {
            KeyMetadata keyMetadata = keyIter.next();
            fieldBuilder.append(keyMetadata.createUpdateClause());
            if (keyIter.hasNext()) {
                fieldBuilder.append(",");
            }
        }

        StringBuilder whereBuilder = new StringBuilder();
        Iterator<IWhere> whereIter = wheres.iterator();
        while (whereIter.hasNext()) {
            IWhere where = whereIter.next();
            whereBuilder.append(where.createWhereSQL());
            if (whereIter.hasNext()) {
                whereBuilder.append(" AND ");
            }
        }
        StringBuilder tableBuilder = new StringBuilder();
        for (IWhere where : wheres) {
            for (String extraTable : where.getExtraTables()) {
                tableBuilder.append(extraTable).append(",");
            }
        }
        tableBuilder.append(getTableName());
        String updateSQL = "DELETE " + getTableName() + " FROM " + tableBuilder.toString() + " WHERE " + whereBuilder.toString();
        PreparedStatement updateStmt = storageConn.prepareStatement(updateSQL);
        int i = 1;
        for (IWhere where : wheres) {
            i = where.setValue(updateStmt, i);
        }
        updateStmt.executeUpdate();
        updateStmt.close();
        dataSet.mergeWheres(wheres);
        insertData(dataSet);
    }

    public void updateData(IRow row, List<IWhere> wheres) throws Exception {
        StringBuilder fieldBuilder = new StringBuilder();
        List<KeyMetadata> updateKeys = new ArrayList<KeyMetadata>();
        for (KeyMetadata keyMetadata : keys.values()) {
            //boolean inWhereClause = false;
            for (IWhere where : wheres) {
                if (where.getKey().equals(keyMetadata.getKey())) {
                    where.getKey().setKeyID(keyMetadata.getKey().getKeyID());
                    //inWhereClause = true;
                }
            }
            //if (!inWhereClause) {
            updateKeys.add(keyMetadata);
            //}
        }
        Iterator<KeyMetadata> keyIter = updateKeys.iterator();
        while (keyIter.hasNext()) {
            KeyMetadata keyMetadata = keyIter.next();
            fieldBuilder.append(keyMetadata.createUpdateClause());
            if (keyIter.hasNext()) {
                fieldBuilder.append(",");
            }
        }

        StringBuilder whereBuilder = new StringBuilder();
        Iterator<IWhere> whereIter = wheres.iterator();
        while (whereIter.hasNext()) {
            IWhere where = whereIter.next();
            whereBuilder.append(where.createWhereSQL());
            if (whereIter.hasNext()) {
                whereBuilder.append(" AND ");
            }
        }
        StringBuilder tableBuilder = new StringBuilder();
        for (IWhere where : wheres) {
            for (String extraTable : where.getExtraTables()) {
                tableBuilder.append(extraTable).append(",");
            }
        }
        tableBuilder.append(getTableName());
        String updateSQL = "DELETE " + getTableName() + " FROM " + tableBuilder.toString() + " WHERE " + whereBuilder.toString();
        PreparedStatement updateStmt = storageConn.prepareStatement(updateSQL);
        int i = 1;
        for (IWhere where : wheres) {
            i = where.setValue(updateStmt, i);
        }
        updateStmt.executeUpdate();
        updateStmt.close();
        for (IWhere where : wheres) {
            if (where.hasConcreteValue()) {
                row.addValue(where.getKey(), where.getConcreteValue());
            }
        }
        insertData(row, keys);
    }

    private int setValue(PreparedStatement insertStmt, IRow row, int i, KeyMetadata keyMetadata) throws SQLException {
        Value value = row.getValue(keyMetadata.getKey());
        if (value == null || value.type() == Value.EMPTY) {
            int sqlType;
            if (keyMetadata.getType() == Value.DATE) {
                sqlType = Types.DATE;
            } else if (keyMetadata.getType() == Value.NUMBER) {
                sqlType = Types.DOUBLE;
            } else {
                sqlType = Types.VARCHAR;
            }
            insertStmt.setNull(i++, sqlType);
            if (keyMetadata.getType() == Value.DATE) {
                insertStmt.setNull(i++, Types.BIGINT);
            }
        } else if (keyMetadata.getType() == Value.DATE) {
            java.util.Date date = null;
            if (value.type() != Value.DATE) {
                AnalysisItem analysisItem = keyMetadata.getAnalysisItem();
                AnalysisDateDimension analysisDateDimension = (AnalysisDateDimension) analysisItem;
                int prevLevel = analysisDateDimension.getDateLevel();
                analysisDateDimension.setDateLevel(AnalysisDateDimension.DAY_LEVEL);
                Value transformedValue = analysisItem.transformValue(value, new InsightRequestMetadata(), false);
                analysisDateDimension.setDateLevel(prevLevel);
                if (transformedValue.type() == Value.EMPTY)
                    insertStmt.setNull(i++, Types.DATE);
                else {
                    DateValue dateValue = (DateValue) transformedValue;
                    date = dateValue.getDate();
                    java.sql.Timestamp sqlDate = new java.sql.Timestamp(date.getTime());
                    insertStmt.setTimestamp(i++, sqlDate);
                }
            } else {
                DateValue dateValue = (DateValue) value;
                date = dateValue.getDate();
                if (date == null) {
                    insertStmt.setNull(i++, Types.DATE);
                } else {
                    java.sql.Timestamp sqlDate = new java.sql.Timestamp(date.getTime());
                    insertStmt.setTimestamp(i++, sqlDate);
                }
            }
            if (date == null) {
                insertStmt.setNull(i++, Types.BIGINT);
            } else {
                long id = dateDimCache.getDateDimID(date, storageConn);
                insertStmt.setLong(i++, id);
            }
        } else if (keyMetadata.getType() == Value.NUMBER) {
            Double num = null;
            if (value.type() == Value.STRING || value.type() == Value.TEXT) {
                num = NumericValue.produceDoubleValue(value.toString());
            } else if (value.type() == Value.NUMBER) {
                NumericValue numericValue = (NumericValue) value;
                num = numericValue.toDouble();
            }
            if (num == null || Double.isNaN(num) || Double.isInfinite(num)) {
                insertStmt.setNull(i++, Types.DOUBLE);
            } else {
                insertStmt.setDouble(i++, num);
            }
        } else {
            String string = null;
            if (value.type() == Value.STRING || value.type() == Value.TEXT) {
                StringValue stringValue = (StringValue) value;
                string = stringValue.getValue();
            } else if (value.type() == Value.NUMBER) {
                NumberFormat nf = NumberFormat.getInstance();
                nf.setGroupingUsed(false);
                string = nf.format(value.toDouble());
            } else if (value.type() == Value.DATE) {
                DateValue dateValue = (DateValue) value;
                string = dateValue.getDate().toString();
            }
            if (string == null) {
                insertStmt.setNull(i++, Types.VARCHAR);
            } else {
                if (keyMetadata.getType() == Value.STRING && string.length() > 253) {
                    string = string.substring(0, 253);
                }
                insertStmt.setString(i++, string);
            }
        }
        return i;
    }

    public void deleteData(List<IWhere> wheres) throws SQLException {
        StringBuilder whereBuilder = new StringBuilder();
        Iterator<IWhere> whereIter = wheres.iterator();
        while (whereIter.hasNext()) {
            IWhere where = whereIter.next();
            whereBuilder.append(where.createWhereSQL());
            if (whereIter.hasNext()) {
                whereBuilder.append(",");
            }
        }

        PreparedStatement deleteStmt = storageConn.prepareStatement("DELETE FROM " + getTableName() + " WHERE " + whereBuilder.toString());
        int i = 1;
        for (IWhere where : wheres) {
            where.setValue(deleteStmt, i++);
        }
        deleteStmt.executeUpdate();
    }

    public String defineTableSQL(boolean hugeTable) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("CREATE TABLE ");
        sqlBuilder.append(getTableName());
        sqlBuilder.append("( ");
        for (KeyMetadata keyMetadata : keys.values()) {
            sqlBuilder.append(getColumnDefinitionSQL(keyMetadata.getKey(), keyMetadata.getType(), hugeTable));
            sqlBuilder.append(",");
        }
        String primaryKey = getTableName() + "_ID";
        sqlBuilder.append(primaryKey);
        sqlBuilder.append(" BIGINT NOT NULL AUTO_INCREMENT,");
        sqlBuilder.append("PRIMARY KEY (");
        sqlBuilder.append(primaryKey);
        sqlBuilder.append("),");
        int indexCount = 0;
        for (KeyMetadata keyMetadata : keys.values()) {
            if (!hugeTable && keyMetadata.getType() == Value.STRING) {
                sqlBuilder.append("INDEX (");
                String column = keyMetadata.getKey().toSQL();
                sqlBuilder.append(column);
                sqlBuilder.append(")");
                sqlBuilder.append(",");
                indexCount++;
            } else if (keyMetadata.getType() == Value.DATE) {
                sqlBuilder.append("INDEX (");
                String column = keyMetadata.getKey().toSQL();
                sqlBuilder.append(column);
                sqlBuilder.append(")");
                sqlBuilder.append(",");
                indexCount++;
            }
            if (keyMetadata.getType() == Value.DATE) {
                sqlBuilder.append("INDEX (");
                String column = "datedim_" + keyMetadata.getKey().getKeyID() + "_id";
                sqlBuilder.append(column);
                sqlBuilder.append(")");
                sqlBuilder.append(",");
                indexCount++;
            }
            if (indexCount >= 60) {
                break;
            }
        }
        if (sqlBuilder.charAt(sqlBuilder.length() - 1) == ',') sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
        sqlBuilder.append(" )");
        //sqlBuilder.append(" ) CHARSET=utf8");
        return sqlBuilder.toString();
    }

    private String getColumnDefinitionSQL(Key key, int type, boolean hugeTable) {
        String column;
        if (type == Value.DATE) {
            column = "k" + key.getKeyID() + " DATETIME, datedim_" + key.getKeyID() + "_id BIGINT(11)";
        } else if (type == Value.NUMBER) {
            column = "k" + key.getKeyID() + " DOUBLE";
        } else if (type == Value.TEXT) {
            column = "k" + key.getKeyID() + " TEXT";
        } else {
            if (hugeTable) {
                column = "k" + key.getKeyID() + " TEXT";
            } else {
                column = "k" + key.getKeyID() + " VARCHAR(255)";
            }
        }
        return column;
    }

    private String getTableName() {
        return "df" + feedID + "v" + version;
    }

    private static void addOrUpdateMetadata(long dataFeedID, FeedPersistenceMetadata metadata, Connection conn) {
        try {
            if (metadata.getMetadataID() > 0) {
                PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM FEED_PERSISTENCE_METADATA WHERE FEED_PERSISTENCE_METADATA_ID = ?");
                deleteStmt.setLong(1, metadata.getMetadataID());
                deleteStmt.executeUpdate();
                deleteStmt.close();
            }

            PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO FEED_PERSISTENCE_METADATA (FEED_ID, " +
                    "VERSION, SIZE, DATABASE_NAME, LAST_DATA_TIME) VALUES (?, ?, ?, ?, ?)");
            insertStmt.setLong(1, dataFeedID);
            insertStmt.setInt(2, metadata.getVersion());
            insertStmt.setLong(3, metadata.getSize());
            insertStmt.setString(4, metadata.getDatabase());
            insertStmt.setTimestamp(5, new Timestamp(metadata.getLastData().getTime()));
            insertStmt.execute();
            insertStmt.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addOrUpdateMetadata(long dataFeedID, FeedPersistenceMetadata metadata) {
        Connection conn = Database.instance().getConnection();
        try {
            addOrUpdateMetadata(dataFeedID, metadata, conn);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public static void liveDataSource(long dataSourceID, Connection conn) {
        FeedPersistenceMetadata metadata = new FeedPersistenceMetadata();
        metadata.setVersion(1);
        metadata.setLastData(new Date());
        addOrUpdateMetadata(dataSourceID, metadata, conn);
    }

    private static FeedPersistenceMetadata createDefaultMetadata(Connection conn) {
        try {
            FeedPersistenceMetadata metadata = new FeedPersistenceMetadata();
            metadata.setVersion(1);
            metadata.setLastData(new Date());
            metadata.setDatabase(DatabaseManager.instance().chooseDatabase(conn));
            return metadata;
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public static FeedPersistenceMetadata getMetadata(long dataFeedID, Connection conn) {
        FeedPersistenceMetadata metadata = null;
        try {
            PreparedStatement versionStmt = conn.prepareStatement("SELECT MAX(VERSION) FROM FEED_PERSISTENCE_METADATA WHERE " +
                    "FEED_ID = ?");
            versionStmt.setLong(1, dataFeedID);
            ResultSet versionRS = versionStmt.executeQuery();
            if (versionRS.next()) {
                long version = versionRS.getLong(1);
                PreparedStatement queryStmt = conn.prepareStatement("SELECT FEED_PERSISTENCE_METADATA_ID, SIZE, VERSION, DATABASE_NAME, LAST_DATA_TIME " +
                        "FROM FEED_PERSISTENCE_METADATA WHERE FEED_ID = ? AND VERSION = ?");
                queryStmt.setLong(1, dataFeedID);
                queryStmt.setLong(2, version);
                ResultSet rs = queryStmt.executeQuery();
                if (rs.next()) {
                    long metadataID = rs.getLong(1);
                    metadata = new FeedPersistenceMetadata();
                    metadata.setMetadataID(metadataID);
                    metadata.setSize(rs.getLong(2));
                    metadata.setVersion(rs.getInt(3));
                    metadata.setDatabase(rs.getString(4));
                    metadata.setLastData(rs.getTimestamp(5));
                }
                queryStmt.close();
            } else {
                throw new RuntimeException("No metadata found for " + dataFeedID);
            }
            versionStmt.close();
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
        return metadata;
    }

    private static void deleteMetadata(long feedID, Connection conn) throws SQLException {
        PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM feed_persistence_metadata WHERE feed_id = ?");
        deleteStmt.setLong(1, feedID);
        deleteStmt.executeUpdate();
        deleteStmt.close();
    }

    public DataSet allData(@NotNull Collection<FilterDefinition> filters, Integer limit) throws SQLException {
        InsightRequestMetadata insightRequestMetadata = new InsightRequestMetadata();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT ");
        for (Map.Entry<Key, KeyMetadata> keyEntry : keys.entrySet()) {
            sqlBuilder.append(keyEntry.getKey().toSQL());
            sqlBuilder.append(",");
        }
        sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
        sqlBuilder.append(" FROM ");
        sqlBuilder.append(getTableName());
        if (filters.size() > 0) {
            sqlBuilder.append(" WHERE ");
            for (FilterDefinition filterDefinition : filters) {
                sqlBuilder.append(filterDefinition.toQuerySQL(getTableName()));
                sqlBuilder.append(",");
            }
            sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
        }
        if (limit != null) {
            sqlBuilder.append(" LIMIT " + limit);
        }
        DataSet dataSet = new DataSet();
        PreparedStatement queryStmt = storageConn.prepareStatement(sqlBuilder.toString());
        int position = 1;
        for (FilterDefinition filterDefinition : filters) {
            KeyMetadata keyMetadata = keys.get(filterDefinition.getField().getKey());
            int type = keyMetadata.getType();
            filterDefinition.populatePreparedStatement(queryStmt, position, type, insightRequestMetadata);
        }
        ResultSet dataRS = queryStmt.executeQuery();
        while (dataRS.next()) {
            int i = 1;
            IRow row = dataSet.createRow();
            for (Map.Entry<Key, KeyMetadata> keyEntry : keys.entrySet()) {
                Key key = keyEntry.getKey();
                KeyMetadata keyMetadata = keyEntry.getValue();
                if (keyMetadata.getType() == Value.DATE) {
                    Timestamp time = dataRS.getTimestamp(i++);
                    if (dataRS.wasNull()) {
                        row.addValue(key, new EmptyValue());
                    } else {
                        row.addValue(key, new DateValue(new Date(time.getTime())));
                    }
                } else if (keyMetadata.getType() == Value.NUMBER) {
                    double value = dataRS.getDouble(i++);
                    if (dataRS.wasNull()) {
                        row.addValue(key, new EmptyValue());
                    } else {
                        row.addValue(key, new NumericValue(value));
                    }
                } else {
                    String value = dataRS.getString(i++);
                    if (dataRS.wasNull()) {
                        row.addValue(key, new EmptyValue());
                    } else {
                        row.addValue(key, new StringValue(value));
                    }
                }
            }
        }
        return dataSet;
    }
}
