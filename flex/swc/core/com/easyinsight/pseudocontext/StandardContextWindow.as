package com.easyinsight.pseudocontext {
import com.easyinsight.analysis.AnalysisHierarchyItem;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.CustomCodeLink;
import com.easyinsight.analysis.DrillThrough;
import com.easyinsight.analysis.DrillThroughEvent;
import com.easyinsight.analysis.DrillThroughExecutor;
import com.easyinsight.analysis.ReportWindowEvent;
import com.easyinsight.analysis.HierarchyDrilldownEvent;
import com.easyinsight.analysis.HierarchyLevel;
import com.easyinsight.analysis.HierarchyRollupEvent;
import com.easyinsight.analysis.Link;
import com.easyinsight.analysis.URLLink;
import com.easyinsight.filtering.FilterRawData;
import com.easyinsight.filtering.FilterValueDefinition;
import com.easyinsight.report.ReportNavigationEvent;

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

    public function StandardContextWindow(analysisItem:AnalysisItem, passthroughFunction:Function, passthroughObject:InteractiveObject, data:Object,
                                          includeDrills:Boolean = true) {
        super();
        this.analysisItem = analysisItem;
        this.passthroughFunction = passthroughFunction;
        this.passthroughObject = passthroughObject;
        this.data = data;
        items = [];
        if (analysisItem is AnalysisHierarchyItem) {
            var hierarchy:AnalysisHierarchyItem = analysisItem as AnalysisHierarchyItem;
            if (includeDrills) {
                var index:int = hierarchy.hierarchyLevels.getItemIndex(hierarchy.hierarchyLevel);
                if (index < (hierarchy.hierarchyLevels.length - 1)) {
                    var drilldownContextItem:ContextMenuItem = new ContextMenuItem("Drilldown");
                    drilldownContextItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, drill);
                    items.push(drilldownContextItem);
                }
                if (index > 0) {
                    var rollupItem:ContextMenuItem = new ContextMenuItem("Rollup");
                    rollupItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, onRollup);
                    items.push(rollupItem);
                }
            }
            for each (var level:HierarchyLevel in hierarchy.hierarchyLevels) {
                var childItem:AnalysisItem = level.analysisItem;
                if (data[childItem.qualifiedName()]) {
                    for each (var hierarchyLink:Link in childItem.links) {
                        composeLink(hierarchyLink);
                    }
                }
            }
        }
        var copyItem:ContextMenuItem = new ContextMenuItem("Copy Value");
        copyItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, copyValue);
        items.push(copyItem);
        if (analysisItem.links.length > 0) {
            for each (var link:Link in analysisItem.links) {
                composeLink(link);
            }
        }
        var menu:ContextMenu = new ContextMenu();
        menu.hideBuiltInItems();
        menu.customItems = items;
        passthroughObject.contextMenu = menu;
    }

    private function composeLink(link:Link):void {
        if (link is URLLink) {
            var url:URLLink = link as URLLink;
            var urlContextItem:ContextMenuItem = new ContextMenuItem(url.label);
            urlContextItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, function(event:ContextMenuEvent):void {

                var url:String = data[link.label + "_link"];
                navigateToURL(new URLRequest(url), "_blank");
            });
            items.push(urlContextItem);
        } else if (link is DrillThrough) {
            var drillThrough:DrillThrough = link as DrillThrough;
            var drillContextItem:ContextMenuItem = new ContextMenuItem(drillThrough.label);
            drillContextItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, function (event:ContextMenuEvent):void {
                var executor:DrillThroughExecutor = new DrillThroughExecutor(drillThrough);
                executor.addEventListener(DrillThroughEvent.DRILL_THROUGH, onDrill);
                executor.send();
            });
            items.push(drillContextItem);
        } else if (link is CustomCodeLink) {
            var customCodeLink:CustomCodeLink = link as CustomCodeLink;
            var codeLinkItem:ContextMenuItem = new ContextMenuItem(customCodeLink.label);
            codeLinkItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, function (event:ContextMenuEvent):void {
                passthroughObject.dispatchEvent(customCodeLink.createEvent(data[analysisItem.qualifiedName()]));
            });
            items.push(codeLinkItem);
        }
    }

    private function onDrill(event:DrillThroughEvent):void {
        var filterDefinition:FilterValueDefinition = new FilterValueDefinition();
        filterDefinition.field = analysisItem;
        filterDefinition.filteredValues = new ArrayCollection([data[analysisItem.qualifiedName()]]);
        filterDefinition.enabled = true;
        filterDefinition.inclusive = true;
        var filters:ArrayCollection = new ArrayCollection([ filterDefinition ]);
        if (event.drillThrough.miniWindow) {
            onReport(new ReportWindowEvent(event.report.id, 0, 0, filters, event.report.dataFeedID, event.report.reportType));
        } else {
            onReport(new ReportNavigationEvent(ReportNavigationEvent.TO_REPORT, event.report, filters));
        }

    }

    private function onRollup(event:ContextMenuEvent):void {
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
    }

    private function onReport(event:Event):void {
        passthroughFunction.call(passthroughObject, event);
    }

    private function copyValue(event:ContextMenuEvent):void {
        var field:String = analysisItem.qualifiedName();
        var formatter:Formatter = analysisItem.getFormatter();
        var objVal:Object = data[field];
        var text:String;
        if (objVal == null) {
            text = "";
        } else {
            text = formatter.format(objVal);
        }
        System.setClipboard(text);
    }
}
}