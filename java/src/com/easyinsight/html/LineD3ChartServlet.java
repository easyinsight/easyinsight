package com.easyinsight.html;

import com.easyinsight.analysis.*;
import com.easyinsight.analysis.definitions.WSLineChartDefinition;
import com.easyinsight.analysis.definitions.WSTwoAxisDefinition;
import com.easyinsight.core.DateValue;
import com.easyinsight.core.EmptyValue;
import com.easyinsight.core.NumericValue;
import com.easyinsight.core.Value;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.Feed;
import com.easyinsight.datafeeds.FeedRegistry;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.export.ExportMetadata;
import com.easyinsight.export.ExportService;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: jamesboe
 * Date: 5/23/12
 * Time: 4:56 PM
 */
@WebServlet(value = "/lineChartD3", asyncSupported = true)
public class LineD3ChartServlet extends HtmlServlet {
    protected void doStuff(HttpServletRequest request, HttpServletResponse response, InsightRequestMetadata insightRequestMetadata,
                           EIConnection conn, WSAnalysisDefinition report, ExportMetadata md) throws Exception {
        WSTwoAxisDefinition twoAxisDefinition = (WSTwoAxisDefinition) report;
        AnalysisItem eventPoint = twoAxisDefinition.getEventPoint();
        AnalysisItem eventPointLabel = twoAxisDefinition.getEventPointLabel();
        twoAxisDefinition.setEventPoint(null);
        DataSet dataSet = DataService.listDataSet(report, insightRequestMetadata, conn);
        if (dataSet.getAsyncSavedReport() != null) {
            report = dataSet.getAsyncSavedReport();
        }
        twoAxisDefinition = (WSTwoAxisDefinition) report;

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        DateFormat dFormat = new SimpleDateFormat("MM/dd/yyyy");

        JSONArray events = new JSONArray();
        if (eventPoint != null) {
            WSListDefinition temp = new WSListDefinition();
            temp.setDataFeedID(twoAxisDefinition.getDataFeedID());
            temp.setColumns(Arrays.asList(eventPoint, eventPointLabel));
            Feed feed = FeedRegistry.instance().getFeed(twoAxisDefinition.getDataFeedID(), conn);
            temp.setFilterDefinitions(feed.getIntrinsicFilters(conn));
            temp.setAddedItems(twoAxisDefinition.getAddedItems());
            temp.setAddonReports(twoAxisDefinition.getAddonReports());
            DataSet tempSet = DataService.listDataSet(temp, insightRequestMetadata, conn);
            for (IRow row : tempSet.getRows()) {
                DateValue eventPointValue = (DateValue) row.getValue(eventPoint);
                Value eventPointLabelValue = row.getValue(eventPointLabel);
                JSONObject eventObject = new JSONObject();
                eventObject.put("date", dFormat.format(eventPointValue.getDate()));
                eventObject.put("label", eventPointLabelValue);
                events.put(eventObject);
            }
        }

        JSONObject generalGoalValue = null;
        AnalysisItem goal = twoAxisDefinition.getGoal();
        if (goal != null) {
            WSListDefinition temp = new WSListDefinition();
            temp.setDataFeedID(twoAxisDefinition.getDataFeedID());
            List<AnalysisItem> columns = new ArrayList<>();
            columns.add(goal);
            AnalysisMeasure m = (AnalysisMeasure) goal;
            m.setAggregation(AggregationTypes.AVERAGE);
            temp.setColumns(columns);
            Feed feed = FeedRegistry.instance().getFeed(twoAxisDefinition.getDataFeedID(), conn);
            temp.setFilterDefinitions(feed.getIntrinsicFilters(conn));
            temp.setAddedItems(twoAxisDefinition.getAddedItems());
            temp.setAddonReports(twoAxisDefinition.getAddonReports());
            DataSet tempSet = DataService.listDataSet(temp, insightRequestMetadata, conn);
            IRow row = tempSet.getRow(0);
            /*for (IRow row : tempSet.getRows()) {*/
            Value goalValue = row.getValue(goal);
            generalGoalValue = new JSONObject();
            AnalysisDateDimension date = (AnalysisDateDimension) twoAxisDefinition.getXaxis();

            if (twoAxisDefinition.getGoalDateLevel() == AnalysisDateDimension.MONTH_LEVEL) {
                if (date.getDateLevel() == AnalysisDateDimension.QUARTER_OF_YEAR_LEVEL) {
                    goalValue = new NumericValue(goalValue.toDouble() * 3);
                }
            }

            generalGoalValue.put("goal", goalValue.toDouble());
        }



        JSONObject object = new JSONObject();


        // need series, need ticks

        JSONArray blahArray = new JSONArray();
        JSONArray labelArray = new JSONArray();
        List<String> colors = twoAxisDefinition.createMultiColors();
        if (twoAxisDefinition.isMultiMeasure()) {
            List<AnalysisItem> measures = twoAxisDefinition.getMeasures();
            //calculateScale(measures, dataSet);
            AnalysisDateDimension date = (AnalysisDateDimension) twoAxisDefinition.getXaxis();



            int i = 0;
            for (AnalysisItem measure : measures) {
                List<JSONObject> pointList = new ArrayList<JSONObject>();

                double maxMultiplier = 0;

                if (twoAxisDefinition.isRelativeScale()) {
                    double min = Double.MAX_VALUE;
                    double max = Double.MIN_VALUE;
                    for (IRow row : dataSet.getRows()) {
                        Double doubleValue = row.getValue(measure).toDouble();
                        if (doubleValue > max) {
                            max = doubleValue;
                        }
                        if (doubleValue < min) {
                            min = doubleValue;
                        }
                    }

                    // we're scaling to where the max should be 100 and the min should 1


                    maxMultiplier = max / 100;
                }

                for (IRow row : dataSet.getRows()) {

                    Value v = row.getValue(date);
                    if (!(v instanceof EmptyValue)) {
                        DateValue dateValue = (DateValue) v;
                        JSONObject point = new JSONObject();
                        pointList.add(point);
                        //point.put("x", dateValue.getDate().getTime());
                        point.put("x", dFormat.format(dateValue.getDate()));
                        point.put("dx", dateValue.getDate().getTime());
                        point.put("t", dateValue.getDate().getTime());
                        if (twoAxisDefinition.isRelativeScale()) {
                            point.put("y", row.getValue(measure).toDouble() / maxMultiplier);
                            point.put("sy", row.getValue(measure).toDouble());
                        } else {
                            point.put("y", row.getValue(measure).toDouble());
                        }
                    }
                }
                Collections.sort(pointList, new Comparator<JSONObject>() {

                    public int compare(JSONObject jsonObject, JSONObject jsonObject1) {
                        try {
                            return ((Long)jsonObject.getLong("dx")).compareTo(jsonObject1.getLong("dx"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                JSONArray points = new JSONArray(pointList);
                JSONObject axisObject = new JSONObject();
                axisObject.put("values", points);
                axisObject.put("key", measure.toDisplay());
                String color = colors.get(i % colors.size());
                axisObject.put("color", color);
                blahArray.put(axisObject);
                i++;
            }

        } else {
            AnalysisItem measure = twoAxisDefinition.getMeasure();
            AnalysisDateDimension date = (AnalysisDateDimension) twoAxisDefinition.getXaxis();
            AnalysisItem yAxis = twoAxisDefinition.getYaxis();
            Map<String, Map<Date, Double>> series = new HashMap<String, Map<Date, Double>>();

            Set<Date> xAxisValues = new HashSet<Date>();

            Map<String, String> customColorMap = new HashMap<>();
            for (IRow row : dataSet.getRows()) {
                Value yAxisValueObj = row.getValue(yAxis);
                String yAxisValue = yAxisValueObj.toString();
                String customColor;
                if (yAxisValueObj.getValueExtension() != null && yAxisValueObj.getValueExtension() instanceof TextValueExtension) {
                    TextValueExtension textValueExtension = (TextValueExtension) yAxisValueObj.getValueExtension();
                    if (textValueExtension.getColor() > 0) {
                        customColor = String.format("#%06X", (0xFFFFFF & textValueExtension.getColor()));
                        customColorMap.put(yAxisValue, customColor);
                    }
                }

                Map<Date, Double> array = series.get(yAxisValue);
                if (array == null) {
                    array = new HashMap<>();
                    series.put(yAxisValue, array);
                }
                JSONArray values = new JSONArray();
                Value value = row.getValue(date);
                if (value.type() == Value.DATE) {
                    DateValue dateValue = (DateValue) value;
                    String formattedDate = dateFormat.format(dateValue.getDate());
                    xAxisValues.add(dateValue.getDate());
                    values.put(formattedDate);
                    values.put(row.getValue(measure).toDouble());
                    array.put(dateValue.getDate(), row.getValue(measure).toDouble());
                }
            }



            int i = 0;
            for (Map.Entry<String, Map<Date, Double>> entry : series.entrySet()) {
                JSONObject axisObject = new JSONObject();
                Set<Date> uniques = new HashSet<Date>();
                for (Map.Entry<Date, Double> entry1 : entry.getValue().entrySet()) {
                    uniques.add(entry1.getKey());
                }
                for (Date xAxisValue : xAxisValues) {
                    if (!uniques.contains(xAxisValue)) {
                        JSONArray ph = new JSONArray();
                        ph.put(xAxisValue);
                        ph.put(0.0);
                        entry.getValue().put(xAxisValue, 0.0);
                    }
                }
                labelArray.put(entry.getKey());
                List<Date> dates = new ArrayList<Date>(entry.getValue().keySet());
                Collections.sort(dates);
                JSONArray points = new JSONArray();
                for (Date x : dates) {
                    JSONObject point = new JSONObject();
                    String formattedDate = dateFormat.format(x);
                    point.put("label", formattedDate);
                    //point.put("x", x.getTime());
                    // if there's a start date
                    point.put("x", dFormat.format(x));
                    point.put("t", x.getTime());
                    point.put("y", entry.getValue().get(x));
                    points.put(point);
                }
                axisObject.put("values", points);
                axisObject.put("key", entry.getKey());
                String customColor = customColorMap.get(entry.getKey());
                String color = customColor != null ? customColor : colors.get(i % colors.size());
                axisObject.put("color", color);
                i++;
                blahArray.put(axisObject);
            }

            List<Map<String, Value>> trendLineValues = (List<Map<String, Value>>) dataSet.getAdditionalProperties().get("trendLineData");

            if (trendLineValues != null) {
                JSONArray trendPoints = new JSONArray();

                List<Date> dates = new ArrayList<>();
                Map<Date, Map<String, Value>> m = new HashMap<>();
                for (Map<String, Value> map : trendLineValues) {
                    DateValue dValue = (DateValue) map.get("date");
                    dates.add(dValue.getDate());
                    m.put(dValue.getDate(), map);
                }
                Collections.sort(dates);

                for (Date d : dates) {
                    Map<String, Value> map = m.get(d);
                    DateValue dValue = (DateValue) map.get("date");
                    NumericValue trendValue = (NumericValue) map.get("trend");
                    JSONObject point = new JSONObject();
                    point.put("x", dFormat.format(dValue.getDate()));
                    point.put("label", dateFormat.format(dValue.getDate()));
                    point.put("t", dValue.getDate().getTime());
                    point.put("y", trendValue.toDouble());
                    trendPoints.put(point);
                }
                if (twoAxisDefinition instanceof WSLineChartDefinition){
                    WSLineChartDefinition line = (WSLineChartDefinition) twoAxisDefinition;
                    JSONObject axisObject = new JSONObject();
                    axisObject.put("values", trendPoints);
                    axisObject.put("key", "Trend Line");
                    axisObject.put("color", ExportService.createHexString(line.getTrendLineColor()));
                    blahArray.put(axisObject);
                }
            }

        }

        object.put("values", blahArray);
        object.put("date_format", LineD3ChartServlet.getJSFormat(((AnalysisDateDimension) twoAxisDefinition.getXaxis()).getDateLevel(), md.dateFormat));
        if (!twoAxisDefinition.isMultiMeasure()) {
            configureAxes(object, twoAxisDefinition, twoAxisDefinition.getXaxis(), twoAxisDefinition.getMeasure(), md);
        } else {
            configureAxes(object, twoAxisDefinition, twoAxisDefinition.getXaxis(), twoAxisDefinition.getMeasures(), md);
        }

        object.put("relative_line", twoAxisDefinition.isRelativeScale());

        if (generalGoalValue != null) {
            object.put("single_goal_value", generalGoalValue);
        }

        if (events.length() > 0) {
            object.put("events", events);
        }

        response.setContentType("application/json");
        String jsonString = object.toString();
        response.getOutputStream().write(jsonString.getBytes());
        response.getOutputStream().flush();
    }

    public static
    @Nullable
    String getJSFormat(int dateLevel, int dateFormat) {
        String sdf = null;
        if (dateLevel == AnalysisDateDimension.YEAR_LEVEL) {
            sdf = "%Y";
        } else if (dateLevel == AnalysisDateDimension.MONTH_LEVEL || dateLevel == AnalysisDateDimension.QUARTER_OF_YEAR_LEVEL) {
            if (dateFormat == 0 || dateFormat == 3) {
                sdf = "%m/%Y";
            } else if (dateFormat == 1) {
                sdf = "%Y-%m";
            } else if (dateFormat == 2) {
                sdf = "%m-%Y";
            } else if (dateFormat == 4) {
                sdf = "%m.%Y";
            }
        } else if (dateLevel == AnalysisDateDimension.HOUR_LEVEL ||
                dateLevel == AnalysisDateDimension.MINUTE_LEVEL) {
            if (dateFormat == 0) {
                sdf = "%m/%d/%Y %H:%M";
            } else if (dateFormat == 1) {
                sdf = "%Y-%m-%d %H:%M";
            } else if (dateFormat == 2) {
                sdf = "%d-%m-%Y %H:%M";
            } else if (dateFormat == 3) {
                sdf = "%d/%m/%Y %H:%M";
            } else if (dateFormat == 4) {
                sdf = "%d.%m.%Y %H:%M";
            }
        } else {
            if (dateFormat == 0) {
                sdf = "%m/%d/%Y";
            } else if (dateFormat == 1) {
                sdf = "%Y-%m-%d";
            } else if (dateFormat == 2) {
                sdf = "%d-%m-%Y";
            } else if (dateFormat == 3) {
                sdf = "%d/%m/%Y";
            } else if (dateFormat == 4) {
                sdf = "%d.%m.%Y";
            }
        }
        return sdf;
    }

    private class Scale {
        private double min;
        private double max;

        private Scale(double min, double max) {
            this.min = min;
            this.max = max;
        }
    }

    private Map<AnalysisItem, Scale> calculateScale(List<AnalysisItem> measures, DataSet dataSet) {


        Map<AnalysisItem, Scale> map = new HashMap<>();
        for (AnalysisItem measure : measures) {
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for (IRow row : dataSet.getRows()) {
                Double doubleValue = row.getValue(measure).toDouble();
                if (doubleValue > max) {
                    max = doubleValue;
                }
                if (doubleValue < min) {
                    min = doubleValue;
                }
            }

            // we're scaling to where the max should be 100 and the min should 1

            double maxMultiplier = max / 100;

            // so 2000 = multiplier of 20, divide values by that

            //

            double multipler = 100 / (max - min);

            System.out.println("got multiplier of " + multipler + " for " + measure.toDisplay());

            for (IRow row : dataSet.getRows()) {
                Double doubleValue = row.getValue(measure).toDouble();
                row.addValue(measure.createAggregateKey(), new NumericValue(doubleValue / maxMultiplier));
            }
            map.put(measure, new Scale(min, max));
        }
        return map;
    }
}
