package com.easyinsight.groups;

import org.hibernate.Session;
import com.easyinsight.database.Database;
import com.easyinsight.analysis.Tag;
import com.easyinsight.analysis.WSAnalysisDefinition;
import com.easyinsight.analysis.AnalysisStorage;
import com.easyinsight.logging.LogClass;
import com.easyinsight.datafeeds.FeedDescriptor;
import com.easyinsight.datafeeds.FeedConsumer;
import com.easyinsight.userupload.UploadPolicy;
import com.easyinsight.email.UserStub;
import com.easyinsight.security.Roles;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.audit.AuditMessage;

import java.util.*;
import java.util.Date;
import java.sql.*;

/**
 * User: James Boe
 * Date: Aug 28, 2008
 * Time: 10:50:24 AM
 */
public class GroupStorage {

    public long addGroupComment(GroupComment groupComment) {
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement insertCommentStmt = conn.prepareStatement("INSERT INTO group_comment (group_id, user_id, comment, time_created) values (?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            insertCommentStmt.setLong(1, groupComment.getGroupID());
            insertCommentStmt.setLong(2, groupComment.getUserID());
            insertCommentStmt.setString(3, groupComment.getMessage());
            insertCommentStmt.setDate(4, new java.sql.Date(new Date().getTime()));
            insertCommentStmt.execute();
            return Database.instance().getAutoGenKey(insertCommentStmt);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.instance().closeConnection(conn);
        }
    }

    public void addGroupAudit(GroupAuditMessage groupAuditMessage, Connection conn) {
        try {
            PreparedStatement insertCommentStmt = conn.prepareStatement("INSERT INTO group_audit_message (group_id, user_id, comment, audit_time) values (?, ?, ?, ?)");
            insertCommentStmt.setLong(1, groupAuditMessage.getGroupID());
            insertCommentStmt.setLong(2, groupAuditMessage.getUserID());
            insertCommentStmt.setString(3, groupAuditMessage.getMessage());
            insertCommentStmt.setDate(4, new java.sql.Date(new Date().getTime()));
            insertCommentStmt.execute();
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public long addGroup(Group group, long userID, Connection conn) throws SQLException {
        PreparedStatement insertGroupStmt = conn.prepareStatement("INSERT INTO COMMUNITY_GROUP " +
                    "(NAME, DESCRIPTION, PUBLICLY_JOINABLE, PUBLICLY_VISIBLE)" +
                    "VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        insertGroupStmt.setString(1, group.getName());
        insertGroupStmt.setString(2, group.getDescription());
        insertGroupStmt.setBoolean(3, group.isPubliclyJoinable());
        insertGroupStmt.setBoolean(4, group.isPubliclyVisible());
        insertGroupStmt.execute();
        long groupID = Database.instance().getAutoGenKey(insertGroupStmt);
        addUserToGroup(userID, groupID, GroupToUserBinding.OWNER, conn);
        saveTags(group.getTags(), groupID, conn);
        return groupID;
    }

    public long addGroup(Group group, long userID) {
        Connection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            long groupID = addGroup(group, userID, conn);
            conn.commit();
            return groupID;
        } catch (Exception e) {
            LogClass.error(e);
            try {
                conn.rollback();
            } catch (SQLException e1) {
                LogClass.error(e1);
            }
            throw new RuntimeException(e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LogClass.error(e);
            }
            Database.instance().closeConnection(conn);
        }
    }

    public Group getGroup(long groupID) {
        Group group = null;
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT NAME, DESCRIPTION, PUBLICLY_JOINABLE, PUBLICLY_VISIBLE FROM " +
                    "COMMUNITY_GROUP WHERE COMMUNITY_GROUP_ID = ?");
            queryStmt.setLong(1, groupID);
            ResultSet rs = queryStmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString(1);
                String description = rs.getString(2);
                boolean publiclyJoinable = rs.getBoolean(3);
                boolean publiclyVisible = rs.getBoolean(4);
                group = new Group();
                group.setName(name);
                group.setGroupID(groupID);
                group.setDescription(description);
                group.setPubliclyJoinable(publiclyJoinable);
                group.setPubliclyVisible(publiclyVisible);
                group.setTags(new ArrayList<Tag>(getTags(groupID, conn)));
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.instance().closeConnection(conn);
        }
        return group;
    }

    public void updateGroup(Group group, Connection conn) {
        try {
            PreparedStatement updateGroupStmt = conn.prepareStatement("UPDATE COMMUNITY_GROUP " +
                    "SET NAME = ?, DESCRIPTION = ?, PUBLICLY_JOINABLE = ?, PUBLICLY_VISIBLE = ? WHERE COMMUNITY_GROUP_ID = ?");
            updateGroupStmt.setString(1, group.getName());
            updateGroupStmt.setString(2, group.getDescription());
            updateGroupStmt.setBoolean(3, group.isPubliclyJoinable());
            updateGroupStmt.setBoolean(4, group.isPubliclyVisible());
            updateGroupStmt.setLong(5, group.getGroupID());
            int rows = updateGroupStmt.executeUpdate();
            if (rows != 1) {
                throw new RuntimeException("Update failed");
            }
            saveTags(group.getTags(), group.getGroupID(), conn);
            conn.commit();
        } catch (Exception e) {
            LogClass.error(e);
            try {
                conn.rollback();
            } catch (SQLException e1) {
                LogClass.error(e1);
            }
            throw new RuntimeException(e);
        } finally {
        }
    }

    public Set<Tag> getTags(long groupID, Connection conn) throws SQLException {
        PreparedStatement queryTagsStmt = conn.prepareStatement("SELECT ANALYSIS_TAGS_ID FROM GROUP_TO_TAG WHERE GROUP_ID = ?");
        queryTagsStmt.setLong(1, groupID);
        Set<Long> tagIDs = new HashSet<Long>();
        ResultSet rs = queryTagsStmt.executeQuery();
        while (rs.next()) {
            tagIDs.add(rs.getLong(1));
        }
        queryTagsStmt.close();
        Set<Tag> tags = new HashSet<Tag>();
        Session session = Database.instance().createSession();
        try {
            session.beginTransaction();
            for (Long tagID : tagIDs) {
                List items = session.createQuery("from Tag where tagID = ?").setLong(0, tagID).list();
                if (items.size() > 0) {
                    Tag tag = (Tag) items.get(0);
                    tags.add(tag);
                }
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
        return tags;
    }

    private void saveTags(List<Tag> tags, long groupID, Connection conn) throws SQLException {
        PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM GROUP_TO_TAG WHERE GROUP_ID = ?");
        deleteStmt.setLong(1, groupID);
        deleteStmt.executeUpdate();
        deleteStmt.close();
        if (tags != null) {
            Session session = Database.instance().createSession(conn);
            try {
                for (Tag tag : tags) {
                    if (tag.getTagID() != null && tag.getTagID() == 0) {
                        tag.setTagID(null);
                    }
                    session.saveOrUpdate(tag);
                }
                session.flush();
            } catch (Exception e) {
                LogClass.error(e);
                throw new RuntimeException(e);
            } finally {
                session.close();
            }
            PreparedStatement insertLinkStmt = conn.prepareStatement("INSERT INTO GROUP_TO_TAG (GROUP_ID, ANALYSIS_TAGS_ID) " +
                    "VALUES (?, ?)");
            for (Tag tag : tags) {
                insertLinkStmt.setLong(1, groupID);
                insertLinkStmt.setLong(2, tag.getTagID());
                insertLinkStmt.execute();
            }
            insertLinkStmt.close();
        }
    }

    public List<GroupDescriptor> getAllPublicGroups() {
        List<GroupDescriptor> descriptors = new ArrayList<GroupDescriptor>();
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT COMMUNITY_GROUP_ID, NAME, DESCRIPTION, COUNT(GROUP_TO_USER_JOIN_ID) FROM COMMUNITY_GROUP, GROUP_TO_USER_JOIN WHERE " +
                    "GROUP_TO_USER_JOIN.GROUP_ID = COMMUNITY_GROUP.COMMUNITY_GROUP_ID AND (COMMUNITY_GROUP.publicly_joinable = ? OR COMMUNITY_GROUP.publicly_visible = ?) " +
                    "GROUP BY COMMUNITY_GROUP_ID, NAME, DESCRIPTION");
            queryStmt.setBoolean(1, true);
            queryStmt.setBoolean(2, true);
            ResultSet rs = queryStmt.executeQuery();
            while (rs.next()) {
                descriptors.add(new GroupDescriptor(rs.getString(2), rs.getLong(1), rs.getInt(4), rs.getString(3)));
            }
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.instance().closeConnection(conn);
        }
        return descriptors;
    }

    public List<GroupUser> getUsersForGroup(long groupID) {
        List<GroupUser> users = new ArrayList<GroupUser>();
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement queryUsersStmt = conn.prepareStatement("SELECT USERNAME, NAME, EMAIL, USER.USER_ID, BINDING_TYPE FROM USER, GROUP_TO_USER_JOIN WHERE " +
                    "GROUP_TO_USER_JOIN.USER_ID = USER.USER_ID AND GROUP_TO_USER_JOIN.GROUP_ID = ?");
            queryUsersStmt.setLong(1, groupID);
            ResultSet rs = queryUsersStmt.executeQuery();
            while (rs.next()) {
                String userName = rs.getString(1);
                String name = rs.getString(2);
                String email = rs.getString(3);
                long userID = rs.getLong(4);
                int role = rs.getInt(5);
                users.add(new GroupUser(userID, userName, email, name, role));
            }
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.instance().closeConnection(conn);
        }
        return users;
    }

    public List<GroupDescriptor> getGroupsForUser(long userID) {
        List<GroupDescriptor> descriptors = new ArrayList<GroupDescriptor>();
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT COMMUNITY_GROUP.COMMUNITY_GROUP_ID, NAME, DESCRIPTION FROM COMMUNITY_GROUP, GROUP_TO_USER_JOIN WHERE " +
                    "USER_ID = ? AND GROUP_TO_USER_JOIN.GROUP_ID = COMMUNITY_GROUP.COMMUNITY_GROUP_ID");
            queryStmt.setLong(1, userID);
            ResultSet rs = queryStmt.executeQuery();
            while (rs.next()) {
                descriptors.add(new GroupDescriptor(rs.getString(2), rs.getLong(1), 0, rs.getString(3)));
            }
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.instance().closeConnection(conn);
        }
        return descriptors;
    }

    public void addUserToGroup(long userID, long groupID, int userRole) {
        Connection conn = Database.instance().getConnection();
        try {
            addUserToGroup(userID, groupID, userRole, conn);
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.instance().closeConnection(conn);
        }
    }

    public void addUserToGroup(long userID, long groupID, int userRole, Connection conn) throws SQLException {
        PreparedStatement queryStmt = conn.prepareStatement("SELECT GROUP_TO_USER_JOIN_ID FROM GROUP_TO_USER_JOIN WHERE " +
                "GROUP_ID = ? AND USER_ID = ?");
        queryStmt.setLong(1, groupID);
        queryStmt.setLong(2, userID);
        ResultSet rs = queryStmt.executeQuery();
        if (rs.next()) {
            PreparedStatement updateUserStmt = conn.prepareStatement("UPDATE GROUP_TO_USER_JOIN SET BINDING_TYPE = ? WHERE USER_ID = ? AND GROUP_ID = ?");
            updateUserStmt.setLong(1, userRole);
            updateUserStmt.setLong(2, userID);
            updateUserStmt.setLong(3, groupID);
            updateUserStmt.executeUpdate();
        } else {
            PreparedStatement addUserStmt = conn.prepareStatement("INSERT INTO GROUP_TO_USER_JOIN (GROUP_ID, USER_ID, BINDING_TYPE) " +
                    "VALUES (?, ?, ?)");
            addUserStmt.setLong(1, groupID);
            addUserStmt.setLong(2, userID);
            addUserStmt.setInt(3, userRole);
            addUserStmt.execute();
        }
    }

    public void removeUserFromGroup(long userID, long groupID, Connection conn) {
        try {
            PreparedStatement deleteUserStmt = conn.prepareStatement("DELETE FROM GROUP_TO_USER_JOIN WHERE USER_ID = ? AND GROUP_ID = ?");
            deleteUserStmt.setLong(1, userID);
            deleteUserStmt.setLong(2, groupID);
            deleteUserStmt.executeUpdate();
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public List<WSAnalysisDefinition> getInsights(long groupID) {
        List<Long> analysisIDs = new ArrayList<Long>();
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT INSIGHT_ID FROM GROUP_TO_INSIGHT WHERE GROUP_ID = ?");
            queryStmt.setLong(1, groupID);
            ResultSet rs = queryStmt.executeQuery();
            while (rs.next()) {
                long analysisID = rs.getLong(1);
                analysisIDs.add(analysisID);
            }
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.instance().closeConnection(conn);
        }
        AnalysisStorage analysisStorage = new AnalysisStorage();
        List<WSAnalysisDefinition> analysisDefs = new ArrayList<WSAnalysisDefinition>();
        for (Long analysisID : analysisIDs) {
            analysisDefs.add(analysisStorage.getAnalysisDefinition(analysisID).createBlazeDefinition());
        }
        return analysisDefs; 
    }

    public String inviteNewUserToGroup(long groupID) {
        Connection conn = Database.instance().getConnection();
        try {
            char[] activationBuf = new char[30];
            for (int i = 0; i < 30; i++) {
                char c;
                int randVal = (int) (Math.random() * 36);
                if (randVal < 26) {
                    c = (char) ('a' + randVal);
                } else {
                    c = (char) (randVal - 26);
                }
                activationBuf[i] = c;
            }
            PreparedStatement addActivationStmt = conn.prepareStatement("INSERT INTO GROUP_NEW_USER_INVITE (GROUP_ID, ACTIVATION_STRING) VALUES (?, ?)");
            addActivationStmt.setLong(1, groupID);
            String activationString = new String(activationBuf);
            addActivationStmt.setString(2, activationString);
            addActivationStmt.execute();
            return activationString; 
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.instance().closeConnection(conn);
        }
    }

    public void addFeedToGroup(long feedID, long groupID, int owner) {
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement existingLinkQuery = conn.prepareStatement("SELECT UPLOAD_POLICY_GROUPS_ID FROM UPLOAD_POLICY_GROUPS WHERE " +
                    "GROUP_ID = ? AND FEED_ID = ?");
            existingLinkQuery.setLong(1, groupID);
            existingLinkQuery.setLong(2, feedID);
            ResultSet existingRS = existingLinkQuery.executeQuery();
            if (existingRS.next()) {
                long existingID = existingRS.getLong(1);
                PreparedStatement updateLinkStmt = conn.prepareStatement("UPDATE UPLOAD_POLICY_GROUPS SET ROLE = ? WHERE " +
                        "UPLOAD_POLICY_GROUPS_ID = ?");
                updateLinkStmt.setLong(1, owner);
                updateLinkStmt.setLong(2, existingID);
                updateLinkStmt.executeUpdate();
            } else {
                PreparedStatement insertFeedStmt = conn.prepareStatement("INSERT INTO UPLOAD_POLICY_GROUPS (GROUP_ID, FEED_ID, ROLE) " +
                        "VALUES (?, ?, ?)");
                insertFeedStmt.setLong(1, groupID);
                insertFeedStmt.setLong(2, feedID);
                insertFeedStmt.setLong(3, owner);
                insertFeedStmt.execute();
            }
            addGroupAudit(new GroupAuditMessage(SecurityUtil.getUserID(), new Date(), "Added data source", groupID), conn);
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.instance().closeConnection(conn);
        }
    }

    public void addInsightToGroup(long feedID, long groupID) {
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement existingLinkQuery = conn.prepareStatement("SELECT GROUP_TO_INSIGHT_ID FROM GROUP_TO_INSIGHT WHERE " +
                    "GROUP_ID = ? AND FEED_ID = ?");
            existingLinkQuery.setLong(1, groupID);
            existingLinkQuery.setLong(2, feedID);
            ResultSet existingRS = existingLinkQuery.executeQuery();
            if (existingRS.next()) {
                long existingID = existingRS.getLong(1);
                PreparedStatement updateLinkStmt = conn.prepareStatement("UPDATE GROUP_TO_INSIGHT SET ROLE = ? WHERE " +
                        "GROUP_TO_INSIGHT_ID = ?");
                updateLinkStmt.setLong(1, Roles.SUBSCRIBER);
                updateLinkStmt.setLong(2, existingID);
                updateLinkStmt.executeUpdate();
            } else {
                PreparedStatement insertFeedStmt = conn.prepareStatement("INSERT INTO GROUP_TO_INSIGHT (GROUP_ID, FEED_ID, ROLE) " +
                        "VALUES (?, ?, ?)");
                insertFeedStmt.setLong(1, groupID);
                insertFeedStmt.setLong(2, feedID);
                insertFeedStmt.setLong(3, Roles.SUBSCRIBER);
                insertFeedStmt.execute();
            }
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.instance().closeConnection(conn);
        }
    }

    public List<FeedDescriptor> getFeeds(long groupID, long userID) {
        List<FeedDescriptor> descriptors = new ArrayList<FeedDescriptor>();
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT DATA_FEED.DATA_FEED_ID, FEED_NAME, " +
                    "DATA_FEED.FEED_TYPE, OWNER_NAME, DESCRIPTION, ATTRIBUTION, UPLOAD_POLICY_GROUPS.ROLE, UPLOAD_POLICY_USERS.ROLE, PUBLICLY_VISIBLE, MARKETPLACE_VISIBLE " +
                    "FROM UPLOAD_POLICY_GROUPS, DATA_FEED LEFT OUTER JOIN UPLOAD_POLICY_USERS ON " +
                    "DATA_FEED.DATA_FEED_ID = UPLOAD_POLICY_USERS.FEED_ID WHERE UPLOAD_POLICY_USERS.USER_ID = ? AND " +
                    "UPLOAD_POLICY_GROUPS.GROUP_ID = ? AND " +
                    "UPLOAD_POLICY_GROUPS.FEED_ID = DATA_FEED.DATA_FEED_ID");
            queryStmt.setLong(1, userID);
            queryStmt.setLong(2, groupID);
            ResultSet rs = queryStmt.executeQuery();
            while (rs.next()) {
                long feedID = rs.getLong(1);
                String name = rs.getString(2);
                int feedType = rs.getInt(3);
                String ownerName = rs.getString(4);
                String description = rs.getString(5);
                String attribution = rs.getString(6);
                int groupRole = rs.getInt(7);
                int userRole = rs.getInt(8);
                boolean publiclyVisible = rs.getBoolean(9);
                boolean marketplaceVisible = rs.getBoolean(10);
                int role = Math.min(groupRole, userRole);
                FeedDescriptor feedDescriptor = createDescriptor(feedID, name, publiclyVisible, marketplaceVisible, role, 0, feedType, ownerName, description, attribution, conn);
                descriptors.add(feedDescriptor);
            }
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.instance().closeConnection(conn);
        }
        return descriptors; 
    }

    private UploadPolicy createUploadPolicy(Connection conn, long feedID, boolean publiclyVisible, boolean marketplaceVisible) throws SQLException {
        UploadPolicy uploadPolicy = new UploadPolicy();
        uploadPolicy.setPubliclyVisible(publiclyVisible);
        uploadPolicy.setMarketplaceVisible(marketplaceVisible);
        List<FeedConsumer> owners = new ArrayList<FeedConsumer>();
        List<FeedConsumer> viewers = new ArrayList<FeedConsumer>();
        PreparedStatement policyUserStmt = conn.prepareStatement("SELECT USER.USER_ID, ROLE, USER.NAME, USER.USERNAME, USER.EMAIL FROM UPLOAD_POLICY_USERS, USER WHERE FEED_ID = ? AND " +
                "UPLOAD_POLICY_USERS.USER_ID = USER.USER_ID");
        policyUserStmt.setLong(1, feedID);
        ResultSet usersRS = policyUserStmt.executeQuery();
        while (usersRS.next()) {
            long userID = usersRS.getLong(1);
            int role = usersRS.getInt(2);
            String name = usersRS.getString(3);
            String userName = usersRS.getString(4);
            String email = usersRS.getString(5);
            UserStub userStub = new UserStub(userID, userName, email, name);
            if (role == Roles.OWNER) {
                owners.add(userStub);
            } else {
                viewers.add(userStub);
            }
        }
        PreparedStatement policyGroupsStmt = conn.prepareStatement("SELECT COMMUNITY_GROUP.NAME, COMMUNITY_GROUP.COMMUNITY_GROUP_ID, ROLE FROM UPLOAD_POLICY_GROUPS, COMMUNITY_GROUP WHERE FEED_ID = ? AND " +
                "UPLOAD_POLICY_GROUPS.GROUP_ID = COMMUNITY_GROUP.COMMUNITY_GROUP_ID");
        policyGroupsStmt.setLong(1, feedID);
        ResultSet groupsRS = policyGroupsStmt.executeQuery();
        while (groupsRS.next()) {
            String groupName = groupsRS.getString(1);
            long groupID = groupsRS.getLong(2);
            int role = groupsRS.getInt(3);
            GroupDescriptor groupDescriptor = new GroupDescriptor(groupName, groupID, 0, null);
            if (role == Roles.OWNER) {
                owners.add(groupDescriptor);
            } else {
                viewers.add(groupDescriptor);
            }
        }
        uploadPolicy.setOwners(owners);
        uploadPolicy.setViewers(viewers);
        return uploadPolicy;
    }

    private FeedDescriptor createDescriptor(long dataFeedID, String feedName, boolean publiclyVisible, boolean marketplaceVisible, Integer userRole,
                                            long size, int feedType, String ownerName, String description, String attribution, Connection conn) throws SQLException {
        UploadPolicy uploadPolicy = createUploadPolicy(conn, dataFeedID, publiclyVisible, marketplaceVisible);
        return new FeedDescriptor(feedName, dataFeedID, uploadPolicy, size, feedType, userRole != null ? userRole : 0, ownerName, description, attribution);
    }


    public List<GroupDescriptor> getGroupsForUserExcludingFeed(long userID, long feedID) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public List<GroupDescriptor> getGroupsForUserExcludingInsight(long userID, long insightID) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public List<AuditMessage> getGroupMessages(long groupID) throws SQLException {
        List<AuditMessage> groupComments = new ArrayList<AuditMessage>();
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT comment, username, group_comment.user_id, time_created, group_comment_id " +
                    "FROM group_comment, user WHERE group_id = ? and " +
                    "group_comment.user_id = user.user_id ORDER BY time_created desc");
            queryStmt.setLong(1, groupID);
            ResultSet rs = queryStmt.executeQuery();
            while (rs.next()) {
                String comment = rs.getString(1);
                String userName = rs.getString(2);
                long userID = rs.getLong(3);
                Date timeCreated = new Date(rs.getDate(4).getTime());
                long groupCommentID = rs.getLong(5);
                GroupComment groupComment = new GroupComment();
                groupComment.setAudit(false);
                groupComment.setMessage(comment);
                groupComment.setUserID(userID);
                groupComment.setUserName(userName);
                groupComment.setGroupCommentID(groupCommentID);
                groupComment.setTimestamp(timeCreated);
                groupComments.add(groupComment);
            }
            PreparedStatement queryAuditStmt = conn.prepareStatement("SELECT comment, username, group_audit_message.user_id, audit_time, group_audit_message_id " +
                    "FROM group_audit_message, user WHERE group_id = ? and " +
                    "group_audit_message.user_id = user.user_id ORDER BY audit_time desc");
            queryAuditStmt.setLong(1, groupID);
            ResultSet auditRS = queryAuditStmt.executeQuery();
            while (auditRS.next()) {
                String comment = auditRS.getString(1);
                String userName = auditRS.getString(2);
                long userID = auditRS.getLong(3);
                Date timeCreated = new Date(auditRS.getDate(4).getTime());
                GroupAuditMessage groupAuditMessage = new GroupAuditMessage();
                groupAuditMessage.setAudit(true);
                groupAuditMessage.setMessage(comment);
                groupAuditMessage.setUserID(userID);
                groupAuditMessage.setUserName(userName);
                groupAuditMessage.setTimestamp(timeCreated);
                groupComments.add(groupAuditMessage);
            }
        } finally {
            Database.instance().closeConnection(conn);
        }
        return groupComments;
    }
}
