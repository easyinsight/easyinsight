/**
 * Created by IntelliJ IDEA.
 * User: jamesboe
 * Date: 1/4/12
 * Time: 2:11 PM
 * To change this template use File | Settings | File Templates.
 */
package com.easyinsight.analysis.diagram {
import com.anotherflexdev.diagrammer.BaseNode;
import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.TrendOutcome;

public class DiagramNode extends BaseNode {

    private var _report:AnalysisDefinition;

    public function DiagramNode() {
    }

    public function get report():AnalysisDefinition {
        return _report;
    }

    public function set report(value:AnalysisDefinition):void {
        _report = value;
    }

    public function set outcome(value:TrendOutcome):void {

    }

    public function get outcome():TrendOutcome {
        return null;
    }
}
}
