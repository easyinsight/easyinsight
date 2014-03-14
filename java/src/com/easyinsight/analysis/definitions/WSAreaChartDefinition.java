package com.easyinsight.analysis.definitions;

import com.easyinsight.analysis.*;
import com.easyinsight.preferences.ApplicationSkin;
import flex.messaging.io.ArrayCollection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: James Boe
 * Date: Mar 21, 2009
 * Time: 6:45:30 PM
 */
public class WSAreaChartDefinition extends WSTwoAxisDefinition {

    private String stackingType = "stacked";
    private List<MultiColor> multiColors = new ArrayList<MultiColor>();
    private int legendMaxWidth;

    public String getStackingType() {
        return stackingType;
    }

    public void setStackingType(String stackingType) {
        this.stackingType = stackingType;
    }

    public List<MultiColor> getMultiColors() {
        return multiColors;
    }

    public void setMultiColors(List<MultiColor> multiColors) {
        this.multiColors = multiColors;
    }

    public int getLegendMaxWidth() {
        return legendMaxWidth;
    }

    public void setLegendMaxWidth(int legendMaxWidth) {
        this.legendMaxWidth = legendMaxWidth;
    }

    public int getChartType() {
        return ChartDefinitionState.AREA_2D;
    }

    public int getChartFamily() {
        return ChartDefinitionState.AREA_FAMILY;
    }

    @Override
    public void populateProperties(List<ReportProperty> properties) {
        super.populateProperties(properties);
        stackingType = findStringProperty(properties, "stackingType", "stacked");
        multiColors = multiColorProperty(properties, "multiColors");
        legendMaxWidth = (int) findNumberProperty(properties, "legendMaxWidth", 200);

    }

    @Override
    public List<ReportProperty> createProperties() {
        List<ReportProperty> properties = super.createProperties();
        properties.add(new ReportStringProperty("stackingType", stackingType));
        properties.add(ReportMultiColorProperty.fromColors(multiColors, "multiColors"));
        properties.add(new ReportNumericProperty("legendMaxWidth", legendMaxWidth));
        return properties;
    }

    @Override
    public List<String> javaScriptIncludes() {
        List<String> includes = super.javaScriptIncludes();
//        includes.add("/js/plugins/jqplot.dateAxisRenderer.min.js");
//        includes.add("/js/plugins/jqplot.canvasTextRenderer.min.js");
//        includes.add("/js/plugins/jqplot.canvasAxisTickRenderer.min.js");
//        includes.add("/js/plugins/jqplot.canvasAxisLabelRenderer.min.js");
//        includes.add("/js/plugins/jqplot.enhancedLegendRenderer.min.js");
//        includes.add("/js/visualizations/chart.js");
//        includes.add("/js/visualizations/util.js");
        return includes;
    }

    @Override
    public JSONObject toJSON(HTMLReportMetadata htmlReportMetadata, List<FilterDefinition> parentDefinitions) throws JSONException {
        JSONObject areaChart = super.toJSON(htmlReportMetadata, parentDefinitions);
        areaChart.put("type", "area");
        areaChart.put("key", getUrlKey());
        areaChart.put("url", "/app/twoAxisChart");
        areaChart.put("parameters", getJsonObject());
        areaChart.put("styles", htmlReportMetadata.createStyleProperties());
        return areaChart;
    }

    @Override
    public String toHTML(String targetDiv, HTMLReportMetadata htmlReportMetadata) {
        JSONObject object = getJsonObject();
        String argh = object.toString();
        argh = argh.replaceAll("\"", "");

        String timezoneOffset = "&timezoneOffset='+new Date().getTimezoneOffset()+'";
        String customHeight = htmlReportMetadata.createStyleProperties().toString();
        argh = "$.getJSON('/app/twoAxisChart?reportID=" + getUrlKey() + timezoneOffset + "&'+ strParams, Chart.getCallback('" + targetDiv + "', " + argh + ", true, " + customHeight + "))";
        return argh;
    }

    private JSONObject getJsonObject() {
        JSONObject params;
        JSONObject object = new JSONObject();
        try {
            Map<String, Object> jsonParams = new LinkedHashMap<String, Object>();
            JSONObject legend = getLegend();
            jsonParams.put("legend", legend);
            jsonParams.put("stackSeries", "true");
            jsonParams.put("showMarker", "false");
            JSONObject seriesDefaults = new JSONObject();
            seriesDefaults.put("fill", "true");

            jsonParams.put("seriesDefaults", seriesDefaults);
            JSONObject grid = getGrid();
            jsonParams.put("grid", grid);

            jsonParams.put("axes", getAxes());
            JSONObject highlighter = new JSONObject();
            highlighter.put("show", true);
            highlighter.put("sizeAdjust", 7.5);
            highlighter.put("useAxesFormatters", "true");
            jsonParams.put("highlighter", highlighter);
            JSONObject cursor = new JSONObject();
            cursor.put("show", false);
            jsonParams.put("cursor", cursor);
            JSONArray seriesColors = getSeriesColors();
            jsonParams.put("seriesColors", seriesColors);
            params = new JSONObject(jsonParams);

            object.put("jqplotOptions", params);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return object;
    }

    @Override
    public JSONObject getAxes() throws JSONException {
        JSONObject axes = new JSONObject();
        JSONObject xAxis = getGroupingAxis(getXaxis());
        xAxis.put("renderer", "$.jqplot.DateAxisRenderer");

        JSONObject xAxisTickOptions = xAxis.getJSONObject("tickOptions");
        AnalysisDateDimension date = (AnalysisDateDimension) this.getXaxis();
        if (date.getDateLevel() == AnalysisDateDimension.DAY_LEVEL) {
            xAxisTickOptions.put("formatString", "'%b %#d'");
        } else if (date.getDateLevel() == AnalysisDateDimension.MONTH_LEVEL) {
            xAxisTickOptions.put("formatString", "'%b'");
        } else if (date.getDateLevel() == AnalysisDateDimension.YEAR_LEVEL) {
            xAxisTickOptions.put("formatString", "'%b'");
        } else {
            xAxisTickOptions.put("formatString", "'%b %#d'");
        }

        xAxis.put("tickOptions", xAxisTickOptions);
        axes.put("xaxis", xAxis);
        if (getMeasure() != null) {
            axes.put("yaxis", getMeasureAxis(getMeasure()));
            axisConfigure((JSONObject) axes.get("yaxis"), getyAxisMininum(), isyAxisMinimumDefined(), getyAxisMaximum(), isyAxisMaximumDefined());
        }
        return axes;
    }

    protected boolean supportsMultiField() {
        return isMultiMeasure();
    }

    protected List<AnalysisItem> reportFieldsForMultiField() {
        return getMeasures();
    }

    protected void assignResults(List<AnalysisItem> fields) {
        setMeasures(fields);
    }

    public void renderConfig(ApplicationSkin applicationSkin) {
        if ("Primary".equals(getColorScheme()) && applicationSkin.getMultiColors() != null && applicationSkin.getMultiColors().size() > 0 &&
                applicationSkin.getMultiColors().get(0).isColor1StartEnabled()) {
            setMultiColors(applicationSkin.getMultiColors());
        }
    }
}
