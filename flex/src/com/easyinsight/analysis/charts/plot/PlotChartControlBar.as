package com.easyinsight.analysis.charts.plot {
import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.AnalysisItemUpdateEvent;
import com.easyinsight.analysis.DimensionDropArea;
import com.easyinsight.analysis.IReportControlBar;
import com.easyinsight.analysis.ListDropAreaGrouping;
import com.easyinsight.analysis.MeasureDropArea;
import com.easyinsight.analysis.ReportDataEvent;
import mx.collections.ArrayCollection;
import mx.containers.HBox;
public class PlotChartControlBar extends HBox implements IReportControlBar  {

    private var dimensionGrouping:ListDropAreaGrouping;
    private var xmeasureGrouping:ListDropAreaGrouping;
    private var ymeasureGrouping:ListDropAreaGrouping;

    private var xAxisDefinition:PlotChartDefinition;

    public function PlotChartControlBar() {
        dimensionGrouping = new ListDropAreaGrouping();
        dimensionGrouping.maxElements = 1;
        dimensionGrouping.dropAreaType = DimensionDropArea;
        dimensionGrouping.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, requestListData);
        xmeasureGrouping = new ListDropAreaGrouping();
        xmeasureGrouping.maxElements = 1;
        xmeasureGrouping.dropAreaType = MeasureDropArea;
        xmeasureGrouping.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, requestListData);
        ymeasureGrouping = new ListDropAreaGrouping();
        ymeasureGrouping.maxElements = 1;
        ymeasureGrouping.dropAreaType = MeasureDropArea;
        ymeasureGrouping.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, requestListData);
    }

    override protected function createChildren():void {
        super.createChildren();
        addChild(dimensionGrouping);
        addChild(xmeasureGrouping);
        addChild(ymeasureGrouping);
         if (xAxisDefinition.dimension != null) {
            dimensionGrouping.addAnalysisItem(xAxisDefinition.dimension);
        }
        if (xAxisDefinition.xaxisMeasure != null) {
            xmeasureGrouping.addAnalysisItem(xAxisDefinition.xaxisMeasure);
        }
        if (xAxisDefinition.yaxisMeasure != null) {
            ymeasureGrouping.addAnalysisItem(xAxisDefinition.yaxisMeasure);
        }
    }

    private function requestListData(event:AnalysisItemUpdateEvent):void {
        dispatchEvent(new ReportDataEvent(ReportDataEvent.REQUEST_DATA));
    }

    public function set analysisDefinition(analysisDefinition:AnalysisDefinition):void {
        xAxisDefinition = analysisDefinition as PlotChartDefinition;
    }

    public function isDataValid():Boolean {
        return (dimensionGrouping.getListColumns().length > 0 && xmeasureGrouping.getListColumns().length > 0
                && ymeasureGrouping.getListColumns().length > 0);
    }

    public function createAnalysisDefinition():AnalysisDefinition {
        xAxisDefinition.dimension = dimensionGrouping.getListColumns()[0];
        xAxisDefinition.xaxisMeasure = xmeasureGrouping.getListColumns()[0];
        xAxisDefinition.yaxisMeasure = ymeasureGrouping.getListColumns()[0];
        return xAxisDefinition;
    }

    public function set analysisItems(analysisItems:ArrayCollection):void {
        xmeasureGrouping.analysisItems = analysisItems;
        dimensionGrouping.analysisItems = analysisItems;
        ymeasureGrouping.analysisItems = analysisItems;
    }

    public function addItem(analysisItem:com.easyinsight.analysis.AnalysisItem):void {
    }
}
}