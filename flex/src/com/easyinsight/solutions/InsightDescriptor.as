package com.easyinsight.solutions {
import com.easyinsight.quicksearch.EIDescriptor;
[Bindable]
[RemoteClass(alias="com.easyinsight.core.InsightDescriptor")]
public class InsightDescriptor extends EIDescriptor {
    public var dataFeedID:int;

    public function InsightDescriptor() {
        
    }

    override public function getType():int {
        return EIDescriptor.REPORT;
    }
}
}