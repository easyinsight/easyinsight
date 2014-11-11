package com.easyinsight.datafeeds.insightly;

import com.easyinsight.analysis.DataSourceConnectivityReportFault;
import com.easyinsight.analysis.ReportException;
import com.easyinsight.datafeeds.ServerDataSourceDefinition;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.xml.security.utils.Base64;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * User: jamesboe
 * Date: 10/22/12
 * Time: 7:19 PM
 */
public abstract class InsightlyBaseSource extends ServerDataSourceDefinition {

    protected static HttpClient getHttpClient(String username, String password) {
        HttpClient client = new HttpClient();
        client.setTimeout(30000);
        client.getParams().setAuthenticationPreemptive(true);

        Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
        client.getState().setCredentials(new AuthScope(AuthScope.ANY), defaultcreds);
        return client;
    }

    public List runJSONRequest(String path, InsightlyCompositeSource insightlyCompositeSource, HttpClient client) {
        String url = "https://api.insight.ly/v2/" + path;
        HttpMethod restMethod = new GetMethod(url);
        restMethod.setRequestHeader("Accept", "application/json");
        restMethod.setRequestHeader("Content-Type", "application/json");

        try {
            client.executeMethod(restMethod);
            if (restMethod.getStatusCode() == 404) {
                throw new ReportException(new DataSourceConnectivityReportFault("Could not locate an Insightly instance at " + url, insightlyCompositeSource));
            } else if (restMethod.getStatusCode() == 401) {
                throw new ReportException(new DataSourceConnectivityReportFault("Your API key was invalid.", insightlyCompositeSource));
            }
            Object obj = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(restMethod.getResponseBodyAsStream());
            try {
                return (List) obj;
            } catch (ClassCastException e) {
                System.out.println(obj);
                throw e;
            }
        } catch (ReportException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List runJSONRequest21(String path, InsightlyCompositeSource insightlyCompositeSource, HttpClient client) {
        String url = "https://api.insight.ly/v2.1/" + path;
        HttpMethod restMethod = new GetMethod(url);
        restMethod.setRequestHeader("Accept", "application/json");
        restMethod.setRequestHeader("Content-Type", "application/json");

        try {
            client.executeMethod(restMethod);
            if (restMethod.getStatusCode() == 404) {
                throw new ReportException(new DataSourceConnectivityReportFault("Could not locate an Insightly instance at " + url, insightlyCompositeSource));
            } else if (restMethod.getStatusCode() == 401) {
                throw new ReportException(new DataSourceConnectivityReportFault("Your API key was invalid.", insightlyCompositeSource));
            }
            Object obj = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(restMethod.getResponseBodyAsStream());
            try {
                return (List) obj;
            } catch (ClassCastException e) {
                System.out.println(obj);
                throw e;
            }
        } catch (ReportException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map runJSONRequestForMap(String path, InsightlyCompositeSource insightlyCompositeSource, HttpClient client) {
        String url = "https://api.insight.ly/v2/" + path;
        HttpMethod restMethod = new GetMethod(url);
        restMethod.setRequestHeader("Accept", "application/json");
        restMethod.setRequestHeader("Content-Type", "application/json");

        try {
            client.executeMethod(restMethod);
            if (restMethod.getStatusCode() == 404) {
                throw new ReportException(new DataSourceConnectivityReportFault("Could not locate an Insightly instance at " + url, insightlyCompositeSource));
            } else if (restMethod.getStatusCode() == 401) {
                throw new ReportException(new DataSourceConnectivityReportFault("Your API key was invalid.", insightlyCompositeSource));
            }
            return (Map) new net.minidev.json.parser.JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(restMethod.getResponseBodyAsStream());
        } catch (ReportException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
