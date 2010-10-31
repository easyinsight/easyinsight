package com.easyinsight.analysis.crosstab {
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
import com.easyinsight.analysis.list.CrosstabDefinitionEditWindow;
import com.easyinsight.util.PopUpUtil;

import flash.events.MouseEvent;

import mx.binding.utils.BindingUtils;
import mx.collections.ArrayCollection;
import mx.containers.Grid;
import mx.containers.GridItem;
import mx.containers.GridRow;
import mx.controls.Button;
import mx.controls.Label;
import mx.events.FlexEvent;
import mx.managers.PopUpManager;

public class CrosstabControlBar extends ReportControlBar implements IReportControlBar {
    private var rowGrouping:ListDropAreaGrouping;
    private var columnGrouping:ListDropAreaGrouping;
    private var measureGrouping:ListDropAreaGrouping;

    private var xAxisDefinition:CrosstabDefinition;

    public function CrosstabControlBar() {
        rowGrouping = new ListDropAreaGrouping();
        rowGrouping.unlimited = true;
        rowGrouping.dropAreaType = DimensionDropArea;
        rowGrouping.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, requestListData);
        columnGrouping = new ListDropAreaGrouping();
        columnGrouping.unlimited = true;
        columnGrouping.dropAreaType = DimensionDropArea;
        columnGrouping.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, requestListData);
        measureGrouping = new ListDropAreaGrouping();
        measureGrouping.maxElements = 1;
        measureGrouping.dropAreaType = MeasureDropArea;
        measureGrouping.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, requestListData);
    }

    [Embed(source="../../../../../assets/table_edit.png")]
    public var tableEditIcon:Class;

    private function editLimits(event:MouseEvent):void {
        var window:CrosstabDefinitionEditWindow = new CrosstabDefinitionEditWindow();
        window.crosstabDefinition = xAxisDefinition;
        window.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, onUpdate);
        PopUpManager.addPopUp(window, this, true);
        PopUpUtil.centerPopUp(window);
    }

    private function onUpdate(event:AnalysisItemUpdateEvent):void {
        dispatchEvent(new ReportDataEvent(ReportDataEvent.REQUEST_DATA));
    }

    override protected function createChildren():void {
        super.createChildren();
        var pieEditButton:Button = new Button();
        pieEditButton.setStyle("icon", tableEditIcon);
        pieEditButton.toolTip = "Edit Chart Properties...";
        pieEditButton.addEventListener(MouseEvent.CLICK, editLimits);
        addChild(pieEditButton);

        var rowGroupingLabel:Label = new Label();
        rowGroupingLabel.text = "Row Grouping:";
        rowGroupingLabel.setStyle("fontSize", 14);
        addChild(rowGroupingLabel);
        addDropAreaGrouping(rowGrouping, this);

        var columnGroupingLabel:Label = new Label();
        columnGroupingLabel.text = "Column Grouping:";
        columnGroupingLabel.setStyle("fontSize", 14);
        addChild(columnGroupingLabel);
        addDropAreaGrouping(columnGrouping, this);

        var measureLabel:Label = new Label();
        measureLabel.text = "Measure:";
        measureLabel.setStyle("fontSize", 14);
        addChild(measureLabel);
        addDropAreaGrouping(measureGrouping, this);

        
        if (xAxisDefinition.columns != null) {
            for each (var column:AnalysisItem in xAxisDefinition.columns) {
                columnGrouping.addAnalysisItem(column);
            }
        }
        if (xAxisDefinition.rows != null) {
            for each (var row:AnalysisItem in xAxisDefinition.rows) {
                rowGrouping.addAnalysisItem(row);
            }
        }
        if (xAxisDefinition.measures != null) {
            for each (var measure:AnalysisItem in xAxisDefinition.measures) {
                measureGrouping.addAnalysisItem(measure);
            }
        }
        var limitLabel:Label = new Label();
        BindingUtils.bindProperty(limitLabel, "text", this, "limitText");
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
        xAxisDefinition = analysisDefinition as CrosstabDefinition;
    }

    public function isDataValid():Boolean {
        return (columnGrouping.getListColumns().length > 0 && measureGrouping.getListColumns().length > 0 &&
                rowGrouping.getListColumns().length > 0);
    }

    public function createAnalysisDefinition():AnalysisDefinition {
        xAxisDefinition.columns = new ArrayCollection(columnGrouping.getListColumns());
        xAxisDefinition.measures = new ArrayCollection(measureGrouping.getListColumns());
        xAxisDefinition.rows = new ArrayCollection(rowGrouping.getListColumns());
        return xAxisDefinition;
    }

    public function addItem(analysisItem:com.easyinsight.analysis.AnalysisItem):void {
        if (analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
            xAxisDefinition.measures = new ArrayCollection([analysisItem]);
        } else if (analysisItem.hasType(AnalysisItemTypes.DIMENSION)) {
            if (xAxisDefinition.columns.length == 0) {
                xAxisDefinition.columns.addItem(analysisItem);
            } else if (xAxisDefinition.rows.length == 0) {
                xAxisDefinition.rows.addItem(analysisItem);
            } else {
                xAxisDefinition.columns = new ArrayCollection([analysisItem]); 
            }
        }
    }

    public function onCustomChangeEvent(event:CustomChangeEvent):void {
    }
}
}