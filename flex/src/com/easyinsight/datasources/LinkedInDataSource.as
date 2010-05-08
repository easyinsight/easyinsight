package com.easyinsight.datasources {
import com.easyinsight.administration.feed.ServerDataSourceDefinition;
import com.easyinsight.customupload.LinkedInDataSourceCreation;
import com.easyinsight.customupload.MeetupDataSourceCreation;
import com.easyinsight.listing.DataFeedDescriptor;

[Bindable]
[RemoteClass(alias="com.easyinsight.datafeeds.linkedin.LinkedInDataSource")]
public class LinkedInDataSource extends ServerDataSourceDefinition {

    public var pin:String;

    public function LinkedInDataSource() {
        super();
        feedName = "LinkedIn";
    }

    override public function isLiveData():Boolean {
        return false;
    }

    override public function getFeedType():int {
        return DataFeedDescriptor.LINKEDIN;
    }

    override public function configClass():Class {
        return LinkedInDataSourceCreation;
    }
}
}