package com.easyinsight.datafeeds.basecamp;

import com.easyinsight.analysis.DataSourceConnectivityReportFault;
import com.easyinsight.analysis.ReportException;
import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.datafeeds.ServerDataSourceDefinition;
import com.easyinsight.logging.LogClass;
import nu.xom.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import java.net.SocketException;
import java.util.Map;

/**
 * User: jamesboe
 * Date: Mar 1, 2010
 * Time: 9:55:12 AM
 */
public abstract class BaseCampBaseSource extends ServerDataSourceDefinition {

    

    protected static HttpClient getHttpClient(String username, String password) {
        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);
        Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
        client.getState().setCredentials(new AuthScope(AuthScope.ANY), defaultcreds);
        return client;
    }

    protected String retrieveContactInfo(HttpClient client, Builder builder, Map<String, String> peopleCache, String contactId, String url, FeedDefinition parentDefinition) throws BaseCampLoginException, ParsingException, ReportException {
        String contactName = null;
        try {
            if(contactId != null) {
                contactName = peopleCache.get(contactId);
                if(contactName == null) {
                    Document contactInfo = runRestRequest("/contacts/person/" + contactId, client, builder, url, null, false, parentDefinition, false);
                    contactName = queryField(contactInfo, "/person/first-name/text()") + " " + queryField(contactInfo, "/person/last-name/text()");
                    peopleCache.put(contactId, contactName);
                }

            }
            return contactName;
        } catch (Exception re) {
            peopleCache.put(contactId, "");
            return "";
        }
    }

    protected static String queryField(Node n, String xpath) {
        Nodes results = n.query(xpath);
        if(results.size() > 0)
            return results.get(0).getValue();
        else
            return null;
    }

    protected static Document runRestRequest(String path, HttpClient client, Builder builder, String url, @Nullable EIPageInfo pageInfo, boolean badCredentialsOnError, FeedDefinition parentDefinition, boolean logRequest) throws BaseCampLoginException, ParsingException, ReportException {

        HttpMethod restMethod = new GetMethod(url + path);

        restMethod.setRequestHeader("Accept", "application/xml");
        restMethod.setRequestHeader("Content-Type", "application/xml");
        boolean successful = false;
        Document doc = null;
        int retryCount = 0;
        do {

            try {
                client.executeMethod(restMethod);
                if (logRequest) {
                    System.out.println(restMethod.getResponseBodyAsString());
                }
                doc = builder.build(restMethod.getResponseBodyAsStream());
                String rootValue = doc.getRootElement().getValue();
                if ("The API is not available to this account".equals(rootValue)) {
                    throw new ReportException(new DataSourceConnectivityReportFault("You need to enable API access to your Basecamp account--you can do this under Settings, API Export, Basecamp API in the Basecamp user interface.", parentDefinition));
                }

                if(pageInfo != null) {
                    Header xPagesHeader = restMethod.getResponseHeader("X-Pages");
                    if (xPagesHeader != null) {
                        pageInfo.MaxPages = Integer.parseInt(xPagesHeader.getValue());
                    }
                }
                successful = true;
            } catch (IOException e) {
                System.out.println("IOException " + e.getMessage());
                retryCount++;
                if (e.getMessage().contains("503") || e instanceof SocketException) {
                    //noinspection EmptyCatchBlock
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e1) {
                    }
                } else {
                    throw new RuntimeException(e);
                }
            } catch (ParsingException e) {
                System.out.println(e.getMessage());
                retryCount++;
                String statusLine = restMethod.getStatusLine().toString();
                if ("HTTP/1.1 404 Not Found".equals(statusLine)) {
                    throw new ReportException(new DataSourceConnectivityReportFault("Could not locate a Basecamp instance at " + url, parentDefinition));
                } else if (statusLine.contains("503")) {
                    Header retryHeader = restMethod.getResponseHeader("Retry-After");
                    if (retryHeader == null) {
                        //noinspection EmptyCatchBlock
                        try {
                            Thread.sleep(20000);
                        } catch (InterruptedException e1) {
                        }
                    } else {
                        int retryTime = Integer.parseInt(retryHeader.getValue());                        
                        int time = retryTime * 1000;
                        //noinspection EmptyCatchBlock
                        try {
                            Thread.sleep(time);
                        } catch (InterruptedException e1) {                            
                        }
                    }
                } else if (statusLine.contains("403")) {
                    throw new Http403Exception();
                } else {
                    if (badCredentialsOnError) {
                        throw new ReportException(new DataSourceConnectivityReportFault("Invalid Basecamp authentication token in connecting to " + url + "--you can find the token under your the My Info link in the upper right corner on your Basecamp page.", parentDefinition));
                    } else {
                        LogClass.error("Unrelated parse error with status line " + statusLine);
                        throw e;
                    }
                }
            } catch (ReportException re) {
                throw re;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } while (!successful && retryCount < 10);
        if (!successful) {
            throw new RuntimeException("Basecamp could not be reached due to a large number of current users, please try again in a bit.");
        }
        return doc;
    }
}
