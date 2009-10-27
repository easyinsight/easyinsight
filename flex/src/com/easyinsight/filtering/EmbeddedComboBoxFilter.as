package com.easyinsight.filtering
{
	import com.easyinsight.analysis.AnalysisDimensionResultMetadata;
	import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.AnalysisTagsResultMetadata;
import com.easyinsight.analysis.Value;

import com.easyinsight.framework.CredentialsCache;

import flash.events.Event;
import flash.events.MouseEvent;	
	
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.containers.HBox;
	import mx.controls.Button;
import mx.controls.CheckBox;
import mx.controls.ComboBox;
import mx.controls.Label;
import mx.events.DropdownEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.RemoteObject;

	public class EmbeddedComboBoxFilter extends HBox implements IEmbeddedFilter
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

        private var _filterEnabled:Boolean;


        [Bindable(event="filterEnabledChanged")]
        public function get filterEnabled():Boolean {
            return _filterEnabled;
        }

        public function set filterEnabled(value:Boolean):void {
            if (_filterEnabled == value) return;
            _filterEnabled = value;
            dispatchEvent(new Event("filterEnabledChanged"));
        }
		
		public function EmbeddedComboBoxFilter(feedID:int, analysisItem:AnalysisItem)
		{
			super();
			this._feedID = feedID;
			this._analysisItem = analysisItem;
            setStyle("verticalAlign", "middle");
		}

        public function set credentials(value:Object):void {
            _credentials = value;
        }

        public function set analysisItems(analysisItems:ArrayCollection):void {
			_analysisItems = analysisItems;
		}
		
		private function onFilterEdit(event:FilterEditEvent):void {
			dispatchEvent(new EmbeddedFilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, event.filterDefinition, event.previousFilterDefinition, this, event.bubbles, event.rebuild));
		}

        private function onChange(event:Event):void {
            var checkbox:CheckBox = event.currentTarget as CheckBox;
            _filterDefinition.enabled = checkbox.selected;
            comboBox.enabled = checkbox.selected;
            dispatchEvent(new EmbeddedFilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, _filterDefinition, null, this));
        }
		
		override protected function createChildren():void {
			super.createChildren();
            var checkbox:CheckBox = new CheckBox();
            checkbox.selected = _filterDefinition == null ? true : _filterDefinition.enabled;
            checkbox.toolTip = "Click to disable this filter.";
            checkbox.addEventListener(Event.CHANGE, onChange);
            addChild(checkbox);
            if (_showLabel) {
                var label:Label = new Label();
                label.text = _analysisItem.display + ":";
                addChild(label);
            } else {
                toolTip = _analysisItem.display;
            }
			if (comboBox == null) {
				comboBox = new ComboBox();
                comboBox.maxWidth = 300;
				comboBox.addEventListener(DropdownEvent.CLOSE, filterValueChanged);
				comboBox.enabled = false;				
			}
			addChild(comboBox);
		}
		
		private function filterValueChanged(event:DropdownEvent):void {			
			var newValue:String = event.currentTarget.selectedLabel;
			
			var selectedValue:String = _filterDefinition.filteredValues.getItemAt(0) as String;
			if (newValue != selectedValue) {
				var newFilteredValues:ArrayCollection = new ArrayCollection();
				newFilteredValues.addItem(newValue);
				_filterDefinition.filteredValues = newFilteredValues;
				dispatchEvent(new EmbeddedFilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, _filterDefinition, null, this));
			} 
		}
		
		override protected function commitProperties():void {
			super.commitProperties();						
			dataService = new RemoteObject();
			dataService.destination = "data";
			dataService.getAnalysisItemMetadata.addEventListener(ResultEvent.RESULT, gotMetadata);
			dataService.getAnalysisItemMetadata.send(_feedID, _analysisItem, CredentialsCache.getCache().createCredentials(), new Date().getTimezoneOffset());
		}

        private function blah():void {
            var metadata:AnalysisTagsResultMetadata;
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
			strings.sort = new Sort();
			strings.refresh();			
			comboBox.dataProvider = strings;
			comboBox.rowCount = Math.min(strings.length, 15);
            if (_filterDefinition == null) {
                _filterDefinition = new FilterValueDefinition();
                _filterDefinition.field = _analysisItem;
                _filterDefinition.filteredValues = new ArrayCollection();
                _filterDefinition.inclusive = true;
                _filterDefinition.enabled = true;
            }
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
			comboBox.enabled = _filterDefinition.enabled;
            if (deleteButton != null) {
			    deleteButton.enabled = true;
            }
			dispatchEvent(new EmbeddedFilterUpdatedEvent(FilterUpdatedEvent.FILTER_ADDED, filterDefinition, null, this));
		}
		
		private function deleteSelf(event:MouseEvent):void {
			dispatchEvent(new EmbeddedFilterDeletionEvent(this));
		}
		
		public function get filterDefinition():FilterDefinition {
			return _filterDefinition;
		}
		
		public function set filterDefinition(filterDefinition:FilterDefinition):void {
			_filterDefinition = filterDefinition as FilterValueDefinition; 	
		}

        private var _showLabel:Boolean;

        public function set showLabel(show:Boolean):void {
            _showLabel = show;
        }
    }
}