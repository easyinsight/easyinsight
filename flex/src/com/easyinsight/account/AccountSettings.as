package com.easyinsight.account {
[Bindable]
[RemoteClass(alias="com.easyinsight.users.AccountSettings")]
public class AccountSettings {

    public var apiEnabled:Boolean;
    public var publicData:Boolean;
    public var marketplace:Boolean;
    public var reportSharing:Boolean;
    public var groupID:int;
    public var dateFormat:int;
    public var currencySymbol:String;
    public var firstDayOfWeek:int;
    public var maxResults:int;
    public var sendEmail:Boolean;
    public var htmlView:Boolean;
    public var defaultFontFamily:String;
    public var locale:String;
    public var fiscalYearStartMonth:int;
    public var zone:String;

    public function AccountSettings() {
    }
}
}