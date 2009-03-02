package com.easyinsight.analysis;

import com.easyinsight.core.Value;
import com.easyinsight.core.NumericValue;
import com.easyinsight.core.EmptyValue;

/**
 * User: James Boe
 * Date: Feb 27, 2009
 * Time: 9:14:20 AM
 */
public class LastValueTemporalAggregation extends TemporalAggregation implements ITemporalAggregation {
    private Double previousValue;
    private Double latestValue;
    private int positionLimit;

    protected LastValueTemporalAggregation(AnalysisDimension sortDate, AnalysisMeasure wrappedMeasure, int newAggregation, boolean requiresReAggregation) {
        super(sortDate, wrappedMeasure, newAggregation, requiresReAggregation);
    }

    public void addValue(Value value, int position) {
        previousValue = latestValue;
        latestValue = value.toDouble();
        positionLimit = position;
    }

    public Value getValue(int i) {
        if (i == positionLimit && latestValue != null) {
            return new NumericValue(latestValue);
        } else {
            return new EmptyValue();
        }
    }
}
