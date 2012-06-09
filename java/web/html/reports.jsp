<!DOCTYPE html>
<%@ page import="com.easyinsight.security.SecurityUtil" %>
<%@ page import="com.easyinsight.core.DataSourceDescriptor" %>
<%@ page import="com.easyinsight.core.EIDescriptor" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.util.Collections" %>
<%@ page import="com.easyinsight.core.InsightDescriptor" %>
<%@ page import="java.util.List" %>
<%@ page import="com.easyinsight.userupload.UserUploadService" %>
<%@ page import="com.easyinsight.dashboard.DashboardDescriptor" %>
<%@ page import="com.easyinsight.audit.ActionLog" %>
<%@ page import="com.easyinsight.admin.AdminService" %>
<%@ page import="java.util.Collection" %>
<%@ page import="com.easyinsight.audit.ActionReportLog" %>
<%@ page import="com.easyinsight.audit.ActionDashboardLog" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="com.easyinsight.database.Database" %>
<%@ page import="com.easyinsight.database.EIConnection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="com.easyinsight.datafeeds.FeedService" %>
<%@ page import="com.easyinsight.datafeeds.FeedStorage" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<html lang="en">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">
    <script type="text/javascript" src="/js/jquery-1.7.2.min.js"></script>
    <link href="/css/bootstrap.css" rel="stylesheet">

    <style type="text/css">
        body {
            padding-top: 45px;
            padding-bottom: 40px;
        }
    </style>
    <link href="/css/bootstrap-responsive.css" rel="stylesheet">
    <script type="text/javascript" src="/js/bootstrap.js"></script>
</head>
<body>
<%
    String userName = (String) session.getAttribute("userName");
    com.easyinsight.security.SecurityUtil.populateThreadLocalFromSession(request);
    try {


%>
<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container-fluid">
            <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </a>
            <div class="btn-group pull-right">
                <a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
                    <i class="icon-user"></i> <%= StringEscapeUtils.escapeHtml(userName) %>
                    <span class="caret"></span>
                </a>
                <ul class="dropdown-menu">
                    <%--<li><a href="#">Profile</a></li>
                    <li class="divider"></li>--%>
                    <li><a href="/app/logoutAction.jsp">Sign Out</a></li>
                </ul>
            </div>
            <div class="nav-collapse">
                <ul class="nav">
                    <li><a href="/app/html">Data Sources</a></li>
                    <li class="active"><a href="#">Reports and Dashboards</a></li>
                    <li><a href="flashAppAction.jsp">Full Interface</a></li>
                </ul>
            </div><!--/.nav-collapse -->
        </div>
    </div>
</div>
<div class="container-fluid">
    <div class="row-fluid">
        <div class="span3">
            <img src="/images/logo2.PNG"/>
            <div class="well sidebar-nav">
                <ul class="nav nav-list">
                    <li class="nav-header">Recent Actions</li>
                    <%
                        Collection<ActionLog> actions = new AdminService().getRecentActions();
                        for (ActionLog actionLog : actions) {
                            if (actionLog instanceof ActionReportLog && actionLog.getActionType() == ActionReportLog.VIEW) {
                                ActionReportLog actionReportLog = (ActionReportLog) actionLog;
                                out.println("<li><a href=\"report/" + actionReportLog.getInsightDescriptor().getUrlKey() + "\">View " + actionReportLog.getInsightDescriptor().getName() + "</a></li>");
                            } else if (actionLog instanceof ActionDashboardLog && actionLog.getActionType() == ActionDashboardLog.VIEW) {
                                ActionDashboardLog actionDashboardLog = (ActionDashboardLog) actionLog;
                                out.println("<li><a href=\"dashboard/" + actionDashboardLog.getDashboardDescriptor().getUrlKey() + "\">View " + actionDashboardLog.getDashboardDescriptor().getName() + "</a></li>");
                            }
                        }
                    %>
                </ul>
            </div>
        </div>
        <div class="span9">
            <table class="table table-striped table-bordered">
                <thead>
                <tr>
                    <th>Name</th>
                </tr>
                </thead>
            <%
                String dataSourceKey = request.getParameter("dataSourceID");
                long dataSourceID = new FeedStorage().dataSourceIDForDataSource(dataSourceKey);

                List<EIDescriptor> descriptors = new UserUploadService().getFeedAnalysisTreeForDataSource(new DataSourceDescriptor(null, dataSourceID, 0, false));
                Collections.sort(descriptors, new Comparator<EIDescriptor>() {

                    public int compare(EIDescriptor eiDescriptor, EIDescriptor eiDescriptor1) {
                        String name1 = eiDescriptor.getName() != null ? eiDescriptor.getName().toLowerCase() : "";
                        String name2 = eiDescriptor1.getName() != null ? eiDescriptor1.getName().toLowerCase() : "";
                        return name1.compareTo(name2);
                    }
                });
                for (EIDescriptor descriptor : descriptors) {
                    if (descriptor instanceof InsightDescriptor) {
                        out.println("<tr><td><a href=\"../report/" + descriptor.getUrlKey() + "\">" + descriptor.getName() + "</td></tr>");
                    } else if (descriptor instanceof DashboardDescriptor) {
                        out.println("<tr><td><a href=\"../dashboard/" + descriptor.getUrlKey() + "\">" + descriptor.getName() + "</td></tr>");
                    }
                }
            %>
            </table>
        </div>
    </div>
</div>
</body>
<%
    } finally {
        SecurityUtil.clearThreadLocal();
    }
%>
</html>