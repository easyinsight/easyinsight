package com.easyinsight.analysis;

import com.easyinsight.core.Value;

/**
 * User: James Boe
 * Date: Jan 16, 2008
 * Time: 9:09:23 PM
 */
public abstract class MaterializedFilterDefinition {
    private AnalysisItem key;
    
    public MaterializedFilterDefinition(AnalysisItem key) {
        this.key = key;
    }

    public AnalysisItem getKey() {
        return key;
    }

    public abstract boolean allows(Value value, Value preTransformValue);
}
