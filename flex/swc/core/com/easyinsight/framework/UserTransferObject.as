package com.easyinsight.framework
{		
	[Bindable]
	[RemoteClass(alias="com.easyinsight.users.UserTransferObject")]
	public class UserTransferObject
	{
		public var accountID:int;			
		public var userID:int;
		public var userName:String;
		public var email:String;
		public var name:String;
        public var accountAdmin:Boolean;
        public var firstName:String;
        public var title:String;
        public var personaID:int;
        public var optInEmail:Boolean;
        public var fixedDashboardID:int;
        public var initialSetupDone:Boolean;
        public var invoiceRecipient:Boolean;
        public var autoRefreshReports:Boolean;
        public var onlyShowTopReports:Boolean;
        public var analyst:Boolean = true;
        public var consultant:Boolean;
        public var testAccountVisible:Boolean = true;
        public var lastLoginDate:Date;
        public var currency:int;
        public var userLocale:String = "0";
        public var dateFormat:int = 6;

        public var selected:Boolean;
		
		public function UserTransferObject()
		{
		}

        public function get fullName():String {
            if (firstName == null || firstName == "") {
                return name;
            }
            return firstName + " " + name;
        }
	}
}