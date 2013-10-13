package com.easyinsight.dashboard {
import com.easyinsight.skin.ImageDescriptor;

import mx.collections.ArrayCollection;
import mx.core.UIComponent;

[Bindable]
[RemoteClass(alias="com.easyinsight.dashboard.DashboardElement")]
public class DashboardElement {

    public var label:String;
    public var elementServerID:int;
    public var urlKey:String;
    public var filters:ArrayCollection = new ArrayCollection();
    public var filterBorderStyle:String = "solid";
    public var filterBorderColor:uint = 0xCCCCCC;
    public var filterBackgroundColor:uint = 0xFFFFFF;
    public var filterBackgroundAlpha:Number = 0;
    public var paddingLeft:int = 5;
    public var paddingRight:int = 5;
    public var paddingTop:int = 5;
    public var paddingBottom:int = 5;
    public var headerBackground:ImageDescriptor;
    public var headerBackgroundColor:uint = 0xFFFFFF;
    public var headerBackgroundAlpha:Number = 0;
    public var preferredWidth:int;
    public var preferredHeight:int;
    public var forceScrollingOff:Boolean;
    public var htmlWidth:int;
    public var dashboardFilterOverrides:ArrayCollection;

    public var dashboardLevel:int;

    public function DashboardElement() {
    }

    /*public function createEditorComponent(dashboardEditorMetadata:DashboardEditorMetadata):UIComponent {
        return null;
    }

    public function createViewComponent(dashboardEditorMetadata:DashboardEditorMetadata):UIComponent {
        return null;
    }*/
}
}