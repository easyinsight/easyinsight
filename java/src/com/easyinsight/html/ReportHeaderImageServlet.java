package com.easyinsight.html;

import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.preferences.ApplicationSkin;
import com.easyinsight.preferences.PreferencesService;
import com.easyinsight.security.SecurityUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * User: jamesboe
 * Date: 5/24/12
 * Time: 2:50 PM
 */
@WebServlet(value = "/reportHeader", asyncSupported = true)
public class ReportHeaderImageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SecurityUtil.populateThreadLocalFromSession(req);
        try {
            Database.useConnection(true, (conn) -> {
                PreferencesService.ImageData id;
                Date d = new Date();
                try {
                    Long imageID = Long.parseLong(req.getParameter("imageID"));
                    id = new PreferencesService().getImageData(imageID, conn);
                } catch (NumberFormatException e) {
                    String imageID = req.getParameter("imageID");
                    id = new PreferencesService().getImageDataByURLKey(imageID, conn);
                }

                byte[] bytes = id.data;
                System.out.println("Benchmark: " + (new Date().getTime() - d.getTime()));
                if (bytes != null) {
                    resp.setContentLength(bytes.length);
                    resp.setContentType(id.descriptor.getContentType());
                    resp.setHeader("Cache-control", "private, max-age: 2592000");
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.SECOND, 2592000);

                    SimpleDateFormat sdf = new SimpleDateFormat();
                    sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
                    sdf.applyPattern("dd MMM yyyy HH:mm:ss z");
                    resp.setHeader("Expires", sdf.format(c.getTime()));
                    resp.setHeader("Content-disposition","inline; filename=reportHeader.png" );
                    resp.getOutputStream().write(bytes);
                    resp.getOutputStream().flush();
                }
            });

        } finally {
            SecurityUtil.clearThreadLocal();
        }
    }
}
