/**
 * Created by IntelliJ IDEA.
 * User: jamesboe
 * Date: 9/26/11
 * Time: 12:29 PM
 * To change this template use File | Settings | File Templates.
 */
package com.easyinsight.analysis.trend {
import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.AnalysisItemTypes;
import com.easyinsight.analysis.KPIDefinition;
import com.easyinsight.analysis.TrendReportFieldExtension;

import mx.collections.ArrayCollection;

[Bindable]
[RemoteClass(alias="com.easyinsight.analysis.definitions.WSTrendDefinition")]
public class TrendDefinition extends KPIDefinition {

    public var trendReportID:int;
    public var majorFontSize:int = 32;
    public var minorFontSize:int = 16;
    public var direction:String = "horizontal";

    public function TrendDefinition() {
    }

    override public function supportsEmbeddedFonts():Boolean {
        return true;
    }

    override public function fromSave(savedDef:AnalysisDefinition):void {
        super.fromSave(savedDef);
        this.trendReportID = TrendDefinition(savedDef).trendReportID;
    }

    override public function get type():int {
        return AnalysisDefinition.TREND;
    }

    override public function getFields():ArrayCollection {
        return new ArrayCollection();
    }

    override public function populate(fields:ArrayCollection):void {
        measures = new ArrayCollection();
        for each (var field:AnalysisItem in fields) {
            if (field != null && field.hasType(AnalysisItemTypes.MEASURE)) {
                measures.addItem(field);
            }
        }
    }
}
}
