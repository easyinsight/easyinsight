package com.easyinsight.servlet.snappcloud;

import com.easyinsight.users.UserService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * Created by IntelliJ IDEA.
 * User: abaldwin
 * Date: Nov 24, 2010
 * Time: 9:51:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class CancelUser extends HttpServlet {

    private static final String RESPONSE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "\t<response>\n" +
        "\t\t<isValid>{0}</isCreated>\n" +
        "\t</response>";
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean success = true;
        try {
            String snappCloudID = request.getParameter("snappcloudid");
            System.out.println("snappcloudid: " + snappCloudID);
            UserService service = new UserService();
            service.cancelSnappCloudAccount(snappCloudID);
        } catch(Exception e) {
            e.printStackTrace();
            success = false;
        }
        response.setStatus(success ? 200 : 422);
        response.getWriter().print(MessageFormat.format(RESPONSE_XML, success));
        response.flushBuffer();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new ServletException();
    }
}
