package com.easyinsight.analysis.charts.yaxisbased.bar {

import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.AnalysisItemTypes;
import com.easyinsight.analysis.AnalysisItemUpdateEvent;
import com.easyinsight.analysis.CustomChangeEvent;
import com.easyinsight.analysis.DataServiceEvent;
import com.easyinsight.analysis.DimensionDropArea;
import com.easyinsight.analysis.IReportControlBar;
import com.easyinsight.analysis.ListDropAreaGrouping;
import com.easyinsight.analysis.MeasureDropArea;
import com.easyinsight.analysis.ReportControlBar;
import com.easyinsight.analysis.ReportDataEvent;
import com.easyinsight.analysis.charts.ChartDefinitionEditWindow;

import com.easyinsight.util.PopUpUtil;

import flash.events.MouseEvent;

import mx.binding.utils.BindingUtils;
import mx.collections.ArrayCollection;
import mx.controls.Button;
import mx.controls.Label;
import mx.controls.LinkButton;
import mx.events.FlexEvent;
import mx.managers.PopUpManager;

public class StackedBarChartControlBar extends ReportControlBar implements IReportControlBar  {

    private var xAxisGrouping:ListDropAreaGrouping;
    private var stackGrouping:ListDropAreaGrouping;
    private var measureGrouping:ListDropAreaGrouping;

    private var xAxisDefinition:StackedBarChartDefinition;

    public function StackedBarChartControlBar() {
        xAxisGrouping = new ListDropAreaGrouping();
        xAxisGrouping.maxElements = 1;
        xAxisGrouping.dropAreaType = DimensionDropArea;
        xAxisGrouping.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, requestListData);
        stackGrouping = new ListDropAreaGrouping();
        stackGrouping.maxElements = 1;
        stackGrouping.dropAreaType = DimensionDropArea;
        stackGrouping.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, requestListData);
        measureGrouping = new ListDropAreaGrouping();
        measureGrouping.maxElements = 1;
        measureGrouping.dropAreaType = MeasureDropArea;
        measureGrouping.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, requestListData);
        setStyle("verticalAlign", "middle");
    }

    [Embed(source="../../../../../../../assets/table_edit.png")]
    public var tableEditIcon:Class;

    override protected function createChildren():void {
        super.createChildren();
        var pieEditButton:Button = new Button();
        pieEditButton.setStyle("icon", tableEditIcon);
        pieEditButton.toolTip = "Edit Chart Properties...";
        pieEditButton.addEventListener(MouseEvent.CLICK, editLimits);
        addChild(pieEditButton);
        var measureLabel:Label = new Label();
        measureLabel.text = "X Axis Measure:";
        measureLabel.setStyle("fontSize", 14);
        addChild(measureLabel);
        addDropAreaGrouping(measureGrouping);
        var groupingLabel:Label = new Label();
        groupingLabel.text = "Y Axis Grouping:";
        groupingLabel.setStyle("fontSize", 14);
        addChild(groupingLabel);
        addDropAreaGrouping(xAxisGrouping);

        var stackLabel:Label = new Label();
        stackLabel.text = "Stack Grouping:";
        stackLabel.setStyle("fontSize", 14);
        addChild(stackLabel);
        addDropAreaGrouping(stackGrouping);

        if (xAxisDefinition.yaxis != null) {
            xAxisGrouping.addAnalysisItem(xAxisDefinition.yaxis);
        }
        if (xAxisDefinition.stackItem != null) {
            stackGrouping.addAnalysisItem(xAxisDefinition.stackItem);
        }
        if (xAxisDefinition.measures != null && xAxisDefinition.measures.length > 0) {
            measureGrouping.addAnalysisItem(xAxisDefinition.measures.getItemAt(0) as AnalysisItem);
        }
        var limitLabel:LinkButton = new LinkButton();
        limitLabel.setStyle("textDecoration", "underline");
        limitLabel.addEventListener(MouseEvent.CLICK, editLimits);
        BindingUtils.bindProperty(limitLabel, "label", this, "limitText");
        addChild(limitLabel);
    }

    private var _limitText:String;

    [Bindable]
    public function get limitText():String {
        return _limitText;
    }

    public function set limitText(val:String):void {
        _limitText = val;
        dispatchEvent(new FlexEvent(FlexEvent.DATA_CHANGE));
    }

    private function editLimits(event:MouseEvent):void {
        var window:ChartDefinitionEditWindow = new ChartDefinitionEditWindow();
        window.chartDefinition = xAxisDefinition;
        window.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, onUpdate);
        PopUpManager.addPopUp(window, this, true);
        PopUpUtil.centerPopUp(window);
    }

    private function onUpdate(event:AnalysisItemUpdateEvent):void {
        dispatchEvent(new ReportDataEvent(ReportDataEvent.REQUEST_DATA));
    }

    public function onDataReceipt(event:DataServiceEvent):void {
        if (event.limitedResults) {
            limitText = "Showing " + event.limitResults + " of " + event.maxResults + " results";
        } else {
            limitText = "";
        }
    }

    private function requestListData(event:AnalysisItemUpdateEvent):void {
        dispatchEvent(new ReportDataEvent(ReportDataEvent.REQUEST_DATA));
    }

    public function set analysisDefinition(analysisDefinition:AnalysisDefinition):void {
        xAxisDefinition = analysisDefinition as StackedBarChartDefinition;
    }

    public function isDataValid():Boolean {
        return (xAxisGrouping.getListColumns().length > 0 && measureGrouping.getListColumns().length > 0 &&
                stackGrouping.getListColumns().length > 0);
    }

    public function createAnalysisDefinition():AnalysisDefinition {
        xAxisDefinition.yaxis = xAxisGrouping.getListColumns()[0];
        xAxisDefinition.measures = new ArrayCollection(measureGrouping.getListColumns());
        xAxisDefinition.stackItem = stackGrouping.getListColumns()[0];
        return xAxisDefinition;
    }

    public function addItem(analysisItem:AnalysisItem):void {
        if (analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
            measureGrouping.addAnalysisItem(analysisItem);
        } else if (analysisItem.hasType(AnalysisItemTypes.DIMENSION)) {
            xAxisGrouping.addAnalysisItem(analysisItem);
        }
        dispatchEvent(new ReportDataEvent(ReportDataEvent.REQUEST_DATA));
    }

    public function onCustomChangeEvent(event:CustomChangeEvent):void {
    }
}
}