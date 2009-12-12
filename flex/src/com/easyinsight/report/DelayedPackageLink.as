package com.easyinsight.report {
import com.easyinsight.LoginDialog;
import com.easyinsight.framework.LoginEvent;

import com.easyinsight.genredata.AnalyzeEvent;
import com.easyinsight.reportpackage.ReportPackageResponse;
import com.easyinsight.util.PopUpUtil;

import flash.display.DisplayObject;

import flash.events.EventDispatcher;

import mx.controls.Alert;
import mx.core.Application;
import mx.managers.PopUpManager;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;
import mx.rpc.remoting.RemoteObject;

public class DelayedPackageLink extends EventDispatcher {
    private var packageID:int;
    private var packageService:RemoteObject;

    public function DelayedPackageLink(packageID:int)
    {
        this.packageID = packageID;
        this.packageService = new RemoteObject();
        packageService.destination = "reportPackageService";
        packageService.openPackageIfPossible.addEventListener(ResultEvent.RESULT, gotFeed);
        packageService.openPackageIfPossible.addEventListener(FaultEvent.FAULT, fault);
    }

    private function fault(event:FaultEvent):void {
        Alert.show(event.fault.message);
    }

    public function execute():void {
        packageService.openPackageIfPossible.send(packageID);
    }

    private function gotFeed(event:ResultEvent):void {
        var packageResponse:ReportPackageResponse = packageService.openPackageIfPossible.lastResult as ReportPackageResponse;
        if (packageResponse.status == ReportPackageResponse.SUCCESS) {
            dispatchEvent(new AnalyzeEvent(new PackageAnalyzeSource(packageResponse.reportPackageDescriptor)));
        } else if (packageResponse.status == ReportPackageResponse.NEED_LOGIN) {
            var loginDialog:LoginDialog = LoginDialog(PopUpManager.createPopUp(Application.application as DisplayObject, LoginDialog, true));
            loginDialog.addEventListener(LoginEvent.LOGIN, delayedFeed);
            PopUpUtil.centerPopUp(loginDialog);
        } else {
            // tried to access a data source they don't have rights to, silently fail
        }
    }

    private function delayedFeed(event:LoginEvent):void {
        packageService.openPackageIfPossible.send(packageID);
    }
}
}