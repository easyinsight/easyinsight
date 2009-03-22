package com.easyinsight.analysis.charts.xaxisbased.column {
import com.easyinsight.analysis.DataViewFactory;
import com.easyinsight.analysis.EmbeddedViewFactory;
import com.easyinsight.analysis.IReportController;
import com.easyinsight.analysis.charts.xaxisbased.XAxisControlBar;
import com.easyinsight.analysis.service.ListDataService;

public class Column3DChartController implements IReportController {
    
    public function Column3DChartController() {
    }

    public function createDataView():DataViewFactory {
        var factory:DataViewFactory = new DataViewFactory();
        factory.reportDataService = ListDataService;
        factory.reportControlBar = XAxisControlBar;
        factory.reportRenderer = "Column3DChartModule.swf";
        factory.newDefinition = Column3DChartDefinition;
        return factory;
    }

    public function createEmbeddedView():EmbeddedViewFactory {
        var factory:EmbeddedViewFactory = new EmbeddedViewFactory();
        factory.reportDataService = ListDataService;
        factory.reportRenderer = "Column3DChartModule.swf";
        factory.newDefinition = Column3DChartDefinition;
        return factory;
    }
}
}