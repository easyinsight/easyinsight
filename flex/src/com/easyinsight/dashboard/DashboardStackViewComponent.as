package com.easyinsight.dashboard {
import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.filtering.FilterDefinition;
import com.easyinsight.filtering.TransformContainer;
import com.easyinsight.filtering.TransformsUpdatedEvent;
import com.easyinsight.framework.LoginEvent;
import com.easyinsight.framework.User;
import com.easyinsight.report.TempReportExportWindow;
import com.easyinsight.skin.BackgroundImage;
import com.easyinsight.skin.ImageLoadEvent;
import com.easyinsight.skin.ImageLoader;
import com.easyinsight.util.CookieUtil;
import com.easyinsight.util.PopUpUtil;

import flash.display.DisplayObject;

import flash.events.Event;
import flash.events.MouseEvent;

import mx.collections.ArrayCollection;
import mx.containers.Box;
import mx.containers.Canvas;
import mx.containers.HBox;
import mx.containers.VBox;
import mx.containers.ViewStack;
import mx.controls.Button;
import mx.controls.ComboBox;
import mx.core.Container;
import mx.core.UIComponent;
import mx.effects.Effect;
import mx.managers.PopUpManager;
import mx.messaging.config.ServerConfig;

import org.efflex.mx.viewStackEffects.CubePapervision3D;
import org.efflex.mx.viewStackEffects.Fade;
import org.efflex.mx.viewStackEffects.FlipPapervision3D;
import org.efflex.mx.viewStackEffects.Pixelate;
import org.efflex.mx.viewStackEffects.Slide;

public class DashboardStackViewComponent extends VBox implements IDashboardViewComponent {

    public var dashboardStack:DashboardStack;

    public var dashboardEditorMetadata:DashboardEditorMetadata;

    public function DashboardStackViewComponent() {
        super();
        this.percentWidth = 100;
        this.percentHeight = 100;
    }

    public function obtainPreferredSizeInfo():SizeInfo {
        return new SizeInfo(dashboardStack.preferredWidth, dashboardStack.preferredHeight);
    }

    private var viewStack:ViewStack;

    private var viewChildren:ArrayCollection;

    private function onChange(targetIndex:int):void {
        var currentComp:UIComponent = viewStack.selectedChild;
        var newComp:UIComponent = viewStack.getChildAt(targetIndex) as UIComponent;
        if (targetIndex > viewStack.selectedIndex) {
            currentComp.setStyle("hideEffect", leftEffect);
            newComp.setStyle("showEffect", rightEffect);
        } else if (targetIndex < viewStack.selectedIndex) {
            currentComp.setStyle("hideEffect", rightEffect);
            newComp.setStyle("showEffect", leftEffect);
        } else {
            return;
        }
        IDashboardViewComponent(newComp).initialRetrieve();
        viewStack.selectedIndex = targetIndex;
        updateAdditionalFilters(filterMap);

        if (childFilterBox != null) {
            childFilterBox.removeAllChildren();
            childFilterBox.addChild(childFilters.getItemAt(targetIndex) as UIComponent);
        }
    }

    private function onButtonClick(event:MouseEvent):void {
        var targetIndex:int = event.currentTarget.data as int;
        onChange(targetIndex);
    }

    private function onComboBoxChange(event:Event):void {
        var targetIndex:int = event.currentTarget.selectedIndex;
        onChange(targetIndex);
    }

    private var leftEffect:Effect;
    private var rightEffect:Effect;

    protected var childFilterBox:Box;

    protected override function createChildren():void {
        super.createChildren();
        buildEffects();
        if (dashboardStack.consolidateHeaderElements) {
            var headerHBox:HBox = new HBox();
            headerHBox.setStyle("verticalAlign", "middle");
            headerHBox.percentWidth = 100;
            var myFiltersBox:HBox = new HBox();
            //myFiltersBox.percentWidth = 100;
            childFilterBox = new HBox();
            childFilterBox.percentWidth = 100;
            buttonsBox = new HBox();
            headerHBox.addChild(myFiltersBox);
            headerHBox.addChild(childFilterBox);
            headerHBox.addChild(buttonsBox);
            addChild(headerHBox);
        } else {
            var headerArea:Canvas = new Canvas();
            defaultButtonsBox = styleHeaderArea(headerArea);
            addChild(headerArea);
        }
        viewStack = new ViewStack();
        viewStack.percentHeight = 100;
        viewStack.percentWidth = 100;
        viewChildren = new ArrayCollection();
        createStackContents();
        var transformContainer:TransformContainer = createTransformContainer();
        if (transformContainer != null) {
            if (dashboardStack.consolidateHeaderElements) {
                if (dashboardStack.filters != null && dashboardStack.filters.length > 1) {
                    myFiltersBox.percentWidth = 100;
                }
                myFiltersBox.addChild(transformContainer);
            } else {
                if (_consolidateHeader) {
                    if (dashboardStack.filters != null && dashboardStack.filters.length > 0) {
                        _consolidateHeader.percentWidth = 100;
                    }
                    _consolidateHeader.addChild(transformContainer);
                } else {
                    addChild(transformContainer);
                }
            }
        }
        addChild(viewStack);

    }

    private var buttonsBox:HBox;
    private var defaultButtonsBox:Container;

    protected function createStackContents():void {
        viewStack.removeAllChildren();
        viewChildren.removeAll();
        createStackChildren(dashboardStack.consolidateHeaderElements ? buttonsBox : defaultButtonsBox);
    }

    private var _consolidateHeader:Container = null;

    public function set consolidateHeader(value:Container):void {
        _consolidateHeader = value;
    }

    private function styleHeaderArea(headerArea:Container):Container {
        headerArea.setStyle("backgroundColor", dashboardStack.headerBackgroundColor);
        headerArea.setStyle("backgroundAlpha", dashboardStack.headerBackgroundAlpha);
        headerArea.setStyle("horizontalAlign", "center");
        headerArea.percentWidth = 100;
        var headerCentering:Box = new Box();
        headerCentering.percentWidth = 100;
        headerCentering.setStyle("horizontalAlign", "center");
        var headerBackgroundImage:BackgroundImage = new BackgroundImage();
        headerBackgroundImage.applyCenterScreenLogic = false;
        headerBackgroundImage.useBindings = false;
        var headerbar:HBox = new HBox();
        if (dashboardStack.headerBackground != null) {
            var headerBarLoader:ImageLoader = new ImageLoader();
            headerBarLoader.addEventListener(ImageLoadEvent.IMAGE_LOADED, function(event:ImageLoadEvent):void {
                headerBackgroundImage.width = event.bitmap.width;
                headerBackgroundImage.height = event.bitmap.height;
                headerBackgroundImage.backgroundImageSource = event.bitmap;
            });
            headerBarLoader.load(dashboardStack.headerBackground.id);
        }
        headerbar.percentWidth = 100;
        headerbar.percentHeight = 100;
        headerbar.setStyle("horizontalAlign", dashboardStack.headerBackground != null ? "right" : "center");
        headerbar.setStyle("verticalAlign", "bottom");
        headerbar.setStyle("paddingBottom", 5);
        headerBackgroundImage.addChild(headerbar);
        headerCentering.addChild(headerBackgroundImage);
        headerArea.addChild(headerCentering);
        if (dashboardStack.headerBackground != null && dashboardEditorMetadata.fixedID) {
            shareButton = new Button();
            shareButton.label = "Share";
            shareButton.styleName = "grayButton";
            shareButton.addEventListener(MouseEvent.CLICK, share);
            headerArea.addChild(shareButton);

            logoutButton = new Button();
            logoutButton.label = "Log Out";
            logoutButton.styleName = "grayButton";
            logoutButton.addEventListener(MouseEvent.CLICK, function(event:MouseEvent):void {
                User.destroy();
                CookieUtil.deleteCookie("eisession");
                ServerConfig.getChannelSet("dashboardService").logout();
                User.getEventNotifier().dispatchEvent(new LoginEvent(LoginEvent.LOGOUT));
            });
            headerArea.addChild(logoutButton);
        }
        return headerbar;
    }

    private function share(event:MouseEvent):void {
        var reports:ArrayCollection = reportCount();
        var window:TempReportExportWindow = new TempReportExportWindow();
        window.dashboard = dashboardEditorMetadata.dashboard;
        window.coreView = DashboardView(dashboardEditorMetadata.dashboardView).rootView as DisplayObject;
        if (reports.length == 1) {
            window.report = reports.getItemAt(0) as AnalysisDefinition;
            window.tabular = true;
        } else {
            window.tabular = false;
        }
        PopUpManager.addPopUp(window, this, true);
        PopUpUtil.centerPopUp(window);
    }


    public function reportCount():ArrayCollection {
        var activeChild:IDashboardViewComponent = IDashboardViewComponent(viewChildren.getItemAt(viewStack.selectedIndex));
        return activeChild.reportCount();
    }

    private var logoutButton:Button;
    private var shareButton:Button;

    override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
        super.updateDisplayList(unscaledWidth, unscaledHeight);
        if (shareButton != null) {
            shareButton.y = 10;
            shareButton.x = shareButton.parent.width - 175;
        }
        if (logoutButton != null) {
            logoutButton.y = 10;
            logoutButton.x = logoutButton.parent.width - 100;
        }
    }

    private function createStackChildren(headerbar:Container):void {
        for (var i:int = 0; i < dashboardStack.gridItems.length; i++) {
            var stackItem:DashboardStackItem = dashboardStack.gridItems.getItemAt(i) as DashboardStackItem;
            var report:DashboardElement = stackItem.dashboardElement;
            if (dashboardStack.selectionType == 'Buttons') {
                var topButton:Button = new Button();
                topButton.styleName = "grayButton";
                topButton.data = i;
                topButton.addEventListener(MouseEvent.CLICK, onButtonClick);
                if (report == null) {
                    topButton.label = "Stack Item " + i;
                } else if (report is DashboardReport) {
                    topButton.label = DashboardReport(report).report.name;
                    if (DashboardReport(report).report.reportType == AnalysisDefinition.HEATMAP) {
                        leftEffect = null;
                        rightEffect = null;
                    }
                } else {
                    if (report.label != null && report.label != "") {
                        topButton.label = report.label;
                    } else {
                        topButton.label = String(i);
                    }
                }
                if (dashboardStack.count > 1) {
                    headerbar.addChild(topButton);
                }
            }
            var comp:UIComponent = createComp(report, i);
            viewChildren.addItem(comp);
            viewStack.addChild(comp);
        }
        if (dashboardStack.selectionType == 'Combo Box') {
            var childComboBox:ComboBox = new ComboBox();
            childComboBox.labelFunction = comboBoxLabelFunction;
            childComboBox.dataProvider = dashboardStack.gridItems;
            childComboBox.addEventListener(Event.CHANGE, onComboBoxChange);
            headerbar.addChild(childComboBox);
        }
    }

    protected function stackChildSize():int {
        return viewChildren.length;
    }

    protected function stackComponents():ArrayCollection {
        return viewChildren;
    }

    protected function createComp(element:DashboardElement, i:int):UIComponent {
        var comp:UIComponent = DashboardElementFactory.createViewUIComponent(element, dashboardEditorMetadata);
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
        return comp;
    }

    protected var childFilters:ArrayCollection = new ArrayCollection();

    private function comboBoxLabelFunction(object:Object):String {
        var report:DashboardElement = DashboardStackItem(object).dashboardElement;
        if (report is DashboardReport) {
            return DashboardReport(report).report.name;
        } else {
            if (report.label != null && report.label != "") {
                return report.label;
            } else {
                return "(Unlabeled)";
            }
        }
    }

    public var stackFilterMap:Object = new Object();

    private function createTransformContainer():TransformContainer {
        if (dashboardStack.filters.length > 0) {
            transformContainer = new TransformContainer();
            transformContainer.setStyle("borderStyle", dashboardStack.filterBorderStyle);
            transformContainer.setStyle("borderColor", dashboardStack.filterBorderColor);
            transformContainer.setStyle("backgroundColor", dashboardStack.filterBackgroundColor);
            transformContainer.setStyle("backgroundAlpha", dashboardStack.filterBackgroundAlpha);
            transformContainer.filterEditable = false;
            var myFilterColl:ArrayCollection = new ArrayCollection();
            for each (var filterDefinition:FilterDefinition in dashboardStack.filters) {
                var existingFilter:FilterDefinition = stackFilterMap[filterDefinition.qualifiedName()];
                var filterToUse:FilterDefinition;
                if (existingFilter == null) {
                    filterToUse = filterDefinition;
                } else {
                    filterToUse = existingFilter;
                }
                stackFilterMap[filterDefinition.qualifiedName()] = filterToUse;
                myFilterColl.addItem(filterToUse);
            }
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
        } else {
            filterMap[elementID] = new ArrayCollection();
        }
        return transformContainer;
    }

    private function buildEffects():void {
        if (dashboardStack.effectType == DashboardStack.SLIDE) {
            leftEffect = new Slide();
            leftEffect.duration = dashboardStack.effectDuration;
            Slide(leftEffect).direction = "left";
            rightEffect = new Slide();
            rightEffect.duration = dashboardStack.effectDuration;
            Slide(rightEffect).direction = "right";
        } else if (dashboardStack.effectType == DashboardStack.FADE) {
            leftEffect = new Fade();
            leftEffect.duration = dashboardStack.effectDuration;
            rightEffect = new Fade();
            rightEffect.duration = dashboardStack.effectDuration;
        } else if (dashboardStack.effectType == DashboardStack.PIXELATE) {
            leftEffect = new Pixelate();
            leftEffect.duration = dashboardStack.effectDuration;
            rightEffect = new Pixelate();
            rightEffect.duration = dashboardStack.effectDuration;
        } else if (dashboardStack.effectType == DashboardStack.ROTATE) {
            leftEffect = new CubePapervision3D();
            leftEffect.duration = dashboardStack.effectDuration;
            CubePapervision3D(leftEffect).direction = "left";
            rightEffect = new CubePapervision3D();
            rightEffect.duration = dashboardStack.effectDuration;
            CubePapervision3D(rightEffect).direction = "right";
        } else if (dashboardStack.effectType == DashboardStack.FLIP) {
            leftEffect = new FlipPapervision3D();
            leftEffect.duration = dashboardStack.effectDuration;
            FlipPapervision3D(leftEffect).direction = "left";
            rightEffect = new FlipPapervision3D();
            rightEffect.duration = dashboardStack.effectDuration;
            FlipPapervision3D(rightEffect).direction = "right";
        }
    }

    private var transformContainer:TransformContainer;

    private var filterMap:Object = new Object();

    public var elementID:String;

    private function transformsUpdated(event:Event):void {
        filterMap[elementID] = transformContainer.getFilterDefinitions();
        updateAdditionalFilters(filterMap);
        refresh();
    }

    public function updateAdditionalFilters(filterMap:Object):void {
        if (filterMap != null) {
            for (var id:String in filterMap) {
                var filters:Object = filterMap[id];
                if (filters != null) {
                    this.filterMap[id] = filters;
                }
            }
        }
        for each (var comp:IDashboardViewComponent in viewChildren) {
            comp.updateAdditionalFilters(this.filterMap);
        }
    }

    public function refresh():void {
        IDashboardViewComponent(viewChildren.getItemAt(viewStack.selectedIndex)).refresh();
    }

    public function initialRetrieve():void {
        var changed:Boolean = false;
        if (transformContainer != null) {
            changed = transformContainer.updateState() || changed;
        }
        if (viewChildren != null && viewChildren.length > 0) {
            if (changed) {
                IDashboardViewComponent(viewChildren.getItemAt(viewStack.selectedIndex)).refresh();
            } else {
                IDashboardViewComponent(viewChildren.getItemAt(viewStack.selectedIndex)).initialRetrieve();
            }
        }
    }

    public function toggleFilters(showFilters:Boolean):void {
        if (transformContainer != null) {
            if (showFilters) {

            } else {

            }
        }
        for each (var comp:IDashboardViewComponent in viewChildren) {
            comp.toggleFilters(showFilters);
        }
    }
}
}