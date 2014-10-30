package com.easyinsight.analysis;

import com.easyinsight.database.EIConnection;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.export.ExportMetadata;
import com.easyinsight.export.ExportService;
import com.easyinsight.export.TreeData;
import com.easyinsight.intention.Intention;
import com.easyinsight.intention.IntentionSuggestion;
import com.easyinsight.intention.NewHierarchyIntention;
import com.easyinsight.pipeline.*;
import com.easyinsight.preferences.ApplicationSkin;
import org.apache.poi.ss.usermodel.*;
import org.json.JSONException;
import org.json.JSONObject;

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
    private boolean headerMode;
    private int summaryBackgroundColor;
    private int summaryTextColor;
    private String defaultMeasureAlignment;
    private String defaultGroupingAlignnment;
    private String defaultDateAlignment;

    public String getDefaultMeasureAlignment() {
        return defaultMeasureAlignment;
    }

    public void setDefaultMeasureAlignment(String defaultMeasureAlignment) {
        this.defaultMeasureAlignment = defaultMeasureAlignment;
    }

    public String getDefaultGroupingAlignnment() {
        return defaultGroupingAlignnment;
    }

    public void setDefaultGroupingAlignnment(String defaultGroupingAlignnment) {
        this.defaultGroupingAlignnment = defaultGroupingAlignnment;
    }

    public String getDefaultDateAlignment() {
        return defaultDateAlignment;
    }

    public void setDefaultDateAlignment(String defaultDateAlignment) {
        this.defaultDateAlignment = defaultDateAlignment;
    }

    public int getSummaryBackgroundColor() {
        return summaryBackgroundColor;
    }

    public void setSummaryBackgroundColor(int summaryBackgroundColor) {
        this.summaryBackgroundColor = summaryBackgroundColor;
    }

    public int getSummaryTextColor() {
        return summaryTextColor;
    }

    public void setSummaryTextColor(int summaryTextColor) {
        this.summaryTextColor = summaryTextColor;
    }

    public boolean isHeaderMode() {
        return headerMode;
    }

    public void setHeaderMode(boolean headerMode) {
        this.headerMode = headerMode;
    }

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
        headerMode = findBooleanProperty(properties, "headerMode", false);
        summaryTextColor = (int) findNumberProperty(properties, "summaryTextColor", 0);
        summaryBackgroundColor = (int) findNumberProperty(properties, "summaryBackgroundColor", 0xaaaaaa);
        defaultMeasureAlignment = findStringProperty(properties, "defaultMeasureAlignment", "none");
        defaultDateAlignment = findStringProperty(properties, "defaultDateAlignment", "none");
        defaultGroupingAlignnment = findStringProperty(properties, "defaultGroupingAlignment", "none");
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
        properties.add(new ReportBooleanProperty("headerMode", headerMode));
        properties.add(new ReportNumericProperty("summaryTextColor", summaryTextColor));
        properties.add(new ReportNumericProperty("summaryBackgroundColor", summaryBackgroundColor));
        properties.add(new ReportStringProperty("defaultMeasureAlignment", defaultMeasureAlignment));
        properties.add(new ReportStringProperty("defaultDateAlignment", defaultDateAlignment));
        properties.add(new ReportStringProperty("defaultGroupingAlignment", defaultGroupingAlignnment));
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
            intentions.add(new NewHierarchyIntention(NewHierarchyIntention.HIERARCHY));
        } else {
            return super.createIntentions(fields, type);
        }
        return intentions;
    }

    public List<INestedComponent> endComponents() {
        List<INestedComponent> components = new ArrayList<INestedComponent>();
        components.add(new HierarchyComponent((AnalysisHierarchyItem) hierarchy));
        return components;
    }

    @Override
    public JSONObject toJSON(HTMLReportMetadata htmlReportMetadata, List<FilterDefinition> parentDefinitions) throws JSONException {
        JSONObject list = super.toJSON(htmlReportMetadata, parentDefinitions);
        list.put("type", "tree");
        list.put("key", getUrlKey());
        list.put("properties", jsonProperties());
        list.put("url", "/app/tree");
        return list;
    }

    public JSONObject jsonProperties() {

        JSONObject p = new JSONObject();
        try {
            List<ReportProperty> properties = createProperties();
            populateProperties(properties);
            for (ReportProperty property : properties) {
                if (property instanceof ReportNumericProperty)
                    p.put(property.getPropertyName(), ((ReportNumericProperty) property).getValue());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return p;
    }

    public void renderConfig(ApplicationSkin applicationSkin) {
        if ("Primary".equals(getColorScheme())) {
            if (applicationSkin.isSummaryTextColorEnabled()) {
                setSummaryTextColor(applicationSkin.getSummaryTextColor());
            }
            if (applicationSkin.isSummaryBackgroundColorEnabled()) {
                setSummaryBackgroundColor(applicationSkin.getSummaryBackgroundColor());
            }
            if (applicationSkin.isHeaderStartEnabled()) {
                setHeaderColor1(applicationSkin.getHeaderStart());
            }
            if (applicationSkin.isReportHeaderTextColorEnabled()) {
                setHeaderTextColor(applicationSkin.getReportHeaderTextColor());
            }
            if (applicationSkin.isHeaderEndEnabled()) {
                setHeaderColor2(applicationSkin.getHeaderEnd());
            }
            if (applicationSkin.isTableColorStartEnabled()) {
                setRowColor1(applicationSkin.getTableColorStart());
            }
            if (applicationSkin.isTableColorEndEnabled()) {
                setRowColor2(applicationSkin.getTableColorEnd());
            }
        }


        if (!"none".equals(defaultGroupingAlignnment)) {
            for (AnalysisItem item : getItems()) {
                if (item.hasType(AnalysisItemTypes.DIMENSION) && !item.hasType(AnalysisItemTypes.DATE_DIMENSION)) {
                    if (item.getReportFieldExtension() == null) {
                        TextReportFieldExtension textReportFieldExtension = new TextReportFieldExtension();
                        textReportFieldExtension.setAlign(defaultGroupingAlignnment);
                        item.setReportFieldExtension(textReportFieldExtension);
                    } else if (item.getReportFieldExtension() instanceof TextReportFieldExtension) {
                        TextReportFieldExtension textReportFieldExtension = (TextReportFieldExtension) item.getReportFieldExtension();
                        if(textReportFieldExtension.getAlign() == null || "Default".equals(textReportFieldExtension.getAlign())) {
                            textReportFieldExtension.setAlign(defaultGroupingAlignnment);
                        }
                    }
                }
            }
        }
        if (!"none".equals(defaultMeasureAlignment)) {
            for (AnalysisItem item : getItems()) {
                if (item.hasType(AnalysisItemTypes.MEASURE)) {
                    if (item.getReportFieldExtension() == null) {
                        TextReportFieldExtension textReportFieldExtension = new TextReportFieldExtension();
                        textReportFieldExtension.setAlign(defaultMeasureAlignment);
                        item.setReportFieldExtension(textReportFieldExtension);
                    } else if (item.getReportFieldExtension() instanceof TextReportFieldExtension) {
                        TextReportFieldExtension textReportFieldExtension = (TextReportFieldExtension) item.getReportFieldExtension();
                        if(textReportFieldExtension.getAlign() == null || "Default".equals(textReportFieldExtension.getAlign())) {
                            textReportFieldExtension.setAlign(defaultMeasureAlignment);
                        }
                    }
                }
            }
        }
        if (!"none".equals(defaultDateAlignment)) {
            for (AnalysisItem item : getItems()) {
                if (item.hasType(AnalysisItemTypes.DATE_DIMENSION)) {
                    if (item.getReportFieldExtension() == null) {
                        TextReportFieldExtension textReportFieldExtension = new TextReportFieldExtension();
                        textReportFieldExtension.setAlign(defaultDateAlignment);
                        item.setReportFieldExtension(textReportFieldExtension);
                    } else if (item.getReportFieldExtension() instanceof TextReportFieldExtension) {
                        TextReportFieldExtension textReportFieldExtension = (TextReportFieldExtension) item.getReportFieldExtension();
                        if(textReportFieldExtension.getAlign() == null || "Default".equals(textReportFieldExtension.getAlign())) {
                            textReportFieldExtension.setAlign(defaultDateAlignment);
                        }
                    }
                }
            }
        }
    }

    public boolean toExcel(InsightRequestMetadata insightRequestMetadata, EIConnection conn, Sheet sheet, Map<ExportService.StyleKey, ExportService.Style> styleMap, Workbook workbook) throws SQLException {
        ExportMetadata exportMetadata = ExportService.createExportMetadata(conn);
        DataSet dataSet = DataService.listDataSet(this, insightRequestMetadata, conn);
        PipelineData pipelineData = dataSet.getPipelineData();

        // create headers on sheet

        int i = 0;

        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);

        {
            TextReportFieldExtension textReportFieldExtension = null;
            if (hierarchy.getReportFieldExtension() != null && hierarchy.getReportFieldExtension() instanceof TextReportFieldExtension) {
                textReportFieldExtension = (TextReportFieldExtension) hierarchy.getReportFieldExtension();
            }

            int width;
            if (textReportFieldExtension != null && textReportFieldExtension.getFixedWidth() > 0) {
                width = textReportFieldExtension.getFixedWidth() / 15 * 256;
            } else if (hierarchy.getWidth() > 0) {
                width = Math.max((hierarchy.getWidth() / 15 * 256), 5000);
            } else {
                width = 5000;
            }

            sheet.setColumnWidth(i, width);

            Cell headerCell = headerRow.createCell(i++);
            headerCell.setCellValue(ExportService.createRichTextString(hierarchy.toUnqualifiedDisplay(), headerCell));
            Font font = workbook.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            headerCell.setCellStyle(cellStyle);
        }

        for (AnalysisItem analysisItem : getItems()) {

            TextReportFieldExtension textReportFieldExtension = null;
            if (analysisItem.getReportFieldExtension() != null && analysisItem.getReportFieldExtension() instanceof TextReportFieldExtension) {
                textReportFieldExtension = (TextReportFieldExtension) analysisItem.getReportFieldExtension();
            }

            int width;
            if (textReportFieldExtension != null && textReportFieldExtension.getFixedWidth() > 0) {
                width = textReportFieldExtension.getFixedWidth() / 15 * 256;
            } else if (analysisItem.getWidth() > 0) {
                width = Math.max((analysisItem.getWidth() / 15 * 256), 5000);
            } else {
                width = 5000;
            }

            sheet.setColumnWidth(i, width);

            Cell headerCell = headerRow.createCell(i++);
            headerCell.setCellValue(ExportService.createRichTextString(analysisItem.toUnqualifiedDisplay(), headerCell));
            Font font = workbook.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            headerCell.setCellStyle(cellStyle);
        }

        TreeData treeData = new TreeData(this, (AnalysisHierarchyItem) hierarchy, exportMetadata, dataSet);
        for (IRow row : dataSet.getRows()) {
            treeData.addRow(row);
        }
        List<TreeRow> rows = treeData.toTreeRows(pipelineData);
        int rowNum = 0;
        for (TreeRow row : rows) {
            rowNum = row.toExcel(insightRequestMetadata, conn, sheet, rowNum, styleMap, exportMetadata, this);
        }

        return dataSet.getRows().size() > 0;
    }
}