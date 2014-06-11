package com.easyinsight.html;

import com.easyinsight.analysis.*;
import com.easyinsight.benchmark.BenchmarkManager;
import com.easyinsight.dashboard.Dashboard;
import com.easyinsight.dashboard.DashboardService;
import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.export.ExportMetadata;
import com.easyinsight.export.ExportService;
import com.easyinsight.logging.LogClass;
import com.easyinsight.security.SecurityUtil;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.hibernate.Session;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: jamesboe
 * Date: 5/31/12
 * Time: 11:03 AM
 */
public class HtmlServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String reportIDString = req.getParameter("reportID");
        if (req.getSession().getAttribute("userID") != null) {
            SecurityUtil.populateThreadLocalFromSession(req);
        } else if (req.getParameter("embedKey") != null) {
            SecurityUtil.populateThreadLocalFromSession(req);
        }
        try {

            InputStream is = req.getInputStream();
            JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
            JSONObject filterObject;
            Object o = null;
            try {
                o = parser.parse(is);

                filterObject = (JSONObject) o;
            } catch (Exception e) {
                String filters = req.getParameter("filters");
                filterObject = (JSONObject) parser.parse(filters);
            }
            if(reportIDString == null)
                reportIDString = (String) filterObject.get("reportID");
            InsightResponse insightResponse = new AnalysisService().openAnalysisIfPossible(reportIDString);
            long reportID;
            if (insightResponse.getStatus() == InsightResponse.SUCCESS) {
                reportID = insightResponse.getInsightDescriptor().getId();
            } else {
                throw new com.easyinsight.security.SecurityException();
            }
            EIConnection conn = Database.instance().getConnection();
            try {
                WSAnalysisDefinition report = new AnalysisService().openAnalysisDefinition(reportID);

                boolean logReport = report.isLogReport();

                // we aren't *just* iterating the report's filters, we're also iterating the dashboard containing the report
                // and retrieving information thereof

                List<FilterDefinition> filters = report.getFilterDefinitions();

                List<FilterDefinition> drillthroughFilters = new ArrayList<FilterDefinition>();

                String drillThroughKey = req.getParameter("drillThroughKey");
                if (drillThroughKey != null) {
                    PreparedStatement queryStmt = conn.prepareStatement("SELECT drillthrough_save_id FROM drillthrough_save WHERE url_key = ?");
                    queryStmt.setString(1, drillThroughKey);
                    ResultSet rs = queryStmt.executeQuery();
                    rs.next();
                    long drillthroughSaveID = rs.getLong(1);
                    PreparedStatement filterStmt = conn.prepareStatement("SELECT filter_id FROM drillthrough_report_save_filter WHERE drillthrough_save_id = ?");
                    filterStmt.setLong(1, drillthroughSaveID);
                    ResultSet filterRS = filterStmt.executeQuery();
                    while (filterRS.next()) {
                        Session hibernateSession = Database.instance().createSession(conn);
                        FilterDefinition filter = (FilterDefinition) hibernateSession.createQuery("from FilterDefinition where filterID = ?").setLong(0, filterRS.getLong(1)).list().get(0);
                        filter.afterLoad();
                        drillthroughFilters.add(filter);
                        hibernateSession.close();
                    }
                }

                Object fromDTObject = filterObject.get("drillthroughKey");
                if (fromDTObject != null) {

                    String fromDT = fromDTObject.toString();
                    PreparedStatement queryStmt = conn.prepareStatement("SELECT drillthrough_save_id FROM drillthrough_save WHERE url_key = ?");
                    queryStmt.setString(1, fromDT);
                    ResultSet rs = queryStmt.executeQuery();
                    rs.next();
                    long drillthroughSaveID = rs.getLong(1);
                    PreparedStatement filterStmt = conn.prepareStatement("SELECT filter_id FROM drillthrough_report_save_filter WHERE drillthrough_save_id = ?");
                    filterStmt.setLong(1, drillthroughSaveID);
                    ResultSet filterRS = filterStmt.executeQuery();
                    while (filterRS.next()) {
                        Session hibernateSession = Database.instance().createSession(conn);
                        FilterDefinition filter = (FilterDefinition) hibernateSession.createQuery("from FilterDefinition where filterID = ?").setLong(0, filterRS.getLong(1)).list().get(0);
                        filter.afterLoad();
                        //drillthroughFilters.add(filter);
                        hibernateSession.close();
                        if (!filter.isShowOnReportView()) {
                            report.getFilterDefinitions().add(filter);
                        }
                    }
                }

                filters.addAll(drillthroughFilters);

                String dashboardIDString = req.getParameter("dashboardID");
                if (dashboardIDString != null) {
                    long dashboardID = Long.parseLong(dashboardIDString);
                    Dashboard dashboard = new DashboardService().getDashboard(dashboardID);
                    filters.addAll(dashboard.filtersForReport(reportID));
                }


                if (filterObject.get("filters") != null) {
                    JSONObject actualFilterObject = (JSONObject) filterObject.get("filters");
                    FilterUtils.adjustFilters(filters, actualFilterObject, report.getName(), logReport);
                } else {
                    FilterUtils.adjustFilters(filters, filterObject, report.getName(), logReport);
                }

                /*
                JSONObject actualFilterObject = (JSONObject) filterObject.get("filters");
                FilterUtils.adjustFilters(filters, actualFilterObject, report.getName(), logReport);
                 */

                InsightRequestMetadata insightRequestMetadata = new InsightRequestMetadata();
                if (req.getParameter("timezoneOffset") != null) {
                    int timezoneOffset = Integer.parseInt(req.getParameter("timezoneOffset"));
                    insightRequestMetadata.setUtcOffset(timezoneOffset);
                }
                ExportMetadata md = ExportService.createExportMetadata(SecurityUtil.getAccountID(false), conn, insightRequestMetadata);
                long start = System.currentTimeMillis();
                doStuff(req, resp, insightRequestMetadata, conn, report, o, md);
                BenchmarkManager.recordBenchmarkForReport("HTMLReportProcessingTime", System.currentTimeMillis() - start,
                        SecurityUtil.getUserID(false), report.getAnalysisID(), conn);
                resp.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
                resp.setHeader("Pragma", "no-cache"); //HTTP 1.0
                resp.setDateHeader("Expires", 0); //prevents caching at the proxy server
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                Database.closeConnection(conn);
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            if (req.getSession().getAttribute("userID") != null) {
                SecurityUtil.clearThreadLocal();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String reportIDString = req.getParameter("reportID");
        if (req.getSession().getAttribute("userID") != null) {
            SecurityUtil.populateThreadLocalFromSession(req);
        }
        try {
            InsightResponse insightResponse = new AnalysisService().openAnalysisIfPossible(reportIDString);
            long reportID;
            if (insightResponse.getStatus() == InsightResponse.SUCCESS) {
                reportID = insightResponse.getInsightDescriptor().getId();
            } else {
                throw new com.easyinsight.security.SecurityException();
            }
            EIConnection conn = Database.instance().getConnection();
            try {
                WSAnalysisDefinition report = new AnalysisService().openAnalysisDefinition(reportID);

                // we aren't *just* iterating the report's filters, we're also iterating the dashboard containing the report
                // and retrieving information thereof

                List<FilterDefinition> filters = report.getFilterDefinitions();

                List<FilterDefinition> drillthroughFilters = new ArrayList<FilterDefinition>();

                String drillThroughKey = req.getParameter("drillThroughKey");
                if (drillThroughKey != null) {
                    PreparedStatement queryStmt = conn.prepareStatement("SELECT drillthrough_save_id FROM drillthrough_save WHERE url_key = ?");
                    queryStmt.setString(1, drillThroughKey);
                    ResultSet rs = queryStmt.executeQuery();
                    rs.next();
                    long drillthroughSaveID = rs.getLong(1);
                    PreparedStatement filterStmt = conn.prepareStatement("SELECT filter_id FROM drillthrough_report_save_filter WHERE drillthrough_save_id = ?");
                    filterStmt.setLong(1, drillthroughSaveID);
                    ResultSet filterRS = filterStmt.executeQuery();
                    while (filterRS.next()) {
                        Session hibernateSession = Database.instance().createSession(conn);
                        FilterDefinition filter = (FilterDefinition) hibernateSession.createQuery("from FilterDefinition where filterID = ?").setLong(0, filterRS.getLong(1)).list().get(0);
                        filter.afterLoad();
                        drillthroughFilters.add(filter);
                        hibernateSession.close();
                    }
                }

                filters.addAll(drillthroughFilters);

                String dashboardIDString = req.getParameter("dashboardID");
                if (dashboardIDString != null) {
                    long dashboardID = Long.parseLong(dashboardIDString);
                    Dashboard dashboard = new DashboardService().getDashboard(dashboardID);
                    filters.addAll(dashboard.filtersForReport(reportID));
                }

                for (FilterDefinition filter : FilterUtils.flattenFilters(filters)) {
                    if (filter instanceof FilterValueDefinition) {
                        FilterValueDefinition filterValueDefinition = (FilterValueDefinition) filter;
                        String value = req.getParameter("filter" + filter.getFilterID());
                        if (value != null) {
                            if (filterValueDefinition.isSingleValue()) {
                                filterValueDefinition.setFilteredValues(Arrays.asList((Object) value));
                            } else {
                                String[] values = value.split(",");
                                List<Object> valueList = new ArrayList<Object>();
                                Collections.addAll(valueList, values);
                                filterValueDefinition.setFilteredValues(valueList);
                            }
                        }
                    } else if (filter instanceof RollingFilterDefinition) {
                        RollingFilterDefinition rollingFilterDefinition = (RollingFilterDefinition) filter;
                        String value = req.getParameter("filter" + filter.getFilterID());
                        if (value != null) {
                            int filterValue = Integer.parseInt(value);
                            rollingFilterDefinition.setInterval(filterValue);
                            if (filterValue == MaterializedRollingFilterDefinition.CUSTOM) {
                                int direction = Integer.parseInt(req.getParameter("filter" + filter.getFilterID() + "direction"));
                                int customValue = Integer.parseInt(req.getParameter("filter" + filter.getFilterID() + "value"));
                                int interval = Integer.parseInt(req.getParameter("filter" + filter.getFilterID() + "interval"));
                                rollingFilterDefinition.setCustomBeforeOrAfter(direction);
                                rollingFilterDefinition.setCustomIntervalAmount(customValue);
                                rollingFilterDefinition.setCustomIntervalType(interval);
                            }
                        }
                    } else if (filter instanceof AnalysisItemFilterDefinition) {
                        AnalysisItemFilterDefinition analysisItemFilterDefinition = (AnalysisItemFilterDefinition) filter;
                        String value = req.getParameter("filter" + filter.getFilterID());
                        if (value != null) {
                            long fieldID = Long.parseLong(value);
                            List<AnalysisItemSelection> possibles = new DataService().possibleFields(analysisItemFilterDefinition, null, null, null);
                            for (AnalysisItemSelection possible : possibles) {
                                if (possible.getAnalysisItem().getAnalysisItemID() == fieldID) {
                                    analysisItemFilterDefinition.setTargetItem(possible.getAnalysisItem());
                                    break;
                                }
                            }
                        }
                    } else if (filter instanceof FlatDateFilter) {
                        FlatDateFilter flatDateFilter = (FlatDateFilter) filter;
                        String value = req.getParameter("filter" + filter.getFilterID());
                        if (value != null) {
                            flatDateFilter.setValue(Integer.parseInt(value));
                        }
                    } else if (filter instanceof MultiFlatDateFilter) {
                        MultiFlatDateFilter multiFlatDateFilter = (MultiFlatDateFilter) filter;
                        String startMonthString = req.getParameter("filter" + filter.getFilterID() + "start");
                        String endMonthString = req.getParameter("filter" + filter.getFilterID() + "end");
                        List<DateLevelWrapper> levels = new ArrayList<DateLevelWrapper>();
                        if (startMonthString != null && endMonthString != null) {
                            int startMonth = Integer.parseInt(startMonthString);
                            int endMonth = Integer.parseInt(endMonthString);
                            for (int i = startMonth; i <= endMonth; i++) {
                                DateLevelWrapper wrapper = new DateLevelWrapper();
                                wrapper.setDateLevel(i);
                                levels.add(wrapper);
                            }
                            multiFlatDateFilter.setLevels(levels);
                        }

                    } else if (filter instanceof FilterDateRangeDefinition) {
                        FilterDateRangeDefinition filterDateRangeDefinition = (FilterDateRangeDefinition) filter;
                        String startDate = req.getParameter("filter" + filter.getFilterID() + "start");
                        String endDate = req.getParameter("filter" + filter.getFilterID() + "end");
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                        if (startDate != null) {
                            filterDateRangeDefinition.setStartDate(dateFormat.parse(startDate));
                        }
                        if (endDate != null) {
                            filterDateRangeDefinition.setEndDate(dateFormat.parse(endDate));
                        }
                    }
                    String enabledParam = req.getParameter("filter" + filter.getFilterID() + "enabled");
                    if (enabledParam != null) {
                        if ("true".equals(enabledParam)) {
                            filter.setEnabled(true);
                        } else {
                            filter.setEnabled(false);
                        }
                    }
                }
                InsightRequestMetadata insightRequestMetadata = new InsightRequestMetadata();
                if (req.getParameter("timezoneOffset") != null) {
                    int timezoneOffset = Integer.parseInt(req.getParameter("timezoneOffset"));
                    insightRequestMetadata.setUtcOffset(timezoneOffset);
                }
                ExportMetadata md = ExportService.createExportMetadata(SecurityUtil.getAccountID(), conn, insightRequestMetadata);
                doStuff(req, resp, insightRequestMetadata, conn, report, md);
                resp.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
                resp.setHeader("Pragma", "no-cache"); //HTTP 1.0
                resp.setDateHeader("Expires", 0); //prevents caching at the proxy server
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                Database.closeConnection(conn);
            }
        } finally {
            if (req.getSession().getAttribute("userID") != null) {
                SecurityUtil.clearThreadLocal();
            }
        }
    }





    protected void doStuff(HttpServletRequest request, HttpServletResponse response, InsightRequestMetadata insightRequestMetadata,
                           EIConnection conn, WSAnalysisDefinition report, ExportMetadata md) throws Exception {

    }

    protected void doStuff(HttpServletRequest request, HttpServletResponse response, InsightRequestMetadata insightRequestMetadata,
                           EIConnection conn, WSAnalysisDefinition report, Object jsonObject, ExportMetadata md) throws Exception {
        doStuff(request, response, insightRequestMetadata, conn, report, md);
    }



    protected void configureAxes(org.json.JSONObject object, WSChartDefinition chart, AnalysisItem xAxisItem, AnalysisItem yAxisItem) throws JSONException {
        configureAxesBase(object, chart, xAxisItem, yAxisItem);
    }

    protected void configureAxes(org.json.JSONObject object, WSChartDefinition chart, AnalysisItem xAxisItem, List<AnalysisItem> items) throws JSONException {
        int aggregation = 0;
        boolean aggChanged = true;
        for (AnalysisItem item : items) {
            if (aggregation == 0) {
                aggregation = item.getFormattingType();
            } else {
                if (item.getFormattingType() != aggregation) {
                    aggChanged = false;
                }
            }
        }
        if (aggChanged) {
            configureAxesBase(object, chart, xAxisItem, items.get(0));
        } else {
            configureAxesBase(object, chart, xAxisItem, null);
        }

    }

    protected void configureAxes(org.json.JSONObject object, WSChartDefinition chart, List<AnalysisItem> items, AnalysisItem yAxisItem) throws JSONException {
        int aggregation = 0;
        boolean aggChanged = true;
        for (AnalysisItem item : items) {
            if (aggregation == 0) {
                aggregation = item.getFormattingType();
            } else {
                if (item.getFormattingType() != aggregation) {
                    aggChanged = false;
                }
            }
        }
        if (aggChanged) {
            configureAxesBase(object, chart, items.get(0), yAxisItem);
        } else {
            configureAxesBase(object, chart, null, yAxisItem);
        }

    }

    protected void configureAxesBase(org.json.JSONObject object, WSChartDefinition chart, @Nullable AnalysisItem xAxisItem,
                                     @Nullable AnalysisItem yAxisItem) throws JSONException {

        if (chart.getxAxisLabel() != null && !"".equals(chart.getxAxisLabel())) {
            object.put("xTitle", chart.getxAxisLabel());
        } else {
            if (xAxisItem == null) {
                object.put("xTitle", "Measures");
            } else {
                object.put("xTitle", xAxisItem.toUnqualifiedDisplay());
            }
        }

        if (xAxisItem != null) {
            object.put("xFormat", createFormatObject(xAxisItem));
        }

        if (chart.getyAxisLabel() != null && !"".equals(chart.getyAxisLabel())) {
            object.put("yTitle", chart.getyAxisLabel());
        } else {
            if (yAxisItem == null) {
                object.put("yTitle", "Measures");
            } else {
                object.put("yTitle", yAxisItem.toUnqualifiedDisplay());
            }
        }

        if (yAxisItem != null) {
            object.put("yFormat", createFormatObject(yAxisItem));
        }

        if (chart.isxAxisMaximumDefined()) {
            object.put("xMax", chart.getxAxisMaximum());
        }
        if (chart.isxAxisMinimumDefined()) {
            object.put("xMin", chart.getxAxisMinimum());
        }
        if (chart.isyAxisMaximumDefined()) {
            object.put("yMax", chart.getyAxisMaximum());
        }
        if (chart.isyAxisMinimumDefined()) {
            object.put("yMin", chart.getyAxisMininum());
        }

        object.put("showLegend", chart.isShowLegend());
    }

    private org.json.JSONObject createFormatObject(AnalysisItem xaxisMeasure) throws JSONException {
        org.json.JSONObject object = new org.json.JSONObject();
        if (xaxisMeasure.hasType(AnalysisItemTypes.MEASURE)) {
            AnalysisMeasure measure = (AnalysisMeasure) xaxisMeasure;
            object.put("type", "measure");
            object.put("precision", measure.getPrecision());
            object.put("numberFormat", measure.getFormattingType());
            object.put("currencySymbol", "$");
            object.put("commaSeparator", ",");
            object.put("decimalSeperator", ".");
        } else {
            object.put("type", "grouping");
        }
        return object;
    }
}
