<%--
  Created by IntelliJ IDEA.
  User: abaldwin
  Date: May 3, 2010
  Time: 12:41:53 PM
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" session="true" %>
<%@ page import="com.easyinsight.connections.database.data.ConnectionInfo" %>
<%@ page import="org.hibernate.Session" %>
<%@ page import="com.easyinsight.connections.database.DataConnection" %>
<%@ page import="org.hibernate.Transaction" %>
<%
    if(session.getAttribute("user") == null) {
        %><jsp:include page="../error.jsp" /><%
    } else {
    
    Session dataSession = DataConnection.getSession();
    Transaction trans = dataSession.getTransaction();
    try {
        trans.begin();
        ConnectionInfo conn = (ConnectionInfo) dataSession.get(ConnectionInfo.class, Long.parseLong(request.getParameter("id")));
        dataSession.delete(conn);
        trans.commit();
        %>
        <script type="text/javascript">jSuccess("Success!", {HorizontalPosition : 'center', VerticalPosition : 'center'}); </script>
        <script type="text/javascript">refreshDataSources();refreshQueries();</script>
    <%} catch(Exception e) {
        trans.rollback(); %>
        <script type="text/javascript">jError("An error occured: <pre><%= e.getMessage() %></pre>", {HorizontalPosition : 'center', VerticalPosition : 'center'});</script>
    <%
    } finally {
        dataSession.close();
    }
}
%>