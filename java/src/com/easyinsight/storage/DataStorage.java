package com.easyinsight.storage;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.csvreader.CsvWriter;
import com.easyinsight.analysis.*;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.*;
import com.easyinsight.pipeline.BetterFilterComponent;
import com.easyinsight.pipeline.Pipeline;
import com.easyinsight.pipeline.PipelineData;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.logging.LogClass;
import com.easyinsight.database.Database;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.core.*;
import com.easyinsight.servlet.SystemSettings;
import com.easyinsight.users.Account;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.*;
import java.util.Date;
import java.sql.*;

import com.easyinsight.users.DataSourceStats;
import com.easyinsight.users.UserAccountAdminService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jets3t.service.S3ServiceException;

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
    private List<IDataTransform> transforms = new ArrayList<IDataTransform>();
    private ReportFault warning;
    private int connectionBillingType;

    public ReportFault getWarning() {
        return warning;
    }

    public void setWarning(ReportFault warning) {
        this.warning = warning;
    }

    /**
     * Creates a read only connection for retrieving data.
     *
     * @param fields the analysis items you want to retrieve
     * @param feedID the ID of the data source
     * @return a DataStorage object for making read calls
     */

    public static DataStorage readConnection(List<AnalysisItem> fields, long feedID, FeedType feedType) {
        DataStorage dataStorage = new DataStorage();
        Map<Key, KeyMetadata> keyMetadatas = new LinkedHashMap<Key, KeyMetadata>();
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
                dataStorage.metadata = createDefaultMetadata(conn, feedType);
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

    public static DataStorage readConnection(List<AnalysisItem> fields, long feedID, FeedType feedType, Connection conn) {
        DataStorage dataStorage = new DataStorage();
        Map<Key, KeyMetadata> keyMetadatas = new LinkedHashMap<Key, KeyMetadata>();
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
        dataStorage.metadata = getMetadata(feedID, conn);
        if (dataStorage.metadata == null) {
            dataStorage.metadata = createDefaultMetadata(conn, feedType);
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

    private IStorageDialect getStorageDialect(String tableName, Key distKey) {
        if (database.getDialect() == Database.MYSQL) {
            return new MySQLStorageDialect(tableName, keys);
        } else if (database.getDialect() == Database.POSTGRES) {
            return new AltPostgresStorageDialect(tableName, keys, distKey);
        } else {
            throw new RuntimeException();
        }
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

    public static TempStorage existingTempConnection(FeedDefinition feedDefinition, EIConnection conn, String tableName) {
        Map<Key, KeyMetadata> keyMetadatas = new LinkedHashMap<Key, KeyMetadata>();
        for (AnalysisItem analysisItem : feedDefinition.getFields()) {
            if (!analysisItem.persistable()) {
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
        return new TempStorage(keyMetadatas, database, tableName);
    }

    public static TempStorage tempConnection(FeedDefinition feedDefinition, EIConnection conn) {
        Map<Key, KeyMetadata> keyMetadatas = new LinkedHashMap<Key, KeyMetadata>();
        List<AnalysisItem> cachedCalculations = new ArrayList<AnalysisItem>();
        for (AnalysisItem analysisItem : feedDefinition.getFields()) {
            if (!analysisItem.persistable()) {
                continue;
            }
            if (analysisItem.hasType(AnalysisItemTypes.CALCULATION)) {
                AnalysisCalculation analysisCalculation = (AnalysisCalculation) analysisItem;
                if (analysisCalculation.isCachedCalculation()) {
                    cachedCalculations.add(analysisCalculation);
                }
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
        List<IDataTransform> transforms = new ArrayList<IDataTransform>();
        if (cachedCalculations.size() > 0) {
            transforms.add(new CachedCalculationTransform(feedDefinition));
        }
        Database database = DatabaseManager.instance().getDatabase(getMetadata(feedDefinition.getDataFeedID(), conn).getDatabase());
        return new TempStorage(feedDefinition.getDataFeedID(), keyMetadatas, database, cachedCalculations, transforms, conn, feedDefinition.getUpdateKey());
    }

    public static DataStorage writeConnection(FeedDefinition feedDefinition, Connection conn, long accountID, boolean systemUpdate) throws SQLException {
        DataStorage dataStorage = new DataStorage();
        dataStorage.accountID = accountID;
        dataStorage.systemUpdate = systemUpdate;
        Map<Key, KeyMetadata> keyMetadatas = new LinkedHashMap<Key, KeyMetadata>();
        for (AnalysisItem analysisItem : feedDefinition.getFields()) {
            if (!analysisItem.persistable()) {
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
        /*if (dataStorage.cachedCalculations.size() > 0) {
            dataStorage.transforms.add(new CachedCalculationTransform(feedDefinition));
        }*/
        if (feedDefinition.getMarmotScript() != null && !"".equals(feedDefinition.getMarmotScript())) {
            StringTokenizer toker = new StringTokenizer(feedDefinition.getMarmotScript(), "\r\n");
            while (toker.hasMoreTokens()) {
                String line = toker.nextToken();
                dataStorage.transforms.addAll(new ReportCalculation(line).apply(feedDefinition));
            }
        }
        dataStorage.metadata = getMetadata(feedDefinition.getDataFeedID(), conn);
        dataStorage.tableDefined = dataStorage.metadata != null;
        if (dataStorage.metadata == null) {
            dataStorage.metadata = createDefaultMetadata(conn, feedDefinition.getFeedType());
        }
        dataStorage.database = DatabaseManager.instance().getDatabase(dataStorage.metadata.getDatabase());
        if (dataStorage.database == null) {
            dataStorage.metadata = createDefaultMetadata(conn, feedDefinition.getFeedType());
            dataStorage.database = DatabaseManager.instance().getDatabase(dataStorage.metadata.getDatabase());
            if (dataStorage.database == null) {
                throw new DatabaseShardException();
            }
        }
        dataStorage.connectionBillingType = new DataSourceTypeRegistry().billingInfoForType(feedDefinition.getFeedType());
        dataStorage.keys = keyMetadatas;
        dataStorage.dataSourceType = feedDefinition.getFeedType().getType();
        dataStorage.feedID = feedDefinition.getDataFeedID();
        dataStorage.version = dataStorage.metadata.getVersion();
        dataStorage.coreDBConn = conn;

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

        PreparedStatement spaceAllowed = conn.prepareStatement("SELECT ACCOUNT_TYPE, MAX_SIZE, max_days_over_size_boundary, days_over_size_boundary, last_boundary_date, pricing_model, core_storage, addon_storage_units FROM ACCOUNT WHERE account_id = ?");
        spaceAllowed.setLong(1, accountID);
        ResultSet spaceRS = spaceAllowed.executeQuery();
        spaceRS.next();
        long fixedSize = spaceRS.getLong(2);
        int maxDaysOver = spaceRS.getInt(3);
        int daysOver = spaceRS.getInt(4);
        Date lastBoundaryDate = spaceRS.getTimestamp(5);
        int pricingModel = spaceRS.getInt(6);
        long coreStorage = spaceRS.getLong(7);
        int addonStorageUnits = spaceRS.getInt(8);
        spaceAllowed.close();

        // if it's a small biz connection and they're on the new pricing model, don't validate storage
        boolean validate = connectionBillingType == ConnectionBillingType.CUSTOM_DATA ||
                connectionBillingType == ConnectionBillingType.QUICKBASE;
        if (pricingModel == 1 && !validate) return;

        if (maxDaysOver == -1) {
            // if max days over = -1, they're an account we're not monitoring at all for size yet
            return;
        }

        List<DataSourceStats> statsList = UserAccountAdminService.sizeDataSources((EIConnection) conn, accountID, pricingModel);
        long usedSize = UserAccountAdminService.usedSize(statsList);

        long maxSize;

        if (pricingModel == 0) {
            maxSize = fixedSize;
        } else {
            maxSize = coreStorage + (long) addonStorageUnits * 250000000L;
        }

        if (usedSize > maxSize) {
            String mb = Account.humanReadableByteCount(maxSize, true);
            // first, if the account is using more than absolute limit of 2x storage, just throw an exception
            if (usedSize > (maxSize * 2)) {
                throw new ReportException(new StorageLimitFault("Retrieval of data for this data source has exceeded your account's storage limit of " + mb + ". You need to reduce the size of the data, clean up other data sources on the account, or upgrade to a higher account tier.", statsList));
            }
            if (daysOver > maxDaysOver) {
                throw new ReportException(new StorageLimitFault("Retrieval of data for this data source has exceeded your account's storage limit of " + mb + ". You need to reduce the size of the data, clean up other data sources on the account, or upgrade to a higher account tier.", statsList));
            }

            Calendar now = Calendar.getInstance();
            Calendar cal = Calendar.getInstance();
            if (lastBoundaryDate != null) {
                cal.setTime(lastBoundaryDate);
            }
            if (lastBoundaryDate != null && (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR))) {
                // we're already on overage for today
            } else {
                daysOver++;
            }
            PreparedStatement updateStmt = conn.prepareStatement("UPDATE ACCOUNT SET DAYS_OVER_SIZE_BOUNDARY = ?, LAST_BOUNDARY_DATE = ?, CURRENT_SIZE = ? WHERE ACCOUNT_ID = ?");
            updateStmt.setInt(1, daysOver);
            updateStmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            updateStmt.setLong(3, usedSize);
            updateStmt.setLong(4, accountID);
            updateStmt.executeUpdate();
            updateStmt.close();

            warning = new StorageLimitFault("Retrieval of data for this data source has exceeded your account's storage limit of " + mb + ". You will need to either upgrade your account or reduce the size of data within " + (maxDaysOver - daysOver + 1) + " days to keep refreshing your data.", statsList);
        } else {
            PreparedStatement updateStmt = conn.prepareStatement("UPDATE ACCOUNT SET CURRENT_SIZE = ? WHERE ACCOUNT_ID = ?");
            updateStmt.setLong(1, usedSize);
            updateStmt.setLong(2, accountID);
            updateStmt.executeUpdate();
            updateStmt.close();
        }
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
                    if (se.getMessage().contains("Unknown table") || se.getMessage().contains("does not exist")) {
                        LogClass.error("Data source " + feedID + " did not have a storage table. Continuing with delete, screwed up data.");
                    } else {
                        throw se;
                    }
                } finally {
                    Database.closeConnection(storageConn);
                }
            }
        }
    }

    public long calculateSize() throws SQLException {
        if (database.getDialect() == Database.POSTGRES) {
            /*PreparedStatement countStmt = storageConn.prepareStatement("SELECT\n" +
                    "    trim(pgdb.datname) AS DATABASE,\n" +
                    "    trim(pgn.nspname) AS SCHEMA,\n" +
                    "    trim(a.name) AS TABLE,\n" +
                    "    b.mbytes,\n" +
                    "    a.rows\n" +
                    "FROM (\n" +
                    "    SELECT db_id, id, NAME, sum(ROWS) AS ROWS\n" +
                    "    FROM stv_tbl_perm a\n" +
                    "    GROUP BY db_id, id, NAME\n" +
                    ") AS a\n" +
                    "JOIN pg_class AS pgc ON pgc.oid = a.id\n" +
                    "JOIN pg_namespace AS pgn ON pgn.oid = pgc.relnamespace\n" +
                    "JOIN pg_database AS pgdb ON pgdb.oid = a.db_id\n" +
                    "JOIN (\n" +
                    "    SELECT tbl, COUNT(*) AS mbytes\n" +
                    "    FROM stv_blocklist\n" +
                    "    GROUP BY tbl\n" +
                    ") b ON a.id = b.tbl\n" +
                    "WHERE a.name = ?");
            countStmt.setString(1, getTableName());
            ResultSet countRS = countStmt.executeQuery();
            long size = 0;
            if (countRS.next()) {
                int rows = countRS.getInt(5);
                size = countRS.getLong(4);
                System.out.println(rows + " = " + size);
            }
            countStmt.close();
            return size;*/
            return 0;
        } else {
            PreparedStatement countStmt = storageConn.prepareStatement("SHOW TABLE STATUS LIKE ?");
            countStmt.setString(1, getTableName());
            ResultSet countRS = countStmt.executeQuery();
            if (countRS.next()) {
                long dataLength = countRS.getLong("Data_length");
                long indexLength = countRS.getLong("Index_length");
                if (dataSourceType == FeedType.HIGHRISE_ACTIVITIES.getType()) {
                    // 339,656,704
                    return 0;
                } else {
                    return dataLength;
                }
            } else {
                return 0;
            }
        }
    }

    public void createTable(@Nullable Key updateKey) throws SQLException {
        ResultSet existsRS = storageConn.getMetaData().getTables(null, null, getTableName(), null);
        if (existsRS.next()) {
            storageConn.prepareStatement("DROP TABLE " + getTableName()).execute();
        }
        try {
            String sql = defineTableSQL(false, updateKey);
            PreparedStatement createSQL = storageConn.prepareStatement(sql);
            createSQL.execute();
        } catch (SQLException e) {
            LogClass.error(e);
            if (e.getMessage().contains("Row size too large")) {
                String sql = defineTableSQL(true, updateKey);
                PreparedStatement createSQL = storageConn.prepareStatement(sql);
                createSQL.execute();
            } else {
                throw new RuntimeException(e);
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
                addOrUpdateMetadata(feedID, metadata, conn, dataSourceType);
            } finally {
                Database.closeConnection(conn);
            }
        } else {
            addOrUpdateMetadata(feedID, metadata, coreDBConn, dataSourceType);
            validateSpace(coreDBConn);
        }
        storageConn.commit();
        ReportCache.instance().flushResults(feedID, (EIConnection) coreDBConn);
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
        if (database.getDialect() == Database.MYSQL) {
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
            dropStmt.close();
        } else {
            String bucketName = "refresh" + tempTable;
            try {
                StringBuilder sb = new StringBuilder();
                for (Key key : keys.keySet()) {
                    sb.append(key.toSQL()).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                String string = "copy " + getTableName() + " (" + sb.toString() + ") from 's3://" + bucketName + "/" + tempTable + "' credentials 'aws_access_key_id=0AWCBQ78TJR8QCY8ABG2;aws_secret_access_key=bTUPJqHHeC15+g59BQP8ackadCZj/TsSucNwPwuI' escape removequotes truncatecolumns emptyasnull blanksasnull delimiter '|' GZIP timeformat 'YYYY-MM-DD HH:MI:SS'";
                PreparedStatement stmt = storageConn.prepareStatement(string);
                stmt.execute();
                stmt.close();
            } finally {
                AmazonS3 s3 = new AmazonS3Client(new BasicAWSCredentials("0AWCBQ78TJR8QCY8ABG2", "bTUPJqHHeC15+g59BQP8ackadCZj/TsSucNwPwuI"));
                ObjectListing objectListing = s3.listObjects(bucketName);
                List<S3ObjectSummary> summaries = objectListing.getObjectSummaries();
                for (S3ObjectSummary summary : summaries) {
                    s3.deleteObject(bucketName, summary.getKey());
                }
                s3.deleteBucket(bucketName);
            }

        }
    }

    public void updateFromTemp(String tempTable, Key updateKey) throws SQLException {

        // todo: building this

        if (database.getDialect() == Database.MYSQL) {
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
            updateStmt.close();
            insertStmt.close();
            PreparedStatement dropStmt = storageConn.prepareStatement("DROP TABLE " + tempTable);
            dropStmt.execute();
            dropStmt.close();
        } else {

            // copy the data into the load table

            String loadTable = "load" + tempTable;
            String bucketName = "refresh" + tempTable;
            try {
                StringBuilder sb = new StringBuilder();
                for (Key key : keys.keySet()) {
                    sb.append("k").append(key.getKeyID()).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                // TODO: argh
                // String string = "copy " + getTableName() + " (" + sb.toString() + ") from 's3://" + bucketName + "/" + tempTable + "' credentials 'aws_access_key_id=0AWCBQ78TJR8QCY8ABG2;aws_secret_access_key=bTUPJqHHeC15+g59BQP8ackadCZj/TsSucNwPwuI' escape removequotes truncatecolumns emptyasnull blanksasnull delimiter '|' GZIP timeformat 'YYYY-MM-DD HH:MI:SS'";
                String string = "copy " + loadTable + " ("+sb.toString()+") from 's3://"+bucketName+"/"+tempTable+"' credentials 'aws_access_key_id=0AWCBQ78TJR8QCY8ABG2;aws_secret_access_key=bTUPJqHHeC15+g59BQP8ackadCZj/TsSucNwPwuI' escape removequotes truncatecolumns emptyasnull blanksasnull delimiter '|' GZIP timeformat 'YYYY-MM-DD HH:MI:SS'";

                PreparedStatement stmt = storageConn.prepareStatement(string);
                stmt.execute();
            } finally {
                AmazonS3 s3 = new AmazonS3Client(new BasicAWSCredentials("AKIAI5YYYFRMWFLLEC2A", "NmonY27/vE03AeGNWhLBmkR41kJrvbWSYhLzh5pE"));
                ObjectListing objectListing = s3.listObjects(bucketName);
                List<S3ObjectSummary> summaries = objectListing.getObjectSummaries();
                for (S3ObjectSummary summary : summaries) {
                    s3.deleteObject(bucketName, summary.getKey());
                }
                s3.deleteBucket(bucketName);
            }

            String tempTableSQL = "CREATE TEMP TABLE " + tempTable + " AS SELECT * FROM " + loadTable;

            PreparedStatement createTempTableStmt = storageConn.prepareStatement(tempTableSQL);
            createTempTableStmt.execute();
            createTempTableStmt.close();

            String deleteSQL = "DELETE FROM " + getTableName() + " USING " + tempTable + " WHERE " + getTableName() + "." + updateKey.toSQL() + " = " +
                    tempTable + "." + updateKey.toSQL();


            StringBuilder columnBuilder = new StringBuilder();
            Iterator<KeyMetadata> keyIter = keys.values().iterator();
            while (keyIter.hasNext()) {
                KeyMetadata keyMetadata = keyIter.next();
                columnBuilder.append(keyMetadata.createInsertClause());
                if (keyIter.hasNext()) {
                    columnBuilder.append(",");
                }
            }


            PreparedStatement deleteStmt = storageConn.prepareStatement(deleteSQL);
            int rows = deleteStmt.executeUpdate();

            deleteStmt.close();

            String insertSQL = "INSERT INTO " + getTableName() + " SELECT " + columnBuilder.toString() + " FROM " + tempTable;



            PreparedStatement insertStmt = storageConn.prepareStatement(insertSQL);
            insertStmt.execute();
            insertStmt.close();

            PreparedStatement dropStmt = storageConn.prepareStatement("DROP TABLE " + tempTable);
            dropStmt.execute();
            dropStmt.close();

            PreparedStatement dropLoadStmt = storageConn.prepareStatement("DROP TABLE " + loadTable);
            dropLoadStmt.execute();
            dropLoadStmt.close();
        }
    }

    public void alter(Key key) throws SQLException {
        System.out.println("Statement of alter = " + "ALTER TABLE " + getTableName() + " ADD " + key.toSQL() + " DOUBLE DEFAULT NULL");
        PreparedStatement stmt = storageConn.prepareStatement("ALTER TABLE " + getTableName() + " ADD " + key.toSQL() + " DOUBLE DEFAULT NULL");
        stmt.execute();
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
        return migrate(previousItems, newItems, true, null);
    }

    public int migrate(List<AnalysisItem> previousItems, List<AnalysisItem> newItems, boolean migrateData) throws Exception {
        return migrate(previousItems, newItems, migrateData, null);
    }

    public int migrate(List<AnalysisItem> previousItems, List<AnalysisItem> newItems, Key updateKey) throws Exception {
        return migrate(previousItems, newItems, true, updateKey);
    }

    public boolean calcCompare(AnalysisItem newItem, AnalysisItem previousItem) {
        if (newItem.hasType(AnalysisItemTypes.CALCULATION)) {
            AnalysisCalculation calc = (AnalysisCalculation) newItem;
            return (calc.isCachedCalculation() && !previousItem.persistable());
        }
        return false;
    }

    public void updateFromPrevious(List<KeyMetadata> keys, String previousTable, String newTable) throws SQLException {
        StringBuilder columnBuilder = new StringBuilder();
        StringBuilder selectBuilder = new StringBuilder();
        Iterator<KeyMetadata> keyIter = keys.iterator();
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
        String insertSQL = "INSERT INTO " + newTable + " (" + columns + ") SELECT " + selectBuilder.toString() + " FROM " + previousTable;
        PreparedStatement insertStmt = storageConn.prepareStatement(insertSQL);
        insertStmt.execute();
    }

    public int migrate(List<AnalysisItem> previousItems, List<AnalysisItem> newItems, final boolean migrateData, Key updateKey) throws Exception {
        // did any items change in a way that requires us to migrate...
        List<FieldMigration> fieldMigrations = new ArrayList<FieldMigration>();
        boolean newFieldsFound = false;
        for (AnalysisItem newItem : newItems) {
            if (!newItem.persistable()) {
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
                            (newItem.hasType(AnalysisItemTypes.TEXT) && !previousItem.hasType(AnalysisItemTypes.TEXT)) ||
                            calcCompare(newItem, previousItem)) {
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

            ResultSet existsRS = storageConn.getMetaData().getTables(null, null, getTableName(), null);
            if (existsRS.next()) {
                storageConn.prepareStatement("DROP TABLE " + getTableName()).execute();
            }

            try {
                String sql = defineTableSQL(false, updateKey);
                LogClass.info("Creating new storage table in migration with sql " + sql);
                PreparedStatement createSQL = storageConn.prepareStatement(sql);
                createSQL.execute();
            } catch (SQLException e) {
                if (e.getMessage().contains("Row size too large")) {
                    String sql = defineTableSQL(true, updateKey);
                    PreparedStatement createSQL = storageConn.prepareStatement(sql);
                    createSQL.execute();
                }
            }

            if (migrateData) {
                if (!fieldMigrations.isEmpty() || dataSourceType != FeedType.QUICKBASE_CHILD.getType()) {
                    slowMigrate(previousItems, fieldMigrations, previousVersion);
                } else {
                    List<KeyMetadata> keys = new ArrayList<KeyMetadata>();
                    for (AnalysisItem analysisItem : previousItems) {
                        if (analysisItem.isDerived()) {
                            continue;
                        }
                        Key key = analysisItem.getKey();
                        if (analysisItem.hasType(AnalysisItemTypes.DATE_DIMENSION)) {
                            keys.add(new KeyMetadata(key, Value.DATE, analysisItem));
                        } else if (analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
                            keys.add(new KeyMetadata(key, Value.NUMBER, analysisItem));
                        } else if (analysisItem.hasType(AnalysisItemTypes.TEXT)) {
                            keys.add(new KeyMetadata(key, Value.TEXT, analysisItem));
                        } else {
                            keys.add(new KeyMetadata(key, Value.STRING, analysisItem));
                        }
                    }
                    updateFromPrevious(keys, "df" + feedID + "v" + previousVersion, getTableName());
                }
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

    private void slowMigrate(List<AnalysisItem> previousItems, List<FieldMigration> fieldMigrations, int previousVersion) throws Exception {
        DataSet existing;
        List<AnalysisItem> previousKeys = new ArrayList<AnalysisItem>();
        for (AnalysisItem previousItem : previousItems) {
            if (previousItem.persistable()) {
                previousKeys.add(previousItem);
            }
        }

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
        if (previousKeys.isEmpty()) {
            existing = new DataSet();
        } else {


            InsightRequestMetadata insightRequestMetadata = new InsightRequestMetadata();
            insightRequestMetadata.setNow(new Date());
            insightRequestMetadata.setAggregateQuery(false);
            try {
                existing = retrieveData(previousKeys, null, 0, keyMetadatas, previousVersion, insightRequestMetadata);
            } catch (Exception e) {
                LogClass.error(e);
                existing = new DataSet();
            }
        }


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

    /**
     * Clears out all data in the data source.
     *
     * @throws java.sql.SQLException if something goes wrong
     */

    public void truncate() throws SQLException {
        truncate(null);
    }

    public void truncate(Key updateKey) throws SQLException {
        try {
            ResultSet tableRS = storageConn.getMetaData().getTables(null, null, getTableName(), null);
            if (tableRS.next()) {
                PreparedStatement truncateStmt = storageConn.prepareStatement("TRUNCATE " + getTableName());
                truncateStmt.execute();
            } else {
                createTable(updateKey);
            }
        } catch (SQLException e) {
            LogClass.error(e);
            if (e.getMessage().contains("doesn't exist")) {
                createTable(updateKey);
            }
        }
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
        long startTime = System.currentTimeMillis();
        if (insightRequestMetadata == null) {
            insightRequestMetadata = new InsightRequestMetadata();
            insightRequestMetadata.setNow(new Date());
        }
        reportItems = new ArrayList<>(reportItems);
        boolean countDistinct = false;
        Set<String> keyStrings = new HashSet<String>();

        for (Key key : this.keys.keySet()) {
            keyStrings.add(key.toSQL());
        }

        AnalysisItem rowIDField = null;


        if (keys == null) {
            keys = new HashMap<>();
            Iterator<AnalysisItem> iter = reportItems.iterator();
            while (iter.hasNext()) {
                AnalysisItem analysisItem = iter.next();
                if (analysisItem.isRowIDField()) {
                    rowIDField = analysisItem;
                    iter.remove();
                    continue;
                }
                if (analysisItem.isDerived()) {
                    iter.remove();
                    continue;
                }
                AggregateKey key = analysisItem.createAggregateKey(false);
                if (!keyStrings.contains(key.toSQL())) {
                    iter.remove();
                    continue;
                }
                if (analysisItem.isKeyColumn()) {
                    analysisItem.setPkKeyName(getTableName() + "_ID");
                }
                if (analysisItem.hasType(AnalysisItemTypes.DATE_DIMENSION)) {
                    keys.put(key, new KeyMetadata(key, Value.DATE, analysisItem));
                } else if (analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
                    AnalysisMeasure analysisMeasure = (AnalysisMeasure) analysisItem;
                    if (analysisMeasure.getAggregation() == AggregationTypes.COUNT_DISTINCT) {
                        countDistinct = true;
                        keys.put(key, new KeyMetadata(key, Value.STRING, analysisItem));
                    } else {
                        AggregateKey testKey = new AggregateKey(key.toBaseKey().toBaseKey(), AnalysisItemTypes.DIMENSION, null);
                        KeyMetadata baseMetadata = this.keys.get(testKey);
                        if (baseMetadata != null && baseMetadata.getType() != Value.NUMBER) {
                            System.out.println("forcing to alt value");
                            keys.put(key, baseMetadata);
                        } else {
                            keys.put(key, new KeyMetadata(key, Value.NUMBER, analysisItem));
                        }
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
        List<FilterDefinition> skippedFilters = new ArrayList<>();
        filters = eligibleFilters(filters, keyStrings, insightRequestMetadata, skippedFilters);



        boolean distinctValid = true;
        for (AnalysisItem item : reportItems) {
            distinctValid = distinctValid && insightRequestMetadata.getDistinctFieldMap().get(item) != null && insightRequestMetadata.getDistinctFieldMap().get(item);
        }

        StringBuilder queryBuilder = new StringBuilder();
        StringBuilder selectBuilder = new StringBuilder();
        StringBuilder fromBuilder = new StringBuilder();
        StringBuilder whereBuilder = new StringBuilder();
        StringBuilder groupByBuilder = new StringBuilder();
        Collection<Key> groupByItems = new HashSet<Key>();
        boolean aggregateQuery = insightRequestMetadata.isAggregateQuery() && !countDistinct;

        createSelectClause(reportItems, selectBuilder, groupByBuilder, aggregateQuery, insightRequestMetadata.isOptimized(), insightRequestMetadata, distinctValid);
        selectBuilder = selectBuilder.deleteCharAt(selectBuilder.length() - 1);
        createFromClause(version, fromBuilder, insightRequestMetadata);
        createWhereClause(filters, whereBuilder, insightRequestMetadata);
        createSQL(filters, limit, queryBuilder, selectBuilder, fromBuilder, whereBuilder, groupByBuilder, groupByItems);

        PreparedStatement queryStmt;
        if (insightRequestMetadata.getFetchSize() > 0) {
            queryStmt = storageConn.prepareStatement(queryBuilder.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            queryStmt.setFetchSize(insightRequestMetadata.getFetchSize());
        } else {
            queryStmt = storageConn.prepareStatement(queryBuilder.toString());
        }

        populateParameters(filters, keys, queryStmt, insightRequestMetadata);
        DataSet dataSet = new DataSet();



        ResultSet dataRS;

        try {
            dataRS = queryStmt.executeQuery();
            processQueryResults(reportItems, keys, dataSet, dataRS, aggregateQuery, insightRequestMetadata.isOptimized(), rowIDField, insightRequestMetadata.isOptimizeDays());
        } catch (Exception e) {
            LogClass.error("On running " + queryBuilder.toString(), e);
            throw new RuntimeException(e);
        }
        for (AdvancedFilterProperties properties : insightRequestMetadata.getFilterPropertiesMap().values()) {
            Statement stmt = storageConn.createStatement();
            stmt.execute("DROP TABLE " + properties.getTable());
            stmt.close();
        }
        dataRS.close();
        queryStmt.close();
        System.out.println("got " + dataSet.getRows().size() + " rows for " + getTableName());
        insightRequestMetadata.addDatabaseTime(System.currentTimeMillis() - startTime);
        dataSet.setLastTime(metadata.getLastData());
        if (insightRequestMetadata.isLogReport()) {
            dataSet.getAudits().add(
                    new ReportAuditEvent(ReportAuditEvent.QUERY, "Query took " + (System.currentTimeMillis() - startTime) + " ms to retrieve " + dataSet.getRows().size() + " rows"));
        }
        //System.out.println("took " + (System.currentTimeMillis() - startTime));
        if (skippedFilters.size() > 0) {
            BetterFilterComponent betterFilterComponent = new BetterFilterComponent(skippedFilters);
            PipelineData pipelineData = new PipelineData(null, null, insightRequestMetadata, null, null, null, null, null);
            dataSet = betterFilterComponent.apply(dataSet, pipelineData);
        }
        return dataSet;
    }

    @NotNull
    private Collection<FilterDefinition> eligibleFilters(@Nullable Collection<FilterDefinition> filters, Set<String> keyStrings, InsightRequestMetadata insightRequestMetadata,
                                                         Collection<FilterDefinition> skippedFilters) {
        Collection<FilterDefinition> eligibleFilters = new ArrayList<>();
        if (filters != null) {
            for (FilterDefinition filterDefinition : filters) {
                if (insightRequestMetadata.getSuppressedFilters().contains(filterDefinition)) {
                    continue;
                }
                if (filterDefinition.getPipelineName().equals(Pipeline.BEFORE) && filterDefinition.validForQuery()) {
                    if (filterDefinition instanceof FilterValueDefinition && database.getDialect() == Database.POSTGRES) {
                        FilterValueDefinition filterValueDefinition = (FilterValueDefinition) filterDefinition;
                        if (filterValueDefinition.getFilteredValues().size() > 970) {
                            skippedFilters.add(filterValueDefinition);
                            continue;
                        }
                    }
                    if (filterDefinition.getField() != null) {
                        if (!keyStrings.contains(filterDefinition.getField().getKey().toSQL())) {
                            continue;
                        }
                        if (filterDefinition instanceof FilterValueDefinition) {
                            FilterValueDefinition filterValueDefinition = (FilterValueDefinition) filterDefinition;
                            if (filterValueDefinition.getFilteredValues() != null && filterValueDefinition.getFilteredValues().size() > SystemSettings.instance().getMaxFilterValues()) {
                                skippedFilters.add(filterValueDefinition);
                                continue;
                            }
                        }
                    }
                    eligibleFilters.add(filterDefinition);
                }
            }
        }
        return eligibleFilters;
    }

    private void processQueryResults(@NotNull Collection<AnalysisItem> reportItems, @NotNull Map<Key, KeyMetadata> keys, @NotNull DataSet dataSet, @NotNull ResultSet dataRS,
                                     boolean aggregateQuery, boolean optimized, @Nullable AnalysisItem rowIDItem, boolean dayOptimize) throws SQLException {
        int rowID = 0;
        while (dataRS.next()) {
            IRow row = dataSet.createRow();
            if (rowIDItem != null) {
                rowID++;
                row.addValue(rowIDItem.createAggregateKey(false), new StringValue(String.valueOf(rowID)));
            }
            int i = 1;
            for (AnalysisItem analysisItem : reportItems) {
                Key key = analysisItem.createAggregateKey(false);
                AggregateKey aggregateKey = analysisItem.createAggregateKey();
                KeyMetadata keyMetadata = keys.get(key);
                if (keyMetadata != null) {
                    if (keyMetadata.getType() == Value.DATE) {
                        AnalysisDateDimension date = (AnalysisDateDimension) analysisItem;
                        if (optimized && aggregateQuery && (date.getDateLevel() == AnalysisDateDimension.MONTH_FLAT || date.getDateLevel() == AnalysisDateDimension.MONTH_LEVEL ||
                                date.getDateLevel() == AnalysisDateDimension.QUARTER_OF_YEAR_LEVEL || date.getDateLevel() == AnalysisDateDimension.QUARTER_OF_YEAR_FLAT)) {
                            int month = dataRS.getInt(i++);
                            int year = dataRS.getInt(i++);
                            Calendar cal = Calendar.getInstance();
                            if (year == 0) {
                                row.addValue(aggregateKey, new EmptyValue());
                            } else {
                                cal.set(Calendar.DAY_OF_MONTH, 2);
                                cal.set(Calendar.MONTH, month - 1);
                                cal.set(Calendar.YEAR, year);
                                row.addValue(aggregateKey, new DateValue(cal.getTime()));
                            }
                        } else if (optimized && aggregateQuery && (date.getDateLevel() == AnalysisDateDimension.YEAR_LEVEL)) {
                            int year = dataRS.getInt(i++);
                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.DAY_OF_MONTH, 2);
                            cal.set(Calendar.MONTH, Calendar.JANUARY);
                            cal.set(Calendar.YEAR, year);
                            row.addValue(aggregateKey, new DateValue(cal.getTime()));
                        } else if (aggregateQuery && dayOptimize && (date.getDateLevel() == AnalysisDateDimension.DAY_LEVEL)) {
                            try {
                                java.sql.Date time = dataRS.getDate(i++);
                                if (dataRS.wasNull()) {
                                    row.addValue(aggregateKey, new EmptyValue());
                                } else {
                                    long milliseconds = time.getTime();
                                    row.addValue(aggregateKey, new DateValue(new Date(milliseconds)));
                                }
                            } catch (SQLException e) {
                                row.addValue(aggregateKey, new EmptyValue());
                            }
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

    private void createWhereClause(Collection<FilterDefinition> filters, StringBuilder whereBuilder, InsightRequestMetadata insightRequestMetadata) {
        if (filters != null && filters.size() > 0) {
            Iterator<FilterDefinition> filterIter = filters.iterator();
            while (filterIter.hasNext()) {
                FilterDefinition filterDefinition = filterIter.next();
                AdvancedFilterProperties advancedFilterProperties = insightRequestMetadata.getFilterPropertiesMap().get(filterDefinition);
                if (advancedFilterProperties != null) {
                    whereBuilder.append(filterDefinition.toQuerySQL(getTableName(), advancedFilterProperties.getTable(), advancedFilterProperties.getKey()));
                } else {
                    whereBuilder.append(filterDefinition.toQuerySQL(getTableName(), database));
                }
                if (filterIter.hasNext()) {
                    whereBuilder.append(" AND ");
                }
            }
        }
    }

    private void createFromClause(int version, StringBuilder fromBuilder, InsightRequestMetadata insightRequestMetadata) {
        String tableName = "df" + feedID + "v" + version;
        fromBuilder.append(tableName);
        for (AdvancedFilterProperties properties : insightRequestMetadata.getFilterPropertiesMap().values()) {
            String table = properties.getTable();
            fromBuilder.append(",").append(table);
        }
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

    private void createSelectClause(Collection<AnalysisItem> reportItems, StringBuilder selectBuilder, StringBuilder groupByBuilder,
                                    boolean aggregateQuery, boolean optimized, InsightRequestMetadata insightRequestMetadata, boolean distinctIsValid) {
        for (AnalysisItem analysisItem : reportItems) {
            if (analysisItem.isDerived()) {
                boolean stillOkay = false;
                if (analysisItem.hasType(AnalysisItemTypes.CALCULATION)) {
                    AnalysisCalculation calc = (AnalysisCalculation) analysisItem;
                    if (calc.isCachedCalculation()) {
                        stillOkay = true;
                    }
                }
                if (!stillOkay) {
                    throw new RuntimeException("Attempt made to query derived analysis item " + analysisItem.toDisplay() + " of class " + analysisItem.getClass().getName());
                }
            }

            String columnName = analysisItem.toKeySQL();
            if (analysisItem.hasType(AnalysisItemTypes.MEASURE) && aggregateQuery) {
                AnalysisMeasure analysisMeasure = (AnalysisMeasure) analysisItem;

                AggregateKey testKey = new AggregateKey(analysisMeasure.createAggregateKey().toBaseKey().toBaseKey(), AnalysisItemTypes.DIMENSION, null);
                KeyMetadata baseMetadata = this.keys.get(testKey);
                if (baseMetadata != null && baseMetadata.getType() != Value.NUMBER) {
                    groupByBuilder.append(columnName);
                    groupByBuilder.append(",");
                } else {
                    int aggregation = analysisMeasure.getQueryAggregation();
                    if (aggregation == AggregationTypes.SUM || aggregation == AggregationTypes.PERCENT_OF_TOTAL) {
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
                        if (database.getDialect() == Database.MYSQL) {
                            groupByBuilder.append("binary(" + columnName + ")");
                        } else {
                            groupByBuilder.append(columnName);
                        }
                        groupByBuilder.append(",");
                    }
                }
                selectBuilder.append(columnName);
                selectBuilder.append(",");
            } else if (analysisItem.hasType(AnalysisItemTypes.DATE_DIMENSION) && aggregateQuery) {
                AnalysisDateDimension date = (AnalysisDateDimension) analysisItem;
                if (optimized && (date.getDateLevel() == AnalysisDateDimension.MONTH_FLAT || date.getDateLevel() == AnalysisDateDimension.MONTH_LEVEL)) {
                    if (database.getDialect() == Database.MYSQL) {
                        selectBuilder.append("month(" + columnName + ") as month" + columnName + ", year(" + columnName + ") as year" + columnName + ",");
                    } else if (database.getDialect() == Database.POSTGRES) {
                        selectBuilder.append("extract(month from " + columnName + ") as month" + columnName + ", extract(year from " + columnName + ") as year" + columnName + ",");
                    } else {
                        throw new RuntimeException();
                    }

                    groupByBuilder.append("month" + columnName + ", year" + columnName + ",");
                } else if (optimized && (date.getDateLevel() == AnalysisDateDimension.YEAR_LEVEL)) {
                    if (database.getDialect() == Database.MYSQL) {
                        selectBuilder.append("year(" + columnName + ") as year" + columnName + ",");
                    } else if (database.getDialect() == Database.POSTGRES) {
                        selectBuilder.append("extract(year from " + columnName + ") as year" + columnName + ",");
                    } else {
                        throw new RuntimeException();
                    }
                    groupByBuilder.append("year" + columnName + ",");
                } else if (optimized && (date.getDateLevel() == AnalysisDateDimension.QUARTER_OF_YEAR_FLAT || date.getDateLevel() == AnalysisDateDimension.QUARTER_OF_YEAR_LEVEL)) {
                    if (database.getDialect() == Database.MYSQL) {
                        selectBuilder.append("month(" + columnName + ") as month" + columnName + ", year(" + columnName + ") as year" + columnName + ",");
                    } else if (database.getDialect() == Database.POSTGRES) {
                        selectBuilder.append("extract(month from " + columnName + ") as month" + columnName + ", extract(year from " + columnName + ") as year" + columnName + ",");
                    }
                    groupByBuilder.append("month" + columnName + ", year" + columnName + ",");
                } else if (insightRequestMetadata.isOptimizeDays() && (date.getDateLevel() == AnalysisDateDimension.DAY_LEVEL)) {
                    if (database.getDialect() == Database.MYSQL) {
                        selectBuilder.append("date(" + columnName + ") as date" + columnName + ",");
                    } else if (database.getDialect() == Database.POSTGRES) {
                        selectBuilder.append(columnName + "::date as date" + columnName + ",");
                    }
                    groupByBuilder.append("date" + columnName + ",");
                } else {
                    selectBuilder.append(columnName);
                    selectBuilder.append(",");
                    if (database.getDialect() == Database.MYSQL  && database.getDialect() == Database.MYSQL) {
                        groupByBuilder.append("binary(" + columnName + ")");
                    } else {
                        groupByBuilder.append(columnName);
                    }
                    groupByBuilder.append(",");
                }
            } else {
                Boolean distinct = insightRequestMetadata.getDistinctFieldMap().get(analysisItem);
                if (distinct != null && distinct && distinctIsValid) {
                    selectBuilder.append("distinct(").append(columnName).append(") as distinct").append(columnName);
                    selectBuilder.append(",");
                } else {
                    if (aggregateQuery) {
                        if (database.getDialect() == Database.MYSQL) {
                            groupByBuilder.append("binary(" + columnName + ")");
                        } else {
                            groupByBuilder.append(columnName);
                        }
                        groupByBuilder.append(",");
                    }
                    selectBuilder.append(columnName);
                    selectBuilder.append(",");
                }
            }

        }
        if (groupByBuilder.length() > 0) {
            groupByBuilder.deleteCharAt(groupByBuilder.length() - 1);
        }
    }

    public void insertData(DataSet dataSet) throws Exception {
        for (IRow row : dataSet.getRows()) {
            for (IDataTransform transform : transforms) {
                transform.handle((EIConnection) coreDBConn, row);
            }
        }
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

    public void trueUpdateData(DataSet dataSet, List<IWhere> wheres) throws Exception {
        StringBuilder fieldBuilder = new StringBuilder();
        List<KeyMetadata> updateKeys = new ArrayList<KeyMetadata>();
        for (KeyMetadata keyMetadata : keys.values()) {
            boolean inWhereClause = false;
            for (IWhere where : wheres) {
                if (where.getKey().equals(keyMetadata.getKey())) {
                    where.getKey().setKeyID(keyMetadata.getKey().getKeyID());
                    inWhereClause = true;
                }
            }
            if (!inWhereClause) {
                updateKeys.add(keyMetadata);
            }
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
        return setValue(insertStmt, i, keyMetadata, value);
    }

    private int setValue(PreparedStatement insertStmt, int i, KeyMetadata keyMetadata, Value value) throws SQLException {

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
                Calendar calendar = Calendar.getInstance();
                Value transformedValue = analysisItem.transformValue(value, new InsightRequestMetadata(), false, calendar);
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
            if (date == null || database.getDialect() == Database.POSTGRES) {
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

    public String defineTableSQL(boolean hugeTable, Key distKey) {
        return getStorageDialect(getTableName(), distKey).defineTableSQL(hugeTable);
    }

    String getTableName() {
        return "df" + feedID + "v" + version;
    }

    private static void addOrUpdateMetadata(long dataFeedID, FeedPersistenceMetadata metadata, Connection conn, int dataSourceType) {
        try {
            //if (dataSourceType == FeedType.DATABASE_CONNECTION.getType() || dataSourceType == FeedType.SALESFORCE_SUB.getType()) {
            //if (metadata.getMetadataID() > 0) {
            try {
                PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM FEED_PERSISTENCE_METADATA WHERE FEED_ID = ? AND " +
                        "VERSION = ?");
                deleteStmt.setLong(1, dataFeedID);
                deleteStmt.setInt(2, metadata.getVersion());
                deleteStmt.executeUpdate();
                deleteStmt.close();
            } catch (SQLException e) {
                // retry once?
                PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM FEED_PERSISTENCE_METADATA WHERE FEED_ID = ? AND " +
                        "VERSION = ?");
                deleteStmt.setLong(1, dataFeedID);
                deleteStmt.setInt(2, metadata.getVersion());
                deleteStmt.executeUpdate();
                deleteStmt.close();
            }
            //}
            /*} else {
                if (metadata.getMetadataID() > 0) {
                    PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM FEED_PERSISTENCE_METADATA WHERE FEED_PERSISTENCE_METADATA_ID = ?");
                    deleteStmt.setLong(1, metadata.getMetadataID());
                    deleteStmt.executeUpdate();
                    deleteStmt.close();
                }
            }*/

            try {
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
                // retry once?
                PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM FEED_PERSISTENCE_METADATA WHERE FEED_ID = ? AND " +
                        "VERSION = ?");
                deleteStmt.setLong(1, dataFeedID);
                deleteStmt.setInt(2, metadata.getVersion());
                deleteStmt.executeUpdate();
                deleteStmt.close();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addOrUpdateMetadata(long dataFeedID, FeedPersistenceMetadata metadata, int dataSourceType) {
        Connection conn = Database.instance().getConnection();
        try {
            addOrUpdateMetadata(dataFeedID, metadata, conn, dataSourceType);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public static void liveDataSource(long dataSourceID, Connection conn, int dataSourceType) {
        FeedPersistenceMetadata metadata = new FeedPersistenceMetadata();
        metadata.setVersion(1);
        metadata.setLastData(new Date());
        addOrUpdateMetadata(dataSourceID, metadata, conn, dataSourceType);
    }

    private static FeedPersistenceMetadata createDefaultMetadata(Connection conn, FeedType feedType) {
        try {
            FeedPersistenceMetadata metadata = new FeedPersistenceMetadata();
            metadata.setVersion(1);
            metadata.setLastData(new Date());
            metadata.setDatabase(DatabaseManager.instance().chooseDatabase(conn, feedType));
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

    public void addRow(IRow row, List<AnalysisItem> fields, List<IDataTransform> transforms) throws SQLException {
        for (IDataTransform transform : transforms) {
            transform.handle((EIConnection) coreDBConn, row);
        }
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ");
        sqlBuilder.append(getTableName());
        StringBuilder paramBuilder = new StringBuilder();
        StringBuilder fieldBuilder = new StringBuilder();
        for (AnalysisItem field : fields) {
            if (field.persistable()) {
                KeyMetadata keyMetadata = keys.get(field.getKey());
                fieldBuilder.append(keyMetadata.createInsertClause()).append(",");
                paramBuilder.append(keyMetadata.createInsertQuestionMarks()).append(",");
            }
        }
        fieldBuilder.deleteCharAt(fieldBuilder.length() - 1);
        paramBuilder.deleteCharAt(paramBuilder.length() - 1);
        sqlBuilder.append(" ( ");
        sqlBuilder.append(fieldBuilder.toString());
        sqlBuilder.append(" ) ");
        sqlBuilder.append(" VALUES ( ");
        sqlBuilder.append(paramBuilder.toString());
        sqlBuilder.append(" ) ");
        PreparedStatement insertStmt = storageConn.prepareStatement(sqlBuilder.toString());
        int i = 1;
        for (AnalysisItem field : fields) {
            if (field.persistable()) {
                KeyMetadata keyMetadata = keys.get(field.getKey());
                i = setValue(insertStmt, i, keyMetadata, row.getValue(field.getKey()));
            }
        }
        insertStmt.execute();
    }

    public void addRow(ActualRow actualRow, List<AnalysisItem> fields, List<IDataTransform> transforms) throws SQLException {
        DataSet dataSet = new DataSet();
        IRow row = dataSet.createRow();
        for (AnalysisItem field : fields) {
            if (field.persistable()) {
                row.addValue(field.getKey(), actualRow.getValues().get(field.qualifiedName()));
            }
        }
        for (IDataTransform transform : transforms) {
            transform.handle((EIConnection) coreDBConn, row);
        }
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ");
        sqlBuilder.append(getTableName());
        StringBuilder paramBuilder = new StringBuilder();
        StringBuilder fieldBuilder = new StringBuilder();
        for (AnalysisItem field : fields) {
            if (field.persistable()) {
                KeyMetadata keyMetadata = keys.get(field.getKey());
                fieldBuilder.append(keyMetadata.createInsertClause()).append(",");
                paramBuilder.append(keyMetadata.createInsertQuestionMarks()).append(",");
            }
        }
        fieldBuilder.deleteCharAt(fieldBuilder.length() - 1);
        paramBuilder.deleteCharAt(paramBuilder.length() - 1);
        sqlBuilder.append(" ( ");
        sqlBuilder.append(fieldBuilder.toString());
        sqlBuilder.append(" ) ");
        sqlBuilder.append(" VALUES ( ");
        sqlBuilder.append(paramBuilder.toString());
        sqlBuilder.append(" ) ");
        PreparedStatement insertStmt = storageConn.prepareStatement(sqlBuilder.toString());
        int i = 1;
        for (AnalysisItem field : fields) {
            if (field.persistable()) {
                KeyMetadata keyMetadata = keys.get(field.getKey());
                i = setValue(insertStmt, i, keyMetadata, row.getValue(field.getKey()));
            }
        }
        insertStmt.execute();
    }

    public void updateRow(IRow newRow, List<AnalysisItem> fields, List<IDataTransform> transforms, long rowID, List<AnalysisItem> allFields) throws SQLException {
        IRow row = rowByID(rowID, allFields);
        for (AnalysisItem field : fields) {
            row.addValue(field.getKey(), newRow.getValues().get(field.getKey()));
        }
        for (IDataTransform transform : transforms) {
            transform.handle((EIConnection) coreDBConn, row);
        }

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("UPDATE ");
        sqlBuilder.append(getTableName());
        sqlBuilder.append(" SET ");
        for (AnalysisItem field : allFields) {
            if (field.persistable()) {
                KeyMetadata keyMetadata = keys.get(field.getKey());
                sqlBuilder.append(keyMetadata.createUpdateClause());
                sqlBuilder.append(",");
            }
        }
        sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
        sqlBuilder.append(" WHERE ").append(getTableName()).append("_ID").append(" = ?");
        PreparedStatement updateStmt = storageConn.prepareStatement(sqlBuilder.toString());
        int i = 1;
        for (AnalysisItem field : allFields) {
            if (field.persistable()) {
                KeyMetadata keyMetadata = keys.get(field.getKey());
                i = setValue(updateStmt, i, keyMetadata, row.getValues().get(field.getKey()));
            }
        }
        updateStmt.setLong(i, rowID);
        int rows = updateStmt.executeUpdate();
    }

    public void updateRow(ActualRow actualRow, List<AnalysisItem> fields, List<IDataTransform> transforms) throws SQLException {
        DataSet dataSet = new DataSet();
        Row row = (Row) dataSet.createRow();
        row.setRowID(actualRow.getRowID());
        for (AnalysisItem field : fields) {
            if (field.persistable()) {
                row.addValue(field.getKey(), actualRow.getValues().get(field.qualifiedName()));
            }
        }
        for (IDataTransform transform : transforms) {
            transform.handle((EIConnection) coreDBConn, row);
        }
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("UPDATE ");
        sqlBuilder.append(getTableName());
        sqlBuilder.append(" SET ");
        for (AnalysisItem field : fields) {
            if (field.persistable()) {
                KeyMetadata keyMetadata = keys.get(field.getKey());
                sqlBuilder.append(keyMetadata.createUpdateClause());
                sqlBuilder.append(",");
            }
        }
        sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
        sqlBuilder.append(" WHERE ").append(getTableName()).append("_ID").append(" = ?");
        PreparedStatement updateStmt = storageConn.prepareStatement(sqlBuilder.toString());
        int i = 1;
        for (AnalysisItem field : fields) {
            if (field.persistable()) {
                KeyMetadata keyMetadata = keys.get(field.getKey());
                i = setValue(updateStmt, i, keyMetadata, row.getValues().get(field.getKey()));
            }
        }
        updateStmt.setLong(i, row.getRowID());
        int rows = updateStmt.executeUpdate();
    }

    public void deleteRow(long rowID) throws SQLException {
        PreparedStatement deleteStmt = storageConn.prepareStatement("DELETE FROM " + getTableName() + " WHERE " + getTableName() + "_ID = ?");
        deleteStmt.setLong(1, rowID);
        deleteStmt.executeUpdate();
    }

    private IRow rowByID(long rowID, List<AnalysisItem> fields) throws SQLException {
        DataSet dataSet = new DataSet();
        IRow existingDataRow = dataSet.createRow();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT ");
        List<AnalysisItem> validFields = new ArrayList<AnalysisItem>();
        for (AnalysisItem field : fields) {
            if (field.persistable()) {
                if (field.isKeyColumn()) {
                    field.setPkKeyName(getTableName() + "_ID");
                }
                validFields.add(field);
                sqlBuilder.append(field.getKey().toSQL());
                sqlBuilder.append(",");
            }
        }
        String pk = getTableName() + "_ID";
        sqlBuilder.append(pk);
        //sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
        sqlBuilder.append(" FROM ");
        sqlBuilder.append(getTableName());
        sqlBuilder.append(" WHERE ").append(pk).append(" = ?");
        PreparedStatement queryStmt = storageConn.prepareStatement(sqlBuilder.toString());
        queryStmt.setLong(1, rowID);
        ResultSet dataRS = queryStmt.executeQuery();
        dataRS.next();
        int i = 1;

        for (AnalysisItem analysisItem : fields) {
            if (analysisItem.persistable()) {

                KeyMetadata keyMetadata = keys.get(analysisItem.getKey());
                Value value;
                if (keyMetadata.getType() == Value.DATE) {
                    Timestamp time = dataRS.getTimestamp(i++);
                    if (dataRS.wasNull()) {
                        value = new EmptyValue();
                    } else {
                        DateValue dateValue = new DateValue(new Date(time.getTime()));
                        value = dateValue;
                    }
                } else if (keyMetadata.getType() == Value.NUMBER) {
                    double doubleValue = dataRS.getDouble(i++);
                    if (dataRS.wasNull()) {
                        value = new EmptyValue();
                    } else {
                        value = new NumericValue(doubleValue);
                    }
                } else {
                    String stringVavlue = dataRS.getString(i++);
                    if (dataRS.wasNull()) {
                        value = new EmptyValue();
                    } else {
                        value = new StringValue(stringVavlue);
                    }
                }
                existingDataRow.addValue(analysisItem.getKey(), value);

            }
        }
        return existingDataRow;
    }

    public ActualRowSet allData(@NotNull Collection<FilterDefinition> filters, @NotNull List<AnalysisItem> fields, @Nullable Integer limit,
                                InsightRequestMetadata insightRequestMetadata) throws SQLException {
        Calendar cal = Calendar.getInstance();
        Calendar shiftedCal = Calendar.getInstance();
        int timeOffset = insightRequestMetadata.getUtcOffset() / 60;
        String string;
        if (timeOffset > 0) {
            string = "GMT-"+Math.abs(timeOffset);
        } else if (timeOffset < 0) {
            string = "GMT+"+Math.abs(timeOffset);
        } else {
            string = "GMT";
        }
        TimeZone timeZone = TimeZone.getTimeZone(string);
        shiftedCal.setTimeZone(timeZone);
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT ");
        List<AnalysisItem> validFields = new ArrayList<AnalysisItem>();
        for (AnalysisItem field : fields) {
            if (field.persistable()) {
                for (FilterDefinition filter : filters) {
                    if (field.getKey().toBaseKey().toKeyString().equals(filter.getField().getKey().toBaseKey().toKeyString())) {
                        field = filter.getField();
                    }
                }
                if (field.isKeyColumn()) {
                    field.setPkKeyName(getTableName() + "_ID");
                }
                validFields.add(field);
                sqlBuilder.append(field.getKey().toSQL());
                sqlBuilder.append(",");
            }
        }
        String pk = getTableName() + "_ID";
        sqlBuilder.append(pk);
        //sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
        sqlBuilder.append(" FROM ");
        sqlBuilder.append(getTableName());
        if (filters.size() > 0) {
            sqlBuilder.append(" WHERE ");
            for (FilterDefinition filterDefinition : filters) {
                sqlBuilder.append(filterDefinition.toQuerySQL(getTableName(), database));
                sqlBuilder.append(",");
            }
            sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
        }
        if (limit != null) {
            sqlBuilder.append(" LIMIT " + limit);
        }
        PreparedStatement queryStmt = storageConn.prepareStatement(sqlBuilder.toString());
        int position = 1;
        Map<Key, Key> lookup = new HashMap<Key, Key>();
        for (AnalysisItem field : fields) {
            lookup.put(field.getKey().toBaseKey(), field.createAggregateKey(false));
        }
        for (FilterDefinition filterDefinition : filters) {
            KeyMetadata keyMetadata = keys.get(lookup.get(filterDefinition.getField().getKey().toBaseKey()));
            int type = keyMetadata.getType();
            filterDefinition.populatePreparedStatement(queryStmt, position, type, insightRequestMetadata);
        }
        List<ActualRow> rows = new ArrayList<ActualRow>();
        ResultSet dataRS = queryStmt.executeQuery();
        while (dataRS.next()) {
            int i = 1;
            ActualRow actualRow = new ActualRow();
            rows.add(actualRow);
            Map<String, Value> valueMap = new HashMap<String, Value>();
            for (AnalysisItem analysisItem : fields) {
                if (analysisItem.persistable()) {
                    KeyMetadata keyMetadata = keys.get(analysisItem.createAggregateKey(false));
                    Value value;
                    if (keyMetadata.getType() == Value.DATE) {
                        Timestamp time = dataRS.getTimestamp(i++);
                        if (dataRS.wasNull()) {
                            value = new EmptyValue();
                        } else {
                            DateValue dateValue = new DateValue(new Date(time.getTime()));
                            dateValue.calculate(cal);
                            value = dateValue;
                        }
                    } else if (keyMetadata.getType() == Value.NUMBER) {
                        double doubleValue = dataRS.getDouble(i++);
                        if (dataRS.wasNull()) {
                            value = new EmptyValue();
                        } else {
                            value = new NumericValue(doubleValue);
                        }
                    } else {
                        String stringVavlue = dataRS.getString(i++);
                        if (dataRS.wasNull()) {
                            value = new EmptyValue();
                        } else {
                            value = new StringValue(stringVavlue);
                        }
                    }
                    valueMap.put(analysisItem.qualifiedName(), value);
                }
            }
            actualRow.setValues(valueMap);
            actualRow.setRowID(dataRS.getLong(pk));
        }
        ActualRowSet actualRowSet = new ActualRowSet();
        actualRowSet.setAnalysisItems(validFields);
        actualRowSet.setRows(rows);
        return actualRowSet;
    }

    public File archive(@NotNull List<AnalysisItem> fields,
                        InsightRequestMetadata insightRequestMetadata) throws SQLException, IOException, S3ServiceException {
        Calendar cal = Calendar.getInstance();
        Calendar shiftedCal = Calendar.getInstance();
        int timeOffset = insightRequestMetadata.getUtcOffset() / 60;
        String string;
        if (timeOffset > 0) {
            string = "GMT-"+Math.abs(timeOffset);
        } else if (timeOffset < 0) {
            string = "GMT+"+Math.abs(timeOffset);
        } else {
            string = "GMT";
        }
        TimeZone timeZone = TimeZone.getTimeZone(string);
        shiftedCal.setTimeZone(timeZone);
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT ");
        List<AnalysisItem> validFields = new ArrayList<AnalysisItem>();

        File file = new File(System.currentTimeMillis() + ".csv");
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos, 512);
        CsvWriter csvWriter = new CsvWriter(bos, ',', Charset.forName("UTF-8"));

        for (AnalysisItem field : fields) {
            if (field.persistable()) {
                if (field.isKeyColumn()) {
                    field.setPkKeyName(getTableName() + "_ID");
                }
                validFields.add(field);
                sqlBuilder.append(field.getKey().toSQL());
                sqlBuilder.append(",");
            }

        }
        String[] fieldNames = new String[validFields.size()];
        int fieldCounter = 0;
        for (AnalysisItem analysisItem : validFields) {
            fieldNames[fieldCounter++] = analysisItem.getKey().toBaseKey().toKeyString();
        }
        csvWriter.writeRecord(fieldNames);
        String pk = getTableName() + "_ID";
        sqlBuilder.append(pk);
        //sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
        sqlBuilder.append(" FROM ");
        sqlBuilder.append(getTableName());



        PreparedStatement queryStmt = storageConn.prepareStatement(sqlBuilder.toString(), java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
        queryStmt.setFetchSize(Integer.MIN_VALUE);

        /*
        stmt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
stmt.setFetchSize(Integer.MIN_VALUE);
         */
        ResultSet dataRS = queryStmt.executeQuery();
        int counter = 0;
        while (dataRS.next()) {
            int i = 1;
            String[] records = new String[validFields.size()];
            for (AnalysisItem analysisItem : validFields) {
                if (analysisItem.persistable()) {
                    KeyMetadata keyMetadata = keys.get(analysisItem.createAggregateKey(false));
                    Value value;
                    if (keyMetadata.getType() == Value.DATE) {
                        Timestamp time = dataRS.getTimestamp(i++);
                        if (dataRS.wasNull()) {
                            value = new EmptyValue();
                        } else {
                            DateValue dateValue = new DateValue(new Date(time.getTime()));
                            dateValue.calculate(cal);
                            value = dateValue;
                        }
                    } else if (keyMetadata.getType() == Value.NUMBER) {
                        double doubleValue = dataRS.getDouble(i++);
                        if (dataRS.wasNull()) {
                            value = new EmptyValue();
                        } else {
                            value = new NumericValue(doubleValue);
                        }
                    } else {
                        String stringVavlue = dataRS.getString(i++);
                        if (dataRS.wasNull()) {
                            value = new EmptyValue();
                        } else {
                            value = new StringValue(stringVavlue);
                        }
                    }
                    records[i - 2] = value.toString();
                    //valueMap.put(analysisItem.qualifiedName(), value);
                }
            }
            csvWriter.writeRecord(records);
            counter++;
            if (counter == 100) {
                csvWriter.flush();
                counter = 0;
            }
        }
        dataRS.close();
        csvWriter.flush();
        csvWriter.close();
        fos.close();
        return file;
    }

    public static void main(String[] args) {
        AmazonS3 s3 = new AmazonS3Client(new BasicAWSCredentials("AKIAI5YYYFRMWFLLEC2A", "NmonY27/vE03AeGNWhLBmkR41kJrvbWSYhLzh5pE"));
        List<Bucket> buckets = s3.listBuckets();
        for (Bucket bucket : buckets) {
            if (bucket.getName().startsWith("refreshdt")) {
                System.out.println(bucket.getName());
                String bucketName = bucket.getName();
                ObjectListing objectListing = s3.listObjects(bucketName);
                List<S3ObjectSummary> summaries = objectListing.getObjectSummaries();
                for (S3ObjectSummary summary : summaries) {
                    System.out.println("\t" + summary.getKey());
                    s3.deleteObject(bucketName, summary.getKey());
                }
                s3.deleteBucket(bucketName);
            }
            /*ObjectListing objectListing = s3.listObjects();
            List<S3ObjectSummary> summaries = objectListing.getObjectSummaries();
            for (S3ObjectSummary summary : summaries) {
                System.out.println("\t" + summary.getKey());
                s3.deleteObject(bucketName, summary.getKey());
            }
            s3.deleteBucket(bucketName);*/
        }
    }
}
