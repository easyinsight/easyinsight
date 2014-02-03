package com.easyinsight.datafeeds.insightly;

import com.easyinsight.analysis.*;
import com.easyinsight.core.*;
import com.easyinsight.core.StringValue;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.storage.IDataStorage;
import org.apache.commons.httpclient.HttpClient;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: jamesboe
 * Date: 10/22/12
 * Time: 7:24 PM
 */
public class InsightlyOpportunitySource extends InsightlyBaseSource {

    public static final String OPPORTUNITY_ID = "Opportunity ID";

    public static final String NAME = "Opportunity Name";
    public static final String DETAILS = "Opportunity Details";
    public static final String PROBABILITY = "Probability";
    public static final String BID_CURRENCY = "Bid Currency";
    public static final String BID_AMOUNT = "Bid Amount";
    public static final String BID_TYPE = "Bid Type";
    public static final String BID_DURATION = "Bid Duration";
    public static final String FORECAST_CLOSE_DATE = "Forecast Close Date";
    public static final String ACTUAL_CLOSE_DATE = "Actual Close Date";
    public static final String CATEGORY = "Opportunity Category";
    public static final String PIPELINE = "Opportunity Pipeline";
    public static final String STAGE = "Opportunity Stage";
    public static final String STATE = "Opportunity State";
    public static final String RESPONSIBLE_USER = "Responsible User";
    public static final String RESPONSIBLE_CREATOR = "Opportunity Created By";
    public static final String DATE_CREATED = "Opportunity Date Created";
    public static final String DATE_UPDATED = "Opportunity Date Updated";
    public static final String OPPORTUNITY_COUNT = "Opportunity Count";

    public InsightlyOpportunitySource() {
        setFeedName("Opportunities");
    }

    @NotNull
    @Override
    protected List<String> getKeys(FeedDefinition parentDefinition) {
        return Arrays.asList(OPPORTUNITY_ID, NAME, DETAILS, DATE_CREATED, DATE_UPDATED, OPPORTUNITY_COUNT, BID_CURRENCY, BID_AMOUNT, BID_TYPE,
                BID_DURATION, FORECAST_CLOSE_DATE, ACTUAL_CLOSE_DATE, CATEGORY, STAGE, STATE, RESPONSIBLE_USER, RESPONSIBLE_CREATOR, PROBABILITY);
    }

    public List<AnalysisItem> createAnalysisItems(Map<String, Key> keys, Connection conn, FeedDefinition parentDefinition) {
        List<AnalysisItem> fields = new ArrayList<AnalysisItem>();
        fields.add(new AnalysisDimension(keys.get(OPPORTUNITY_ID)));
        fields.add(new AnalysisDimension(keys.get(NAME)));
        fields.add(new AnalysisDimension(keys.get(CATEGORY)));
        fields.add(new AnalysisDimension(keys.get(STAGE)));
        fields.add(new AnalysisDimension(keys.get(STATE)));
        fields.add(new AnalysisDimension(keys.get(RESPONSIBLE_USER)));
        fields.add(new AnalysisDimension(keys.get(RESPONSIBLE_CREATOR)));
        fields.add(new AnalysisDimension(keys.get(BID_TYPE)));
        fields.add(new AnalysisDimension(keys.get(BID_CURRENCY)));
        fields.add(new AnalysisDimension(keys.get(DETAILS)));
        InsightlyCompositeSource insightlyCompositeSource = (InsightlyCompositeSource) parentDefinition;
        HttpClient httpClient = getHttpClient(insightlyCompositeSource.getInsightlyApiKey(), "x");
        List customFields = runJSONRequest("customFields", insightlyCompositeSource, httpClient);
        for (Object customFieldObject : customFields) {
            Map customFieldMap = (Map) customFieldObject;
            String fieldFor = customFieldMap.get("FIELD_FOR").toString();
            if ("OPPORTUNITY".equals(fieldFor)) {
                String customFieldID = customFieldMap.get("CUSTOM_FIELD_ID").toString();
                Key key = keys.get(customFieldID);
                if (key == null) {
                    key = new NamedKey(customFieldID);
                }

                String fieldType = customFieldMap.get("FIELD_TYPE").toString();
                if ("DATE".equals(fieldType)) {
                    fields.add(new AnalysisDateDimension(key, customFieldMap.get("FIELD_NAME").toString(), AnalysisDateDimension.DAY_LEVEL));
                } else {
                    fields.add(new AnalysisDimension(key, customFieldMap.get("FIELD_NAME").toString()));
                }
            }

        }
        fields.add(new AnalysisDateDimension(keys.get(DATE_CREATED), true, AnalysisDateDimension.DAY_LEVEL));
        fields.add(new AnalysisDateDimension(keys.get(DATE_UPDATED), true, AnalysisDateDimension.DAY_LEVEL));
        fields.add(new AnalysisDateDimension(keys.get(FORECAST_CLOSE_DATE), true, AnalysisDateDimension.DAY_LEVEL));
        fields.add(new AnalysisDateDimension(keys.get(ACTUAL_CLOSE_DATE), true, AnalysisDateDimension.DAY_LEVEL));
        fields.add(new AnalysisMeasure(keys.get(OPPORTUNITY_COUNT), AggregationTypes.SUM));
        Key probabilityKey = keys.get(PROBABILITY);
        if (probabilityKey == null) {
            probabilityKey = new NamedKey(PROBABILITY);
        }
        Key pipelineKey = keys.get(PIPELINE);
        if (pipelineKey == null) {
            pipelineKey = new NamedKey(PIPELINE);
        }
        fields.add(new AnalysisDimension(pipelineKey, true));
        fields.add(new AnalysisMeasure(probabilityKey, PROBABILITY, AggregationTypes.AVERAGE, true, FormattingConfiguration.PERCENTAGE));
        fields.add(new AnalysisMeasure(keys.get(BID_DURATION), AggregationTypes.SUM));
        fields.add(new AnalysisMeasure(keys.get(BID_AMOUNT), BID_AMOUNT, AggregationTypes.SUM, true, FormattingConfiguration.CURRENCY));
        return fields;
    }

    private Value getValue(Map map, String param) {
        Object obj = map.get(param);
        if (obj == null) {
            return new EmptyValue();
        } else {
            return new StringValue(obj.toString());
        }
    }

    @Override
    public DataSet getDataSet(Map<String, Key> keys, Date now, FeedDefinition parentDefinition, IDataStorage IDataStorage, EIConnection conn, String callDataID, Date lastRefreshDate) throws ReportException {
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DataSet dataSet = new DataSet();
            InsightlyCompositeSource insightlyCompositeSource = (InsightlyCompositeSource) parentDefinition;
            HttpClient httpClient = getHttpClient(insightlyCompositeSource.getInsightlyApiKey(), "x");
            List userList = runJSONRequest("users", insightlyCompositeSource, httpClient);
            Map<String, String> userMap = new HashMap<String, String>();
            for (Object userObject : userList) {
                Map user = (Map) userObject;
                if (user.get("USER_ID") != null) {
                    String userID = user.get("USER_ID").toString();
                    Object firstNameObj = user.get("FIRST_NAME");
                    Object lastNameObj = user.get("LAST_NAME");
                    String name;
                    if (firstNameObj == null && lastNameObj == null) {
                        name = null;
                    } else if (firstNameObj != null && lastNameObj == null) {
                        name = firstNameObj.toString();
                    } else if (firstNameObj == null && lastNameObj != null) {
                        name = lastNameObj.toString();
                    } else {
                        name = firstNameObj.toString() + " " + lastNameObj.toString();
                    }
                    if (name != null) {
                        userMap.put(userID, name);
                    }

                }
            }

            Map<String, String> pipelineMap = new HashMap<String, String>();
            List pipelineList = runJSONRequest("Pipelines", insightlyCompositeSource, httpClient);
            for (Object pipelineObject : pipelineList) {
                Map pipelineObj = (Map) pipelineObject;
                String pipelineID = pipelineObj.get("PIPELINE_ID").toString();
                pipelineMap.put(pipelineID, pipelineObj.get("PIPELINE_NAME").toString());
            }
            List pipelineStageList = runJSONRequest("PipelineStages", insightlyCompositeSource, httpClient);
            Map<String, String> pipelineStageMap = new HashMap<String, String>();
            for (Object pipelineObject : pipelineStageList) {
                Map pipelineStageObject = (Map) pipelineObject;
                pipelineStageMap.put(pipelineStageObject.get("STAGE_ID").toString(), pipelineStageObject.get("STAGE_NAME").toString());
            }
            List categoryList = runJSONRequest("opportunityCategories", insightlyCompositeSource, httpClient);
            Map<String, String> categoryMap = new HashMap<String, String>();
            for (Object categoryObject : categoryList) {
                Map category = (Map) categoryObject;
                categoryMap.put(category.get("CATEGORY_ID").toString(), category.get("CATEGORY_NAME").toString());
            }
            List contactList = runJSONRequest("opportunities", insightlyCompositeSource, httpClient);
            for (Object contactObj : contactList) {
                IRow row = dataSet.createRow();
                Map contactMap = (Map) contactObj;
                row.addValue(keys.get(OPPORTUNITY_ID), contactMap.get("OPPORTUNITY_ID").toString());
                row.addValue(keys.get(NAME), getValue(contactMap, "OPPORTUNITY_NAME"));
                row.addValue(keys.get(DETAILS), getValue(contactMap, "OPPORTUNITY_DETAILS"));
                row.addValue(keys.get(BID_TYPE), getValue(contactMap, "BID_TYPE"));
                Value opportunityState = getValue(contactMap, "OPPORTUNITY_STATE");
                if ("LOST".equals(opportunityState.toString())) {
                    opportunityState = new StringValue("Lost");
                } else if ("OPEN".equals(opportunityState.toString())) {
                    opportunityState = new StringValue("Open");
                } else if ("WON".equals(opportunityState.toString())) {
                    opportunityState = new StringValue("Won");
                }
                row.addValue(keys.get(STATE), opportunityState);
                row.addValue(keys.get(PROBABILITY), getValue(contactMap, "PROBABILITY"));
                row.addValue(keys.get(BID_CURRENCY), getValue(contactMap, "BID_CURRENCY"));
                row.addValue(keys.get(BID_AMOUNT), getValue(contactMap, "BID_AMOUNT"));
                row.addValue(keys.get(BID_DURATION), getValue(contactMap, "BID_DURATION"));

                String responsibleUser = userMap.get(getValue(contactMap, "RESPONSIBLE_USER_ID").toString());
                if (responsibleUser != null) {
                    row.addValue(keys.get(RESPONSIBLE_USER), responsibleUser);
                }

                String dealCreator = userMap.get(getValue(contactMap, "OWNER_USER_ID").toString());
                if (dealCreator != null) {
                    row.addValue(keys.get(RESPONSIBLE_CREATOR), dealCreator);
                }

                String category = categoryMap.get(getValue(contactMap, "CATEGORY_ID").toString());
                if (category != null) {
                    row.addValue(keys.get(CATEGORY), category);
                }
                String pipelineStageID = pipelineStageMap.get(getValue(contactMap, "STAGE_ID").toString());
                if (pipelineStageID != null) {
                    row.addValue(keys.get(STAGE), pipelineStageID);
                }
                String pipelineID = pipelineMap.get(getValue(contactMap, "PIPELINE_ID").toString());
                if (pipelineID != null) {
                    row.addValue(keys.get(PIPELINE), pipelineID);
                }
                for (AnalysisItem field : getFields()) {
                    if (field.getKey().toKeyString().startsWith("OPPORTUNITY_FIELD")) {
                        if (field.hasType(AnalysisItemTypes.DATE_DIMENSION)) {
                            Object obj = contactMap.get(field.getKey().toKeyString());
                            if (obj != null) {
                                row.addValue(field.getKey(), sdf.parse(obj.toString()));
                            }
                        } else {
                            row.addValue(field.getKey(), getValue(contactMap, field.getKey().toKeyString()));
                        }
                    }
                }
                row.addValue(keys.get(DATE_CREATED), new DateValue(sdf.parse(contactMap.get("DATE_CREATED_UTC").toString())));
                row.addValue(keys.get(DATE_UPDATED), new DateValue(sdf.parse(contactMap.get("DATE_UPDATED_UTC").toString())));
                Object forecastObj = contactMap.get("FORECAST_CLOSE_DATE");
                if (forecastObj != null) {
                    row.addValue(keys.get(FORECAST_CLOSE_DATE), new DateValue(sdf.parse(forecastObj.toString())));
                }
                /*Object actualCloseObj = contactMap.get("ACTUAL_CLOSE_DATE");
                System.out.println(actualCloseObj);
                if (actualCloseObj != null) {*/
                    if ("Won".equals(opportunityState.toString())) {
                        row.addValue(keys.get(ACTUAL_CLOSE_DATE), new DateValue(sdf.parse(contactMap.get("DATE_UPDATED_UTC").toString())));
                    } else {
                        //row.addValue(keys.get(ACTUAL_CLOSE_DATE), new DateValue(sdf.parse(actualCloseObj.toString())));
                    }
                //}
                row.addValue(keys.get(OPPORTUNITY_COUNT), 1);
            }
            return dataSet;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.INSIGHTLY_OPPORTUNITIES;
    }
}
