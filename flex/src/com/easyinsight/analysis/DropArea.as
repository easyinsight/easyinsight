package com.easyinsight.analysis
{
import com.easyinsight.commands.CommandEvent;
import com.easyinsight.skin.ImageConstants;

import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.events.ContextMenuEvent;
import flash.events.Event;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.geom.Point;
import flash.ui.ContextMenuItem;
import flash.ui.Keyboard;

import mx.collections.ArrayCollection;
import mx.containers.HBox;
import mx.controls.AdvancedDataGrid;
import mx.controls.Button;
import mx.controls.DataGrid;
import mx.controls.Image;
import mx.controls.List;
import mx.core.Application;
import mx.core.DragSource;
import mx.core.IUIComponent;
import mx.core.UIComponent;
import mx.events.DragEvent;
import mx.managers.DragManager;
import mx.managers.PopUpManager;
import mx.states.AddChild;
import mx.states.State;

public class DropArea extends HBox
{
    private var _analysisItem:AnalysisItem;



    private var editButton:Button;
    private var deleteButton:Button;

    private var _analysisItems:ArrayCollection;

    private var _report:AnalysisDefinition;

    private var _dataSourceID:int;

    public var defaultBackgroundColor:uint = 0xFFFFFF;

    public function set dataSourceID(value:int):void {
        _dataSourceID = value;
    }

    public function DropArea()
    {
        super();
        addChild(createNoDataLabel());
        horizontalScrollPolicy = "off";
        this.addEventListener(DragEvent.DRAG_ENTER, dragEnterHandler);
        this.addEventListener(DragEvent.DRAG_DROP, dragDropHandler);
        this.addEventListener(DragEvent.DRAG_OVER, dragOverHandler);
        this.addEventListener(DragEvent.DRAG_EXIT, dragExitHandler);
        this.addEventListener(KeyboardEvent.KEY_UP, keyPressed);


        editButton = new Button();
        editButton.label = "...";
        editButton.addEventListener(MouseEvent.CLICK, editEvent);
        var configured:State = new State();
        configured.name = "Configured";
        var addChildAction:AddChild = new AddChild();
        addChildAction.relativeTo = this;
        addChildAction.target = editButton;
        var addDeleteButton:AddChild = new AddChild();
        deleteButton = new Button();
        deleteButton.setStyle("icon", ImageConstants.DELETE_ICON);
        deleteButton.addEventListener(MouseEvent.CLICK, onDelete);
        deleteButton.toolTip = "Clear This Field";
        addDeleteButton.relativeTo = this;
        addDeleteButton.target = deleteButton;
        configured.overrides = [ addChildAction, addDeleteButton ];
        states = [ configured ];

        this.setStyle("borderStyle", "solid");
        this.setStyle("borderThickness", 2);
        setStyle("verticalAlign", "middle");
        setStyle("borderColor", 0xB7BABC);
        setStyle("backgroundColor", 0xFFFFFF);
        var deleteContextItem:ContextMenuItem = new ContextMenuItem("Delete Field", true);
        deleteContextItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, onDelete);
        PopupMenuFactory.assignMenu(this, [ deleteContextItem ]);
    }

    public function set report(value:AnalysisDefinition):void {
        _report = value;
    }

    public function highlight(analysisItem:AnalysisItem):Boolean {
        var valid:Boolean = recommend(analysisItem);
        if (valid) {
            setStyle("borderColor", 0x00AA00);
            setStyle("backgroundColor", 0xBBFFBB);
        }
        return valid;
    }

    public function normal():void {
        setStyle("borderColor", 0xB7BABC);
        setStyle("backgroundColor", defaultBackgroundColor);
    }

    private function createNoDataLabel():UIComponent {
        var label:EmptyDropAreaLabel = new EmptyDropAreaLabel();
        label.text = getNoDataLabel();
        return label;
    }

    private function onDelete(event:Event):void {
        deletion();
    }

    private var _dataSourceFields:ArrayCollection;

    public function set dataSourceFields(value:ArrayCollection):void {
        _dataSourceFields = value;
    }

    public function set analysisItems(analysisItems:ArrayCollection):void {
        this._analysisItems = analysisItems;
    }

    protected function supportsDrilldown():Boolean {                         
        return true;
    }

    public function customEditor():Class {
        if (analysisItem.hasType(AnalysisItemTypes.CALCULATION)) {
            return CalculationMeasureWindow;
        } else if (analysisItem.hasType(AnalysisItemTypes.DERIVED_GROUPING)) {
            return DerivedGroupingWindow;
        } else if (analysisItem.hasType(AnalysisItemTypes.DERIVED_DATE)) {
            return DerivedDateWindow;
        } else if (analysisItem.hasType(AnalysisItemTypes.HIERARCHY)) {
            return HierarchyWindow;
        } else {
            return null;
        }
    }

    protected function editEvent(event:MouseEvent, initialWindow:int = 0):void {
        var analysisItemEditor:AnalysisItemEditWindow = new AnalysisItemEditWindow();
        analysisItemEditor.editorClass = customEditor();
        analysisItemEditor.dataSourceID = _dataSourceID;
        analysisItemEditor.analysisItems = this._analysisItems;
        analysisItemEditor.analysisItem = this.analysisItem;
        analysisItemEditor.report = this._report;
        analysisItemEditor.initialWindow = initialWindow;
        if (_report != null) {
            analysisItemEditor.reportType = _report.reportType;
        }
        if (startX > 0) {
            analysisItemEditor.x = startX;
            analysisItemEditor.y = startY;
        } else {
            var p:Point = new Point(this.x, this.y);
            var g:Point = localToGlobal(p);
            analysisItemEditor.x = g.x;
            analysisItemEditor.y = g.y;
        }
        PopUpManager.addPopUp(analysisItemEditor, UIComponent(Application.application));
        dispatchEvent(new FieldEditorEvent(FieldEditorEvent.FIELD_EDITOR_OPENED, analysisItemEditor));
        analysisItemEditor.addEventListener(AnalysisItemEditEvent.ANALYSIS_ITEM_EDIT, itemEdited, false, 0, true);
        analysisItemEditor.addEventListener(Event.CLOSE, onClose, false, 0, true);
    }
    
    private function onClose(event:Event):void {
        dispatchEvent(new FieldEditorEvent(FieldEditorEvent.FIELD_EDITOR_CLOSED, event.currentTarget as AnalysisItemEditWindow));
    }

    public var startX:int;
    public var startY:int;

    private function itemEdited(event:AnalysisItemEditEvent):void {
        dispatchEvent(new CommandEvent(new DropAreaDragUpdateCommand(this, this.analysisItem, event.analysisItem)));
    }

    public function createAnalysisItem():AnalysisItem {
        return analysisItem;
    }

    public function get analysisItem():AnalysisItem {
        return this._analysisItem;
    }

    public function set analysisItem(analysisItem:AnalysisItem):void {
        if (this._analysisItem != null) {
            getChildAt(0).removeEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
        }
        this._analysisItem = analysisItem;

        removeChildAt(0);
        if (analysisItem == null) {
            addChildAt(createNoDataLabel(), 0);
            currentState = "";
        } else {
            var component:UIComponent = DropAreaFactory.createDropItemElement(this, analysisItem);
            component.addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
            addChildAt(component, 0);
            currentState = "Configured";
        }
        if (analysisItem != null && analysisItem.kpi) {
            defaultBackgroundColor = 0xDDDDDD;
            setStyle("backgroundColor", defaultBackgroundColor);
        } else {
            defaultBackgroundColor = 0xFFFFFF;
            setStyle("backgroundColor", defaultBackgroundColor);
        }
    }

    protected function getNoDataLabel():String {
        return null;
    }

    public function dragEnterHandler(event:DragEvent):void {
        var analysisItem:AnalysisItem = null;
        var okay:Boolean = true;
        if (event.dragInitiator is DataGrid) {
            var initialList:DataGrid = event.dragInitiator as DataGrid;
            var newAnalysisItem:AnalysisItemWrapper = initialList.selectedItem as AnalysisItemWrapper;
            analysisItem = newAnalysisItem.analysisItem;
        } else if (event.dragInitiator is DropArea) {
            if (this.analysisItem == null) {
                okay = false;
            } else {
                var dropArea:DropArea = event.dragInitiator as DropArea;
                analysisItem = dropArea.analysisItem;
            }
        } else if (event.dragInitiator is AdvancedDataGrid) {
            var analysisItemLabel:AdvancedDataGrid = event.dragInitiator as AdvancedDataGrid;
            newAnalysisItem = analysisItemLabel.selectedItem as AnalysisItemWrapper;
            if (newAnalysisItem.isAnalysisItem()) {
                analysisItem = newAnalysisItem.analysisItem;
            } else {
                okay = false;
            }
        }
        if (okay && accept(analysisItem)) {
            //setStyle("borderColor", "green");
            DragManager.acceptDragDrop(event.currentTarget as IUIComponent);
        }
    }

    public function accept(analysisItem:AnalysisItem):Boolean {
        return true;
    }

    public function recommend(analysisItem:AnalysisItem):Boolean {
        return true;
    }

    public function isConfigured():Boolean {
        return _analysisItem != null;
    }

    public function getDropAreaType():String {
        return null;
    }

    public function keyPressed(event:KeyboardEvent):void {
        if (event.keyCode == Keyboard.DELETE) {
            dispatchEvent(new CommandEvent(new DragAreaDeleteCommand(this)));
        }
    }

    public function deletion():void {
        dispatchEvent(new AnalysisChangedEvent());
        dispatchEvent(new DropAreaDeletionEvent(this));
    }

    private function onMouseDown(event:MouseEvent):void {
        var dragSource:DragSource = new DragSource();
        var bd:BitmapData = new BitmapData(this.width, this.height);
        bd.draw(this);
        var bitmap:Bitmap = new Bitmap(bd);
        var image:Image = new Image();
        image.source = bitmap;
        DragManager.doDrag(this, dragSource, event, image);
    }

    public function dragDropHandler(event:DragEvent):void {
        if (event.dragInitiator is DataGrid) {
            var newAnalysisItem:AnalysisItemWrapper = event.dragSource.dataForFormat("items")[0] as AnalysisItemWrapper;
            if (this.analysisItem == null) {
                dispatchEvent(new CommandEvent(new DropAreaAddedCommand(this, newAnalysisItem.analysisItem)));
            } else {
                dispatchEvent(new CommandEvent(new DropAreaDragUpdateCommand(this, this.analysisItem, newAnalysisItem.analysisItem)));
            }
        } else if (event.dragInitiator is DropArea) {
            var dropArea:DropArea = event.dragInitiator as DropArea;
            dispatchEvent(new CommandEvent(new DropAreaSwapCommand(dropArea, this)));
        } else if (event.dragInitiator is AdvancedDataGrid) {
            newAnalysisItem = event.dragSource.dataForFormat("treeDataGridItems")[0] as AnalysisItemWrapper;
            if (newAnalysisItem.isAnalysisItem()) {
                if (this.analysisItem == null) {
                    dispatchEvent(new CommandEvent(new DropAreaAddedCommand(this, newAnalysisItem.analysisItem)));
                } else {
                    dispatchEvent(new CommandEvent(new DropAreaDragUpdateCommand(this, this.analysisItem, newAnalysisItem.analysisItem)));
                }
            }
        } else if (event.dragInitiator is List) {
            var list:List = event.dragInitiator as List;
            var newListItem:AnalysisItem = event.dragSource.dataForFormat("items")[0];
            if (this.analysisItem == null) {
                dispatchEvent(new CommandEvent(new DropAreaAddedCommand(this, newListItem)));
            } else {
                dispatchEvent(new CommandEvent(new DropAreaDragUpdateCommand(this, this.analysisItem, newListItem)));
            }
        }
    }

    protected function dragOverHandler(event:DragEvent):void {
        DragManager.showFeedback(DragManager.MOVE);
    }

    protected function dragExitHandler(event:DragEvent):void {
        //setStyle("borderColor", 0xB7BABC);
    }
}
}