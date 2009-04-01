package com.easyinsight.analysis {
[Event(name="returnData", type="com.easyinsight.analysis.DataServiceEvent")]
public interface IEmbeddedDataService {
    function retrieveData(reportID:int):void;
    function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0,
                useWeakReference:Boolean = false):void;
}
}