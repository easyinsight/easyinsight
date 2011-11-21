package com.easyinsight.calculations;

import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.DataResults;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.pipeline.IComponent;
import com.easyinsight.pipeline.PipelineData;

import java.util.Iterator;

/**
 * User: jamesboe
 * Date: 11/20/11
 * Time: 5:16 PM
 */
public class AdjustPipelineComponent implements IComponent {

    private String name = "Related Provider";

    public DataSet apply(DataSet dataSet, PipelineData pipelineData) {
        Iterator<AnalysisItem> iter = pipelineData.getAllRequestedItems().iterator();
        while (iter.hasNext()) {
            AnalysisItem analysisItem = iter.next();
            if (analysisItem.getDisplayName() != null && analysisItem.getDisplayName().contains(name)) {
                System.out.println("Removed " + analysisItem.toDisplay() + " from pipeline");
                iter.remove();
            }
        }
        return dataSet;
    }

    public void decorate(DataResults listDataResults) {
    }
}
