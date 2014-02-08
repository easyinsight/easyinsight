package com.easyinsight.datafeeds.pivotaltrackerv5;

import com.easyinsight.analysis.DataSourceInfo;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.datafeeds.IServerDataSourceDefinition;
import com.easyinsight.datafeeds.composite.ChildConnection;
import com.easyinsight.datafeeds.composite.CompositeServerDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * User: jamesboe
 * Date: 2/6/14
 * Time: 4:47 PM
 */
public class PivotalTrackerV5CompositeSource extends CompositeServerDataSource {

    private String token;

    public PivotalTrackerV5CompositeSource() {
        setFeedName("Pivotal Tracker");
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public void customStorage(Connection conn) throws SQLException {
        super.customStorage(conn);
        PreparedStatement clearStmt = conn.prepareStatement("DELETE FROM pivotalv5 WHERE DATA_SOURCE_ID = ?");
        clearStmt.setLong(1, getDataFeedID());
        clearStmt.executeUpdate();
        clearStmt.close();
        PreparedStatement saveStmt = conn.prepareStatement("INSERT INTO pivotalv5 (DATA_SOURCE_ID, TOKEN) VALUES (?, ?)");
        saveStmt.setLong(1, getDataFeedID());
        saveStmt.setString(2, token);
        saveStmt.execute();
        saveStmt.close();
    }

    @Override
    public void customLoad(Connection conn) throws SQLException {
        super.customLoad(conn);
        PreparedStatement queryStmt = conn.prepareStatement("SELECT TOKEN FROM pivotalv5 WHERE data_source_id = ?");
        queryStmt.setLong(1, getDataFeedID());
        ResultSet rs = queryStmt.executeQuery();
        if (rs.next()) {
            token = rs.getString(1);
        }
        queryStmt.close();
    }

    protected List<IServerDataSourceDefinition> sortSources(List<IServerDataSourceDefinition> children) {
        List<IServerDataSourceDefinition> end = new ArrayList<IServerDataSourceDefinition>();
        Set<Integer> set = new HashSet<Integer>();
        for (IServerDataSourceDefinition s : children) {
            if (s.getFeedType().getType() == FeedType.PIVOTAL_V5_ITERATION.getType()) {
                set.add(s.getFeedType().getType());
                end.add(s);
            }
        }
        for (IServerDataSourceDefinition s : children) {
            if (s.getFeedType().getType() == FeedType.PIVOTAL_V5_STORY.getType() &&
                    s.getFeedType().getType() == FeedType.PIVOTAL_V5_EPIC.getType()) {
                set.add(s.getFeedType().getType());
                end.add(s);
            }
        }
        for (IServerDataSourceDefinition s : children) {
            if (!set.contains(s.getFeedType().getType())) {
                end.add(s);
            }
        }
        return end;
    }

    @Override
    protected void refreshDone() {
        super.refreshDone();
        storyIDToLabelMap = null;
        epicIDToLabelMap = null;
        iterationToStoryMap = null;
        userMap = null;
    }

    public String getUser(String userID) {
        if (userID == null) {
            return null;
        }
        return userMap.get(userID);
    }

    public Map<String, String> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<String, String> userMap) {
        this.userMap = userMap;
    }

    public Map<String, List<String>> getStoryIDToLabelMap() {
        return storyIDToLabelMap;
    }

    public void setStoryIDToLabelMap(Map<String, List<String>> storyIDToLabelMap) {
        this.storyIDToLabelMap = storyIDToLabelMap;
    }

    public Map<String, List<String>> getEpicIDToLabelMap() {
        return epicIDToLabelMap;
    }

    public void setEpicIDToLabelMap(Map<String, List<String>> epicIDToLabelMap) {
        this.epicIDToLabelMap = epicIDToLabelMap;
    }

    public Map<String, String> getIterationToStoryMap() {
        return iterationToStoryMap;
    }

    public void setIterationToStoryMap(Map<String, String> iterationToStoryMap) {
        this.iterationToStoryMap = iterationToStoryMap;
    }

    private Map<String, String> userMap = new HashMap<String, String>();
    private Map<String, List<String>> storyIDToLabelMap = new HashMap<String, List<String>>();
    private Map<String, List<String>> epicIDToLabelMap = new HashMap<String, List<String>>();
    private Map<String, String> iterationToStoryMap = new HashMap<String, String>();

    @Override
    public FeedType getFeedType() {
        return FeedType.PIVOTAL_V5_COMPOSITE;
    }

    @Override
    public int getDataSourceType() {
        return DataSourceInfo.COMPOSITE_PULL;
    }

    @Override
    protected Set<FeedType> getFeedTypes() {
        Set<FeedType> types = new HashSet<FeedType>();
        types.add(FeedType.PIVOTAL_V5_PROJECT);
        types.add(FeedType.PIVOTAL_V5_EPIC);
        types.add(FeedType.PIVOTAL_V5_STORY);
        types.add(FeedType.PIVOTAL_V5_STORY_TO_LABEL);
        types.add(FeedType.PIVOTAL_V5_LABEL);
        types.add(FeedType.PIVOTAL_V5_ITERATION);
        return types;
    }

    @Override
    protected Collection<ChildConnection> getChildConnections() {
        return Arrays.asList(new ChildConnection(FeedType.PIVOTAL_V5_PROJECT, FeedType.PIVOTAL_V5_EPIC, PivotalTrackerV5ProjectSource.ID, PivotalTrackerV5EpicSource.PROJECT_ID),
                new ChildConnection(FeedType.PIVOTAL_V5_PROJECT, FeedType.PIVOTAL_V5_STORY, PivotalTrackerV5ProjectSource.ID, PivotalTrackerV5StorySource.PROJECT_ID),
                new ChildConnection(FeedType.PIVOTAL_V5_PROJECT, FeedType.PIVOTAL_V5_ITERATION, PivotalTrackerV5ProjectSource.ID, PivotalTrackerV5IterationSource.PROJECT_ID),
                new ChildConnection(FeedType.PIVOTAL_V5_EPIC, FeedType.PIVOTAL_V5_LABEL, PivotalTrackerV5EpicSource.LABEL_ID, PivotalTrackerV5LabelSource.ID),
                new ChildConnection(FeedType.PIVOTAL_V5_STORY_TO_LABEL, FeedType.PIVOTAL_V5_LABEL, PivotalTrackerV5StoryToLabelSource.LABEL_ID, PivotalTrackerV5LabelSource.ID),
                new ChildConnection(FeedType.PIVOTAL_V5_STORY_TO_LABEL, FeedType.PIVOTAL_V5_STORY, PivotalTrackerV5StoryToLabelSource.STORY_ID, PivotalTrackerV5StorySource.ID),
                new ChildConnection(FeedType.PIVOTAL_V5_ITERATION, FeedType.PIVOTAL_V5_STORY, PivotalTrackerV5IterationSource.ID, PivotalTrackerV5StorySource.ITERATION_ID));
    }
}
