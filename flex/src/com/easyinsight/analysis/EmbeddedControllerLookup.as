package com.easyinsight.analysis {
import com.easyinsight.analysis.charts.bubble.BubbleChartEmbeddedController;
import com.easyinsight.analysis.charts.plot.PlotChartEmbeddedController;
import com.easyinsight.analysis.charts.twoaxisbased.area.Area3DChartEmbeddedController;
import com.easyinsight.analysis.charts.twoaxisbased.area.AreaChartEmbeddedController;
import com.easyinsight.analysis.charts.twoaxisbased.line.Line3DChartEmbeddedController;
import com.easyinsight.analysis.charts.twoaxisbased.line.LineChartEmbeddedController;
import com.easyinsight.analysis.charts.xaxisbased.column.Column3DChartEmbeddedController;
import com.easyinsight.analysis.charts.xaxisbased.column.ColumnChartEmbeddedController;
import com.easyinsight.analysis.charts.xaxisbased.pie.Pie3DChartEmbeddedController;
import com.easyinsight.analysis.charts.xaxisbased.pie.PieChartEmbeddedController;
import com.easyinsight.analysis.charts.yaxisbased.bar.Bar3DChartEmbeddedController;
import com.easyinsight.analysis.charts.yaxisbased.bar.BarChartEmbeddedController;
import com.easyinsight.analysis.crosstab.CrosstabEmbeddedController;
import com.easyinsight.analysis.gauge.GaugeEmbeddedController;
import com.easyinsight.analysis.list.ListEmbeddedController;
import com.easyinsight.analysis.maps.MapEmbeddedController;
import com.easyinsight.analysis.tree.TreeEmbeddedController;
import com.easyinsight.analysis.treemap.TreeMapEmbeddedController;

public class EmbeddedControllerLookup {

    public function EmbeddedControllerLookup() {
    }

    public static function controllerForType(type:int):Class {
        var controller:Class;
        switch(type) {
            case AnalysisDefinition.LIST:
                controller = ListEmbeddedController;
                break;
            case AnalysisDefinition.CROSSTAB:
                controller = CrosstabEmbeddedController;
                break;
            case AnalysisDefinition.MAP:
                controller = MapEmbeddedController;
                break;
            case AnalysisDefinition.COLUMN:
                controller = ColumnChartEmbeddedController;
                break;
            case AnalysisDefinition.COLUMN3D:
                controller = Column3DChartEmbeddedController;
                break;
            case AnalysisDefinition.BAR:
                controller = BarChartEmbeddedController;
                break;
            case AnalysisDefinition.BAR3D:
                controller = Bar3DChartEmbeddedController;
                break;
            case AnalysisDefinition.PIE:
                controller = PieChartEmbeddedController;
                break;
            case AnalysisDefinition.PIE3D:
                controller = Pie3DChartEmbeddedController;
                break;
            case AnalysisDefinition.LINE:
                controller = LineChartEmbeddedController;
                break;
            case AnalysisDefinition.LINE3D:
                controller = Line3DChartEmbeddedController;
                break;
            case AnalysisDefinition.AREA:
                controller = AreaChartEmbeddedController;
                break;
            case AnalysisDefinition.AREA3D:
                controller = Area3DChartEmbeddedController;
                break;
            case AnalysisDefinition.PLOT:
                controller = PlotChartEmbeddedController;
                break;
            case AnalysisDefinition.BUBBLE:
                controller = BubbleChartEmbeddedController;
                break;
            case AnalysisDefinition.GAUGE:
                controller = GaugeEmbeddedController;
                break;
            case AnalysisDefinition.TREEMAP:
                controller = TreeMapEmbeddedController;
                break;
            case AnalysisDefinition.TREE:
                controller = TreeEmbeddedController;
                break;
        }
        return controller;
    }
}
}