package com.easyinsight.api.v3;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Tag;
import com.easyinsight.admin.HealthListener;
import com.easyinsight.admin.Status;
import com.easyinsight.cache.MemCachedManager;
import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.logging.LogClass;
import org.json.JSONArray;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * User: jamesboe
 * Date: 1/26/11
 * Time: 10:51 AM
 */
@WebServlet(value = "/admin/health", asyncSupported = true)
public class HealthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        EIConnection conn = Database.instance().getConnection();
        try {
            String memcached = req.getParameter("memcached");

            PreparedStatement queryStmt = conn.prepareStatement("SELECT SERVER_HOST FROM SERVER WHERE ENABLED = ?");
            queryStmt.setBoolean(1, true);
            ResultSet rs = queryStmt.executeQuery();
            List<Status> statusList = new ArrayList<Status>();
            JSONArray arr = new JSONArray();
            while (rs.next()) {

                String host = rs.getString(1);
                Status status;
                status = (Status) MemCachedManager.get("servers" + host);


                if (status == null) {
                    status = new Status();
                    status.setExtendedCode("Failure");
                    status.setExtendedMessage(host + " has failed to update with any status information");
                    statusList.add(status);
                } else if ((System.currentTimeMillis() - status.getTime()) > (1000 * 60 * 4)) {
                    status.setExtendedCode("Failure");
                    status.setExtendedMessage(host + " hasn't updated updated status for at least four minutes");
                    statusList.add(status);
                } else if (HealthListener.FAILURE.equals(status.getCode())) {
                    status.setExtendedCode("Failure");
                    status.setExtendedMessage(status.getMessage());
                    statusList.add(status);
                } else {
                    statusList.add(status);
                }
                status.setHost(host);

                if("json".equals(req.getParameter("format"))) {

                    AmazonEC2Client ec2 = new AmazonEC2Client(new BasicAWSCredentials("0AWCBQ78TJR8QCY8ABG2", "bTUPJqHHeC15+g59BQP8ackadCZj/TsSucNwPwuI"));
                    DescribeInstancesRequest request = new DescribeInstancesRequest();
                    request.setFilters(Arrays.asList(new Filter("private-dns-name", Arrays.asList(host + ".ec2.internal"))));
                    DescribeInstancesResult result = ec2.describeInstances(request);
                    if(result.getReservations().size() > 0) {
                        List<Tag> tags = result.getReservations().get(0).getInstances().get(0).getTags();
                        for(Tag t : tags) {
                            if(t.getKey().equals("Role"))
                                status.setRole(t.getValue());
                        }
                        status.setPublicDns(result.getReservations().get(0).getInstances().get(0).getPublicDnsName());
                    }
                    arr.put(status.toJSON());
                }
            }
            StringBuilder xmlBuilder = new StringBuilder();
            xmlBuilder.append("<response>\r\n");
            for (Status status : statusList) {
                xmlBuilder.append("\t<server>\r\n");
                xmlBuilder.append("\t\t<status>");
                xmlBuilder.append(status.getExtendedCode());
                xmlBuilder.append("</status>\r\n");
                xmlBuilder.append(status.getHealthInfo() == null ? "" : status.getHealthInfo().toXML());
                xmlBuilder.append("\t\t<message>");
                xmlBuilder.append(status.getExtendedMessage());
                xmlBuilder.append("</message>\r\n");
                xmlBuilder.append("\t\t<time>");
                xmlBuilder.append(new Date(status.getTime()));
                xmlBuilder.append("</time>\r\n");
                xmlBuilder.append("\t</server>\r\n");
            }
            xmlBuilder.append("</response>");
            if("json".equals(req.getParameter("format")))
                resp.getOutputStream().write(arr.toString().getBytes());
            else
                resp.getOutputStream().write(xmlBuilder.toString().getBytes());
            resp.getOutputStream().flush();
        } catch (Exception e) {
            LogClass.error(e);
        } finally {
            Database.closeConnection(conn);
        }
    }
}
