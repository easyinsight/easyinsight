package com.easyinsight.twitter {
[Bindable]
[RemoteClass(alias="com.easyinsight.twitter.Tweet")]
public class Tweet {

    public var status:String;
    public var timeString:String;

    public function Tweet() {
    }
}
}