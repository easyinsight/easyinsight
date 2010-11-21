package com.easyinsight.account
{
	
	[Bindable]
	[RemoteClass(alias="com.easyinsight.users.AccountTransferObject")]
	public class Account
	{
        public static const PERSONAL:int = 1;
        public static const BASIC:int = 2;
        public static const PLUS:int = 3;
        public static const PRO:int = 4;
        public static const PREMIUM:int = 5;
        public static const ENTERPRISE:int = 6;
        public static const ADMINISTRATOR:int = 7;

        public static const INACTIVE:int = 1;
        public static const ACTIVE:int = 2;
        public static const DELINQUENT:int = 3;
        public static const SUSPENDED:int = 4;
        public static const CLOSED:int = 5;
        public static const PENDING_BILLING:int = 6;
        public static const PREPARING:int = 7;
        public static const BETA:int = 8;
        public static const TRIAL:int = 9;
        public static const CLOSING:int = 10;
        public static const REACTIVATION_POSSIBLE:int = 11;

		public var accountType:int;
		public var accountID:int;
        public var maxUsers:int;
        public var maxSize:int;
        public var name:String;
        public var accountState:int;
        public var apiEnabled:Boolean = true;
        public var activated:Boolean;
        public var optInEmail:Boolean;
        public var currencySymbol:String = "$";
		
		public function Account()
		{
		}

	}
}