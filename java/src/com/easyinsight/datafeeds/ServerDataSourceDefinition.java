package com.easyinsight.datafeeds;

import com.easyinsight.users.Credentials;
import com.easyinsight.users.User;
import com.easyinsight.users.SubscriptionLicense;
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

import org.jetbrains.annotations.NotNull;
import org.hibernate.Session;
import flex.messaging.MessageBroker;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.util.UUIDUtils;

/**
 * User: James Boe
 * Date: Mar 30, 2009
 * Time: 8:50:59 PM
 */
public abstract class ServerDataSourceDefinition extends FeedDefinition implements IServerDataSourceDefinition {

    private String username;
    private String password;
    private String sessionId;


    public void setCredentialsDefinition(int i) { }

    public long create(Credentials credentials, Connection conn) throws SQLException, CloneNotSupportedException {
        DataStorage metadata = null;
        try {
            Map<String, Key> keys = newDataSourceFields(credentials);
            DataSet dataSet = getDataSet(credentials, keys, new Date(), null);
            setFields(createAnalysisItems(keys, dataSet, credentials, conn));
            setOwnerName(retrieveUser(conn, SecurityUtil.getUserID()).getUserName());
            UploadPolicy uploadPolicy = new UploadPolicy(SecurityUtil.getUserID());
            setUploadPolicy(uploadPolicy);
            FeedCreationResult feedCreationResult = new FeedCreation().createFeed(this, conn, dataSet, SecurityUtil.getUserID());
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
                user.setLicenses(new ArrayList<SubscriptionLicense>());
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
     */
    @NotNull
    protected abstract List<String> getKeys();

    public Map<String, Key> newDataSourceFields(Credentials credentials) {
        Map<String, Key> keyMap = new HashMap<String, Key>();
        if (getDataFeedID() == 0) {
            List<String> keys = getKeys();
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

    protected void addData(DataStorage dataStorage, DataSet dataSet) throws SQLException {
        dataStorage.truncate();
        dataStorage.insertData(dataSet);
    }

    public CredentialsResponse refreshData(Credentials credentials, long accountID, Date now, FeedDefinition parentDefinition) {
        Connection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            refreshData(credentials, accountID, now, conn, null);
            conn.commit();
            return new CredentialsResponse(true);
        } catch (Exception e) {
            LogClass.error(e);
            try {
                conn.rollback();
            } catch (SQLException e1) {
                LogClass.error(e1);
            }
            return new CredentialsResponse(false, e.getMessage());
        } finally {
            Database.instance().closeConnection(conn);
        }
    }

    public boolean refreshData(Credentials credentials, long accountID, Date now, Connection conn, FeedDefinition parentDefinition) throws Exception {
        DataStorage dataStorage = null;
        try {
            if(credentials == null) {
                if(this.getCredentialsDefinition() == CredentialsDefinition.STANDARD_USERNAME_PW) {
                    credentials = new Credentials();
                    credentials.setUserName(getUsername());
                    credentials.setPassword(retrievePassword());
                }
            }
            Map<String, Key> keys = newDataSourceFields(credentials);
            DataSet dataSet = getDataSet(credentials, newDataSourceFields(credentials), now, parentDefinition);
            List<AnalysisItem> items = createAnalysisItems(keys, dataSet, credentials, conn);
            dataStorage = DataStorage.writeConnection(this, conn, accountID);
            int version = dataStorage.getVersion();
            int newVersion = dataStorage.migrate(getFields(), items);
            addData(dataStorage, dataSet);
            dataStorage.commit();
            notifyOfDataUpdate();
            return version != newVersion;
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

    private void notifyOfDataUpdate() {
        MessageBroker msgBroker = MessageBroker.getMessageBroker(null);
        String clientID = UUIDUtils.createUUID();
        AsyncMessage msg = new AsyncMessage();
        msg.setDestination("dataUpdates");
        msg.setHeader(AsyncMessage.SUBTOPIC_HEADER_NAME, String.valueOf(getDataFeedID()));
        msg.setMessageId(clientID);
        msg.setTimestamp(System.currentTimeMillis());
        if (msgBroker != null) {
            msgBroker.routeMessageToService(msg, null);
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // This is not a getter so that we don't pass the value on to the client

    public String retrievePassword() {
        return password;
    }

    public String getPassword() {
        return null;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

}
