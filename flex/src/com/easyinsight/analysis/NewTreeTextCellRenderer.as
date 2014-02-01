package com.easyinsight.analysis {
import com.easyinsight.analysis.summary.NewSummaryCellRenderer;

public class NewTreeTextCellRenderer extends NewSummaryCellRenderer {

    public function NewTreeTextCellRenderer() {
        super();
        this.multiline = true;
        this.wordWrap = true;
    }


    override protected function setText(text:String):void {
        if (analysisItem.hasType(AnalysisItemTypes.TEXT)) {
            var analysisText:AnalysisText = analysisItem as AnalysisText;
            if (analysisText.html) {
                this.htmlText = text;
            } else {
                this.text = text;
            }
        } else if (analysisItem.hasType(AnalysisItemTypes.DERIVED_GROUPING)) {
            var derivedGrouping:DerivedAnalysisDimension = analysisItem as DerivedAnalysisDimension;
            if (derivedGrouping.html) {
                this.htmlText = text;
            } else {
                this.text = text;
            }
        } else {
            this.text = text;
        }
    }
}
}