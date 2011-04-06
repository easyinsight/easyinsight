package com.easyinsight.pipeline;

import com.easyinsight.analysis.AnalysisDateDimension;
import com.easyinsight.analysis.WSAnalysisDefinition;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.InsightRequestMetadata;

import java.util.*;

/**
 * User: James Boe
 * Date: May 18, 2009
 * Time: 2:41:44 PM
 */
public class PipelineData {
    private WSAnalysisDefinition report;
    private List<AnalysisItem> allItems;
    private Collection<AnalysisItem> reportItems;
    private InsightRequestMetadata insightRequestMetadata;
    private Map<String, String> dataSourceProperties;
    private Set<AnalysisItem> allRequestedItems;

    public PipelineData(WSAnalysisDefinition report, Collection<AnalysisItem> reportItems, InsightRequestMetadata insightRequestMetadata,
                        List<AnalysisItem> allItems, Map<String, String> dataSourceProperties, Set<AnalysisItem> allRequestedItems) {
        this.report = report;
        this.reportItems = reportItems;
        this.insightRequestMetadata = insightRequestMetadata;
        this.allItems = allItems;
        this.dataSourceProperties = dataSourceProperties;
        this.allRequestedItems = allRequestedItems;
    }

    public void setAllRequestedItems(Set<AnalysisItem> allRequestedItems) {
        this.allRequestedItems = allRequestedItems;
    }

    public Set<AnalysisItem> getAllRequestedItems() {
        return allRequestedItems;
    }

    public Map<String, String> getDataSourceProperties() {
        return dataSourceProperties;
    }

    public List<AnalysisItem> getAllItems() {
        return allItems;
    }

    public WSAnalysisDefinition getReport() {
        return report;
    }

    public void setReport(WSAnalysisDefinition report) {
        this.report = report;
    }

    public Collection<AnalysisItem> getReportItems() {
        return reportItems;
    }

    public void setReportItems(Collection<AnalysisItem> reportItems) {
        this.reportItems = reportItems;
    }

    public InsightRequestMetadata getInsightRequestMetadata() {
        return insightRequestMetadata;
    }

    public void setInsightRequestMetadata(InsightRequestMetadata insightRequestMetadata) {
        this.insightRequestMetadata = insightRequestMetadata;
    }
}
