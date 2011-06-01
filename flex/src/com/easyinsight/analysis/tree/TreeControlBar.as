package com.easyinsight.analysis.tree {
import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.AnalysisItemTypes;
import com.easyinsight.analysis.AnalysisItemUpdateEvent;
import com.easyinsight.analysis.CustomChangeEvent;
import com.easyinsight.analysis.DataServiceEvent;
import com.easyinsight.analysis.FeedMetadata;
import com.easyinsight.analysis.HierarchyDropArea;
import com.easyinsight.analysis.IReportControlBar;
import com.easyinsight.analysis.ListDropArea;
import com.easyinsight.analysis.ListDropAreaGrouping;
import com.easyinsight.analysis.ReportControlBar;
import com.easyinsight.analysis.ReportDataEvent;
import com.easyinsight.analysis.list.EnableLookupEditingEvent;

import flash.events.MouseEvent;

import mx.collections.ArrayCollection;

import mx.controls.Button;
import mx.controls.Label;

public class TreeControlBar extends ReportControlBar implements IReportControlBar {
    private var hierarchyGrouping:ListDropAreaGrouping;
    private var itemGrouping:ListDropAreaGrouping;
    private var treeDefinition:TreeDefinition;

    public function TreeControlBar() {
        hierarchyGrouping = new ListDropAreaGrouping();
        hierarchyGrouping.maxElements = 1;
        hierarchyGrouping.dropAreaType = HierarchyDropArea;
        hierarchyGrouping.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, requestListData);

        itemGrouping = new ListDropAreaGrouping();
        itemGrouping.unlimited = true;
        itemGrouping.dropAreaType = ListDropArea;
        itemGrouping.addEventListener(AnalysisItemUpdateEvent.ANALYSIS_LIST_UPDATE, requestListData);
    }

    [Embed(source="../../../../../assets/tables_x16.png")]
    public var lookupTableIcon:Class;

    private function onUpdate(event:AnalysisItemUpdateEvent):void {
        dispatchEvent(new ReportDataEvent(ReportDataEvent.REQUEST_DATA));
    }

    private var _feedMetadata:FeedMetadata;

    public function set feedMetadata(value:FeedMetadata):void {
        _feedMetadata = value;
    }

    override protected function createChildren():void {
        super.createChildren();
        if (_feedMetadata.lookupTables.length > 0) {
            var lookupTableButton:Button = new Button();
            lookupTableButton.setStyle("icon", lookupTableIcon);
            lookupTableButton.toolTip = "Edit Lookup Tables";
            lookupTableButton.addEventListener(MouseEvent.CLICK, editLookupTables);
            addChild(lookupTableButton);
        }
        var groupingLabel:Label = new Label();
        groupingLabel.text = "Hierarchy:";
        groupingLabel.setStyle("fontSize", 14);
        addChild(groupingLabel);
        addDropAreaGrouping(hierarchyGrouping);
        var areaMeasureLabel:Label = new Label();
        areaMeasureLabel.text = "Fields:";
        areaMeasureLabel.setStyle("fontSize", 14);
        addChild(areaMeasureLabel);
        addDropAreaGrouping(itemGrouping);

        if (treeDefinition.hierarchy != null) {
            hierarchyGrouping.addAnalysisItem(treeDefinition.hierarchy);
        }
        if (treeDefinition.items != null) {
            for each (var item:AnalysisItem in treeDefinition.items) {
                itemGrouping.addAnalysisItem(item);
            }
        }
    }

    private function requestListData(event:AnalysisItemUpdateEvent):void {
        dispatchEvent(new ReportDataEvent(ReportDataEvent.REQUEST_DATA));
    }

    public function set analysisDefinition(analysisDefinition:AnalysisDefinition):void {
        treeDefinition = analysisDefinition as TreeDefinition;
    }

    public function createAnalysisDefinition():AnalysisDefinition {
        treeDefinition.hierarchy = hierarchyGrouping.getListColumns()[0];
        treeDefinition.items = new ArrayCollection(itemGrouping.getListColumns());
        return treeDefinition;
    }

    private function editLookupTables(event:MouseEvent):void {
        dispatchEvent(new EnableLookupEditingEvent());
        dispatchEvent(new ReportDataEvent(ReportDataEvent.REQUEST_DATA, false));
    }

    public function isDataValid():Boolean {
        return (hierarchyGrouping.getListColumns().length > 0 && itemGrouping.getListColumns().length > 0);
    }

    public function addItem(analysisItem:AnalysisItem):void {
        if (analysisItem.hasType(AnalysisItemTypes.HIERARCHY)) {
            hierarchyGrouping.addAnalysisItem(analysisItem);
        } else {
            itemGrouping.addAnalysisItem(analysisItem);
        }
        dispatchEvent(new ReportDataEvent(ReportDataEvent.REQUEST_DATA));
    }

    public function onCustomChangeEvent(event:CustomChangeEvent):void {
    }

    public function onDataReceipt(event:DataServiceEvent):void {
    }
}
}