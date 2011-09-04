package com.easyinsight.dashboard {
import com.easyinsight.skin.ImageDescriptor;

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
    public var stackControl:int;
    public var consolidateHeaderElements:Boolean;
    public var selectionType:String = "Buttons";

    public function DashboardStack() {
        super();
    }
}
}