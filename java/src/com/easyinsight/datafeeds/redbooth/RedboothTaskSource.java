package com.easyinsight.datafeeds.redbooth;

import com.easyinsight.analysis.*;
import com.easyinsight.core.Key;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.storage.IDataStorage;
import org.apache.commons.httpclient.HttpClient;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jamesboe
 * Date: 2/19/14
 * Time: 7:09 PM
 */
public class RedboothTaskSource extends RedboothBaseSource {
    public static final String ID = "ID";
    public static final String NAME = "Name";
    public static final String PROJECT_ID = "Project ID";
    public static final String TASK_LIST_ID = "Task List ID";
    public static final String STATUS = "Status";
    public static final String TYPE = "Type";
    public static final String ASSIGNED_TO = "Assignee";
    public static final String CREATED_AT = "Created At";
    public static final String POSITION = "Position";
    public static final String COMMENTS_COUNT = "Comment Count";
    public static final String URGENT = "Urgent";
    public static final String DUE_ON = "Due On";
    public static final String UPDATED_AT = "Updated At";
    public static final String COMPLETED_AT = "Completed At";
    public static final String COUNT = "Task Count";

    public RedboothTaskSource() {
        setFeedName("Tasks");
    }

    protected void createFields(FieldBuilder fieldBuilder, Connection conn, FeedDefinition parentDefinition) {
        fieldBuilder.addField(ID, new AnalysisDimension());
        fieldBuilder.addField(NAME, new AnalysisDimension());
        fieldBuilder.addField(PROJECT_ID, new AnalysisDimension());
        fieldBuilder.addField(TASK_LIST_ID, new AnalysisDimension());
        fieldBuilder.addField(POSITION, new AnalysisDimension());
        fieldBuilder.addField(URGENT, new AnalysisDimension());
        fieldBuilder.addField(TYPE, new AnalysisDimension());
        fieldBuilder.addField(STATUS, new AnalysisDimension());
        fieldBuilder.addField(ASSIGNED_TO, new AnalysisDimension());
        fieldBuilder.addField(CREATED_AT, new AnalysisDateDimension());
        fieldBuilder.addField(COMPLETED_AT, new AnalysisDateDimension());
        fieldBuilder.addField(DUE_ON, new AnalysisDateDimension());
        fieldBuilder.addField(UPDATED_AT, new AnalysisDateDimension());
        fieldBuilder.addField(COUNT, new AnalysisMeasure());
        fieldBuilder.addField(COMMENTS_COUNT, new AnalysisMeasure());
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.REDBOOTH_TASK;
    }

    @Override
    public DataSet getDataSet(Map<String, Key> keys, Date now, FeedDefinition parentDefinition, IDataStorage IDataStorage, EIConnection conn, String callDataID, Date lastRefreshDate) throws ReportException {
        RedboothCompositeSource redboothCompositeSource = (RedboothCompositeSource) parentDefinition;
        DataSet dataSet = new DataSet();
        HttpClient httpClient = getHttpClient(redboothCompositeSource);
        Map base = (Map) queryList("/api/1/tasks", redboothCompositeSource, httpClient);
        List<Map> references = (List<Map>) base.get("references");
        Map<String, String> users = new HashMap<String, String>();
        for (Map ref : references) {
            String type = ref.get("type").toString();
            if ("User".equals(type)) {
                users.put(ref.get("id").toString(), ref.get("first_name").toString() + " " + ref.get("last_name").toString());
            }
        }
        List<Map> organizations = (List<Map>) base.get("objects");
        for (Map org : organizations) {
            IRow row = dataSet.createRow();
            row.addValue(keys.get(ID), getJSONValue(org, "id"));
            row.addValue(keys.get(NAME), getJSONValue(org, "name"));
            row.addValue(keys.get(PROJECT_ID), getJSONValue(org, "project_id"));
            row.addValue(keys.get(TASK_LIST_ID), getJSONValue(org, "task_list_id"));
            row.addValue(keys.get(POSITION), getJSONValue(org, "position"));
            row.addValue(keys.get(COMMENTS_COUNT), getJSONValue(org, "comments_count"));
            row.addValue(keys.get(URGENT), getJSONValue(org, "urgent"));
            row.addValue(keys.get(TYPE), getJSONValue(org, "type"));
            row.addValue(keys.get(DUE_ON), getDate(org, "due_on"));
            String statusCode = getJSONValue(org, "status");
            String status = "";
            if ("0".equals(statusCode)) {
                status = "New";
            } else if ("1".equals(statusCode)) {
                status = "Open";
            } else if ("2".equals(statusCode)) {
                status = "Hold";
            } else if ("3".equals(statusCode)) {
                status = "Resolved";
            } else if ("4".equals(statusCode)) {
                status = "Rejected";
            }
            String assignedID = getJSONValue(org, "user_id");
            if (assignedID != null) {
                row.addValue(ASSIGNED_TO, users.get(assignedID));
            }
            row.addValue(keys.get(STATUS), status);
            row.addValue(CREATED_AT, getDate(org, "created_at"));
            row.addValue(UPDATED_AT, getDate(org, "updated_at"));
            row.addValue(COMPLETED_AT, getDate(org, "completed_at"));
            row.addValue(COUNT, 1);
        }
        return dataSet;
    }
}