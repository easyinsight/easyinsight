package com.easyinsight.datafeeds.youtrack;

import com.easyinsight.analysis.DataSourceConnectivityReportFault;
import com.easyinsight.analysis.ReportException;
import com.easyinsight.datafeeds.ServerDataSourceDefinition;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;

/**
 * User: jamesboe
 * Date: 10/16/12
 * Time: 5:48 PM
 */
public abstract class YouTrackBaseSource extends ServerDataSourceDefinition {
    protected static Document runRestRequest(String path, HttpClient client, Builder builder, YouTrackCompositeSource parentSource) throws ReportException, IOException, ParsingException {
        try {
            HttpMethod restMethod = new GetMethod(parentSource.createURL(path)) {
                @Override
                public boolean getFollowRedirects() {
                    return true;
                }
            };
            //restMethod.setFollowRedirects(true);
            //restMethod.setRequestHeader("Accept", "application/xml");
            //restMethod.setRequestHeader("Content-Type", "application/xml");

            restMethod.setRequestHeader("Cookie", parentSource.getCookie());

            client.executeMethod(restMethod);

            if (restMethod.getStatusCode() == 401) {
                throw new ReportException(new DataSourceConnectivityReportFault("Invalid user name or password.", parentSource));
            }
            String string = restMethod.getResponseBodyAsString();
            string = string.replaceAll("[\\000]*", "");
            ByteArrayInputStream bais = new ByteArrayInputStream(string.getBytes());
            return new Builder().build(bais);
        } catch (IllegalArgumentException e) {
            throw new ReportException(new DataSourceConnectivityReportFault(e.getMessage(), parentSource));
        }
    }
}
