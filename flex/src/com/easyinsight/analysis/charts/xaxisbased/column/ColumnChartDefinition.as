package com.easyinsight.analysis.charts.xaxisbased.column {
import com.easyinsight.analysis.ChartDefinition;
import com.easyinsight.analysis.charts.ChartTypes;
import com.easyinsight.analysis.charts.xaxisbased.XAxisDefinition;
import com.easyinsight.analysis.AnalysisDefinition;

[Bindable]
[RemoteClass(alias="com.easyinsight.analysis.definitions.WSColumnChartDefinition")]
public class ColumnChartDefinition extends XAxisDefinition{

    public var chartColor:uint;
    public var useChartColor:Boolean;
    public var columnSort:String = ChartDefinition.SORT_UNSORTED;

    public function ColumnChartDefinition() {
        super();
    }

    override public function get type():int {
        return AnalysisDefinition.COLUMN;
    }

    override public function getChartType():int {
        return ChartTypes.COLUMN_2D;
    }

    override public function getChartFamily():int {
        return ChartTypes.COLUMN_FAMILY;
    }
}
}