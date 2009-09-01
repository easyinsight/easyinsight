package com.easyinsight.dbservice {
[Bindable]
[RemoteClass(alias="com.easyinsight.dbservice.SQLServerConfiguration")]
public class SQLServerConfiguration extends DatabaseConfiguration{

    public var host:String;
    public var port:String;
    public var databaseName:String;
    public var userName:String;
    public var password:String;

    public function SQLServerConfiguration() {
        super();
    }
}
}