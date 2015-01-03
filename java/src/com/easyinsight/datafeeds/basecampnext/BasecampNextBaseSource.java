package com.easyinsight.datafeeds.basecampnext;

import com.easyinsight.analysis.DataSourceConnectivityReportFault;
import com.easyinsight.analysis.ReportException;
import com.easyinsight.datafeeds.ServerDataSourceDefinition;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.poi.util.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.joda.time.format.DateTimeFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * User: jamesboe
 * Date: 3/26/12
 * Time: 11:06 AM
 */
public abstract class BasecampNextBaseSource extends ServerDataSourceDefinition {

    protected static Date getDate(Map n, String key, DateTimeFormatter simpleDateFormat) throws java.text.ParseException {
        Object obj = n.get(key);
        if(obj != null)
            return simpleDateFormat.parseDateTime(obj.toString()).toDate();
        else
            return null;
    }

    protected static String getValue(Map n, String key) {
        Object obj = n.get(key);
        if(obj != null)
            return obj.toString();
        else
            return null;
    }

    protected JSONArray runJSONRequest(String path, BasecampNextCompositeSource parentDefinition, HttpClient httpClient) {
        return runJSONRequest(path, parentDefinition, null, httpClient);
    }

    protected JSONArray runJSONRequest(String path, BasecampNextCompositeSource parentDefinition, @Nullable Date lastRefreshDate, HttpClient httpClient) {
        if (parentDefinition.getEndpoint() == null) {
            throw new ReportException(new DataSourceConnectivityReportFault("You need to reauthorize Easy Insight access to your Basecamp account.", parentDefinition));
        }
        DateFormat df = new SimpleDateFormat("EEE',' dd MMM yyyy HH:mm:ss Z");
        HttpMethod restMethod = new GetMethod("https://basecamp.com/"+parentDefinition.getEndpoint()+"/api/v1/" + path);

        restMethod.setRequestHeader("Authorization", "Bearer " + parentDefinition.getAccessToken());
        restMethod.setRequestHeader("Content-Type", "Content-Type: application/json; charset=utf-8");
        restMethod.setRequestHeader("User-Agent", "Easy Insight (http://www.easy-insight.com/)");
        /*if (lastRefreshDate != null && lastRefreshDate.getTime() >= 100) {
            restMethod.setRequestHeader("If-None-Match", df.format(lastRefreshDate));
            restMethod.setRequestHeader("If-Modified-Since", df.format(lastRefreshDate));
        }*/
        boolean successful = false;
        JSONArray jsonObject = null;
        int retryCount = 0;
        do {
            String responseString = null;
            try {
                httpClient.executeMethod(restMethod);
                if (lastRefreshDate != null && lastRefreshDate.getTime() >= 100) {
                    
                    Header header = restMethod.getResponseHeader("Last-Modified");
                    if (header != null) {
                        Date lastDate = df.parse(header.getValue());
                        long delta = lastRefreshDate.getTime() - lastDate.getTime();
                        long daysSinceChange = delta / (60 * 60 * 1000 * 24);
                        if (daysSinceChange > 2) {
                            return null;
                        }
                        // Thu, 29 Mar 2012 20:18:58 GMT
                    }
                    //System.out.println("argh");
                }
                //System.out.println(restMethod.getResponseBodyAsString());
                if (restMethod.getStatusCode() == 404 && path.endsWith("projects.json")) {
                    throw new ReportException(new DataSourceConnectivityReportFault("We were unable to retrieve the list of projects for this Basecamp account. Check that you created this connection under the right 37Signals user, and if so, that your Basecamp account is still active. If this problem persists, contact support@easy-insight.com.", parentDefinition));
                }
                if (restMethod.getStatusCode() == 429 || restMethod.getStatusCode() == 502 || restMethod.getStatusCode() == 503 ||
                        restMethod.getStatusCode() == 504 || restMethod.getStatusCode() == 408) {
                    retryCount++;
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e1) {
                    }
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    IOUtils.copy(restMethod.getResponseBodyAsStream(), baos);
                    String string = new String(baos.toByteArray(), Charset.forName("UTF-8"));
                    jsonObject = (JSONArray) new net.minidev.json.parser.JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(string.getBytes("UTF-8"));
                    restMethod.releaseConnection();
                    successful = true;
                }
            } catch (IOException e) {
                retryCount++;
                if (e.getMessage().contains("429") || e instanceof SocketException) {
                    //noinspection EmptyCatchBlock
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e1) {
                    }
                } else {
                    throw new RuntimeException(e);
                }
            } catch (ReportException re) {
                throw re;
            } catch (Throwable e) {
                System.out.println("Response on " + "https://basecamp.com/"+parentDefinition.getEndpoint()+"/api/v1/" + path + " generated " + responseString);
                throw new RuntimeException(e);
            }
        } while (!successful && retryCount < 10);
        if (!successful) {
            throw new RuntimeException("Basecamp could not be reached due to a large number of current users, please try again in a bit.");
        }
        return jsonObject;
    }

    public static void main(String[] args) throws ParseException {
        DateFormat df = new SimpleDateFormat("EEE',' dd MMM yyyy HH:mm:ss Z");
        System.out.println(df.parse("Thu, 29 Mar 2012 20:18:58 GMT"));
    }

    protected JSONObject runJSONRequestForObject(String path, BasecampNextCompositeSource parentDefinition, HttpClient httpClient) {

        HttpMethod restMethod = new GetMethod("https://basecamp.com/"+parentDefinition.getEndpoint()+"/api/v1/" + path);

        restMethod.setRequestHeader("Authorization", "Bearer " + parentDefinition.getAccessToken());
        restMethod.setRequestHeader("Content-Type", "Content-Type: application/json; charset=utf-8");
        restMethod.setRequestHeader("User-Agent", "Easy Insight (http://www.easy-insight.com/)");
        boolean successful = false;
        JSONObject jsonObject = null;
        int retryCount = 0;
        do {
            String responseString = null;
            try {
                httpClient.executeMethod(restMethod);
                if (restMethod.getStatusCode() == 429 || restMethod.getStatusCode() == 502 || restMethod.getStatusCode() == 503 ||
                        restMethod.getStatusCode() == 504 || restMethod.getStatusCode() == 408) {
                    retryCount++;
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e1) {
                    }
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    IOUtils.copy(restMethod.getResponseBodyAsStream(), baos);
                    String string = new String(baos.toByteArray(), Charset.forName("UTF-8"));
                    jsonObject = (JSONObject) new net.minidev.json.parser.JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(string.getBytes("UTF-8"));
                    successful = true;
                }
            } catch (IOException e) {
                System.out.println("IOException " + e.getMessage());
                retryCount++;
                if (e.getMessage().contains("429") || e instanceof SocketException) {
                    //noinspection EmptyCatchBlock
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e1) {
                    }
                } else {
                    throw new RuntimeException(e);
                }
            } catch (ReportException re) {
                throw re;
            } catch (Throwable e) {
                System.out.println("Response on " + "https://basecamp.com/"+parentDefinition.getEndpoint()+"/api/v1/" + path + " generated " + responseString);
                throw new RuntimeException(e);
            }
        } while (!successful && retryCount < 10);
        if (!successful) {
            throw new RuntimeException("Basecamp could not be reached due to a large number of current users, please try again in a bit.");
        }
        return jsonObject;
    }

    protected JSONObject rawJSONRequestForObject(String path, BasecampNextCompositeSource parentDefinition) {
        HttpClient client = new HttpClient();
        HttpMethod restMethod = new GetMethod(path);

        restMethod.setRequestHeader("Authorization", "Bearer " + parentDefinition.getAccessToken());
        restMethod.setRequestHeader("Content-Type", "Content-Type: application/json; charset=utf-8");
        restMethod.setRequestHeader("User-Agent", "Easy Insight (http://www.easy-insight.com/)");
        boolean successful = false;
        JSONObject jsonObject = null;
        int retryCount = 0;
        do {
            String responseString = null;
            try {
                client.executeMethod(restMethod);
                if (restMethod.getStatusCode() == 429 || restMethod.getStatusCode() == 502 || restMethod.getStatusCode() == 503 ||
                        restMethod.getStatusCode() == 504 || restMethod.getStatusCode() == 408) {
                    retryCount++;
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e1) {
                    }
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    IOUtils.copy(restMethod.getResponseBodyAsStream(), baos);
                    String string = new String(baos.toByteArray(), Charset.forName("UTF-8"));
                    jsonObject = (JSONObject) new net.minidev.json.parser.JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(string.getBytes("UTF-8"));
                    successful = true;
                }
            } catch (IOException e) {
                System.out.println("IOException " + e.getMessage());
                retryCount++;
                if (e.getMessage().contains("429") || e instanceof SocketException) {
                    //noinspection EmptyCatchBlock
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e1) {
                    }
                } else {
                    throw new RuntimeException(e);
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
        return jsonObject;
    }
}
