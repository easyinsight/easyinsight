package com.easyinsight.filtering
{
	import com.easyinsight.analysis.AnalysisDimensionResultMetadata;
	import com.easyinsight.analysis.AnalysisItem;
	import com.easyinsight.analysis.Value;

import com.easyinsight.framework.CredentialsCache;

import flash.events.MouseEvent;
	import flash.geom.Point;
	
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.containers.HBox;
	import mx.controls.Button;
	import mx.controls.ComboBox;
	import mx.events.DropdownEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.RemoteObject;

	public class ComboBoxFilter extends HBox implements IFilter
	{
		private var _filterDefinition:FilterValueDefinition;
		private var _feedID:int;
		private var dataService:RemoteObject;
		private var _analysisItem:AnalysisItem;
		
		private var comboBox:ComboBox;
		private var deleteButton:Button;
		private var editButton:Button;
		private var _analysisItems:ArrayCollection;

        private var _credentials:Object;
		
		[Bindable]
        [Embed(source="../../../../assets/navigate_cross.png")]
        public var deleteIcon:Class;
        
        [Bindable]
        [Embed(source="../../../../assets/pencil.png")]
        public var editIcon:Class;
		
		public function ComboBoxFilter(feedID:int, analysisItem:AnalysisItem)
		{
			super();
			this._feedID = feedID;
			this._analysisItem = analysisItem;			
		}

        public function set credentials(value:Object):void {
            _credentials = value;
        }

        public function set analysisItems(analysisItems:ArrayCollection):void {
			_analysisItems = analysisItems;
		}		
		
		private function edit(event:MouseEvent):void {
			var window:GeneralFilterEditSettings = new GeneralFilterEditSettings();
			window.addEventListener(FilterEditEvent.FILTER_EDIT, onFilterEdit);
			window.analysisItems = _analysisItems;
			window.filterDefinition = _filterDefinition;
			PopUpManager.addPopUp(window, this, true);
			var point:Point = new Point();
			point.x = 0;
			point.y = 0;
			point = this.localToGlobal(point);
			window.x = point.x + 25;
			window.y = point.y + 25;
		}
		
		private function onFilterEdit(event:FilterEditEvent):void {
			dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, event.filterDefinition, event.previousFilterDefinition, this));
		}
		
		override protected function createChildren():void {
			super.createChildren();
			if (comboBox == null) {
				comboBox = new ComboBox();
				comboBox.addEventListener(DropdownEvent.CLOSE, filterValueChanged);
				comboBox.enabled = false;				
			}
			addChild(comboBox);
			if (editButton == null) {
				editButton = new Button();
				editButton.addEventListener(MouseEvent.CLICK, edit);
				editButton.setStyle("icon", editIcon);
				editButton.toolTip = "Edit";
			}
			addChild(editButton);
			if (deleteButton == null) {
				deleteButton = new Button();
				deleteButton.addEventListener(MouseEvent.CLICK, deleteSelf);
				deleteButton.setStyle("icon", deleteIcon);
				deleteButton.toolTip = "Delete";
				deleteButton.enabled = false;
			}
			addChild(deleteButton);
		}
		
		private function filterValueChanged(event:DropdownEvent):void {			
			var newValue:String = event.currentTarget.selectedLabel;
			
			var selectedValue:String = _filterDefinition.filteredValues.getItemAt(0) as String;
			if (newValue != selectedValue) {
				var newFilteredValues:ArrayCollection = new ArrayCollection();
				newFilteredValues.addItem(newValue);
				_filterDefinition.filteredValues = newFilteredValues;
				dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, _filterDefinition, null, this));
			} 
		}
		
		override protected function commitProperties():void {
			super.commitProperties();						
			dataService = new RemoteObject();
			dataService.destination = "data";
			dataService.getAnalysisItemMetadata.addEventListener(ResultEvent.RESULT, gotMetadata);
			dataService.getAnalysisItemMetadata.send(_feedID, _analysisItem, CredentialsCache.getCache().createCredentials());
		}
		
		private function gotMetadata(event:ResultEvent):void {
			var analysisDimensionResultMetadata:AnalysisDimensionResultMetadata = dataService.getAnalysisItemMetadata.lastResult as 
				AnalysisDimensionResultMetadata;
			var strings:ArrayCollection = new ArrayCollection();
			for each (var value:Value in analysisDimensionResultMetadata.values) {
				var string:String = String(value.getValue());
				if (!strings.contains(string)) {
					strings.addItem(string);
				}
			}
			var sort:Sort = new Sort();
			strings.sort = sort;
			strings.refresh();			
			comboBox.dataProvider = strings;
			comboBox.rowCount = Math.min(strings.length, 15);
			var selectedValue:String;
			if (_filterDefinition.filteredValues.length == 0) {
				_filterDefinition.filteredValues.addItem(strings.getItemAt(0));
			}
            var filterObj:Object = _filterDefinition.filteredValues.getItemAt(0);
            if (filterObj is Value) {
                selectedValue = String(filterObj.getValue());
            } else {
                selectedValue = filterObj as String;
            }
			//selectedValue = _filterDefinition.filteredValues.getItemAt(0) as String;
			comboBox.selectedItem = selectedValue;
			comboBox.enabled = true;
			deleteButton.enabled = true;
			dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_ADDED, filterDefinition, null, this));
		}
		
		private function deleteSelf(event:MouseEvent):void {
			dispatchEvent(new FilterDeletionEvent(this));
		}
		
		public function get filterDefinition():FilterDefinition {
			return _filterDefinition;
		}
		
		public function set filterDefinition(filterDefinition:FilterDefinition):void {
			_filterDefinition = filterDefinition as FilterValueDefinition; 	
		}
	}
}