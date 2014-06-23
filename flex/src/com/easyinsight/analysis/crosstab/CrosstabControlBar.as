package com.easyinsight.analysis.crosstab {
import com.easyinsight.analysis.AnalysisChangedEvent;
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
import com.easyinsight.analysis.ReportPropertiesEvent;

import flash.events.MouseEvent;

import mx.binding.utils.BindingUtils;
import mx.collections.ArrayCollection;
import mx.controls.Label;
import mx.events.FlexEvent;

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
        measureGrouping.unlimited = true;
        measureGrouping.dropAreaType = MeasureDropArea;
        measureGrouping.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, requestListData);
    }

    private function onUpdate(event:AnalysisItemUpdateEvent):void {
        dispatchEvent(new ReportDataEvent(ReportDataEvent.REQUEST_DATA));
    }

    override protected function createChildren():void {
        super.createChildren();

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
        limitLabel.addEventListener(MouseEvent.CLICK, function(event:MouseEvent):void {
            dispatchEvent(new ReportPropertiesEvent(2));
        });
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
            measureGrouping.addAnalysisItem(analysisItem);
        } else if (analysisItem.hasType(AnalysisItemTypes.DIMENSION)) {
            if (xAxisDefinition.columns == null || xAxisDefinition.columns.length == 0) {
                columnGrouping.addAnalysisItem(analysisItem);
            } else if (xAxisDefinition.rows == null || xAxisDefinition.rows.length == 0) {
                rowGrouping.addAnalysisItem(analysisItem);
            } else {
                columnGrouping.addAnalysisItem(analysisItem);
            }
        }
        dispatchEvent(new ReportDataEvent(ReportDataEvent.REQUEST_DATA));
        dispatchEvent(new AnalysisChangedEvent(false));
    }

    public function onCustomChangeEvent(event:CustomChangeEvent):void {
    }
}
}