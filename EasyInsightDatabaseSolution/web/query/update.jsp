<%--
  Created by IntelliJ IDEA.
  User: abaldwin
  Date: May 7, 2010
  Time: 11:32:27 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" session="true" %>
<%@ page import="com.easyinsight.connections.database.DataConnection" %>
<%@ page import="com.easyinsight.connections.database.data.Query" %>
<%@ page import="org.hibernate.Session" %>
<%@ page import="org.hibernate.Transaction" %>
<%@ page import="com.easyinsight.connections.database.data.ConnectionInfo" %>
<%
    if(session.getAttribute("user") == null) {
        response.sendRedirect("login.jsp");
    }
    Session dataSession = DataConnection.getSession();
    Transaction trans = dataSession.getTransaction();
    try {
        trans.begin();
        Query query = (Query) dataSession.get(Query.class, Long.parseLong(request.getParameter("id")));
        query.update(request.getParameterMap());
        query.setConnectionInfo((ConnectionInfo) dataSession.get(ConnectionInfo.class, Long.parseLong(request.getParameter("queryConnection"))));
        dataSession.save(query);
        trans.commit();
        %>
        <span class="success">Success!</span><script type="text/javascript">refreshQueries();$('#editQuery')[0].reset();$('#editQuery').hide();$('#newQueryButton').show();</script>
    <%} catch(Exception e) {
        trans.rollback(); %>
        <span class="failure">An error occured in updating the query: <pre>
            <%= e.getMessage() %>
        </pre></span>
    <%
    } finally {
        dataSession.close();
    }
%>