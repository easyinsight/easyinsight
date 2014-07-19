<!DOCTYPE html>
<%@ page import="com.easyinsight.security.SecurityUtil" %>
<%@ page import="com.easyinsight.analysis.*" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="java.util.List" %>
<%@ page import="com.easyinsight.logging.LogClass" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.easyinsight.html.*" %>
<%@ page import="com.easyinsight.datafeeds.FeedStorage" %>
<%@ page import="com.easyinsight.core.DataSourceDescriptor" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="com.easyinsight.database.Database" %>
<%@ page import="com.easyinsight.database.EIConnection" %>
<%@ page import="com.easyinsight.cache.MemCachedManager" %>
<%@ page import="java.io.ObjectInputStream" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="com.easyinsight.core.InsightDescriptor" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%
    com.easyinsight.security.SecurityUtil.populateThreadLocalFromSession(request);
    try {
        long reportID;
        List<FilterDefinition> drillthroughFilters = new ArrayList<FilterDefinition>();
        String drillthroughArgh = request.getParameter("drillthroughKey");
        WSAnalysisDefinition report = null;
        InsightResponse insightResponse;
        if (drillthroughArgh != null) {
            DrillThroughData drillThroughData = Utils.drillThroughFiltersForReport(drillthroughArgh);
            drillthroughFilters = drillThroughData.getFilters();
            reportID = drillThroughData.getReportID();
            insightResponse = new AnalysisService().openAnalysisIfPossibleByID(reportID);
        } else if (request.getParameter("iframeKey") != null) {
            byte[] bytes = (byte[]) MemCachedManager.get(request.getParameter("iframeKey"));
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            report = (WSAnalysisDefinition) ois.readObject();
            for (FilterDefinition filter : report.getFilterDefinitions()) {
                filter.setShowOnReportView(false);
            }
            SecurityUtil.authorizeFeedAccess(report.getDataFeedID());
            insightResponse = new InsightResponse(InsightResponse.SUCCESS, new InsightDescriptor());
        } else {
            String reportIDString = request.getParameter("reportID");
            insightResponse = new AnalysisService().openAnalysisIfPossible(reportIDString);
        }

        if (insightResponse.getStatus() == InsightResponse.SUCCESS) {
            reportID = insightResponse.getInsightDescriptor().getId();
        } else if (insightResponse.getStatus() == InsightResponse.PRIVATE_ACCESS) {
            throw new ReportAccessException();
        } else {
            throw new com.easyinsight.analysis.ReportNotFoundException("The report does not exist.");
        }

        String showToolbarString = request.getParameter("showToolbar");
        boolean showToolbar = showToolbarString != null && "1".equals(showToolbarString);

        boolean phone = Utils.isPhone(request);
        boolean iPad = Utils.isTablet(request);

        String noBackground = request.getParameter("noBackground");
        if ("1".equals(noBackground)) {

        }

        if (request.getParameter("iframeKey") == null) {
            report = new AnalysisStorage().getAnalysisDefinition(reportID);
        }
        if (report == null) {
            throw new com.easyinsight.analysis.ReportNotFoundException("Attempt made to load report " + reportID + " which doesn't exist.");
        }
        if (drillthroughFilters != null) {
            report.getFilterDefinitions().addAll(drillthroughFilters);
        }
        DataSourceDescriptor dataSourceDescriptor = new FeedStorage().dataSourceURLKeyForDataSource(report.getDataFeedID());

        UIData uiData = Utils.createUIData();
        JSONObject reportJSON = new JSONObject();
        reportJSON.put("name", (Object) report.getName());
        reportJSON.put("id", -1);
        reportJSON.put("filters", new JSONArray());
        reportJSON.put("drillthroughID", (Object) drillthroughArgh);
        JSONObject styleJSON = new JSONObject();
        styleJSON.put("main_stack_start", (Object) "#FFFFFF");
        styleJSON.put("alternative_stack_start", (Object) "#FFFFFF");
        reportJSON.put("styles", styleJSON);
        JSONObject intermediate = new JSONObject();
        reportJSON.put("base", intermediate);
        intermediate.put("show_label", false);
        intermediate.put("id", (Object) (report.getUrlKey() + "_container"));
        intermediate.put("overrides", new JSONArray());
        intermediate.put("filters", new JSONArray());
        intermediate.put("type", (Object) "report");
        JSONObject jj = new JSONObject();
        jj.put("name", (Object) report.getName());
        jj.put("id", (Object) report.getUrlKey());
        HTMLReportMetadata md = new HTMLReportMetadata();
        md.setEmbedded(true);
        jj.put("metadata", report.toJSON(md, new ArrayList<FilterDefinition>()));
        intermediate.put("report", jj);

        String noHeader = request.getParameter("noHeader");
        boolean showHeader = noHeader == null || !"1".equals(noHeader);

        EIConnection c = Database.instance().getConnection();
        JSONObject userObject = new JSONObject();
        try {
            userObject = SecurityUtil.getUserJSON(c, request);
        } finally {
            Database.closeConnection(c);
        }

        userObject.put("embedded", true);
        if (request.getParameter("iframeKey") != null) {
            userObject.put("iframeKey", request.getParameter("iframeKey"));
        }

%>

<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta charset="utf-8">
    <meta name="author" content="">
    <title>Easy Insight &mdash; <%= StringEscapeUtils.escapeHtml(report.getName()) %>
    </title>
    <jsp:include page="../html/bootstrapHeader.jsp"/>
    <jsp:include page="../html/reportDashboardHeader.jsp"/>
    <%
        List<String> jsIncludes = report.javaScriptIncludes();
        for (String jsInclude : jsIncludes) {
    %><%= "<script type=\"text/javascript\" src=\"" + jsInclude + "\"></script>"%><%
    }
    List<String> cssIncludes = report.cssIncludes();
    for (String cssInclude : cssIncludes) {
%><%= "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + cssInclude + "\" />"%><%
    }
%>
    <script type="text/javascript" src="/js/dashboard.js"></script>
    <% if ("1".equals(noBackground)) { %>
    <style type="text/css">
        body {
            background-image:none;
        }

        .well {
            border-style: none;
            border-color: #ffffff;
            border-width: 0;
            border-radius: 0;
            box-shadow: none;
            padding: 0;
            margin: 0;
        }
    </style>
    <% } %>
    <script type="text/javascript" language="JavaScript">
        var dashboardJSON = <%= reportJSON %>;
        var userJSON = <%= userObject %>;
    </script>
</head>
<body>
<% if (showToolbar) { %>
<div class="nav navbar-pills reportNav">
    <div class="container">
        <div class="row">
            <div class="col-md-6 reportBlah">
            </div>
            <div class="col-md-6 reportControlToolbar">
                <div class="btn-toolbar pull-right">
                    <div class="btn-group reportControlBtnGroup">
                        <a class="reportControl" data-toggle="dropdown" href="#">
                            Export the Report
                            <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu">
                            <li>
                                <button class="btn btn-inverse export_excel" type="button"
                                        style="padding:5px;margin:5px;width:150px">Export to Excel
                                </button>
                            </li>
                            <li>
                                <button class="btn btn-inverse export_pdf" type="button"
                                        style="padding:5px;margin:5px;width:150px">Export to PDF
                                </button>
                            </li>
                            <li>
                                <button class="btn btn-inverse" type="button"
                                        onclick="$('#emailReportWindow').modal(true, true, true)"
                                        style="padding:5px;margin:5px;width:150px">Email the Report
                                </button>
                            </li>
                        </ul>
                    </div>
                    <div class="btn-group reportControlBtnGroup">
                        <a class="reportControl" data-toggle="dropdown" href="#">
                            Refresh Data
                            <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu">
                            <li>
                                <button class="btn btn-inverse" type="button" onclick="refreshReport()"
                                        style="padding:5px;margin:5px;width:150px">Refresh the Report
                                </button>
                            </li>
                            <%
                                if(((Boolean) request.getAttribute("public")) != true) {
                                FeedMetadata feedMetadata = new DataService().getFeedMetadata(report.getDataFeedID());
                                if (feedMetadata.getDataSourceInfo().getType() == DataSourceInfo.COMPOSITE_PULL || feedMetadata.getDataSourceInfo().getType() == DataSourceInfo.STORED_PULL) {
                            %>
                            <li>
                                <button class="btn btn-inverse" type="button" id="refreshDataSourceButton"
                                        onclick="refreshDataSource('<%= dataSourceDescriptor.getUrlKey() %>')" style="padding:5px;margin:5px;width:150px">Refresh
                                    Data
                                    Source
                                </button>
                            </li>
                            <%
                                    }
                                }
                            %>
                        </ul>
                    </div>
                    <%
                        boolean visibleFilter = false;
                        for (FilterDefinition filter : report.getFilterDefinitions()) {
                            if (filter.isShowOnReportView()) {
                                visibleFilter = true;
                                break;
                            }
                        }
                        if (visibleFilter) {
                    %>
                    <div class="btn-group reportControlBtnGroup">
                        <a class="reportControl toggle-filters">Toggle Filters</a>
                    </div>
                    <%
                        }
                    %>
                </div>
            </div>
        </div>
    </div>
</div>
<% } %>
<div class="container">
    <jsp:include page="../html/exportModalWindow.jsp">
        <jsp:param name="reportID" value="<%= report.getUrlKey()%>"/>
    </jsp:include>
    <jsp:include page="../html/emailReportWindow.jsp"/>
    <jsp:include page="../html/refreshingDataSource.jsp"/>
    <%= showHeader ? uiData.createHeader(report.getName()) : "" %>
    <div id="base"/>
</div>
</body>
<%
    } catch (ReportAccessException rae) {
        response.sendRedirect(RedirectUtil.getURL(request, "/accessFault.jsp"));
    } catch (ReportNotFoundException e) {
        LogClass.error(e);
        response.sendError(404);
    } finally {
        SecurityUtil.clearThreadLocal();
    }
%>
</html>