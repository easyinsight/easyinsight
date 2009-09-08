package com.easyinsight.pipeline;

import com.easyinsight.dataset.DataSet;
import com.easyinsight.analysis.ListTransform;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.ListDataResults;

import java.util.List;
import java.util.ArrayList;

/**
 * User: James Boe
 * Date: May 18, 2009
 * Time: 2:48:47 PM
 */
public class BroadAggregationComponent implements IComponent {

    public DataSet apply(DataSet dataSet, PipelineData pipelineData) {
        List<AnalysisItem> derivedItems = new ArrayList<AnalysisItem>();
        for (AnalysisItem item : pipelineData.getReport().getAllAnalysisItems()) {
            derivedItems.addAll(item.getDerivedItems());
        }

        List<AnalysisItem> list = new ArrayList<AnalysisItem>(pipelineData.getReportItems());
        ListTransform listTransform = dataSet.listTransform(list, list);
        return listTransform.aggregate(list, derivedItems, list);
    }

    public void decorate(ListDataResults listDataResults) {
    }
}