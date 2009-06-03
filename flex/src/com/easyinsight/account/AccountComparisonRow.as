package com.easyinsight.account
{
	public class AccountComparisonRow
	{
		private var properties:Object = new Object();
		private var _comparisonRowName:String;
		
		public function get comparisonRowName():String {
			return this._comparisonRowName;
		}
		
		public function AccountComparisonRow(label:String, free:String,
			individual:String, group:String, professional:String, enterprise:String) {
			this._comparisonRowName = label;
			properties["free"] = free;
			properties["individual"] = individual;
            properties["group"] = group;
			properties["professional"] = professional;
            properties["enterprise"] = enterprise;
		}

		public function getText(field:String):String {
			return properties[field];
		} 
	}
}