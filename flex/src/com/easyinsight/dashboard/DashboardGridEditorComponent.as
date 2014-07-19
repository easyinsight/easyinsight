package com.easyinsight.dashboard {
import com.easyinsight.analysis.list.SizeOverrideEvent;
import com.easyinsight.util.PopUpUtil;

import flash.events.Event;

import mx.collections.ArrayCollection;
import mx.containers.Grid;
import mx.containers.GridItem;
import mx.containers.GridRow;
import mx.managers.PopUpManager;

public class DashboardGridEditorComponent extends Grid implements IDashboardEditorComponent {

    public var dashboardGrid:DashboardGrid;

    public function DashboardGridEditorComponent() {
        super();
        this.percentWidth = 100;
        this.percentHeight = 100;
        horizontalScrollPolicy = "off";
        verticalScrollPolicy = "off";
        addEventListener(SizeOverrideEvent.SIZE_OVERRIDE, onSizeOverride);
    }

    public function obtainPreferredSizeInfo():SizeInfo {
        return new SizeInfo();
    }

    public function stackPopulate(positions:DashboardStackPositions):void {

    }

    public function save():void {
        var items:ArrayCollection = new ArrayCollection();
        for (var i:int = 0; i < dashboardGrid.rows; i++) {
            if (i < getChildren().length) {
                var row:GridRow = getChildAt(i) as GridRow;
                for (var j:int = 0; j < dashboardGrid.columns; j++) {
                    if (j < row.getChildren().length) {
                        var item:GridItem = row.getChildAt(j) as GridItem;
                        var box:DashboardBox = item.getChildAt(0) as DashboardBox;
                        box.save();
                        if (box.element != null) {
                            var dashboardGridItem:DashboardGridItem = new DashboardGridItem();
                            dashboardGridItem.rowIndex = i;
                            dashboardGridItem.columnIndex = j;
                            dashboardGridItem.dashboardElement = box.element;
                            items.addItem(dashboardGridItem);
                        }
                    }
                }
            }
        }
        dashboardGrid.gridItems = items;
    }

    protected override function createChildren():void {
        super.createChildren();
        if (dashboardGrid.rows == 0 && dashboardGrid.columns == 0) {
            var window:GridDimensionsWindow = new GridDimensionsWindow();
            window.addEventListener(GridDimensionEvent.GRID_DIMENSION, onDimensions, false, 0, true);
            PopUpManager.addPopUp(window, this, true);
            PopUpUtil.centerPopUp(window);
        }
        recreateStructure();
    }

    private function onSizeOverride(event:SizeOverrideEvent):void {
        for (var i:int = 0; i < dashboardGrid.rows; i++) {
            var gridRow:GridRow = getChildAt(i) as GridRow;
            for (var j:int = 0; j < dashboardGrid.columns; j++) {
                var e:DashboardGridItem = findItem(i, j);
                var gridItem:GridItem = gridRow.getChildAt(j) as GridItem;
                var box:DashboardBox = gridItem.getChildAt(0) as DashboardBox;
                var comp:IDashboardEditorComponent = box.component();
                if (comp != null) {
                    var childSizeInfo:SizeInfo = comp.obtainPreferredSizeInfo();

                    if (childSizeInfo.preferredWidth != 0) {
                        gridItem.width = childSizeInfo.preferredWidth + dashboardGrid.paddingLeft + dashboardGrid.paddingRight;
                        gridItem.percentWidth = NaN;
                    } else {
                        gridItem.width = NaN;
                        gridItem.percentWidth = 100;
                    }
                    if (!dashboardEditorMetadata.dashboard.absoluteSizing) {
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
        }
    }

    private var viewChildren:ArrayCollection;



    private function recreateStructure():void {
        removeAllChildren();
        var dashboardAbsoluteHeight:Boolean = false;
        if (dashboardEditorMetadata != null) {
            if (dashboardEditorMetadata.dashboard.absoluteSizing) {
                dashboardAbsoluteHeight = true;
            }
        }
        viewChildren = new ArrayCollection();
        for (var i:int = 0; i < dashboardGrid.rows; i++) {
            var gridRow:GridRow = new GridRow();
            gridRow.horizontalScrollPolicy = "off";
            gridRow.verticalScrollPolicy = "off";
            var gridHeight:Boolean = false;
            addChild(gridRow);
            for (var j:int = 0; j < dashboardGrid.columns; j++) {
                var e:DashboardGridItem = findItem(i, j);
                var gridItem:GridItem = new GridItem();
                gridItem.horizontalScrollPolicy = "off";
                gridItem.verticalScrollPolicy = "off";
                gridItem.setStyle("horizontalAlign", "center");
                gridItem.setStyle("borderThickness", 1);
                gridItem.setStyle("borderStyle", "solid");
                gridItem.percentHeight = 100;
                var box:DashboardBox = new DashboardBox();
                viewChildren.addItem(box);
                box.dashboardEditorMetadata = dashboardEditorMetadata;
                if (e != null && e.dashboardElement != null) {
                    box.element = e.dashboardElement;
                    var comp:IDashboardViewComponent = DashboardElementFactory.createViewUIComponent(e.dashboardElement, dashboardEditorMetadata, dashboardGrid) as IDashboardViewComponent;
                    var childSizeInfo:SizeInfo = comp.obtainPreferredSizeInfo();
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
                } else {
                    gridItem.percentWidth = 100;
                }
                gridItem.addChild(box);
                gridRow.addChild(gridItem);
            }
            gridRow.percentWidth = 100;
            if (!gridHeight && !dashboardAbsoluteHeight) {
                gridRow.percentHeight = 100;
            }
        }
        //initialRetrieve();
    }

    private function onDimensions(event:GridDimensionEvent):void {
        dashboardGrid.rows = event.rows;
        dashboardGrid.columns = event.columns;
        recreateStructure();
    }

    private function onChange(event:Event):void {
        save();
        recreateStructure();
        dispatchEvent(new DashboardPopulateEvent(DashboardPopulateEvent.DASHBOARD_POPULATE));
    }

    private function findItem(x:int, y:int):DashboardGridItem {
        for each (var e:DashboardGridItem in dashboardGrid.gridItems) {
            if (e.rowIndex == x && e.columnIndex == y) {
                return e;
            }
        }
        return null;
    }

    public function toggleControls(show:Boolean):void {
        for (var i:int = 0; i < dashboardGrid.rows; i++) {
            var row:GridRow = getChildAt(i) as GridRow;
            for (var j:int = 0; j < dashboardGrid.columns; j++) {
                var item:GridItem = row.getChildAt(j) as GridItem;
                if (show) {
                    item.setStyle("borderThickness", 1);
                    item.setStyle("borderStyle", "solid");
                } else {
                    item.setStyle("borderThickness", 0);
                    item.setStyle("borderStyle", "none");
                }
                var box:DashboardBox = item.getChildAt(0) as DashboardBox;
                box.toggleControls(show);
            }
        }
    }

    public function validate(results:Array):void {
        var valid:String = null;
        for (var i:int = 0; i < dashboardGrid.rows; i++) {
            var row:GridRow = getChildAt(i) as GridRow;
            for (var j:int = 0; j < dashboardGrid.columns; j++) {
                var item:GridItem = row.getChildAt(j) as GridItem;
                var box:DashboardBox = item.getChildAt(0) as DashboardBox;
                if (box.element == null) {
                    box.validationFail();
                    results.push("You need to fully configure this grid.");
                } else {
                    box.validate(results);
                }
            }
        }
    }

    public var dashboardEditorMetadata:DashboardEditorMetadata;

    public function edit():void {
        var window:GridEditWindow = new GridEditWindow();
        window.dashboardElement = dashboardGrid;
        window.addEventListener(Event.CHANGE, onChange, false, 0, true);
        PopUpManager.addPopUp(window, this, true);
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

    public function forceRetrieve():void {
        for each (var comp:IDashboardViewComponent in viewChildren) {
            comp.forceRetrieve();
        }
    }

    public function initialRetrieve():void {
        for each (var comp:IDashboardViewComponent in viewChildren) {
            comp.initialRetrieve();
        }
    }

    public function reportCount():ArrayCollection {
        var reports:ArrayCollection = new ArrayCollection();
        for each (var comp:IDashboardViewComponent in viewChildren) {
            reports.addAll(comp.reportCount());
        }
        return reports;
    }

    public function toggleFilters(showFilters:Boolean):void {
    }

    public function recordToPDF(imageMap:Object):void {
    }
}
}