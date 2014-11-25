package com.easyinsight.analysis
{
import com.easyinsight.AnalysisItemDeleteEvent;
import com.easyinsight.skin.ImageConstants;
import com.easyinsight.util.PopUpUtil;

import flash.events.Event;
import flash.events.MouseEvent;
import flash.geom.Point;

import mx.binding.utils.BindingUtils;
import mx.collections.ArrayCollection;
import mx.controls.Button;
import mx.controls.listClasses.IListItemRenderer;
import mx.core.UIComponent;
import mx.events.FlexEvent;
import mx.managers.PopUpManager;

import mx.rpc.events.ResultEvent;
import mx.rpc.remoting.RemoteObject;

public class ReportEditorFieldControls extends UIComponent implements IListItemRenderer
	{
		private var analysisItemWrapper:AnalysisItemWrapper;
		private var _displayName:String;
		private var _analysisItems:ArrayCollection;
		private var button:Button;
		private var editButton:Button;
		private var deleteButton:Button;
    private var _dataSourceID:int;

        private var analysisService:RemoteObject;

		[Bindable]
		[Embed(source="../../../../assets/copy.png")]
        public var copyIcon:Class;





    private var _showDelete:Boolean;
    private var _showCopy:Boolean;

    [Bindable(event="showCopyChanged")]
    public function get showCopy():Boolean {
        return _showCopy;
    }


    public function set dataSourceID(value:int):void {
        _dataSourceID = value;
    }

    public function set showCopy(value:Boolean):void {
        if (_showCopy == value) return;
        _showCopy = value;
        dispatchEvent(new Event("showCopyChanged"));
    }

    [Bindable(event="showDeleteChanged")]
    public function get showDelete():Boolean {
        return _showDelete;
    }

    public function set showDelete(value:Boolean):void {
        if (_showDelete == value) return;
        _showDelete = value;
        dispatchEvent(new Event("showDeleteChanged"));
    }

    private var _showEdit:Boolean;


    [Bindable(event="showEditChanged")]
    public function get showEdit():Boolean {
        return _showEdit;
    }

    public function set showEdit(value:Boolean):void {
        if (_showEdit == value) return;
        _showEdit = value;
        dispatchEvent(new Event("showEditChanged"));
    }

    public function ReportEditorFieldControls()
		{
			super();	
			setStyle("horizontalAlign", "center");
			this.percentWidth = 100;		
		}

    override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
        super.updateDisplayList(unscaledWidth, unscaledHeight);
        var buttonWidth:int = 40;
        var buttonHeight:int = 22;
        var padding:int = 5;
        editButton.move((padding),0);
        editButton.setActualSize(buttonWidth, buttonHeight);
        button.move((padding * 2) + (buttonWidth),0);
        button.setActualSize(buttonWidth, buttonHeight);
        deleteButton.move((padding * 3) + (buttonWidth * 2),0);
        deleteButton.setActualSize(buttonWidth, buttonHeight);
    }
		
		override protected function createChildren():void {
            if (editButton == null) {
				editButton = new Button();
				editButton.setStyle("icon", ImageConstants.EDIT_ICON);
				editButton.toolTip = "Edit...";
                BindingUtils.bindProperty(editButton, "visible", this, "showEdit");
                editButton.addEventListener(MouseEvent.CLICK, editItem);
			}
            addChild(editButton);
			if (button == null) {
				button = new Button();
				button.setStyle("icon", copyIcon);
				button.toolTip = "Copy...";
                BindingUtils.bindProperty(button, "visible", this, "showCopy");
                button.addEventListener(MouseEvent.CLICK, copy);
			}
            addChild(button);

            if (deleteButton == null) {
                deleteButton = new Button();
                deleteButton.setStyle("icon", ImageConstants.DELETE_ICON);
                BindingUtils.bindProperty(deleteButton, "visible", this, "showDelete");
                deleteButton.toolTip = "Delete Field";

                deleteButton.addEventListener(MouseEvent.CLICK, deleteItem);
            }
            addChild(deleteButton);
		}

        private function copy(event:MouseEvent):void {
            analysisService = new RemoteObject();
            analysisService.destination = "analysisDefinition";
            analysisService.cloneItem.addEventListener(ResultEvent.RESULT, onCopy);
            analysisService.cloneItem.send(analysisItemWrapper.analysisItem);
        }

    private function onCopy(event:ResultEvent):void {
        var copyItem:AnalysisItem = analysisService.cloneItem.lastResult as AnalysisItem;
        var p:Point = new Point(this.x, this.y);
        var g:Point = parent.localToGlobal(p);
        dispatchEvent(new AnalysisItemCopyEvent(AnalysisItemCopyEvent.ITEM_COPY, copyItem, null, g.x, g.y));
    }
		
		[Bindable]
		public function get displayName():String {
			return _displayName;
		}
		
		public function set displayName(displayName:String):void {
			this._displayName = displayName;
		}

        private function deleteItem(event:MouseEvent):void {
            dispatchEvent(new AnalysisItemDeleteEvent(analysisItemWrapper));
        }

        public function set analysisItems(value:ArrayCollection):void {
            _analysisItems = value;
        }

        private function editItem(event:MouseEvent):void {

            var analysisItem:AnalysisItem = analysisItemWrapper.analysisItem;
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
			analysisItemEditor.editorClass = editor; 			
			analysisItemEditor.editorClass2 = editor2;
			analysisItemEditor.analysisItem = analysisItemWrapper.analysisItem;
            analysisItemEditor.dataSourceID = _dataSourceID;
			analysisItemEditor.analysisItems = this._analysisItems;
			analysisItemEditor.addEventListener(AnalysisItemEditEvent.ANALYSIS_ITEM_EDIT, analysisItemEdited, false, 0, true);
			PopUpManager.addPopUp(analysisItemEditor, this.parent);
			PopUpUtil.centerPopUp(analysisItemEditor);
        }
		
		private function analysisItemEdited(event:AnalysisItemEditEvent):void {
            var existingItem:AnalysisItem = this.analysisItemWrapper.analysisItem;
			this.analysisItemWrapper.analysisItem = event.analysisItem;
			this.analysisItemWrapper.displayName = event.analysisItem.display;
			this.displayName = event.analysisItem.displayName;
            dispatchEvent(new AddedItemUpdateEvent(AddedItemUpdateEvent.UPDATE, existingItem, this.analysisItemWrapper, event.analysisItem));
		}
		
		[Bindable("dataChange")]
		public function set data(value:Object):void {
			this.analysisItemWrapper = value as AnalysisItemWrapper;
			this.displayName = analysisItemWrapper.displayName;
            if (analysisItemWrapper.analysisItem != null) {
                showCopy = true;
                if (analysisItemWrapper.analysisItem.concrete) {
                    showEdit = false;
                    showDelete = false;
                } else {
                    showEdit = true;
                    showDelete = true;
                }
            } else {
                showCopy = false;
            }
            dispatchEvent(new FlexEvent(FlexEvent.DATA_CHANGE));
		}
		
		public function get data():Object {
			return this.analysisItemWrapper;
		}
	}
}