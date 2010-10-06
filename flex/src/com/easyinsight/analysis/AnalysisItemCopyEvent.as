package com.easyinsight.analysis {
import flash.events.Event;

public class AnalysisItemCopyEvent extends Event {

    public static const ITEM_COPY:String = "itemCopy";
    public static const ITEM_EDIT:String = "itemEdit";

    public var analysisItem:AnalysisItem;
    public var wrapper:AnalysisItemWrapper;

    public function AnalysisItemCopyEvent(type:String, analysisItem:AnalysisItem, wrapper:AnalysisItemWrapper = null) {
        super(type, true);
        this.analysisItem = analysisItem;
        this.wrapper = wrapper;
    }

    override public function clone():Event {
        return new AnalysisItemCopyEvent(type, analysisItem, wrapper);
    }
}
}