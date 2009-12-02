package com.easyinsight.analysis.definitions;

import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.WSChartDefinition;
import com.easyinsight.analysis.ChartDefinitionState;

import java.util.Map;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

/**
 * User: James Boe
 * Date: Mar 21, 2009
 * Time: 6:45:48 PM
 */
public class WSPlotChartDefinition extends WSChartDefinition {
    private AnalysisItem dimension;
    private AnalysisItem iconGrouping;
    private AnalysisItem xaxisMeasure;
    private AnalysisItem yaxisMeasure;

    private boolean calculateCorrelation;

    public boolean isCalculateCorrelation() {
        return calculateCorrelation;
    }

    public void setCalculateCorrelation(boolean calculateCorrelation) {
        this.calculateCorrelation = calculateCorrelation;
    }

    public AnalysisItem getIconGrouping() {
        return iconGrouping;
    }

    public void setIconGrouping(AnalysisItem iconGrouping) {
        this.iconGrouping = iconGrouping;
    }

    public AnalysisItem getXaxisMeasure() {
        return xaxisMeasure;
    }

    public void setXaxisMeasure(AnalysisItem xaxisMeasure) {
        this.xaxisMeasure = xaxisMeasure;
    }

    public AnalysisItem getYaxisMeasure() {
        return yaxisMeasure;
    }

    public void setYaxisMeasure(AnalysisItem yaxisMeasure) {
        this.yaxisMeasure = yaxisMeasure;
    }

    public AnalysisItem getDimension() {
        return dimension;
    }

    public void setDimension(AnalysisItem dimension) {
        this.dimension = dimension;
    }

    public void createReportStructure(Map<String, AnalysisItem> structure) {
        addItems("xAxisMeasure", Arrays.asList(xaxisMeasure), structure);
        addItems("yAxisMeasure", Arrays.asList(yaxisMeasure), structure);
        addItems("dimension", Arrays.asList(dimension), structure);
        if (iconGrouping != null) {
            addItems("series", Arrays.asList(iconGrouping), structure);
        }
    }

    public void populateFromReportStructure(Map<String, AnalysisItem> structure) {
        xaxisMeasure = firstItem("xAxisMeasure", structure);
        yaxisMeasure = firstItem("yAxisMeasure", structure);
        dimension = firstItem("dimension", structure);
        iconGrouping = firstItem("series", structure);
    }

    public Set<AnalysisItem> getAllAnalysisItems() {
        Set<AnalysisItem> columnList = new HashSet<AnalysisItem>();
        columnList.add(dimension);
        columnList.add(xaxisMeasure);
        columnList.add(yaxisMeasure);
        if (iconGrouping != null) {
            columnList.add(iconGrouping);
        }
        return columnList;
    }

    public int getChartType() {
        return ChartDefinitionState.PLOT_TYPE;
    }

    public int getChartFamily() {
        return ChartDefinitionState.PLOT_FAMILY;
    }
}
