package com.easyinsight.filtering
{
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.IRetrievalState;
import com.easyinsight.skin.ImageConstants;

import flash.events.Event;
import flash.events.MouseEvent;


import mx.collections.ArrayCollection;
import mx.containers.Box;
import mx.containers.HBox;
import mx.controls.Button;
import mx.controls.CheckBox;
import mx.controls.ComboBox;
import mx.controls.Label;
import mx.controls.LinkButton;
import mx.core.UIComponent;

import mx.events.DropdownEvent;
import mx.managers.PopUpManager;
import mx.states.AddChild;
import mx.states.State;

public class RollingRangeFilter extends HBox implements IFilter
{
    private var rollingFilter:RollingDateRangeFilterDefinition;
    private var _feedID:int;
    private var _analysisItem:AnalysisItem;

    private var comboBox:ComboBox;
    private var deleteButton:Button;
    private var _analysisItems:ArrayCollection;

    private var rangeOptions:ArrayCollection;

    private var _retrievalState:IRetrievalState;

    private var filterMetadata:FilterMetadata;

    public function RollingRangeFilter(feedID:int, analysisItem:AnalysisItem, retrievalState:IRetrievalState, filterMetadata:FilterMetadata)
    {
        super();
        this._feedID = feedID;
        this._analysisItem = analysisItem;
        rangeOptions = new ArrayCollection();
        rangeOptions.addItem(new RangeOption("All", RollingDateRangeFilterDefinition.ALL));
        rangeOptions.addItem(new RangeOption("Last Day", RollingDateRangeFilterDefinition.DAY));
        rangeOptions.addItem(new RangeOption("Last 7 Days", RollingDateRangeFilterDefinition.WEEK));
        rangeOptions.addItem(new RangeOption("Last 30 Days", RollingDateRangeFilterDefinition.MONTH));
        rangeOptions.addItem(new RangeOption("Last 90 Days", RollingDateRangeFilterDefinition.QUARTER));
        rangeOptions.addItem(new RangeOption("Last 365 Days", RollingDateRangeFilterDefinition.YEAR));
        rangeOptions.addItem(new RangeOption("Today", RollingDateRangeFilterDefinition.DAY_TO_NOW));
        rangeOptions.addItem(new RangeOption("Week to Date", RollingDateRangeFilterDefinition.WEEK_TO_NOW));
        rangeOptions.addItem(new RangeOption("Month to Date", RollingDateRangeFilterDefinition.MONTH_TO_NOW));
        rangeOptions.addItem(new RangeOption("Quarter to Date", RollingDateRangeFilterDefinition.QUARTER_TO_NOW));
        rangeOptions.addItem(new RangeOption("Year to Date", RollingDateRangeFilterDefinition.YEAR_TO_NOW));
        rangeOptions.addItem(new RangeOption("Last Full Day", RollingDateRangeFilterDefinition.LAST_FULL_DAY));
        rangeOptions.addItem(new RangeOption("Last Full Week", RollingDateRangeFilterDefinition.LAST_FULL_WEEK));
        rangeOptions.addItem(new RangeOption("Last Full Month", RollingDateRangeFilterDefinition.LAST_FULL_MONTH));
        rangeOptions.addItem(new RangeOption("Last Full Quarter", RollingDateRangeFilterDefinition.LAST_FULL_QUARTER));
        rangeOptions.addItem(new RangeOption("Last Full Year", RollingDateRangeFilterDefinition.LAST_FULL_YEAR));
        rangeOptions.addItem(new RangeOption("Prior Full Week", RollingDateRangeFilterDefinition.PREVIOUS_FULL_WEEK));
        rangeOptions.addItem(new RangeOption("Prior Full Month", RollingDateRangeFilterDefinition.PREVIOUS_FULL_MONTH));
        rangeOptions.addItem(new RangeOption("Prior Full Quarter", RollingDateRangeFilterDefinition.PREVIOUS_FULL_QUARTER));
        rangeOptions.addItem(new RangeOption("Prior Full Year", RollingDateRangeFilterDefinition.PREVIOUS_FULL_YEAR));
        rangeOptions.addItem(new RangeOption("Last Week to Current Day", RollingDateRangeFilterDefinition.LAST_WEEK_TO_NOW));
        rangeOptions.addItem(new RangeOption("Last Month to Current Day", RollingDateRangeFilterDefinition.LAST_MONTH_TO_NOW));
        rangeOptions.addItem(new RangeOption("Last Quarter to Current Day", RollingDateRangeFilterDefinition.LAST_QUARTER_TO_NOW));
        rangeOptions.addItem(new RangeOption("Last Year to Current Day", RollingDateRangeFilterDefinition.LAST_YEAR_TO_NOW));
        rangeOptions.addItem(new RangeOption("Custom", RollingDateRangeFilterDefinition.CUSTOM));

        this.filterMetadata = filterMetadata;
        _retrievalState = retrievalState;
        setStyle("verticalAlign", "middle");
    }

    private var _loadingFromReport:Boolean = false;


    public function set loadingFromReport(value:Boolean):void {
        _loadingFromReport = value;
    }

    public function set filterDefinition(filterDefinition:FilterDefinition):void
    {
        this.rollingFilter = filterDefinition as RollingDateRangeFilterDefinition;
    }

    private var _filterEditable:Boolean = true;

    public function set filterEditable(editable:Boolean):void {
        _filterEditable = editable;
    }

    public function get filterDefinition():FilterDefinition
    {
        return this.rollingFilter;
    }

    public function set analysisItems(analysisItems:ArrayCollection):void
    {
        this._analysisItems = analysisItems;
    }

    private function edit(event:MouseEvent):void {
        var window:GeneralFilterEditSettings = new GeneralFilterEditSettings();
        window.filterMetadata = filterMetadata;
        window.detailClass = DateRangeDetailEditor;
        window.addEventListener(FilterEditEvent.FILTER_EDIT, onFilterEdit, false, 0, true);
        window.analysisItems = _analysisItems;
        window.filterDefinition = rollingFilter;
        PopUpManager.addPopUp(window, this, true);
        window.x = 50;
        window.y = 50;
    }

    private function onChange(event:Event):void {
        var checkbox:CheckBox = event.currentTarget as CheckBox;
        rollingFilter.enabled = checkbox.selected;
        dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, rollingFilter, null, this));
    }

    private function onFilterEdit(event:FilterEditEvent):void {
        if (event.filterDefinition is RollingDateRangeFilterDefinition) {
            var selected:Object = comboBox.selectedItem;
            var options:Object = new Object();
            for each (var rangeOption:RangeOption in rangeOptions) {
                if (rangeOption.data > RollingDateRangeFilterDefinition.ALL) {
                    options[rangeOption.data] = rangeOption;
                }
            }
            for each (rangeOption in rangeOptions) {
                if (rangeOption.data > RollingDateRangeFilterDefinition.ALL) {
                    rangeOptions.removeItemAt(rangeOptions.getItemIndex(rangeOption));
                }
            }
            for each (var interval:CustomRollingInterval in RollingDateRangeFilterDefinition(event.filterDefinition).intervals) {
                var existing:RangeOption = options[interval.intervalNumber];
                if (existing == null || existing.label != interval.filterLabel) {
                    rangeOptions.addItem(new RangeOption(interval.filterLabel, interval.intervalNumber));
                } else {
                    rangeOptions.addItem(existing);
                }
            }
            comboBox.selectedItem = selected;
        }
        if (filterLabel is LinkButton) {
            LinkButton(filterLabel).label = FilterDefinition.getLabel(rollingFilter, _analysisItem);
        }
        dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, event.filterDefinition, event.previousFilterDefinition, this));
    }

    private var filterLabel:UIComponent;

    override protected function createChildren():void {
        super.createChildren();
        if (rollingFilter != null && !rollingFilter.toggleEnabled) {
            var checkbox:CheckBox = new CheckBox();
            checkbox.selected = rollingFilter == null ? true : rollingFilter.enabled;
            checkbox.toolTip = "Click to disable this filter.";
            checkbox.addEventListener(Event.CHANGE, onChange);
            addChild(checkbox);
            /*if (rollingFilter != null && (rollingFilter.intrinsic || rollingFilter.trendFilter)) {
             checkbox.enabled = false;
             checkbox.toolTip = "This filter is an intrinsic part of the data source and cannot be disabled.";
             }*/

        }

        if (rollingFilter != null) {
            for each (var interval:CustomRollingInterval in rollingFilter.intervals) {
                rangeOptions.addItem(new RangeOption(interval.filterLabel, interval.intervalNumber));
            }
        }

        if (_filterEditable) {
            var lb:LinkButton = new LinkButton();
            lb.styleName = "filterLabel";
            lb.label = FilterDefinition.getLabel(rollingFilter, _analysisItem);
            lb.addEventListener(MouseEvent.CLICK, edit);
            filterLabel = lb;
        } else {
            var label:Label = new Label();
            label.styleName = "filterLabel";
            label.text = FilterDefinition.getLabel(rollingFilter, _analysisItem);
            filterLabel = label;
        }
        addChild(filterLabel);

        if (comboBox == null) {
            var newFilter:Boolean = false;
            if (rollingFilter == null) {
                newFilter = true;
                rollingFilter = new RollingDateRangeFilterDefinition();
                rollingFilter.field = _analysisItem;
                rollingFilter.toggleEnabled = true;
            }
            if (rollingFilter.intrinsic || rollingFilter.trendFilter) {
                rangeOptions.removeItemAt(0);
            }
            comboBox = new ComboBox();
            comboBox.rowCount = 16 + ((rollingFilter.intrinsic || rollingFilter.trendFilter) ? 0 : 1);
            comboBox.dataProvider = rangeOptions;
            comboBox.addEventListener(DropdownEvent.CLOSE, filterValueChanged);

            if (newFilter) {
                comboBox.selectedIndex = 0;
            } else {
                for each (var rangeOption:RangeOption in rangeOptions) {
                    if (rangeOption.data == rollingFilter.interval) {
                        comboBox.selectedItem = rangeOption;
                    }
                }
            }

            if (_loadingFromReport) {
                _loadingFromReport = false;
            } else {
                dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_ADDED, filterDefinition, null, this));
            }
        }
        addChild(comboBox);
        var customState:State = new State();
        customState.name = "Custom";
        var customRollingFilter:CustomRollingFilter = new CustomRollingFilter();
        customRollingFilter.filter = rollingFilter;
        customRollingFilter.addEventListener("customRollingFilterEvent", customFilter);
        var targetBox:Box = new Box();
        addChild(targetBox);
        var addOp:AddChild = new AddChild(targetBox, customRollingFilter);
        customState.overrides = [ addOp ];
        this.states = [ customState ];
        if (rollingFilter.interval == RollingDateRangeFilterDefinition.CUSTOM) {
            currentState = "Custom";
        }
        if (_filterEditable) {
            if (deleteButton == null) {
                deleteButton = new Button();
                deleteButton.addEventListener(MouseEvent.CLICK, deleteSelf);
                deleteButton.setStyle("icon", ImageConstants.DELETE_ICON);
                /*if (rollingFilter.intrinsic) {
                 deleteButton.enabled = false;
                 deleteButton.toolTip = "This filter is an intrinsic part of the data source and cannot be deleted.";
                 } else {*/
                deleteButton.toolTip = "Delete";
                //}
            }
            addChild(deleteButton);
        }
    }

    private function customFilter(event:Event):void {
        dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, rollingFilter, null, this));
    }

    private function filterValueChanged(event:DropdownEvent):void {
        var newValue:int = event.currentTarget.selectedItem.data;
        if (newValue == RollingDateRangeFilterDefinition.CUSTOM) {
            currentState = "Custom";
        } else {
            currentState = "";
        }
        var currentValue:int = rollingFilter.interval;
        if (newValue != currentValue) {
            rollingFilter.interval = newValue;
            try {
                if (_retrievalState != null) {
                    _retrievalState.updateFilter(rollingFilter, filterMetadata);
                }
            } catch (e:Error) {
            }
            dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, rollingFilter, null, this));
        }
    }

    override protected function commitProperties():void {
        super.commitProperties();
    }

    private function deleteSelf(event:MouseEvent):void {
        dispatchEvent(new FilterDeletionEvent(this));
    }
}


}

class RangeOption {
    public var label:String;
    public var data:int;

    function RangeOption(label:String, data:int) {
        this.label = label;
        this.data = data;
    }
}