package com.easyinsight.filtering
{
	import com.easyinsight.analysis.AnalysisItem;

import flash.events.EventDispatcher;

[Bindable]
	[RemoteClass(alias="com.easyinsight.analysis.FilterDefinition")]
	public class FilterDefinition extends EventDispatcher
	{
		public static const VALUE:int = 1;
		public static const RANGE:int = 2;
		public static const DATE:int = 3;
		public static const ROLLING_DATE:int = 4;
		public static const LAST_VALUE:int = 5;
		public static const PATTERN:int = 6;
		public static const FIRST_VALUE:int = 7;
        public static const ORDERED:int = 8;
        public static const OR:int = 9;
        public static const NULL:int = 10;
        public static const NAMED_REF:int = 11;
        public static const FLAT_DATE:int = 12;
        public static const ANALYSIS_ITEM:int = 13;
        public static const MULTI_FLAT_DATE:int = 14;
        public static const MONTH_CUTOFF:int = 15;

		public var field:AnalysisItem;
		public var applyBeforeAggregation:Boolean = true;
        public var filterID:int;
        public var intrinsic:Boolean = false;
        public var enabled:Boolean = true;
        public var showOnReportView:Boolean = true;
    public var filterName:String;
    public var templateFilter:Boolean;
    public var toggleEnabled:Boolean;
    public var minimumRole:int = 4;
    public var marmotScript:String;
    public var trendFilter:Boolean;
    public var notCondition:Boolean;
    public var parentFilters:String;
		
		public function FilterDefinition()
			{
			super();
		}

    public function retrieveParentFilters():Array {
        if (parentFilters == null || parentFilters == "") {
            return [];
        }
        return parentFilters.split(",");
        //return [ parentFilters ];
    }

    public function matches(filterDefinition:FilterDefinition):Boolean {
        return field != null && filterDefinition.field != null && field.matches(filterDefinition.field) && getType() == filterDefinition.getType();
    }

    public static function getLabel(filterDefinition:FilterDefinition, field:AnalysisItem):String {
        if (filterDefinition == null) {
            if (field == null) {
                return "";
            } else {
                return field.display + ":";
            }
        }
        if (filterDefinition.filterName != null && filterDefinition.filterName != "") {
            return filterDefinition.filterName + ":";
        }
        if (filterDefinition.field != null) {
            return filterDefinition.field.display + ":";
        }
        return "";
    }

		public function getType():int {
			return 0;
		}

        public function updateFromSaved(savedItemFilter:FilterDefinition):void {
            filterID = savedItemFilter.filterID;
            if (field != null) {
                field.updateFromSaved(savedItemFilter.field);
            }
        }

        public function updateFromReportView(filter:FilterDefinition):void {
            field = filter.field;
        }

    public function qualifiedName():String {
        if (field != null) {
            return field.qualifiedName() + ":" + getType();
        } else {
            return filterName + ":" + String(getType());
        }
    }

    public function get displayName():String {
        return getLabel(this, field);
    }
}
}