package com.easyinsight.pipeline;

import com.easyinsight.analysis.*;
import com.easyinsight.calculations.*;
import com.easyinsight.dataset.DataSet;

import java.util.List;

/**
 * User: jamesboe
 * Date: Nov 23, 2009
 * Time: 11:25:37 AM
 */
public class CalculationComponent implements IComponent, DescribableComponent {

    private AnalysisCalculation analysisCalculation;

    public CalculationComponent(AnalysisCalculation analysisCalculation) {
        this.analysisCalculation = analysisCalculation;
    }

    public AnalysisCalculation getAnalysisCalculation() {
        return analysisCalculation;
    }

    public DataSet apply(DataSet dataSet, PipelineData pipelineData) {
        if (dataSet.getRows().size() == 0) {
            return dataSet;
        }
        FieldCalculationLogic fieldCalculationLogic = new FieldCalculationLogic(analysisCalculation, dataSet);
        fieldCalculationLogic.calculate(analysisCalculation.getCalculationString(), pipelineData.getReport(), pipelineData.getAllItems(), pipelineData.getInsightRequestMetadata());
        pipelineData.getReportItems().add(analysisCalculation);
        List<IComponent> components = fieldCalculationLogic.getGeneratedComponents();
        for (IComponent component : components) {
            dataSet = component.apply(dataSet, pipelineData);
        }
        return dataSet;
    }

    public void decorate(DataResults listDataResults) {

    }

    public String getDescription() {
        return "CalculationComponent on " + analysisCalculation.toDisplay();
    }
}
