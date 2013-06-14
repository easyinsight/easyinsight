package com.easyinsight.datafeeds.zendesk;

import com.easyinsight.analysis.*;
import com.easyinsight.core.*;
import com.easyinsight.core.StringValue;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.logging.LogClass;
import com.easyinsight.storage.IDataStorage;
import com.easyinsight.storage.IWhere;
import com.easyinsight.storage.StringWhere;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;
import org.apache.commons.httpclient.HttpClient;
import org.jetbrains.annotations.NotNull;

import java.net.URLEncoder;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: jamesboe
 * Date: 3/21/11
 * Time: 6:39 PM
 */
public class ZendeskCommentSource extends ZendeskBaseSource {

    public static final String COMMENT_TICKET_ID = "Comment Ticket ID";
    public static final String AUTHOR = "Comment Author";
    public static final String COMMENT_BODY = "Comment Body";
    public static final String COMMENT_CREATED_AT = "Comment Created At";
    public static final String COUNT = "Comment Count";

    public ZendeskCommentSource() {
        setFeedName("Comments");
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.ZENDESK_COMMENTS;
    }

    @NotNull
    @Override
    protected List<String> getKeys(FeedDefinition parentDefinition) {
        return new ArrayList<String>(Arrays.asList(COMMENT_TICKET_ID, COUNT, AUTHOR, COMMENT_BODY, COMMENT_CREATED_AT));
    }

    public List<AnalysisItem> createAnalysisItems(Map<String, Key> keys, Connection conn, FeedDefinition parentDefinition) {
        List<AnalysisItem> items = new ArrayList<AnalysisItem>();
        items.add(new AnalysisDimension(keys.get(COMMENT_TICKET_ID), true));
        items.add(new AnalysisDimension(keys.get(AUTHOR), true));
        items.add(new AnalysisDateDimension(keys.get(COMMENT_CREATED_AT), true, AnalysisDateDimension.DAY_LEVEL));
        items.add(new AnalysisText(keys.get(COMMENT_BODY)));
        items.add(new AnalysisMeasure(keys.get(COUNT), AggregationTypes.SUM));
        return items;
    }

    @Override
    public DataSet getDataSet(Map<String, Key> keys, Date now, FeedDefinition parentDefinition, IDataStorage IDataStorage, EIConnection conn, String callDataID, Date lastRefreshDate) throws ReportException {
        try {

            ZendeskCompositeSource zendeskCompositeSource = (ZendeskCompositeSource) parentDefinition;
            if(!zendeskCompositeSource.isLoadComments())
                return new DataSet();
            HttpClient httpClient = getHttpClient(zendeskCompositeSource.getZdUserName(), zendeskCompositeSource.getZdPassword());
            ZendeskUserCache zendeskUserCache = zendeskCompositeSource.getOrCreateUserCache(httpClient);
            return getAllTickets(keys, zendeskCompositeSource, zendeskUserCache, IDataStorage);

        } catch (ReportException re) {
            LogClass.error(re);
            throw re;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    private String getUserName(String userID, ZendeskUserCache zendeskUserCache) throws InterruptedException {
        ZendeskUser zendeskUser = zendeskUserCache.getUsers().get(userID);
        if (zendeskUser == null) {
            return "";
        } else {
            return zendeskUser.getName();
        }
    }

    @Override
    protected boolean clearsData(FeedDefinition parentSource) {
        return false;
    }

    private void getUpdatedTickets(Map<String, Key> keys, ZendeskCompositeSource zendeskCompositeSource, Date lastUpdateDate, IDataStorage IDataStorage,
                                   ZendeskUserCache zendeskUserCache) throws Exception {

        HttpClient httpClient = getHttpClient(zendeskCompositeSource.getZdUserName(), zendeskCompositeSource.getZdPassword());

        DateFormat updateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        String updateDate = updateFormat.format(cal.getTime());
        Key noteKey = zendeskCompositeSource.getField(COMMENT_TICKET_ID).toBaseKey();
        String nextPage = zendeskCompositeSource.getUrl() + "/api/v2/search.json?query=" + "type:comment%20updated>" + updateDate;
        while (nextPage != null) {
            //
            Map ticketObjects = queryList(nextPage, zendeskCompositeSource, httpClient);

            List results = (List) ticketObjects.get("results");
            for (Object obj : results) {
                Map map = (Map) obj;
                System.out.println("blah");
            }
            if (ticketObjects.get("next_page") != null) {
                nextPage = ticketObjects.get("next_page").toString();
            } else {
                nextPage = null;
            }
        }
        /*do {

            path += "&page=" + page;
            Document doc = runRestRequest(zendeskCompositeSource, httpClient, path, builder);
            Nodes ticketNodes = doc.query("/records/record");
            if (page == 1) {
                Nodes countNodes = doc.query("/records/@count");
                if (countNodes.size() == 1) {
                    count = Integer.parseInt(countNodes.get(0).getValue());
                } else {
                    count = 0;
                }
            }
            for (int i = 0; i < ticketNodes.size(); i++) {
                DataSet dataSet = new DataSet();
                Node ticketNode = ticketNodes.get(i);
                String id = parseTicket(keys, zendeskUserCache, dataSet, ticketNode);
                if (id != null) {
                    StringWhere userWhere = new StringWhere(noteKey, id);
                    IDataStorage.updateData(dataSet, Arrays.asList((IWhere) userWhere));
                }
            }
            page++;
            recordCount += 15;
        } while (recordCount < count);*/
    }

    private DataSet getAllTickets(Map<String, Key> keys, ZendeskCompositeSource zendeskCompositeSource, ZendeskUserCache userCache, IDataStorage dataStorage) throws Exception {
        DataSet dataSet = new DataSet();
        HttpClient httpClient = new HttpClient();
        Builder builder = new Builder();


        int page = 1;
        int count = 0;
        int recordCount = 0;
        for (String ticketId : zendeskCompositeSource.ticketIdList()) {
            String path = "/api/v2/tickets/" + ticketId + "/audits.json";
            do {
                JSONObject jo = (JSONObject) runJSONRestRequest(zendeskCompositeSource, httpClient, path, builder);
                System.out.println(jo.toString());
                JSONArray audits = (JSONArray) jo.get("audits");
                for (Object o : audits) {
                    JSONObject audit = (JSONObject) o;
                    JSONArray events = (JSONArray) audit.get("events");
                    for (Object oo : events) {
                        JSONObject event = (JSONObject) oo;
                        if ("Comment".equals(event.get("type"))) {
                            parseTicket(keys, userCache, dataSet, event, audit, ticketId);
                        }
                    }
                }
                dataStorage.insertData(dataSet);
                dataSet = new DataSet();
                path = (String) jo.get("next_page");
            } while(path != null);

        }
//        do {
//            String path = "/search.xml?query=type:comment";
//            if (page > 1) {
//                path += "&page=" + page;
//            }
//            Document doc = runRestRequest(zendeskCompositeSource, httpClient, path, builder);
//            if (page == 1) {
//                Nodes countNodes = doc.query("/records/@count");
//                if (countNodes.size() == 1) {
//                    count = Integer.parseInt(countNodes.get(0).getValue());
//                } else {
//                    count = 0;
//                }
//            }
//            Nodes ticketNodes = doc.query("/records/record");
//            for (int i = 0; i < ticketNodes.size(); i++) {
//                Node ticketNode = ticketNodes.get(i);
//                parseTicket(keys, userCache, dataSet, ticketNode);
//            }
//            page++;
//            dataStorage.insertData(dataSet);
//            dataSet = new DataSet();
//            recordCount += 15;
//        } while (recordCount < count);
        return null;
    }

    private String parseTicket(Map<String, Key> keys, ZendeskUserCache userCache, DataSet dataSet, JSONObject event, JSONObject audit, String ticketID) throws ParseException, InterruptedException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            Value author = queryUser(event.get("author_id").toString(), userCache);
            IRow row = dataSet.createRow();
            row.addValue(keys.get(COMMENT_TICKET_ID), ticketID);
            row.addValue(keys.get(AUTHOR), author);
            String a = (String) audit.get("created_at");
            System.out.println(a);
            row.addValue(keys.get(COMMENT_CREATED_AT), df.parse(a));
            System.out.println(audit.get("created_at"));
            row.addValue(keys.get(COMMENT_BODY), event.get("html_body").toString());
            row.addValue(keys.get(COUNT), 1);
            return ticketID;
        } catch (ReportException re) {
            throw re;
        } catch (Exception e) {
            LogClass.error(e);
        }
        return null;
    }

    @Override
    protected String getUpdateKeyName() {
        return COMMENT_TICKET_ID;
    }

    protected Value queryUser(String value, ZendeskUserCache zendeskUserCache) throws InterruptedException {
        if (value != null && !"".equals(value)) {
            try {
                return new StringValue(getUserName(value, zendeskUserCache));
            } catch (Exception e) {
                return new EmptyValue();
            }
        }
        return new EmptyValue();
    }

    protected Value queryDate(Node node, String target) throws ParseException {
        String value = queryField(node, target);
        if (value != null && !"".equals(value)) {
            try {
                return new DateValue(javax.xml.bind.DatatypeConverter.parseDateTime(value).getTime());
            } catch (Exception e) {
                LogClass.error(e);
                return new EmptyValue();
            }
        }
        return new EmptyValue();
    }

    private DataSet getTicketsChangedSince() {
        return null;
    }
}
