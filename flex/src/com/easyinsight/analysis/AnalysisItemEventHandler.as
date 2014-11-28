package com.easyinsight.analysis {
import com.easyinsight.AnalysisItemDeleteEvent;
import com.easyinsight.WindowManagement;

import com.easyinsight.util.ProgressAlert;

import mx.collections.ArrayCollection;

import mx.core.UIComponent;
import mx.managers.PopUpManager;
import mx.rpc.events.ResultEvent;
import mx.rpc.remoting.RemoteObject;

[Event(name="addedItemUpdate", type="com.easyinsight.analysis.AddedItemUpdateEvent")]
[Event(name="itemCopy", type="com.easyinsight.analysis.AnalysisItemCopyEvent")]
[Event(name="analysisItemDelete", type="com.easyinsight.AnalysisItemDeleteEvent")]
public class AnalysisItemEventHandler extends UIComponent {

    private var analysisService:RemoteObject;

    private var _analysisItems:ArrayCollection;
    private var _dataSourceID:int;

    private var _report:AnalysisDefinition;


    public function get report():AnalysisDefinition {
        return _report;
    }

    public function set report(value:AnalysisDefinition):void {
        _report = value;
    }

    public function AnalysisItemEventHandler() {
        super();
        analysisService = new RemoteObject();
        analysisService.destination = "analysisDefinition";
        analysisService.cloneItem.addEventListener(ResultEvent.RESULT, onCopy)
    }

    public function set analysisItems(value:ArrayCollection):void {
        _analysisItems = value;
    }

    public function set dataSourceID(value:int):void {
        _dataSourceID = value;
    }

    public function copyField(event:ReportEditorFieldEvent):void {
        tmpX = event.x;
        tmpY = event.y;
        ProgressAlert.alert(this, "Copying the field...", null, analysisService.cloneItem);
        analysisService.cloneItem.send(event.item.analysisItem);
    }

    private var tmpX:int;
    private var tmpY:int;

    private function onCopy(event:ResultEvent):void {
        var copyItem:AnalysisItem = analysisService.cloneItem.lastResult as AnalysisItem;
        edit(copyItem, analysisItemCreated);
    }

    public function deleteField(event:ReportEditorFieldEvent):void {
        dispatchEvent(new AnalysisItemDeleteEvent(event.item));
    }

    private function edit(analysisItem:AnalysisItem, handler:Function, analysisItemWrapper:AnalysisItemWrapper = null, x:int = 0, y:int = 0):void {
        var editor:Class = null;
        var editor2:Class = null;
        if (analysisItem.hasType(AnalysisItemTypes.CALCULATION)) {
            editor = TryAgainCalculationMeasureWindow;
        } else if (analysisItem.hasType(AnalysisItemTypes.DERIVED_GROUPING)) {
            editor = TryAgainDerivedGroupingWindow;
        } else if (analysisItem.hasType(AnalysisItemTypes.HIERARCHY)) {
            editor = HierarchyWindow;
        } else if (analysisItem.hasType(AnalysisItemTypes.DERIVED_DATE)) {
            editor = TryAgainDerivedDateWindow;
        }
        if (analysisItem.hasType(AnalysisItemTypes.CALCULATION)) {
            editor2 = CalculationMeasureConfigWindow;
        } else if (analysisItem.hasType(AnalysisItemTypes.DERIVED_GROUPING)) {
            editor2 = DerivedGroupingConfigWindow;
        } else if (analysisItem.hasType(AnalysisItemTypes.DERIVED_DATE)) {
            editor2 = DerivedDateConfigWindow;
        }
        var analysisItemEditor:AnalysisItemEditWindow = new AnalysisItemEditWindow();
        analysisItemEditor.x = tmpX;
        analysisItemEditor.y = tmpY;
        analysisItemEditor.editorClass = editor;
        analysisItemEditor.editorClass2 = editor2;
        analysisItemEditor.report = report;
        analysisItemEditor.originalWrapper = analysisItemWrapper;
        analysisItemEditor.analysisItem = analysisItem;
        analysisItemEditor.dataSourceID = _dataSourceID;
        analysisItemEditor.analysisItems = this._analysisItems;
        analysisItemEditor.addEventListener(AnalysisItemEditEvent.ANALYSIS_ITEM_EDIT, handler, false, 0, true);
        WindowManagement.manager.addWindow(analysisItemEditor);
        PopUpManager.addPopUp(analysisItemEditor, this.parent);
    }

    private function analysisItemCreated(event:AnalysisItemEditEvent):void {
        var copyItem:AnalysisItem = event.analysisItem;
        var node:AnalysisItemNode = new AnalysisItemNode();
        node.analysisItem = copyItem;
        var wrapper:AnalysisItemWrapper = new AnalysisItemWrapper(node);
        dispatchEvent(new AnalysisItemCopyEvent(AnalysisItemCopyEvent.ITEM_COPY, copyItem, wrapper));
    }

    public function editField(event:ReportEditorFieldEvent):void {
        tmpX = event.x;
        tmpY = event.y;
        var analysisItemWrapper:AnalysisItemWrapper = event.item;
        edit(event.item.analysisItem, analysisItemEdited, analysisItemWrapper);
    }

    private function analysisItemEdited(event:AnalysisItemEditEvent):void {
        var analysisItemWrapper:AnalysisItemWrapper = event.previousItemWrapper;
        var existingItem:AnalysisItem = analysisItemWrapper.analysisItem;
        analysisItemWrapper.analysisItem = event.analysisItem;
        analysisItemWrapper.displayName = event.analysisItem.display;
        dispatchEvent(new AddedItemUpdateEvent(AddedItemUpdateEvent.UPDATE, existingItem, analysisItemWrapper, event.analysisItem));
    }
}
}