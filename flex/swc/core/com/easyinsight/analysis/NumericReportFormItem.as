package com.easyinsight.analysis {
import flash.events.MouseEvent;

import mx.controls.TextInput;

public class NumericReportFormItem extends ReportFormItem {

    private var minValue:int;
    private var maxValue:int;
    private var textInput:TextInput;

    public function NumericReportFormItem(label:String, property:String, value:Object, report:Object,
            minValue:int, maxValue:int, enabledProperty:String = null) {
        super(label, property, value, report, enabledProperty);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    protected override function createChildren():void {
        super.createChildren();
        textInput = new TextInput();
        if (this.value != null) textInput.text = String(this.value);
        addChild(textInput);
    }

    override public function validate():Boolean {
        var num:Number = getValue() as Number;
        if (isNaN(num) || !isFinite(num)) {
            textInput.errorString = "Please enter a valid number.";
            textInput.setFocus();
            textInput.dispatchEvent(new MouseEvent(MouseEvent.MOUSE_OVER));
            return false;
        }
        if (num < minValue) {
            textInput.errorString = label + " must be greater than " + minValue + ".";
            textInput.setFocus();
            textInput.dispatchEvent(new MouseEvent(MouseEvent.MOUSE_OVER));
            return false;
        }
        if (num > maxValue) {
            textInput.errorString = label + " must be less than " + maxValue + ".";
            textInput.setFocus();
            textInput.dispatchEvent(new MouseEvent(MouseEvent.MOUSE_OVER));
            return false;
        }
        return true;
    }

    override protected function getValue():Object {
        return Number(textInput.text);
    }
}
}