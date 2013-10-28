package com.easyinsight.report {
import com.easyinsight.analysis.EmbeddedControllerLookup;

import com.easyinsight.analysis.IEmbeddedReportController;
import com.easyinsight.dashboard.DashboardStackPositions;
import com.easyinsight.framework.PerspectiveInfo;
import com.easyinsight.genredata.ExchangeItem;
import com.easyinsight.quicksearch.EIDescriptor;
import com.easyinsight.solutions.InsightDescriptor;



import mx.collections.ArrayCollection;

public class ReportAnalyzeSource extends PerspectiveInfo {

    public function ReportAnalyzeSource(insightDescriptor:InsightDescriptor, filters:ArrayCollection = null, exchangeItem:ExchangeItem = null, reportList:ArrayCollection = null,
            reportIndex:int = 0, crumbs:Array = null, originalExchangeItem:EIDescriptor = null, additionalItems:ArrayCollection = null, stackPositions:DashboardStackPositions = null) {
        super(PerspectiveInfo.REPORT_VIEW);
        var properties:Object = new Object();
        properties.reportID = insightDescriptor.id;
        properties.urlKey = insightDescriptor.urlKey;
        properties.reportType = insightDescriptor.reportType;
        properties.dataSourceID = insightDescriptor.dataFeedID;
        properties.parameterFilters = filters;
        properties.exchangeItem = exchangeItem;
        properties.reportList = reportList;
        properties.reportIndex = reportIndex;
        properties.breadCrumbs = crumbs;
        properties.originalExchangeItem = originalExchangeItem;
        properties.additionalItems = additionalItems;
        properties.stackPositions = stackPositions;
        var controllerClass:Class = EmbeddedControllerLookup.controllerForType(insightDescriptor.reportType);
        var controller:IEmbeddedReportController = new controllerClass();
        properties.viewFactory = controller.createEmbeddedView();
        this.properties = properties;
    }
}
}