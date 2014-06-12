package com.easyinsight.html;

import com.easyinsight.api.v3.JSONServlet;
import com.easyinsight.api.v3.ResponseInfo;
import com.easyinsight.database.EIConnection;
import com.easyinsight.users.UserAccountAdminService;
import net.minidev.json.JSONArray;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Alan
 * Date: 4/24/14
 * Time: 10:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class DeleteUsersServlet extends JSONServlet {

    @Override
    protected ResponseInfo processPost(net.minidev.json.JSONObject jsonObject, EIConnection conn, HttpServletRequest request) throws Exception {
        List userIDs = (List) jsonObject.get("user_ids");
        UserAccountAdminService service = new UserAccountAdminService();
        List<Integer> userIDsToDelete = new ArrayList<>();
        for (Object o : userIDs) {
            int userID = Integer.parseInt(o.toString());
            userIDsToDelete.add(userID);
        }
        service.deleteUsers(userIDsToDelete);
        JSONObject response = new JSONObject();
        try {
            response.put("success", true);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return new ResponseInfo(200, response.toString());
    }

    @Override
    protected ResponseInfo processGet(net.minidev.json.JSONObject jsonObject, EIConnection conn, HttpServletRequest request) throws Exception {
        long userID = Long.parseLong(request.getParameter("userID"));
        UserAccountAdminService service = new UserAccountAdminService();
        service.deleteUser(userID);
        JSONObject response = new JSONObject();
        try {
            response.put("success", true);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return new ResponseInfo(200, response.toString());
    }

    @Override
    protected ResponseInfo processJSON(net.minidev.json.JSONObject jsonObject, EIConnection conn, HttpServletRequest request) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
