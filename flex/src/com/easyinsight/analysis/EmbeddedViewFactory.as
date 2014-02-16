package com.easyinsight.analysis {

import com.easyinsight.analysis.list.SizeOverrideEvent;
import com.easyinsight.analysis.service.EmbeddedDataService;
import com.easyinsight.analysis.service.ReportRetrievalFault;
import com.easyinsight.customupload.ProblemDataEvent;
import com.easyinsight.filtering.FilterDefinition;
import com.easyinsight.framework.Constants;
import com.easyinsight.framework.DataServiceLoadingEvent;
import com.easyinsight.framework.ReportModuleLoader;
import com.easyinsight.report.ReportCanvas;
import com.easyinsight.report.ReportEventProcessor;
import com.easyinsight.report.ReportInfo;
import com.easyinsight.report.ReportNavigationEvent;
import com.easyinsight.report.ReportSetupEvent;
import com.easyinsight.util.EIErrorEvent;

import flash.events.Event;

import mx.binding.utils.BindingUtils;
import mx.collections.ArrayCollection;
import mx.containers.Canvas;
import mx.core.UIComponent;
import mx.rpc.events.ResultEvent;
import mx.rpc.remoting.RemoteObject;

public class EmbeddedViewFactory extends Canvas implements IRetrievable {

    private var _reportID:int;
    private var _dataSourceID:int;
    private var _availableFields:ArrayCollection;
    private var _filterDefinitions:ArrayCollection;
    private var _additionalFilterDefinitions:ArrayCollection;
    private var _reportType:int;
    private var _adHocMode:Boolean = true;

    private var _reportRendererModule:String;
    //private var _newDefinition:Class;
    private var _reportDataService:Class = EmbeddedDataService;

    private var _reportRenderer:IReportRenderer;
    private var _dataService:IEmbeddedDataService = new EmbeddedDataService();

    private var _additionalItems:ArrayCollection;

    private var pendingRequest:Boolean = false;

    public function EmbeddedViewFactory() {
        super();
        //setStyle("backgroundColor", 0xCCCCCC);
        this.percentHeight = 100;
        this.percentWidth = 100;
    }

    public function set additionalItems(value:ArrayCollection):void {
        _additionalItems = value;
    }

    public function set adHocMode(value:Boolean):void {
        _adHocMode = value;
    }

    public function set additionalFilterDefinitions(value:ArrayCollection):void {
        _additionalFilterDefinitions = value;
    }

    public function get reportRendererModule():String {
        return _reportRendererModule;
    }

    public function set reportDataService(val:Class):void {
        _reportDataService = val;
    }

    public function set reportRenderer(val:String):void {
        _reportRendererModule = val;
    }

    public function get dataService():IEmbeddedDataService {
        return _dataService;
    }

    private var reportCanvas:ReportCanvas;

    private var _showLoading:Boolean = false;

    [Bindable(event="showLoadingChanged")]
    public function get showLoading():Boolean {
        return _showLoading;
    }

    public function set showLoading(value:Boolean):void {
        if (_showLoading == value) return;
        _showLoading = value;
        dispatchEvent(new Event("showLoadingChanged"));
    }

    private function onError(event:EIErrorEvent):void {
        stackTrace = event.error.getStackTrace();
        overlayIndex = 3;
    }

    private var _reportPaddingWidth:int = 10;

    public function set reportPaddingWidth(value:int):void {
        _reportPaddingWidth = value;
    }

    private var resultsUID:String;

    private function onShowAll(event:ReportPagingEvent):void {
        resultsUID = event.uid;
        forceRetrieve();
    }

    override protected function createChildren():void {
        super.createChildren();
        addEventListener(ReportPagingEvent.SHOW_ALL, onShowAll);
        _dataService = new _reportDataService();
        _dataService.addEventListener(DataServiceLoadingEvent.LOADING_STARTED, dataLoadingEvent);
        _dataService.addEventListener(DataServiceLoadingEvent.LOADING_STOPPED, dataLoadingEvent);
        _dataService.addEventListener(EmbeddedDataServiceEvent.DATA_RETURNED, gotData);
        _dataService.addEventListener(ReportRetrievalFault.RETRIEVAL_FAULT, retrievalFault);
        _dataService.addEventListener(EIErrorEvent.ERROR, onError);
        canvas = new Canvas();
        if (_styleCanvas) {
            canvas.setStyle("borderStyle", "solid");
            canvas.setStyle("borderThickness", 1);
            canvas.setStyle("cornerRadius", 8);
            canvas.setStyle("dropShadowEnabled", true);
        }
        canvas.setStyle("backgroundAlpha", 1);
        canvas.setStyle("backgroundColor", 0xFFFFFF);
        reportCanvas = new ReportCanvas();
        canvas.x = _reportPaddingWidth;
        reportCanvas.x = _reportPaddingWidth;
        if (_spaceSides) {
            canvas.y = 5;
            reportCanvas.y = 10;
        }
        BindingUtils.bindProperty(reportCanvas, "loading", this, "loading");
        BindingUtils.bindProperty(reportCanvas, "overlayIndex", this, "overlayIndex");
        BindingUtils.bindProperty(reportCanvas, "stackTrace", this, "stackTrace");
        canvas.addChild(reportCanvas);
        addChild(canvas);
        noData = new NoData();
        loadReportRenderer();
    }

    private var _styleCanvas:Boolean = true;

    public function set styleCanvas(value:Boolean):void {
        _styleCanvas = value;
    }

    private var canvas:Canvas;

    private var noData:NoData;

    private var _spaceSides:Boolean = true;

    public function set spaceSides(value:Boolean):void {
        _spaceSides = value;
    }

    override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
        super.updateDisplayList(unscaledWidth, unscaledHeight);
        if (_spaceSides) {
            canvas.width = unscaledWidth - (_reportPaddingWidth * 2);
            canvas.height = unscaledHeight - 10;
            reportCanvas.width = unscaledWidth - (_reportPaddingWidth * 4);
            reportCanvas.height = unscaledHeight - 40;
        } else {
            canvas.width = unscaledWidth - (_reportPaddingWidth * 2);
            canvas.height = unscaledHeight;
            reportCanvas.width = unscaledWidth - (_reportPaddingWidth * 4);
            reportCanvas.height = unscaledHeight;
        }

        if (currentComponent != null && (currentComponent.height != reportCanvas.height || currentComponent.width != reportCanvas.width)) {
            currentComponent.height = reportCanvas.height;
            currentComponent.width = reportCanvas.width;
            currentComponent.invalidateDisplayList();
        }
    }

    private var _dashboardID:int;

    public function set dashboardID(value:int):void {
        _dashboardID = value;
    }

    public function get reportType():int {
        return _reportType;
    }

    public function set reportType(value:int):void {
        _reportType = value;
    }

    public function set availableFields(val:ArrayCollection):void {
        _availableFields = val;
    }

    public function set reportID(val:int):void {
        _reportID = val;
    }

    public function loadRenderer():void {

    }

    public function set dataSourceID(value:int):void {
        _dataSourceID = value;
    }


    public function get reportID():int {
        return _reportID;
    }

    public function get dataSourceID():int {
        return _dataSourceID;
    }

    public function get availableFields():ArrayCollection {
        return _availableFields;
    }

    public function set filterDefinitions(value:ArrayCollection):void {
        _filterDefinitions = value;
    }

    public function get filterDefinitions():ArrayCollection {
        return _filterDefinitions;
    }

    private var _drillthroughFilters:ArrayCollection;

    public function set drillthroughFilters(value:ArrayCollection):void {
        _drillthroughFilters = value;
    }

    public function get drillthroughFilters():ArrayCollection {
        return _drillthroughFilters;
    }

    private function retrievalFault(event:ReportRetrievalFault):void {
        overlayIndex = 2;
        dispatchEvent(event);
    }

    private function dataLoadingEvent(event:DataServiceLoadingEvent):void {
        if (event.type == DataServiceLoadingEvent.LOADING_STARTED) overlayIndex = 1;
        loading = event.type == DataServiceLoadingEvent.LOADING_STARTED;
        dispatchEvent(event);
    }

    private var analysisService:RemoteObject;

    public function setup():void {
        analysisService = new RemoteObject();
        analysisService.destination = "analysisDefinition";
        analysisService.getReportInfo.addEventListener(ResultEvent.RESULT, gotReport);
        analysisService.getReportInfo.send(_reportID, showHeader);
    }

    public var showHeader:Boolean = false;

    public function gotReport(event:ResultEvent):void {
        var info:ReportInfo = analysisService.getReportInfo.lastResult as ReportInfo;
        if (info.accessDenied) {
            overlayIndex = 4;
        } else {
            if (info.report.adHocExecution) {
                _adHocMode = false;
                overlayIndex = 5;
            }
            report = info.report;
            dispatchEvent(new ReportSetupEvent(info));
        }
    }

    private var currentComponent:UIComponent;

    private function showReport():void {
        if (!UIComponent(_reportRenderer).parent) {
            if (currentComponent) {
                reportCanvas.removeChild(currentComponent);
            }
            currentComponent = _reportRenderer as UIComponent;
            currentComponent.height = reportCanvas.height;
            currentComponent.width = reportCanvas.width;
            reportCanvas.addChildAt(currentComponent, 0);
            invalidateDisplayList();
        }
    }

    private function showNoData():void {
        if (!noData.parent) {
            if (currentComponent) {
                reportCanvas.removeChild(currentComponent);
            }
            currentComponent = noData;
            reportCanvas.addChildAt(noData, 0);
        }
    }

    public function refresh():void {
        retrieveData();
    }

    public function forceRetrieve():void {
        retrieveData(true);
    }

    public function retrieveData(forceRetrieve:Boolean = false):void {
        if (_adHocMode || forceRetrieve) {
            if (_reportRenderer == null) {
                pendingRequest = true;
            } else {
                var overrides:ArrayCollection = new ArrayCollection();
                for each (var hierarchyOverride:AnalysisItemOverride in overrideObj) {
                    overrides.addItem(hierarchyOverride);
                }
                var filters:ArrayCollection;
                if (filterDefinitions == null) {
                    filters = new ArrayCollection();
                } else {
                    filters = new ArrayCollection(filterDefinitions.toArray());
                }
                if (_additionalFilterDefinitions != null) {
                    for each (var filter:FilterDefinition in _additionalFilterDefinitions) {
                        filters.addItem(filter);
                    }
                }
                _dataService.retrieveData(reportID, dataSourceID, filters, false, drillthroughFilters, _noCache, overrides, createRequestParams(), _additionalItems);
            }
        }
    }

    private function createRequestParams():RequestParams {
        var requestParams:RequestParams = new RequestParams();
        requestParams.uid = resultsUID;
        return requestParams;
    }

    private var overrideObj:Object = new Object();

    public function addOverride(hierarchyOverride:AnalysisItemOverride):void {
        overrideObj[hierarchyOverride.analysisItemID] = hierarchyOverride;
    }

    private var _noCache:Boolean = false;

    public function set noCache(value:Boolean):void {
        _noCache = value;
    }

    private var _report:AnalysisDefinition;


    [Bindable(event="reportChanged")]
    public function get report():AnalysisDefinition {
        return _report;
    }

    public function set report(value:AnalysisDefinition):void {
        if (_report == value) return;
        _report = value;
        dispatchEvent(new Event("reportChanged"));
    }

    private function onProblem(event:ProblemDataEvent):void {
        var overrides:ArrayCollection = new ArrayCollection();
        for each (var hierarchyOverride:AnalysisItemOverride in overrideObj) {
            overrides.addItem(hierarchyOverride);
        }
        _dataService.retrieveData(reportID, dataSourceID, filterDefinitions, false, drillthroughFilters, _noCache, overrides, createRequestParams(), _additionalItems);
    }

    private var _usePreferredHeight:Boolean = false;

    public function set usePreferredHeight(value:Boolean):void {
        _usePreferredHeight = value;
    }

    public function gotData(event:EmbeddedDataServiceEvent):void {
        if (event.reportFault != null) {
            event.reportFault.popup(this, onProblem);
        } else {
            event.additionalProperties.preferredSize = _usePreferredHeight;
            event.additionalProperties.prefix = Constants.instance().prefix;
            overlayIndex = 0;
            try {
                _report = event.analysisDefinition;
                postProcess(_report);
                if (event.hasData) {
                    showReport();
                    _reportRenderer.renderReport(event.dataSet, event.analysisDefinition, new Object(), event.additionalProperties);
                } else {
                    if (_usePreferredHeight) {
                        dispatchEvent(new SizeOverrideEvent(-1, 100));
                    }
                    showNoData();
                }
                dispatchEvent(event);
            } catch(e:Error) {
                stackTrace = e.getStackTrace();
                overlayIndex = 3;
            }
        }

    }

    private var postProcessors:ArrayCollection = new ArrayCollection();

    public function registerPostProcessor(processor:IReportPostProcessor):void {
        postProcessors.addItem(processor);
    }

    private function postProcess(report:AnalysisDefinition):void {
        for each (var p:IReportPostProcessor in postProcessors) {
            p.processReport(report);
        }

    }

    private function loadReportRenderer():void {
        reportModuleLoader = new ReportModuleLoader();
        reportModuleLoader.addEventListener("moduleLoaded", reportLoadHandler);
        reportModuleLoader.loadReportRenderer(_reportRendererModule, reportCanvas);
    }

    private var _stackTrace:String;

    private var _overlayIndex:int;

    private var _loading:Boolean;

    [Bindable(event="stackTraceChanged")]
    public function get stackTrace():String {
        return _stackTrace;
    }

    public function set stackTrace(value:String):void {
        if (_stackTrace == value) return;
        _stackTrace = value;
        dispatchEvent(new Event("stackTraceChanged"));
    }

    [Bindable(event="overlayIndexChanged")]
    public function get overlayIndex():int {
        return _overlayIndex;
    }

    public function set overlayIndex(value:int):void {
        if (_overlayIndex == value) return;
        _overlayIndex = value;
        dispatchEvent(new Event("overlayIndexChanged"));
    }

    [Bindable(event="loadingChanged")]
    public function get loading():Boolean {
        return _loading;
    }

    public function set loading(value:Boolean):void {
        if (_loading == value) return;
        _loading = value;
        dispatchEvent(new Event("loadingChanged"));
    }

    private var reportModuleLoader:ReportModuleLoader;

    private function reportLoadHandler(event:Event):void {
        _reportRenderer = reportModuleLoader.create() as IReportRenderer;
        _reportRenderer.addEventListener(ReportRendererEvent.FORCE_RENDER, forceRender, false, 0, true);
        _reportRenderer.addEventListener(ReportNavigationEvent.TO_REPORT, toReport, false, 0, true);
        _reportRenderer.addEventListener(ReportWindowEvent.REPORT_WINDOW, onReportWindow, false, 0, true);
        _reportRenderer.addEventListener(AnalysisItemChangeEvent.ANALYSIS_ITEM_CHANGE, onItemChange, false, 0, true);
        UIComponent(_reportRenderer).percentWidth = 100;
        UIComponent(_reportRenderer).percentHeight = 100;
        _dataService.preserveValues = _reportRenderer.preserveValues();
        if (pendingRequest) {
            pendingRequest = false;
            refresh();
        }
    }

    private function onItemChange(event:AnalysisItemChangeEvent):void {
        var o:DateLevelOverride = new DateLevelOverride();
        o.analysisItemID = event.item.analysisItemID;
        o.dateLevel = AnalysisDateDimension(event.item).dateLevel;
        dispatchEvent(new AnalysisItemOverrideEvent(o));
    }

    private function onReportWindow(event:ReportWindowEvent):void {
        var window:ReportEventProcessor = ReportEventProcessor.fromEvent(event, this);
        window.addEventListener(ReportNavigationEvent.TO_REPORT, function(event:ReportNavigationEvent):void {
            dispatchEvent(event);
        }, false, 0, true);
    }

    private function toReport(event:ReportNavigationEvent):void {
        //dispatchEvent(event);
    }

    public function updateExportMetadata():void {
        _reportRenderer.updateExportMetadata(null);
    }

    private function forceRender(event:ReportRendererEvent):void {
        refresh();
    }
}
}