package com.easyinsight.filtering
{
import com.easyinsight.analysis.AnalysisItem;

import flash.events.MouseEvent;
import flash.geom.Point;

import mx.collections.ArrayCollection;
import mx.containers.HBox;
import mx.controls.Button;
import mx.controls.ComboBox;
import mx.controls.Label;
import mx.events.DropdownEvent;
import mx.managers.PopUpManager;

public class RollingRangeFilter extends HBox implements IFilter
{
    private var rollingFilter:RollingDateRangeFilterDefinition;
    private var _feedID:int;
    private var _analysisItem:AnalysisItem;

    private var comboBox:ComboBox;
    private var deleteButton:Button;
    private var editButton:Button;
    private var _analysisItems:ArrayCollection;

    private var rangeOptions:ArrayCollection;

    [Bindable]
    [Embed(source="../../../../assets/navigate_cross.png")]
    public var deleteIcon:Class;

    [Bindable]
    [Embed(source="../../../../assets/pencil.png")]
    public var editIcon:Class;

    public function RollingRangeFilter(feedID:int, analysisItem:AnalysisItem)
    {
        super();
        this._feedID = feedID;
        this._analysisItem = analysisItem;
        rangeOptions = new ArrayCollection();
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
        rangeOptions.addItem(new RangeOption("Last Day of Data", RollingDateRangeFilterDefinition.LAST_DAY));
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
        window.detailClass = DateRangeDetailEditor;
        window.addEventListener(FilterEditEvent.FILTER_EDIT, onFilterEdit);
        window.analysisItems = _analysisItems;
        window.filterDefinition = rollingFilter;
        PopUpManager.addPopUp(window, this, true);
        window.x = 50;
        window.y = 50;
    }

    private function onFilterEdit(event:FilterEditEvent):void {
        dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, event.filterDefinition, event.previousFilterDefinition, this));
    }

    override protected function createChildren():void {
        super.createChildren();
        if (_showLabel) {
            var label:Label = new Label();
            label.text = _analysisItem.display + ":";
            addChild(label);
        } else {
            toolTip = _analysisItem.display;
        }
        if (comboBox == null) {
            comboBox = new ComboBox();
            comboBox.rowCount = 14;
            comboBox.dataProvider = rangeOptions;
            comboBox.addEventListener(DropdownEvent.CLOSE, filterValueChanged);

            if (rollingFilter == null) {
                comboBox.selectedIndex = 0;
            } else {
                for each (var rangeOption:RangeOption in rangeOptions) {
                    if (rangeOption.data == rollingFilter.interval) {
                        comboBox.selectedItem = rangeOption;
                    }
                }
            }
            if (rollingFilter == null) {
                rollingFilter = new RollingDateRangeFilterDefinition();
                rollingFilter.field = _analysisItem;
            }
            dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_ADDED, filterDefinition, null, this));
        }
        addChild(comboBox);
        if (_filterEditable) {
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
                if (rollingFilter.intrinsic) {
                    deleteButton.enabled = false;
                    deleteButton.toolTip = "This filter is an intrinsic part of the data source and cannot be deleted.";
                } else {
                    deleteButton.toolTip = "Delete";
                }
            }
            addChild(deleteButton);
        }
    }

    private function filterValueChanged(event:DropdownEvent):void {
        var newValue:int = event.currentTarget.selectedItem.data;
        var currentValue:int = rollingFilter.interval;
        if (newValue != currentValue) {
            rollingFilter.interval = newValue;
            dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, rollingFilter, null, this));
        }
    }

    override protected function commitProperties():void {
        super.commitProperties();
    }

    private function deleteSelf(event:MouseEvent):void {
        dispatchEvent(new FilterDeletionEvent(this));
    }

    private var _showLabel:Boolean;

    public function set showLabel(show:Boolean):void {
        _showLabel = show;
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