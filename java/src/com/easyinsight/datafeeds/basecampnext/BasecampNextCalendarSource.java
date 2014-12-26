package com.easyinsight.datafeeds.basecampnext;

import com.easyinsight.analysis.*;
import com.easyinsight.core.Key;
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
import org.apache.commons.httpclient.HttpClient;
import org.jetbrains.annotations.NotNull;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: jamesboe
 * Date: 3/29/12
 * Time: 2:30 PM
 */
public class BasecampNextCalendarSource extends BasecampNextBaseSource {

    public static final String CALENDAR_ID = "Calendar ID";
    public static final String CALENDAR_NAME = "Calendar Name";
    public static final String UPDATED_AT = "Calendar Updated At";
    public static final String URL = "Calendar URL";
    public static final String CALENDAR_EVENT_ID = "Calendar Event ID";
    public static final String CALENDAR_EVENT_SUMMARY = "Calendar Event Summary";
    public static final String CALENDAR_EVENT_DESCRIPTION = "Calendar Event Description";
    public static final String CALENDAR_EVENT_CREATED_AT = "Calendar Event Created At";
    public static final String CALENDAR_EVENT_UPDATED_AT = "Calendar Event Updated At";
    public static final String CALENDAR_EVENT_ALL_DAY = "Calendar Event All Day";
    public static final String CALENDAR_EVENT_STARTS_AT = "Calendar Event Starts At";
    public static final String CALENDAR_EVENT_ENDS_AT = "Calendar Event Ends At";
    public static final String CALENDAR_EVENT_URL = "Calendar Event URL";
    public static final String CALENDAR_EVENT_COUNT = "Calendar Event Count";
    public static final String CALENDAR_EVENT_PROJECT_ID = "Calendar Event Project ID";

    public BasecampNextCalendarSource() {
        setFeedName("Calendar");
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.BASECAMP_NEXT_CALENDAR;
    }

    @NotNull
    @Override
    protected List<String> getKeys(FeedDefinition parentDefinition) {
        return Arrays.asList(CALENDAR_ID, CALENDAR_NAME, UPDATED_AT, URL, CALENDAR_EVENT_ID, CALENDAR_EVENT_SUMMARY,
                CALENDAR_EVENT_DESCRIPTION, CALENDAR_EVENT_CREATED_AT, CALENDAR_EVENT_UPDATED_AT, CALENDAR_EVENT_ALL_DAY,
                CALENDAR_EVENT_STARTS_AT, CALENDAR_EVENT_ENDS_AT, CALENDAR_EVENT_URL, CALENDAR_EVENT_COUNT, CALENDAR_EVENT_PROJECT_ID);
    }

    @Override
    public List<AnalysisItem> createAnalysisItems(Map<String, Key> keys, Connection conn, FeedDefinition parentDefinition) {
        List<AnalysisItem> analysisitems = new ArrayList<AnalysisItem>();
        analysisitems.add(new AnalysisDimension(keys.get(CALENDAR_ID), CALENDAR_ID));
        analysisitems.add(new AnalysisDimension(keys.get(CALENDAR_NAME), CALENDAR_NAME));
        analysisitems.add(new AnalysisDimension(keys.get(CALENDAR_EVENT_PROJECT_ID), CALENDAR_EVENT_PROJECT_ID));
        analysisitems.add(new AnalysisDimension(keys.get(URL), URL));
        analysisitems.add(new AnalysisDateDimension(keys.get(UPDATED_AT), true, AnalysisDateDimension.DAY_LEVEL));
        analysisitems.add(new AnalysisDimension(keys.get(CALENDAR_EVENT_ID)));
        analysisitems.add(new AnalysisDimension(keys.get(CALENDAR_EVENT_SUMMARY)));
        analysisitems.add(new AnalysisDimension(keys.get(CALENDAR_EVENT_DESCRIPTION)));
        analysisitems.add(new AnalysisDimension(keys.get(CALENDAR_EVENT_ALL_DAY)));
        analysisitems.add(new AnalysisDimension(keys.get(CALENDAR_EVENT_URL)));
        analysisitems.add(new AnalysisDateDimension(keys.get(CALENDAR_EVENT_CREATED_AT), true, AnalysisDateDimension.DAY_LEVEL));
        analysisitems.add(new AnalysisDateDimension(keys.get(CALENDAR_EVENT_UPDATED_AT), true, AnalysisDateDimension.DAY_LEVEL));
        analysisitems.add(new AnalysisDateDimension(keys.get(CALENDAR_EVENT_STARTS_AT), true, AnalysisDateDimension.DAY_LEVEL));
        analysisitems.add(new AnalysisDateDimension(keys.get(CALENDAR_EVENT_ENDS_AT), true, AnalysisDateDimension.DAY_LEVEL));
        analysisitems.add(new AnalysisMeasure(keys.get(CALENDAR_EVENT_COUNT), AggregationTypes.SUM));
        return analysisitems;
    }

    private Date parseDate(String string) {
        if (string != null && !"null".equals(string) && !"".equals(string)) {
            try {
                return format.parseDateTime(string).toDate();
            } catch (Exception e) {
                try {
                    return altFormat.parseDateTime(string).toDate();
                } catch (Exception e1) {
                    try {
                        return otherFormat.parse(string);
                    } catch (ParseException e2) {
                        LogClass.error(e2);
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private static final DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    private static final DateTimeFormatter altFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
    public static final DateFormat otherFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public DataSet getDataSet(Map<String, Key> keys, Date now, FeedDefinition parentDefinition, IDataStorage IDataStorage, EIConnection conn, String callDataID, Date lastRefreshDate) throws ReportException {
        HttpClient httpClient = new HttpClient();
        try {
            BasecampNextCompositeSource basecampNextCompositeSource = (BasecampNextCompositeSource) parentDefinition;
            if (basecampNextCompositeSource.isUseProjectUpdatedAt()) {
                DataSet dataSet = new DataSet();
                JSONArray jsonArray = runJSONRequest("calendars.json", (BasecampNextCompositeSource) parentDefinition, httpClient);
                for (int i = 0; i < jsonArray.size(); i++) {

                    JSONObject projectObject = (JSONObject) jsonArray.get(i);
                    String calendarID = getValue(projectObject, "id");
                    String calendarName = getValue(projectObject,"name");
                    String calendarURL = getValue(projectObject,"url");
                    Date calendarUpdatedAt;
                    try {
                        calendarUpdatedAt = format.parseDateTime(getValue(projectObject,"updated_at")).toDate();
                    } catch (Exception e) {
                        calendarUpdatedAt = altFormat.parseDateTime(getValue(projectObject,"updated_at")).toDate();
                    }
                    JSONArray eventArray = runJSONRequest("calendars/"+calendarID+"/calendar_events.json", (BasecampNextCompositeSource) parentDefinition, httpClient);
                    parseCalendarEvents(keys, dataSet, calendarID, calendarName, calendarURL, calendarUpdatedAt, eventArray, "");
                    JSONArray pastEventArray = runJSONRequest("calendars/"+calendarID+"/calendar_events/past.json", (BasecampNextCompositeSource) parentDefinition, httpClient);
                    parseCalendarEvents(keys, dataSet, calendarID, calendarName, calendarURL, calendarUpdatedAt, pastEventArray, "");
                }
                if (lastRefreshDate == null || lastRefreshDate.getTime() < 100) {
                    IDataStorage.insertData(dataSet);
                } else {
                    StringWhere stringWhere = new StringWhere(keys.get(CALENDAR_EVENT_PROJECT_ID), "");
                    IDataStorage.updateData(dataSet, Arrays.asList((IWhere) stringWhere));
                }

                List<Project> projects = basecampNextCompositeSource.getOrCreateProjectCache().getProjects();

                for (Project project : projects) {
                    String projectID = project.getId();
                    if (basecampNextCompositeSource.isUseProjectUpdatedAt()) {
                        if (lastRefreshDate != null && project.getUpdatedAt().before(lastRefreshDate)) {
                            System.out.println("skipping project " + project.getName() + " updated at " + project.getUpdatedAt());
                            continue;
                        } else {
                            System.out.println("not skipping " + project.getName());
                        }
                    }
                    dataSet = new DataSet();
                    try {
                        Object eventObj = runJSONRequest("projects/" + projectID + "/calendar_events.json", (BasecampNextCompositeSource) parentDefinition, httpClient);
                        if (eventObj instanceof JSONArray) {
                            JSONArray eventArray = (JSONArray) eventObj;
                            parseCalendarEvents(keys, dataSet, null, null, null, null, eventArray, projectID);
                        }
                        Object pastEventObject = runJSONRequest("projects/"+projectID+"/calendar_events/past.json", (BasecampNextCompositeSource) parentDefinition, httpClient);
                        if (pastEventObject instanceof JSONArray) {
                            JSONArray eventArray = (JSONArray) pastEventObject;
                            parseCalendarEvents(keys, dataSet, null, null, null, null, eventArray, projectID);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (lastRefreshDate == null || lastRefreshDate.getTime() < 100) {
                        IDataStorage.insertData(dataSet);
                    } else {
                        StringWhere stringWhere = new StringWhere(keys.get(CALENDAR_EVENT_PROJECT_ID), projectID);
                        IDataStorage.updateData(dataSet, Arrays.asList((IWhere) stringWhere));
                    }
                }
                return null;
            } else {
                DataSet dataSet = new DataSet();
                JSONArray jsonArray = runJSONRequest("calendars.json", (BasecampNextCompositeSource) parentDefinition, httpClient);
                for (int i = 0; i < jsonArray.size(); i++) {

                    JSONObject projectObject = (JSONObject) jsonArray.get(i);
                    String calendarID = getValue(projectObject, "id");
                    String calendarName = getValue(projectObject,"name");
                    String calendarURL = getValue(projectObject,"url");
                    Date calendarUpdatedAt;
                    try {
                        calendarUpdatedAt = format.parseDateTime(getValue(projectObject,"updated_at")).toDate();
                    } catch (Exception e) {
                        calendarUpdatedAt = altFormat.parseDateTime(getValue(projectObject,"updated_at")).toDate();
                    }
                    JSONArray eventArray = runJSONRequest("calendars/"+calendarID+"/calendar_events.json", (BasecampNextCompositeSource) parentDefinition, httpClient);
                    parseCalendarEvents(keys, dataSet, calendarID, calendarName, calendarURL, calendarUpdatedAt, eventArray, null);
                    JSONArray pastEventArray = runJSONRequest("calendars/"+calendarID+"/calendar_events/past.json", (BasecampNextCompositeSource) parentDefinition, httpClient);
                    parseCalendarEvents(keys, dataSet, calendarID, calendarName, calendarURL, calendarUpdatedAt, pastEventArray, null);
                }
                List<Project> projects = basecampNextCompositeSource.getOrCreateProjectCache().getProjects();

                for (Project project : projects) {
                    String projectID = project.getId();

                    try {
                        Object eventObj = runJSONRequest("projects/" + projectID + "/calendar_events.json", (BasecampNextCompositeSource) parentDefinition, httpClient);
                        if (eventObj instanceof JSONArray) {
                            JSONArray eventArray = (JSONArray) eventObj;
                            parseCalendarEvents(keys, dataSet, null, null, null, null, eventArray, projectID);
                        }
                        Object pastEventObject = runJSONRequest("projects/"+projectID+"/calendar_events/past.json", (BasecampNextCompositeSource) parentDefinition, httpClient);
                        if (pastEventObject instanceof JSONArray) {
                            JSONArray eventArray = (JSONArray) pastEventObject;
                            parseCalendarEvents(keys, dataSet, null, null, null, null, eventArray, projectID);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return dataSet;
            }

        } catch (ReportException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getUpdateKeyName() {
        return CALENDAR_EVENT_PROJECT_ID;
    }

    @Override
    protected boolean clearsData(FeedDefinition parentSource) {
        BasecampNextCompositeSource compositeSource = (BasecampNextCompositeSource) parentSource;
        return !compositeSource.isUseProjectUpdatedAt();
    }

    private void parseCalendarEvents(Map<String, Key> keys, DataSet dataSet, String calendarID, String calendarName, String calendarURL, Date calendarUpdatedAt, JSONArray eventArray, String projectID) {
        for (int j = 0; j < eventArray.size(); j++) {
            JSONObject calendarEvent = (JSONObject) eventArray.get(j);
            String id = getValue(calendarEvent,"id");
            String summary = getValue(calendarEvent,"summary");
            String url = getValue(calendarEvent,"url");
            String description = getValue(calendarEvent,"description");
            Date createdAt = parseDate(getValue(calendarEvent,"created_at"));
            Date updatedAt = parseDate(getValue(calendarEvent,"updated_at"));
            Date startsAt = parseDate(getValue(calendarEvent,"starts_at"));
            Date endsAt = parseDate(getValue(calendarEvent,"ends_at"));
            IRow row = dataSet.createRow();
            row.addValue(keys.get(CALENDAR_ID), calendarID);
            row.addValue(keys.get(CALENDAR_NAME), calendarName);
            row.addValue(keys.get(UPDATED_AT), calendarUpdatedAt);
            row.addValue(keys.get(URL), calendarURL);
            row.addValue(keys.get(CALENDAR_EVENT_ID), id);
            row.addValue(keys.get(CALENDAR_EVENT_SUMMARY), summary);
            row.addValue(keys.get(CALENDAR_EVENT_DESCRIPTION), description);
            row.addValue(keys.get(CALENDAR_EVENT_CREATED_AT), createdAt);
            row.addValue(keys.get(CALENDAR_EVENT_UPDATED_AT), updatedAt);
            row.addValue(keys.get(CALENDAR_EVENT_STARTS_AT), startsAt);
            row.addValue(keys.get(CALENDAR_EVENT_ENDS_AT), endsAt);
            row.addValue(keys.get(CALENDAR_EVENT_PROJECT_ID), projectID);
            row.addValue(keys.get(CALENDAR_EVENT_URL), url);
            row.addValue(keys.get(CALENDAR_EVENT_COUNT), 1);
        }
    }
}