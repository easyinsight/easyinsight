package com.easyinsight.pseudocontext {
import com.easyinsight.analysis.ActualRowExecutor;
import com.easyinsight.analysis.AnalysisDateDimension;
import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.AnalysisItemChangeEvent;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.AnalysisItemTypes;
import com.easyinsight.analysis.DrillThrough;
import com.easyinsight.analysis.DrillThroughEvent;
import com.easyinsight.analysis.DrillThroughExecutor;
import com.easyinsight.analysis.ReportWindowEvent;
import com.easyinsight.analysis.Link;
import com.easyinsight.analysis.URLLink;
import com.easyinsight.report.ReportNavigationEvent;
import com.easyinsight.rowedit.ActualRowEvent;
import com.easyinsight.solutions.InsightDescriptor;

import flash.display.InteractiveObject;
import flash.events.ContextMenuEvent;
import flash.events.Event;
import flash.net.URLRequest;
import flash.net.navigateToURL;
import flash.system.System;
import flash.ui.ContextMenu;
import flash.ui.ContextMenuItem;

import mx.collections.ArrayCollection;
import mx.formatters.Formatter;

public class StandardContextWindow {

    private var items:Array;

    private var analysisItem:AnalysisItem;

    private var passthroughFunction:Function;
    private var passthroughObject:InteractiveObject;
    private var data:Object;
    private var filterDefinitions:ArrayCollection;
    private var copyText:String;

    private var altKey:String;
    
    private var report:AnalysisDefinition;

    public function StandardContextWindow(analysisItem:AnalysisItem, passthroughFunction:Function, passthroughObject:InteractiveObject, data:Object,
                                          report:AnalysisDefinition, 
                                          includeDrills:Boolean = true, filterDefinitions:ArrayCollection = null, copyText:String = null, additionalOptions:Array = null,
            altKey:String = null) {
        super();
        this.analysisItem = analysisItem;
        this.passthroughFunction = passthroughFunction;
        this.report = report;
        this.passthroughObject = passthroughObject;
        this.filterDefinitions = filterDefinitions;
        this.data = data;
        this.copyText = copyText;
        this.altKey = altKey;
        items = [];
        if (analysisItem is AnalysisDateDimension) {
            var date:AnalysisDateDimension = analysisItem as AnalysisDateDimension;
            if (date.dateLevel == AnalysisItemTypes.YEAR_LEVEL) {
                items.push(defineDateLink(AnalysisItemTypes.QUARTER_OF_YEAR, "Quarter of Year"));
                items.push(defineDateLink(AnalysisItemTypes.MONTH_LEVEL, "Month of Year"));
                items.push(defineDateLink(AnalysisItemTypes.WEEK_LEVEL, "Week of Year"));
            } else if (date.dateLevel == AnalysisItemTypes.QUARTER_OF_YEAR) {
                items.push(defineDateLink(AnalysisItemTypes.YEAR_LEVEL, "Year"));
                items.push(defineDateLink(AnalysisItemTypes.MONTH_LEVEL, "Month of Year"));
            } else if (date.dateLevel == AnalysisItemTypes.MONTH_LEVEL) {
                items.push(defineDateLink(AnalysisItemTypes.YEAR_LEVEL, "Year"));
                items.push(defineDateLink(AnalysisItemTypes.QUARTER_OF_YEAR, "Quarter of Year"));
                items.push(defineDateLink(AnalysisItemTypes.WEEK_LEVEL, "Week of Year"));
            } else if (date.dateLevel == AnalysisItemTypes.WEEK_LEVEL) {
                items.push(defineDateLink(AnalysisItemTypes.YEAR_LEVEL, "Year"));
                items.push(defineDateLink(AnalysisItemTypes.QUARTER_OF_YEAR, "Quarter of Year"));
                items.push(defineDateLink(AnalysisItemTypes.MONTH_LEVEL, "Month of Year"));
                items.push(defineDateLink(AnalysisItemTypes.DAY_LEVEL, "Day of Year"));
            } else if (date.dateLevel == AnalysisItemTypes.DAY_LEVEL) {
                items.push(defineDateLink(AnalysisItemTypes.MONTH_LEVEL, "Month of Year"));
                items.push(defineDateLink(AnalysisItemTypes.WEEK_LEVEL, "Week of Year"));
            }
        }
        //
        var copyItem:ContextMenuItem = new ContextMenuItem("Copy Value");
        copyItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, copyValue);
        items.push(copyItem);
        var exportItem:ContextMenuItem = new ContextMenuItem("Export Report");
        exportItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, exportReport);
        items.push(exportItem);
        if (analysisItem.links.length > 0) {
            for each (var link:Link in analysisItem.links) {
                composeLink(link);
            }
        }
        if (additionalOptions != null) {
            for each (var additionalItem:ContextMenuItem in additionalOptions) {
                items.push(additionalItem);
            }
        }
        if (report != null && report.rowsEditable) {
            var allDataItem:ContextMenuItem = new ContextMenuItem("Edit Rows...");
            allDataItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, function (event:ContextMenuEvent):void {
                var executor:ActualRowExecutor = new ActualRowExecutor(data, analysisItem, report);
                executor.addEventListener(ActualRowEvent.ACTUAL_ROW_DATA, onActualRowData);
                executor.send();
            });
            items.push(allDataItem);
        }
        var menu:ContextMenu = new ContextMenu();
        menu.hideBuiltInItems();
        menu.customItems = items;
        passthroughObject.contextMenu = menu;
    }

    private function onActualRowData(event:ActualRowEvent):void {
        passthroughFunction.call(passthroughObject, event);
    }

    private function defineDateLink(targetLevel:int, label:String):ContextMenuItem {
        var item:ContextMenuItem = new ContextMenuItem(label);
        item.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, function(event:ContextMenuEvent):void {
            var date:AnalysisDateDimension = analysisItem as AnalysisDateDimension;
            date.dateLevel = targetLevel;
            passthroughFunction.call(passthroughObject, new AnalysisItemChangeEvent(date));
        });
        return item;
    }

    private function composeLink(link:Link):void {
        if (link is URLLink) {
            var url:URLLink = link as URLLink;
            var urlContextItem:ContextMenuItem = new ContextMenuItem(url.label);
            urlContextItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, function(event:ContextMenuEvent):void {
                var key:String = altKey != null ? altKey : "";
                var url:String = data[key + link.label + "_link"];
                navigateToURL(new URLRequest(url), "_blank");
            });
            items.push(urlContextItem);
        } else if (link is DrillThrough) {
            var drillThrough:DrillThrough = link as DrillThrough;
            var drillContextItem:ContextMenuItem = new ContextMenuItem(drillThrough.label);
            drillContextItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, function (event:ContextMenuEvent):void {
                var executor:DrillThroughExecutor = new DrillThroughExecutor(drillThrough, data, analysisItem, report);
                executor.addEventListener(DrillThroughEvent.DRILL_THROUGH, onDrill);
                executor.send();
            });
            items.push(drillContextItem);
        }
    }

    private function onDrill(event:DrillThroughEvent):void {
        if (event.drillThrough.miniWindow) {
            onReport(new ReportWindowEvent(event.drillThroughResponse.descriptor.id, 0, 0, event.drillThroughResponse.filters,
                    InsightDescriptor(event.drillThroughResponse.descriptor).dataFeedID,
                    InsightDescriptor(event.drillThroughResponse.descriptor).reportType));
        } else {
            onReport(new ReportNavigationEvent(ReportNavigationEvent.TO_REPORT, event.drillThroughResponse.descriptor,
                    event.drillThroughResponse.filters));
        }

    }

    /*private function onRollup(event:ContextMenuEvent):void {
        var hierarchyItem:AnalysisHierarchyItem = analysisItem as AnalysisHierarchyItem;
        var index:int = hierarchyItem.hierarchyLevels.getItemIndex(hierarchyItem.hierarchyLevel);
        if (index > 0) {
            hierarchyItem.hierarchyLevel = hierarchyItem.hierarchyLevels.getItemAt(index - 1) as HierarchyLevel;
            passthroughFunction.call(passthroughObject, new HierarchyRollupEvent(hierarchyItem.hierarchyLevel.analysisItem, hierarchyItem, index - 1));
        }
    }

    private function drill(event:ContextMenuEvent):void {
        var hierarchyItem:AnalysisHierarchyItem = analysisItem as AnalysisHierarchyItem;
        var index:int = hierarchyItem.hierarchyLevels.getItemIndex(hierarchyItem.hierarchyLevel);
        if (index < (hierarchyItem.hierarchyLevels.length - 1)) {
            var dataField:String = analysisItem.qualifiedName();
            var dataString:String = data[dataField];
            var filterRawData:FilterRawData = new FilterRawData();
            filterRawData.addPair(hierarchyItem.hierarchyLevel.analysisItem, dataString);
            hierarchyItem.hierarchyLevel = hierarchyItem.hierarchyLevels.getItemAt(index + 1) as HierarchyLevel;
            passthroughFunction.call(passthroughObject, new HierarchyDrilldownEvent(HierarchyDrilldownEvent.DRILLDOWN, filterRawData,
                    hierarchyItem, index + 1));
        }
    }*/

    private function onReport(event:Event):void {
        passthroughFunction.call(passthroughObject, event);
    }

    private function exportReport(event:ContextMenuEvent):void {
        passthroughFunction.call(passthroughObject, new Event("export", true));
    }

    private function copyValue(event:ContextMenuEvent):void {
        var text:String;
        if (copyText == null) {
            var field:String = analysisItem.qualifiedName();
            var formatter:Formatter = analysisItem.getFormatter();
            var objVal:Object = data[field];

            if (objVal == null) {
                text = "";
            } else {
                text = formatter.format(objVal);
            }
        } else {
            text = copyText;
        }
        System.setClipboard(text);
    }
}
}