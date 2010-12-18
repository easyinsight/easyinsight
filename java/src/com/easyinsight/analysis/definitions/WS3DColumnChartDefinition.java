package com.easyinsight.analysis.definitions;

import com.easyinsight.analysis.*;

import java.util.List;

/**
 * User: James Boe
 * Date: Mar 21, 2009
 * Time: 4:59:19 PM
 */
public class WS3DColumnChartDefinition extends WSXAxisDefinition {

    private int chartColor;
    private boolean useChartColor;
    private String columnSort;

    public int getChartType() {
        return ChartDefinitionState.COLUMN_3D;
    }

    public int getChartFamily() {
        return ChartDefinitionState.COLUMN_FAMILY;
    }

    public int getChartColor() {
        return chartColor;
    }

    public void setChartColor(int chartColor) {
        this.chartColor = chartColor;
    }

    public boolean isUseChartColor() {
        return useChartColor;
    }

    public void setUseChartColor(boolean useChartColor) {
        this.useChartColor = useChartColor;
    }

    public String getColumnSort() {
        return columnSort;
    }

    public void setColumnSort(String columnSort) {
        this.columnSort = columnSort;
    }

    @Override
    public void populateProperties(List<ReportProperty> properties) {
        super.populateProperties(properties);
        chartColor = (int) findNumberProperty(properties, "chartColor", 0);
        useChartColor = findBooleanProperty(properties, "useChartColor", false);
        columnSort = findStringProperty(properties, "columnSort", "Unsorted");
    }

    @Override
    public List<ReportProperty> createProperties() {
        List<ReportProperty> properties = super.createProperties();
        properties.add(new ReportNumericProperty("chartColor", chartColor));
        properties.add(new ReportBooleanProperty("useChartColor", useChartColor));
        properties.add(new ReportStringProperty("columnSort", columnSort));
        return properties;
    }
}
