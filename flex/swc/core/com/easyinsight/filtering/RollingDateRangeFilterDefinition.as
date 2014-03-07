package com.easyinsight.filtering
{
import com.easyinsight.filtering.CustomRollingInterval;

import mx.collections.ArrayCollection;
import mx.utils.ObjectUtil;

[Bindable]
	[RemoteClass(alias="com.easyinsight.analysis.RollingFilterDefinition")]
	public class RollingDateRangeFilterDefinition extends FilterDefinition
	{
		public static const DAY:int = 1;
		public static const WEEK:int = 2;
		public static const MONTH:int = 3;
		public static const QUARTER:int = 4;
		public static const YEAR:int = 5;
		public static const DAY_TO_NOW:int = 6;
		public static const WEEK_TO_NOW:int = 7;
		public static const MONTH_TO_NOW:int = 8;
		public static const QUARTER_TO_NOW:int = 9;
		public static const YEAR_TO_NOW:int = 10;        
		public static const LAST_DAY:int = 11;        
		public static const LAST_FULL_DAY:int = 12;
		public static const LAST_FULL_WEEK:int = 13;
		public static const LAST_FULL_MONTH:int = 14;        
		public static const CUSTOM:int = 18;
		public static const ALL:int = 19;
		public static const LAST_FULL_QUARTER:int = -1;
		public static const LAST_FULL_YEAR:int = -2;
		public static const LAST_MONTH_TO_NOW:int = -3;
		public static const PREVIOUS_FULL_MONTH:int = -4;
		public static const LAST_WEEK_TO_NOW:int = -5;
		public static const PREVIOUS_FULL_WEEK:int = -6;
		public static const LAST_QUARTER_TO_NOW:int = -7;
		public static const PREVIOUS_FULL_QUARTER:int = -8;
		public static const PREVIOUS_FULL_YEAR:int = -9;
		public static const LAST_YEAR_TO_NOW:int = -10;

    /*
     intervals.addItem(createInterval("Last Full Quarter", "dayofmonth(nowdate() - quarters(1), 1)", "dayofmonth(nowdate() - quarters(1), daysinquarter(nowdate() - quarters(1))))"));
     intervals.addItem(createInterval("Previous Full Year", "nowdate() - years(1)", "dayofyear(nowdate() - years(1), daysinyear(nowdate() - years(1)))"));
     intervals.addItem(createInterval("Last Month to Now", "dayofmonth(nowdate() - months(1), 1)", "dayofmonth(nowdate() - months(1), dayofmonth(nowdate()))"));
     intervals.addItem(createInterval("Previous Full Month", "dayofmonth(nowdate() - months(2), 1)", "dayofmonth(nowdate() - months(2), daysinmonth(nowdate() - months(2))))"));
     intervals.addItem(createInterval("Last Week to Now", "dayofweek(nowdate() - weeks(1), 1)", "dayofweek(nowdate() - weeks(1), dayofweek(nowdate()))"));
     intervals.addItem(createInterval("Previous Full Week", "dayofweek(nowdate() - weeks(2), 1)", "dayofmonth(nowdate() - weeks(2), 7))"));
     intervals.addItem(createInterval("Last Quarter to Now", "dayofquarter(nowdate() - quarters(1), 1)", "dayofquarter(nowdate() - quarters(1), dayofquarter(nowdate()))"));
     intervals.addItem(createInterval("Previous Full Quarter", "dayofmonth(nowdate() - quarters(2), 1)", "dayofmonth(nowdate() - quarters(2), daysinquarter(nowdate() - quarters(2))))"));
     */

        public static const LAST:int = 0;
        public static const NEXT:int = 1;
        public static const BEFORE:int = 2;
        public static const AFTER:int = 3; 

		public var interval:int = ALL;
        public var customBeforeOrAfter:int = LAST;
        public var customIntervalType:int = 2;
        public var customIntervalAmount:int = 1;

        private var intervalPH:CustomRollingInterval;

        public var intervals:ArrayCollection = new ArrayCollection();
    public var pullCustomIntervalsFromAccount:Boolean;
		
		public function RollingDateRangeFilterDefinition()
		{
			super();
		}
		
		override public function getType():int {
			return FilterDefinition.ROLLING_DATE;
		}

        override protected function subclassClone(filter:FilterDefinition):void {
            var intervals:ArrayCollection = new ArrayCollection();
            for each (var interval:CustomRollingInterval in this.intervals) {
                var copiedInterval:CustomRollingInterval = ObjectUtil.copy(interval) as CustomRollingInterval;
                copiedInterval.customRollingIntervalID = 0;
                intervals.addItem(copiedInterval);
            }
            RollingDateRangeFilterDefinition(filter).intervals = intervals;
        }

        override public function getSaveValue():Object {
            return interval;
        }

        override public function loadFromSharedObject(value:Object):void {
            interval = value as int;
        }
	}
}