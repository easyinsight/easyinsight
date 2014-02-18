package com.easyinsight.analysis;

import com.easyinsight.calculations.NamespaceGenerator;
import com.easyinsight.database.EIConnection;
import com.easyinsight.pipeline.Pipeline;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * User: jamesboe
 * Date: 4/11/12
 * Time: 10:02 AM
 */
public class AnalysisItemRetrievalStructure {
    private boolean onStorage;
    private WSAnalysisDefinition report;
    private AnalysisDefinition baseReport;
    private List<String> sections = new ArrayList<String>(Arrays.asList(Pipeline.BEFORE, Pipeline.AFTER, Pipeline.LAST, Pipeline.LAST_FILTERS));
    private String currentSection;
    private InsightRequestMetadata insightRequestMetadata = new InsightRequestMetadata();
    private Map<String, UniqueKey> namespaceMap;
    private EIConnection conn;
    private boolean noCalcs;


    public boolean isNoCalcs() {
        return noCalcs;
    }

    public void setNoCalcs(boolean noCalcs) {
        this.noCalcs = noCalcs;
    }

    public AnalysisItemRetrievalStructure(@Nullable String currentSection, AnalysisItemRetrievalStructure structure) {
        setReport(structure.getReport());
        setConn(structure.getConn());
        if (structure.namespaceMap != null) {
            this.namespaceMap = structure.namespaceMap;
        }
        setInsightRequestMetadata(structure.getInsightRequestMetadata());
        setOnStorage(structure.isOnStorage());
        setBaseReport(structure.baseReport);
        setSections(structure.sections);

        this.currentSection = currentSection;
    }

    public EIConnection getConn() {
        return conn;
    }

    public Map<String, UniqueKey> getNamespaceMap() {
        if (namespaceMap == null) {
            if (report == null) {
                namespaceMap = new HashMap<String, UniqueKey>();
            } else {
                namespaceMap = new NamespaceGenerator().generate(report.getDataFeedID(), report.getAddonReports(), conn);
            }
        }
        return namespaceMap;
    }

    public AnalysisItemRetrievalStructure(@Nullable String currentSection) {
        this.currentSection = currentSection;
    }

    public InsightRequestMetadata getInsightRequestMetadata() {
        return insightRequestMetadata;
    }

    public void setInsightRequestMetadata(InsightRequestMetadata insightRequestMetadata) {
        this.insightRequestMetadata = insightRequestMetadata;
    }

    public List<String> getSections() {
        return sections;
    }

    public boolean onOrAfter(String sectionName) {
        if (currentSection == null) {
            return true;
        }
        int position = sections.indexOf(currentSection);
        int index = sections.indexOf(sectionName);
        return index >= position;
    }

    public void setSections(List<String> sections) {
        this.sections = sections;
    }

    public void setBaseReport(AnalysisDefinition baseReport) {
        this.baseReport = baseReport;
    }

    public WSAnalysisDefinition getReport() {
        return report;
    }

    public void setReport(WSAnalysisDefinition report) {
        this.report = report;
    }

    public void setConn(EIConnection conn) {
        this.conn = conn;
    }

    public List<FilterDefinition> getFilters() {
        if (report != null && report.getFilterDefinitions() != null) {
            return report.getFilterDefinitions();
        }
        if (baseReport != null && baseReport.getFilterDefinitions() != null) {
            return baseReport.getFilterDefinitions();
        }
        return new ArrayList<FilterDefinition>();
    }

    public boolean isOnStorage() {
        return onStorage;
    }

    public void setOnStorage(boolean onStorage) {
        this.onStorage = onStorage;
    }
}
