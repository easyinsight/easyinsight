package com.easyinsight.datafeeds;

import com.easyinsight.analysis.ReportCache;
import com.easyinsight.analysis.ReportException;
import com.easyinsight.database.EIConnection;
import com.easyinsight.scorecard.DataSourceRefreshEvent;
import com.easyinsight.storage.IDataStorage;
import com.easyinsight.storage.TempStorage;
import com.easyinsight.users.User;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.core.Key;
import com.easyinsight.core.NamedKey;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.userupload.CredentialsResponse;
import com.easyinsight.userupload.UploadPolicy;
import com.easyinsight.database.Database;
import com.easyinsight.storage.DataStorage;
import com.easyinsight.logging.LogClass;
import com.easyinsight.security.SecurityUtil;

import java.util.*;
import java.sql.Connection;
import java.sql.SQLException;

import com.easyinsight.util.ServiceUtil;
import org.jetbrains.annotations.NotNull;
import org.hibernate.Session;

import javax.servlet.http.HttpServletRequest;

/**
 * User: James Boe
 * Date: Mar 30, 2009
 * Time: 8:50:59 PM
 */
public abstract class ServerDataSourceDefinition extends FeedDefinition implements IServerDataSourceDefinition {

    public void loadingProgress(int current, int total, String message, String callDataID) {
        if (callDataID != null) {
            DataSourceRefreshEvent info = new DataSourceRefreshEvent();
            info.setDataSourceID(getParentSourceID() == 0 ? getDataFeedID() : getParentSourceID());
            info.setDataSourceName(message);
            info.setType(DataSourceRefreshEvent.PROGRESS);
            info.setUserId(SecurityUtil.getUserID());
            info.setCurrent(current);
            info.setMax(total);
            ServiceUtil.instance().updateStatus(callDataID, ServiceUtil.RUNNING, info);
        }
    }

    public void defineCustomFields() {

    }

    public void exchangeTokens(EIConnection conn, HttpServletRequest request, String externalPin) throws Exception {
    }

    public long create(EIConnection conn, List<AnalysisItem> externalAnalysisItems, FeedDefinition parentDefinition) throws Exception {
        DataStorage metadata = null;
        try {
            if (externalAnalysisItems == null) {
                Map<String, Key> keys = newDataSourceFields(parentDefinition);
                List<AnalysisItem> fields = createAnalysisItems(keys, conn, parentDefinition);
                setFields(fields);
            } else {
                setFields(externalAnalysisItems);
            }
            setOwnerName(retrieveUser(conn, SecurityUtil.getUserID()).getUserName());
            UploadPolicy uploadPolicy = new UploadPolicy(SecurityUtil.getUserID(), SecurityUtil.getAccountID());
            setUploadPolicy(uploadPolicy);
            FeedCreationResult feedCreationResult = new FeedCreation().createFeed(this, conn, new DataSet(), uploadPolicy);
            metadata = feedCreationResult.getTableDefinitionMetadata();
            if (metadata != null) metadata.commit();
            return feedCreationResult.getFeedID();
        } catch (SQLException e) {
            if (metadata != null) {
                metadata.rollback();
            }
            throw e;
        } finally {
            if (metadata != null) {
                metadata.closeConnection();
            }
        }
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
            } catch (Exception e) {
                session.getTransaction().rollback();
                throw new RuntimeException(e);
            } finally {
                session.close();
            }
            if (results.size() > 0) {
                user = (User) results.get(0);
            }
            return user;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * The names of the fields used in this data source
     * @return the key names
     * @param parentDefinition
     */
    @NotNull
    protected abstract List<String> getKeys(FeedDefinition parentDefinition);

    public Map<String, Key> newDataSourceFields(FeedDefinition parentDefinition) {
        Map<String, Key> keyMap = new HashMap<String, Key>();
        if (getFields().size() == 0) {
            List<String> keys = getKeys(parentDefinition);
            for (String key : keys) {
                keyMap.put(key, new NamedKey(key));
            }
        } else {
            for (AnalysisItem field : getFields()) {
                keyMap.put(field.getKey().toKeyString(), field.getKey());
            }
        }
        return keyMap;
    }

    protected void addData(IDataStorage IDataStorage, DataSet dataSet) throws Exception {

        if (dataSet != null) {
            IDataStorage.insertData(dataSet);
        }
    }

    public CredentialsResponse refreshData(long accountID, Date now, FeedDefinition parentDefinition, String callDataID, Date lastRefreshTime) {
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            refreshData(accountID, now, conn, parentDefinition, callDataID, lastRefreshTime);
            conn.commit();
            ReportCache.instance().flushResults(getDataFeedID());
            return new CredentialsResponse(true, getDataFeedID());
        } catch (ReportException re) {
            return new CredentialsResponse(false, re.getReportFault());
        } catch (Exception e) {
            LogClass.error(e);
            conn.rollback();
            return new CredentialsResponse(false, e.getMessage(), getDataFeedID());
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
    }

    public MigrationResult migrations(EIConnection conn, FeedDefinition parentDefinition) throws Exception {
        boolean changed = false;
        Map<String, Key> keys = newDataSourceFields(parentDefinition);
        List<AnalysisItem> fields = createAnalysisItems(keys, conn, parentDefinition);
        List<AnalysisItem> newFields = new ArrayList<AnalysisItem>();
        for (AnalysisItem field : fields) {
            if (field == null) {
                continue;
            }
            if (field.getKey() == null) {
                continue;
            }
            AnalysisItem existingField = findAnalysisItemByKey(field.getKey().toKeyString());
            if (existingField == null) {
                newFields.add(field);
            }
        }
        if (newFields.size() > 0) {
            changed = true;
            System.out.println("Discovered new fields = " + newFields);
            for (AnalysisItem newField : newFields) {
                getFields().add(newField);
                keys.put(newField.getKey().toKeyString(), newField.getKey());
            }
            new DataSourceInternalService().updateFeedDefinition(this, conn, true, false);
        }
        return new MigrationResult(changed, keys);
    }

    public String tempLoad(Map<String, Key> keys, Date now, FeedDefinition parentDefinition, String callDataID, Date lastRefreshTime, EIConnection conn) throws Exception {
        TempStorage tempStorage = DataStorage.tempConnection(this, conn);
        boolean insertTemp = clearsData(parentDefinition) || lastRefreshTime == null || lastRefreshTime.getTime() < 100;
        String sql;
        if (insertTemp) {
            sql = tempStorage.defineTempInsertTable();
        } else {
            sql = tempStorage.defineTempUpdateTable();
        }
        tempStorage.createTable(sql);
        System.out.println("Refreshing " + getDataFeedID() + " - " + getFeedName() + " at " + new Date());
        DataSet dataSet = getDataSet(keys, now, parentDefinition, tempStorage, conn, callDataID, lastRefreshTime);
        if (dataSet != null) {
            tempStorage.insertData(dataSet);
        }
        System.out.println("Completed refresh of " + getDataFeedID() + " at " + new Date());
        return tempStorage.getTableName();
    }

    protected Key getUpdateKey() {
        return findAnalysisItem(getUpdateKeyName()).getKey();
    }

    protected String getUpdateKeyName() {
        return null;
    }

    public void applyTempLoad(EIConnection conn, long accountID, FeedDefinition parentDefinition, Date lastRefreshTime, String tempTable) throws Exception {
        DataStorage dataStorage = null;
        try {
            dataStorage = DataStorage.writeConnection(this, conn, accountID);
            boolean insert = clearsData(parentDefinition) || lastRefreshTime == null || lastRefreshTime.getTime() < 100;
            if (insert) {
                dataStorage.truncate();
                dataStorage.insertFromSelect(tempTable);
            } else {
                if (getUpdateKeyName() == null) {
                    dataStorage.insertFromSelect(tempTable);
                } else {
                    dataStorage.updateFromTemp(tempTable, getUpdateKey());
                }
            }
            dataStorage.commit();
        } catch (Exception e) {
            if (dataStorage != null) {
                dataStorage.rollback();
            }
            throw e;
        } finally {
            if (dataStorage != null) {
                dataStorage.closeConnection();
            }
        }
    }

    public boolean refreshData(long accountID, Date now, EIConnection conn, FeedDefinition parentDefinition, String callDataID, Date lastRefreshTime) throws Exception {
        boolean changed = false;
        DataStorage dataStorage = null;
        try {
            Map<String, Key> keys = newDataSourceFields(parentDefinition);
            List<AnalysisItem> fields = createAnalysisItems(keys, conn, parentDefinition);
            List<AnalysisItem> newFields = new ArrayList<AnalysisItem>();
            for (AnalysisItem field : fields) {
                if (field == null) {
                    continue;
                }
                if (field.getKey() == null) {
                    continue;
                }
                AnalysisItem existingField = findAnalysisItemByKey(field.getKey().toKeyString());
                if (existingField == null) {
                    newFields.add(field);
                }
            }
            if (newFields.size() > 0) {
                changed = true;
                System.out.println("Discovered new fields = " + newFields);
                for (AnalysisItem newField : newFields) {
                    getFields().add(newField);
                    keys.put(newField.getKey().toKeyString(), newField.getKey());
                }
                new DataSourceInternalService().updateFeedDefinition(this, conn, true, false);
            }
            dataStorage = DataStorage.writeConnection(this, conn, accountID);
            System.out.println("Refreshing " + getDataFeedID() + " for account " + accountID + " at " + new Date());
            if (clearsData(parentDefinition) || lastRefreshTime == null || lastRefreshTime.getTime() < 100) {
                dataStorage.truncate(); 
            }
            DataSet dataSet = getDataSet(keys, now, parentDefinition, dataStorage, conn, callDataID, lastRefreshTime);
            addData(dataStorage, dataSet);
            dataStorage.commit();
            System.out.println("Completed refresh of " + getDataFeedID() + " for account " + accountID + " at " + new Date());
        } catch (Exception e) {
            if (dataStorage != null) {
                dataStorage.rollback();
            }
            throw e;
        } finally {
            if (dataStorage != null) {
                dataStorage.closeConnection();
            }
        }
        return changed;
    }

    protected boolean clearsData(FeedDefinition parentSource) {
        return true;
    }

}
