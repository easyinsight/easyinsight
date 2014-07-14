package com.easyinsight.pipeline;

import com.easyinsight.analysis.*;
import com.easyinsight.core.EmptyValue;
import com.easyinsight.core.Value;
import com.easyinsight.dataset.DataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jamesboe
 * Date: 9/21/11
 * Time: 11:54 AM
 */
public class FieldFilterComponent implements IComponent {

    private List<FilterPair> filterPairs = new ArrayList<FilterPair>();

    public void addFilterPair(AnalysisItem analysisItem, FilterDefinition filterDefinition) {
        filterPairs.add(new FilterPair(analysisItem, filterDefinition));
    }

    public DataSet apply(DataSet dataSet, PipelineData pipelineData) {
        for (FilterPair filterPair : filterPairs) {
            filterPair.materializedFilterDefinition = filterPair.filterDefinition.materialize(pipelineData.getInsightRequestMetadata());
            StringBuilder sb = new StringBuilder();
            for (AnalysisItem analysisItem : pipelineData.getAllRequestedItems()) {
                sb.append(analysisItem.toDisplay()).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            pipelineData.getInsightRequestMetadata().addAudit(filterPair.analysisItem, "Applied field filter on " + filterPair.filterDefinition.getField().toDisplay() + " with " + sb.toString() + " as other fields present.");
        }
        System.out.println(dataSet.getRows());
        for (IRow row : dataSet.getRows()) {
            for (FilterPair filterPair : filterPairs) {
                Value value = row.getValue(filterPair.filterDefinition.getField());
                boolean invalid = !filterPair.materializedFilterDefinition.allows(value);
                if (filterPair.filterDefinition.isNotCondition()) {
                    invalid = !invalid;
                }
                if (invalid) {
                    row.addValue(filterPair.analysisItem.createAggregateKey(), new EmptyValue());
                }
            }
        }
        return dataSet;
    }

    public void decorate(DataResults listDataResults) {
    }

    private static class FilterPair {
        AnalysisItem analysisItem;
        FilterDefinition filterDefinition;
        MaterializedFilterDefinition materializedFilterDefinition;

        private FilterPair(AnalysisItem analysisItem, FilterDefinition filterDefinition) {
            this.analysisItem = analysisItem;
            this.filterDefinition = filterDefinition;
        }
    }
}
