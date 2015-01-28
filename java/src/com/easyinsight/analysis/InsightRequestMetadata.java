package com.easyinsight.analysis;

import com.easyinsight.intention.IntentionSuggestion;
import com.easyinsight.pipeline.Pipeline;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.io.Serializable;

/**
 * User: James Boe
 * Date: Oct 28, 2008
 * Time: 11:22:35 AM
 */
public class InsightRequestMetadata implements Serializable {
    private Date now = new Date();
    private int depth = 0;
    private boolean cacheForHTML;
    private transient ZoneId zoneID;
    private boolean runningAsync;
    private AnalysisDateDimension baseDate;
    private int utcOffset;
    private transient boolean avoidKeyDisplayCollisions;
    private boolean noCache;
    private List<AnalysisItemOverride> hierarchyOverrides = new ArrayList<AnalysisItemOverride>();
    private boolean aggregateQuery = true;
    private List<JoinOverride> joinOverrides = new ArrayList<JoinOverride>();
    private boolean optimized;
    private boolean traverseAllJoins;
    private boolean logReport;
    private Collection<AnalysisItem> reportItems;
    private List<AnalysisItem> additionalAnalysisItems = new ArrayList<AnalysisItem>();
    private transient Map<UniqueKey, AnalysisItem> uniqueIteMap = new HashMap<UniqueKey, AnalysisItem>();
    private transient Map<String, UniqueKey> fieldToUniqueMap = new HashMap<String, UniqueKey>();
    private transient Map<AnalysisItem, Set<String>> pipelineAssignmentMap = new HashMap<AnalysisItem, Set<String>>();
    private transient Map<AnalysisItem, String> derivedFieldAssignmentMap = new HashMap<AnalysisItem, String>();
    private transient boolean optimizeDays;
    private transient Map<String, Boolean> timeshiftState = new HashMap<String, Boolean>();
    private transient boolean noAsync;

    private transient List<ReportAuditEvent> auditEvents = new ArrayList<ReportAuditEvent>();
    private transient List<String> warnings = new ArrayList<String>();

    private transient Collection<FilterDefinition> reportFilters;

    private transient Set<FilterDefinition> suppressedFilters = new HashSet<FilterDefinition>();

    private transient List<IntentionSuggestion> suggestions = new ArrayList<IntentionSuggestion>();

    private transient String targetCurrency;
    private transient Map<AnalysisItem, AnalysisItem> currencyMap = new HashMap<AnalysisItem, AnalysisItem>();
    private List<AddonReport> addonReports;
    private boolean noDataOnNoJoin;
    private String ip;
    private transient boolean noLogging;
    private transient Map<String, List<String>> fieldAudits = new LinkedHashMap<>();
    private transient Map<String, List<String>> filterAudits = new LinkedHashMap<>();

    private long dataSourceID;

    private transient boolean aggregationRowChanged;

    private transient boolean noAggregation;

    private transient List<AnalysisItem> allItems;
    private transient AnalysisItemRetrievalStructure structure;

    private transient Set<AnalysisItem> originalDateItems;

    public boolean isRunningAsync() {
        return runningAsync;
    }

    public void setRunningAsync(boolean runningAsync) {
        this.runningAsync = runningAsync;
    }

    public Map<String, Boolean> getTimeshiftState() {
        if (timeshiftState == null) {
            timeshiftState = new HashMap<>();
        }
        return timeshiftState;
    }

    public long getDataSourceID() {
        return dataSourceID;
    }

    public void setDataSourceID(long dataSourceID) {
        this.dataSourceID = dataSourceID;
    }

    public boolean isCacheForHTML() {
        return cacheForHTML;
    }

    public void setCacheForHTML(boolean cacheForHTML) {
        this.cacheForHTML = cacheForHTML;
    }

    public boolean isNoAsync() {
        return noAsync;
    }

    public void setNoAsync(boolean noAsync) {
        this.noAsync = noAsync;
    }

    public ZoneId createZoneID() {
        if (this.zoneID != null) {
            return this.zoneID;
        }
        return ZoneId.ofOffset("", ZoneOffset.ofHours(-(getUtcOffset() / 60)));
    }

    public ZoneId getZoneID() {
        return zoneID;
    }

    public void setZoneID(ZoneId zoneID) {
        this.zoneID = zoneID;
    }

    public void setTimeshiftState(Map<String, Boolean> timeshiftState) {
        this.timeshiftState = timeshiftState;
    }

    public boolean isAggregationRowChanged() {
        return aggregationRowChanged;
    }

    public void setAggregationRowChanged(boolean aggregationRowChanged) {
        this.aggregationRowChanged = aggregationRowChanged;
    }

    public Set<AnalysisItem> getOriginalDateItems() {
        return originalDateItems;
    }

    public void setOriginalDateItems(Set<AnalysisItem> originalDateItems) {
        this.originalDateItems = originalDateItems;
    }

    public boolean isAvoidKeyDisplayCollisions() {
        return avoidKeyDisplayCollisions;
    }

    public void setAvoidKeyDisplayCollisions(boolean avoidKeyDisplayCollisions) {
        this.avoidKeyDisplayCollisions = avoidKeyDisplayCollisions;
    }

    public boolean isNoAggregation() {
        return noAggregation;
    }

    public void setNoAggregation(boolean noAggregation) {
        this.noAggregation = noAggregation;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isOptimizeDays() {
        return optimizeDays;
    }

    public void setOptimizeDays(boolean optimizeDays) {
        this.optimizeDays = optimizeDays;
    }

    public List<AnalysisItem> getAllItems() {
        return allItems;
    }

    public void setAllItems(List<AnalysisItem> allItems) {
        this.allItems = allItems;
    }

    public AnalysisItemRetrievalStructure getStructure() {
        return structure;
    }

    public void setStructure(AnalysisItemRetrievalStructure structure) {
        this.structure = structure;
    }

    public Map<String, List<String>> getFieldAudits() {
        return fieldAudits;
    }

    public AnalysisDateDimension getBaseDate() {
        return baseDate;
    }

    public void setBaseDate(AnalysisDateDimension baseDate) {
        this.baseDate = baseDate;
    }

    public Map<String, List<String>> getFilterAudits() {
        return filterAudits;
    }

    public void setFilterAudits(Map<String, List<String>> filterAudits) {
        this.filterAudits = filterAudits;
    }

    public void addAudit(AnalysisItem field, String audit) {
        if (fieldAudits == null) {
            fieldAudits = new HashMap<>();
        }
        List<String> audits = fieldAudits.get(field.toDisplay());
        if (audits == null) {
            audits = new ArrayList<String>();
            fieldAudits.put(field.toDisplay(), audits);
        }
        audits.add(audit);
    }

    public void addFieldAudit(String fieldName, String audit) {
        if (fieldAudits == null) {
            fieldAudits = new HashMap<>();
        }
        List<String> audits = fieldAudits.get(fieldName);
        if (audits == null) {
            audits = new ArrayList<String>();
            fieldAudits.put(fieldName, audits);
        }
        audits.add(audit);
    }

    public void addAudit(FilterDefinition filter, String audit) {
        if (filterAudits == null) {
            filterAudits = new HashMap<>();
        }
        List<String> audits = filterAudits.get(filter.label(false));
        if (audits == null) {
            audits = new ArrayList<String>();
            filterAudits.put(filter.label(false), audits);
        }
        audits.add(audit);
    }

    public List<ReportAuditEvent> getAuditEvents() {
        return auditEvents;
    }

    public void setAuditEvents(List<ReportAuditEvent> auditEvents) {
        this.auditEvents = auditEvents;
    }

    public Collection<FilterDefinition> getReportFilters() {
        return reportFilters;
    }

    public void setReportFilters(Collection<FilterDefinition> reportFilters) {
        this.reportFilters = reportFilters;
    }

    private transient Map<String, Boolean> filterOverrideMap = new HashMap<String, Boolean>();

    private transient int fetchSize;

    private transient Map<AnalysisItem, Boolean> distinctFieldMap = new HashMap<AnalysisItem, Boolean>();

    private transient Collection<FilterDefinition> filters;

    public Collection<FilterDefinition> getFilters() {
        return filters;
    }

    public void setFilters(Collection<FilterDefinition> filters) {
        this.filters = filters;
    }

    public boolean isLogReport() {
        return logReport;
    }

    public void setLogReport(boolean logReport) {
        this.logReport = logReport;
    }

    public List<String> getWarnings() {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public Map<String, Boolean> getFilterOverrideMap() {
        if (filterOverrideMap == null) {
            filterOverrideMap = new HashMap<>();
        }
        return filterOverrideMap;
    }

    public void setFilterOverrideMap(Map<String, Boolean> filterOverrideMap) {
        this.filterOverrideMap = filterOverrideMap;
    }

    public Set<FilterDefinition> getSuppressedFilters() {
        if (suppressedFilters == null) {
            suppressedFilters = new HashSet<>();
        }
        return suppressedFilters;
    }

    public void setSuppressedFilters(Set<FilterDefinition> suppressedFilters) {
        this.suppressedFilters = suppressedFilters;
    }

    private long databaseTime = 0;

    public Map<AnalysisItem, Boolean> getDistinctFieldMap() {
        if (distinctFieldMap == null) {
            distinctFieldMap = new HashMap<>();
        }
        return distinctFieldMap;
    }

    public void setDistinctFieldMap(Map<AnalysisItem, Boolean> distinctFieldMap) {
        this.distinctFieldMap = distinctFieldMap;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public boolean isNoDataOnNoJoin() {
        return noDataOnNoJoin;
    }

    public void setNoDataOnNoJoin(boolean noDataOnNoJoin) {
        this.noDataOnNoJoin = noDataOnNoJoin;
    }

    public boolean isNoLogging() {
        return noLogging;
    }

    public void setNoLogging(boolean noLogging) {
        this.noLogging = noLogging;
    }

    public void addDatabaseTime(long time) {
        databaseTime += time;
    }

    public long getDatabaseTime() {
        return databaseTime;
    }

    public List<AnalysisItem> getAdditionalAnalysisItems() {
        return additionalAnalysisItems;
    }

    public void setAdditionalAnalysisItems(List<AnalysisItem> additionalAnalysisItems) {
        this.additionalAnalysisItems = additionalAnalysisItems;
    }

    public List<AddonReport> getAddonReports() {
        return addonReports;
    }

    public void setAddonReports(List<AddonReport> addonReports) {
        this.addonReports = addonReports;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Map<AnalysisItem, AnalysisItem> getCurrencyMap() {
        if (currencyMap == null) {
            currencyMap = new HashMap<>();
        }
        return currencyMap;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public void assignDerived(AnalysisItem analysisItem, String pipeline) {
        derivedFieldAssignmentMap.put(analysisItem, pipeline);
    }

    public String getDerived(AnalysisItem analysisItem) {
        if (derivedFieldAssignmentMap == null) {
            derivedFieldAssignmentMap = new HashMap<>();
        }
        String pipeline = derivedFieldAssignmentMap.get(analysisItem);
        if (pipeline == null) {
            if (analysisItem.hasType(AnalysisItemTypes.CALCULATION)) {
                pipeline = ((AnalysisCalculation) analysisItem).getPipelineName();
            } else if (analysisItem.hasType(AnalysisItemTypes.DERIVED_DIMENSION)) {
                pipeline = ((DerivedAnalysisDimension) analysisItem).getPipelineName();
            } else if (analysisItem.hasType(AnalysisItemTypes.DERIVED_DATE)) {
                pipeline = ((DerivedAnalysisDateDimension) analysisItem).getPipelineName();
            }
            derivedFieldAssignmentMap.put(analysisItem, pipeline);
        }
        return pipeline;
    }

    public void assign(AnalysisItem analysisItem, String pipeline) {
        if (pipelineAssignmentMap == null) {
            pipelineAssignmentMap = new HashMap<>();
        }
        Set<String> pipelines = pipelineAssignmentMap.get(analysisItem);
        if (pipelines == null) {
            pipelines = new HashSet<String>();
            pipelineAssignmentMap.put(analysisItem, pipelines);
        }
        pipelines.add(pipeline);
    }

    public Set<String> getPipelines(AnalysisItem analysisItem) {
        if (pipelineAssignmentMap == null) {
            pipelineAssignmentMap = new HashMap<>();
        }
        Set<String> pipelines = pipelineAssignmentMap.get(analysisItem);
        if (pipelines == null) {
            pipelines = new HashSet<String>();
            pipelineAssignmentMap.put(analysisItem, pipelines);
        }
        return pipelines;
    }

    public List<IntentionSuggestion> getSuggestions() {
        return suggestions;
    }

    public Map<UniqueKey, AnalysisItem> getUniqueIteMap() {
        if (uniqueIteMap == null) {
            uniqueIteMap = new HashMap<>();
        }
        return uniqueIteMap;
    }

    public void setUniqueIteMap(Map<UniqueKey, AnalysisItem> uniqueIteMap) {
        this.uniqueIteMap = uniqueIteMap;
    }

    public Map<String, UniqueKey> getFieldToUniqueMap() {
        if (fieldToUniqueMap == null) {
            fieldToUniqueMap = new HashMap<>();
        }
        return fieldToUniqueMap;
    }

    public void setFieldToUniqueMap(Map<String, UniqueKey> fieldToUniqueMap) {
        this.fieldToUniqueMap = fieldToUniqueMap;
    }

    public Collection<AnalysisItem> getReportItems() {
        return reportItems;
    }

    public void setReportItems(Collection<AnalysisItem> reportItems) {
        this.reportItems = reportItems;
    }

    public boolean isTraverseAllJoins() {
        return traverseAllJoins;
    }

    public void setTraverseAllJoins(boolean traverseAllJoins) {
        this.traverseAllJoins = traverseAllJoins;
    }

    public boolean isOptimized() {
        return optimized;
    }

    public void setOptimized(boolean optimized) {
        this.optimized = optimized;
    }

    public List<JoinOverride> getJoinOverrides() {
        return joinOverrides;
    }

    public void setJoinOverrides(List<JoinOverride> joinOverrides) {
        this.joinOverrides = joinOverrides;
    }

    public boolean isAggregateQuery() {
        return aggregateQuery;
    }

    public void setAggregateQuery(boolean aggregateQuery) {
        this.aggregateQuery = aggregateQuery;
    }

    public List<AnalysisItemOverride> getHierarchyOverrides() {

        if (hierarchyOverrides == null) {
            hierarchyOverrides = new ArrayList<>();
        }
        return hierarchyOverrides;
    }

    public void setHierarchyOverrides(List<AnalysisItemOverride> hierarchyOverrides) {
        this.hierarchyOverrides = hierarchyOverrides;
    }

    public boolean isNoCache() {
        return noCache;
    }

    public void setNoCache(boolean noCache) {
        this.noCache = noCache;
    }

    public int getUtcOffset() {
        return utcOffset;
    }

    public void setUtcOffset(int utcOffset) {
        this.utcOffset = utcOffset;
    }

    public Date getNow() {
        return now;
    }

    public void setNow(Date now) {
        this.now = now;
    }

    private Map<String, Pipeline> pipelineMap = new HashMap<String, Pipeline>();

    private Map<String, List<AnalysisItem>> pipelineFieldMap = new HashMap<String, List<AnalysisItem>>();

    public Pipeline findPipeline(String name) {
        return pipelineMap.get(name);
    }

    public Map<String, Pipeline> getPipelineMap() {
        if (pipelineMap == null) {
            pipelineMap = new HashMap<>();
        }
        return pipelineMap;
    }

    public void putPipeline(String name, Pipeline pipeline) {
        pipelineMap.put(name, pipeline);
    }

    private transient List<String> intermediatePipelines = new ArrayList<String>();

    public List<String> getIntermediatePipelines() {
        if (intermediatePipelines == null) {
            intermediatePipelines = new ArrayList<>();
        }
        return intermediatePipelines;
    }

    public void setIntermediatePipelines(List<String> intermediatePipelines) {
        this.intermediatePipelines = intermediatePipelines;
    }

    private Map<String, String> pipelineAssignments = new HashMap<String, String>();

    public void assignFieldToPipeline(String field, String pipelineName) {
        if (pipelineAssignments == null) {
            pipelineAssignments = new HashMap<>();
        }
        pipelineAssignments.put(field, pipelineName);
    }

    public String getPipelineNameForField(String field) {
        return pipelineAssignments.get(field);
    }

    public List<AnalysisItem> getFieldsForPipeline(String name) {
        if (pipelineFieldMap == null) {
            pipelineFieldMap = new HashMap<>();
        }
        return pipelineFieldMap.get(name);
    }

    private Map<String, String> filterAssignments = new HashMap<String, String>();

    public String getPipelineNameForFilter(String filter) {
        return filterAssignments.get(filter);
    }

    public void assignFilterToPipeline(String field, String pipelineName) {
        if (filterAssignments == null) {
            filterAssignments = new HashMap<>();
        }
        filterAssignments.put(field, pipelineName);
    }

    public void pipelineAssign(AnalysisItem analysisItem) {
        String name = getPipelineNameForField(analysisItem.toDisplay());
        if (name != null) {
            if (derivedFieldAssignmentMap == null) {
                derivedFieldAssignmentMap = new HashMap<>();
            }
            derivedFieldAssignmentMap.put(analysisItem, name);
            if (pipelineFieldMap == null) {
                pipelineFieldMap = new HashMap<>();
            }
            List<AnalysisItem> fields = pipelineFieldMap.get(name);
            if (fields == null) {
                fields = new ArrayList<>();
                pipelineFieldMap.put(name, fields);
            }
            assign(analysisItem, name);
            fields.add(analysisItem);
        }
        if (analysisItem.getFilters() != null) {
            for (FilterDefinition filter : analysisItem.getFilters()) {
                if (filter.getFilterName() != null && !"".equals(filter.getFilterName())) {
                    name = getPipelineNameForField(filter.getFilterName());
                    filter.setPipelineName(name);
                }
            }
        }
    }

    public void pipelineAssign(FilterDefinition filter) {
        if (filter.getFilterName() != null && !"".equals(filter.getFilterName())) {
            String name = getPipelineNameForFilter(filter.getFilterName());
            if (name != null) {
                filter.setPipelineName(name);
            }
        }
    }

    public List<IntentionSuggestion> generateSuggestions() {
        List<IntentionSuggestion> suggestionList = new ArrayList<IntentionSuggestion>();
        if (!isAggregationRowChanged()) {
            /*suggestionList.add(new IntentionSuggestion("turn off aggregate query if possible", "We recommend turning off aggregate query if possible to speed up the report load time.",
                    IntentionSuggestion.SCOPE_REPORT, IntentionSuggestion.TURN_OFF_AGGREGATE_QUERY, IntentionSuggestion.YOU_SHOULD_DO_THIS));*/
        }
        if (suggestions != null) {
            suggestionList.addAll(suggestions);
        }
        return suggestionList;
    }
}
