package com.easyinsight.pipeline;

import com.easyinsight.analysis.*;
import com.easyinsight.core.Value;
import com.easyinsight.dataset.DataSet;

import java.util.Calendar;

/**
 * User: James Boe
 * Date: May 18, 2009
 * Time: 3:18:51 PM
 */
public class DateTransformComponent implements IComponent {

    private AnalysisItem analysisItem;

    public DateTransformComponent(AnalysisItem analysisItem) {
        this.analysisItem = analysisItem;
    }

    public DataSet apply(DataSet dataSet, PipelineData pipelineData) {
        Calendar calendar = Calendar.getInstance();
        for (IRow row : dataSet.getRows()) {

            Value value = row.getValue(analysisItem.createAggregateKey());
            /*boolean shift = false;
            if (analysisItem.hasType(AnalysisItemTypes.DATE_DIMENSION)) {
                shift = ((AnalysisDateDimension) analysisItem).isTimeshift();
            }*/
            boolean timezoneShift = false;
            if (analysisItem.hasType(AnalysisItemTypes.DATE_DIMENSION)) {
                AnalysisDateDimension date = (AnalysisDateDimension) analysisItem;
                timezoneShift = date.isTimeshift();
                System.out.println("using " + timezoneShift + " for " + date.toDisplay());
            }
            Value transformedValue = analysisItem.transformValue(value, pipelineData.getInsightRequestMetadata(), timezoneShift, calendar);

            row.addValue(analysisItem.createAggregateKey(), transformedValue);

        }
        return dataSet;
    }

    public void decorate(DataResults listDataResults) {
    }
}
