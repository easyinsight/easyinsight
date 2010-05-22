<%@ page import="com.easyinsight.connections.database.data.ConnectionInfo" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="com.easyinsight.connections.database.DataConnection" %>
<%@ page import="org.hibernate.Session" %>
<%--
  Created by IntelliJ IDEA.
  User: abaldwin
  Date: Apr 30, 2010
  Time: 12:38:06 PM
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" session="true" %>
<%
    if(session.getAttribute("user") == null) {
        response.sendRedirect("login.jsp");
    }
    Session dataSession = DataConnection.getSession();
    Connection connection = null;
    try {
        ConnectionInfo conn = null;
        if(request.getParameter("id") != null && !request.getParameter("id").isEmpty() && !"edit".equals(request.getParameter("edit"))) {
            conn = (ConnectionInfo) dataSession.get(ConnectionInfo.class, Long.parseLong(request.getParameter("id")));
        } else {
            conn = ConnectionInfo.createConnectionInfo(request.getParameterMap());
        }
        connection = conn.createConnection(); %>
        <span class="success">Success!</span>
    <%
    } catch(Exception e) { %>
        <span class="failure"><pre><%= e.getMessage() %></pre></span>
    <% }  finally {
        dataSession.close();
        if(connection != null)
            connection.close();
    }
%>