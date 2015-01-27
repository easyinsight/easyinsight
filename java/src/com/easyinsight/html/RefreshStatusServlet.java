package com.easyinsight.html;

import com.easyinsight.analysis.DataSourceConnectivityReportFault;
import com.easyinsight.scorecard.DataSourceRefreshEvent;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.userupload.CredentialsResponse;
import com.easyinsight.userupload.UserUploadService;
import com.easyinsight.util.AsyncService;
import com.easyinsight.util.CallData;
import com.easyinsight.util.ServiceUtil;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: jamesboe
 * Date: 6/1/12
 * Time: 10:47 AM
 */
@WebServlet(value = "/refreshStatus", asyncSupported = true)
public class RefreshStatusServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        SecurityUtil.populateThreadLocalFromSession(req);
        try {
            String callDataID = req.getParameter("callDataID");
            CallData callData = new AsyncService().getCallData(callDataID);
            JSONObject jsonObject = new JSONObject();
            if (callData == null) {
                jsonObject.put("status", 4);
            } else {
                jsonObject.put("status", callData.getStatus());
                String message = callData.getStatusMessage();
                if (message == null && callData.getResult() instanceof DataSourceRefreshEvent) {
                    DataSourceRefreshEvent event = (DataSourceRefreshEvent) callData.getResult();
                    message = event.getDataSourceName();
                }
                jsonObject.put("statusMessage", message);
                if (callData.getStatus() == ServiceUtil.FAILED) {
                    if (callData.getResult() instanceof DataSourceConnectivityReportFault) {
                        DataSourceConnectivityReportFault fault = (DataSourceConnectivityReportFault) callData.getResult();
                        jsonObject.put("problemHTML", fault.getMessage());
                        req.getSession().setAttribute("failureMessage", fault.getMessage());
                    } else {
                        jsonObject.put("problemHTML", "Something went wrong in pulling data for this data source.");
                    }
                }
            }
            response.setContentType("application/json");
            response.getOutputStream().write(jsonObject.toString().getBytes());
            response.getOutputStream().flush();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            SecurityUtil.clearThreadLocal();
        }
    }
}
