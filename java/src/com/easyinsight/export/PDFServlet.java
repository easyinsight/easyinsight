package com.easyinsight.export;

import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.logging.LogClass;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * User: jamesboe
 * Date: Oct 28, 2010
 * Time: 11:22:02 AM
 */
public class PDFServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long imageID = (Long) req.getSession().getAttribute("imageID");
        Long userID = (Long) req.getSession().getAttribute("userID");
        if (imageID != null && userID != null) {
            EIConnection conn = Database.instance().getConnection();
            try {
                PreparedStatement queryStmt = conn.prepareStatement("SELECT PNG_IMAGE, REPORT_NAME FROM PNG_EXPORT WHERE PNG_EXPORT_ID = ? AND " +
                        "USER_ID = ?");
                queryStmt.setLong(1, imageID);
                queryStmt.setLong(2, userID);
                ResultSet rs = queryStmt.executeQuery();
                if (rs.next()) {
                    byte[] bytes = rs.getBytes(1);
                    String reportName = rs.getString(2);
                    resp.setContentType("application/x-download");
                    resp.setContentLength(bytes.length);
                    reportName = URLEncoder.encode(reportName, "UTF-8");
                    resp.setHeader("Content-disposition","attachment; filename=" + reportName+".pdf" );
                    resp.getOutputStream().write(bytes);
                }
            } catch (Exception e) {
                LogClass.error(e);
            } finally {
                Database.closeConnection(conn);
            }
        } else {
            
        }
    }
}
