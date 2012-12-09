package com.easyinsight.analysis;

import com.easyinsight.intention.Intention;
import com.easyinsight.intention.IntentionSuggestion;
import com.easyinsight.intention.NewHierarchyIntention;
import com.easyinsight.pipeline.*;

import java.sql.SQLException;
import java.util.*;

/**
 * User: James Boe
 * Date: Jul 17, 2008
 * Time: 7:45:24 PM
 */
public class WSTreeDefinition extends WSAnalysisDefinition {

    private long treeDefinitionID;
    private int rowColor1;
    private int rowColor2;
    private int headerColor1;
    private int headerColor2;
    private int textColor;
    private int headerTextColor;
    private boolean autoExpandAll;
    private boolean summaryTotal;

    public boolean isSummaryTotal() {
        return summaryTotal;
    }

    public void setSummaryTotal(boolean summaryTotal) {
        this.summaryTotal = summaryTotal;
    }

    public boolean isAutoExpandAll() {
        return autoExpandAll;
    }

    public void setAutoExpandAll(boolean autoExpandAll) {
        this.autoExpandAll = autoExpandAll;
    }

    public long getTreeDefinitionID() {
        return treeDefinitionID;
    }

    public void setTreeDefinitionID(long treeDefinitionID) {
        this.treeDefinitionID = treeDefinitionID;
    }

    public String getDataFeedType() {
        return AnalysisTypes.TREE;
    }

    private AnalysisItem hierarchy;
    private List<AnalysisItem> items;

    public AnalysisItem getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(AnalysisItem hierarchy) {
        this.hierarchy = hierarchy;
    }

    public List<AnalysisItem> getItems() {
        return items;
    }

    public void setItems(List<AnalysisItem> items) {
        this.items = items;
    }

    public void createReportStructure(Map<String, AnalysisItem> structure) {
        addItems("hierarchy", Arrays.asList(hierarchy), structure);
        Collections.sort(items, new Comparator<AnalysisItem>() {

            public int compare(AnalysisItem analysisItem, AnalysisItem analysisItem1) {
                return new Integer(analysisItem.getItemPosition()).compareTo(analysisItem1.getItemPosition());
            }
        });
        addItems("items", items, structure);
    }

    public void populateFromReportStructure(Map<String, AnalysisItem> structure) {
        hierarchy = firstItem("hierarchy", structure);
        items = items("items", structure);
    }

    public Set<AnalysisItem> getAllAnalysisItems() {
        Set<AnalysisItem> columnList = new HashSet<AnalysisItem>();
        if (hierarchy != null) {
            AnalysisHierarchyItem item = (AnalysisHierarchyItem) hierarchy;
            for (HierarchyLevel level : item.getHierarchyLevels()) {
                columnList.add(level.getAnalysisItem());
            }
        }
        columnList.addAll(items);
        return columnList;
    }

    public void updateMetadata() {
        int position = 0;
        AnalysisHierarchyItem item = (AnalysisHierarchyItem) hierarchy;
        for (HierarchyLevel level : item.getHierarchyLevels()) {
            level.getAnalysisItem().setItemPosition(position++);
        }
        for (AnalysisItem otherItem : items) {
            otherItem.setItemPosition(position + otherItem.getItemPosition());
        }
        int hierarchySortSequence = item.getSortSequence();
        if (hierarchySortSequence > 0) {
            int newSortSequence = hierarchySortSequence;
            for (HierarchyLevel level : item.getHierarchyLevels()) {
                level.getAnalysisItem().setSortSequence(newSortSequence++);
                level.getAnalysisItem().setWidth(item.getWidth());
            }
            for (AnalysisItem otherItem : items) {
                if (otherItem.getSortSequence() > hierarchySortSequence) {
                    otherItem.setSortSequence(newSortSequence + otherItem.getSortSequence());
                }
            }
        }
    }

    public int getRowColor1() {
        return rowColor1;
    }

    public void setRowColor1(int rowColor1) {
        this.rowColor1 = rowColor1;
    }

    public int getRowColor2() {
        return rowColor2;
    }

    public void setRowColor2(int rowColor2) {
        this.rowColor2 = rowColor2;
    }

    public int getHeaderColor1() {
        return headerColor1;
    }

    public void setHeaderColor1(int headerColor1) {
        this.headerColor1 = headerColor1;
    }

    public int getHeaderColor2() {
        return headerColor2;
    }

    public void setHeaderColor2(int headerColor2) {
        this.headerColor2 = headerColor2;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getHeaderTextColor() {
        return headerTextColor;
    }

    public void setHeaderTextColor(int headerTextColor) {
        this.headerTextColor = headerTextColor;
    }

    @Override
    public List<IComponent> createComponents() {
        List<IComponent> components = super.createComponents();
        if (summaryTotal) {
            components.add(new ListSummaryComponent());
        }
        return components;
    }

    @Override
    public void populateProperties(List<ReportProperty> properties) {
        super.populateProperties(properties);
        rowColor1 = (int) findNumberProperty(properties, "rowColor1", 0xffffff);
        rowColor2 = (int) findNumberProperty(properties, "rowColor2", 0xF7F7F7);
        headerColor1 = (int) findNumberProperty(properties, "headerColor1", 0xffffff);
        headerColor2 = (int) findNumberProperty(properties, "headerColor2", 0xEFEFEF);
        textColor = (int) findNumberProperty(properties, "textColor", 0x000000);
        headerTextColor = (int) findNumberProperty(properties, "headerTextColor", 0x000000);
        autoExpandAll = findBooleanProperty(properties, "autoExpandAll", false);
        summaryTotal = findBooleanProperty(properties, "summaryTotal", false);
    }

    public List<ReportProperty> createProperties() {
        List<ReportProperty> properties = super.createProperties();
        properties.add(new ReportNumericProperty("rowColor1", rowColor1));
        properties.add(new ReportNumericProperty("rowColor2", rowColor2));
        properties.add(new ReportNumericProperty("headerColor1", headerColor1));
        properties.add(new ReportNumericProperty("headerColor2", headerColor2));
        properties.add(new ReportNumericProperty("textColor", textColor));
        properties.add(new ReportNumericProperty("headerTextColor", headerTextColor));
        properties.add(new ReportBooleanProperty("autoExpandAll", autoExpandAll));
        properties.add(new ReportBooleanProperty("summaryTotal", summaryTotal));
        return properties;
    }

    @Override
    public List<IntentionSuggestion> suggestIntentions(WSAnalysisDefinition report) {
        List<IntentionSuggestion> suggestions = super.suggestIntentions(report);
        WSTreeDefinition tree = (WSTreeDefinition) report;
        if (tree.getHierarchy() == null) {
            suggestions.add(new IntentionSuggestion("Create a Hierarchy",
                    "This action will create a new hierarchy of fields.",
                    IntentionSuggestion.SCOPE_REPORT, IntentionSuggestion.NEW_HIERARCHY, IntentionSuggestion.OTHER));

        }
        return suggestions;
    }

    public List<Intention> createIntentions(List<AnalysisItem> fields, int type) throws SQLException {
        List<Intention> intentions = new ArrayList<Intention>();
        if (type == IntentionSuggestion.NEW_HIERARCHY) {
            intentions.add(new NewHierarchyIntention());
        } else {
            throw new RuntimeException("Unrecognized intention type");
        }
        return intentions;
    }

    public List<INestedComponent> endComponents() {
        List<INestedComponent> components = new ArrayList<INestedComponent>();
        components.add(new HierarchyComponent((AnalysisHierarchyItem) hierarchy));
        return components;
    }
}