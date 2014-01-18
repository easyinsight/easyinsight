package com.easyinsight.pipeline;

import com.easyinsight.analysis.UniqueKey;
import com.easyinsight.analysis.WSAnalysisDefinition;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.InsightRequestMetadata;
import com.easyinsight.database.EIConnection;

import java.util.*;

/**
 * User: James Boe
 * Date: May 18, 2009
 * Time: 2:41:44 PM
 */
public class PipelineData implements  Cloneable {
    private WSAnalysisDefinition report;
    private List<AnalysisItem> allItems;
    private Collection<AnalysisItem> reportItems;
    private InsightRequestMetadata insightRequestMetadata;
    private Map<String, String> dataSourceProperties;
    private Set<AnalysisItem> allRequestedItems;
    private Map<UniqueKey, AnalysisItem> uniqueItems;
    private Map<String, UniqueKey> namespaceMap;
    private EIConnection conn;

    public PipelineData(WSAnalysisDefinition report, Collection<AnalysisItem> reportItems, InsightRequestMetadata insightRequestMetadata,
                        List<AnalysisItem> allItems, Map<String, String> dataSourceProperties, Set<AnalysisItem> allRequestedItems,
                        Map<UniqueKey, AnalysisItem> uniqueItems, Map<String, UniqueKey> namespaceMap) {
        this.report = report;
        this.reportItems = reportItems;
        this.insightRequestMetadata = insightRequestMetadata;
        this.namespaceMap = namespaceMap;
        this.allItems = allItems;
        this.dataSourceProperties = dataSourceProperties;
        this.allRequestedItems = allRequestedItems;
        this.uniqueItems = uniqueItems;
    }

    public Map<String, UniqueKey> getNamespaceMap() {
        return namespaceMap;
    }

    public void setNamespaceMap(Map<String, UniqueKey> namespaceMap) {
        this.namespaceMap = namespaceMap;
    }

    public EIConnection getConn() {
        return conn;
    }

    public void setConn(EIConnection conn) {
        this.conn = conn;
    }

    public PipelineData clone() throws CloneNotSupportedException {
        PipelineData clone = (PipelineData) super.clone();
        clone.setReportItems(new ArrayList<AnalysisItem>(reportItems));
        clone.setAllRequestedItems(new HashSet<AnalysisItem>(allRequestedItems));
        return clone;
    }

    public Map<UniqueKey, AnalysisItem> getUniqueItems() {
        return uniqueItems;
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
