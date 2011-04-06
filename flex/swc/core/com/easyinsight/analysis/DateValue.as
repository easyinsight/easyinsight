package com.easyinsight.analysis
{
	[Bindable]
	[RemoteClass(alias="com.easyinsight.core.DateValue")]
	
	public class DateValue extends Value
	{
		public var year:int;
        public var month:int;
        public var day:int;
        public var hour:int;
        public var minute:int;

        public var cachedDate:Date;

        public var date:Date;
				
		public function DateValue()
		{
			super();
		}

        private function getDate():Date {
            if (cachedDate == null) {
                cachedDate = new Date();
                cachedDate.setUTCFullYear(year, month, day);
                //cachedDate.setFullYear(year, month, day);
                //cachedDate.setHours(hour, minute);
                cachedDate.setUTCHours(hour, minute);
            }
            return cachedDate;
        }

        override public function type():int {
            return Value.DATE;
        }

        override public function toString():String {
            return String(getDate());
        }

        override public function toDate():Date {
            return getDate();
        }
		
		override public function getValue():Object {
			return getDate();
		}
	}
}