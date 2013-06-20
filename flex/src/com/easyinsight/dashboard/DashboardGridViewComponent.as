package com.easyinsight.dashboard {

import com.easyinsight.analysis.list.SizeOverrideEvent;

import mx.collections.ArrayCollection;
import mx.containers.Grid;
import mx.containers.GridItem;
import mx.containers.GridRow;
import mx.controls.Alert;
import mx.core.UIComponent;

public class DashboardGridViewComponent extends Grid implements IDashboardViewComponent {

    public var dashboardGrid:DashboardGrid;

    public var dashboardEditorMetadata:DashboardEditorMetadata;

    public function DashboardGridViewComponent() {
        super();
        setStyle("paddingLeft", 0);
        setStyle("paddingRight", 0);
        setStyle("paddingTop", 0);
        setStyle("paddingBottom", 0);
        setStyle("horizontalGap", 0);
        setStyle("verticalGap", 0);
        addEventListener(SizeOverrideEvent.SIZE_OVERRIDE, onSizeOverride);
    }
    
    private function onSizeOverride(event:SizeOverrideEvent):void {
        for (var i:int = 0; i < dashboardGrid.rows; i++) {
            var gridRow:GridRow = getChildAt(i) as GridRow;
            for (var j:int = 0; j < dashboardGrid.columns; j++) {
                var e:DashboardGridItem = findItem(i, j);
                var gridItem:GridItem = gridRow.getChildAt(j) as GridItem;
                if (dashboardEditorMetadata != null && dashboardEditorMetadata.borderThickness == 0) {
                    gridItem.horizontalScrollPolicy = "off";
                    gridItem.verticalScrollPolicy = "off";
                }
                var childSizeInfo:SizeInfo = IDashboardViewComponent(gridItem.getChildAt(0)).obtainPreferredSizeInfo();
                if (childSizeInfo.preferredWidth != 0) {
                    //gridItem.width = childSizeInfo.preferredWidth + dashboardGrid.paddingLeft + dashboardGrid.paddingRight;
                    gridItem.percentWidth = NaN;
                } else {
                    gridItem.width = NaN;
                    gridItem.percentWidth = 100;
                }
                if (childSizeInfo.preferredHeight != 0) {
                    gridItem.height = childSizeInfo.preferredHeight + dashboardGrid.paddingTop + dashboardGrid.paddingBottom;
                    gridItem.percentHeight = NaN;
                } else {
                    gridItem.height = NaN;
                    gridItem.percentHeight = 100;
                }
            }
        }
    }

    private var viewChildren:ArrayCollection;

    public function rebuildSizing():void {

    }

    protected override function createChildren():void {
        super.createChildren();
        var dashboardAbsoluteHeight:Boolean = false;
        if (dashboardEditorMetadata != null) {
            if (dashboardEditorMetadata.dashboard.absoluteSizing) {
                dashboardAbsoluteHeight = true;
            }
        }
        setStyle("backgroundColor", dashboardGrid.backgroundColor);
        setStyle("backgroundAlpha", dashboardGrid.backgroundAlpha);
        viewChildren = new ArrayCollection();

        for (var i:int = 0; i < dashboardGrid.rows; i++) {
            var gridRow:GridRow = new GridRow();
            gridRow.setStyle("paddingLeft", 0);
            gridRow.setStyle("paddingRight", 0);
            gridRow.setStyle("paddingTop", 0);
            gridRow.setStyle("paddingBottom", 0);
            addChild(gridRow);
            var gridHeight:Boolean = false;
            for (var j:int = 0; j < dashboardGrid.columns; j++) {
                var e:DashboardGridItem = findItem(i, j);
                var gridItem:GridItem = new GridItem();

                var child:UIComponent = DashboardElementFactory.createViewUIComponent(e.dashboardElement, dashboardEditorMetadata, dashboardGrid);
                var childSizeInfo:SizeInfo = IDashboardViewComponent(child).obtainPreferredSizeInfo();
                if (childSizeInfo.preferredWidth == 0) {
                    gridItem.percentWidth = 100;
                } else {
                    gridItem.percentWidth = NaN;
                }
                if (dashboardAbsoluteHeight) {
                    gridHeight = true;
                    gridItem.percentHeight = NaN;
                } else {
                    if (childSizeInfo.preferredHeight == 0 && !childSizeInfo.autoCalcHeight) {
                        gridItem.percentHeight = 100;
                    } else {
                        gridHeight = true;
                        gridItem.percentHeight = NaN;
                    }
                }
                gridItem.setStyle("horizontalAlign", "center");

                viewChildren.addItem(child);
                gridItem.addChild(child);
                gridRow.addChild(gridItem);
            }

            gridRow.percentWidth = 100;
            if (!gridHeight && !dashboardAbsoluteHeight) {
                gridRow.percentHeight = 100;
            }
        }
        percentWidth = 100;
        if (!dashboardAbsoluteHeight) {
            percentHeight = 100;
        }
    }

    public function obtainPreferredSizeInfo():SizeInfo {
        for (var i:int = 0; i < dashboardGrid.rows; i++) {
            for (var j:int = 0; j < dashboardGrid.columns; j++) {
                var e:DashboardGridItem = findItem(i, j);
            }
        }
        return new SizeInfo(dashboardGrid.preferredWidth, dashboardGrid.preferredHeight);
    }

    private function findItem(x:int, y:int):DashboardGridItem {
        for each (var e:DashboardGridItem in dashboardGrid.gridItems) {
            if (e.rowIndex == x && e.columnIndex == y) {
                return e;
            }
        }
        return null;
    }

    public function refresh():void {
        for each (var comp:IDashboardViewComponent in viewChildren) {
            comp.refresh();
        }
    }

    public function updateAdditionalFilters(filters:Object):void {
        for each (var comp:IDashboardViewComponent in viewChildren) {
            comp.updateAdditionalFilters(filters);
        }
    }

    public function initialRetrieve():void {
        for each (var comp:IDashboardViewComponent in viewChildren) {
            comp.initialRetrieve();
        }
    }


    public function toggleFilters(showFilters:Boolean):void {
        for each (var comp:IDashboardViewComponent in viewChildren) {
            comp.toggleFilters(showFilters);
        }
    }

    public function stackPopulate(positions:DashboardStackPositions):void {
        for each (var comp:IDashboardViewComponent in viewChildren) {
            comp.stackPopulate(positions);
        }
    }

    public function reportCount():ArrayCollection {
        var reports:ArrayCollection = new ArrayCollection();
        for each (var comp:IDashboardViewComponent in viewChildren) {
            reports.addAll(comp.reportCount());
        }
        return reports;
    }
}
}