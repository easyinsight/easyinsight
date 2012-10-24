<%@ page import="com.easyinsight.database.Database" %>
<%@ page import="org.hibernate.Session" %>
<%@ page import="com.easyinsight.security.SecurityUtil" %>
<%@ page import="com.easyinsight.users.Account" %>
<%@ page import="com.easyinsight.html.RedirectUtil" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%
    String googleDomainName = (String) session.getAttribute("googleDomainName");
    String doneURL = (String) session.getAttribute("googleCallbackURL");
    Long userID = (Long) session.getAttribute("userID");
    if (userID == null) {
        session.setAttribute("loginRedirect", RedirectUtil.getURL(request, "/app/linkToExistingAction.jsp"));
        System.out.println("redirecting to log in, no user ID found in session");
        response.sendRedirect(RedirectUtil.getURL(request, "login.jsp"));
    } else {
        System.out.println("blah");
        Session hibernateSession = Database.instance().createSession();
        try {
            hibernateSession.beginTransaction();
            Account account = (Account) hibernateSession.createQuery("from Account where accountID = ?").setLong(0, (Long) session.getAttribute("accountID")).list().get(0);
            if (account.getGoogleDomainName() != null) {
                System.out.println("redirecting to domain already linked");
                response.sendRedirect(RedirectUtil.getURL(request, "/app/domainAlreadyLinked.jsp"));
            } else {
                // redirect back to google
                System.out.println("redirecting back to google URL of " + doneURL);
                account.setGoogleDomainName(googleDomainName);
                response.sendRedirect(doneURL);
            }
            hibernateSession.getTransaction().commit();
        } catch (Exception e) {
            hibernateSession.getTransaction().rollback();
        } finally {
            hibernateSession.close();
        }
    }
%>