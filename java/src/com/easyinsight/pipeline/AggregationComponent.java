package com.easyinsight.pipeline;

import com.easyinsight.analysis.DataResults;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.analysis.ListTransform;
import com.easyinsight.analysis.AnalysisItem;

import java.util.List;
import java.util.ArrayList;

/**
 * User: James Boe
 * Date: May 18, 2009
 * Time: 2:48:47 PM
 */
public class AggregationComponent implements IComponent {

    public DataSet apply(DataSet dataSet, PipelineData pipelineData) {
        List<AnalysisItem> derivedItems = new ArrayList<AnalysisItem>();
        for (AnalysisItem item : pipelineData.getReport().getAllAnalysisItems()) {
            derivedItems.addAll(item.getDerivedItems());
        }
        List<AnalysisItem> allRequestedAnalysisItems = new ArrayList<AnalysisItem>(pipelineData.getReport().getAllAnalysisItems());
        List<AnalysisItem> list = new ArrayList<AnalysisItem>(pipelineData.getReportItems());
        ListTransform listTransform = dataSet.listTransform(list, allRequestedAnalysisItems);
        return listTransform.aggregate(list, derivedItems);
    }

    public void decorate(DataResults listDataResults) {
    }
}
