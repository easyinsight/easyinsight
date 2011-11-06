package com.easyinsight.pipeline;

import com.easyinsight.analysis.*;
import com.easyinsight.datafeeds.Feed;
import com.easyinsight.dataset.DataSet;

import java.util.*;

/**
 * User: James Boe
 * Date: May 18, 2009
 * Time: 2:19:19 PM
 */
public abstract class Pipeline {

    private List<IComponent> components = new ArrayList<IComponent>();
    private PipelineData pipelineData;
    private ResultsBridge resultsBridge = new ListResultsBridge();

    public Pipeline setup(WSAnalysisDefinition report, Feed dataSource, InsightRequestMetadata insightRequestMetadata) {
        List<AnalysisItem> allFields = new ArrayList<AnalysisItem>(dataSource.getFields());
        if (report.getAddedItems() != null) {
            allFields.addAll(report.getAddedItems());
        }
        Set<AnalysisItem> allNeededAnalysisItems = compilePipelineData(report, insightRequestMetadata, allFields, dataSource);
        components = generatePipelineCommands(allNeededAnalysisItems, pipelineData.getAllRequestedItems(), report.retrieveFilterDefinitions(), report, pipelineData.getAllItems());
        if (report.hasCustomResultsBridge()) {
            resultsBridge = report.getCustomResultsBridge();
        }
        return this;
    }

    public Pipeline setup(WSAnalysisDefinition report, Feed dataSource, InsightRequestMetadata insightRequestMetadata, List<AnalysisItem> allFields) {
        Set<AnalysisItem> allNeededAnalysisItems = compilePipelineData(report, insightRequestMetadata, allFields, dataSource);
        components = generatePipelineCommands(allNeededAnalysisItems, pipelineData.getAllRequestedItems(), report.retrieveFilterDefinitions(), report, pipelineData.getAllItems());
        if (report.hasCustomResultsBridge()) {
            resultsBridge = report.getCustomResultsBridge();
        }
        return this;
    }

    public Pipeline setup(WSAnalysisDefinition report, InsightRequestMetadata insightRequestMetadata, List<AnalysisItem> allItems) {
        Set<AnalysisItem> allNeededAnalysisItems = compilePipelineData(report, insightRequestMetadata, allItems, null);
        components = generatePipelineCommands(allNeededAnalysisItems, pipelineData.getAllRequestedItems(), report.retrieveFilterDefinitions(), report, pipelineData.getAllItems());
        if (report.hasCustomResultsBridge()) {
            resultsBridge = report.getCustomResultsBridge();
        }
        return this;
    }

    public PipelineData getPipelineData() {
        return pipelineData;
    }

    public Pipeline setup(Set<AnalysisItem> analysisItems, List<AnalysisItem> allFields) {
        pipelineData = new PipelineData(null, analysisItems, null, allFields, new HashMap<String, String>(), analysisItems);
        components = generatePipelineCommands(analysisItems, analysisItems, new ArrayList<FilterDefinition>(), null, allFields);
        return this;
    }

    protected abstract List<IComponent> generatePipelineCommands(Set<AnalysisItem> allNeededAnalysisItems, Set<AnalysisItem> reportItems, Collection<FilterDefinition> filters, WSAnalysisDefinition report, List<AnalysisItem> allItems);
         
    private Set<AnalysisItem> compilePipelineData(WSAnalysisDefinition report, InsightRequestMetadata insightRequestMetadata, List<AnalysisItem> allFields, Feed dataSource) {

        /*List<AnalysisItem> allFields = new ArrayList<AnalysisItem>(allItems);
        if (report.getAddedItems() != null) {
            allFields.addAll(report.getAddedItems());
        }*/

        Set<AnalysisItem> allRequestedAnalysisItems = report.getAllAnalysisItems();
        allRequestedAnalysisItems.remove(null);

        Set<AnalysisItem> allNeededAnalysisItems = new LinkedHashSet<AnalysisItem>();

        if (report.retrieveFilterDefinitions() != null) {
            for (FilterDefinition filterDefinition : report.retrieveFilterDefinitions()) {
                List<AnalysisItem> items = filterDefinition.getAnalysisItems(allFields, allRequestedAnalysisItems, false, true, CleanupComponent.AGGREGATE_CALCULATIONS);
                allNeededAnalysisItems.addAll(items);
            }
        }
        for (AnalysisItem item : allRequestedAnalysisItems) {
            if (item.isValid()) {
                List<AnalysisItem> baseItems = item.getAnalysisItems(allFields, allRequestedAnalysisItems, false, true, CleanupComponent.AGGREGATE_CALCULATIONS);
                allNeededAnalysisItems.addAll(baseItems);
                List<AnalysisItem> linkItems = item.addLinkItems(allFields);
                allNeededAnalysisItems.addAll(linkItems);
                if (item.isVirtual()) {
                    allNeededAnalysisItems.add(item);
                }
            }
        }
        allNeededAnalysisItems.addAll(report.getLimitFields());
        if (report.getReportRunMarmotScript() != null) {
            StringTokenizer toker = new StringTokenizer(report.getReportRunMarmotScript(), "\r\n");
            while (toker.hasMoreTokens()) {
                String line = toker.nextToken();
                List<AnalysisItem> items = ReportCalculation.getAnalysisItems(line, allFields, allRequestedAnalysisItems, false, true, CleanupComponent.AGGREGATE_CALCULATIONS);
                allNeededAnalysisItems.addAll(items);
            }
        }

        pipelineData = new PipelineData(report, allNeededAnalysisItems, insightRequestMetadata, allFields, dataSource == null ? new HashMap<String, String>() : dataSource.getProperties(), allRequestedAnalysisItems);
        return allNeededAnalysisItems;
    }

    protected final Collection<AnalysisItem> items(int type, Collection<AnalysisItem> items) {
        Collection<AnalysisItem> matchingItems = new ArrayList<AnalysisItem>();
        for (AnalysisItem item : items) {
            if (item.hasType(type)) {
                matchingItems.add(item);
            }
        }
        return matchingItems;
    }

    public DataSet toDataSet(DataSet dataSet) {
        for (IComponent component : components) {
            //System.out.println(component.getClass().getName() + " - " + dataSet);
            dataSet = component.apply(dataSet, pipelineData);
        }
        return dataSet;
    }

    private DataSet resultSet;

    public DataSet getResultSet() {
        return resultSet;
    }

    public DataResults toList(DataSet dataSet) {
        for (IComponent component : components) {
            //System.out.println(component.getClass().getName() + " - " + dataSet);
            dataSet = component.apply(dataSet, pipelineData);
        }
        resultSet = dataSet;
        DataResults results = resultsBridge.toDataResults(dataSet, new ArrayList<AnalysisItem>(pipelineData.getAllRequestedItems()));
        for (IComponent component : components) {
            component.decorate(results);
        }
        return results;
    }
}
