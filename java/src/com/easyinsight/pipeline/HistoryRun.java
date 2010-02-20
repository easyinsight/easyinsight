package com.easyinsight.pipeline;

import com.easyinsight.dataset.DataSet;
import com.easyinsight.analysis.*;
import com.easyinsight.datafeeds.Feed;
import com.easyinsight.datafeeds.FeedRegistry;
import com.easyinsight.datafeeds.CredentialFulfillment;
import com.easyinsight.core.Value;
import com.easyinsight.kpi.KPIValue;

import java.util.*;

/**
 * User: James Boe
 * Date: Jul 5, 2009
 * Time: 9:48:00 PM
 */
public class HistoryRun {

    public List<KPIValue> lastTwoValues(long dataSourceID, AnalysisMeasure measure, List<FilterDefinition> filters,
                                                     List<CredentialFulfillment> credentials, int timeWindow) throws TokenMissingException {
        // the way this should work...

        // if date dimension and time window
        // make the query where the date is X and the filter is Y

        FilterDefinition rollingFilter = null;
        for (FilterDefinition filter : filters) {
            if (filter instanceof RollingFilterDefinition) {
                rollingFilter = filter;
                filter.getField().setSort(2);
            }
        }

        Feed feed = FeedRegistry.instance().getFeed(dataSourceID);


        Date endDate = new Date();


        if (rollingFilter != null) {

            long time;
            if (timeWindow == 0) {
                time = 1000L * 60L * 60L * 24L * 2L;
            } else {
                time = 1000L * 60L * 60L * 24L * timeWindow;
            }
            Date startDate = new Date(endDate.getTime() - time);


            KPIValue startValue = blah(createReport(dataSourceID, measure, filters), feed, startDate, measure, credentials);
            KPIValue endValue = blah(createReport(dataSourceID, measure, filters), feed, endDate, measure, credentials);
            return Arrays.asList(startValue, endValue);
        } else {
            KPIValue value = blah(createReport(dataSourceID, measure, filters), feed, endDate, measure, credentials);
            return Arrays.asList(value);
        }
    }

    public WSAnalysisDefinition createReport(long dataSourceID, AnalysisMeasure analysisMeasure, List<FilterDefinition> filters) {
        WSListDefinition report = new WSListDefinition();
        report.setDataFeedID(dataSourceID);
        report.setColumns(Arrays.asList((AnalysisItem) analysisMeasure));
        report.setFilterDefinitions(filters);
        return report;
    }

    public KPIValue blah(WSAnalysisDefinition analysisDefinition, Feed feed, Date endDate,
                         AnalysisMeasure measure, List<CredentialFulfillment> credentials) {
        InsightRequestMetadata insightRequestMetadata = new InsightRequestMetadata();
        insightRequestMetadata.setCredentialFulfillmentList(credentials);
        insightRequestMetadata.setNow(endDate);
        Set<AnalysisItem> analysisItems = analysisDefinition.getColumnItems(feed.getFields());
        Set<AnalysisItem> validQueryItems = new HashSet<AnalysisItem>();
        for (AnalysisItem analysisItem : analysisItems) {
            if (!analysisItem.isDerived()) {
                validQueryItems.add(analysisItem);
            }
        }
        boolean aggregateQuery = true;
        for (AnalysisItem analysisItem : analysisDefinition.getAllAnalysisItems()) {
            if (analysisItem.blocksDBAggregation()) {
                aggregateQuery = false;
            }
        }
        insightRequestMetadata.setAggregateQuery(aggregateQuery);
        Collection<FilterDefinition> filters = analysisDefinition.retrieveFilterDefinitions();
        DataSet dataSet = feed.getAggregateDataSet(validQueryItems, filters, insightRequestMetadata, feed.getFields(), false);
        //results = dataSet.toList(analysisDefinition, feed.getFields(), insightRequestMetadata);
        Pipeline pipeline = new StandardReportPipeline();
        pipeline.setup(analysisDefinition, feed, insightRequestMetadata);
        DataSet result = pipeline.toDataSet(dataSet);
        KPIValue goalValue;
        if (result.getRows().size() > 0) {
            IRow row = result.getRow(0);
            Value value = row.getValue(measure.createAggregateKey());
            goalValue = new KPIValue();
            goalValue.setDate(endDate);
            goalValue.setValue(value.toDouble());
        } else {
            goalValue = new KPIValue();
            goalValue.setDate(endDate);
            goalValue.setValue(0);
        }
        return goalValue;
    }
}
