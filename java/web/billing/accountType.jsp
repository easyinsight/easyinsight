<!DOCTYPE html>
<%@ page import="com.easyinsight.users.Account" %>
<%@ page import="com.easyinsight.database.Database" %>
<%@ page import="org.hibernate.Session" %>
<%@ page import="com.easyinsight.security.SecurityUtil" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="com.easyinsight.users.UserAccountAdminService" %>
<%@ page import="com.easyinsight.database.EIConnection" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="com.easyinsight.html.HtmlConstants" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="en">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">
    <title>Easy Insight Account Management</title>
    <jsp:include page="../html/bootstrapHeader.jsp"/>
    <%
        String userName = (String) session.getAttribute("userName");
        EIConnection conn = Database.instance().getConnection();
        Session hibernateSession = Database.instance().createSession(conn);
        com.easyinsight.security.SecurityUtil.populateThreadLocalFromSession(request);

        try {
            if (!SecurityUtil.isAccountAdmin()) {
                response.sendRedirect("access.jsp");
                return;
            }
            if ((SecurityUtil.getAccountTier() == Account.PREMIUM || SecurityUtil.getAccountTier() == Account.ENTERPRISE)) {
                response.sendRedirect("access.jsp");
                return;
            }

            Account account = (Account) hibernateSession.createQuery("from Account where accountID = ?").setLong(0, SecurityUtil.getAccountID()).list().get(0);

            if (account.getPricingModel() == 0) {
                response.sendRedirect("legacyAccountType.jsp");
                return;
            }

            boolean trial = account.getAccountState() == Account.TRIAL;

            boolean annual = account.getBillingMonthOfYear() != null;

            String discountString;
            if (annual) {
                discountString = NumberFormat.getCurrencyInstance().format(account.createCost() / 12);
            } else {
                discountString = "$0.00";
            }

            String priceString = account.createCostString();

            long usedStorage = new UserAccountAdminService().getAccountStorage();
            String usedStorageString = Account.humanReadableByteCount(usedStorage, true) + " / " + Account.humanReadableByteCount(account.getMaxSize(), true);
    %>
    <script type="text/javascript">

        var designers = <%= account.getCoreDesigners() %>;
        var baseStorage = <%= account.getCoreStorage() / 1000000 %>;
        var baseConnections = <%= account.getCoreSmallBizConnections() %>;
        var addonDesigners = <%= account.getAddonDesigners() %>;
        var addonStorageUnits = <%= account.getAddonStorageUnits() %>;
        var addonConnections = <%= account.getAddonSmallBizConnections() %>;
        var accountType = <%= account.getAccountType() %>;
        var enterpriseCost = <%= account.getEnterpriseAddonCost() %>
        var storageCost = 0;
        var billingInterval = <%= account.getBillingMonthOfYear() != null ? 2 : 1%>;

        function updateAccountValue(designerInput) {
            addonDesigners = parseInt(designerInput.value);
            $('#additionalDesigners').html(addonDesigners);
            $('#totalDesigners').html(designers + addonDesigners);
            $('#designerCost').html("$" + addonDesigners * 50);
            updatePrice();
        }

        function updateBillingInterval(selector) {
            billingInterval = parseInt(selector.value);
            updatePrice();
        }

        function updatePrice() {
            var cost = 50 + (addonDesigners * 50) + (addonStorageUnits * 150) + (addonConnections * 25) + enterpriseCost;
            if (billingInterval == 2) {
                cost = cost * 12;
            }
            var discount = (billingInterval == 2 ? cost / 12 : 0);

            var costString = "$" + cost + ".00";
            var endCost = "$" + (cost - discount) + ".00";
            var discountString = "$" + discount + ".00";
            $('#proPrice').html(costString);
            $('#proDiscount').html(discountString);
            $('#proTotal').html(endCost);
        }

        function updateStorageCostPro(source) {
            addonStorageUnits = parseInt(source.value);
            $('#additionalStorage').html(addonStorageUnits);
            $('#totalStorage').html(baseStorage + addonStorageUnits * 250 + " MB");
            $('#storageCost').html("$" + addonStorageUnits * 150);
            updatePrice();
        }

        function updateSmallBizConnections(source) {
            addonConnections = parseInt(source.value);
            $('#additionalConnections').html(addonConnections);
            $('#totalConnections').html(baseConnections + addonConnections);
            $('#connectionCost').html("$" + addonConnections * 25);
            updatePrice();
        }
    </script>
</head>
<body>

<jsp:include page="../header.jsp">
    <jsp:param name="userName" value="<%= userName %>"/>
    <jsp:param name="headerActive" value="<%= HtmlConstants.ACCOUNT %>"/>
</jsp:include>

<div class="container">
    <div class="row">
        <div class="col-md-12">
            <div style="width:100%;text-align: center">
                <img src="/images/logo2.PNG" alt="Easy Insight Logo"/>
            </div>
        </div>
        <div class="row">
            <div class="col-md-8 col-md-offset-1" style="background-color: #E6E6E6;padding:10px;border-radius: 5px">
                <div class="row">
                    <div class="col-md-6">
                        <h3>Account Info</h3>
                    </div>
                </div>
                <% if (request.getParameter("success") != null) { %>
                <div class="row">
                    <div class="col-md-8">
                        <p style="font-size: 12px;padding: 0;margin-bottom: 5px;color: #009900">Your account type has
                            been changed.</p>
                    </div>
                </div>
                <% } %>
                <div class="row">
                    <div class="col-md-offset-1 col-md-10">
                        <div class="well" style="background-color: #FBFBFB">
                            <%
                                if (trial) {
                            %>
                            <div style="float:left;height:110px;padding-top:30px;padding-right:60px"><h4>Your Free Trial
                                Account</h4></div>
                            <div style="height:110px">
                                <p><%= account.getCoreDesigners() + account.getAddonDesigners() %> Designers
                                </p>

                                <p><%= account.getCoreSmallBizConnections() + account.getAddonSmallBizConnections() %> Small Business Connections
                                </p>
                                <p><%= Account.humanReadableByteCount(account.getCoreStorage() + (long) account.getAddonStorageUnits() * 250000000L, true) %> Custom Data Storage
                                </p>
                                <p>Billed <%= account.billingInterval() %>
                                </p>


                            </div>
                            <%
                            } else {
                            %>
                            <div style="float:left;height:110px;padding-top:30px;padding-right:60px"><h4>Your Current
                                Account</h4></div>
                            <div style="height:110px">
                                <p><%= account.getCoreDesigners() + account.getAddonDesigners() %> Designers
                                </p>

                                <p><%= account.getCoreSmallBizConnections() + account.getAddonSmallBizConnections() %> Small Business Connections
                                </p>
                                <p><%= Account.humanReadableByteCount(account.getCoreStorage() + (long) account.getAddonStorageUnits() * 250000000L, true) %> Custom Data Storage</p>
                                <p>Billed <%= account.billingInterval() %></p>
                            </div>
                            <%
                                }
                            %>
                        </div>
                    </div>
                </div>


                <div style="background-color: #F0F0F0;padding: 8px">
                    <div style="background-color:#FFFFFF;padding: 10px;">
                        <div class="row">
                            <div class="col-md-3">
                                <h3>Designers</h3>
                            </div>
                            <div class="col-md-2" style="text-align: center">
                                <div>
                                    <h3><%= account.getCoreDesigners()%></h3>
                                    <h5>Free</h5>
                                </div>
                            </div>
                            <div class="col-md-2" style="text-align: center">
                                <div>
                                    <h3 id="additionalDesigners"><%= account.getAddonDesigners() %></h3>
                                    <h5>Additional</h5>
                                </div>
                            </div>
                            <div class="col-md-2" style="text-align: center">
                                <div>
                                    <h3 id="totalDesigners"><%= account.getCoreDesigners() + account.getAddonDesigners()%></h3>
                                    <h5>Total</h5>
                                </div>
                            </div>
                            <div class="col-md-2" style="text-align: center">
                                <div>
                                    <h3 id="designerCost">$<%= account.getAddonDesigners() * 25 %></h3>
                                    <h5>Cost</h5>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-3">
                                <h3>Small Business Connections</h3>
                            </div>
                            <div class="col-md-2" style="text-align: center">
                                <div>
                                    <h3><%= account.getCoreSmallBizConnections()%></h3>
                                    <h5>Free</h5>
                                </div>
                            </div>
                            <div class="col-md-2" style="text-align: center">
                                <div>
                                    <h3 id="additionalConnections"><%= account.getAddonSmallBizConnections()%></h3>
                                    <h5>Additional</h5>
                                </div>
                            </div>
                            <div class="col-md-2" style="text-align: center">
                                <div>
                                    <h3 id="totalConnections"><%= account.getCoreSmallBizConnections() + account.getAddonSmallBizConnections()%></h3>
                                    <h5>Total</h5>
                                </div>
                            </div>
                            <div class="col-md-2" style="text-align: center">
                                <div>
                                    <h3 id="connectionCost">$<%= account.getAddonSmallBizConnections() * 25%></h3>
                                    <h5>Cost</h5>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-3">
                                <h3>Custom Data Storage</h3>
                            </div>
                            <div class="col-md-2" style="text-align: center">
                                <div>
                                    <h3><%= account.getCoreStorage() / 1000000 %> MB</h3>
                                    <h5>Free</h5>
                                </div>
                            </div>
                            <div class="col-md-2" style="text-align: center">
                                <div>
                                    <h3 id="additionalStorage"><%= account.getAddonStorageUnits()%></h3>
                                    <h5>250 MB addons</h5>
                                </div>
                            </div>
                            <div class="col-md-2" style="text-align: center">
                                <div>
                                    <h3 id="totalStorage"><%= (account.getCoreStorage() + ((long) account.getAddonStorageUnits() * 250000000L)) / 1000000 %> MB</h3>
                                    <h5>Total</h5>
                                </div>
                            </div>
                            <div class="col-md-2" style="text-align: center">
                                <div>
                                    <h3 id="storageCost">$<%= account.getAddonStorageUnits() * 150%></h3>
                                    <h5>Cost</h5>
                                </div>
                            </div>
                        </div>

                    </div>
                    <form method="post"
                          action="/app/billing/newModelAccountTypeAction.jsp">
                    <div class="row" style="padding: 20px 18px inherit;text-align: center">

                            <div class="col-md-6" style="background-color:#FFFFFF;padding: 10px; margin: 10px;text-align: left">

                                <label style="font-size: 14px">Additional Designers ($50/per)</label>
                                <input type="number" min="0" style="width:290px" name="numberDesigners"
                                       value="<%= account.getAddonDesigners() %>" id="numberDesignersPro"
                                       onchange="updateAccountValue(this)">
                                <label style="font-size: 14px">Additional Small Business Connections ($25/per)</label>
                                <input type="number" min="0" style="width:290px" name="numberConnections"
                                       value="<%= account.getAddonSmallBizConnections() %>" id="numberSmallConnections"
                                       onchange="updateSmallBizConnections(this)">
                                <label style="font-size: 14px">Additional 250 MB Storage Blocks ($150/per)</label>
                                <input type="number" min="0" style="width:290px" name="numberStorageBlocks"
                                       value="<%= account.getAddonStorageUnits() %>" id="numberStorageBlocks"
                                       onchange="updateStorageCostPro(this)">

                            </div>
                            <div class="col-md-4" style="background-color:#FFFFFF;padding: 10px; margin: 10px;width:313px;text-align: left">
                                <div style="float:right">
                                    <span style="font-size: 14px">$50.00</span>
                                </div>
                                <div>
                                    <p style="font-size: 14px">Base Price</p>
                                </div>
                                <div style="float:right">
                                    <span style="font-size: 14px" id="proPrice"><%= account.addonCostString() %></span>
                                </div>
                                <div>
                                    <p style="font-size: 14px">Addon Price</p>
                                </div>
                                <div style="float:right">
                                    <span style="font-size: 14px" id="enterpriseAddonPrice"><%= account.enterpriseCostString() %></span>
                                </div>
                                <div>
                                    <p style="font-size: 14px">Enterprise Addon Price</p>
                                </div>
                                <div style="float:right">
                                    <span style="font-size: 14px" id="proDiscount"><%= discountString %></span>
                                </div>
                                <div>
                                    <p style="font-size: 14px">Discount</p>
                                </div>
                                <hr>
                                <div style="float:right">
                                    <span style="font-size: 16px"
                                          id="proTotal"><strong><%= account.createTotalCostString() %>
                                    </strong></span>
                                </div>
                                <div>
                                    <p style="font-size: 16px">Total</p>
                                </div>
                                <% if (request.getSession().getAttribute("errorString") != null) { %>
                                <fieldset class="control-group error">
                                    <label class="formAreaP control-label"><%= request.getSession().getAttribute("errorString")%>
                                    </label>
                                </fieldset>
                                <% } %>
                                <div style="float:right">
                                    <button class="btn btn-success" type="submit">Update Account</button>
                                </div>
                            </div>



                    </div>
                    </form>
                </div>

            </div>
            <div class="col-md-3">
                <div class="well" style="background-color: #d5d5d5">
                    <p><strong>Have questions?</strong></p>

                    <p>You can contact Easy Insight at 1-720-316-8174 or sales@easy-insight.com if you have any
                        questions or concerns around your account billing.</p>
                </div>
            </div>
        </div>
    </div>
</div>

<%
    } finally {
        hibernateSession.close();
        Database.closeConnection(conn);
        SecurityUtil.clearThreadLocal();
    }
%>
</body>
</html>
