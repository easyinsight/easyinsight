<!DOCTYPE html>
<%@ page import="com.easyinsight.security.SecurityUtil" %>
<%@ page import="com.easyinsight.logging.LogClass" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="com.easyinsight.html.HtmlConstants" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="en">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">
    <title>Easy Insight Server Error</title>
    <script type="text/javascript" src="/js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="/js/jquery-ui-1.8.20.custom.min.js"></script>
    <link href="/css/bootstrap.css" rel="stylesheet">
    <link href="/css/smoothness/jquery-ui-1.8.20.custom.css" rel="stylesheet">
    <link href="/css/bootstrap-responsive.css" rel="stylesheet">
    <script type="text/javascript" src="/js/bootstrap.js"></script>
</head>
<body>
<%

    String userName = (String) session.getAttribute("userName");
    if (userName != null) {
        com.easyinsight.security.SecurityUtil.populateThreadLocalFromSession(request);
    }

    Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
    if (exception != null) {
        LogClass.error(exception);
    }
    try {

%>
<jsp:include page="header.jsp">
    <jsp:param name="userName" value="<%= userName %>"/>
    <jsp:param name="headerActive" value="<%= HtmlConstants.NONE %>"/>
</jsp:include>
<div class="container">
    <div class="row">
        <div class="col-md-12">
            <div style="width:100%;text-align: center">
                <img src="/images/logo2.PNG" alt="Easy Insight Logo"/>
            </div>
        </div>
        <div class="col-md-6 col-md-offset-3">
            <div class="well" style="text-align:center">
                <h3>Billing Temporarily Disabled</h3>
                <p>We're sorry, but billing is currently disabled as we're upgrading the billing system.  Please try again soon.</p>
            </div>
        </div>
    </div>
</div>

<%
    } finally {
        if (userName != null) {
            SecurityUtil.clearThreadLocal();
        }
    }
%>
</body>
</html>