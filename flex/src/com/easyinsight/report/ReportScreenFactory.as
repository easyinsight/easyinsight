package com.easyinsight.report {
import com.easyinsight.goals.GoalTreeViewContainer;
import com.easyinsight.quicksearch.EIDescriptor;
import com.easyinsight.solutions.InsightDescriptor;
import com.easyinsight.util.IAsyncScreen;
import com.easyinsight.util.IAsyncScreenFactory;

public class ReportScreenFactory implements IAsyncScreenFactory{
    public function ReportScreenFactory() {
        super();
    }

    public function createScreen(descriptor:EIDescriptor):IAsyncScreen {
        if (descriptor.getType() == EIDescriptor.REPORT) {
            var insightDescriptor:InsightDescriptor = descriptor as InsightDescriptor;
            var reportView:ReportView = new ReportView();
            reportView.reportID = insightDescriptor.id;
            reportView.reportType = insightDescriptor.reportType;
            reportView.dataSourceID = insightDescriptor.dataFeedID;
            reportView.showBack = false;
            return reportView;
        } else {
            var goalView:GoalTreeViewContainer = new GoalTreeViewContainer();
            goalView.goalTreeID = descriptor.id;
            goalView.embedded = true;
            return goalView;
        }
    }
}
}