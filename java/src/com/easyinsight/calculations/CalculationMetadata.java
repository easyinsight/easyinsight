package com.easyinsight.calculations;

import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.FilterDefinition;
import com.easyinsight.analysis.InsightRequestMetadata;
import com.easyinsight.analysis.WSAnalysisDefinition;
import com.easyinsight.dashboard.Dashboard;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.Feed;
import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.pipeline.IComponent;

import java.util.*;

/**
 * User: jamesboe
 * Date: 9/17/11
 * Time: 1:03 PM
 */
public class CalculationMetadata {
    private WSAnalysisDefinition report;
    private Collection<AnalysisItem> dataSourceFields;
    private DataSet dataSet;
    private FilterDefinition filterDefinition;
    private Dashboard dashboard;
    private Feed feed;
    private EIConnection connection;
    private Collection<FilterDefinition> filters;
    private InsightRequestMetadata insightRequestMetadata;
    private FeedDefinition dataSource;
    private Map<String, ICalculationCache> cacheMap = new HashMap<String, ICalculationCache>();
    private List<IComponent> generatedComponents = new ArrayList<IComponent>();

    public List<IComponent> getGeneratedComponents() {
        return generatedComponents;
    }

    public ICalculationCache getCache(ICacheBuilder cacheBuilder, String key) {
        ICalculationCache cache = cacheMap.get(key);
        if (cache == null) {
            cache = cacheBuilder.createCache();
            cache.fromDataSet(dataSet);
            cacheMap.put(key, cache);
        }
        return cache;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public FeedDefinition getDataSource() {
        return dataSource;
    }

    public void setDataSource(FeedDefinition dataSource) {
        this.dataSource = dataSource;
    }

    public InsightRequestMetadata getInsightRequestMetadata() {
        return insightRequestMetadata;
    }

    public void setInsightRequestMetadata(InsightRequestMetadata insightRequestMetadata) {
        this.insightRequestMetadata = insightRequestMetadata;
    }

    public Collection<FilterDefinition> getFilters() {
        return filters;
    }

    public void setFilters(Collection<FilterDefinition> filters) {
        this.filters = filters;
    }

    public EIConnection getConnection() {
        return connection;
    }

    public void setConnection(EIConnection connection) {
        this.connection = connection;
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    public FilterDefinition getFilterDefinition() {
        return filterDefinition;
    }

    public void setFilterDefinition(FilterDefinition filterDefinition) {
        this.filterDefinition = filterDefinition;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public Collection<AnalysisItem> getDataSourceFields() {
        return dataSourceFields;
    }

    public void setDataSourceFields(Collection<AnalysisItem> dataSourceFields) {
        this.dataSourceFields = dataSourceFields;
    }

    public WSAnalysisDefinition getReport() {
        return report;
    }

    public void setReport(WSAnalysisDefinition report) {
        this.report = report;
    }
}
