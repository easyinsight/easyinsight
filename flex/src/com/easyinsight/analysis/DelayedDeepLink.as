package com.easyinsight.analysis
{
	import com.easyinsight.LoginDialog;
	import com.easyinsight.framework.LoginEvent;
import com.easyinsight.genredata.AnalyzeEvent;
import com.easyinsight.listing.AnalysisDefinitionAnalyzeSource;

import com.easyinsight.util.PopUpUtil;

import flash.display.DisplayObject;
	import flash.events.EventDispatcher;
	
	import mx.core.Application;
	import mx.managers.PopUpManager;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.RemoteObject;
	
	public class DelayedDeepLink extends EventDispatcher
	{
		private var analysisService:RemoteObject;
		private var analysisID:String;
		
		public function DelayedDeepLink(analysisID:String)
		{
			this.analysisID = analysisID;
			this.analysisService = new RemoteObject();
			analysisService.destination = "analysisDefinition";
			analysisService.openAnalysisIfPossible.addEventListener(ResultEvent.RESULT, gotAnalysisDefinition);		
		}
		
		public function execute():void {
			analysisService.openAnalysisIfPossible.send(analysisID);
		}

		private function gotAnalysisDefinition(event:ResultEvent):void {
        	var insightResponse:InsightResponse = analysisService.openAnalysisIfPossible.lastResult as InsightResponse;
        	if (insightResponse.status == InsightResponse.SUCCESS) {
        		dispatchEvent(new AnalyzeEvent(new AnalysisDefinitionAnalyzeSource(insightResponse.insightDescriptor)));
        	} else if (insightResponse.status == InsightResponse.NEED_LOGIN) {
        		var loginDialog:LoginDialog = LoginDialog(PopUpManager.createPopUp(Application.application as DisplayObject, LoginDialog, true));
        		loginDialog.addEventListener(LoginEvent.LOGIN, delayedAnalysis);
                PopUpUtil.centerPopUp(loginDialog);
        	} else {
                // silently fail, user trying to spoof an ID
            }
        }  
        
        private function delayedAnalysis(event:LoginEvent):void {
        	analysisService.openAnalysisIfPossible.send(analysisID);
        }
	}
}