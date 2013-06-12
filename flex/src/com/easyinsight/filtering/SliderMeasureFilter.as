package com.easyinsight.filtering
{
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.formatter.FormattingConfiguration;
import com.easyinsight.skin.ImageConstants;

import flash.events.Event;
import flash.events.MouseEvent;

import mx.binding.utils.BindingUtils;
import mx.collections.ArrayCollection;
import mx.containers.HBox;
import mx.controls.Alert;
import mx.controls.Button;
import mx.controls.CheckBox;
import mx.controls.HSlider;
import mx.controls.Label;
import mx.controls.LinkButton;
import mx.controls.Text;
import mx.controls.TextInput;
import mx.core.UIComponent;
import mx.events.SliderEvent;
import mx.formatters.Formatter;
import mx.managers.PopUpManager;
import mx.states.AddChild;
import mx.states.RemoveChild;
import mx.states.State;

public class SliderMeasureFilter extends HBox implements IFilter
{

    public static const OPERATOR_STRINGS:Object = {1: "<", 2: "<=" }
    private var hslider:HSlider;
    private var _filterDefinition:FilterRangeDefinition;
    private var analysisItem:AnalysisItem;
    private var lowValue:int;
    private var highValue:int;

    private var _lowValueString:String;
    private var _highValueString:String;

    private var _lowOperatorString:String;
    private var _highOperatorString:String;

    [Bindable(event="lowOperatorStringChanged")]
    public function set lowOperatorString(value:String):void {
        if(_lowOperatorString == value) return;
        _lowOperatorString = value;
        dispatchEvent(new Event("lowOperatorStringChanged"));
    }

    public function get lowOperatorString():String {
        return _lowOperatorString;
    }

    [Bindable(event="highOperatorStringChanged")]
    public function set highOperatorString(value:String):void {
        if(_highOperatorString == value) return;
        _highOperatorString = value;
        dispatchEvent(new Event("highOperatorStringChanged"));
    }

    public function get highOperatorString():String {
        return _highOperatorString;
    }

    private var lowInput:Label;
    private var highInput:Label;
    private var lowOperator:Label;
    private var highOperator:Label;

    private var _analysisItems:ArrayCollection;

    public function SliderMeasureFilter(feedID:int, analysisItem:AnalysisItem) {
        super();
        this.analysisItem = analysisItem;
    }

    private var _loadingFromReport:Boolean = false;


    public function set loadingFromReport(value:Boolean):void {
        _loadingFromReport = value;
    }

    [Bindable(event="lowValueStringChanged")]
    public function get lowValueString():String {
        return _lowValueString;
    }

    public function set lowValueString(value:String):void {
        if (_lowValueString == value) return;
        _lowValueString = value;
        dispatchEvent(new Event("lowValueStringChanged"));
    }

    [Bindable(event="highValueStringChanged")]
    public function get highValueString():String {
        return _highValueString;
    }

    public function set highValueString(value:String):void {
        if (_highValueString == value) return;
        _highValueString = value;
        dispatchEvent(new Event("highValueStringChanged"));
    }

    public function set analysisItems(analysisItems:ArrayCollection):void {
        _analysisItems = analysisItems;
    }

    public function edit(event:MouseEvent):void {
        var window:GeneralFilterEditSettings = new GeneralFilterEditSettings();
        window.detailClass = MeasureFilterEditor;
        window.addEventListener(FilterEditEvent.FILTER_EDIT, onFilterEdit, false, 0, true);
        window.analysisItems = _analysisItems;
        window.filterDefinition = _filterDefinition;
        PopUpManager.addPopUp(window, this, true);
        window.x = 50;
        window.y = 50;
    }

    private function onFilterEdit(event:FilterEditEvent):void {
        if (initLabel != null && initLabel is LinkButton) {
            LinkButton(initLabel).label = FilterDefinition.getLabel(event.filterDefinition, analysisItem);
        }
        if (altInitLabel != null) {
            altInitLabel.label = FilterDefinition.getLabel(event.filterDefinition, analysisItem);
        }
        var measureFilter:FilterRangeDefinition = event.filterDefinition as FilterRangeDefinition;
        if (measureFilter.startValueDefined) {
            var lowString:String;
            if (measureFilter.field.formattingConfiguration.formattingType == FormattingConfiguration.MILLISECONDS) {
                var hours:int = _filterDefinition.startValue / 60 / 60 / 1000;
                if (hours >= 24) {
                    lowString = String(hours / 24) + " days";
                } else {
                    lowString = String(hours) + " hours";
                }
            } else {
                lowString =  _filterDefinition.field.getFormatter().format(_filterDefinition.startValue);
            }
            lowValueString = lowString;
        }
        if (measureFilter.endValueDefined) {
            var highString:String;
            if (measureFilter.field.formattingConfiguration.formattingType == FormattingConfiguration.MILLISECONDS) {
                var highHours:int = _filterDefinition.endValue / 60 / 60 / 1000;
                if (highHours >= 24) {
                    highString = String(highHours / 24) + " days";
                } else {
                    highString = String(highHours) + " hours";
                }
            } else {
                highString =  _filterDefinition.field.getFormatter().format(_filterDefinition.endValue);
            }
            highValueString = highString;
        }

        if (measureFilter.startValueDefined || measureFilter.endValueDefined) {
            currentState = "Configured";
        } else {
            currentState = "";
        }
        if(measureFilter.lowerOperator > 0 && measureFilter.upperOperator > 0) {
            lowOperatorString = OPERATOR_STRINGS[measureFilter.lowerOperator];
            highOperatorString = OPERATOR_STRINGS[measureFilter.upperOperator];
        }
        dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, event.filterDefinition, event.previousFilterDefinition, this));
    }

    private function onChange(event:Event):void {
        var checkbox:CheckBox = event.currentTarget as CheckBox;
        _filterDefinition.enabled = checkbox.selected;
        dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, _filterDefinition, null, this));
    }

    private var initLabel:UIComponent;
    private var altInitLabel:LinkButton;

    private var leftInput:TextInput;
    private var rightInput:TextInput;

    private function onInputChange(event:Event):void {
        if (leftInput != null) {
            _filterDefinition.startValue = Number(leftInput.text);
        }
        if (rightInput != null) {
            _filterDefinition.endValue = Number(rightInput.text);
        }
        dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, _filterDefinition, null, this));
    }

    override protected function createChildren():void {
        super.createChildren();
        if (lowInput == null) {


            if (_filterDefinition == null || !_filterDefinition.toggleEnabled) {
                var checkbox:CheckBox = new CheckBox();
                checkbox.selected = _filterDefinition == null ? true : _filterDefinition.enabled;
                checkbox.toolTip = "Click to disable this filter.";
                checkbox.addEventListener(Event.CHANGE, onChange);
                addChild(checkbox);
            }




            if (_filterEditable) {
                initLabel = new LinkButton();
                initLabel.addEventListener(MouseEvent.CLICK, edit);
                initLabel.styleName = "filterLabel";
                LinkButton(initLabel).label = FilterDefinition.getLabel(_filterDefinition, analysisItem);

                altInitLabel = new LinkButton();
                altInitLabel.addEventListener(MouseEvent.CLICK, edit);
                altInitLabel.styleName = "filterLabel";
                LinkButton(altInitLabel).label = FilterDefinition.getLabel(_filterDefinition, analysisItem);
                //LinkButton(altInitLabel).label = "XYZ";
                var haveDataState:State = new State();
                haveDataState.name = "Configured";
                var defaultBox:HBox = new HBox();
                var removeOp:RemoveChild = new RemoveChild();
                removeOp.target = defaultBox;
                var addChildOp:AddChild = new AddChild();
                haveDataState.overrides = [ removeOp, addChildOp ];
                var box:HBox = new HBox();
                addChildOp.target = box;

                lowInput = new Label();
                BindingUtils.bindProperty(lowInput, "text", this, "lowValueString");


                var between:Text = new Text();

                highOperator = new Label();
                BindingUtils.bindProperty(highOperator, "text", this, "highOperatorString");
                lowOperator = new Label();
                BindingUtils.bindProperty(lowOperator, "text", this, "lowOperatorString");

                between.text = " " + analysisItem.display + " ";


                highInput = new Label();
                BindingUtils.bindProperty(highInput, "text", this, "highValueString");
                box.addChild(lowInput);
                box.addChild(lowOperator);
                box.addChild(altInitLabel);
                box.addChild(highOperator);
                box.addChild(highInput);

                var deleteButton:Button = new Button();
                deleteButton.addEventListener(MouseEvent.CLICK, deleteSelf);
                deleteButton.setStyle("icon", ImageConstants.DELETE_ICON);
                deleteButton.toolTip = "Delete";
                box.addChild(deleteButton);


                this.states = [ haveDataState ];




                defaultBox.addChild(initLabel);

                var deleteDefault:Button = new Button();
                deleteDefault.addEventListener(MouseEvent.CLICK, deleteSelf);
                deleteDefault.setStyle("icon", ImageConstants.DELETE_ICON);
                deleteDefault.toolTip = "Delete";
                defaultBox.addChild(deleteDefault);

                addChild(defaultBox);
            } else {
                initLabel = new Label();

                initLabel.styleName = "filterLabel";
                /*if (_filterDefinition.startValueDefined && !_filterDefinition.endValueDefined) {
                    // E@sy mone$1
                    Label(initLabel).text = FilterDefinition.getLabelWithEnd(_filterDefinition, analysisItem, " " + OPERATOR_STRINGS[_filterDefinition.lowerOperator]);
                    // Bra1n is marching
                } else if (!_filterDefinition.startValueDefined && _filterDefinition.endValueDefined) {
                    Label(initLabel).text = FilterDefinition.getLabelWithEnd(_filterDefinition, analysisItem, " " + OPERATOR_STRINGS[_filterDefinition.upperOperator]);
                } else {*/
                    Label(initLabel).text = FilterDefinition.getLabel(_filterDefinition, analysisItem);
                //}
                addChild(initLabel);
                setStyle("verticalAlign", "middle");
                var f:Formatter = _filterDefinition.field.getFormatter();
                if (_filterDefinition.startValueDefined) {
                    var leftLabel:TextInput = new TextInput();
                    leftInput = leftLabel;
                    leftInput.addEventListener(Event.CHANGE, onInputChange);
                    leftLabel.text = f.format(_filterDefinition.startValue);
                    addChild(leftLabel);
                }
                if (_filterDefinition.showSlider) {
                    var slider:HSlider;
                    if (_filterDefinition.startValueDefined && _filterDefinition.endValueDefined) {
                        slider = createDoubleSlider();
                    } else if (_filterDefinition.startValueDefined) {
                        slider = createMinSlider();
                    } else if (_filterDefinition.endValueDefined) {
                        slider = createMaxSlider();
                    }
                    if (slider != null) {
                        addChild(slider);
                    }
                }
                if (_filterDefinition.endValueDefined) {
                    var rightLabel:TextInput = new TextInput();
                    rightInput = rightLabel;
                    rightInput.addEventListener(Event.CHANGE, onInputChange);
                    rightLabel.text = f.format(_filterDefinition.endValue);
                    addChild(rightLabel);    
                }
            }
        }


        if (_filterDefinition == null) {
            _filterDefinition = new FilterRangeDefinition();
            _filterDefinition.startValueDefined = false;
            _filterDefinition.endValueDefined = false;
            _filterDefinition.field = analysisItem;
            _filterDefinition.lowerOperator = 1;
            _filterDefinition.upperOperator = 1;

        } else {
            if (_filterDefinition.startValueDefined) {
                var lowString:String;
                if (_filterDefinition.field.formattingConfiguration.formattingType == FormattingConfiguration.MILLISECONDS) {
                    var hours:int = _filterDefinition.startValue / 60 / 60 / 1000;
                    if (hours >= 24) {
                        lowString = String(hours / 24) + " days";
                    } else {
                        lowString = String(hours) + " hours";
                    }
                } else {
                    lowString =  _filterDefinition.field.getFormatter().format(_filterDefinition.startValue);
                }
                lowValueString = lowString;
            }
            if (_filterDefinition.endValueDefined) {
                var highString:String;
                if (_filterDefinition.field.formattingConfiguration.formattingType == FormattingConfiguration.MILLISECONDS) {
                    var highHours:int = _filterDefinition.endValue / 60 / 60 / 1000;
                    if (highHours >= 24) {
                        highString = String(highHours / 24) + " days";
                    } else {
                        highString = String(highHours) + " hours";
                    }
                } else {
                    highString =  _filterDefinition.field.getFormatter().format(_filterDefinition.endValue);
                }
                highValueString = highString;
            }
            lowOperatorString = OPERATOR_STRINGS[_filterDefinition.lowerOperator];
            highOperatorString = OPERATOR_STRINGS[_filterDefinition.upperOperator];
            if (_filterEditable) {
                currentState = "Configured";
            }

        }

        if (_loadingFromReport) {
            _loadingFromReport = false;

        } else {
            dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_ADDED, filterDefinition, null, this));
        }
    }

    private function createDoubleSlider():HSlider {
        var slider:HSlider = new HSlider();
        slider.dataTipFormatFunction = dataTipFormatter;
        slider.minimum = _filterDefinition.startValue;
        slider.maximum = _filterDefinition.endValue;
        //slider.labels = [ _filterDefinition.startValue, _filterDefinition.endValue ];
        slider.showDataTip = true;
        slider.thumbCount = 2;
        slider.liveDragging = false;
        slider.values = [ _filterDefinition.startValue, _filterDefinition.endValue ];
        slider.addEventListener(SliderEvent.THUMB_RELEASE, onRelease);
        slider.setStyle("bottom", 0);
        slider.setStyle("dataTipPlacement", "top");
        return slider;
    }

    private function createMinSlider():HSlider {
        var slider:HSlider = new HSlider();
        slider.dataTipFormatFunction = dataTipFormatter;
        slider.minimum = _filterDefinition.startValue;
        slider.maximum = _filterDefinition.endValue;
        //slider.labels = [ _filterDefinition.startValue, _filterDefinition.endValue ];
        slider.showDataTip = true;
        slider.thumbCount = 1;
        slider.liveDragging = false;
        slider.value = _filterDefinition.startValue;
        slider.addEventListener(SliderEvent.THUMB_RELEASE, onRelease);
        slider.setStyle("bottom", 0);
        slider.setStyle("dataTipPlacement", "top");
        return slider;
    }

    private function createMaxSlider():HSlider {
        var slider:HSlider = new HSlider();
        slider.dataTipFormatFunction = dataTipFormatter;
        slider.minimum = _filterDefinition.startValue;
        slider.maximum = _filterDefinition.endValue;
        //slider.labels = [ _filterDefinition.startValue, _filterDefinition.endValue ];
        slider.showDataTip = true;
        slider.thumbCount = 1;
        slider.liveDragging = false;
        slider.value = _filterDefinition.endValue;
        slider.addEventListener(SliderEvent.THUMB_RELEASE, onRelease);
        slider.setStyle("bottom", 0);
        slider.setStyle("dataTipPlacement", "top");
        return slider;
    }

    private function dataTipFormatter(value:Number):String {
        var f:Formatter = _filterDefinition.field.getFormatter();
        return f.format(value);

    }

    private function onRelease(event:SliderEvent):void {
        var slider:HSlider = event.currentTarget as HSlider;
        if (_filterDefinition.startValueDefined && _filterDefinition.endValueDefined) {
            _filterDefinition.currentStartValue = slider.values[0];
            _filterDefinition.currentStartValueDefined = true;
            _filterDefinition.currentEndValue = slider.values[1];
            _filterDefinition.currentEndValueDefined = true;
        } else if (_filterDefinition.startValueDefined) {
            _filterDefinition.currentStartValue = slider.value;
            _filterDefinition.currentStartValueDefined = true;
        } else if (_filterDefinition.endValueDefined) {
            _filterDefinition.currentEndValue = slider.value;
            _filterDefinition.currentEndValueDefined = true;
        }

        dispatchEvent(new FilterUpdatedEvent(FilterUpdatedEvent.FILTER_UPDATED, _filterDefinition, null, this));
    }

    public function set filterDefinition(filterDefinition:FilterDefinition):void {
        this._filterDefinition = filterDefinition as FilterRangeDefinition;
    }

    private var _filterEditable:Boolean = true;

    public function set filterEditable(editable:Boolean):void {
        _filterEditable = editable;
    }

    public function get filterDefinition():FilterDefinition {
        return this._filterDefinition;
    }

    private function deleteSelf(event:MouseEvent):void {
        dispatchEvent(new FilterDeletionEvent(this));
    }
}
}