<%@ page import="com.easyinsight.users.UserService" %>
<%@ page import="com.easyinsight.html.RedirectUtil" %>
<%@ page import="com.easyinsight.security.SecurityUtil" %>
<%@ page import="com.easyinsight.database.EIConnection" %>
<%@ page import="com.easyinsight.database.Database" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%
    String ipAddress  = request.getHeader("X-FORWARDED-FOR");
    if(ipAddress == null) {
        ipAddress = request.getRemoteAddr();
    }
    try {
        com.easyinsight.security.SecurityUtil.populateThreadLocalFromSession(request);
    } catch(Exception e) {
        throw new RuntimeException("Problem on firstLoginAction page from " + ipAddress);
    }
    try {
        request.getSession().removeAttribute("errorString");
        String password = request.getParameter("password");
        String passwordConfirm = request.getParameter("confirmPassword");
        String errorString = null;
        if (password == null || "".equals(password.trim())) {
            errorString = "Please enter the new password.";
        } else if (passwordConfirm == null || "".equals(passwordConfirm.trim())) {
            errorString = "Please confirm the new password.";
        } else if (password.length() < 8) {
            errorString = "Your password must be at least eight characters.";
        } else if (password.length() > 20) {
            errorString = "Your password must be less than twenty characters.";
        } else if (!password.equals(passwordConfirm)) {
            errorString = "Your passwords did not match.";
        }
        if (errorString != null) {
            request.getSession().setAttribute("errorString", errorString);
            response.sendRedirect(RedirectUtil.getURL(request, "/app/user/reactivation.jsp?error=true"));
        } else {
            request.getSession().removeAttribute("errorString");
            String validation = new UserService().updatePassword(password);
            if (validation != null) {
                request.getSession().setAttribute("errorString", validation);
                response.sendRedirect(RedirectUtil.getURL(request, "/app/user/reactivation.jsp?error=true"));
            } else {
                request.getSession().removeAttribute("resetPassword");

                // is the account default to HTML?

                String redirectUrl;

                EIConnection conn = Database.instance().getConnection();
                try {
                    redirectUrl = RedirectUtil.getURL(request, "/app/html/connections.jsp");
                } finally {
                    Database.closeConnection(conn);
                }
                response.sendRedirect(redirectUrl);
            }
        }
    } finally {
        SecurityUtil.clearThreadLocal();
    }
%>