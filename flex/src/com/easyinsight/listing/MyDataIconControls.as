package com.easyinsight.listing
{
import com.easyinsight.administration.feed.CredentialsResponse;
import com.easyinsight.customupload.DataSourceConfiguredEvent;
import com.easyinsight.customupload.FileFeedUpdateWindow;
import com.easyinsight.customupload.RefreshWindow;
import com.easyinsight.customupload.UploadConfigEvent;
import com.easyinsight.framework.Credentials;
import com.easyinsight.framework.GenericFaultHandler;
import com.easyinsight.framework.User;
import com.easyinsight.genredata.AnalyzeEvent;
import com.easyinsight.report.PackageAnalyzeSource;
import com.easyinsight.report.ReportAnalyzeSource;
import com.easyinsight.reportpackage.ReportPackageDescriptor;
import com.easyinsight.reportpackage.ReportPackageWindow;
import com.easyinsight.solutions.InsightDescriptor;

import com.easyinsight.util.PopUpUtil;
import com.easyinsight.util.ProgressAlert;

import flash.events.Event;
import flash.events.MouseEvent;

import mx.binding.utils.BindingUtils;
import mx.containers.HBox;
import mx.controls.Alert;
import mx.controls.Button;
import mx.managers.PopUpManager;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;
import mx.rpc.remoting.RemoteObject;

public class MyDataIconControls extends HBox
{
    private var obj:Object;

    [Embed(source="../../../../assets/refresh.png")]
    public var refreshIcon:Class;

    [Embed(source="../../../../assets/businessman_edit.png")]
    public var adminIcon:Class;

    [Embed(source="../../../../assets/copy.png")]
    public var copyIcon:Class;

    [Embed(source="../../../../assets/media_play_green.png")]
    public var playIcon:Class;

    [Embed(source="../../../../assets/navigate_cross.png")]
    public var deleteIcon:Class;

    private var userUploadSource:RemoteObject;

    private var refreshButton:Button;
    private var adminButton:Button;
    private var analyzeButton:Button;
    private var deleteButton:Button;
    private var copyButton:Button;

    private var _analyzeTooltip:String = "Analyze...";
    private var _analyzeVisible:Boolean = true;
    private var _refreshTooltip:String = "Refresh...";
    private var _refreshVisible:Boolean = true;
    private var _adminTooltip:String = "Administer...";
    private var _adminVisible:Boolean = true;
    private var _copyTooltip:String = "Copy...";
    private var _copyVisible:Boolean = true;
    private var _deleteTooltip:String = "Delete...";
    private var _deleteVisible:Boolean = true;

    public function MyDataIconControls()
    {
        super();
        analyzeButton = new Button();
        analyzeButton.setStyle("icon", playIcon);
        BindingUtils.bindProperty(analyzeButton, "toolTip", this, "analyzeTooltip");
        BindingUtils.bindProperty(analyzeButton, "visible", this, "analyzeVisible");
        analyzeButton.addEventListener(MouseEvent.CLICK, analyzeCalled);
        addChild(analyzeButton);
        refreshButton = new Button();
        refreshButton.setStyle("icon", refreshIcon);
        BindingUtils.bindProperty(refreshButton, "toolTip", this, "refreshTooltip");
        BindingUtils.bindProperty(refreshButton, "visible", this, "refreshVisible");
        refreshButton.addEventListener(MouseEvent.CLICK, refreshCalled);
        addChild(refreshButton);
        adminButton = new Button();
        adminButton.setStyle("icon", adminIcon);
        BindingUtils.bindProperty(adminButton, "toolTip", this, "adminTooltip");
        BindingUtils.bindProperty(adminButton, "visible", this, "adminVisible");
        adminButton.addEventListener(MouseEvent.CLICK, adminCalled);
        addChild(adminButton);
        copyButton = new Button();
        copyButton.setStyle("icon", copyIcon);
        BindingUtils.bindProperty(copyButton, "toolTip", this, "copyTooltip");
        BindingUtils.bindProperty(copyButton, "visible", this, "copyVisible");
        copyButton.addEventListener(MouseEvent.CLICK, copyCalled);
        addChild(copyButton);
        deleteButton = new Button();
        deleteButton.setStyle("icon", deleteIcon);
        BindingUtils.bindProperty(deleteButton, "toolTip", this, "deleteTooltip");
        BindingUtils.bindProperty(deleteButton, "visible", this, "deleteVisible");
        deleteButton.addEventListener(MouseEvent.CLICK, deleteCalled);
        addChild(deleteButton);

        this.addEventListener(RefreshNotificationEvent.REFRESH_NOTIFICATION, notifyRefresh);

        this.setStyle("paddingLeft", 5);
        this.setStyle("paddingRight", 5);
    }

    [Bindable(event="analyzeTooltipChanged")]
    public function get analyzeTooltip():String {
        return _analyzeTooltip;
    }

    public function set analyzeTooltip(value:String):void {
        if (_analyzeTooltip == value) return;
        _analyzeTooltip = value;
        dispatchEvent(new Event("analyzeTooltipChanged"));
    }

    [Bindable(event="analyzeVisibleChanged")]
    public function get analyzeVisible():Boolean {
        return _analyzeVisible;
    }

    public function set analyzeVisible(value:Boolean):void {
        if (_analyzeVisible == value) return;
        _analyzeVisible = value;
        dispatchEvent(new Event("analyzeVisibleChanged"));
    }

    [Bindable(event="refreshTooltipChanged")]
    public function get refreshTooltip():String {
        return _refreshTooltip;
    }

    public function set refreshTooltip(value:String):void {
        if (_refreshTooltip == value) return;
        _refreshTooltip = value;
        dispatchEvent(new Event("refreshTooltipChanged"));
    }

    [Bindable(event="refreshVisibleChanged")]
    public function get refreshVisible():Boolean {
        return _refreshVisible;
    }

    public function set refreshVisible(value:Boolean):void {
        if (_refreshVisible == value) return;
        _refreshVisible = value;
        dispatchEvent(new Event("refreshVisibleChanged"));
    }

    [Bindable(event="adminTooltipChanged")]
    public function get adminTooltip():String {
        return _adminTooltip;
    }

    public function set adminTooltip(value:String):void {
        if (_adminTooltip == value) return;
        _adminTooltip = value;
        dispatchEvent(new Event("adminTooltipChanged"));
    }

    [Bindable(event="adminVisibleChanged")]
    public function get adminVisible():Boolean {
        return _adminVisible;
    }

    public function set adminVisible(value:Boolean):void {
        if (_adminVisible == value) return;
        _adminVisible = value;
        dispatchEvent(new Event("adminVisibleChanged"));
    }

    [Bindable(event="copyTooltipChanged")]
    public function get copyTooltip():String {
        return _copyTooltip;
    }

    public function set copyTooltip(value:String):void {
        if (_copyTooltip == value) return;
        _copyTooltip = value;
        dispatchEvent(new Event("copyTooltipChanged"));
    }

    [Bindable(event="copyVisibleChanged")]
    public function get copyVisible():Boolean {
        return _copyVisible;
    }

    public function set copyVisible(value:Boolean):void {
        if (_copyVisible == value) return;
        _copyVisible = value;
        dispatchEvent(new Event("copyVisibleChanged"));
    }

    [Bindable(event="deleteTooltipChanged")]
    public function get deleteTooltip():String {
        return _deleteTooltip;
    }

    public function set deleteTooltip(value:String):void {
        if (_deleteTooltip == value) return;
        _deleteTooltip = value;
        dispatchEvent(new Event("deleteTooltipChanged"));
    }

    [Bindable(event="deleteVisibleChanged")]
    public function get deleteVisible():Boolean {
        return _deleteVisible;
    }

    public function set deleteVisible(value:Boolean):void {
        if (_deleteVisible == value) return;
        _deleteVisible = value;
        dispatchEvent(new Event("deleteVisibleChanged"));
    }

    private function notifyRefresh(event:RefreshNotificationEvent):void {
        Alert.show("Your data has begun to refresh and will be available when the refresh completes. This process may take some time depending on the size of the data source. You will receive a notification when the process is complete.");
    }



    private function copyCalled(event:MouseEvent):void {
        var window:CopyDataSourceWindow = new CopyDataSourceWindow();
        window.dataSourceDescriptor = obj as DataFeedDescriptor;
        PopUpManager.addPopUp(window, this.parent.parent.parent, true);
        window.addEventListener(UploadConfigEvent.UPLOAD_CONFIG_COMPLETE, passEvent);
        PopUpUtil.centerPopUp(window);
    }

    private function refreshCalled(event:MouseEvent):void {
        if (obj is DataFeedDescriptor) {
            var feedDescriptor:DataFeedDescriptor = obj as DataFeedDescriptor;
            switch (feedDescriptor.feedType) {
                case DataFeedDescriptor.STATIC:
                case DataFeedDescriptor.EMPTY:
                    fileData(feedDescriptor);
                    break;
                default:
                    refreshData(feedDescriptor);
                    break;
            }
        }
    }

    private function deleteCalled(event:MouseEvent):void {
        dispatchEvent(new DeleteDataSourceEvent(obj));
    }

    private function analyzeCalled(event:MouseEvent):void {
        if (obj is DataFeedDescriptor) {
            var descriptor:DataFeedDescriptor = obj as DataFeedDescriptor;
            dispatchEvent(new AnalyzeEvent(new DescriptorAnalyzeSource(descriptor.dataFeedID, descriptor.name)));
        } else if (obj is InsightDescriptor) {
            var analysisDefinition:InsightDescriptor = obj as InsightDescriptor;
            dispatchEvent(new AnalyzeEvent(new ReportAnalyzeSource(analysisDefinition)));
        } else if (obj is ReportPackageDescriptor) {
            var packageDescriptor:ReportPackageDescriptor = obj as ReportPackageDescriptor;
            dispatchEvent(new AnalyzeEvent(new PackageAnalyzeSource(packageDescriptor)));
        }
    }

    private function refreshData(feedDescriptor:DataFeedDescriptor):void {
        /*if (feedDescriptor.hasSavedCredentials) {
            userUploadSource = new RemoteObject();
            userUploadSource.destination = "userUpload";
            userUploadSource.refreshData.addEventListener(ResultEvent.RESULT, completedRefresh);
            userUploadSource.refreshData.addEventListener(FaultEvent.FAULT, GenericFaultHandler.genericFault);
            userUploadSource.refreshData.send(feedDescriptor.dataFeedID, null, false);
            dispatchEvent(new RefreshNotificationEvent());
            return;
        }
        else */
        var c:Credentials = User.getCredentials(feedDescriptor.dataFeedID);
        if (c != null) {
            userUploadSource = new RemoteObject();
            userUploadSource.destination = "userUpload";
            userUploadSource.refreshData.addEventListener(ResultEvent.RESULT, completedRefresh);
            userUploadSource.refreshData.addEventListener(FaultEvent.FAULT, GenericFaultHandler.genericFault);
            ProgressAlert.alert(this, "Refreshing data...", null, userUploadSource.refreshData);
            userUploadSource.refreshData.send(feedDescriptor.dataFeedID, c, false, true);
            //dispatchEvent(new RefreshNotificationEvent());
            return;
        }

        var refreshWindow:RefreshWindow = new RefreshWindow();
        refreshWindow.dataSourceID = feedDescriptor.dataFeedID;
        refreshWindow.addEventListener(DataSourceConfiguredEvent.DATA_SOURCE_CONFIGURED, dsConfigured);
        /*refreshWindow.addEventListener(UploadConfigEvent.UPLOAD_CONFIG_COMPLETE, passEvent);
        refreshWindow.addEventListener(RefreshNotificationEvent.REFRESH_NOTIFICATION, notifyRefresh);*/
        PopUpManager.addPopUp(refreshWindow, this, true);
        PopUpUtil.centerPopUp(refreshWindow);
    }

    private function dsConfigured(event:DataSourceConfiguredEvent):void {
        dispatchEvent(new UploadConfigEvent(UploadConfigEvent.UPLOAD_CONFIG_COMPLETE));
    }

    private function passEvent(event:UploadConfigEvent):void {
        dispatchEvent(event);
    }


    private function completedRefresh(event:ResultEvent):void {
        var credentialsResponse:CredentialsResponse = userUploadSource.refreshData.lastResult as CredentialsResponse;
        if (!credentialsResponse.successful)
            Alert.show(credentialsResponse.failureMessage, "Error");
        dispatchEvent(new UploadConfigEvent(UploadConfigEvent.UPLOAD_CONFIG_COMPLETE));
    }

    private function fileData(feedDescriptor:DataFeedDescriptor):void {
        var feedUpdateWindow:FileFeedUpdateWindow = FileFeedUpdateWindow(PopUpManager.createPopUp(this.parent.parent.parent, FileFeedUpdateWindow, true));
        feedUpdateWindow.feedID = feedDescriptor.dataFeedID;
        feedUpdateWindow.addEventListener(RefreshNotificationEvent.REFRESH_NOTIFICATION, notifyRefresh);
        PopUpUtil.centerPopUp(feedUpdateWindow);

    }

    private function adminCalled(event:MouseEvent):void {
        if (obj is DataFeedDescriptor) {
            var descriptor:DataFeedDescriptor = obj as DataFeedDescriptor;
            dispatchEvent(new AnalyzeEvent(new FeedAdminAnalyzeSource(descriptor.dataFeedID)));
        } else if (obj is InsightDescriptor) {
            var analysisDefinition:InsightDescriptor = obj as InsightDescriptor;
            dispatchEvent(new AnalyzeEvent(new AnalysisDefinitionAnalyzeSource(analysisDefinition)));
        } else if (obj is ReportPackageDescriptor) {
            var packageDescriptor:ReportPackageDescriptor = obj as ReportPackageDescriptor;
            var window:ReportPackageWindow = new ReportPackageWindow();
            window.reportPackageID = packageDescriptor.id;
            PopUpManager.addPopUp(window, this, true);
            PopUpUtil.centerPopUp(window);
        }
    }

    override public function set data(value:Object):void {
        this.obj = value;
        if (value is DataFeedDescriptor) {
            var descriptor:DataFeedDescriptor = value as DataFeedDescriptor;
            refreshButton.setVisible(true);
            adminVisible = descriptor.role == DataFeedDescriptor.OWNER;
            adminTooltip = "Administer the data source...";
            copyVisible = true;
            deleteVisible = descriptor.groupSourceID == 0;
        } else if (value is InsightDescriptor) {
            refreshVisible = false;
            adminVisible = true;
            adminTooltip = "Open report in the report editor...";
            copyVisible = false;
        } else if (value is ReportPackageDescriptor) {
            refreshVisible = false;
            adminVisible = true;
            adminTooltip = "Edit the package definition...";
            copyVisible = false;
        }
    }

    override public function get data():Object {
        return this.obj;
    }
}
}