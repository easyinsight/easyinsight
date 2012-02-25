package com.easyinsight.analysis
{


import com.easyinsight.genredata.AnalyzeEvent;
import com.easyinsight.report.ReportAnalyzeSource;
import com.easyinsight.util.ProgressAlert;

import flash.events.EventDispatcher;

import mx.core.Application;
import mx.core.UIComponent;
import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.RemoteObject;
	
	public class DelayedReportLink extends EventDispatcher
	{
		private var analysisService:RemoteObject;
		private var analysisID:String;
		
		public function DelayedReportLink(analysisID:String)
		{
			this.analysisID = analysisID;
			this.analysisService = new RemoteObject();
			analysisService.destination = "analysisDefinition";
			analysisService.openAnalysisIfPossible.addEventListener(ResultEvent.RESULT, gotAnalysisDefinition);		
		}
		
		public function execute():void {
            ProgressAlert.alert(UIComponent(Application.application), "Opening the report...", null, analysisService.openAnalysisIfPossible);
			analysisService.openAnalysisIfPossible.send(analysisID);
		}

		private function gotAnalysisDefinition(event:ResultEvent):void {
        	var insightResponse:InsightResponse = analysisService.openAnalysisIfPossible.lastResult as InsightResponse;
        	if (insightResponse.status == InsightResponse.SUCCESS) {
                dispatchEvent(new AnalyzeEvent(new ReportAnalyzeSource(insightResponse.insightDescriptor)));
        	} else {
                // silently fail, user trying to spoof an ID
            }
        }
	}
}