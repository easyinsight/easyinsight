package com.easyinsight.dashboard {
import com.easyinsight.solutions.InsightDescriptor;

import mx.core.UIComponent;

[Bindable]
[RemoteClass(alias="com.easyinsight.dashboard.DashboardReport")]
public class DashboardReport extends DashboardElement {

    public var report:InsightDescriptor;

    public function DashboardReport() {
        super();
    }

    override public function createEditorComponent():UIComponent {
        var comp:DashboardReportEditorComponent = new DashboardReportEditorComponent();
        comp.report = this;
        return comp;
    }

    override public function createViewComponent():UIComponent {
        var comp:DashboardReportViewComponent = new DashboardReportViewComponent();
        comp.dashboardReport = this;
        return comp;
    }
}
}