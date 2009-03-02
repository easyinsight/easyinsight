package com.easyinsight.goals;

import com.easyinsight.analysis.Tag;
import com.easyinsight.solutions.Solution;
import com.easyinsight.solutions.SolutionService;
import com.easyinsight.solutions.SolutionGoalTreeDescriptor;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.security.Roles;
import com.easyinsight.logging.LogClass;
import com.easyinsight.email.UserStub;
import com.easyinsight.users.Account;

import java.util.*;

/**
 * User: James Boe
 * Date: Oct 23, 2008
 * Time: 3:06:56 PM
 */
public class GoalService {

    private GoalStorage goalStorage = new GoalStorage();
    private GoalEvaluationStorage goalEvaluationStorage = new GoalEvaluationStorage();

    public GoalTree createGoalTree(GoalTree goalTree) {
        SecurityUtil.authorizeAccountTier(Account.PROFESSIONAL);
        long userID = SecurityUtil.getUserID();
        try {
            UserStub userStub = new UserStub();
            userStub.setUserID(userID);
            goalStorage.addGoalTree(goalTree);
            return goalTree;
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

    public GoalTree updateGoalTree(GoalTree goalTree) {
        SecurityUtil.authorizeGoalTree(goalTree.getGoalTreeID(), Roles.OWNER);
        try {
            goalStorage.updateGoalTree(goalTree);
            return goalTree;
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
                if (descriptor.getGoalTreeID() == goalTreeID) {
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
            GoalTreeNodeData data = createDataTreeForDates(goalTree, startDate, endDate);
            goalTree.setRootNode(data);
            return goalTree;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public List<GoalValue> getGoalValues(final long goalTreeNodeID, final Date startDate, final Date endDate) {
        // TODO: secure this
        try {
            return goalEvaluationStorage.getGoalValues(goalTreeNodeID, startDate, endDate);
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
                    data.populateCurrentValue();                    
                    data.determineOutcome(startDate, endDate, goalEvaluationStorage);
                }
            };
        visitor.visit(dataNode);
        dataNode.summarizeOutcomes();
        return dataNode;
    }

    public GoalTree getGoalTree(long goalTreeID) {
        SecurityUtil.authorizeGoalTree(goalTreeID, Roles.SUBSCRIBER);
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
        // TODO: secure this
        long userID = SecurityUtil.getUserID();
        try {
            goalStorage.addUserToGoal(userID, goalTreeNodeID);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

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
}
