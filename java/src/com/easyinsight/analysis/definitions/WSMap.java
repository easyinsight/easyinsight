package com.easyinsight.analysis.definitions;

import com.easyinsight.analysis.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * User: jamesboe
 * Date: 6/5/14
 * Time: 2:09 PM
 */
public class WSMap extends WSAnalysisDefinition {

    private AnalysisItem region;
    private AnalysisItem measure;
    private AnalysisItem latitude;
    private AnalysisItem longitude;
    private AnalysisItem pointMeasure;
    private AnalysisItem pointGrouping;
    private int regionFillStart;
    private int regionFillEnd;
    private int noDataFill = 0xCCCCCC;
    private String map = "US States";
    private List<MultiColor> pointColors = new ArrayList<>();
    private long mapID;
    private transient String[] boundSet;
    private String centerLong;
    private String centerLat;
    private int defaultZoom;
    private int maxZoom;
    private int radius;
    private int blur;

    public String getCenterLong() {
        return centerLong;
    }

    public void setCenterLong(String centerLong) {
        this.centerLong = centerLong;
    }

    public String getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(String centerLat) {
        this.centerLat = centerLat;
    }

    public int getDefaultZoom() {
        return defaultZoom;
    }

    public void setDefaultZoom(int defaultZoom) {
        this.defaultZoom = defaultZoom;
    }

    public int getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(int maxZoom) {
        this.maxZoom = maxZoom;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getBlur() {
        return blur;
    }

    public void setBlur(int blur) {
        this.blur = blur;
    }

    public String[] getBoundSet() {
        return boundSet;
    }

    public void setBoundSet(String[] boundSet) {
        this.boundSet = boundSet;
    }

    public int getNoDataFill() {
        return noDataFill;
    }

    public void setNoDataFill(int noDataFill) {
        this.noDataFill = noDataFill;
    }

    public List<MultiColor> getPointColors() {
        return pointColors;
    }

    public void setPointColors(List<MultiColor> pointColors) {
        this.pointColors = pointColors;
    }

    public AnalysisItem getPointGrouping() {
        return pointGrouping;
    }

    public void setPointGrouping(AnalysisItem pointGrouping) {
        this.pointGrouping = pointGrouping;
    }

    public int getRegionFillStart() {
        return regionFillStart;
    }

    public void setRegionFillStart(int regionFillStart) {
        this.regionFillStart = regionFillStart;
    }

    public int getRegionFillEnd() {
        return regionFillEnd;
    }

    public void setRegionFillEnd(int regionFillEnd) {
        this.regionFillEnd = regionFillEnd;
    }

    public AnalysisItem getLatitude() {
        return latitude;
    }

    public void setLatitude(AnalysisItem latitude) {
        this.latitude = latitude;
    }

    public AnalysisItem getLongitude() {
        return longitude;
    }

    public void setLongitude(AnalysisItem longitude) {
        this.longitude = longitude;
    }

    public AnalysisItem getPointMeasure() {
        return pointMeasure;
    }

    public void setPointMeasure(AnalysisItem pointMeasure) {
        this.pointMeasure = pointMeasure;
    }

    public long getMapID() {
        return mapID;
    }

    public void setMapID(long mapID) {
        this.mapID = mapID;
    }

    public AnalysisItem getRegion() {
        return region;
    }

    public void setRegion(AnalysisItem region) {
        this.region = region;
    }

    public AnalysisItem getMeasure() {
        return measure;
    }

    public void setMeasure(AnalysisItem measure) {
        this.measure = measure;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    @Override
    public String getDataFeedType() {
        return AnalysisTypes.TOPO;
    }

    @Override
    public Set<AnalysisItem> getAllAnalysisItems() {
        Set<AnalysisItem> sets = new HashSet<AnalysisItem>();
        sets.add(region);
        sets.add(measure);
        if (latitude != null && longitude != null && pointMeasure != null) {
            sets.add(latitude);
            sets.add(longitude);
            sets.add(pointMeasure);
            if (pointGrouping != null) {
                sets.add(pointGrouping);
            }
        }
        return sets;
    }

    @Override
    public void createReportStructure(Map<String, AnalysisItem> structure) {
        addItems("region", Arrays.asList(region), structure);
        addItems("measure", Arrays.asList(measure), structure);
        if (latitude != null) {
            addItems("latitude", Arrays.asList(latitude), structure);
        }
        if (longitude != null) {
            addItems("longitude", Arrays.asList(longitude), structure);
        }
        if (pointMeasure != null) {
            addItems("pointMeasure", Arrays.asList(pointMeasure), structure);
        }
        if (pointGrouping != null) {
            addItems("pointGrouping", Arrays.asList(pointGrouping), structure);
        }
    }

    @Override
    public void populateFromReportStructure(Map<String, AnalysisItem> structure) {
        region = firstItem("region", structure);
        measure = firstItem("measure", structure);
        latitude = firstItem("latitude", structure);
        longitude = firstItem("longitude", structure);
        pointMeasure = firstItem("pointMeasure", structure);
        pointGrouping = firstItem("pointGrouping", structure);
    }

    public List<String> createMultiColors() {
        List<MultiColor> multiColors = configuredMultiColors();
        List<String> resultColors = new ArrayList<String>();
        if (multiColors != null && !multiColors.isEmpty()) {
            MultiColor testColor = multiColors.get(0);
            if (testColor.isColor1StartEnabled()) {
                for (MultiColor color : multiColors) {
                    if (color.isColor1StartEnabled()) {
                        resultColors.add(String.format("#%06X", (0xFFFFFF & color.getColor1Start())));
                    }
                }
                return resultColors;
            }
        }
        return Arrays.asList("#F8F877");
    }

    @Override
    public void populateProperties(List<ReportProperty> properties) {
        super.populateProperties(properties);
        map = findStringProperty(properties, "map", "US States");
        regionFillStart = (int) findNumberProperty(properties, "regionFillStart", 0x0);
        regionFillEnd = (int) findNumberProperty(properties, "regionFillEnd", 0x0);
        pointColors = multiColorProperty(properties, "pointColors");
        noDataFill = (int) findNumberProperty(properties, "noDataFill", 0xCCCCCC);
        radius = (int) findNumberProperty(properties, "radius", 15);
        blur = (int) findNumberProperty(properties, "blur", 15);
        centerLat = findStringProperty(properties, "centerLat", "");
        centerLong = findStringProperty(properties, "centerLong", "");
        defaultZoom = (int) findNumberProperty(properties, "defaultZoom", 4);
        maxZoom = (int) findNumberProperty(properties, "maxZoom", 7);
    }

    @Override
    public List<ReportProperty> createProperties() {
        List<ReportProperty> properties = super.createProperties();
        properties.add(new ReportStringProperty("map", map));
        properties.add(new ReportNumericProperty("regionFillStart", regionFillStart));
        properties.add(new ReportNumericProperty("regionFillEnd", regionFillEnd));
        properties.add(new ReportNumericProperty("noDataFill", noDataFill));
        properties.add(ReportMultiColorProperty.fromColors(pointColors, "pointColors"));
        properties.add(new ReportNumericProperty("radius", radius));
        properties.add(new ReportNumericProperty("blur", blur));
        properties.add(new ReportNumericProperty("defaultZoom", defaultZoom));
        properties.add(new ReportNumericProperty("maxZoom", maxZoom));
        properties.add(new ReportStringProperty("centerLat", centerLat));
        properties.add(new ReportStringProperty("centerLong", centerLong));
        return properties;
    }

    protected List<MultiColor> configuredMultiColors() {
        return pointColors;
    }

    @Override
    public JSONObject toJSON(HTMLReportMetadata htmlReportMetadata, List<FilterDefinition> parentDefinitions) throws JSONException {

        JSONObject areaChart = super.toJSON(htmlReportMetadata, parentDefinitions);
        if ("Leaflet".equals(getMap())) {
            areaChart.put("type", "leaflet");
        } else {
            areaChart.put("type", "topomap");
        }
        areaChart.put("key", getUrlKey());
        areaChart.put("url", "/app/topoMap");
        areaChart.put("parameters", new JSONObject());
        areaChart.put("styles", htmlReportMetadata.createStyleProperties());
        return areaChart;
    }
}
