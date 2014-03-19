package com.easyinsight.pipeline;

import com.easyinsight.analysis.DataResults;
import com.easyinsight.analysis.FilterDefinition;
import com.easyinsight.analysis.IRow;
import com.easyinsight.analysis.MaterializedFilterDefinition;
import com.easyinsight.dataset.DataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jamesboe
 * Date: 10/21/13
 * Time: 11:44 AM
 */
public class BetterFilterComponent implements IComponent {

    private List<FilterDefinition> filters;

    public BetterFilterComponent(List<FilterDefinition> filters) {
        this.filters = filters;
    }

    public DataSet apply(DataSet dataSet, PipelineData pipelineData) {
        DataSet resultDataSet = new DataSet();
        resultDataSet.copyState(dataSet);
        List<MaterializedFilterDefinition> materializedFilterDefinitionList = new ArrayList<MaterializedFilterDefinition>();
        for (FilterDefinition filter : filters) {
            MaterializedFilterDefinition materializedFilter = filter.materialize(pipelineData.getInsightRequestMetadata());
            materializedFilterDefinitionList.add(materializedFilter);
            materializedFilter.log(pipelineData.getInsightRequestMetadata(), filter);
        }
        for (IRow row : dataSet.getRows()) {
            boolean valid = true;
            int i = 0;
            for (MaterializedFilterDefinition filter : materializedFilterDefinitionList) {
                valid = filter.validate(row, filters.get(i));
                if (!valid) {
                    break;
                }
                i++;
            }
            if (valid) {
                IRow newRow = resultDataSet.createRow();
                newRow.addValues(row);
                newRow.setPassthroughRow(row.getPassthroughRow());
            }
        }
        return resultDataSet;
    }

    public void decorate(DataResults listDataResults) {
    }
}
