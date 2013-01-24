package com.easyinsight.analysis.definitions;

import com.easyinsight.analysis.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * User: James Boe
 * Date: Mar 20, 2009
 * Time: 7:23:14 PM
 */
public class WSStackedColumnChartDefinition extends WSXAxisDefinition {

    private int chartColor;
    private boolean useChartColor;
    private String columnSort;
    private AnalysisItem stackItem;
    private String labelPosition = "none";
    private int labelFontSize;
    private String labelFontWeight;
    private int labelInsideFontColor;
    private boolean useInsideLabelFontColor;
    private List<MultiColor> multiColors = new ArrayList<MultiColor>();

    public List<MultiColor> getMultiColors() {
        return multiColors;
    }

    public void setMultiColors(List<MultiColor> multiColors) {
        this.multiColors = multiColors;
    }

    public int getLabelFontSize() {
        return labelFontSize;
    }

    public void setLabelFontSize(int labelFontSize) {
        this.labelFontSize = labelFontSize;
    }

    public String getLabelFontWeight() {
        return labelFontWeight;
    }

    public void setLabelFontWeight(String labelFontWeight) {
        this.labelFontWeight = labelFontWeight;
    }

    public int getLabelInsideFontColor() {
        return labelInsideFontColor;
    }

    public void setLabelInsideFontColor(int labelInsideFontColor) {
        this.labelInsideFontColor = labelInsideFontColor;
    }

    public boolean isUseInsideLabelFontColor() {
        return useInsideLabelFontColor;
    }

    public void setUseInsideLabelFontColor(boolean useInsideLabelFontColor) {
        this.useInsideLabelFontColor = useInsideLabelFontColor;
    }

    public String getLabelPosition() {
        return labelPosition;
    }

    public void setLabelPosition(String labelPosition) {
        this.labelPosition = labelPosition;
    }

    public AnalysisItem getStackItem() {
        return stackItem;
    }

    public void setStackItem(AnalysisItem stackItem) {
        this.stackItem = stackItem;
    }

    public int getChartType() {
        return ChartDefinitionState.COLUMN_2D_STACKED;
    }

    public int getChartFamily() {
        return ChartDefinitionState.COLUMN_FAMILY;
    }

    public String getColumnSort() {
        return columnSort;
    }

    public void setColumnSort(String columnSort) {
        this.columnSort = columnSort;
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

    @Override
    public void populateProperties(List<ReportProperty> properties) {
        super.populateProperties(properties);
        chartColor = (int) findNumberProperty(properties, "chartColor", 0);
        useChartColor = findBooleanProperty(properties, "useChartColor", false);
        columnSort = findStringProperty(properties, "columnSort", "Unsorted");
        labelFontWeight = findStringProperty(properties, "labelFontWeight", "none");
        labelFontSize = (int) findNumberProperty(properties, "labelFontSize", 12);
        labelPosition = findStringProperty(properties, "labelPosition", "none");
        labelInsideFontColor = (int) findNumberProperty(properties, "labelInsideFontColor", 0);
        useInsideLabelFontColor = findBooleanProperty(properties, "useInsideLabelFontColor", false);
        multiColors = multiColorProperty(properties, "multiColors");
    }

    @Override
    public List<ReportProperty> createProperties() {
        List<ReportProperty> properties = super.createProperties();
        properties.add(new ReportNumericProperty("chartColor", chartColor));
        properties.add(new ReportBooleanProperty("useChartColor", useChartColor));
        properties.add(new ReportStringProperty("columnSort", columnSort));
        properties.add(new ReportStringProperty("labelPosition", labelPosition));
        properties.add(new ReportNumericProperty("labelFontSize", labelFontSize));
        properties.add(new ReportStringProperty("labelFontWeight", labelFontWeight));
        properties.add(new ReportBooleanProperty("useInsideLabelFontColor", useInsideLabelFontColor));
        properties.add(new ReportNumericProperty("labelInsideFontColor", labelInsideFontColor));
        properties.add(ReportMultiColorProperty.fromColors(multiColors, "multiColors"));
        return properties;
    }

    public void createReportStructure(Map<String, AnalysisItem> structure) {
        super.createReportStructure(structure);
        addItems("stackItem", Arrays.asList(stackItem), structure);
    }

    public void populateFromReportStructure(Map<String, AnalysisItem> structure) {
        super.populateFromReportStructure(structure);
        stackItem = firstItem("stackItem", structure);
    }

    public Set<AnalysisItem> getAllAnalysisItems() {
        Set<AnalysisItem> columnList = super.getAllAnalysisItems();
        columnList.add(stackItem);
        return columnList;
    }

    @Override
    public List<String> javaScriptIncludes() {
        List<String> includes = super.javaScriptIncludes();
        includes.add("/js/plugins/jqplot.gradientBarRenderer.js");
        includes.add("/js/plugins/jqplot.categoryAxisRenderer.min.js");
        includes.add("/js/plugins/jqplot.canvasAxisTickRenderer.min.js");
        includes.add("/js/plugins/jqplot.canvasAxisLabelRenderer.min.js");
        includes.add("/js/plugins/jqplot.canvasTextRenderer.min.js");
        includes.add("/js/visualizations/chart.js");
        includes.add("/js/visualizations/util.js");
        return includes;
    }

    @Override
        protected JSONObject getLegend() throws JSONException {
            JSONObject o = super.getLegend();
            o.put("renderer", "$.jqplot.GradientTableLegendRenderer");
            return o;
        }

    @Override
    public String toHTML(String targetDiv, HTMLReportMetadata htmlReportMetadata) {

        JSONObject params;
        JSONObject fullObject = new JSONObject();
        try {
            Map<String, Object> jsonParams = new LinkedHashMap<String, Object>();
            jsonParams.put("legend", getLegend());
            jsonParams.put("stackSeries", "true");
            JSONObject seriesDefaults = new JSONObject();
            seriesDefaults.put("renderer", "$.jqplot.GradientBarRenderer");
            JSONObject rendererOptions = new JSONObject();
            rendererOptions.put("barDirection", "'vertical'");
            rendererOptions.put("varyBarColor", "true");
            rendererOptions.put("barMargin", 45);
            rendererOptions.put("highlightMouseOver", "true");
            seriesDefaults.put("rendererOptions", rendererOptions);
            jsonParams.put("seriesDefaults", seriesDefaults);
            jsonParams.put("grid", getGrid());
            JSONObject axes = new JSONObject();
            axes.put("xaxis", getGroupingAxis(getXaxis()));
            axes.put("yaxis", getMeasureAxis(getMeasures().get(0)));
            jsonParams.put("axes", axes);
            JSONArray seriesColors = getSeriesColors();
            jsonParams.put("seriesColors", seriesColors);

            params = new JSONObject(jsonParams);
            fullObject.put("jqplotOptions", params);
            JSONObject drillthroughOptions = new JSONObject();
            drillthroughOptions.put("embedded", htmlReportMetadata.isEmbedded());
            fullObject.put("drillthrough", drillthroughOptions);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        String argh = fullObject.toString();
        argh = argh.replaceAll("\"", "");
        String timezoneOffset = "&timezoneOffset='+new Date().getTimezoneOffset()+'";
        String customHeight = htmlReportMetadata.createStyleProperties();
        String xyz = "$.getJSON('/app/stackedChart?reportID="+getUrlKey()+timezoneOffset+"&'+ strParams, Chart.getStackedColumnChart('"+ targetDiv + "', " + argh + ","+customHeight+"))";
        /*return "$.getJSON('/app/stackedChart?reportID="+getUrlKey()+timezoneOffset+"&'+ strParams, function(data) {\n" +
                "                var s1 = data[\"values\"];\n" +
                "                var plot1 = $.jqplot('"+targetDiv+"', s1, " + argh + ");afterRefresh();\n})";*/
        return xyz;
    }

    protected JSONArray getSeriesColors() {
        return transformColors(super.getSeriesColors());
    }

    protected JSONArray transformColors(JSONArray colors) {
            JSONArray newColors = new JSONArray();
            try {

                for (int i = 0; i < colors.length(); i++) {
                    JSONArray gradient = new JSONArray();
                    String color2 = "'#FFFFFF'";
                    Object color = colors.get(i);


                    JSONObject colorStop = new JSONObject();
                    colorStop.put("point", 0);
                    colorStop.put("color", color);
                    gradient.put(colorStop);

                    colorStop = new JSONObject();
                    colorStop.put("point", .15);
                    colorStop.put("color", color2);
                    gradient.put(colorStop);

                    colorStop = new JSONObject();
                    colorStop.put("point", .5);
                    colorStop.put("color", color);
                    gradient.put(colorStop);

                    colorStop = new JSONObject();
                    colorStop.put("point", .9);
                    colorStop.put("color", color);
                    gradient.put(colorStop);

                    colorStop = new JSONObject();
                    colorStop.put("point", 1);
                    colorStop.put("color", color2);
                    gradient.put(colorStop);


                    newColors.put(gradient);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return newColors;
        }
}
