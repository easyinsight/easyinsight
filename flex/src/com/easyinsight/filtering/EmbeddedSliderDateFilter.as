package com.easyinsight.filtering
{
	import com.easyinsight.analysis.AnalysisDateDimensionResultMetadata;
	import com.easyinsight.analysis.AnalysisItem;
	import com.easyinsight.analysis.AnalysisItemResultMetadata;
import com.easyinsight.framework.User;

import flash.events.Event;
import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.containers.HBox;
import mx.controls.Alert;
import mx.controls.CheckBox;
import mx.controls.DateField;
	import mx.controls.HSlider;
import mx.controls.Label;
import mx.events.CalendarLayoutChangeEvent;
	import mx.events.SliderEvent;
import mx.formatters.DateFormatter;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.RemoteObject;

	public class EmbeddedSliderDateFilter extends HBox implements IEmbeddedFilter
	{
		private var dataService:RemoteObject;
		private var hslider:HSlider;
		private var lowField:DateField;
		private var highField:DateField;
		private var lowDate:Date;
		private var highDate:Date;
		private var delta:Number;
		private var analysisItem:AnalysisItem;
		private var _filterDefinition:FilterDateRangeDefinition;
		private var _analysisItems:ArrayCollection;
		
		[Bindable]
        [Embed(source="../../../../assets/navigate_cross.png")]
        public var deleteIcon:Class;
        
        [Bindable]
        [Embed(source="../../../../assets/pencil.png")]
        public var editIcon:Class;
		
		public function EmbeddedSliderDateFilter(feedID:int, analysisItem:AnalysisItem) {
			super();
			this.analysisItem = analysisItem;
			dataService = new RemoteObject();
			dataService.destination = "data";
			dataService.getAnalysisItemMetadata.addEventListener(ResultEvent.RESULT, gotMetadata);
            dataService.getAnalysisItemMetadata.addEventListener(FaultEvent.FAULT, onFault);
			dataService.getAnalysisItemMetadata.send(feedID, analysisItem, new Date().getTimezoneOffset());
		}

        private function onFault(event:FaultEvent):void {
            Alert.show(event.fault.faultDetail);
        }

        private function dataTipFormatter(value:Number):String {
            var date:Date = new Date(lowDate.valueOf() + delta * (value / 100));
            var df:DateFormatter = new DateFormatter();
            df.formatString = User.getInstance().getDateFormat();
            return df.format(date);
            
        }


        private function onChange(event:Event):void {
            var checkbox:CheckBox = event.currentTarget as CheckBox;
            _filterDefinition.enabled = checkbox.selected;
            dispatchEvent(new EmbeddedFilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, _filterDefinition, null, this));
        }
		
		private function gotMetadata(event:ResultEvent):void {
			var metadata:AnalysisItemResultMetadata = dataService.getAnalysisItemMetadata.lastResult as AnalysisItemResultMetadata;
			var dateMetadata:AnalysisDateDimensionResultMetadata = metadata as AnalysisDateDimensionResultMetadata;
			this.lowDate = dateMetadata.earliestDate;
			this.highDate = dateMetadata.latestDate;
			this.delta = highDate.valueOf() - lowDate.valueOf();
			hslider = new HSlider();
			hslider.thumbCount = 2;
			hslider.liveDragging = false;
			hslider.minimum = 0;
            hslider.dataTipFormatFunction = dataTipFormatter;
			hslider.maximum = 100;
			hslider.values = [0, 100];
			hslider.addEventListener(SliderEvent.THUMB_RELEASE, thumbRelease);
			lowField = new DateField();
			lowField.selectedDate = dateMetadata.earliestDate;
			lowField.addEventListener(CalendarLayoutChangeEvent.CHANGE, lowDateChange);
			highField = new DateField();
			highField.selectedDate = dateMetadata.latestDate;
			highField.addEventListener(CalendarLayoutChangeEvent.CHANGE, highDateChange);
            //if (!_filterEditable) {
                var checkbox:CheckBox = new CheckBox();
                checkbox.selected = _filterDefinition == null ? true : _filterDefinition.enabled;;
                checkbox.toolTip = "Click to disable this filter.";
                checkbox.addEventListener(Event.CHANGE, onChange);
                addChild(checkbox);
            //}
            if (_showLabel) {
                var label:Label = new Label();
                label.text = analysisItem.display + ":";
                addChild(label);
            } else {
                toolTip = analysisItem.display;
            }
			addChild(lowField);
			addChild(hslider);
			addChild(highField);

                if (_filterDefinition.sliding && _filterDefinition.startDate != null && _filterDefinition.endDate != null) {
                    var nowDelta:int = dateMetadata.latestDate.getTime() - _filterDefinition.endDate.getTime();
                    _filterDefinition.startDate = new Date(_filterDefinition.startDate.getTime() + nowDelta);
                    _filterDefinition.endDate = new Date(_filterDefinition.endDate.getTime() + nowDelta);
                }
				if (_filterDefinition.startDate == null) {
					_filterDefinition.startDate = dateMetadata.earliestDate;
				}
				lowField.selectedDate = _filterDefinition.startDate;
				if (_filterDefinition.endDate == null) {
					_filterDefinition.endDate = dateMetadata.latestDate;
				}
				highField.selectedDate = _filterDefinition.endDate;
				var newLowVal:int = ((lowField.selectedDate.valueOf() - lowDate.valueOf()) / delta) * 100;
				var newHighVal:int = ((highField.selectedDate.valueOf() - lowDate.valueOf()) / delta) * 100;
				hslider.values = [ newLowVal, newHighVal ] ;

			
			
			dispatchEvent(new EmbeddedFilterUpdatedEvent(FilterUpdatedEvent.FILTER_ADDED, filterDefinition, null, this));			
		}

		private function onFilterEdit(event:FilterEditEvent):void {
			dispatchEvent(new EmbeddedFilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, event.filterDefinition, event.previousFilterDefinition, this));
		}
		
		private function deleteSelf(event:MouseEvent):void {
			dispatchEvent(new EmbeddedFilterDeletionEvent(this));
		}

        private var _filterEditable:Boolean = true;

        public function set filterEditable(editable:Boolean):void {
            _filterEditable = editable;
        }
		
		private function thumbRelease(event:SliderEvent):void {
			var lowValue:int = hslider.values[0];
			var highValue:int = hslider.values[1];
			
			var newLowDate:Date = new Date();
			var newHighDate:Date = new Date();		
			
			newLowDate.setTime(lowDate.valueOf() + delta * (lowValue / 100));
			newHighDate.setTime(lowDate.valueOf() + delta * (highValue / 100));
			
			_filterDefinition.startDate = newLowDate;
			_filterDefinition.endDate = newHighDate;
			lowField.selectedDate = newLowDate;
			highField.selectedDate = newHighDate;			
			dispatchEvent(new EmbeddedFilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, filterDefinition, null, this));
		}
		
		private function lowDateChange(event:CalendarLayoutChangeEvent):void {
			var newLowVal:int = ((event.newDate.valueOf() - lowDate.valueOf()) / delta) * 100;			
			hslider.values = [ newLowVal, hslider.values[1] ] ;
			_filterDefinition.startDate = event.newDate;
			dispatchEvent(new EmbeddedFilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, filterDefinition, null, this));
		}
		
		private function highDateChange(event:CalendarLayoutChangeEvent):void {
			var newHighVal:int = ((event.newDate.valueOf() - lowDate.valueOf()) / delta) * 100;
			hslider.values = [ hslider.values[0], newHighVal ] ;
			_filterDefinition.endDate = event.newDate;
            _filterDefinition.sliding = false;
			dispatchEvent(new EmbeddedFilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, filterDefinition, null, this));
		}
		
		public function set filterDefinition(filterDefinition:FilterDefinition):void {
			this._filterDefinition = filterDefinition as FilterDateRangeDefinition;			
		}
		
		public function get filterDefinition():FilterDefinition {
			return _filterDefinition;
		}

        private var _showLabel:Boolean;

        public function set showLabel(show:Boolean):void {
            _showLabel = show;
        }
	}
}