package com.easyinsight.salesforce
{
import com.easyinsight.administration.feed.ServerDataSourceDefinition;
import com.easyinsight.customupload.SalesforceDataSourceCreation;
import com.easyinsight.datasources.DataSourceType;

[Bindable]
	[RemoteClass(alias="com.easyinsight.datafeeds.salesforce.SalesforceBaseDataSource")]
	public class SalesforceFeedDefinition extends ServerDataSourceDefinition
	{

    public var accessToken:String;
    public var refreshToken:String;
    public var instanceName:String;

		public function SalesforceFeedDefinition()
		{
			super();
		    this.feedName = "Salesforce";
        }


    override public function configClass():Class {
        return SalesforceDataSourceCreation;
    }

    override public function getFeedType():int {
        return DataSourceType.SALESFORCE;
    }
	}
}