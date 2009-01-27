package com.easyinsight.analysis;

import com.easyinsight.calculations.generated.CalculationsParser;
import com.easyinsight.calculations.generated.CalculationsLexer;
import com.easyinsight.calculations.NodeFactory;
import com.easyinsight.security.*;
import com.easyinsight.security.SecurityException;
import com.easyinsight.logging.LogClass;
import com.easyinsight.database.Database;

import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.hibernate.Session;

/**
 * User: James Boe
 * Date: Jan 10, 2008
 * Time: 7:32:24 PM
 */
public class AnalysisService implements IAnalysisService {

    private AnalysisStorage analysisStorage = new AnalysisStorage();

    public Collection<WSAnalysisDefinition> getAnalysisDefinitions() {
        long userID = SecurityUtil.getUserID();
        try {
            Collection<WSAnalysisDefinition> analysisDefinitions = new ArrayList<WSAnalysisDefinition>();
            for (WSAnalysisDefinition analysisDefinition : analysisStorage.getAllDefinitions(userID)) {
                if (analysisDefinition.isRootDefinition()) {
                    continue;
                }
                analysisDefinitions.add(analysisDefinition);
            }
            return analysisDefinitions;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public List<WidgetAnalysisStub> getWidgetAnalyses() {
        Collection<WSAnalysisDefinition> defs = getAnalysisDefinitions();
        List<WidgetAnalysisStub> stubs = new ArrayList<WidgetAnalysisStub>();
        for (WSAnalysisDefinition def : defs) {
            WidgetAnalysisStub stub = new WidgetAnalysisStub();
            stub.setAnalysisID(def.getAnalysisID());
            stub.setName(def.getName());
            stubs.add(stub);
        }
        return stubs;
        //return new ArrayList<WidgetAnalysisStub>();
    }

    public String validateCalculation(String calculationString) {
        String validationString = null;
        try {
            CalculationsLexer lexer = new CalculationsLexer(new ANTLRStringStream(calculationString));
            CommonTokenStream tokes = new CommonTokenStream();
            tokes.setTokenSource(lexer);
            CalculationsParser parser = new CalculationsParser(tokes);
            parser.setTreeAdaptor(new NodeFactory());
            parser.expr();
        } catch (Exception e) {
            validationString = e.getMessage();
        }
        return validationString;
    }

    public void addAnalysisView(long analysisID) {
        analysisStorage.addAnalysisView(analysisID);
    }

    public void rateAnalysis(long accountID, long analysisID, int rating) {
        analysisStorage.rateAnalysis(analysisID, accountID, rating);
    }

    public long saveAnalysisDefinition(WSAnalysisDefinition wsAnalysisDefinition) {
        long userID = SecurityUtil.getUserID();
        if (wsAnalysisDefinition.getAnalysisID() > 0) {
            SecurityUtil.authorizeInsight(wsAnalysisDefinition.getAnalysisID());
        }
        Connection conn = Database.instance().getConnection();
        Session session = Database.instance().createSession(conn);
        try {
            conn.setAutoCommit(false);
            for (Tag tag : wsAnalysisDefinition.getTagCloud()) {
                if (tag.getTagID() != null && tag.getTagID() == 0) {
                    tag.setTagID(null);
                }
            }

            PreparedStatement getBindingsStmt = conn.prepareStatement("SELECT USER_ID, RELATIONSHIP_TYPE FROM USER_TO_ANALYSIS WHERE ANALYSIS_ID = ?");
            getBindingsStmt.setLong(1, wsAnalysisDefinition.getAnalysisID());
            ResultSet rs = getBindingsStmt.executeQuery();
            List<UserToAnalysisBinding> bindings = new ArrayList<UserToAnalysisBinding>();
            while (rs.next()) {
                long bindingUserID = rs.getLong(1);
                int relationshipType = rs.getInt(2);
                bindings.add(new UserToAnalysisBinding(bindingUserID, relationshipType));
            }
            if (bindings.isEmpty()) {
                bindings.add(new UserToAnalysisBinding(userID, UserPermission.OWNER));
            }
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM USER_TO_ANALYSIS WHERE ANALYSIS_ID = ?");
            stmt.setLong(1, wsAnalysisDefinition.getAnalysisID());
            stmt.executeUpdate();

            AnalysisDefinition analysisDefinition = AnalysisDefinitionFactory.fromWSDefinition(wsAnalysisDefinition);
            analysisDefinition.setUserBindings(bindings);
            analysisStorage.saveAnalysis(analysisDefinition, session);
            conn.commit();
            return analysisDefinition.getAnalysisID();
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
            session.close();
            Database.instance().closeConnection(conn);
        }
    }    

    public void deleteAnalysisDefinition(WSAnalysisDefinition analysisDefinition) {
        long userID = SecurityUtil.getUserID();
        SecurityUtil.authorizeInsight(analysisDefinition.getAnalysisID());
        AnalysisDefinition dbAnalysisDef = analysisStorage.getAnalysisDefinition(analysisDefinition.getAnalysisID());

        boolean canDelete = analysisStorage.canUserDelete(userID, dbAnalysisDef);
        if (canDelete) {
            analysisStorage.deleteAnalysisDefinition(dbAnalysisDef);
        } else {
            Iterator<UserToAnalysisBinding> bindingIter = dbAnalysisDef.getUserBindings().iterator();
            while (bindingIter.hasNext()) {
                UserToAnalysisBinding binding = bindingIter.next();
                if (binding.getUserID() == userID) {
                    bindingIter.remove();
                }
            }
            analysisStorage.saveAnalysis(dbAnalysisDef);
        }
    }

    public void subscribeToAnalysis(long analysisID) {
        try {
            long userID = SecurityUtil.getUserID();
            AnalysisDefinition analysisDefinition = analysisStorage.getAnalysisDefinition(analysisID);
            UserToAnalysisBinding binding = new UserToAnalysisBinding(userID, Roles.SUBSCRIBER);
            analysisDefinition.getUserBindings().add(binding);
            analysisStorage.saveAnalysis(analysisDefinition);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public WSAnalysisDefinition openAnalysisDefinition(long analysisID) {
        try {
            SecurityUtil.authorizeInsight(analysisID);
            addAnalysisView(analysisID);
            AnalysisDefinition analysisDefinition = analysisStorage.getAnalysisDefinition(analysisID);
            return analysisDefinition.createBlazeDefinition();
        } catch (Exception e) {
            LogClass.error(e);
            return null;
        }
    }

    public InsightResponse openAnalysisIfPossible(long analysisID) {
        InsightResponse insightResponse;
        try {
            try {
                SecurityUtil.authorizeInsight(analysisID);
                addAnalysisView(analysisID);
                AnalysisDefinition analysisDefinition = analysisStorage.getAnalysisDefinition(analysisID);
                insightResponse = new InsightResponse(true, analysisDefinition.createBlazeDefinition());
            } catch (SecurityException e) {
                insightResponse = new InsightResponse(false, null);
            }
        } catch (Exception e) {
            LogClass.error(e);
            return null;
        }
        return insightResponse;
    }

    public Collection<WSAnalysisDefinition> getAllDefinitions() {
        /*long userID = SecurityUtil.getUserID();
        try {
            Collection<WSAnalysisDefinition> analysisDefinitions = new ArrayList<WSAnalysisDefinition>();
            for (AnalysisDefinition analysisDefinition : analysisStorage.getAllDefinitions(userID)) {
                analysisDefinitions.add(analysisDefinition.createWSDefinition());
            }
            return analysisDefinitions;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } */
        throw new UnsupportedOperationException();
    }

    public UserCapabilities getUserCapabilitiesForFeed(long feedID) {
        long userID = SecurityUtil.getUserID();
        int feedRole = Integer.MAX_VALUE;
        boolean groupMember = false;
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement existingLinkQuery = conn.prepareStatement("SELECT ROLE FROM UPLOAD_POLICY_USERS WHERE " +
                    "USER_ID = ? AND FEED_ID = ?");
            existingLinkQuery.setLong(1, userID);
            existingLinkQuery.setLong(2, feedID);
            ResultSet rs = existingLinkQuery.executeQuery();
            if (rs.next()) {
                feedRole = rs.getInt(1);
            }
            PreparedStatement queryStmt = conn.prepareStatement("SELECT COUNT(COMMUNITY_GROUP.COMMUNITY_GROUP_ID) FROM COMMUNITY_GROUP, GROUP_TO_USER_JOIN WHERE " +
                    "USER_ID = ? AND GROUP_TO_USER_JOIN.GROUP_ID = COMMUNITY_GROUP.COMMUNITY_GROUP_ID");
            queryStmt.setLong(1, userID);
            ResultSet groupRS = queryStmt.executeQuery();
            groupRS.next();
            groupMember = groupRS.getInt(1) > 0;
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.instance().closeConnection(conn);
        }
        return new UserCapabilities(Integer.MAX_VALUE, feedRole, groupMember);
    }

    public UserCapabilities getUserCapabilitiesForInsight(long feedID, long insightID) {
        UserCapabilities userCapabilities = getUserCapabilitiesForFeed(feedID);
        long userID = SecurityUtil.getUserID();
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement existingLinkQuery = conn.prepareStatement("SELECT RELATIONSHIP_TYPE FROM USER_TO_ANALYSIS WHERE " +
                    "USER_ID = ? AND ANALYSIS_ID = ?");
            existingLinkQuery.setLong(1, userID);
            existingLinkQuery.setLong(2, insightID);
            ResultSet rs = existingLinkQuery.executeQuery();
            if (rs.next()) {
                userCapabilities.setAnalysisRole(rs.getInt(1));
            }
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.instance().closeConnection(conn);
        }
        return userCapabilities;
    }
}
