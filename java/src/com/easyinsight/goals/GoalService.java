package com.easyinsight.goals;

import com.easyinsight.analysis.Tag;
import com.easyinsight.analysis.AnalysisMeasure;
import com.easyinsight.analysis.FilterDefinition;
import com.easyinsight.solutions.Solution;
import com.easyinsight.solutions.SolutionService;
import com.easyinsight.solutions.SolutionGoalTreeDescriptor;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.security.Roles;
import com.easyinsight.logging.LogClass;
import com.easyinsight.email.UserStub;
import com.easyinsight.users.Account;
import com.easyinsight.users.Credentials;
import com.easyinsight.database.EIConnection;
import com.easyinsight.database.Database;
import com.easyinsight.datafeeds.*;
import com.easyinsight.pipeline.HistoryRun;

import java.util.*;

/**
 * User: James Boe
 * Date: Oct 23, 2008
 * Time: 3:06:56 PM
 */
public class GoalService {

    private GoalStorage goalStorage = new GoalStorage();
    private GoalEvaluationStorage goalEvaluationStorage = new GoalEvaluationStorage();

    public boolean canAccessGoalTree(long goalTreeID) {
        try {
            SecurityUtil.authorizeGoalTree(goalTreeID, Roles.SUBSCRIBER);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    public GoalSaveInfo createGoalTree(GoalTree goalTree) {
        SecurityUtil.authorizeAccountTier(Account.PROFESSIONAL);
        if (goalTree.getAdministrators() == null || goalTree.getAdministrators().size() == 0) {
            throw new RuntimeException("At least one administrator must be defined.");
        }
        long userID = SecurityUtil.getUserID();
        try {
            UserStub userStub = new UserStub();
            userStub.setUserID(userID);
            return goalStorage.addGoalTree(goalTree);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public void deleteMilestone(long milestoneID) {
        SecurityUtil.authorizeMilestone(milestoneID);
        try {
            goalStorage.deleteMilestone(milestoneID);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public long saveMilestone(GoalTreeMilestone goalTreeMilestone) {
        long accountID = SecurityUtil.getAccountID();
        try {
            return goalStorage.addMilestone(goalTreeMilestone, accountID);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public List<GoalTreeMilestone> getMilestones() {
        long accountID = SecurityUtil.getAccountID();
        try {
            return goalStorage.getMilestones(accountID);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public void updateMilestone(GoalTreeMilestone goalTreeMilestone) {
        SecurityUtil.authorizeMilestone(goalTreeMilestone.getMilestoneID());
        try {
            goalStorage.updateMilestone(goalTreeMilestone);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public void deleteGoalTree(long goalTreeID) {
        SecurityUtil.authorizeGoalTree(goalTreeID, Roles.OWNER);
        try {
            goalStorage.deleteGoalTree(goalTreeID);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public List<GoalValue> generateHistory(AnalysisMeasure analysisMeasure, List<FilterDefinition> filters, long dataSourceID, Date startDate, Date endDate,
                                           List<CredentialFulfillment> credentials) {
        try {
            return new HistoryRun().calculateHistoricalValues(dataSourceID, analysisMeasure, filters, startDate, endDate, credentials);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public GoalSaveInfo updateGoalTree(GoalTree goalTree) {
        SecurityUtil.authorizeGoalTree(goalTree.getGoalTreeID(), Roles.OWNER);
        try {
            return goalStorage.updateGoalTree(goalTree);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public AvailableGoalTreeList getGoalTreesForInstall(long goalTreeID) {
        AvailableGoalTreeList availableGoalTreeList = new AvailableGoalTreeList();
        List<GoalTreeDescriptor> myTrees = new ArrayList<GoalTreeDescriptor>();
        List<SolutionGoalTreeDescriptor> solutionTrees;
        try {
            List<GoalTreeDescriptor> goalTrees = getGoalTrees();
            Iterator<GoalTreeDescriptor> iter = goalTrees.iterator();
            while (iter.hasNext()) {
                GoalTreeDescriptor descriptor = iter.next();
                if (descriptor.getId() == goalTreeID) {
                    iter.remove();
                }
            }
            myTrees.addAll(goalTrees);
            solutionTrees = new SolutionService().getTreesFromSolutions();
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
        availableGoalTreeList.setMyTrees(myTrees);
        availableGoalTreeList.setSolutionTrees(solutionTrees);
        return availableGoalTreeList;
    }

    public AvailableSolutionList getSolutionsByTags(List<Tag> tags) {
        try {
            List<Solution> tagSolutions;
            if (tags == null) {
                tagSolutions = new ArrayList<Solution>();
            } else {
                tagSolutions = new SolutionService().getSolutionsWithTags(tags);
            }
            List<Solution> allSolutions = new SolutionService().getSolutions();
            return new AvailableSolutionList(tagSolutions, allSolutions);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public List<CredentialRequirement> getCredentialsForGoalTree(long goalTreeID, boolean allSources, final boolean includeSubTrees, List<CredentialFulfillment> existingCredentials) {
        final EIConnection conn = Database.instance().getConnection();
        Map<Long, CredentialRequirement> credentialMap = new HashMap<Long, CredentialRequirement>();
        final Set<Long> dataSourceIDs;
        try {
            dataSourceIDs = GoalUtil.getDataSourceIDs(goalTreeID, includeSubTrees, conn);
            if (allSources) {
                for (Long dataSourceID : dataSourceIDs) {
                    FeedDefinition feedDefinition = new FeedStorage().getFeedDefinitionData(dataSourceID, conn);
                    if (feedDefinition.getCredentialsDefinition() == CredentialsDefinition.STANDARD_USERNAME_PW) {
                        IServerDataSourceDefinition dataSource = (IServerDataSourceDefinition) feedDefinition;
                        Credentials credentials = null;
                        for (CredentialFulfillment fulfillment : existingCredentials) {
                            if (fulfillment.getDataSourceID() == feedDefinition.getDataFeedID()) {
                                credentials = fulfillment.getCredentials();
                            }
                        }
                        boolean noCredentials = true;
                        if (credentials != null) {
                            noCredentials = dataSource.validateCredentials(credentials) != null;
                        }
                        if (noCredentials) {
                            credentialMap.put(feedDefinition.getDataFeedID(), new CredentialRequirement(feedDefinition.getDataFeedID(), feedDefinition.getFeedName(),
                                CredentialsDefinition.STANDARD_USERNAME_PW));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
        if (!allSources) {
            for (Long dataSourceID : dataSourceIDs) {
                Feed feed = FeedRegistry.instance().getFeed(dataSourceID);
                Credentials credentials = null;
                for (CredentialFulfillment fulfillment : existingCredentials) {
                    if (fulfillment.getDataSourceID() == dataSourceID) {
                        credentials = fulfillment.getCredentials();
                    }
                }
                boolean noCredentials = true;
                if (credentials != null) {
                    noCredentials = new FeedStorage().getFeedDefinitionData(dataSourceID).validateCredentials(credentials) != null;
                }
                if (noCredentials) {
                    List<CredentialRequirement> credentialRequirements = feed.getCredentialRequirement();
                    for (CredentialRequirement credentialRequirement : credentialRequirements) {
                        credentialMap.put(credentialRequirement.getDataSourceID(), credentialRequirement);
                    }
                }
            }
        }

        return new ArrayList<CredentialRequirement>(credentialMap.values());
    }

    public GoalTree forceRefresh(long goalTreeID, Date startDate, Date endDate, List<CredentialFulfillment> credentialsList, boolean allSources, boolean includeSubTrees) {
        SecurityUtil.authorizeGoalTree(goalTreeID, Roles.SUBSCRIBER);
        try {
            if (allSources) {
                Set<Long> dataSourceIDs = GoalUtil.getDataSourceIDs(goalTreeID, includeSubTrees);
                for (Long dataSourceID : dataSourceIDs) {
                    FeedDefinition feedDefinition = new FeedStorage().getFeedDefinitionData(dataSourceID);
                    if (feedDefinition.getCredentialsDefinition() == CredentialsDefinition.STANDARD_USERNAME_PW) {
                        IServerDataSourceDefinition dataSource = (IServerDataSourceDefinition) feedDefinition;
                        Credentials credentials = null;
                        for (CredentialFulfillment fulfillment : credentialsList) {
                            if (fulfillment.getDataSourceID() == feedDefinition.getDataFeedID()) {
                                credentials = fulfillment.getCredentials();
                            }
                        }
                        dataSource.refreshData(credentials, SecurityUtil.getAccountID(), new Date(), null);
                    }
                }
            }
            EIConnection conn = Database.instance().getConnection();
            try {
                conn.setAutoCommit(false);
                GoalTree goalTree = goalStorage.retrieveGoalTree(goalTreeID, conn);
                goalEvaluationStorage.forceEvaluate(goalTree, conn, credentialsList);
                conn.commit();
            } catch (Exception e) {
                LogClass.error(e);
                conn.rollback();
                throw new RuntimeException(e);
            } finally {
                conn.setAutoCommit(true);
                Database.closeConnection(conn);
            }
            return createDataTree(goalTreeID, startDate, endDate);
        } catch (RuntimeException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public GoalTree createDataTree(long goalTreeID, Date startDate, Date endDate) {
        SecurityUtil.authorizeGoalTree(goalTreeID, Roles.SUBSCRIBER);
        try {
            if (endDate == null) {
                Calendar cal = Calendar.getInstance();
                endDate = cal.getTime();
                cal.add(Calendar.DAY_OF_YEAR, -7);
                startDate = cal.getTime();
            }
            GoalTree goalTree = getGoalTree(goalTreeID);
            goalStorage.decorateDataTree(goalTree);
            GoalTreeNodeData data = createDataTreeForDates(goalTree, startDate, endDate);
            goalTree.setRootNode(data);
            return goalTree;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public List<GoalValue> getGoalValues(final long goalTreeNodeID, final Date startDate, final Date endDate) {
        SecurityUtil.authorizeGoal(goalTreeNodeID, Roles.SUBSCRIBER);
        try {
            return goalEvaluationStorage.getGoalValuesFromDatabase(goalTreeNodeID, startDate, endDate);
        } catch (Exception e) {                                             
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    private GoalTreeNodeData createDataTreeForDates(GoalTree goalTree, final Date startDate, final Date endDate) {
        GoalTreeNodeData dataNode = new GoalTreeNodeDataBuilder().build(goalTree.getRootNode());
        GoalTreeVisitor visitor = new GoalTreeVisitor() {

                protected void accept(GoalTreeNode goalTreeNode) {
                    GoalTreeNodeData data = (GoalTreeNodeData) goalTreeNode;
                    try {
                        if (data.getSubTreeID() > 0) {
                            GoalStorage goalStorage = new GoalStorage();
                            GoalTree subTree = goalStorage.retrieveGoalTree(data.getSubTreeID());
                            GoalTreeNodeData subData = createDataTreeForDates(subTree, startDate, endDate);
                            if (data.getAnalysisMeasure() == null) {
                                data.setCurrentValue(subData.getCurrentValue());
                                data.setGoalOutcome(subData.getGoalOutcome());
                            } else {
                                data.populateCurrentValue();
                                data.determineOutcome(startDate, endDate, goalEvaluationStorage);
                            }
                        } else {
                            data.populateCurrentValue();
                            data.determineOutcome(startDate, endDate, goalEvaluationStorage);
                        }
                    } catch (Exception e) {
                        LogClass.error(e);
                    }
                }
            };
        visitor.visit(dataNode);
        dataNode.summarizeOutcomes();
        return dataNode;
    }

    public GoalTree getGoalTree(long goalTreeID) {
        SecurityUtil.authorizeGoalTreeSolutionInstall(goalTreeID);
        try {
            return goalStorage.retrieveGoalTree(goalTreeID);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public List<GoalTreeDescriptor> getGoalTrees() {
        try {
            return goalStorage.getTreesForUser(SecurityUtil.getUserID());
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public void subscribeToGoal(long goalTreeNodeID) {
        SecurityUtil.authorizeGoal(goalTreeNodeID, Roles.SUBSCRIBER);
        long userID = SecurityUtil.getUserID();
        try {
            goalStorage.addUserToGoal(userID, goalTreeNodeID);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    // what do we want to see here...
    // we want to see the latest value
    // we want to understand some context around that number
    // context is relevant to the goal that we're trying to meet
    // sparkline seems useful as one thing there
    

    public List<GoalTreeNodeData> getGoals() {
        long userID = SecurityUtil.getUserID();
        try {
            Calendar cal = Calendar.getInstance();
            Date endDate = cal.getTime();
            cal.add(Calendar.DAY_OF_YEAR, -7);
            Date startDate = cal.getTime();
            return goalStorage.getGoalsForUser(userID, startDate, endDate);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public List<GoalDescriptor> getGoalsForTree(long treeID) {
        final List<GoalDescriptor> nodes = new ArrayList<GoalDescriptor>();
        SecurityUtil.authorizeGoalTree(treeID, Roles.SUBSCRIBER);
        try {
            GoalTree goalTree = goalStorage.retrieveGoalTree(treeID);
            GoalTreeVisitor visitor = new GoalTreeVisitor() {

                protected void accept(GoalTreeNode goalTreeNode) {
                    nodes.add(new GoalDescriptor(goalTreeNode.getName(), goalTreeNode.getGoalTreeNodeID()));
                }
            };
            visitor.visit(goalTree.getRootNode());
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
        return nodes;
    }

    public List<GoalValue> calculateSlope(long goalID, Date startDate, Date endDate) {
        try {
            return goalEvaluationStorage.calculateSlope(goalID, startDate, endDate);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }
}
