<!DOCTYPE html>
<%@ page import="com.easyinsight.database.EIConnection" %>
<%@ page import="com.easyinsight.database.Database" %>
<%@ page import="org.hibernate.Session" %>
<%@ page import="com.easyinsight.users.User" %>
<%@ page import="com.easyinsight.users.InternalUserService" %>
<%@ page import="com.easyinsight.users.UserServiceResponse" %>
<%@ page import="com.easyinsight.security.SecurityUtil" %>
<%@ page import="com.easyinsight.html.RedirectUtil" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<html lang="en">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">
    <title>Easy Insight Sign In</title>
    <script type="text/javascript" src="/js/jquery-1.7.2.min.js"></script>
    <link href="/css/bootstrap.min.css" rel="stylesheet">

    <style type="text/css">

        .center_stuff {
            text-align:center;
        }
    </style>
    <link href="/css/bootstrap-responsive.min.css" rel="stylesheet">
    <script type="text/javascript" src="/js/bootstrap.min.js"></script>
    <script type="text/javascript">
        function preserveHash() {
            $('input[name=urlhash]').val(window.location.hash);
        }
    </script>
</head>
<%
    if(request.getSession().getAttribute("accountID") != null) {
        response.sendRedirect(RedirectUtil.getURL(request, "/app/index.jsp"));
        return;
    }
    Cookie[] cookies = request.getCookies();
    String cookieValue = null;
    String userName = null;
    if (cookies != null) {
        for (Cookie cookie : cookies) {
            if ("eiRememberMe".equals(cookie.getName())) {
                cookieValue = cookie.getValue();
            } else if ("eiUserName".equals(cookie.getName())) {
                userName = cookie.getValue();
            }
        }
    }
    if (cookieValue != null && userName != null) {
        EIConnection conn = Database.instance().getConnection();
        Session hibernateSession = Database.instance().createSession(conn);
        try {
            conn.setAutoCommit(false);
            UserServiceResponse userServiceResponse = new InternalUserService().validateCookie(cookieValue, userName, conn, hibernateSession);
            if (userServiceResponse != null) {
                SecurityUtil.populateSession(session, userServiceResponse);
                Cookie usernameCookie = new Cookie("eiUserName", userName);
                usernameCookie.setSecure(true);
                usernameCookie.setMaxAge(60 * 60 * 24 * 30);
                response.addCookie(usernameCookie);
                Cookie rememberMeCookie = new Cookie("eiRememberMe", new InternalUserService().createCookie(userServiceResponse.getUserID(), conn));
                rememberMeCookie.setSecure(true);
                rememberMeCookie.setMaxAge(60 * 60 * 24 * 30);
                response.addCookie(rememberMeCookie);
                String redirectUrl = RedirectUtil.getURL(request, "/app/");
                if(session.getAttribute("loginRedirect") != null) {
                    redirectUrl = ((String) session.getAttribute("loginRedirect"));
                    session.removeAttribute("loginRedirect");
                }
                String urlHash = request.getParameter("urlhash");
                if(urlHash != null)
                    redirectUrl = redirectUrl + urlHash;
                response.sendRedirect(redirectUrl);
            }
            conn.commit();
            if (userServiceResponse != null) {
                return;
            }
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            hibernateSession.close();
            conn.close();
        }
    }

%>
<body>
<div class="container">
    <div class="row">

        <div class="col-md-6 col-md-offset-3">

            <form class="well" method="get" action="/app/openid" id="loginForm" onsubmit="preserveHash()">

                <input type="hidden" name='OWASP_CSRFTOKEN' value="<%= session.getAttribute("OWASP_CSRFTOKEN")%>" />

                <div style="width:100%;text-align: center">
                    <img src="/images/logo2.PNG" alt="Easy Insight Logo"/>
                </div>

                <input type="hidden" id="urlhash" name="urlhash"/>

                <label for="hd" class="promptLabel">
                    Domain
                </label>
                <input type="text" name="hd" id="hd" style="width:100%;font-size:14px;height:28px" autocapitalize="off" autocorrect="off" autoFocus/>

                <label class="checkbox">
                    <input type="checkbox" id="rememberMeCheckbox" name="rememberMeCheckbox" />Remember me on this computer
                </label>
                <button class="btn btn-inverse" type="submit" value="Sign In">Sign In</button>
        </div>
    </div>
</div>
</body>
</html>