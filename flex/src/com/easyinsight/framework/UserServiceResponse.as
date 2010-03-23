package com.easyinsight.framework
{
import com.easyinsight.preferences.UISettings;

[Bindable]
	[RemoteClass(alias="com.easyinsight.users.UserServiceResponse")]
	public class UserServiceResponse
	{
		public var successful:Boolean;
		public var failureMessage:String;
		public var userID:int;
		public var accountID:int;
		public var name:String;
		public var spaceAllowed:int;
		public var accountType:int;
		public var email:String;
		public var userName:String;
		public var encryptedPassword:String;
        public var accountAdmin:Boolean;
        public var dataSourceCreator:Boolean;
        public var insightCreator:Boolean;
        public var activated:Boolean;
        public var billingInformationGiven:Boolean;
        public var accountState:int;
        public var uiSettings:UISettings;
		
		public function UserServiceResponse()
			{
			super();
		}

	}
}