package com.easyinsight.datafeeds.basecamp;

import nu.xom.*;
import org.apache.commons.httpclient.HttpClient;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jamesboe
 * Date: Jun 7, 2010
 * Time: 10:51:28 AM
 */
public class BaseCampCache extends BaseCampBaseSource {
    private Map<String, String> userMap = new HashMap<String, String>();

    public void populateCaches(HttpClient client, String url) throws BaseCampLoginException, ParsingException {
        Builder builder = new Builder();
        Document projects = runRestRequest("/people.xml", client, builder, url, null, true);
        Nodes userNodes = projects.query("/people/person");
        for (int i = 0; i < userNodes.size(); i++) {
            Node userNode = userNodes.get(i);
            String id = queryField(userNode, "id/text()");
            String name = queryField(userNode, "first-name/text()") + " " + queryField(userNode, "last-name/text()");
            userMap.put(id, name);
        }
    }

    public String getUserName(String userID) {
        String name = userMap.get(userID);
        if (name == null) {
            name = "";
        }
        return name;
    }

    @NotNull
    @Override
    protected List<String> getKeys() {
        throw new UnsupportedOperationException();
    }
}
