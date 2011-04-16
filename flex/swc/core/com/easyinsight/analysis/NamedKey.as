package com.easyinsight.analysis
{
import mx.events.FlexEvent;
	[Bindable]
	[RemoteClass(alias="com.easyinsight.core.NamedKey")]
	public class NamedKey extends Key
	{
		public var name:String;
        public var indexed:Boolean;
        public var keyChanged:Boolean;
		
		public function NamedKey()
		{
			super();
		}
		
		override public function createString():String {
			return name;
		}

        override public function toBaseKey():Key {
            return this;
        }

        public function get nameValue():String {
            return name;
        }

        override public function internalString():String {
            return name;
        }

        public function set nameValue(nameValue:String):void {
            this.name = nameValue;
            dispatchEvent(new FlexEvent(FlexEvent.DATA_CHANGE));
        }
	}
}