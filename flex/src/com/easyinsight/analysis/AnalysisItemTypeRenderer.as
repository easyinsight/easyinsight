package com.easyinsight.analysis {
import mx.events.FlexEvent;

import mx.binding.utils.BindingUtils;
import mx.controls.Image;
import mx.containers.HBox;

public class AnalysisItemTypeRenderer extends HBox {
    [Bindable]
    [Embed(source="../../../../assets/calendar.png")]
    public static var dateIcon:Class;

    [Bindable]
    [Embed(source="../../../../assets/text.png")]
    public static var groupingIcon:Class;

    [Bindable]
    [Embed(source="../../../../assets/text_sum.png")]
    public static var measureIcon:Class;

    [Bindable]
    [Embed(source="../../../../assets/cubes_x16.png")]
    public static var hierarchyIcon:Class;

    [Bindable]
    [Embed(source="../../../../assets/folder.png")]
    public static var folderIcon:Class;
                               
    [Bindable]
    [Embed(source="../../../../assets/text_formula.png")]
    public static var calculationIcon:Class;


    [Bindable]
    [Embed(source="../../../../assets/graph_edge_curved.png")]
    public static var cycleIcon:Class;


    private var typeIcon:Image;

    private var _typeSource:Class = groupingIcon;

    private var analysisItemWrapper:AnalysisItemWrapper;

    public function AnalysisItemTypeRenderer() {
        super();
        setStyle("horizontalAlign", "center");
        this.percentWidth = 100;
    }

    [Bindable]
    public function get typeSource():Class {
        return _typeSource;
    }

    public function set typeSource(val:Class):void {
        _typeSource = val;
        dispatchEvent(new FlexEvent(FlexEvent.DATA_CHANGE));
    }

    override protected function createChildren():void {
        super.createChildren();
        if (typeIcon == null) {
            typeIcon = new Image();
            BindingUtils.bindProperty(typeIcon, "source", this, "typeSource");
            //typeIcon.source = typeSource;
        }
        addChild(typeIcon);
    }

    override public function get data():Object {
        return analysisItemWrapper;
    }

    public static function getImageClass(analysisItemWrapper:AnalysisItemWrapper):Class {
        var typeSource:Class;
        if (analysisItemWrapper.isAnalysisItem()) {
            if (analysisItemWrapper.analysisItem.hasType(AnalysisItemTypes.CALCULATION)) {
                typeSource = calculationIcon;
            } else if (analysisItemWrapper.analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
                //typeIcon.source = measureIcon;
                typeSource = measureIcon;
            } else if (analysisItemWrapper.analysisItem.hasType(AnalysisItemTypes.STEP)) {
                typeSource = cycleIcon;
            } else if (analysisItemWrapper.analysisItem.hasType(AnalysisItemTypes.DATE)) {
                //typeIcon.source = dateIcon;
                typeSource = dateIcon;
            } else if (analysisItemWrapper.analysisItem.hasType(AnalysisItemTypes.HIERARCHY)) {
                typeSource = hierarchyIcon;
            } else {
                //typeIcon.source = grou/ingIcon;
                typeSource = groupingIcon;
            }
        } else {
            typeSource = folderIcon;
        }
        return typeSource;
    }

    override public function set data(obj:Object):void {
        this.analysisItemWrapper = obj as AnalysisItemWrapper;
        if (analysisItemWrapper.isAnalysisItem()) {
            if (analysisItemWrapper.analysisItem.hasType(AnalysisItemTypes.CALCULATION)) {
                typeSource = calculationIcon;
                this.toolTip = "Calculation";
            } else if (analysisItemWrapper.analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
                //typeIcon.source = measureIcon;
                typeSource = measureIcon;
                this.toolTip = "Measure";
            } else if (analysisItemWrapper.analysisItem.hasType(AnalysisItemTypes.STEP)) {
                typeSource = cycleIcon;
                this.toolTip = "Cycle";
            } else if (analysisItemWrapper.analysisItem.hasType(AnalysisItemTypes.DATE)) {
                //typeIcon.source = dateIcon;
                typeSource = dateIcon;
                this.toolTip = "Date";
            } else if (analysisItemWrapper.analysisItem.hasType(AnalysisItemTypes.HIERARCHY)) {
                typeSource = hierarchyIcon;
                this.toolTip = "Hierarchy";
            } else {
                //typeIcon.source = grou/ingIcon;
                typeSource = groupingIcon;
                this.toolTip = "Grouping";
            }
        } else {
            typeSource = folderIcon;
            this.toolTip = "Folder";
        }
    }
}
}