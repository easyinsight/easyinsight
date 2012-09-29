package com.easyinsight.kpi {
import com.easyinsight.util.CancelButton;
import com.easyinsight.util.SaveButton;

import flash.events.Event;
import flash.events.MouseEvent;

import mx.binding.utils.BindingUtils;
import mx.containers.Canvas;
import mx.containers.HBox;
import mx.controls.Button;
import mx.controls.LinkButton;

[Event(name="kpiTutorialNext", type="com.easyinsight.kpi.KPITutorialEvent")]
[Event(name="kpiTutorialPrevious", type="com.easyinsight.kpi.KPITutorialEvent")]
[Event(name="kpiTutorialCancel", type="com.easyinsight.kpi.KPITutorialEvent")]
[Event(name="kpiTutorialFinish", type="com.easyinsight.kpi.KPITutorialEvent")]
[Event(name="expertMode", type="com.easyinsight.kpi.KPIModeEvent")]
public class TutorialButtons extends Canvas {
    public function TutorialButtons() {
        super();
        percentWidth = 100;
        setStyle("backgroundColor", 0xFFFFFF);
        setStyle("horizontalAlign", "center");

    }

    private var _backEnabled:Boolean;
    private var _nextEnabled:Boolean;
    private var _finishEnabled:Boolean;
    private var _backVisible:Boolean = true;
    private var _nextVisible:Boolean = true;
    private var _cancelVisible:Boolean = true;
    private var _finishVisible:Boolean = true;


    [Bindable(event="cancelVisibleChanged")]
    public function get cancelVisible():Boolean {
        return _cancelVisible;
    }

    public function set cancelVisible(value:Boolean):void {
        if (_cancelVisible == value) return;
        _cancelVisible = value;
        dispatchEvent(new Event("cancelVisibleChanged"));
    }

    [Bindable(event="finishVisibleChanged")]
    public function get finishVisible():Boolean {
        return _finishVisible;
    }

    public function set finishVisible(value:Boolean):void {
        if (_finishVisible == value) return;
        _finishVisible = value;
        dispatchEvent(new Event("finishVisibleChanged"));
    }

    [Bindable(event="backVisibleChanged")]
    public function get backVisible():Boolean {
        return _backVisible;
    }

    public function set backVisible(value:Boolean):void {
        if (_backVisible == value) return;
        _backVisible = value;
        dispatchEvent(new Event("backVisibleChanged"));
    }

    [Bindable(event="nextVisibleChanged")]
    public function get nextVisible():Boolean {
        return _nextVisible;
    }

    public function set nextVisible(value:Boolean):void {
        if (_nextVisible == value) return;
        _nextVisible = value;
        dispatchEvent(new Event("nextVisibleChanged"));
    }

    private var _tutorialPanel:ITutorialPanel;

    public function set tutorialPanel(value:ITutorialPanel):void {
        _tutorialPanel = value;
    }

    private function back(event:MouseEvent):void {
        dispatchEvent(new KPITutorialEvent(KPITutorialEvent.PREVIOUS));
    }

    private function next(event:MouseEvent):void {
        if (_tutorialPanel.validate()) {
            _tutorialPanel.saveValues();
            dispatchEvent(new KPITutorialEvent(KPITutorialEvent.NEXT));
        }
    }

    private function finish(event:MouseEvent):void {
        if (_tutorialPanel.validate()) {
            _tutorialPanel.saveValues();
            dispatchEvent(new KPITutorialEvent(KPITutorialEvent.FINISH));
        }
    }

    private function cancel(event:MouseEvent):void {
        dispatchEvent(new KPITutorialEvent(KPITutorialEvent.CANCEL));
    }

    protected override function createChildren():void {
        super.createChildren();
        /*var expertBox:Box = new Box();
        var expertButton:Button = new Button();
        expertButton.label = "Expert Mode";
        expertButton.setStyle("fontSize", 14);
        expertButton.addEventListener(MouseEvent.CLICK, expertMode);
        expertBox.setStyle("paddingBottom", 8);
        expertBox.setStyle("paddingLeft", 8);
        expertBox.addChild(expertButton);
        BindingUtils.bindProperty(expertButton, "visible", this, "showExpertMode");
        addChild(expertBox);*/
        var hbox:HBox = new HBox();
        hbox.percentWidth = 100;
        hbox.setStyle("horizontalAlign", "center");
        var backButton:Button = new SaveButton();
        backButton.label = "Back";
        backButton.setStyle("fontSize", 14);
        backButton.addEventListener(MouseEvent.CLICK, back);
        BindingUtils.bindProperty(backButton, "enabled", this, "backEnabled");
        BindingUtils.bindProperty(backButton, "visible", this, "backVisible");
        hbox.addChild(backButton);
        var nextButton:Button = new SaveButton();
        nextButton.label = "Next";
        nextButton.setStyle("fontSize", 14);
        BindingUtils.bindProperty(nextButton, "enabled", this, "nextEnabled");
        BindingUtils.bindProperty(nextButton, "visible", this, "nextVisible");
        nextButton.addEventListener(MouseEvent.CLICK, next);
        hbox.addChild(nextButton);
        var finishButton:Button = new SaveButton();
        finishButton.label = "Finish";
        finishButton.setStyle("fontSize", 14);
        BindingUtils.bindProperty(finishButton, "enabled", this, "finishEnabled");
        BindingUtils.bindProperty(finishButton, "visible", this, "finishVisible");
        finishButton.addEventListener(MouseEvent.CLICK, finish);
        hbox.addChild(finishButton);
        var cancelButton:LinkButton = new CancelButton();
        cancelButton.label = "Cancel";
        cancelButton.setStyle("fontSize", 14);
        cancelButton.addEventListener(MouseEvent.CLICK, cancel);
        BindingUtils.bindProperty(cancelButton, "visible", this, "cancelVisible");
        hbox.addChild(cancelButton);
        hbox.setStyle("paddingBottom", 8);
        addChild(hbox);
    }

    [Bindable(event="backEnabledChanged")]
    public function get backEnabled():Boolean {
        return _backEnabled;
    }

    public function set backEnabled(value:Boolean):void {
        if (_backEnabled == value) return;
        _backEnabled = value;
        dispatchEvent(new Event("backEnabledChanged"));
    }

    [Bindable(event="nextEnabledChanged")]
    public function get nextEnabled():Boolean {
        return _nextEnabled;
    }

    public function set nextEnabled(value:Boolean):void {
        if (_nextEnabled == value) return;
        _nextEnabled = value;
        dispatchEvent(new Event("nextEnabledChanged"));
    }

    [Bindable(event="finishEnabledChanged")]
    public function get finishEnabled():Boolean {
        return _finishEnabled;
    }

    public function set finishEnabled(value:Boolean):void {
        if (_finishEnabled == value) return;
        _finishEnabled = value;
        dispatchEvent(new Event("finishEnabledChanged"));
    }
}
}