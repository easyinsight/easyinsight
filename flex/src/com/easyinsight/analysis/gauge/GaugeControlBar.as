package com.easyinsight.analysis.gauge {
import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.AnalysisItemUpdateEvent;
import com.easyinsight.analysis.CustomChangeEvent;
import com.easyinsight.analysis.DataServiceEvent;
import com.easyinsight.analysis.IReportControlBar;
import com.easyinsight.analysis.MeasureDropArea;
import com.easyinsight.analysis.ReportDataEvent;
import com.easyinsight.map.MapDropAreaGrouping;
import flash.events.Event;
import mx.collections.ArrayCollection;
import mx.containers.HBox;
import mx.controls.ComboBox;
import mx.controls.Label;
import mx.controls.TextInput;
public class GaugeControlBar extends HBox implements IReportControlBar {

    private var measureGrouping:MapDropAreaGrouping;
    private var maxValueInput:TextInput;
    private var gaugeTypeBox:ComboBox;
    private var gaugeDefinition:GaugeDefinition;

    public function GaugeControlBar() {
        measureGrouping = new MapDropAreaGrouping();
        measureGrouping.maxElements = 1;
        measureGrouping.dropAreaType = MeasureDropArea;
        measureGrouping.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, requestListData);
        maxValueInput = new TextInput();
        gaugeTypeBox = new ComboBox();
        gaugeTypeBox.dataProvider = new ArrayCollection( [{label: "Circular Gauge", type: 1}, {label: "Horizontal Gauge", type: 2}]);
        setStyle("verticalAlign", "middle");
    }


    override protected function createChildren():void {
        super.createChildren();
        var gaugeLabel:Label = new Label();
        gaugeLabel.text = "Gauge Type: ";
        addChild(gaugeLabel);
        addChild(gaugeTypeBox);
        var measureLabel:Label = new Label();
        measureLabel.text = "Measure: ";
        addChild(measureLabel);
        addChild(measureGrouping);
        var maxValueLabel:Label = new Label();
        maxValueLabel.text = "Max Value: ";
        addChild(maxValueLabel);
        addChild(maxValueInput);
        maxValueInput.addEventListener(Event.CHANGE, onMaxValueChange);
        if (gaugeDefinition.measure != null) {
            measureGrouping.addAnalysisItem(gaugeDefinition.measure);
        }
        maxValueInput.text = String(gaugeDefinition.maxValue);
    }

    private function onMaxValueChange(event:Event):void {
        dispatchEvent(new ReportDataEvent(ReportDataEvent.REQUEST_DATA));
    }

    private function requestListData(event:AnalysisItemUpdateEvent):void {
        dispatchEvent(new ReportDataEvent(ReportDataEvent.REQUEST_DATA));
    }

    public function set analysisDefinition(analysisDefinition:AnalysisDefinition):void {
        gaugeDefinition = analysisDefinition as GaugeDefinition;
    }

    public function createAnalysisDefinition():AnalysisDefinition {
        gaugeDefinition.measure = measureGrouping.getListColumns()[0];
        gaugeDefinition.maxValue = int(maxValueInput.text);
        return gaugeDefinition;
    }

    public function set analysisItems(analysisItems:ArrayCollection):void {
        measureGrouping.analysisItems = analysisItems;
    }

    public function isDataValid():Boolean {
        var value:int = int(maxValueInput.text);
        return (measureGrouping.getListColumns().length > 0 && value > 0);
    }

    public function addItem(analysisItem:AnalysisItem):void {

    }

    public function onCustomChangeEvent(event:CustomChangeEvent):void {
    }

    public function onDataReceipt(event:DataServiceEvent):void {
    }
}
}