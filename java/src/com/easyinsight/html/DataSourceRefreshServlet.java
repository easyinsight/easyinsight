package com.easyinsight.html;

import com.easyinsight.datafeeds.FeedService;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.userupload.CredentialsResponse;
import com.easyinsight.userupload.UserUploadService;
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
@WebServlet(value = "/refreshDataSource", asyncSupported = true)
public class DataSourceRefreshServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        SecurityUtil.populateThreadLocalFromSession(req);
        try {
            String urlKey = req.getParameter("urlKey");
            Long dataSourceID = new FeedService().openFeedIfPossible(urlKey).getFeedDescriptor().getId();
            CredentialsResponse credentialsResponse = new UserUploadService().refreshData(dataSourceID);
            String callDataID = credentialsResponse.getCallDataID();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("callDataID", callDataID);
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
