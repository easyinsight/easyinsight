package com.easyinsight.filtering
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.easyinsight.analysis.FilterValueDefinition")]
	public class FilterValueDefinition extends FilterDefinition
	{
		public var filteredValues:ArrayCollection = new ArrayCollection();
		public var inclusive:Boolean;
        public var singleValue:Boolean;
        public var autoComplete:Boolean;

		public function FilterValueDefinition()
		{
			super();
		}
		
		override public function getType():int {
			return FilterDefinition.VALUE;
		}

        override public function matches(filterDefinition:FilterDefinition):Boolean {
            var matches:Boolean = super.matches(filterDefinition);
            if (matches) {
                var valFilter:FilterValueDefinition = filterDefinition as FilterValueDefinition;
                matches = inclusive == valFilter.inclusive && singleValue == valFilter.singleValue &&
                        autoComplete == valFilter.autoComplete && filteredValues.length == valFilter.filteredValues.length;
                if (matches) {
                    var foundItem:Boolean = false;
                    for each (var obj:Object in filteredValues) {
                        foundItem = valFilter.filteredValues.getItemIndex(obj) != -1;
                        if (!foundItem) {
                            matches = false;
                            break;
                        }
                    }
                    return matches;
                }
            }
            return false;
        }
    }
}