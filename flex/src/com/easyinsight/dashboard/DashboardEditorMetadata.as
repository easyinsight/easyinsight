/**
 * Created by IntelliJ IDEA.
 * User: jamesboe
 * Date: 5/20/11
 * Time: 5:11 PM
 * To change this template use File | Settings | File Templates.
 */
package com.easyinsight.dashboard {
import com.easyinsight.filtering.TransformContainer;

import mx.collections.ArrayCollection;
import mx.core.UIComponent;

public class DashboardEditorMetadata {

    public var allFields:ArrayCollection;
    public var dataSourceID:int;
    public var availableFields:ArrayCollection;
    public var role:int;
    public var fixedID:Boolean;
    public var dashboardView:UIComponent;
    public var borderThickness:int;
    public var borderColor:uint;
    public var dashboardID:int;
    public var dashboard:Dashboard;
    public var dashboardStackPositions:DashboardStackPositions;
    public var transformContainer:TransformContainer;

    public function DashboardEditorMetadata() {
    }
}
}
