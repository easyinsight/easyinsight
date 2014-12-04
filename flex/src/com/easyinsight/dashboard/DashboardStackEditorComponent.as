package com.easyinsight.dashboard {
import com.easyinsight.WindowManagement;
import com.easyinsight.filtering.TransformContainer;
import com.easyinsight.util.PopUpUtil;

import flash.events.MouseEvent;

import mx.collections.ArrayCollection;
import mx.containers.HBox;
import mx.controls.Button;
import mx.core.Container;
import mx.core.UIComponent;
import mx.managers.PopUpManager;

public class DashboardStackEditorComponent extends DashboardStackViewComponent implements IDashboardEditorComponent {

    public function DashboardStackEditorComponent() {
        super();
        this.percentWidth = 100;
        this.percentHeight = 100;
    }

    private function dashboardPopulate(event:DashboardPopulateEvent):void {
        var box:DashboardBox = event.dashboardBox;
        var index:int = viewChildren.getItemIndex(box);
        var pos:int = index;
        (getButtonsBox().getChildAt(pos))["dashboardBox"] = box;
        if (box.element == null) {
            (getButtonsBox().getChildAt(pos))["label"] = "Stack Item " + index;
        } else if (box.element is DashboardReport) {
            (getButtonsBox().getChildAt(pos))["label"] = DashboardReport(box.element).report.name;
        } else if (box.element.label != null && box.element.label != "") {
            (getButtonsBox().getChildAt(pos))["label"] = box.element.label;
        } else {
            (getButtonsBox().getChildAt(pos))["label"] = "Stack Item " + index;
        }  
    }

    override protected function includeFilterContainer():Boolean {
        return true;
    }

    /*override protected function customize(transformContainer:TransformContainer):void {
        var button:Button = new Button();
        button.addEventListener(MouseEvent.CLICK, onNewFilter);
        button.label = "New Filter";
        button.styleName = "flatCreateButton";
        transformContainer.addChildAt(button, 0);
        transformContainer.analysisItems = dashboardEditorMetadata.allFields;
        transformContainer.reportView = false;
        transformContainer.filterEditable = true;
    }*/

    private function onNewFilter(event:MouseEvent):void {
        transformContainer.addNewFilter();
    }

    override protected function onChange(targetIndex:int):void {
        DashboardEditButton(getButtonsBox().getChildAt(viewStack.selectedIndex)).selected = false;
        DashboardEditButton(getButtonsBox().getChildAt(targetIndex)).selected = true;
        super.onChange(targetIndex);
    }

    override protected function createComp(element:DashboardElement, i:int):UIComponent {
        var box:DashboardBox = new DashboardBox();
        box.addEventListener(DashboardPopulateEvent.DASHBOARD_POPULATE, dashboardPopulate);
        box.dashboardEditorMetadata = dashboardEditorMetadata;
        if (element == null) {

        } else {
            var comp:UIComponent = DashboardElementFactory.createEditorUIComponent(element, dashboardEditorMetadata);
            if (comp is DashboardStackViewComponent) {
                DashboardStackViewComponent(comp).stackFilterMap = this.stackFilterMap;
            } else if (comp is DashboardReportViewComponent) {
                //DashboardReportViewComponent(comp).stackFilterMap = this.stackFilterMap;
            }
            if (dashboardStack.consolidateHeaderElements) {
                var filterContainer:Container = new HBox();
                childFilters.addItem(filterContainer);
                if (i == 0) {
                    childFilterBox.addChild(filterContainer);
                }
                if (dashboardStack.consolidateHeaderElements && comp is DashboardStackViewComponent) {
                    DashboardStackViewComponent(comp).consolidateHeader = filterContainer;
                } else if (dashboardStack.consolidateHeaderElements && comp is DashboardReportViewComponent) {
                    //DashboardReportViewComponent(comp).consolidateHeader = filterContainer;
                }
            }

            box.element = element;
            box.editorComp = IDashboardEditorComponent(comp);
        }
        return box;
    }

    public function getItems():ArrayCollection {
        var comps:ArrayCollection = stackComponents();
        var items:ArrayCollection = new ArrayCollection();
        for (var i:int = 0; i < dashboardStack.count; i++) {
            if (i < comps.length) {
                var box:DashboardBox = comps.getItemAt(i) as DashboardBox;
                box.save();
                var dashboardGridItem:DashboardStackItem = new DashboardStackItem();
                dashboardGridItem.position = i;
                dashboardGridItem.dashboardElement = box.element;
                items.addItem(dashboardGridItem);
            }
        }
        return items;
    }

    public function save():void {
        dashboardStack.filters = transformContainer.getFilterDefinitions();
        var comps:ArrayCollection = stackComponents();
        var items:ArrayCollection = new ArrayCollection();
        for (var i:int = 0; i < dashboardStack.count; i++) {
            if (i < comps.length) {
                var box:DashboardBox = comps.getItemAt(i) as DashboardBox;
                box.save();
                var dashboardGridItem:DashboardStackItem = new DashboardStackItem();
                dashboardGridItem.position = i;
                dashboardGridItem.dashboardElement = box.element;
                items.addItem(dashboardGridItem);
            }
        }
        dashboardStack.gridItems = items;
    }

    override protected function editMode():Boolean {
        return true;
    }

    protected override function createChildren():void {
        if (dashboardStack.count == 0) {
            dashboardStack.count = 1;
            var eTemp:DashboardStackItem = new DashboardStackItem();
            dashboardStack.gridItems.addItem(eTemp);
        }
        super.createChildren();
        for (var i:int = 0; i < getButtonsBox().getChildren().length; i++) {
            var btn:DashboardEditButton = getButtonsBox().getChildAt(i) as DashboardEditButton;
            if (i == 0) {
                btn.selected = true;
            }
            btn.dashboardBox = stackComponents().getItemAt(i) as DashboardBox;
        }
    }
    
    private function deletePage(event:DashboardStackEvent):void {
        var button:UIComponent = event.currentTarget as UIComponent;
        dashboardStack.count--;
        var index:int = button["data"];
        dashboardStack.gridItems.removeItemAt(index);
        var btnIndex:int = getButtonsBox().getChildIndex(button);
        getButtonsBox().removeChildAt(btnIndex);
        viewStack.removeChildAt(index);
        viewChildren.removeItemAt(index);
        for (var i:int = 0; i < getButtonsBox().getChildren().length; i++) {
            var btn:DashboardEditButton = getButtonsBox().getChildAt(i) as DashboardEditButton;
            btn.data = i;
        }
    }

    override protected function createStackButton(index:int, label:String):UIComponent {
        var topButton:DashboardEditButton = new DashboardEditButton();
        topButton.dashboardStack = dashboardStack;
        topButton.addEventListener(DashboardStackEvent.DELETE_PAGE, deletePage);
        topButton.addEventListener(DashboardStackEvent.CLICK, onButtonClick);
        topButton.data = index;
        topButton.label = label;
        return topButton;
    }

    public function addStackElement():void {
        dashboardStack.count++;
        var stackItem:DashboardStackItem = new DashboardStackItem();
        dashboardStack.gridItems.addItem(stackItem);
        addStackChild(stackItem, dashboardStack.gridItems.length - 1);
    }

    private function onDimensions(event:GridDimensionEvent):void {
        dispatchEvent(new DashboardPopulateEvent(DashboardPopulateEvent.DASHBOARD_POPULATE));
        if (dashboardStack.count != stackChildSize()) {
            for (var i:int = stackChildSize(); i < dashboardStack.count; i++) {
                if (dashboardStack.gridItems.length > i) {

                } else {
                    var eTemp:DashboardStackItem = new DashboardStackItem();
                    dashboardStack.gridItems.addItem(eTemp);
                }
            }
            createStackContents();
        } else if (event.changed) {
            createStackContents();
        }
    }

    public function toggleControls(show:Boolean):void {
        var comps:ArrayCollection = stackComponents();
        for each (var box:DashboardBox in comps) {
            box.toggleControls(show);
        }
    }

    public function validate(results:Array):void {
        var valid:Boolean = false;
        var comps:ArrayCollection = stackComponents();
        if (comps.length != dashboardStack.count) {
            this.errorString = "You need to configure all children of this stack.";
            dispatchEvent(new MouseEvent(MouseEvent.MOUSE_OVER));
            results.push("You need to configure all children of this stack.");
        }
        if (!valid) {
            for each (var box:DashboardBox in comps) {
                if (box.element == null) {
                    box.errorString = "You need to configure this section of the grid.";
                    box.dispatchEvent(new MouseEvent(MouseEvent.MOUSE_OVER));
                    results.push("You need to configure all children of this stack.");
                } else {
                    box.validate(results);
                }
            }
        }
    }

    public function edit():void {
        var window:StackDimensionsWindow = new StackDimensionsWindow();
        window.editorComponent = this;
        window.dashboardStack = dashboardStack;
        window.availableDimensions = dashboardEditorMetadata.availableFields;
        window.dataSourceID = dashboardEditorMetadata.dataSourceID;
        window.allFields = dashboardEditorMetadata.allFields;
        window.addEventListener(GridDimensionEvent.GRID_DIMENSION, onDimensions, false, 0, true);
        WindowManagement.manager.addWindow(window);
        PopUpManager.addPopUp(window, this, true);
        PopUpUtil.centerPopUp(window);
    }
}
}