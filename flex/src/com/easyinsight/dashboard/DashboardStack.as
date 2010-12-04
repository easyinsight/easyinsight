package com.easyinsight.dashboard {
import mx.collections.ArrayCollection;
import mx.core.UIComponent;

[Bindable]
[RemoteClass(alias="com.easyinsight.dashboard.DashboardStack")]
public class DashboardStack extends DashboardElement {

    public static const SLIDE:int = 1;
    public static const FADE:int = 2;
    public static const PIXELATE:int = 3;
    public static const ROTATE:int = 4;
    public static const NONE:int = 5;
    public static const FLIP:int = 6;

    public var gridItems:ArrayCollection = new ArrayCollection();
    public var count:int;
    public var effectType:int;
    public var effectDuration:int = 1000;

    public function DashboardStack() {
        super();
    }


    override public function createEditorComponent():UIComponent {
        var comp:DashboardStackEditorComponent = new DashboardStackEditorComponent();
        comp.dashboardStack = this;
        return comp;
    }

    override public function createViewComponent():UIComponent {
        var comp:DashboardStackViewComponent = new DashboardStackViewComponent();
        comp.dashboardStack = this;
        return comp;
    }
}
}