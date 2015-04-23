package com.easyinsight.filtering
{
	import com.easyinsight.analysis.AnalysisDateDimensionResultMetadata;
import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.AnalysisItem;
	import com.easyinsight.analysis.AnalysisItemResultMetadata;
import com.easyinsight.analysis.AnalysisItemTypes;
import com.easyinsight.analysis.IRetrievalState;
import com.easyinsight.filtering.FilterMetadata;
import com.easyinsight.framework.User;
import com.easyinsight.skin.ImageConstants;

import flash.events.Event;
import flash.events.MouseEvent;
import mx.binding.utils.BindingUtils;
import mx.collections.ArrayCollection;
import mx.containers.Box;
import mx.containers.HBox;
import mx.containers.ViewStack;
import mx.controls.Alert;
import mx.controls.Button;
import mx.controls.CheckBox;
import mx.controls.DateField;
	import mx.controls.HSlider;
import mx.controls.Label;
import mx.controls.LinkButton;
import mx.core.UIComponent;
import mx.events.CalendarLayoutChangeEvent;
	import mx.events.SliderEvent;
import mx.formatters.DateFormatter;
import mx.managers.PopUpManager;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.RemoteObject;

	public class SliderDateFilter extends HBox implements IFilter
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

        private var _loadingFromReport:Boolean = false;

        private var _retrievalState:IRetrievalState;

    public function set loadingFromReport(value:Boolean):void {
        _loadingFromReport = value;
    }

        private var _reportID:int;

    private var _dashboardID:int;

    public function set reportID(value:int):void {
        _reportID = value;
    }

    public function set dashboardID(value:int):void {
        _dashboardID = value;
    }

        private var _report:AnalysisDefinition;

        private var filterMetadata:FilterMetadata;
		
		public function SliderDateFilter(feedID:int, analysisItem:AnalysisItem, reportID:int, dashboardID:int, retrievalState:IRetrievalState,
                                         filterMetadata:FilterMetadata, report:AnalysisDefinition = null) {
			super();
            this.feedID = feedID;
            this.report = report;
			this.analysisItem = analysisItem;
            this.filterMetadata = filterMetadata;
            this._retrievalState = retrievalState;
            _report = report;
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

        private var _leftLabel:String;

        private var _rightLabel:String;

        private var _leftIndex:int;

        private var _rightIndex:int;


        [Bindable(event="leftLabelChanged")]
        public function get leftLabel():String {
            return _leftLabel;
        }

        public function set leftLabel(value:String):void {
            if (_leftLabel == value) return;
            _leftLabel = value;
            dispatchEvent(new Event("leftLabelChanged"));
        }

        [Bindable(event="rightLabelChanged")]
        public function get rightLabel():String {
            return _rightLabel;
        }

        public function set rightLabel(value:String):void {
            if (_rightLabel == value) return;
            _rightLabel = value;
            dispatchEvent(new Event("rightLabelChanged"));
        }

        [Bindable(event="leftIndexChanged")]
        public function get leftIndex():int {
            return _leftIndex;
        }

        public function set leftIndex(value:int):void {
            if (_leftIndex == value) return;
            _leftIndex = value;
            dispatchEvent(new Event("leftIndexChanged"));
        }

        [Bindable(event="rightIndexChanged")]
        public function get rightIndex():int {
            return _rightIndex;
        }

        public function set rightIndex(value:int):void {
            if (_rightIndex == value) return;
            _rightIndex = value;
            dispatchEvent(new Event("rightIndexChanged"));
        }

        private function onChange(event:Event):void {
            var checkbox:CheckBox = event.currentTarget as CheckBox;
            _filterDefinition.enabled = checkbox.selected;
            try {
                if (_retrievalState != null) {
                    _retrievalState.updateFilter(_filterDefinition, filterMetadata);
                }
            } catch (e:Error) {
            }
            dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, _filterDefinition, null, this));
        }

        private var filterLabel:UIComponent;

        private var feedID:int;

        private var report:AnalysisDefinition;

        override protected function createChildren():void {
            super.createChildren();
            if (_filterDefinition == null || _filterDefinition.sliderRange) {
                dataService = new RemoteObject();
                dataService.destination = "data";
                dataService.getAnalysisItemMetadata.addEventListener(ResultEvent.RESULT, gotMetadata);
                dataService.getAnalysisItemMetadata.addEventListener(FaultEvent.FAULT, onFault);
                dataService.getAnalysisItemMetadata.send(feedID, analysisItem, new Date().getTimezoneOffset(), _reportID, _dashboardID, report);
            } else {
                createComponents();
            }
        }

        private function createComponents():void {

            if (_filterDefinition == null) {
                _filterDefinition = new FilterDateRangeDefinition();
                _filterDefinition.startDate = lowDate;
                _filterDefinition.endDate = highDate;
                _filterDefinition.field = analysisItem;
                if (analysisItem.hasType(AnalysisItemTypes.STEP)) {
                    _filterDefinition.applyBeforeAggregation = false;
                }
                _filterDefinition.sliding = true;
            } else {
                if (_filterDefinition.startDate == null) {
                    _filterDefinition.startDate = lowDate;
                }
                lowDate = _filterDefinition.startDate;
                if (_filterDefinition.endDate == null) {
                    _filterDefinition.endDate = highDate;
                }
                highDate = _filterDefinition.endDate;

                leftIndex = 0;
                rightIndex = 0;
            }

            if (_filterDefinition == null || !_filterDefinition.toggleEnabled) {
                var checkbox:CheckBox = new CheckBox();
                checkbox.selected = _filterDefinition == null ? true : _filterDefinition.enabled;
                checkbox.toolTip = "Click to disable this filter.";
                checkbox.addEventListener(Event.CHANGE, onChange);
                addChild(checkbox);
            }

            if (_filterEditable) {
                filterLabel = new LinkButton();
                filterLabel.setStyle("fontSize", 12);
                filterLabel.setStyle("textDecoration", "underline");
                filterLabel.addEventListener(MouseEvent.CLICK, edit);
                LinkButton(filterLabel).label = FilterDefinition.getLabel(_filterDefinition, analysisItem);
            } else {
                filterLabel = new Label();
                Label(filterLabel).text = FilterDefinition.getLabel(_filterDefinition, analysisItem);
            }
            filterLabel.styleName = "filterLabel";
            addChild(filterLabel);

            var formatString:String;
            if (User.getInstance() == null) {
                formatString = "YYYY-MM-DD";
            } else {
                switch (User.getInstance().dateFormat) {
                    case 0:
                        formatString = "MM/DD/YYYY";
                        break;
                    case 1:
                        formatString = "YYYY-MM-DD";
                        break;
                    case 2:
                        formatString = "DD-MM-YYYY";
                        break;
                    case 3:
                        formatString = "DD/MM/YYYY";
                        break;
                    case 4:
                        formatString = "DD.MM.YYYY";
                        break;
                }
            }

            if (_filterDefinition.sliderRange) {
                hslider = new HSlider();
                hslider.thumbCount = 2;
                hslider.liveDragging = false;
                hslider.minimum = 0;
                hslider.dataTipFormatFunction = dataTipFormatter;
                hslider.maximum = 100;
                hslider.values = [0, 100];
                hslider.addEventListener(SliderEvent.THUMB_RELEASE, thumbRelease);
            }



            if (_filterDefinition.startDateEnabled) {
                lowField = new DateField();
                lowField.editable = true;
                lowField.formatString = formatString;
                lowField.selectedDate = lowDate;
                lowField.addEventListener(CalendarLayoutChangeEvent.CHANGE, lowDateChange);
            }
            if (_filterDefinition.endDateEnabled) {
                highField = new DateField();
                highField.editable = true;
                highField.selectedDate = highDate;
                highField.formatString = formatString;
                highField.addEventListener(CalendarLayoutChangeEvent.CHANGE, highDateChange);
            }

            if (lowField != null) {
                if (_filterDefinition.startDateYear > 0) {
                    _filterDefinition.startDate = new Date(_filterDefinition.startDateYear, _filterDefinition.startDateMonth, _filterDefinition.startDateDay);
                }
                lowField.selectedDate = _filterDefinition.startDate;
            }

            if (highField != null) {
                if (_filterDefinition.endDateYear > 0) {
                    _filterDefinition.endDate = new Date(_filterDefinition.endDateYear, _filterDefinition.endDateMonth, _filterDefinition.endDateDay);
                }
                highField.selectedDate = _filterDefinition.endDate;
            }

            updateStartEnd();

            if (hslider != null) {
                var newLowVal:int = ((lowField.selectedDate.valueOf() - lowDate.valueOf()) / delta) * 100;
                var newHighVal:int = ((highField.selectedDate.valueOf() - lowDate.valueOf()) / delta) * 100;
                hslider.values = [ newLowVal, newHighVal ] ;
            }

            if (lowField != null) {
                var leftSideStack:ViewStack = new ViewStack();
                BindingUtils.bindProperty(leftSideStack, "selectedIndex", this, "leftIndex");
                leftSideStack.resizeToContent = true;
                var leftFieldBox:Box = new Box();
                leftFieldBox.addChild(lowField);
                leftSideStack.addChild(leftFieldBox);
                var leftLabel:Label = new Label();
                BindingUtils.bindProperty(leftLabel, "text", this, "leftLabel");
                var leftBox:Box = new Box();
                leftBox.addChild(leftLabel);
                leftSideStack.addChild(leftBox);
                addChild(leftSideStack);
            }

            if (hslider != null) {
                addChild(hslider);
            }

            if (highField != null) {
                var rightSideStack:ViewStack = new ViewStack();
                BindingUtils.bindProperty(rightSideStack, "selectedIndex", this, "rightIndex");
                rightSideStack.resizeToContent = true;
                var rightFieldBox:Box = new Box();
                rightFieldBox.addChild(highField);
                rightSideStack.addChild(rightFieldBox);
                var rightLabel:Label = new Label();
                BindingUtils.bindProperty(rightLabel, "text", this, "rightLabel");
                var rightBox:Box = new Box();
                rightBox.addChild(rightLabel);
                rightSideStack.addChild(rightBox);
                addChild(rightSideStack);
            }

            if (_filterEditable) {


                var deleteButton:Button = new Button();
                deleteButton.addEventListener(MouseEvent.CLICK, deleteSelf);
                deleteButton.setStyle("icon", ImageConstants.DELETE_ICON);
                if (_filterDefinition.intrinsic) {
                    deleteButton.enabled = false;
                    deleteButton.toolTip = "This filter is an intrinsic part of the data source and cannot be deleted.";
                } else {
                    deleteButton.toolTip = "Delete";
                }
                addChild(deleteButton);
            }


            if (_loadingFromReport) {
                _loadingFromReport = false;

            } else {
                dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_ADDED, filterDefinition, null, this));
            }
        }

        private function gotMetadata(event:ResultEvent):void {
			var metadata:AnalysisItemResultMetadata = dataService.getAnalysisItemMetadata.lastResult as AnalysisItemResultMetadata;
			var dateMetadata:AnalysisDateDimensionResultMetadata = metadata as AnalysisDateDimensionResultMetadata;
			this.lowDate = dateMetadata.earliestDate;
            if (lowDate == null) {
                lowDate = new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 30));
            }
			this.highDate = dateMetadata.latestDate;
            if (highDate == null) {
                highDate = new Date();
            }
			this.delta = highDate.valueOf() - lowDate.valueOf();
            updateStartEnd();
            createComponents();
		}
		
		public function set analysisItems(analysisItems:ArrayCollection):void {
			_analysisItems = analysisItems;
		}		
		
		private function edit(event:MouseEvent):void {
			var window:GeneralFilterEditSettings = new GeneralFilterEditSettings();
            window.filterMetadata = filterMetadata;
			window.detailClass = DateRangeDetail;
			window.addEventListener(FilterEditEvent.FILTER_EDIT, onFilterEdit, false, 0, true);
			window.analysisItems = _analysisItems;
			window.filterDefinition = _filterDefinition;
			PopUpManager.addPopUp(window, this, true);
			window.x = 50;
			window.y = 50;
		}
		
		private function onFilterEdit(event:FilterEditEvent):void {
            if (filterLabel is LinkButton) {
                LinkButton(filterLabel).label = FilterDefinition.getLabel(event.filterDefinition, analysisItem);
            }
            if (event.filterDefinition is FilterDateRangeDefinition) {
                var filter:FilterDateRangeDefinition = event.filterDefinition as FilterDateRangeDefinition;
                leftIndex = 0;
                rightIndex = 0;

            }
			dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, event.filterDefinition, event.previousFilterDefinition, this));
		}
		
		private function deleteSelf(event:MouseEvent):void {
			dispatchEvent(new FilterDeletionEvent(this));
		}

        private var _filterEditable:Boolean = true;

        public function set filterEditable(editable:Boolean):void {
            _filterEditable = editable;
        }
        
        private function updateStartEnd():void {
            if (_filterDefinition != null && lowDate != null) {
                _filterDefinition.startDateYear = lowDate.getFullYear();
                _filterDefinition.startDateMonth = lowDate.getMonth() + 1;
                _filterDefinition.startDateDay = lowDate.getDate();
            }
            if (_filterDefinition != null && highDate != null) {
                _filterDefinition.endDateYear = highDate.getFullYear();
                _filterDefinition.endDateMonth = highDate.getMonth() + 1;
                _filterDefinition.endDateDay = highDate.getDate();
            }
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
            updateStartEnd();
            try {
                if (_retrievalState != null) {
                    _retrievalState.updateFilter(_filterDefinition, filterMetadata);
                }
            } catch (e:Error) {
            }
			dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, filterDefinition, null, this));
		}
		
		private function lowDateChange(event:CalendarLayoutChangeEvent):void {
            if (hslider != null) {
                var newLowVal:int = ((event.newDate.valueOf() - lowDate.valueOf()) / delta) * 100;
                hslider.values = [ newLowVal, hslider.values[1] ] ;
            }
            var date:Date = event.newDate;
            var offsetDelta:int = new Date().timezoneOffset - date.timezoneOffset;
            if (offsetDelta > 0) {
                date.hours = 1;
            }
			_filterDefinition.startDate = date;
            updateStartEnd();
            try {
                if (_retrievalState != null) {
                    _retrievalState.updateFilter(_filterDefinition, filterMetadata);
                }
            } catch (e:Error) {
            }
			dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, filterDefinition, null, this));
		}
		
		private function highDateChange(event:CalendarLayoutChangeEvent):void {
            if (hslider != null) {
                var newHighVal:int = ((event.newDate.valueOf() - lowDate.valueOf()) / delta) * 100;
                hslider.values = [ hslider.values[0], newHighVal ] ;
            }
            var date:Date = event.newDate;
            var offsetDelta:int = new Date().timezoneOffset - date.timezoneOffset;
            if (offsetDelta > 0) {
                date.hours = 1;
            }
			_filterDefinition.endDate = date;
            _filterDefinition.sliding = false;
            updateStartEnd();
            try {
                if (_retrievalState != null) {
                    _retrievalState.updateFilter(_filterDefinition, filterMetadata);
                }
            } catch (e:Error) {
            }
			dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, filterDefinition, null, this));
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