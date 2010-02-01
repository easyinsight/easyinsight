package com.easyinsight.goals;

import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.logging.LogClass;

import java.util.HashSet;
import java.util.Set;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * User: James Boe
 * Date: Jul 4, 2009
 * Time: 2:06:22 PM
 */
public class GoalUtil {
    private static class GoalDataSourceVisitor extends GoalTreeVisitor {
        private Set<Long> dataSourceIDs = new HashSet<Long>();

        private boolean includeSubTrees = false;
        private Connection conn;

        private GoalDataSourceVisitor(boolean includeSubTrees, Connection conn) {
            this.includeSubTrees = includeSubTrees;
            this.conn = conn;
        }

        protected void accept(GoalTreeNode goalTreeNode) {
            if (goalTreeNode.getKpi() != null) {
                dataSourceIDs.add(goalTreeNode.getKpi().getCoreFeedID());
            }            
            if (includeSubTrees && goalTreeNode.getSubTreeID() > 0) {
                GoalStorage goalStorage = new GoalStorage();
                GoalTree subTree = goalStorage.retrieveGoalTree(goalTreeNode.getSubTreeID());
                GoalDataSourceVisitor subTreeVisitor = new GoalDataSourceVisitor(includeSubTrees, conn);
                subTreeVisitor.visit(subTree.getRootNode());
                dataSourceIDs.addAll(subTreeVisitor.dataSourceIDs);
            }
        }

        public Set<Long> getDataSourceIDs() {
            return dataSourceIDs;
        }
    }

    public static Set<Long> getDataSourceIDs(long goalTreeID, boolean includeSubTrees) {
        EIConnection conn = Database.instance().getConnection();
        try {
            return getDataSourceIDs(goalTreeID, includeSubTrees, conn);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public static Set<Long> getDataSourceIDs(long goalTreeID, boolean includeSubTrees, EIConnection conn) throws Exception {
        GoalDataSourceVisitor visitor = new GoalDataSourceVisitor(includeSubTrees, conn);
        GoalTree goalTree = new GoalStorage().retrieveGoalTree(goalTreeID, conn);
        visitor.visit(goalTree.getRootNode());
        return visitor.getDataSourceIDs();
    }
}
