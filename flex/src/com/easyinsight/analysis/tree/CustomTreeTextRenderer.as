package com.easyinsight.analysis.tree {
import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.AnalysisHierarchyItem;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.HierarchyLevel;
import com.easyinsight.analysis.TextReportFieldExtension;
import com.easyinsight.pseudocontext.PseudoContextWindow;
import com.easyinsight.pseudocontext.StandardContextWindow;

import flash.events.Event;

import flash.events.MouseEvent;

import mx.controls.Alert;

import mx.controls.listClasses.IListItemRenderer;
import mx.core.UITextField;
import mx.core.UITextFormat;
import mx.managers.PopUpManager;

public class CustomTreeTextRenderer extends UITextField implements IListItemRenderer {

    private var _analysisItem:AnalysisItem;
    private var _data:Object;
    private var _report:AnalysisDefinition;

    public function CustomTreeTextRenderer() {
        super();
    }

    public function set report(value:AnalysisDefinition):void {
        _report = value;
    }

    public function get analysisItem():AnalysisItem {
        return _analysisItem;
    }

    public function set analysisItem(val:AnalysisItem):void {
        _analysisItem = val;
    }
    
    private var _depth:int;

    public function get depth():int {
        return _depth;
    }

    public function set depth(value:int):void {
        _depth = value;
    }

    public function validateProperties():void {
    }

    public function validateDisplayList():void {
    }

    public function validateSize(recursive:Boolean = false):void {
    }
    public function get data():Object {
        return _data;
    }

    public function set data(value:Object):void {
        this._data = value;
        if (analysisItem.reportFieldExtension != null && analysisItem.reportFieldExtension is TextReportFieldExtension) {
            var ext:TextReportFieldExtension = analysisItem.reportFieldExtension as TextReportFieldExtension;
            if (ext.wordWrap) {
                this.multiline = true;
                this.wordWrap = true;
            }
        }
        var treeDef:TreeDefinition = _report as TreeDefinition;
        var index:int = -1;
        for (var i:int = 0; i < AnalysisHierarchyItem(treeDef.hierarchy).hierarchyLevels.length; i++) {
            var level:HierarchyLevel = AnalysisHierarchyItem(treeDef.hierarchy).hierarchyLevels.getItemAt(i) as HierarchyLevel;
            if (level.analysisItem == analysisItem) {
                depth = i;
            }
        }

        var color:uint = depth == 0 ? 0xFFFFFF : 0x000000;
        trace("depth = " + depth + ", color = " + color);
        var tf:UITextFormat = new UITextFormat(this.systemManager, _report.getFont(), _report.fontSize, color);
        setTextFormat(tf);

        new StandardContextWindow(analysisItem, passThrough, this, value, _report, false);
    }

    private function passThrough(event:Event):void {
        dispatchEvent(event);
    }
}
}