package com.easyinsight.dashboard {
import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.EmbeddedControllerLookup;
import com.easyinsight.analysis.EmbeddedDataServiceEvent;
import com.easyinsight.analysis.EmbeddedViewFactory;
import com.easyinsight.analysis.IEmbeddedReportController;
import com.easyinsight.analysis.PopupMenuFactory;
import com.easyinsight.filtering.FilterDefinition;
import com.easyinsight.filtering.TransformContainer;
import com.easyinsight.filtering.TransformsUpdatedEvent;
import com.easyinsight.report.ReportSetupEvent;

import flash.events.Event;

import mx.collections.ArrayCollection;
import mx.containers.Box;
import mx.containers.Canvas;
import mx.containers.VBox;
import mx.controls.Label;

public class DashboardReportViewComponent extends VBox implements IDashboardViewComponent  {

    public var dashboardReport:DashboardReport;
    private var viewFactory:EmbeddedViewFactory;



    private var report:AnalysisDefinition;

    public function DashboardReportViewComponent() {
        super();
        percentWidth = 100;
        percentHeight = 100;
        //setStyle("cornerRadius", 10);
        setStyle("horizontalAlign", "center");
        setStyle("verticalAlign", "middle");
        setStyle("paddingLeft", 15);
        setStyle("paddingRight", 15);
        setStyle("paddingTop", 15);
        setStyle("paddingBottom", 15);
        setStyle("backgroundColor", 0xFFFFFF);
        setStyle("backgroundAlpha", 1);
    }

    public function obtainPreferredSizeInfo():SizeInfo {
        return new SizeInfo(dashboardReport.preferredWidth, dashboardReport.preferredHeight);
    }

    protected override function createChildren():void {
        super.createChildren();
        if (dashboardEditorMetadata.borderThickness > 0) {
            setStyle("borderStyle", "solid");
            setStyle("borderThickness", dashboardEditorMetadata.borderThickness);
            setStyle("borderColor", dashboardEditorMetadata.borderColor);
        }
        var controllerClass:Class = EmbeddedControllerLookup.controllerForType(dashboardReport.report.reportType);
        var controller:IEmbeddedReportController = new controllerClass();
        viewFactory = controller.createEmbeddedView();
        viewFactory.reportID = dashboardReport.report.id;
        viewFactory.dataSourceID = dashboardReport.report.dataFeedID;
        if (dashboardReport.showLabel) {
            var vbox:VBox = new VBox();
            vbox.percentHeight = 100;
            vbox.percentWidth = 100;
            vbox.setStyle("horizontalAlign", "center");
            addChild(vbox);
            var label:Label = new Label();
            label.text = dashboardReport.report.name;
            vbox.addChild(label);
            vbox.addChild(viewFactory);
        } else {
            addChild(viewFactory);
        }

        viewFactory.addEventListener(ReportSetupEvent.REPORT_SETUP, onReportSetup);
        viewFactory.addEventListener(EmbeddedDataServiceEvent.DATA_RETURNED, onData);
        viewFactory.setup();
        viewFactory.contextMenu = PopupMenuFactory.reportFactory.createReportContextMenu(dashboardReport.report, viewFactory, this);
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

    private var transformContainer:TransformContainer;

    public var elementID:String;

    private function transformsUpdated(event:Event):void {
        filterMap[elementID] = transformContainer.getFilterDefinitions();
        updateAdditionalFilters(filterMap);
        refresh();
    }

    public var dashboardEditorMetadata:DashboardEditorMetadata;

    public function toggleFilters(showFilters:Boolean):void {
        if (transformContainer != null) {
            if (showFilters) {
                removeChild(transformContainer);
            } else {
                addChildAt(transformContainer, 0);
            }
        }
    }

    private function onReportSetup(event:ReportSetupEvent):void {
        var filterDefinitions:ArrayCollection = event.reportInfo.report.filterDefinitions;
        //viewFactory.filterDefinitions = filterDefinitions;
        if (event.reportInfo.report.filterDefinitions.length > 0) {
            var parentFilters:ArrayCollection = createAdditionalFilters(filterMap);
            transformContainer = new TransformContainer();
            /*transformContainer.setStyle("borderStyle", dashboardStack.filterBorderStyle);
            transformContainer.setStyle("borderColor", dashboardStack.filterBorderColor);
            transformContainer.setStyle("backgroundColor", dashboardStack.filterBackgroundColor);
            transformContainer.setStyle("backgroundAlpha", dashboardStack.filterBackgroundAlpha);*/
            transformContainer.filterEditable = false;
            var myFilterColl:ArrayCollection = new ArrayCollection();
            for each (var filterDefinition:FilterDefinition in filterDefinitions) {
                var exists:Boolean = false;
                for each (var existing:FilterDefinition in parentFilters) {
                    if (existing.getType() == filterDefinition.getType()) {
                        exists = true;
                    }
                }
                if (exists) {
                    continue;
                }
                myFilterColl.addItem(filterDefinition);
            }
            if (myFilterColl.length > 0) {
                transformContainer.existingFilters = myFilterColl;
                filterMap[elementID] = myFilterColl;
                updateAdditionalFilters(filterMap);
                transformContainer.percentWidth = 100;
                transformContainer.setStyle("paddingLeft", 10);
                transformContainer.setStyle("paddingRight", 10);
                transformContainer.setStyle("paddingTop", 10);
                transformContainer.setStyle("paddingBottom", 10);
                transformContainer.reportView = true;
                transformContainer.feedID = dashboardEditorMetadata.dataSourceID;
                transformContainer.role = dashboardEditorMetadata.role;
                transformContainer.addEventListener(TransformsUpdatedEvent.UPDATED_TRANSFORMS, transformsUpdated);
                addChildAt(transformContainer, 0);
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