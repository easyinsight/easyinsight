package com.easyinsight.analysis
{
	[Bindable]
	[RemoteClass(alias="com.easyinsight.core.Value")]
	public class Value
	{

        public static const STRING:int = 1;
        public static const NUMBER:int = 2;
        public static const DATE:int = 3;
        public static const EMPTY:int = 4;

        public var links:Object;
        public var originalValue:Value;

		public function Value()
		{
		}

        public function type():int {
            return 0;
        }

        public function toNumber():Number {
            return 0;
        }

        public function toString():String {
            return "";
        }

        public function toDate():Date {
            return null;
        }

		public function getValue():Object {
			return null;
		}

        public function toSortValue():Value {
            if (originalValue != null) {
                return originalValue;
            }
            return this;
        }
	}
}