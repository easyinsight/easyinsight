package com.easyinsight.datafeeds;

import com.easyinsight.analysis.*;
import com.easyinsight.core.DerivedKey;
import com.easyinsight.core.Key;
import com.easyinsight.core.Value;
import com.easyinsight.database.EIConnection;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.users.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.Serializable;
import java.util.*;

/**
 * User: James Boe
 * Date: Jan 28, 2008
 * Time: 6:47:05 PM
 */
public class CompositeFeedConnection implements Serializable, IJoin {
    private Long sourceFeedID;
    private Long targetFeedID;
    private Key sourceJoin;
    private Key targetJoin;
    private AnalysisItem sourceItem;
    private AnalysisItem targetItem;
    private List<Key> sourceJoins = new ArrayList<Key>();
    private List<Key> targetJoins = new ArrayList<Key>();
    private List<AnalysisItem> sourceItems = new ArrayList<AnalysisItem>();
    private List<AnalysisItem> targetItems = new ArrayList<AnalysisItem>();
    private boolean sourceOuterJoin;
    private boolean targetOuterJoin;
    private boolean sourceJoinOnOriginal;
    private boolean targetJoinOnOriginal;
    private String sourceFeedName;
    private String targetFeedName;
    private String label = "";
    private boolean optimized;
    private String marmotScript;

    public CompositeFeedConnection() {
    }

    public CompositeFeedConnection(Long sourceFeedID, Long targetFeedID) {
        this.sourceFeedID = sourceFeedID;
        this.targetFeedID = targetFeedID;
    }

    public CompositeFeedConnection(Long sourceFeedID, Long targetFeedID, Key sourceJoin, Key targetJoin) {
        this.sourceFeedID = sourceFeedID;
        this.targetFeedID = targetFeedID;
        this.sourceJoin = sourceJoin;
        sourceJoins = Arrays.asList(sourceJoin);
        this.targetJoin = targetJoin;
        targetJoins = Arrays.asList(targetJoin);
    }

    public CompositeFeedConnection(Long sourceFeedID, Long targetFeedID, Key sourceJoin, Key targetJoin, String sourceName, String targetName,
                                   boolean sourceOuterJoin, boolean targetOuterJoin, boolean sourceJoinOnOriginal, boolean targetJoinOnOriginal) {
        this.sourceFeedID = sourceFeedID;
        this.targetFeedID = targetFeedID;
        this.sourceJoin = sourceJoin;
        sourceJoins = Arrays.asList(sourceJoin);
        this.targetJoin = targetJoin;
        targetJoins = Arrays.asList(targetJoin);
        this.sourceFeedName = sourceName;
        this.targetFeedName = targetName;
        this.sourceOuterJoin = sourceOuterJoin;
        this.targetOuterJoin = targetOuterJoin;
        this.sourceJoinOnOriginal = sourceJoinOnOriginal;
        this.targetJoinOnOriginal = targetJoinOnOriginal;
    }

    public CompositeFeedConnection(Long sourceFeedID, Long targetFeedID, Key sourceJoin, Key targetJoin, String sourceName, String targetName,
                                   boolean sourceOuterJoin, boolean targetOuterJoin, boolean sourceJoinOnOriginal, boolean targetJoinOnOriginal, String marmotScript) {
        this(sourceFeedID, targetFeedID, sourceJoin, targetJoin, sourceName, targetName, sourceOuterJoin, targetOuterJoin, sourceJoinOnOriginal, targetJoinOnOriginal);
        this.marmotScript = marmotScript;
    }

    public CompositeFeedConnection(Long sourceFeedID, Long targetFeedID, AnalysisItem sourceItem, AnalysisItem targetItem, String sourceName, String targetName,
                                   boolean sourceOuterJoin, boolean targetOuterJoin, boolean sourceJoinOnOriginal, boolean targetJoinOnOriginal, String marmotScript) {
        this(sourceFeedID, targetFeedID, sourceItem, targetItem, sourceName, targetName, sourceOuterJoin, targetOuterJoin, sourceJoinOnOriginal, targetJoinOnOriginal);
        this.marmotScript = marmotScript;
    }

    public CompositeFeedConnection(Long sourceFeedID, Long targetFeedID, AnalysisItem sourceItem, AnalysisItem targetItem, String sourceName, String targetName,
                                   boolean sourceOuterJoin, boolean targetOuterJoin, boolean sourceJoinOnOriginal, boolean targetJoinOnOriginal) {
        this.sourceFeedID = sourceFeedID;
        this.targetFeedID = targetFeedID;
        this.sourceItem = sourceItem;
        sourceItems = Arrays.asList(sourceItem);
        this.targetItem = targetItem;
        targetItems = Arrays.asList(targetItem);
        this.sourceFeedName = sourceName;
        this.targetFeedName = targetName;
        this.sourceOuterJoin = sourceOuterJoin;
        this.targetOuterJoin = targetOuterJoin;
        this.sourceJoinOnOriginal = sourceJoinOnOriginal;
        this.targetJoinOnOriginal = targetJoinOnOriginal;
    }

    public boolean isOptimized() {
        return optimized;
    }

    public void setOptimized(boolean optimized) {
        this.optimized = optimized;
    }

    public boolean isSourceJoinOnOriginal() {
        return sourceJoinOnOriginal;
    }

    public void setSourceJoinOnOriginal(boolean sourceJoinOnOriginal) {
        this.sourceJoinOnOriginal = sourceJoinOnOriginal;
    }

    public boolean isTargetJoinOnOriginal() {
        return targetJoinOnOriginal;
    }
    
    public void setBlockLabel(String label) {
        this.label = label;
    }

    public String getBlockLabel() {
        return label;
    }

    public boolean isPostJoin() {
        return false;
    }

    public String getMarmotScript() {
        return marmotScript;
    }

    public void setMarmotScript(String marmotScript) {
        this.marmotScript = marmotScript;
    }

    public void reconcile(List<CompositeFeedNode> compositeFeedNodes, List<AnalysisItem> fields) {

    }

    public void setTargetJoinOnOriginal(boolean targetJoinOnOriginal) {
        this.targetJoinOnOriginal = targetJoinOnOriginal;
    }

    public List<AnalysisItem> getSourceItems() {
        return sourceItems;
    }

    public void setSourceItems(List<AnalysisItem> sourceItems) {
        this.sourceItems = sourceItems;
    }

    public List<AnalysisItem> getTargetItems() {
        return targetItems;
    }

    public void setTargetItems(List<AnalysisItem> targetItems) {
        this.targetItems = targetItems;
    }

    public boolean isSourceOuterJoin() {
        return sourceOuterJoin;
    }

    public void setSourceOuterJoin(boolean sourceOuterJoin) {
        this.sourceOuterJoin = sourceOuterJoin;
    }

    public boolean isTargetOuterJoin() {
        return targetOuterJoin;
    }

    public void setTargetOuterJoin(boolean targetOuterJoin) {
        this.targetOuterJoin = targetOuterJoin;
    }

    public String getSourceFeedName() {
        return sourceFeedName;
    }

    public void setSourceFeedName(String sourceFeedName) {
        this.sourceFeedName = sourceFeedName;
    }

    public String getTargetFeedName() {
        return targetFeedName;
    }

    public void setTargetFeedName(String targetFeedName) {
        this.targetFeedName = targetFeedName;
    }

    public AnalysisItem getSourceItem() {
        return sourceItem;
    }

    public void setSourceItem(AnalysisItem sourceItem) {
        this.sourceItem = sourceItem;
    }

    public AnalysisItem getTargetItem() {
        return targetItem;
    }

    public void setTargetItem(AnalysisItem targetItem) {
        this.targetItem = targetItem;
    }

    public void setSourceJoins(List<Key> sourceJoins) {
        this.sourceJoins = sourceJoins;
    }

    public void setTargetJoins(List<Key> targetJoins) {
        this.targetJoins = targetJoins;
    }

    public List<Key> getSourceJoins() {
        return sourceJoins;
    }

    public List<Key> getTargetJoins() {
        return targetJoins;
    }

    public Long getSourceFeedID() {
        return sourceFeedID;
    }

    public void setSourceFeedID(Long sourceFeedID) {
        this.sourceFeedID = sourceFeedID;
    }

    public Long getTargetFeedID() {
        return targetFeedID;
    }

    public void setTargetFeedID(Long targetFeedID) {
        this.targetFeedID = targetFeedID;
    }

    public Key getSourceJoin() {
        return sourceJoin;
    }

    public void setSourceJoin(Key sourceJoin) {
        this.sourceJoin = sourceJoin;
        sourceJoins = Arrays.asList(sourceJoin);
    }

    public Key getTargetJoin() {
        return targetJoin;
    }

    public void setTargetJoin(Key targetJoin) {
        this.targetJoin = targetJoin;
        targetJoins = Arrays.asList(targetJoin);
    }

    public void store(Connection conn, long feedID) throws SQLException {
        if (sourceItem != null && targetItem != null) {
            PreparedStatement connInsertStmt = conn.prepareStatement("INSERT INTO COMPOSITE_CONNECTION (" +
                    "SOURCE_FEED_NODE_ID, TARGET_FEED_NODE_ID, source_item_id, target_item_id, COMPOSITE_FEED_ID, " +
                    "left_join, right_join, left_join_on_original, right_join_on_original, marmot_script) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            connInsertStmt.setLong(1, sourceFeedID);
            connInsertStmt.setLong(2, targetFeedID);
            connInsertStmt.setLong(3, sourceItem.getAnalysisItemID());
            connInsertStmt.setLong(4, targetItem.getAnalysisItemID());
            connInsertStmt.setLong(5, feedID);
            connInsertStmt.setBoolean(6, sourceOuterJoin);
            connInsertStmt.setBoolean(7, targetOuterJoin);
            connInsertStmt.setBoolean(8, sourceJoinOnOriginal);
            connInsertStmt.setBoolean(9, targetJoinOnOriginal);
            connInsertStmt.setString(10, marmotScript);
            connInsertStmt.execute();
            connInsertStmt.close();
        } else {
            PreparedStatement connInsertStmt = conn.prepareStatement("INSERT INTO COMPOSITE_CONNECTION (" +
                    "SOURCE_FEED_NODE_ID, TARGET_FEED_NODE_ID, SOURCE_JOIN, TARGET_JOIN, COMPOSITE_FEED_ID, " +
                    "left_join, right_join, left_join_on_original, right_join_on_original, marmot_script) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            connInsertStmt.setLong(1, sourceFeedID);
            connInsertStmt.setLong(2, targetFeedID);
            connInsertStmt.setLong(3, sourceJoin.getKeyID());
            connInsertStmt.setLong(4, targetJoin.getKeyID());
            connInsertStmt.setLong(5, feedID);
            connInsertStmt.setBoolean(6, sourceOuterJoin);
            connInsertStmt.setBoolean(7, targetOuterJoin);
            connInsertStmt.setBoolean(8, sourceJoinOnOriginal);
            connInsertStmt.setBoolean(9, targetJoinOnOriginal);
            connInsertStmt.setString(10, marmotScript);
            connInsertStmt.execute();
            connInsertStmt.close();
        }
    }

    private boolean matches(AnalysisItem source, Key targetKey, long sourceID) {
        Key key = source.getKey();
        if (key.toKeyString().equals(targetKey.toKeyString())) {
            if (key instanceof DerivedKey) {
                DerivedKey derivedKey = (DerivedKey) key;
                if (derivedKey.getFeedID() == sourceID) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matches(AnalysisItem source, Key targetKey) {
        Key key = source.getKey();
        return (key.toKeyString().equals(targetKey.toKeyString()));
    }

    public MergeAudit merge(DataSet sourceSet, DataSet dataSet, Set<AnalysisItem> sourceFields,
                            Set<AnalysisItem> targetFields, String sourceName, String targetName, EIConnection conn, long sourceID, long targetID, int operations) {
        Key myJoinDimension = null;
        if (sourceItem == null) {
            for (AnalysisItem item : sourceFields) {
                if (matches(item, getSourceJoin(), sourceID)) {
                    myJoinDimension = item.createAggregateKey();
                }
            }
            if (myJoinDimension == null) {
                for (AnalysisItem item : sourceFields) {
                    if (matches(item, getSourceJoin())) {
                        myJoinDimension = item.createAggregateKey();
                    }
                }
            }
        } else {
            for (AnalysisItem item : sourceFields) {
                if (matches(item, sourceItem.getKey(), sourceID)) {
                    myJoinDimension = item.createAggregateKey();
                }
            }
            if (myJoinDimension == null) {
                for (AnalysisItem item : sourceFields) {
                    if (matches(item, sourceItem.getKey())) {
                        myJoinDimension = item.createAggregateKey();
                    }
                }
            }
        }
        Key fromJoinDimension = null;
        if (targetItem == null) {
            for (AnalysisItem item : targetFields) {
                if (matches(item, getTargetJoin(), targetID)) {
                    fromJoinDimension = item.createAggregateKey();
                }
            }
            if (fromJoinDimension == null) {
                for (AnalysisItem item : targetFields) {
                    if (matches(item, getTargetJoin())) {
                        fromJoinDimension = item.createAggregateKey();
                    }
                }
            }
        } else {
            for (AnalysisItem item : targetFields) {
                if (matches(item, targetItem.getKey(), targetID)) {
                    fromJoinDimension = item.createAggregateKey();
                }
            }
            if (fromJoinDimension == null) {
                for (AnalysisItem item : targetFields) {
                    if (matches(item, targetItem.getKey())) {
                        fromJoinDimension = item.createAggregateKey();
                    }
                }
            }
        }
        if (myJoinDimension == null) {
            System.out.println("Couldn't find " + getSourceJoin().toKeyString() + " on " + sourceName);
        }
        if (fromJoinDimension == null) {
            System.out.println("Couldn't find " + getTargetJoin().toKeyString() + " on " + targetName);
        }
        String mergeString = "Merging data set on " + sourceName + " : " + myJoinDimension.toKeyString() + " to " + targetName + " : " + fromJoinDimension.toKeyString();

        Map<Value, List<IRow>> index = new HashMap<Value, List<IRow>>();

        Collection<IRow> unjoinedRows = new ArrayList<IRow>();
        List<IRow> sourceSetRows = sourceSet.getRows();
        List<IRow> targetSetRows = dataSet.getRows();
        int accountType;
        try {
            accountType = SecurityUtil.getAccountTier();
        } catch (Exception e) {
            accountType = Account.BASIC;
        }
        Iterator<IRow> sourceIter = sourceSetRows.iterator();
        while (sourceIter.hasNext()) {
            IRow row = sourceIter.next();
            Value joinDimensionValue = row.getValue(myJoinDimension);
            if (joinDimensionValue == null || joinDimensionValue.type() == Value.EMPTY) {
                if (!sourceOuterJoin) {
                    operations++;
                    if (operations % 100000 == 0) {
                        validateOperation(operations, accountType);
                    }
                    unjoinedRows.add(row);
                }
            } else {
                List<IRow> rows = index.get(joinDimensionValue);
                if (rows == null){
                    rows = new ArrayList<IRow>(1);
                    index.put(joinDimensionValue, rows);
                }
                operations++;
                if (operations % 100000 == 0) {
                    validateOperation(operations, accountType);
                }
                rows.add(row);
            }
        }
        DataSet result = new DataSet();
        Map<Value, List<IRow>> indexCopy = new HashMap<Value, List<IRow>>(index);
        Iterator<IRow> targetIter = targetSetRows.iterator();
        while (targetIter.hasNext()) {
            IRow row = targetIter.next();
            Value joinDimensionValue = row.getValue(fromJoinDimension);
            if (joinDimensionValue == null || joinDimensionValue.type() == Value.EMPTY) {
                if (!targetOuterJoin) {
                    operations++;
                    if (operations % 100000 == 0) {
                        validateOperation(operations, accountType);
                    }
                    unjoinedRows.add(row);
                }
            } else {
                indexCopy.remove(joinDimensionValue);
                List<IRow> sourceRows = index.get(joinDimensionValue);
                if (sourceRows == null) {
                    if (!targetOuterJoin) {
                        operations++;
                        if (operations % 100000 == 0) {
                            validateOperation(operations, accountType);
                        }
                        unjoinedRows.add(row);
                    }
                } else {
                    for (IRow sourceRow : sourceRows) {
                        operations++;
                        if (operations % 100000 == 0) {
                            validateOperation(operations, accountType);
                        }
                        sourceRow.merge(row, result);
                    }
                }
            }
        }


        for (List<IRow> rows : indexCopy.values()) {
            for (IRow row : rows) {
                result.createRow().addValues(row);
            }
        }
        for (IRow row : unjoinedRows) {
            result.createRow().addValues(row);
        }
        MergeAudit mergeAudit = new MergeAudit(mergeString, result);
        mergeAudit.setOperations(operations);
        return mergeAudit;
    }

    private void validateOperation(int operationCount, int accountType) {
        if (accountType == Account.BASIC) {
            if (operationCount > 5000000) {
                throw new ReportException(new GenericReportFault("The query requesting data was too complex."));
            }
        } else if (accountType == Account.PLUS) {
            if (operationCount > 7500000) {
                throw new ReportException(new GenericReportFault("The query requesting data was too complex."));
            }
        } else {
            if (operationCount > 10000000) {
                throw new ReportException(new GenericReportFault("The query requesting data was too complex."));
            }
        }
    }

    private List<Key> removeSourceKeys;

    public void setRemoveSourceKeys(List<Key> removeSourceKeys) {
        this.removeSourceKeys = removeSourceKeys;
    }

    private List<Key> removeTargetKeys;

    public void setRemoveTargetKeys(List<Key> removeTargetKeys) {
        this.removeTargetKeys = removeTargetKeys;
    }

    public boolean hasRemoveSourceKeys() {
        return removeSourceKeys != null && removeSourceKeys.size() > 0;
    }

    public boolean hasRemoveTargetKeys() {
        return removeTargetKeys != null && removeTargetKeys.size() > 0;
    }

    public IRow removeSourceValues(IRow row, Set<AnalysisItem> sourceFields) {
        IRow outerRow = new Row(row.getDataSetKeys());
        outerRow.addValues(row);
        for (Key key : removeSourceKeys) {
            Key matchedKey = null;
            for (AnalysisItem item : sourceFields) {
                if (item.getKey().toBaseKey().getKeyID() == (key.toBaseKey().getKeyID())) {
                    matchedKey = item.createAggregateKey();
                }
            }
            row.removeValue(matchedKey);
        }
        return outerRow;
    }

    public IRow removeTargetValues(IRow row, Set<AnalysisItem> targetFields) {
        IRow outerRow = new Row(row.getDataSetKeys());
        outerRow.addValues(row);
        for (Key key : removeTargetKeys) {
            Key matchedKey = null;
            for (AnalysisItem item : targetFields) {
                if (item.getKey().toBaseKey().getKeyID() == (key.toBaseKey().getKeyID())) {
                    matchedKey = item.createAggregateKey();
                }
            }
            row.removeValue(matchedKey);
        }
        return outerRow;
    }
}
