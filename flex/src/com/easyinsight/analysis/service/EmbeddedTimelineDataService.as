package com.easyinsight.analysis.service {

import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.EmbeddedDataResults;
import com.easyinsight.analysis.EmbeddedDataServiceEvent;
import com.easyinsight.analysis.EmbeddedTimelineResults;
import com.easyinsight.analysis.IEmbeddedDataService;
import com.easyinsight.analysis.ListDataResults;
import com.easyinsight.analysis.SeriesDataResults;
import com.easyinsight.analysis.Value;
import com.easyinsight.analysis.conditions.ConditionRenderer;
import com.easyinsight.framework.CredentialsCache;
import com.easyinsight.framework.DataServiceLoadingEvent;
import com.easyinsight.framework.InsightRequestMetadata;

import flash.events.EventDispatcher;
import mx.collections.ArrayCollection;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;
import mx.rpc.remoting.RemoteObject;
import mx.controls.Alert;

public class EmbeddedTimelineDataService extends EventDispatcher implements IEmbeddedDataService {

    private var dataRemoteSource:RemoteObject;

    public function EmbeddedTimelineDataService() {
        super();
        dataRemoteSource = new RemoteObject();
        dataRemoteSource.destination = "data";
        dataRemoteSource.getEmbeddedResults.addEventListener(ResultEvent.RESULT, processListData);
        dataRemoteSource.getEmbeddedResults.addEventListener(FaultEvent.FAULT, onFault);
    }

    private function onFault(event:FaultEvent):void {
        dispatchEvent(new ReportRetrievalFault(event.fault.message));
    }

    private function processListData(event:ResultEvent):void {
        var results:EmbeddedTimelineResults = dataRemoteSource.getEmbeddedResults.lastResult as EmbeddedTimelineResults;
        var dataSets:ArrayCollection = new ArrayCollection();
        if (results.credentialRequirements == null || results.credentialRequirements.length == 0) {


            for each (var listData:ListDataResults in results.listDatas) {
                dataSets.addItem(new ListDataService().translate(listData));
            }
            results.additionalProperties.seriesValues = results.seriesValues;
        }
        dispatchEvent(new EmbeddedDataServiceEvent(EmbeddedDataServiceEvent.DATA_RETURNED, dataSets, results.definition, new Object(), results.dataSourceAccessible,
                results.attribution, listData.credentialRequirements, listData.dataSourceInfo, results.ratingsAverage,
                results.ratingsCount, results.additionalProperties));
        dispatchEvent(new DataServiceLoadingEvent(DataServiceLoadingEvent.LOADING_STOPPED));
    }

    public function retrieveData(reportID:int, dataSourceID:int, filters:ArrayCollection, refreshAll:Boolean, drillthroughFilters:ArrayCollection,
            noCache:Boolean, hierarchyOverrides:ArrayCollection):void {
        dispatchEvent(new DataServiceLoadingEvent(DataServiceLoadingEvent.LOADING_STARTED));
        var insightRequestMetadata:InsightRequestMetadata = new InsightRequestMetadata();
        insightRequestMetadata.refreshAllSources = refreshAll;
        insightRequestMetadata.credentialFulfillmentList = CredentialsCache.getCache().createCredentials();
        insightRequestMetadata.noCache = noCache;
        insightRequestMetadata.hierarchyOverrides = hierarchyOverrides;
        dataRemoteSource.getEmbeddedResults.send(reportID, dataSourceID, filters, insightRequestMetadata, drillthroughFilters);
    }
}
}