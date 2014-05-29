package com.easyinsight.datafeeds;

import com.easyinsight.cache.MemCachedManager;
import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.logging.LogClass;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * User: jboe
 * Date: Jan 3, 2008
 * Time: 3:37:02 PM
 */
public class FeedRegistry {
    private FeedStorage feedStorage = new FeedStorage();
    private static FeedRegistry instance;

    public static void initialize() {
        instance = new FeedRegistry();
    }

    public static FeedRegistry instance() {
        return instance;
    }

    public void flushCache(long feedID) {
        MemCachedManager.delete("feed" + feedID);
    }

    public Feed getFeed(long identifier) {
        Feed feed = (Feed) MemCachedManager.get("feed" + identifier);
        EIConnection conn = Database.instance().getConnection();
        try {
            if (feed == null || !isLatestVersion(feed, identifier, conn)) {
                LogClass.debug("Cache miss for feed id: " + identifier);
                FeedDefinition feedDefinition;
                feedDefinition = feedStorage.getFeedDefinitionData(identifier, conn);
                feed = feedDefinition.createFeed(conn);
                feed.setVersion(getLatestVersion(identifier, conn));
                MemCachedManager.add("feed" + identifier, 10000, feed);
            }
            else
                LogClass.debug("Cache hit for feed id: " + identifier);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
        return feed;
    }

    public Feed getFeed(long identifier, EIConnection conn) {
        Feed feed = (Feed) MemCachedManager.get("feed" + identifier);

        try {
            if (feed == null || !isLatestVersion(feed, identifier, conn)) {
                LogClass.debug("Cache miss for feed id: " + identifier);
                FeedDefinition feedDefinition;
                feedDefinition = feedStorage.getFeedDefinitionData(identifier, conn);
                feed = feedDefinition.createFeed(conn);
                feed.setVersion(getLatestVersion(identifier, conn));
                MemCachedManager.add("feed" + identifier, 10000, feed);
            }
            else
                LogClass.debug("Cache hit for feed id: " + identifier);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return feed;
    }

    public Feed getFeed(long identifier, EIConnection conn, FeedDefinition parentSource) {
        Feed feed = (Feed) MemCachedManager.get("feed" + identifier);

        try {
            if (feed == null || !isLatestVersion(feed, identifier, conn)) {
                LogClass.debug("Cache miss for feed id: " + identifier);
                FeedDefinition feedDefinition;
                feedDefinition = feedStorage.getFeedDefinitionData(identifier, conn);
                feed = feedDefinition.createFeed(conn, parentSource);
                feed.setVersion(getLatestVersion(identifier, conn));
                MemCachedManager.add("feed" + identifier, 10000, feed);
            }
            else
                LogClass.debug("Cache hit for feed id: " + identifier);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return feed;
    }

    private boolean isLatestVersion(Feed feed, long identifier, EIConnection conn) throws SQLException {
        if(feed == null)
            return false;
        LogClass.debug("Feed version: " + feed.getVersion());
        int latestVersion = getLatestVersion(identifier, conn);
        LogClass.debug("Latest version: " + latestVersion);
        return feed.getVersion() == latestVersion;
    }

    private int getLatestVersion(long identifier, EIConnection conn) throws SQLException {
        int version = 0;

        PreparedStatement stmt = conn.prepareStatement("SELECT MAX(VERSION) FROM feed_persistence_metadata WHERE feed_id = ?");
        stmt.setLong(1, identifier);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            version = rs.getInt(1);
        }
        stmt.close();
        return version;
    }


}
