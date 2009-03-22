package com.easyinsight.analysis;

import com.easyinsight.analysis.AnalysisItem;

import java.util.*;

/**
 * User: James Boe
 * Date: Jan 10, 2008
 * Time: 7:33:07 PM
 */
public class WSCrosstabDefinition extends WSAnalysisDefinition {

    private List<AnalysisItem> columns;
    private List<AnalysisItem> rows;
    private List<AnalysisItem> measures;
    private Long crosstabDefinitionID;

    public Long getCrosstabDefinitionID() {
        return crosstabDefinitionID;
    }

    public void setCrosstabDefinitionID(Long crosstabDefinitionID) {
        this.crosstabDefinitionID = crosstabDefinitionID;
    }

    public List<AnalysisItem> getColumns() {
        return columns;
    }

    public void setColumns(List<AnalysisItem> columns) {
        this.columns = columns;
    }

    public List<AnalysisItem> getRows() {
        return rows;
    }

    public void setRows(List<AnalysisItem> rows) {
        this.rows = rows;
    }

    public List<AnalysisItem> getMeasures() {
        return measures;
    }

    public void setMeasures(List<AnalysisItem> measures) {
        this.measures = measures;
    }

    public String getDataFeedType() {
        return "Crosstab";
    }

    public Set<AnalysisItem> getAllAnalysisItems() {
        Set<AnalysisItem> AnalysisItems = new HashSet<AnalysisItem>();
        AnalysisItems.addAll(columns);
        AnalysisItems.addAll(rows);
        AnalysisItems.addAll(measures);
        return AnalysisItems;
    }

    protected void createReportStructure(Map<String, AnalysisItem> structure) {
        addItems("column", getColumns(), structure);
        addItems("row", getRows(), structure);
        addItems("measure", getMeasures(), structure);
    }

    public void populateFromReportStructure(Map<String, AnalysisItem> structure) {
        setColumns(items("column", structure));
        setRows(items("row", structure));
        setMeasures(items("measure", structure));
    }
}
