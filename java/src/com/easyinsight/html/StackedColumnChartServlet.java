package com.easyinsight.html;

import com.easyinsight.analysis.*;
import com.easyinsight.analysis.definitions.*;
import com.easyinsight.core.Value;
import com.easyinsight.database.EIConnection;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.export.ExportMetadata;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * User: jamesboe
 * Date: 5/23/12
 * Time: 4:56 PM
 */
public class StackedColumnChartServlet extends HtmlServlet {

    private static interface Populator {
        JSONObject createArray(Double measure, String string, int index) throws JSONException;

        public Integer getIndex(JSONObject jsonObject) throws JSONException;

        public Double getMeasure(JSONObject jsonObject) throws JSONException;
    }

    private static class ColumnPopulator implements Populator {

        public JSONObject createArray(Double measure, String string, int index) throws JSONException {
            JSONObject point = new JSONObject();
            point.put("x", string);
            point.put("y", measure);
            point.put("index", index);
            return point;
        }

        public Integer getIndex(JSONObject jsonObject) throws JSONException {
            return jsonObject.getInt("index");
        }

        public Double getMeasure(JSONObject jsonObject) throws JSONException {
            return jsonObject.getDouble("y");
        }
    }

    private static class BarPopulator implements Populator {

        public JSONObject createArray(Double measure, String string, int index) throws JSONException {
            JSONObject point = new JSONObject();
            point.put("x", string);
            point.put("y", measure);
            point.put("index", index);
            return point;
        }

        public Integer getIndex(JSONObject jsonObject) throws JSONException {
            return jsonObject.getInt("index");
        }

        public Double getMeasure(JSONObject jsonObject) throws JSONException {
            return jsonObject.getDouble("y");
        }
    }

    protected void doStuff(HttpServletRequest request, HttpServletResponse response, InsightRequestMetadata insightRequestMetadata,
                           EIConnection conn, WSAnalysisDefinition report, ExportMetadata md) throws Exception {
        DataSet dataSet = DataService.listDataSet(report, insightRequestMetadata, conn);

        JSONObject pointLabels = new JSONObject();


        boolean dateAxis = false;
        boolean sortStackAscending = false;
        boolean sortStackDescending = false;

        JSONObject seriesDefaults = new JSONObject();
        JSONObject object = new JSONObject();
        // need series, need ticks
        AnalysisItem xAxisItem;
        AnalysisItem measureItem;
        AnalysisItem stackItem;
        AnalysisItem minItem = null;
        final Populator populator;
        if (report instanceof WSStackedColumnChartDefinition) {
            WSStackedColumnChartDefinition columnChartDefinition = (WSStackedColumnChartDefinition) report;
            xAxisItem = columnChartDefinition.getXaxis();
            stackItem = columnChartDefinition.getStackItem();
            measureItem = columnChartDefinition.getMeasures().get(0);
            populator = new ColumnPopulator();
            sortStackAscending = "Stack Ascending".equals(columnChartDefinition.getStackSort());
            sortStackDescending = "Stack Descending".equals(columnChartDefinition.getStackSort());
        } else if (report instanceof WSStackedBarChartDefinition) {
            WSStackedBarChartDefinition columnChartDefinition = (WSStackedBarChartDefinition) report;
            xAxisItem = columnChartDefinition.getYaxis();
            stackItem = columnChartDefinition.getStackItem();
            measureItem = columnChartDefinition.getMeasures().get(0);
            minItem = columnChartDefinition.getMinimumXAxis();
            dateAxis = columnChartDefinition.isDateAxis();
            populator = new BarPopulator();
            if ("inside".equals(columnChartDefinition.getLabelPosition())) {
                seriesDefaults.put("pointLabels", pointLabels);
                pointLabels.put("labels", new JSONArray());
            }
            sortStackAscending = "Stack Ascending".equals(columnChartDefinition.getStackSort());
            sortStackDescending = "Stack Descending".equals(columnChartDefinition.getStackSort());
        } else {
            throw new RuntimeException();
        }

        int i = 1;
        Map<String, List<JSONObject>> seriesMap = new LinkedHashMap<String, List<JSONObject>>();
        Map<String, Integer> indexMap = new HashMap<String, Integer>();
        Map<Integer, String> reverseIndexMap = new HashMap<Integer, String>();

        JSONArray axisNames = new JSONArray();


        JSONObject rendererOptions = new JSONObject();






        Link l = stackItem.defaultLink();

        rendererOptions.put("highlightMouseOver", l != null);

        if (l != null && l instanceof DrillThrough) {
            JSONObject drillthrough = new JSONObject();
            drillthrough.put("reportID", report.getUrlKey());
            drillthrough.put("id", l.createID());
            drillthrough.put("source", stackItem.getAnalysisItemID());
            drillthrough.put("xaxis", xAxisItem.getAnalysisItemID());
            drillthrough.put("stack", stackItem.getAnalysisItemID());
            object.put("drillthrough", drillthrough);
        }

        List<String> colors = ((WSChartDefinition) report).createMultiColors();

        Map<String, Double> indexToMin = new HashMap<>();

        List<Map<String, Object>> seriesList = new ArrayList<>();

        for (IRow row : dataSet.getRows()) {

            String xAxisValue = row.getValue(xAxisItem).toString();
            Value sValue = row.getValue(stackItem);
            String stackValue = sValue.toString();
            Integer index = indexMap.get(xAxisValue);
            if (index == null) {
                axisNames.put(xAxisValue);
                index = i++;
                indexMap.put(xAxisValue, index);
                reverseIndexMap.put(index, xAxisValue);

                if (minItem != null) {
                    double minValue = row.getValue(minItem).toDouble();
                    indexToMin.put(xAxisValue, minValue);
                }
            }


            List<JSONObject> array = seriesMap.get(stackValue);
            if (array == null) {
                Map<String, Object> map = new HashMap<>();
                //JSONObject seriesObj = new JSONObject();
                map.put("label", stackValue);
                map.put("sort", sValue.toSortValue());
                seriesList.add(map);
                array = new ArrayList<>();
                seriesMap.put(stackValue, array);
            }

            Value curValue = row.getValue(measureItem);
            Double measure = curValue.toDouble();
            // need to end up with...
            array.add(populator.createArray(measure, xAxisValue, index));


        }

        if (sortStackAscending) {
            seriesList.sort((o1, o2) -> o1.get("sort").toString().compareTo(o2.get("sort").toString()));
        } else if (sortStackDescending) {
            seriesList.sort((o1, o2) -> o2.get("sort").toString().compareTo(o1.get("sort").toString()));
        }

        JSONArray series = new JSONArray();
        for (Map<String, Object> map : seriesList) {
            JSONObject seriesObject = new JSONObject();
            seriesObject.put("label", map.get("label"));
            series.put(seriesObject);
        }

        Map<String, List<JSONObject>> sortedSeriesMap = new LinkedHashMap<>();
        for (Map<String, Object> map : seriesList) {
            String label = (String) map.get("label");
            sortedSeriesMap.put(label, seriesMap.get(label));
        }
        seriesMap = sortedSeriesMap;



        for (List<JSONObject> array : seriesMap.values()) {
            Set<Integer> contained = new HashSet<Integer>();
            for (JSONObject anArray : array) {
                Integer point = populator.getIndex(anArray);
                contained.add(point);
            }
            for (int j = 1; j < i; j++) {
                if (!contained.contains(j)) {
                    array.add(populator.createArray(0., reverseIndexMap.get(j), j));
                }
            }
            Collections.sort(array, new Comparator<JSONObject>() {

                public int compare(JSONObject jsonArray, JSONObject jsonArray1) {
                    try {
                        Integer i1 = populator.getIndex(jsonArray);
                        Integer i2 = populator.getIndex(jsonArray1);
                        return i1.compareTo(i2);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        List<Integer> zeroIndicies = new ArrayList<Integer>();
        if (seriesMap.entrySet().size() > 0) {
            Map.Entry<String, List<JSONObject>> first = null;
            for (Map.Entry<String, List<JSONObject>> entry : seriesMap.entrySet()) {
                first = entry;
                break;
            }
            for (int k = 0; k < first.getValue().size(); k++) {
                JSONObject jsonArray = first.getValue().get(k);
                Integer curIndex = populator.getIndex(jsonArray);
                Double total = 0.0;
                for (Map.Entry<String, List<JSONObject>> entry : seriesMap.entrySet()) {
                    Double curVal = 0.0;
                    for (JSONObject arr : entry.getValue()) {
                        if (populator.getIndex(arr).equals(curIndex)) {
                            curVal = populator.getMeasure(arr);
                            break;
                        }
                    }
                    total = total + curVal;
                }
                if (total == 0.0) {
                    zeroIndicies.add(k);
                }
            }
        }
        // start from top removing them in reverse order should make it easier
//
        for (int k = zeroIndicies.size() - 1; k >= 0; k--) {
            for (Map.Entry<String, List<JSONObject>> entry : seriesMap.entrySet()) {
                entry.getValue().remove(entry.getValue().get(zeroIndicies.get(k)));
            }
            axisNames.remove(k);
        }


        // series map is keyed on stack value
        // needs to contain x axis value + y value

        JSONArray blahs = new JSONArray();
        int k = 0;
        double maxY = Double.MIN_VALUE;
        Map<String, Double> stackMap = new HashMap<>();

        for (Map.Entry<String, List<JSONObject>> entry : seriesMap.entrySet()) {

            double total = 0;
            for (JSONObject arr : entry.getValue()) {
                total = total + populator.getMeasure(arr);
            }
            if (total != 0) {
                JSONObject axisObject = new JSONObject();
                axisObject.put("key", entry.getKey());

                String color = colors.get(k % colors.size());
                axisObject.put("color", color);
                List<JSONObject> list = entry.getValue();

                if (dateAxis) {
                    for (JSONObject jo : list) {
                        String key = jo.get("x").toString();
                        Double min = stackMap.get(key);
                        if (min == null) {
                            min = indexToMin.get(key);
                            stackMap.put(key, min);
                        }
                        System.out.println("min = " + new Date(min.longValue()));
                        Double y = (Double) jo.get("y");
                        Double delta = y - min;
                        maxY = Math.max(maxY, y);
                        jo.put("y", delta);
                        jo.put("xMin", min);
                        stackMap.put(key, (min + delta));
                    }
                }

                axisObject.put("values", list);
                blahs.put(axisObject);




                /*if (minItem != null && seriesKey.equals(firstLabel)) {
                    for (JSONObject arr : entry.getValue()) {
                        String key = arr.get("x").toString();
                        Double min = indexToMin.get(key);
                        if (min != null) {
                            System.out.println("updating to " + min + " for " + seriesKey + " and " + key);
                            arr.put("xMin", min);
                            //arr.put("y0", min);

                        }
                    }
                }*/
            } else {
                zeroIndicies.add(k);
            }
            k++;
        }

        /*for (int j = zeroIndicies.size() - 1; j >= 0; j--) {
            series.remove(zeroIndicies.get(j));
        }*/
        object.put("maxY", maxY);
        object.put("values", blahs);
        object.put("floatingY", minItem != null);
        configureAxes(object, (WSChartDefinition) report, xAxisItem, measureItem, md);

        if (dateAxis) {
            object.put("dateAxis", dateAxis);
        }

        response.setContentType("application/json");
        System.out.println(object.toString());
        response.getOutputStream().write(object.toString().getBytes());
        response.getOutputStream().flush();
    }
}
