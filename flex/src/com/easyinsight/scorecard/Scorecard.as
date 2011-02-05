package com.easyinsight.scorecard {
import flash.events.Event;
import flash.events.EventDispatcher;

import mx.collections.ArrayCollection;

[Bindable]
[RemoteClass(alias="com.easyinsight.scorecard.Scorecard")]
public class Scorecard extends EventDispatcher {

    public var kpis:ArrayCollection = new ArrayCollection();
    public var name:String;
    public var scorecardID:int;
    public var urlKey:String;
    public var description:String;
    public var accountVisible:Boolean;
    public var exchangeVisible:Boolean;
    private var _configuring:Boolean;

    public function Scorecard() {
    }


    [Bindable(event="configuringChanged")]
    public function get configuring():Boolean {
        return _configuring;
    }

    public function set configuring(value:Boolean):void {
        if (_configuring == value) return;
        _configuring = value;
        dispatchEvent(new Event("configuringChanged"));
    }
}
}