package com.easyinsight.analysis;

import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.dataset.LimitsResults;

import java.util.*;

/**
 * User: James Boe
 * Date: Jan 11, 2008
 * Time: 10:19:11 AM
 */
public class WSListDefinition extends WSAnalysisDefinition {
    private Long listDefinitionID;
    private List<AnalysisItem> columns;
    private boolean showLineNumbers = false;
    private ListLimitsMetadata listLimitsMetadata;

    public ListLimitsMetadata getListLimitsMetadata() {
        return listLimitsMetadata;
    }

    public void setListLimitsMetadata(ListLimitsMetadata listLimitsMetadata) {
        this.listLimitsMetadata = listLimitsMetadata;
    }

    public Long getListDefinitionID() {
        return listDefinitionID;
    }

    public void setListDefinitionID(Long listDefinitionID) {
        this.listDefinitionID = listDefinitionID;
    }

    public List<AnalysisItem> getColumns() {
        return columns;
    }

    public void setColumns(List<AnalysisItem> columns) {
        this.columns = columns;
    }

    public boolean isShowLineNumbers() {
        return showLineNumbers;
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
        this.showLineNumbers = showLineNumbers;
    }

    public String getDataFeedType() {
        return "List";
    }

    public List<AnalysisItem> getAllAnalysisItems() {
        Set<AnalysisItem> columnList = new HashSet<AnalysisItem>();
        for (AnalysisItem item : columns) {
            columnList.add(item);
        }
        columnList.addAll(getLimitFields());
        return new ArrayList<AnalysisItem>(columnList);
    }

    public LimitsResults applyLimits(DataSet dataSet) {
        LimitsResults limitsResults;
        if (listLimitsMetadata != null) {
            int count = dataSet.getRows().size();
            limitsResults = new LimitsResults(count > listLimitsMetadata.getNumber(), count, listLimitsMetadata.getNumber());
            dataSet.sort(listLimitsMetadata.getAnalysisItem(), listLimitsMetadata.isTop());
            dataSet.subset(listLimitsMetadata.getNumber());
        } else {
            limitsResults = super.applyLimits(dataSet);
        }
        return limitsResults;
    }

    public List<AnalysisItem> getLimitFields() {
        if (listLimitsMetadata != null) {
            return Arrays.asList(listLimitsMetadata.getAnalysisItem());
        } else {
            return super.getLimitFields();
        }
    }
}
