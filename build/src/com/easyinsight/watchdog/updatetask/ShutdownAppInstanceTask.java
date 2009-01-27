package com.easyinsight.watchdog.updatetask;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.tools.ant.BuildException;

/**
 * User: James Boe
 * Date: Jan 27, 2009
 * Time: 3:32:23 PM
 */
public class ShutdownAppInstanceTask extends AppInstanceTask {
    public void execute() throws BuildException {
        try {
            HttpClient httpClient = new HttpClient();
            for (String instance : getInstances()) {
                HttpMethod shutdownMethod = new GetMethod("http://" + instance + ":4000/update?operation=shutdown");
                httpClient.executeMethod(shutdownMethod);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }
}
