package com.easyinsight.html;

import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.datafeeds.FeedService;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.solutions.SolutionKPIData;
import com.easyinsight.solutions.SolutionService;
import com.easyinsight.userupload.CredentialsResponse;
import com.easyinsight.userupload.UserUploadService;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: jamesboe
 * Date: 6/1/12
 * Time: 10:47 AM
 */
public class ConnectionInstalledServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        SecurityUtil.populateThreadLocalFromSession(req);
        try {
            long dataSourceID = Long.parseLong(req.getParameter("dataSourceID"));
            FeedDefinition dataSource = new FeedService().getFeedDefinition(dataSourceID);
            SolutionKPIData solutionKPIData = new SolutionKPIData();
            solutionKPIData.setDataSourceID(dataSourceID);
            new SolutionService().addKPIData(solutionKPIData);
            String endURL = "reports/" + dataSource.getApiKey();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("url", endURL);
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
