<!DOCTYPE html>
<%@ page import="com.easyinsight.security.SecurityUtil" %>
<%@ page import="com.easyinsight.preferences.ApplicationSkin" %>
<%@ page import="com.easyinsight.analysis.*" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="com.easyinsight.datafeeds.FeedStorage" %>
<%@ page import="java.util.List" %>
<%@ page import="com.easyinsight.preferences.ApplicationSkinSettings" %>
<%@ page import="org.hibernate.Session" %>
<%@ page import="com.easyinsight.database.Database" %>
<%@ page import="com.easyinsight.preferences.ImageDescriptor" %>
<%@ page import="com.easyinsight.core.DataSourceDescriptor" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<html lang="en">
<%
    String userName = (String) session.getAttribute("userName");
    com.easyinsight.security.SecurityUtil.populateThreadLocalFromSession(request);
    try {
        String reportIDString = request.getParameter("reportID");
        InsightResponse insightResponse = new AnalysisService().openAnalysisIfPossible(reportIDString);
        long reportID;
        if (insightResponse.getStatus() == InsightResponse.SUCCESS) {
            reportID = insightResponse.getInsightDescriptor().getId();
        } else {
            throw new com.easyinsight.security.SecurityException();
        }
        boolean phone = request.getHeader("User-Agent").toLowerCase().matches(".*(android|avantgo|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino).*")||request.getHeader("User-Agent").toLowerCase().substring(0,4).matches("1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\\-(n|u)|c55\\/|capi|ccwa|cdm\\-|cell|chtm|cldc|cmd\\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\\-s|devi|dica|dmob|do(c|p)o|ds(12|\\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\\-|_)|g1 u|g560|gene|gf\\-5|g\\-mo|go(\\.w|od)|gr(ad|un)|haie|hcit|hd\\-(m|p|t)|hei\\-|hi(pt|ta)|hp( i|ip)|hs\\-c|ht(c(\\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\\-(20|go|ma)|i230|iac( |\\-|\\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\\/)|klon|kpt |kwc\\-|kyo(c|k)|le(no|xi)|lg( g|\\/(k|l|u)|50|54|e\\-|e\\/|\\-[a-w])|libw|lynx|m1\\-w|m3ga|m50\\/|ma(te|ui|xo)|mc(01|21|ca)|m\\-cr|me(di|rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\\-2|po(ck|rt|se)|prox|psio|pt\\-g|qa\\-a|qc(07|12|21|32|60|\\-[2-7]|i\\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\\-|oo|p\\-)|sdk\\/|se(c(\\-|0|1)|47|mc|nd|ri)|sgh\\-|shar|sie(\\-|m)|sk\\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\\-|v\\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\\-|tdg\\-|tel(i|m)|tim\\-|t\\-mo|to(pl|sh)|ts(70|m\\-|m3|m5)|tx\\-9|up(\\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|xda(\\-|2|g)|yas\\-|your|zeto|zte\\-");
        WSAnalysisDefinition report = new AnalysisStorage().getAnalysisDefinition(reportID);
        if (report == null) {
            throw new RuntimeException("Attempt made to load report " + reportID + " which doesn't exist.");
        }
        List<FilterDefinition> drillthroughFilters = (List<FilterDefinition>) session.getAttribute("drillthroughFiltersFor" + report.getAnalysisID());
        if (drillthroughFilters != null) {
            report.getFilterDefinitions().addAll(drillthroughFilters);
        }
        DataSourceDescriptor dataSourceDescriptor = new FeedStorage().dataSourceURLKeyForDataSource(report.getDataFeedID());

        ApplicationSkin applicationSkin;
        String headerStyle;

        Session hibernateSession = Database.instance().createSession();
        try {
            applicationSkin = ApplicationSkinSettings.retrieveSkin(SecurityUtil.getUserID(), hibernateSession, SecurityUtil.getAccountID());
            headerStyle = "width:100%;overflow: hidden;";
        } finally {
            hibernateSession.close();
        }
        ImageDescriptor headerImageDescriptor = null;
        String headerTextStyle = "width: 100%;text-align: center;font-size: 18px;padding-top:10px;";
        if (applicationSkin != null && applicationSkin.isReportHeader()) {
            headerImageDescriptor = applicationSkin.getReportHeaderImage();
            int reportBackgroundColor = applicationSkin.getReportBackgroundColor();
            headerStyle += "background-color: " + String.format("#%06X", (0xFFFFFF & reportBackgroundColor));
            headerTextStyle += "color: " + String.format("#%06X", (0xFFFFFF & applicationSkin.getReportTextColor()));
        }

%>

<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta charset="utf-8">
    <meta name="author" content="">
    <title>Easy Insight &mdash; <%= StringEscapeUtils.escapeHtml(report.getName()) %></title>
    <script type="text/javascript" src="/js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="/js/date.js"></script>
    <script type="text/javascript" src="/js/jquery.datePicker.js"></script>
    <link href="/css/bootstrap.css" rel="stylesheet">



    <link href="/css/datePicker.css" rel="stylesheet" />
    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="/js/html5.js"></script>
    <![endif]-->
    <!--[if lt IE 9]><script language="javascript" type="text/javascript" src="/js/excanvas.js"></script><![endif]-->

    <script type="text/javascript" src="/js/jquery.jqplot.js"></script>

    <style type="text/css">

        body {
            padding-top: 40px;
        }

        #refreshDiv {
            display: none;
        }

        #problemHTML {
            display: none;
        }

        <%
            if (phone) {
                %>
        #filterRow {
            display: none;
        }
                <%
            }
        %>

    </style>

    <link href="/css/bootstrap-responsive.css" rel="stylesheet" />
    <link href="/css/app.css" rel="stylesheet" />
    <script type="text/javascript" src="/js/bootstrap.js"></script>

    <%
        List<String> jsIncludes = report.javaScriptIncludes();
        for (String jsInclude : jsIncludes) {
            out.println("<script type=\"text/javascript\" src=\"" + jsInclude + "\"></script>");
        }
        List<String> cssIncludes = report.cssIncludes();
        for (String cssInclude : cssIncludes) {
            out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\""+cssInclude+"\" />");
        }
    %>
    <script type="text/javascript">

        var filterBase = {};

        Date.firstDayOfWeek = 0;
        Date.format = 'yyyy/mm/dd';

        $(document).ready(function() {
            refreshReport();
        });

        function updateFilter(name, key, refreshFunction) {
            var optionMenu = document.getElementById(name);
            var chosenOption = optionMenu.options[optionMenu.selectedIndex];
            var keyedFilter = filterBase[key];
            if (keyedFilter == null) {
                keyedFilter = {};
                filterBase[key] = keyedFilter;
            }
            keyedFilter[name] = chosenOption.value;
            refreshFunction();
        }

        function updateRangeFilter(key, refreshFunction) {
            var keyedFilter = filterBase[key];
            if (keyedFilter == null) {
                keyedFilter = {};
                filterBase[key] = keyedFilter;
            }
            keyedFilter[key + "start"] = document.getElementById(key + 'start').value;
            keyedFilter[key + "end"] = document.getElementById(key + 'end').value;
            refreshFunction();
        }

        var filtersShown = true;

        function toggleFilters() {
            if (filtersShown) {
                $('#filterRow').hide();
            } else {
                $('#filterRow').show();
            }
            filtersShown = !filtersShown;
        }

        function updateRollingFilter(name, key, refreshFunction) {
            var optionMenu = document.getElementById(name);
            var chosenOption = optionMenu.value;
            var keyedFilter = filterBase[key];
            if (keyedFilter == null) {
                keyedFilter = {};
                filterBase[key] = keyedFilter;
            }
            if (chosenOption == '18') {
                keyedFilter[name + "direction"] = document.getElementById('customDirection' + name).value;
                keyedFilter[name + "value"] = document.getElementById('customValue' + name).value;
                keyedFilter[name + "interval"] = document.getElementById('customInterval' + name).value;
            }
            keyedFilter[name] = chosenOption;
            refreshFunction();
        }

        function updateMultiMonth(name, key, refreshFunction) {
            var startName = name + "start";
            var endName = name + "end";
            var startMonth = $("#"+startName).val();
            var endMonth = $("#"+endName).val();
            var keyedFilter = filterBase[key];
            if (keyedFilter == null) {
                keyedFilter = {};
                filterBase[key] = keyedFilter;
            }
            keyedFilter[name + "start"] = startMonth;
            keyedFilter[name + "end"] = endMonth;
            refreshFunction();
        }

        function updateMultiFilter(name, key, refreshFunction) {
            var keyedFilter = filterBase[key];
            if (keyedFilter == null) {
                keyedFilter = {};
                filterBase[key] = keyedFilter;
            }
            var selects = $("#"+name).val();
            keyedFilter[name] = selects;
            refreshFunction();
        }

        function filterEnable(name, key, refreshFunction) {
            var keyedFilter = filterBase[key];
            if (keyedFilter == null) {
                keyedFilter = {};
                filterBase[key] = keyedFilter;
            }
            keyedFilter[name + "enabled"] = document.getElementById(name + 'enabled').checked;

            refreshFunction();
        }

        function drillThrough(params) {
            $.getJSON('/app/drillThrough?' + params, function(data) {
                var url = data["url"];
                window.location.href = url;
            });
        }

        function refreshDataSource() {
            $("#refreshDiv").show();
            $.getJSON('/app/refreshDataSource?dataSourceID=<%= report.getDataFeedID() %>', function(data) {
                var callDataID = data["callDataID"];
                again(callDataID);
            });
        }

        function onDataSourceResult(data, callDataID) {
            var status = data["status"];
            if (status == 1) {
                // running
                again(callDataID);
            } else if (status == 2) {
                $("#refreshDiv").hide();
                refreshReport();
            } else {
                $("#refreshDiv").hide();
                $("#problemHTML").show();
                $("#problemHTML").html(data["problemHTML"]);
            }
        }

        function email() {
            var format = $('input:radio[name=emailGroup]:checked').val();
            var recipient = $('#input01').val();
            var subject = $('#input02').val();
            var body = $('#input03').value;
            $.getJSON('/app/emailReport?reportID=<%= report.getUrlKey()%>&format=' + format + "&recipient="+recipient + "&subject=" + subject + "&body=" + body, function(data) {
                alert('Email sent.');
            });
        }

        function again(callDataID) {
            setTimeout(function() {
                $.getJSON('/app/refreshStatus?callDataID=' + callDataID, function(data) {
                    onDataSourceResult(data, callDataID);
                });
            }, 5000);
        }

        function refreshReport() {
            $('#refreshingReport').modal(true, true, true);
            var strParams = "";
            for (var key in filterBase) {
                var keyedFilter = filterBase[key];
                for (var filterValue in keyedFilter) {
                    var value = keyedFilter[filterValue];
                    strParams += filterValue + "=" + value + "&";
                }
            }
            /*if ($('#reportTarget').height() == null) {
                $('#reportTarget').ready(function() {

                });
            } else {*/
            <%= report.toHTML("reportTarget") %>
            //}
        }

        function afterRefresh() {
            $('#refreshingReport').modal('hide');
        }
    </script>
</head>
<body style="background-color: #f5f5f5">

<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container-fluid">
            <div class="nav-collapse">
                <div class="btn-group pull-right">
                    <a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="icon-user"></i> <%= StringEscapeUtils.escapeHtml(userName) %>
                        <span class="caret"></span>
                    </a>
                    <ul class="dropdown-menu">
                        <% if (phone) { %>
                            <li><a href="/app/html">Data Sources</a></li>
                            <li><a href="/app/html/reports/<%= dataSourceDescriptor.getUrlKey() %>"><%=StringEscapeUtils.escapeHtml(dataSourceDescriptor.getName())%></a></li>
                            <li><a href="#" onclick="toggleFilters()">Toggle Filters</a></li>
                            <li><a href="#" onclick="refreshReport()">Refresh Report</a></li>
                        <%
                            FeedMetadata feedMetadata = new DataService().getFeedMetadata(report.getDataFeedID());
                            if (feedMetadata.getDataSourceInfo().getType() == DataSourceInfo.COMPOSITE_PULL || feedMetadata.getDataSourceInfo().getType() == DataSourceInfo.STORED_PULL) {
                        %>
                            <li><a href="#" onclick="refreshDataSource()">Refresh the Data Source</a></li>
                        <%
                            }
                        %>
                        <% } else { %>
                            <li><a href="/app/html/flashAppAction.jsp">Switch to Full Interface</a></li>
                        <% } %>
                        <%--<li><a href="#">Profile</a></li>--%>
                        <li class="divider"></li>
                        <li><a href="/app/logoutAction.jsp">Sign Out</a></li>
                    </ul>
                </div>
            </div>

            <% if (!phone) { %>
            <div class="nav-collapse">
                <ul class="nav">
                    <li><a href="/app/html">Data Sources</a></li>
                    <li><a href="/app/html/reports/<%= dataSourceDescriptor.getUrlKey() %>"><%=StringEscapeUtils.escapeHtml(dataSourceDescriptor.getName())%></a></li>
                    <li class="active"><a href="#"><%= StringEscapeUtils.escapeHtml(report.getName()) %></a></li>
                </ul>
            </div>
            <div class="nav-collapse btn-toolbar" style="margin-top:0px;margin-bottom: 0px">
                <div class="btn-group">
                    <a class="btn btn-inverse dropdown-toggle" data-toggle="dropdown" href="#">
                        Export the Report
                        <span class="caret"></span>
                    </a>
                    <ul class="dropdown-menu">
                        <li><button class="btn btn-inverse" type="button" onclick="window.location.href='/app/exportExcel?reportID=<%= report.getUrlKey() %>'" style="padding:5px;margin:5px;width:150px">Export to Excel</button></li>
                        <li><button class="btn btn-inverse" type="button" onclick="$('#emailReportWindow').modal(true, true, true)" style="padding:5px;margin:5px;width:150px">Email the Report</button></li>
                    </ul>
                </div>
                <div class="btn-group">
                    <a class="btn btn-inverse dropdown-toggle" data-toggle="dropdown" href="#">
                        Refresh Data
                        <span class="caret"></span>
                    </a>
                    <ul class="dropdown-menu">
                        <li><button class="btn btn-inverse" type="button" onclick="refreshReport()" style="padding:5px;margin:5px;width:150px">Refresh the Report</button></li>
                        <%
                            FeedMetadata feedMetadata = new DataService().getFeedMetadata(report.getDataFeedID());
                            if (feedMetadata.getDataSourceInfo().getType() == DataSourceInfo.COMPOSITE_PULL || feedMetadata.getDataSourceInfo().getType() == DataSourceInfo.STORED_PULL) {
                        %>
                        <li><button class="btn btn-inverse" type="button" id="refreshDataSourceButton" onclick="refreshDataSource()" style="padding:5px;margin:5px;width:150px">Refresh Data Source</button></li>
                        <%
                            }
                        %>
                    </ul>
                </div>
                <div class="btn-group">
                    <button class="btn btn-inverse" onclick="toggleFilters()">Toggle Filters</button>
                </div>
            </div>
            <% } %>
        </div>
    </div>
</div>


<div class="container-fluid" id="reportHeader">
    <div class="row-fluid">
        <div class="span12">
            <% if (applicationSkin != null && applicationSkin.isReportHeader()) { %>
            <div style="<%= headerStyle %>">
                <div style="padding:10px;float:left">
                    <div style="background-color: #FFFFFF;padding: 5px">
                        <%

                            if (headerImageDescriptor != null) {
                                out.println("<img src=\"/app/reportHeader?imageID="+headerImageDescriptor.getId()+"\"/>");
                            }
                        %>
                    </div>
                </div>
                <div style="<%= headerTextStyle %>">
                    <%= StringEscapeUtils.escapeHtml(report.getName()) %>
                </div>
            </div>
            <% } else { %>
            <div style="<%= headerTextStyle %>">
                <%= StringEscapeUtils.escapeHtml(report.getName()) %>
            </div>
            <% } %>
        </div>
    </div>
    <div class="row-fluid" id="filterRow">
        <div class="span12">

            <%
                for (FilterDefinition filterDefinition : report.getFilterDefinitions()) {
                    if (filterDefinition.isShowOnReportView()) {
                        out.println("<div class=\"filterDiv\">" + filterDefinition.toHTML(new FilterHTMLMetadata(report)) + "</div>");
                    }
                }
            %>

        </div>
    </div>
</div>
    <div class="container">
        <div class="modal hide fade" id="exportModalWindow">
            <div class="modal-header">
                <button data-dismiss="modal">×</button>
                <h3>Export Options</h3>
            </div>
            <div class="modal-body">
                <a href="../exportExcel?reportID=<%= report.getAnalysisID() %>" class="btn">Export to Excel</a>
                <button class="btn" onclick="$('#exportModalWindow').modal('hide'); $('#emailReportWindow').modal(true, true, true)">Email Report</button>
            </div>
        </div>
        <div class="modal hide" id="refreshingReport">
            <div class="modal-body">
                Refreshing the report...
                <div class="progress progress-striped active">
                    <div class="bar"
                         style="width: 100%;"></div>
                </div>
            </div>
        </div>
        <div class="modal hide fade" id="emailReportWindow">
            <div class="modal-header">
                <button class="close" data-dismiss="modal">×</button>
                <h3>Email Report</h3>
            </div>
            <div class="modal-body">
                <form class="form-horizontal">
                    <div class="control-group">
                        <%--<label class="control-label" for="input01">Which format?</label>--%>
                        <div class="controls">
                            <input type="radio" name="emailGroup" value="4">HTML
                            <input type="radio" name="emailGroup" value="1">Excel
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="input01">Who will this email go to?</label>
                        <div class="controls">
                            <input type="text" class="input-xlarge" id="input01">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="input02">What should the subject of the email be?</label>
                        <div class="controls">
                            <input type="text" class="input-xlarge" id="input02">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="textarea">Any message to include with the email?</label>
                        <div class="controls">
                            <textarea class="input-xlarge" id="textarea" rows="5"></textarea>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button class="btn" data-dismiss="modal" onclick="email()">Send</button>
            </div>
        </div>


        <div class="row">
            <div class="span12" style="text-align:center" id="refreshDiv">
                Refreshing the data source...
                <div class="progress progress-striped active">
                    <div class="bar"
                         style="width: 100%;"></div>
                </div>
            </div>
            <div class="span12" style="text-align:center" id="problemHTML">
            </div>
        </div>

        <div class="row">
            <div class="span12">
                <div class="well" style="background-color: #ffffff">
                    <div id="chartpseudotooltip" style="z-index:100;"></div>
                    <div id="reportTarget">
                        <div id="reportTargetReportArea" class="reportArea">

                            <%= report.rootHTML() %>
                        </div>


                        <div class="noData">We didn't find any data for the fields and filters that you specified in the report.</div>
                    </div>
                </div>
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