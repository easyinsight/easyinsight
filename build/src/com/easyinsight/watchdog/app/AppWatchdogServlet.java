package com.easyinsight.watchdog.app;

import org.mortbay.jetty.security.Password;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * User: James Boe
 * Date: Jan 27, 2009
 * Time: 1:31:21 AM
 */
public class AppWatchdogServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse response) throws ServletException, IOException {
        String operation = httpServletRequest.getParameter("operation");
        String type = httpServletRequest.getParameter("type");
        String role = httpServletRequest.getParameter("role");
        if ("shutdown".equals(operation)) {
            new AppWatchdog().shutdown();
        } else if ("startup".equals(operation)) {
            new AppWatchdog().start();
        } else if ("restart".equals(operation)) {
            new AppWatchdog().restart();
        } else if ("update".equals(operation)) {
            new AppWatchdog().update();
        } else if ("download".equals(operation)) {
            new AppWatchdog().download(role, type);
        }
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    public static void main(String[] args) {
    }
}
