<%@ page import="java.util.Date" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.TimeZone" %>
<%@ page import="java.security.MessageDigest" %>
<%@ page import="com.easyinsight.users.Account" %>
<%@ page import="com.easyinsight.database.Database" %>
<%@ page import="org.hibernate.Session" %>
<%@ page import="java.security.NoSuchAlgorithmException" %>
<%@ page import="com.easyinsight.billing.BillingUtil" %>
<%@ page import="com.easyinsight.security.PasswordService" %>
<%@ page import="com.easyinsight.users.User" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Formatter" %>
<%@ page import="com.easyinsight.config.ConfigLoader" %>
<%--
  Created by IntelliJ IDEA.
  User: abaldwin
  Date: Jun 21, 2009
  Time: 11:36:48 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html xmlns="http://www.w3.org/1999/xhtml" style="width:100%"><!-- InstanceBegin template="/Templates/Base.dwt" codeOutsideHTMLIsLocked="false" -->
<%
    long accountID = 0;
    Account account = null;
    User user = null;
    Session s = Database.instance().createSession();
    try {
        if(request.getParameter("username") != null && request.getParameter("password") != null) {
            String encryptedPass = PasswordService.getInstance().encrypt(request.getParameter("password"));

            List results = s.createQuery("from User where userName = ? and password = ?").setString(0, request.getParameter("username")).setString(1, encryptedPass).list();
            if(results.size() != 1) {
                response.sendRedirect("login.jsp?error=true");
                return;
            }
            user =(User) results.get(0);
            account = user.getAccount();
            accountID = account.getAccountID();
            request.getSession().setAttribute("accountID", accountID);
            request.getSession().setAttribute("userID", user.getUserID());
        }
        if(account == null) {
            if(request.getSession().getAttribute("accountID")== null) {
                response.sendRedirect("login.jsp?error=true");
                return;
            }
            accountID = (Long) request.getSession().getAttribute("accountID");
            long userID = (Long) request.getSession().getAttribute("userID");
            user = (User) s.createQuery("from User where userID = ?").setLong(0, userID).list().get(0);
            account = (Account) s.createQuery("from Account where accountID = ?").setLong(0, accountID).list().get(0);
        }
    }
    finally {
        s.close();
    }
    if((account.getAccountType() == Account.PROFESSIONAL || account.getAccountType() == Account.PREMIUM || account.getAccountType() == Account.ENTERPRISE)
            && !user.isAccountAdmin())
      response.sendRedirect("access.jsp");

    String keyID = BillingUtil.getKeyID();
    String key = BillingUtil.getKey();
    String orderID = "";
    String amount = "1.00";
    String type = "auth";
    if(account.getAccountState() == Account.DELINQUENT) {
        amount = String.valueOf(account.monthlyCharge());
        type = "sale";
    }

      DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
      df.setTimeZone(TimeZone.getTimeZone("UTC"));
      String time = df.format(new Date());
      request.getSession().setAttribute("billingTime", time);
      String hashString = orderID + "|" + amount + "|" + String.valueOf(accountID) + "|" + time + "|" + key;
      String hash = BillingUtil.MD5Hash(hashString);
      String accountInfoString = null;
      switch(account.getAccountType()) {
          case Account.BASIC:
              accountInfoString = "Basic";
              break;
          case Account.PROFESSIONAL:
              accountInfoString = "Professional";
              break;
          default:
              accountInfoString = "";
      }
      Formatter f = new Formatter();
      String charge = f.format("%.2f", account.monthlyCharge()).toString();

%>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<!-- InstanceBeginEditable name="doctitle" -->
<title>Easy Insight - Billing</title>
<!-- InstanceEndEditable -->
<!-- InstanceBeginEditable name="head" -->
        <script language="javascript" type="text/javascript">
        function setCCexp() {
            document.getElementById("ccexp").value = document.getElementById("ccexpMonth").value + document.getElementById("ccexpYear").value;

        }
    </script>
    <style type="text/css">
        #centerPage p {
            text-indent:0px;
        }
        td:first-child {
            text-align:right;
        }
        span.error {
            color: red;
        }

        .submitButton {
            height:25px;
            width:66px;
            background-image: url('submit.gif');
        }

        .submitButton:hover{
            background-image: url('submitSelected.gif');
        }

        .submitButton:active{
            background-image: url('submitActive.gif');
        }

    </style>
<!-- InstanceEndEditable -->
    <link type="text/css" rel="stylesheet" media="screen" href="/css/base.css" />
</head>
<body style="width:100%;text-align:center;margin:0px auto;">
    <div style="width:1000px;border-left-style:solid;border-left-color:#DDDDDD;border-left-width:1px;border-right-style:solid;border-right-color:#DDDDDD;border-right-width:1px;margin:0 auto;">
    	<div style="width:100%;text-align:left;height:70px;position:relative">
        	<a href="/index.html"><img src="/images/logo.jpg" alt="Easy Insight Logo" name="logo" id="logo" /></a>
            <div class="signupHeadline"><a href="https://www.easy-insight.com/app/" class="signupButton"></a> <a href="https://www.easy-insight.com/app/#page=account" class="signupforfreeButton"></a></div>
            <div class="headline"><a id="productPage" href="/product.html">PRODUCT</a> <a id="dataPage" href="/data.html">DATA</a> <a id="solutionsPage" href="/webanalytics.html">SOLUTIONS</a> <a id="blogPage" href="http://jamesboe.blogspot.com/">BLOG</a>  <a id="companyPage" href="/company.html">COMPANY</a></div>
        </div>
	    <!-- InstanceBeginEditable name="submenu" -->
    	<!-- InstanceEndEditable -->
        <div id="content">
		    <!-- InstanceBeginEditable name="content" -->
            <div style="width:100%;background-color:#FFFFFF">
                <div style="width:100%;text-align:center"><h1 style="color:#FFFFFF;background-image:url(/images/banner-wide.jpg);background-repeat:repeat-y;padding:10px;">Billing</h1></div>
                <p>Please input your billing information below. Your first billing cycle will start upon completion of any remaining trial time. Easy Insight does not offer any type of refund after billing.</p>
        <p style="font-size:10px">Items marked with a <span class="error">*</span> are required.</p>
        <% if(request.getParameter("error") != null) { %>
            <p><span class="error">There was an error with your billing information. Please input the correct information below.</span></p>
        <% } %>
  <form method="post" action="https://secure.braintreepaymentgateway.com/api/transact.php" onsubmit="setCCexp()">
      <input id="ccexp" type="hidden" value="" name="ccexp"/>
      <input id="customer_vault_id" type="hidden" value="<%= accountID %>" name="customer_vault_id" />
      <input id="customer_vault" type="hidden" value="<%= (account.isBillingInformationGiven() != null && account.isBillingInformationGiven()) ? "update_customer" : "add_customer" %>" name="customer_vault" />
      <input id="redirect" type="hidden" value="<%= ConfigLoader.instance().getRedirectLocation() %>/app/billing/submit.jsp" name="redirect"/>
      <input id="payment" type="hidden" value="creditcard" name="creditcard" />
      <input id="key_id" type="hidden" value="<%= keyID %>" name="key_id"/>
      <input id="orderid" type="hidden" value="<%= orderID %>" name="orderid"/>
      <input id="amount" type="hidden" value="<%= amount %>" name="amount"/>
      <input id="time" type="hidden" value="<%= time %>" name="time"/>
      <input id="hash" type="hidden" value="<%= hash %>" name="hash"/>
      <input id="type" type="hidden" value="<%= type %>" name="type" />
      <table cellpadding="3"><tbody>
      <tr>
            <td>Company:</td><td><input id="company" type="text" value="" name="company" style="width:16.5em" /></td>
      </tr>
      <tr>
          <td>First Name:</td><td><input id="firstname" type="text" value="" name="firstname" style="width:16.5em" /><span class="error">*</span></td>
          <td>Last Name:</td><td colspan="3"><input id="lastname" type="text" value="" name="lastname" style="width:16.5em" /><span class="error">*</span></td>
      </tr>
      <tr>
          <td>Address 1:</td><td> <input id="address1" type="text" value="" name="address1" style="width:16.5em" /><span class="error">*</span></td>
      </tr>
      <tr>
          <td>Address 2:</td><td><input id="address2" type="text" value="" name="address2" style="width:16.5em" /></td>
      </tr>
      <tr>
          <td>City:</td><td><input id="city" type="text" value="" name="city" style="width:16.5em" /><span class="error">*</span></td><td style="text-align:right">State:</td><td><input id="state" type="text" value="" style="width:2.5em" maxlength="2" name="state" /><span class="error">*</span></td><td style="text-align:right">Zip Code:</td><td> <input id="zip" type="text" value="" name="zip" /><span class="error">*</span></td>
      </tr>
      <tr>
          <td>Phone #:</td> <td><input id="phone" type="text" value="" name="phone" style="width:16.5em" /><span class="error">*</span></td>
      </tr>
      <tr>
          <td>Credit Card Number:</td><td><input id="ccnumber" type="text" style="width:16.5em" name="ccnumber"/><span class="error">*</span></td><td>CVV/CVC:</td> <td><input id="cvv" type="text" value="" name="cvv" style="width:3.5em" /><span class="error">*</span></td>
      </tr>
      <tr><td>Expiration date:</td><td><select id="ccexpMonth">
          <option value="01">01 - January</option>
          <option value="02">02 - February</option>
          <option value="03">03 - March</option>
          <option value="04">04 - April</option>
          <option value="05">05 - May</option>
          <option value="06">06 - June</option>
          <option value="07">07 - July</option>
          <option value="08">08 - August</option>
          <option value="09">09 - September</option>
          <option value="10">10 - October</option>
          <option value="11">11 - November</option>
          <option value="12">12 - December</option>
      </select> /
      <select id="ccexpYear">
          <option value="09">09</option>
          <option value="10">10</option>
          <option value="11">11</option>
          <option value="12">12</option>
          <option value="13">13</option>
          <option value="14">14</option>
          <option value="15">15</option>
          <option value="16">16</option>
          <option value="17">17</option>
          <option value="18">18</option>
          <option value="19">19</option>
          <option value="20">20</option>
      </select>
      <span class="error">*</span></td></tr>
      <tr><td colspan="6" style="font-size:14px;text-align:left">You are signing up for the <%= accountInfoString %> account tier, and you will be charged $<%= charge %> USD monthly for your subscription.  <% if(account.getAccountState() == Account.DELINQUENT) { %> Since your account is currently delinquent, the first charge will be applied today. <% } %>  </td></tr>
      <tr><td colspan="6" style="text-align:center"> <input class="submitButton" type="image" value="" src="transparent.gif" name="commit"/></td></tr>
      </tbody></table>


  </form>
            </div>
    		<!-- InstanceEndEditable -->
<div id="footer" style="margin:0px;padding:12px 0px;width:100%;text-align:left">
            	<div style="float:right;padding-right:200px;">
                    <span style="font-weight:bold;font-size:12px">Security and Privacy</span>
                    <ul>
                        <li><a href="/terms.html">Terms of Service</a></li>
                        <li><a href="/privacy.html">Privacy Policy</a></li>
                    </ul>
                </div>
                <div style="padding-left:180px;">
					<span style="font-weight:bold;font-size:12px;">About</span>
                    <ul>
                        <li><a href="/company.html">Company Overview</a></li>
                        <li><a href="/whoweare.html">Who We Are</a></li>
                        <li><a href="/contactus.html">Contact Us</a></li>
                    </ul>
                </div>
            </div>
	    </div>
    </div>
</body>
<!-- InstanceEnd --></html>