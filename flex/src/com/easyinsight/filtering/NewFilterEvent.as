/**
 * Created by ${PRODUCT_NAME}.
 * User: jamesboe
 * Date: 2/17/11
 * Time: 10:23 AM
 * To change this template use File | Settings | File Templates.
 */
package com.easyinsight.filtering {
import com.easyinsight.analysis.AnalysisItem;

import flash.events.Event;

public class NewFilterEvent extends Event {

    public static const NEW_FILTER:String = "newFilter";

    public static const FIELD_FILTER:int = 1;
    public static const OR_FILTER:int = 2;

    public var analysisItem:AnalysisItem;

    public var filterType:int;

    public function NewFilterEvent(filterType:int, analysisItem:AnalysisItem = null) {
        super(NEW_FILTER);
        this.filterType = filterType;
        this.analysisItem = analysisItem;
    }

    override public function clone():Event {
        return new NewFilterEvent(filterType, analysisItem);
    }
}
}
