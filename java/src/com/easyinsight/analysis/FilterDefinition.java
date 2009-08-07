package com.easyinsight.analysis;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * User: James Boe
 * Date: Jan 12, 2008
 * Time: 9:47:18 PM
 */
public abstract class FilterDefinition implements Serializable {
    private AnalysisItem field;
    private boolean applyBeforeAggregation = true;
    private long filterID;
    private boolean intrinsic;
    private boolean enabled = true; 

    public FilterDefinition() {
    }

    public FilterDefinition(AnalysisItem field) {
        this.field = field;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isIntrinsic() {
        return intrinsic;
    }

    public void setIntrinsic(boolean intrinsic) {
        this.intrinsic = intrinsic;
    }

    public long getFilterID() {
        return filterID;
    }

    public void setFilterID(long filterID) {
        this.filterID = filterID;
    }

    public AnalysisItem getField() {
        return field;
    }

    public void setField(AnalysisItem field) {
        this.field = field;
    }

    public boolean isApplyBeforeAggregation() {
        return applyBeforeAggregation;
    }

    public void setApplyBeforeAggregation(boolean applyBeforeAggregation) {
        this.applyBeforeAggregation = applyBeforeAggregation;
    }

    public abstract PersistableFilterDefinition toPersistableFilterDefinition();

    public abstract MaterializedFilterDefinition materialize(InsightRequestMetadata insightRequestMetadata);

    public abstract String toQuerySQL(String tableName);

    public abstract int populatePreparedStatement(PreparedStatement preparedStatement, int start, int type, InsightRequestMetadata insightRequestMetadata) throws SQLException;

    public boolean validForQuery() {
        return enabled;
    }
}
