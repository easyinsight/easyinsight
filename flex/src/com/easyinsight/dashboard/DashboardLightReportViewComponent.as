package com.easyinsight.dashboard {
import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.EmbeddedControllerLookup;
import com.easyinsight.analysis.EmbeddedDataServiceEvent;
import com.easyinsight.analysis.EmbeddedViewFactory;
import com.easyinsight.analysis.IEmbeddedReportController;
import com.easyinsight.analysis.PopupMenuFactory;
import com.easyinsight.analysis.list.SizeOverrideEvent;
import com.easyinsight.filtering.FilterDefinition;
import com.easyinsight.filtering.TransformContainer;
import com.easyinsight.filtering.TransformsUpdatedEvent;
import com.easyinsight.report.EmbedReportContextMenuFactory;
import com.easyinsight.report.ReportSetupEvent;
import com.easyinsight.util.SaveButton;

import flash.events.Event;
import flash.events.MouseEvent;

import mx.collections.ArrayCollection;
import mx.containers.Canvas;
import mx.containers.HBox;
import mx.containers.VBox;
import mx.controls.Label;

public class DashboardLightReportViewComponent extends Canvas implements IDashboardViewComponent  {

    public var dashboardReport:DashboardReport;
    private var viewFactory:EmbeddedViewFactory;

    private var report:AnalysisDefinition;

    public function DashboardLightReportViewComponent() {
        super();        
        //setStyle("cornerRadius", 10);
        setStyle("horizontalAlign", "center");
        setStyle("verticalAlign", "middle");
        setStyle("verticalGap", 0);
        setStyle("paddingLeft", 0);
        setStyle("paddingRight", 0);
        setStyle("paddingTop", 0);
        setStyle("paddingBottom", 0);
        setStyle("backgroundColor", 0xFFFFFF);
        setStyle("backgroundAlpha", 1);
    }

    public function obtainPreferredSizeInfo():SizeInfo {
        return new SizeInfo(dashboardReport.preferredWidth, alteredHeight == -1 ? dashboardReport.preferredHeight : alteredHeight, dashboardReport.autoCalculateHeight);
    }
    
    private var alteredHeight:int = -1;

    protected override function createChildren():void {
        super.createChildren();
        if (dashboardEditorMetadata.borderThickness == 0) {
            horizontalScrollPolicy = "off";
            verticalScrollPolicy = "off";
        }
        var sizeInfo:SizeInfo = obtainPreferredSizeInfo();
        if (sizeInfo.preferredWidth > 0) {
            width = dashboardReport.preferredWidth;
        } else {
            percentWidth = 100;
        }
        if (sizeInfo.preferredHeight > 0) {
            height = dashboardReport.preferredHeight;
        } else {
            percentHeight = 100;
        }
        /*if (dashboardEditorMetadata.borderThickness > 0) {
            setStyle("borderStyle", "inset");
            setStyle("borderThickness", 3);
            setStyle("borderColor", 0x00000);
        }*/
        var controllerClass:Class = EmbeddedControllerLookup.controllerForType(dashboardReport.report.reportType);
        var controller:IEmbeddedReportController = new controllerClass();
        viewFactory = controller.createEmbeddedView();
        viewFactory.styleCanvas = false;
        viewFactory.usePreferredHeight = dashboardReport.autoCalculateHeight;
        viewFactory.reportID = dashboardReport.report.id;
        viewFactory.dataSourceID = dashboardEditorMetadata.dataSourceID;
        viewFactory.dashboardID = dashboardEditorMetadata.dashboardID;
        viewFactory.spaceSides = dashboardReport.spaceSides;
        addChild(viewFactory);

        viewFactory.addEventListener(ReportSetupEvent.REPORT_SETUP, onReportSetup);
        viewFactory.addEventListener(EmbeddedDataServiceEvent.DATA_RETURNED, onData);
        viewFactory.addEventListener(SizeOverrideEvent.SIZE_OVERRIDE, sizeOverride);
        viewFactory.setup();
        if (dashboardEditorMetadata.fixedID) {
            viewFactory.contextMenu = new EmbedReportContextMenuFactory().createReportContextMenu(dashboardReport.report, viewFactory, this);
        } else {
            viewFactory.contextMenu = PopupMenuFactory.reportFactory.createReportContextMenu(dashboardReport.report, viewFactory, this);
        }

    }

    private function sizeOverride(event:SizeOverrideEvent):void {
        event.stopPropagation();
        if (event.height != -1) {
            var h:int = event.height + 20;
            this.height = h;
            alteredHeight = h;
            dispatchEvent(new SizeOverrideEvent(-1, h));
        }
    }


    private var filterMap:Object = new Object();

    public function updateAdditionalFilters(filterMap:Object):void {
        if (filterMap != null) {
            for (var id:String in filterMap) {
                var filters:Object = filterMap[id];
                if (filters != null) {
                    this.filterMap[id] = filters;
                }
            }
        }
    }

    private var setup:Boolean;

    private var queued:Boolean;

    private function onData(event:EmbeddedDataServiceEvent):void {
        this.report = event.analysisDefinition;
    }

    private function createAdditionalFilters(filterMap:Object):ArrayCollection {
        var filterColl:ArrayCollection = new ArrayCollection();
        for (var id:String in filterMap) {
            var filters:Object = filterMap[id];
            if (filters != null) {
                var filterList:ArrayCollection = filters as ArrayCollection;
                for each (var filter:FilterDefinition in filterList) {
                    filterColl.addItem(filter);
                }
            }
        }
        return filterColl;
    }

    public var elementID:String;

    private function transformsUpdated(event:Event):void {
        updateAdditionalFilters(filterMap);
        refresh();
    }

    public var dashboardEditorMetadata:DashboardEditorMetadata;

    public function toggleFilters(showFilters:Boolean):void {
    }

    private var hasFilters:Boolean = false;

    private function onReportSetup(event:ReportSetupEvent):void {
        var filterDefinitions:ArrayCollection = event.reportInfo.report.filterDefinitions;
        //viewFactory.filterDefinitions = filterDefinitions;
        if (event.reportInfo.report.filterDefinitions.length > 0) {

            var parentFilters:ArrayCollection = createAdditionalFilters(filterMap);

            var myFilterColl:ArrayCollection = new ArrayCollection();
            var visibleFilters:int = 0;
            for each (var filterDefinition:FilterDefinition in filterDefinitions) {
                var exists:Boolean = false;
                for each (var existing:FilterDefinition in parentFilters) {
                    if (existing.qualifiedName() == filterDefinition.qualifiedName()) {
                        exists = true;
                    }
                }
                if (exists) {
                    continue;
                }
                if (filterDefinition.showOnReportView) {
                    visibleFilters++;
                }
                myFilterColl.addItem(filterDefinition);
            }
        }
        viewFactory.additionalFilterDefinitions = createAdditionalFilters(filterMap);
        setup = true;
        if (queued) {
            queued = false;
            retrievedDataOnce = true;
            viewFactory.refresh();
        }
    }

    private function runReport(event:MouseEvent):void {
        viewFactory.forceRetrieve();
    }

    public function refresh():void {
        if (setup) {
            retrievedDataOnce = true;
            viewFactory.additionalFilterDefinitions = createAdditionalFilters(filterMap);
            viewFactory.refresh();
        } else {
            queued = true;
        }
    }

    private var retrievedDataOnce:Boolean;

    public function initialRetrieve():void {
        if (!retrievedDataOnce) {
            if (setup) {
                retrievedDataOnce = true;
                viewFactory.additionalFilterDefinitions = createAdditionalFilters(filterMap);
                viewFactory.refresh();
            } else {
                queued = true;
            }
        }
    }

    public function reportCount():ArrayCollection {
        var reports:ArrayCollection = new ArrayCollection();
        reports.addItem(report);
        return reports;
    }
}
}