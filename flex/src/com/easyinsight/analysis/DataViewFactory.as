package com.easyinsight.analysis {
import com.easyinsight.analysis.heatmap.TopoMapDefinition;
import com.easyinsight.customupload.ProblemDataEvent;
import com.easyinsight.filtering.FilterRawData;
import com.easyinsight.framework.DataServiceLoadingEvent;
import com.easyinsight.framework.ReportModuleLoader;
import com.easyinsight.report.ReportCanvas;

import com.easyinsight.report.ReportEventProcessor;

import com.easyinsight.report.ReportNavigationEvent;
import com.easyinsight.report.RunReportCanvas;

import flash.display.DisplayObject;
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.MouseEvent;
import flash.utils.getQualifiedClassName;

import mx.binding.utils.BindingUtils;
import mx.binding.utils.ChangeWatcher;
import mx.collections.ArrayCollection;
import mx.containers.Canvas;
import mx.containers.HBox;
import mx.containers.VBox;
import mx.controls.Button;
import mx.core.IUIComponent;
import mx.core.UIComponent;
import mx.events.DragEvent;
import mx.managers.DragManager;

public class DataViewFactory extends VBox implements IRetrievable {

    private var _reportControlBar:Class;
    private var _reportRendererModule:String;
    private var _newDefinition:Class;
    private var _reportDataService:Class;

    private var _adHocMode:Boolean;
    
    private var _analysisDefinition:AnalysisDefinition;

    private var _lastData:ArrayCollection;
    private var _availableFields:ArrayCollection;

    private var _dataSourceID:int;

    private var _controlBar:IReportControlBar;
    private var _reportRenderer:IReportRenderer;
    private var _dataService:IReportDataService;

    private var _reportSelectionEnabled:Boolean = false;

    private var _reportSelectable:Boolean = false;

    private var pendingRequest:Boolean = false;

    private var htmlView:Boolean;

    public function DataViewFactory() {
        this.percentHeight = 100;
        this.percentWidth = 100;        
    }

    public function hideReport():void {
        if (htmlView) {
            currentComponent.visible = false;
        }
    }

    public function restoreReport():void {
        if (htmlView) {
            currentComponent.visible = true;
        }
    }

    [Bindable(event="reportSelectionEnabledChanged")]
    public function get reportSelectionEnabled():Boolean {
        return _reportSelectionEnabled;
    }

    public function set reportSelectionEnabled(value:Boolean):void {
        if (_reportSelectionEnabled == value) return;
        _reportSelectionEnabled = value;
        dispatchEvent(new Event("reportSelectionEnabledChanged"));
    }

    [Bindable(event="reportSelectableChanged")]
    public function get reportSelectable():Boolean {
        return _reportSelectable;
    }

    public function set reportSelectable(value:Boolean):void {
        if (_reportSelectable == value) return;
        _reportSelectable = value;
        dispatchEvent(new Event("reportSelectableChanged"));
    }

    public function noExplicitControlBarWidth():void {
        ReportControlBar(_controlBar).percentWidth = 100;
        ReportControlBar(_controlBar).width = NaN;
    }

    public function explicitControlBarWidth(width:int):void {
        ReportControlBar(_controlBar).percentWidth = NaN;
        ReportControlBar(_controlBar).width = width;
    }

    public function highlightDropAreas(analysisItem:AnalysisItem):void {
        var highlight:Boolean = _controlBar.highlight(analysisItem);
        if (highlight) {
            reportCanvas.highlight();
        }
        /*if (notConfigured.parent) {
            notConfigured.highlight(analysisItem);
        } else {

        }*/
    }

    public function revertDropAreas():void {
        _controlBar.normal();
        //notConfigured.normal();
        reportCanvas.normal();
    }

    public function set adHocMode(value:Boolean):void {
        _adHocMode = value;
    }

    public function set dataSourceID(value:int):void {
        _dataSourceID = value;
    }

    public function set availableFields(val:ArrayCollection):void {
        _availableFields = val;
    }

    public function set analysisDefinition(val:AnalysisDefinition):void {
        _analysisDefinition = val;
        if (_analysisDefinition is TopoMapDefinition) {
            htmlView = true;
        } else {
            htmlView = false;
        }
    }

    public function get updatedReport():AnalysisDefinition {
        return _controlBar.createAnalysisDefinition();
    }

    public function get analysisDefinition():AnalysisDefinition {
        return _analysisDefinition;
    }

    public function set reportDataService(val:Class):void {
        _reportDataService = val;
    }

    public function set reportControlBar(val:Class):void {
        _reportControlBar = val;
    }

    public function set reportRenderer(val:String):void {
        _reportRendererModule = val;
    }

    public function set newDefinition(val:Class):void {
        _newDefinition = val;
    }

    public function createReportControlBar():IReportControlBar {
        return new _reportControlBar();
    }

    public function get dropAreaControlBar():IReportControlBar {
        return _controlBar;
    }

    private var _controlBarWidth:int;


    [Bindable(event="controlBarWidthChanged")]
    public function get controlBarWidth():int {
        return _controlBarWidth;
    }

    public function set controlBarWidth(value:int):void {
        if (_controlBarWidth == value) return;
        _controlBarWidth = value;
        dispatchEvent(new Event("controlBarWidthChanged"));
    }

    private var showingAll:Boolean = false;

    private function onShowAll(event:ReportPagingEvent):void {
        showingAll = true;
        resultsUID = event.uid;
        forceRetrieve();
    }

    /*private var _dataSourceFields:ArrayCollection;

    public function set dataSourceFields(value:ArrayCollection):void {
        _dataSourceFields = value;
    }*/

    private function hideControlBar(event:MouseEvent):void {
        var button:Button = event.currentTarget as Button;
        if (showingControlBar) {
            button.label = "Show Fields";
            UIComponent(_controlBar).includeInLayout = false;
            UIComponent(_controlBar).visible = false;
        } else {
            button.label = "Hide";
            UIComponent(_controlBar).includeInLayout = true;
            UIComponent(_controlBar).visible = true;
        }
        showingControlBar = !showingControlBar;
    }

    private var showingControlBar:Boolean = true;

    override protected function createChildren():void {
        super.createChildren();

        _dataService = new _reportDataService();
        _dataService.addEventListener(DataServiceLoadingEvent.LOADING_STARTED, dataLoadingEvent, false, 0, true);
        _dataService.addEventListener(DataServiceLoadingEvent.LOADING_STOPPED, dataLoadingEvent, false, 0, true);
        _dataService.addEventListener(DataServiceEvent.DATA_RETURNED, gotData);

        addEventListener(ReportPagingEvent.SHOW_ALL, onShowAll);

        _controlBar = createReportControlBar();
        _controlBar["id"] = "_controlBar";
        _controlBar.analysisItems = _availableFields;
        //_controlBar.dataSourceFields = _dataSourceFields;
        _controlBar.dataSourceID = _dataSourceID;
        _controlBar.addEventListener(ReportDataEvent.REQUEST_DATA, onDataRequest, false, 0, true);
        _controlBar.addEventListener(CustomChangeEvent.CUSTOM_CHANGE, customChangeFromControlBar, false, 0, true);
        _controlBar.analysisDefinition = _analysisDefinition;
        if (Object(_controlBar).hasOwnProperty("feedMetadata")) {
            _controlBar["feedMetadata"] = _feedMetadata;
        }
        var box2:HBox = new HBox();

        box2.setStyle("verticalAlign", "middle");
        box2.setStyle("backgroundColor", 0xFFFFFF);
        box2.setStyle("borderStyle", "solid");
        box2.setStyle("borderThickness", 1);
        box2.setStyle("paddingLeft", 5);
        box2.setStyle("paddingRight", 5);
        box2.setStyle("paddingBottom", 5);
        box2.setStyle("paddingTop", 5);
        box2.percentWidth = 100;
        var collapseButton:Button = new Button();
        collapseButton.label = "Hide";
        collapseButton.addEventListener(MouseEvent.CLICK, hideControlBar);
        box2.addChild(collapseButton);
        addChild(box2);
        box2.addChild(_controlBar as DisplayObject);

        canvas = new Canvas();
        canvas.setStyle("borderStyle", "solid");
        canvas.setStyle("borderThickness", 1);
        canvas.setStyle("cornerRadius", 8);
        canvas.setStyle("dropShadowEnabled", true);
        canvas.setStyle("backgroundAlpha", 1);
        canvas.setStyle("backgroundColor", 0xFFFFFF);
        canvas.percentHeight = 100;
        canvas.percentWidth = 100;
        reportCanvas = new ReportCanvas();
        reportCanvas.addEventListener(DragEvent.DRAG_ENTER, dragEnterHandler);
        reportCanvas.addEventListener(DragEvent.DRAG_DROP, dragDropHandler);
        reportCanvas.addEventListener(DragEvent.DRAG_OVER, dragOverHandler);
        BindingUtils.bindProperty(reportCanvas, "loading", this, "showLoading");
        BindingUtils.bindProperty(reportCanvas, "overlayIndex", this, "overlayIndex");
        BindingUtils.bindProperty(reportCanvas, "stackTrace", this, "stackTrace");
        reportCanvas.x = 10;
        reportCanvas.y = 10;
        canvas.addChild(reportCanvas);
        addChild(canvas);
        //if (overlayIndex == 0) {

        if (!defaultManualRun) {
            noData = new NoData();
            currentComponent = noData;
            reportCanvas.addChildAt(noData, 0);
        } else {
            runReportCanvas = new RunReportCanvas();
            currentComponent = runReportCanvas;
            reportCanvas.addChildAt(runReportCanvas, 0);
        }

        //}
        notConfigured = new NotConfigured();
        //notConfigured.controlBar = _controlBar;
        loadReportRenderer();
    }

    private static function dragEnterHandler(event:DragEvent):void {
        var data:Object = event.dragSource.dataForFormat("treeDataGridItems");
        if (data != null) {
            var newItem:AnalysisItemWrapper = data[0];
            if (newItem.isAnalysisItem()) {
                DragManager.acceptDragDrop(IUIComponent(event.currentTarget));
            }
        }
    }

    private function dragDropHandler(event:DragEvent):void {
        event.preventDefault();
        var newItem:AnalysisItemWrapper = event.dragSource.dataForFormat("treeDataGridItems")[0];
        if (newItem.isAnalysisItem()) {
            _controlBar.addItem(newItem.analysisItem);
        }
    }

    protected static function dragOverHandler(event:DragEvent):void {
        DragManager.showFeedback(DragManager.MOVE);
    }

    override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
        super.updateDisplayList(unscaledWidth, unscaledHeight);
        if (reportCanvas != null && canvas != null && currentComponent != null) {
            reportCanvas.width = canvas.width - 20;
            reportCanvas.height = canvas.height - 20;
            /*if (currentComponent != null && (currentComponent.height != reportCanvas.height || currentComponent.width != reportCanvas.width)) {
                currentComponent.height = reportCanvas.height;
                currentComponent.width = reportCanvas.width;
                currentComponent.invalidateDisplayList();
            }*/
            if (currentComponent.height != reportCanvas.height || currentComponent.width != reportCanvas.width) {
                currentComponent.height = reportCanvas.height;
                currentComponent.width = reportCanvas.width;
                currentComponent.invalidateDisplayList();
            }
        }

    }

    private var noData:NoData;
    private var runReportCanvas:RunReportCanvas;

    private var notConfigured:NotConfigured;

    private var canvas:Canvas;

    private var reportCanvas:ReportCanvas;

    private var _showLoading:Boolean = false;
    
    private var _stackTrace:String;

    private var _overlayIndex:int;

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

    [Bindable(event="showLoadingChanged")]
    public function get showLoading():Boolean {
        return _showLoading;
    }

    public function set showLoading(value:Boolean):void {
        if (_showLoading == value) return;
        _showLoading = value;
        dispatchEvent(new Event("showLoadingChanged"));
    }

    public function cleanup():void {
        _dataService.removeEventListener(DataServiceLoadingEvent.LOADING_STARTED, dataLoadingEvent);
        _dataService.removeEventListener(DataServiceLoadingEvent.LOADING_STOPPED, dataLoadingEvent);
        _dataService.removeEventListener(DataServiceEvent.DATA_RETURNED, gotData);
        _controlBar.removeEventListener(ReportDataEvent.REQUEST_DATA, onDataRequest);
        _controlBar.removeEventListener(CustomChangeEvent.CUSTOM_CHANGE, customChangeFromControlBar);
        if (_reportRenderer != null) {
            _reportRenderer.removeEventListener(ReportRendererEvent.ADD_ITEM, onItemAdded);
            _reportRenderer.removeEventListener(ReportRendererEvent.FORCE_RENDER, forceRender);
            _reportRenderer.removeEventListener(CustomChangeEvent.CUSTOM_CHANGE, customChangeFromRenderer);
            if (UIComponent(_reportRenderer).parent) {
                reportCanvas.removeChild(_reportRenderer as DisplayObject);
            }
        }
    }

    private function dataLoadingEvent(event:DataServiceLoadingEvent):void {
        if (event.type == DataServiceLoadingEvent.LOADING_STARTED) overlayIndex = 1;
        showLoading = (event.type == DataServiceLoadingEvent.LOADING_STARTED);
        dispatchEvent(event);
    }

    private function onDataRequest(event:ReportDataEvent):void {
        if (event.reload || _lastData == null) {
            refresh();
        } else {
            _analysisDefinition = _controlBar.createAnalysisDefinition();
            if (_hasData) {
                showReport();
                _reportRenderer.renderReport(_lastData, _analysisDefinition, new Object(), _lastProperties);
            } else {
                showNoData();
            }

        }
    }

    public function rerender():void {
        if (_lastData == null) {
            refresh();
        } else {
            _analysisDefinition = _controlBar.createAnalysisDefinition();
            if (_hasData) {
                showReport();
                _reportRenderer.renderReport(_lastData, _analysisDefinition, new Object(), _lastProperties);
            } else {
                showNoData();
            }
        }
    }

    public function refresh():void {
        if (_adHocMode) {
            if (_reportRenderer == null) {
                pendingRequest = true;
            } else {
                _analysisDefinition = _controlBar.createAnalysisDefinition();
                if (_controlBar.isDataValid()) {
                    _analysisDefinition.createDefaultLimits();
                    _dataService.retrieveData(_analysisDefinition, false, createRequestParams());
                } else {
                    showNotConfigured();
                }
            }
        }
    }

    private var _feedMetadata:FeedMetadata;

    public function set feedMetadata(value:FeedMetadata):void {
        _feedMetadata = value;
    }

    public function forceRetrieve():void {
        if (_reportRenderer == null) {
            pendingRequest = true;
        } else {
            _analysisDefinition = _controlBar.createAnalysisDefinition();
            if (_controlBar.isDataValid()) {
                _analysisDefinition.createDefaultLimits();
                _dataService.retrieveData(_analysisDefinition, false, createRequestParams());
            } else {
                showNotConfigured();
            }
        }
    }

    private var resultsUID:String;

    private function createRequestParams():RequestParams {
        var requestParams:RequestParams = new RequestParams();
        requestParams.showAll = showingAll;
        requestParams.uid = resultsUID;
        return requestParams;
    }

    public function isDataValid():Boolean {
        return _controlBar.isDataValid();
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
        if (noData == null) {
            noData = new NoData();
        }
        if (!noData.parent) {
            if (currentComponent) {
                reportCanvas.removeChild(currentComponent);
            }
            currentComponent = noData;
            reportCanvas.addChildAt(noData, 0);
        }
    }

    public var defaultManualRun:Boolean;

    private function showNotConfigured():void {
        if (notConfigured == null) {
            notConfigured = new NotConfigured();
        }
        if (!notConfigured.parent) {
            if (currentComponent) {
                reportCanvas.removeChild(currentComponent);
            }
            currentComponent = notConfigured;
            reportCanvas.addChildAt(notConfigured, 0);
        }
    }

    private var _lastProperties:Object;
    
    private var _hasData:Boolean;

    private function gotData(event:DataServiceEvent):void {
        dispatchEvent(event);
        if (event.reportFault != null) {
            event.reportFault.popup(this, onProblem);
        } else {
            try {
                _controlBar.onDataReceipt(event);
                _lastData = event.dataSet;
                _lastProperties = event.additionalProperties;
                _hasData = event.hasData;
                overlayIndex = 0;

                if (event.hasData) {
                    showReport();
                    if (event.reportAudit != null && event.reportAudit.length > 0) {
                        _controlBar.createExplainButton();
                    } else {
                        _controlBar.deleteExplainButton();
                    }
                    if (event.report == null) {
                        //_analysisDefinition.renderConfig();
                        _reportRenderer.renderReport(event.dataSet, _analysisDefinition, new Object(), event.additionalProperties);
                    } else {
                        _reportRenderer.renderReport(event.dataSet, event.report, new Object(), event.additionalProperties);
                    }
                    _analysisDefinition.fieldEvents = event.events;
                    _analysisDefinition.filterEvents = event.filterEvents;
                } else {
                    showNoData();
                }
            } catch (e:Error) {
                stackTrace = e.getStackTrace();
                overlayIndex = 3;
            }
        }
    }

    private function onProblem(event:ProblemDataEvent):void {
        _dataService.retrieveData(_analysisDefinition, false, createRequestParams());
    }

    private var reportModuleLoader:ReportModuleLoader;

    private function loadReportRenderer():void {
        reportModuleLoader = new ReportModuleLoader();
        reportModuleLoader.addEventListener("moduleLoaded", reportLoadHandler);
        reportModuleLoader.loadReportRenderer(_reportRendererModule, reportCanvas);
    }
            
    private function reportLoadHandler(event:Event):void {
        _reportRenderer = reportModuleLoader.create() as IReportRenderer;
        //moduleInfo = null;
        if (_reportRenderer != null) {
            _reportRenderer.addEventListener(ReportRendererEvent.ADD_ITEM, onItemAdded, false, 0, true);
            _reportRenderer.addEventListener(ReportRendererEvent.REMOVE_ITEM, onItemRemoved, false, 0, true);
            _reportRenderer.addEventListener(ReportRendererEvent.FORCE_RENDER, forceRender, false, 0, true);
            _reportRenderer.addEventListener(CustomChangeEvent.CUSTOM_CHANGE, customChangeFromRenderer, false, 0, true);
            _reportRenderer.addEventListener(ReportWindowEvent.REPORT_WINDOW, onReportWindow, false, 0, true);
            _reportRenderer.addEventListener(ReportNavigationEvent.TO_REPORT, toReport, false, 0, true);
            _reportRenderer.addEventListener(AnalysisItemChangeEvent.ANALYSIS_ITEM_CHANGE, itemChange, false, 0, true);
            if (Object(_reportRenderer).hasOwnProperty("feedMetadata")) {
                _reportRenderer["feedMetadata"] = _feedMetadata;
            }
            if (_reportRenderer is ISelectableReportRenderer) {
                reportSelectable = true;
                reportWatcher = BindingUtils.bindProperty(_reportRenderer, "selectionEnabled", this, "reportSelectionEnabled");
            } else {
                reportSelectable = false;
            }
            _dataService.preserveValues = _reportRenderer.preserveValues();
            //reportCanvas.addChild(_reportRenderer as DisplayObject);
            if (pendingRequest) {
                pendingRequest = false;
                refresh();
            }
        }
    }

    private function itemChange(event:AnalysisItemChangeEvent):void {
        forceRetrieve();
    }

    private var reportWatcher:ChangeWatcher;

    private function toReport(event:ReportNavigationEvent):void {

    }

    private function onReportWindow(event:ReportWindowEvent):void {
        if (event.dataSourceID == 0) {
            event.dataSourceID = _dataSourceID;
        }
        ReportEventProcessor.fromEvent(event, this);
    }

    private function customChangeFromControlBar(event:CustomChangeEvent):void {
        _reportRenderer.onCustomChangeEvent(event);
    }    

    private function customChangeFromRenderer(event:CustomChangeEvent):void {
        _controlBar.onCustomChangeEvent(event);
    }

    private function forceRender(event:ReportRendererEvent):void {
        refresh();
    }

    private function onItemAdded(event:ReportRendererEvent):void {
        _controlBar.addItem(event.analysisItem);
    }

    private function onItemRemoved(event:ReportRendererEvent):void {
        EventDispatcher(_controlBar).dispatchEvent(event);
    }

    public function fromExistingDefinition(existingDefinition:AnalysisDefinition):AnalysisDefinition {
        if (getQualifiedClassName(existingDefinition) == getQualifiedClassName(_newDefinition)) {
            return existingDefinition;
        } else {
            var newDef:AnalysisDefinition = new _newDefinition();
            if (_explicitType > 0) {
                newDef.reportType = _explicitType;
            }
            var fields:ArrayCollection = existingDefinition.getFields();
            newDef.populate(fields);
            newDef.initialConfig();
            copyStandardData(existingDefinition, newDef);
            return newDef;
        }
    }

    private static function copyStandardData(source:AnalysisDefinition, target:AnalysisDefinition):void {
        target.name = source.name;
        target.dataFeedID = source.dataFeedID;
        target.analysisID = source.analysisID;
        target.dataScrubs = source.dataScrubs;
        target.dateCreated = source.dateCreated;
        target.filterDefinitions = source.filterDefinitions;
        target.policy = source.policy;
        target.addedItems = source.addedItems;
        target.canSaveDirectly = source.canSaveDirectly;
        target.description = source.description;
        target.marketplaceVisible = source.marketplaceVisible;
        target.accountVisible = source.accountVisible;
        target.solutionVisible = source.solutionVisible;
        target.publiclyVisible = source.publiclyVisible;
        target.temporaryReport = source.temporaryReport;
        target.fullJoins = source.fullJoins;
        target.optimized = source.optimized;
        target.dateUpdated = source.dateUpdated;
        target.fontName = source.fontName;
        target.fontSize = source.fontSize;
        target.marmotScript = source.marmotScript;
        target.reportRunMarmotScript = source.reportRunMarmotScript;
        target.joinOverrides = source.joinOverrides;
        target.folder = source.folder;
        target.urlKey = source.urlKey;
        target.cacheMinutes = source.cacheMinutes;
        target.cacheable = source.cacheable;
        target.addonReports = source.addonReports;
        target.baseDate = source.baseDate;
        target.dayAggregation = source.dayAggregation;
        target.enableLocalStorage = source.enableLocalStorage;
        target.aggregateQueryIfPossible = source.aggregateQueryIfPossible;
        target.publicWithKey = source.publicWithKey;
        target.manualButRunFirst = source.manualButRunFirst;
        target.adHocExecution = source.adHocExecution;
        target.generalSizeLimit = source.generalSizeLimit;

        copyPropertyIfExists(source, target, "multiColors");
        copyPropertyIfExists(source, target, "labelPosition");
        copyPropertyIfExists(source, target, "labelFontSize");
        copyPropertyIfExists(source, target, "labelFontWeight");
        copyPropertyIfExists(source, target, "labelInsideFontColor");
        copyPropertyIfExists(source, target, "labelOutsideFontColor");
        copyPropertyIfExists(source, target, "useInsideLabelFontColor");
        copyPropertyIfExists(source, target, "useOutsideLabelFontColor");
        copyPropertyIfExists(source, target, "chartColor");
        copyPropertyIfExists(source, target, "useChartColor");
        copyPropertyIfExists(source, target, "gradientColor");
        copyPropertyIfExists(source, target, "columnSort");
        copyPropertyIfExists(source, target, "showLabels");
        copyPropertyIfExists(source, target, "briefLabels");
        copyPropertyIfExists(source, target, "nowDate");
        copyPropertyIfExists(source, target, "previousDate");
        copyPropertyIfExists(source, target, "summaryTextColor");
        copyPropertyIfExists(source, target, "summaryBackgroundColor");
        copyPropertyIfExists(source, target, "headerTextColor");
        copyPropertyIfExists(source, target, "textColor");
        copyPropertyIfExists(source, target, "rowColor1");
        copyPropertyIfExists(source, target, "rowColor2");
        copyPropertyIfExists(source, target, "colorScheme");
        copyPropertyIfExists(source, target, "defaultMeasureAlignment");
        copyPropertyIfExists(source, target, "defaultGroupingAlignnment");
        copyPropertyIfExists(source, target, "defaultDateAlignment");
        copyPropertyIfExists(source, target, "hideNoData");
    }

    private static function copyPropertyIfExists(source:AnalysisDefinition, target:AnalysisDefinition, property:String):void {
        if (source.hasOwnProperty(property) && target.hasOwnProperty(property)) {
            target[property] = source[property];
        }
    }

    public function createNewDefinition():AnalysisDefinition {
        _analysisDefinition = new _newDefinition();
        if (_explicitType > 0) {
            _analysisDefinition.reportType = _explicitType;
        }
        return _analysisDefinition;
    }

    private var _explicitType:int;


    public function set explicitType(value:int):void {
        _explicitType = value;
    }

    public function createFilterRawData():FilterRawData {
        return ISelectableReportRenderer(_reportRenderer).createFilterRawData();
    }

    public function invalidateItems(invalidAnalysisItemIDs:ArrayCollection):void {
    }

    public function updateExportMetadata():void {
        _reportRenderer.updateExportMetadata(_analysisDefinition);
    }

    public function getCoreView():DisplayObject {
        return _reportRenderer as DisplayObject;
    }

    public function addItem(analysisItem:AnalysisItem):void {
        _controlBar.addItem(analysisItem);
    }}
}