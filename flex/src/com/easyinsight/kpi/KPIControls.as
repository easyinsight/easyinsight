package com.easyinsight.kpi {

import com.easyinsight.skin.ImageConstants;
import com.easyinsight.util.PopUpUtil;

import flash.events.MouseEvent;

import mx.containers.HBox;
import mx.controls.Button;
import mx.managers.PopUpManager;
import mx.rpc.events.ResultEvent;
import mx.rpc.remoting.RemoteObject;

public class KPIControls extends HBox {

    private var kpi:KPI;
    private var editButton:Button;
    private var deleteButton:Button;
    private var copyButton:Button;



    [Bindable]
    [Embed(source="../../../../assets/copy.png")]
    private var copyIcon:Class;

    private var kpiService:RemoteObject;

    public function KPIControls() {
        super();
        editButton = new Button();
        editButton.setStyle("icon", ImageConstants.EDIT_ICON);
        editButton.addEventListener(MouseEvent.CLICK, onEdit);
        addChild(editButton);
        copyButton = new Button();
        copyButton.setStyle("icon", copyIcon);
        copyButton.addEventListener(MouseEvent.CLICK, onCopy);
        addChild(copyButton);
        deleteButton = new Button();
        deleteButton.setStyle("icon", ImageConstants.DELETE_ICON);
        deleteButton.addEventListener(MouseEvent.CLICK, onDelete);
        addChild(deleteButton);
        setStyle("horizontalAlign", "center");
        setStyle("verticalAlign", "middle");
        this.percentWidth = 100;
        this.percentHeight = 100;
        
    }

    private function onCopy(event:MouseEvent):void {
        kpiService = new RemoteObject();
        kpiService.destination = "kpiService";
        kpiService.copyKPI.addEventListener(ResultEvent.RESULT, onCopyResult);
        kpiService.copyKPI.send(kpi, 0);
    }

    private function onCopyResult(event:ResultEvent):void {
        var copy:KPI = kpiService.copyKPI.lastResult as KPI;
        dispatchEvent(new KPIEvent(KPIEvent.KPI_ADDED, copy));
        var kpiWindow:KPIParentWindow = new KPIParentWindow();
        kpiWindow.scorecardID = 0;
        kpiWindow.kpi = copy;
        kpiWindow.addEventListener(KPIEvent.KPI_EDITED, updatedKPI, false, 0, true);
        PopUpManager.addPopUp(kpiWindow, this, true);
        PopUpUtil.centerPopUp(kpiWindow);
    }

    private function onEdit(event:MouseEvent):void {
        var kpiWindow:KPIParentWindow = new KPIParentWindow();
        kpiWindow.scorecardID = 0;
        kpiWindow.kpi = kpi;
        kpiWindow.addEventListener(KPIEvent.KPI_EDITED, updatedKPI, false, 0, true);
        PopUpManager.addPopUp(kpiWindow, this, true);
        PopUpUtil.centerPopUp(kpiWindow);
    }

    private function updatedKPI(event:KPIEvent):void {
        dispatchEvent(event);
    }

    private function onDelete(event:MouseEvent):void {
        kpiService = new RemoteObject();
        kpiService.destination = "kpiService";
        kpiService.deleteKPI.addEventListener(ResultEvent.RESULT, onDeleteResult);
        kpiService.deleteKPI.send(kpi.kpiID);
    }

    private function onDeleteResult(event:ResultEvent):void {
        dispatchEvent(new KPIEvent(KPIEvent.KPI_REMOVED, kpi));
    }

    override public function set data(val:Object):void {
        kpi = val as KPI;
    }

    override public function get data():Object {
        return kpi;
    }
}
}