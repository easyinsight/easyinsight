package com.easyinsight.scorecard {
import com.easyinsight.analysis.list.ListDefinition;
import com.easyinsight.genredata.AnalyzeEvent;
import com.easyinsight.kpi.KPI;
import com.easyinsight.listing.ReportEditorAnalyzeSource;
import com.easyinsight.report.ReportAnalyzeSource;
import com.easyinsight.solutions.InsightDescriptor;

import flash.events.ContextMenuEvent;
import flash.ui.ContextMenu;
import flash.ui.ContextMenuItem;

import mx.collections.ArrayCollection;

public class ScorecardContextWindow {

    private var items:Array;

    private var kpi:KPI;

    private var passthroughFunction:Function;
    private var passthroughObject:Object;

    public function ScorecardContextWindow(kpi:KPI, passthroughFunction:Function, passthroughObject:Object, otherItems:Array = null) {
        super();
        this.kpi = kpi;
        this.passthroughFunction = passthroughFunction;
        this.passthroughObject = passthroughObject;
        items = [];
        if (otherItems != null) {
            for each (var contextItem:ContextMenuItem in otherItems) {
                items.push(contextItem);
            }
        }
        if (kpi != null) {
            var copyItem:ContextMenuItem = new ContextMenuItem("Analyze the KPI...");
            copyItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, analyzeKPI);
            items.push(copyItem);
            if (kpi.reports != null && kpi.reports.length > 0) {

                for each (var report:InsightDescriptor in kpi.reports) {
                    var reportContextItem:ContextMenuItem = new ContextMenuItem(report.name);
                    reportContextItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, createReport(report, kpi.filters));
                    items.push(reportContextItem);
                }
            }
        }
        var menu:ContextMenu = new ContextMenu();
        menu.hideBuiltInItems();
        menu.customItems = items;
        passthroughObject.contextMenu = menu; 
    }

    private function createReport(report:InsightDescriptor, filters:ArrayCollection):Function {
        return function(event:ContextMenuEvent):void {
            passthroughFunction.call(passthroughObject, new AnalyzeEvent(new ReportAnalyzeSource(report, filters)));
        };
    }

    /*private function findReports(event:ContextMenuEvent):void {
        passthroughFunction.call(passthroughObject, new NavigationEvent("Exchange", null, {viewMode: 1, displayMode: 0, subTopicID: kpi.connectionID}));
    }*/

    private function analyzeKPI(event:ContextMenuEvent):void {
        var report:ListDefinition = new ListDefinition();
        report.filterDefinitions = kpi.filters;
        report.canSaveDirectly = true;
        report.dataFeedID = kpi.coreFeedID;
        report.columns = new ArrayCollection([ kpi.analysisMeasure ]);
        report.name = kpi.name;
        passthroughFunction.call(passthroughObject, new AnalyzeEvent(new ReportEditorAnalyzeSource(report)));
    }
}
}