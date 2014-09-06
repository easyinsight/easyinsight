package com.easyinsight.analysis;

/**
 * User: James Boe
 * Date: Oct 17, 2008
 * Time: 10:42:22 AM
 */
public class AggregationFactory {

    private AnalysisMeasure analysisMeasure;
    private IAggregationState aggregationState;
    private boolean skip;

    public AggregationFactory(AnalysisMeasure analysisMeasure, boolean skip) {
        this.analysisMeasure = analysisMeasure;
        if (!skip) {
            switch (analysisMeasure.getAggregation()) {
                case AggregationTypes.NORMALS:
                    aggregationState = new NormalsAggregationState();
                    break;
                case AggregationTypes.RANK:
                    aggregationState = new RankAggregationState();
                    break;
                case AggregationTypes.PERCENT_OF_TOTAL:
                    aggregationState = new PercentOfTotalAggregationState();
                    break;
                default:
                    aggregationState = new ReAggregationState(analysisMeasure);
                    break;
            }
        }
        this.skip = skip;
    }

    public Aggregation getAggregation() {
        return getAggregation(analysisMeasure.getQueryAggregation());
    }

    public Aggregation getAggregation(int value) {
        if (skip) {
            return new SumAggregation();
        }
        Aggregation aggregation;
        switch (value) {
            case AggregationTypes.SUM:
                aggregation = new SumAggregation();
                break;
            case AggregationTypes.AVERAGE:
                aggregation = new AverageAggregation();
                break;
            case AggregationTypes.MAX:
                aggregation = new MaxAggregation();
                break;
            case AggregationTypes.MIN:
                aggregation = new MinAggregation();
                break;
            case AggregationTypes.COUNT:
                aggregation = new CountAggregation();
                break;
            case AggregationTypes.COUNT_DISTINCT:
                aggregation = new CountAggregation();
                break;
            case AggregationTypes.LAST_VALUE:
                aggregation = new SumAggregation();
                break;
            case AggregationTypes.MEDIAN:
                aggregation = new MedianAggregation();
                break;
            case AggregationTypes.VARIANCE:
                aggregation = new VarianceAggregation();
                break;
            case AggregationTypes.RANK:
                aggregation = new RankAggregation(aggregationState, getAggregation(AggregationTypes.SUM));
                aggregationState.addAggregation(aggregation);
                break;
            case AggregationTypes.PERCENT_OF_TOTAL:
                aggregation = new PercentOfValueAggregation(aggregationState, getAggregation(AggregationTypes.SUM));
                aggregationState.addAggregation(aggregation);
                break;
            default:
                aggregation = new SumAggregation();
        }
        return aggregation;
    }
}
