package com.easyinsight.analysis;

import com.easyinsight.analysis.definitions.*;
import com.easyinsight.analysis.gauge.GaugeDefinitionState;

import java.util.ArrayList;
import java.util.List;

/**
 * User: James Boe
 * Date: Jan 10, 2008
 * Time: 8:08:08 PM
 */
public class AnalysisDefinitionFactory {

    public static AnalysisDefinition fromWSDefinition(WSAnalysisDefinition wsAnalysisDefinition) {
        AnalysisDefinitionState analysisDefinitionState;
        if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.CROSSTAB)) {
            WSCrosstabDefinition wsCrosstabDefinition = (WSCrosstabDefinition) wsAnalysisDefinition;
            CrosstabDefinitionState crosstabDefinition = new CrosstabDefinitionState();
            crosstabDefinition.setCrosstabDefinitionID(wsCrosstabDefinition.getCrosstabDefinitionID());
            analysisDefinitionState = crosstabDefinition;
        } else if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.LIST)) {
            WSListDefinition wsListDefinition = (WSListDefinition) wsAnalysisDefinition;
            ListDefinitionState listDefinition = new ListDefinitionState();
            listDefinition.setShowRowNumbers(wsListDefinition.isShowLineNumbers());
            listDefinition.setListLimitsMetadata(wsListDefinition.getListLimitsMetadata());
            listDefinition.setSummarizeAll(wsListDefinition.isSummaryTotal());
            listDefinition.setDefinitionID(wsListDefinition.getListDefinitionID());
            analysisDefinitionState = listDefinition;
        } else if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.CHART)) {
            WSChartDefinition wsChart = (WSChartDefinition) wsAnalysisDefinition;
            ChartDefinitionState chartDefinition = new ChartDefinitionState();
            if (Double.isNaN(wsChart.getElevationAngle())) {
                wsChart.setElevationAngle(0);
            }
            if (Double.isNaN(wsChart.getRotationAngle())) {
                wsChart.setRotationAngle(0);
            }
            chartDefinition.setElevationAngle(wsChart.getElevationAngle());
            chartDefinition.setRotationAngle(wsChart.getRotationAngle());
            chartDefinition.setChartFamily(wsChart.getChartFamily());
            chartDefinition.setChartType(wsChart.getChartType());
            chartDefinition.setLimitsMetadata(wsChart.getLimitsMetadata());
            chartDefinition.setDefinitionID(wsChart.getChartDefinitionID());
            analysisDefinitionState = chartDefinition;
        } else if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.MAP)) {
            WSMapDefinition wsMap = (WSMapDefinition) wsAnalysisDefinition;
            MapDefinitionState mapDefinition = new MapDefinitionState();
            mapDefinition.setMapType(wsMap.getMapType());
            mapDefinition.setMapDefinitionID(wsMap.getMapDefinitionID());
            analysisDefinitionState = mapDefinition;
        } else if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.GAUGE)) {
            WSGaugeDefinition wsGauge = (WSGaugeDefinition) wsAnalysisDefinition;
            GaugeDefinitionState gaugeDefinition = new GaugeDefinitionState();
            gaugeDefinition.setGaugeType(wsGauge.getGaugeType());
            gaugeDefinition.setMaxValue(wsGauge.getMaxValue());
            gaugeDefinition.setGaugeDefinitionID(wsGauge.getGaugeDefinitionID());
            analysisDefinitionState = gaugeDefinition;
        } else if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.TREEMAP)) {
            WSTreeMapDefinition wsTree = (WSTreeMapDefinition) wsAnalysisDefinition;
            TreeMapDefinitionState treeMapDefinitionState = new TreeMapDefinitionState();
            treeMapDefinitionState.setDefinitionID(wsTree.getTreeMapDefinitionID());
            treeMapDefinitionState.setColorScheme(wsTree.getColorScheme());
            analysisDefinitionState = treeMapDefinitionState;
        } else if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.TREE)) {
            WSTreeDefinition wsTree = (WSTreeDefinition) wsAnalysisDefinition;
            TreeDefinitionState treeDefinitionState = new TreeDefinitionState();
            treeDefinitionState.setTreeDefinitionID(wsTree.getTreeDefinitionID());
            analysisDefinitionState = treeDefinitionState;
         }else if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.FORM)) {
            WSForm wsForm = (WSForm) wsAnalysisDefinition;
            FormDefinitionState formDefinitionState = new FormDefinitionState();
            formDefinitionState.setFormID(wsForm.getFormID());
            analysisDefinitionState = formDefinitionState;
        } else if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.TIMELINE)) {
            WSTimeline wsTimeline = (WSTimeline) wsAnalysisDefinition;
            TimelineDefinitionState timelineDefinitionState = new TimelineDefinitionState();
            timelineDefinitionState.setDefinitionID(wsTimeline.getTimelineID());
            timelineDefinitionState.setFilter(wsTimeline.getSequence());
            timelineDefinitionState.setContainedReport(AnalysisDefinitionFactory.fromWSDefinition(wsTimeline.getReport()));
            analysisDefinitionState = timelineDefinitionState;
        } else if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.HEATMAP)) {
            WSHeatMap heatMap = (WSHeatMap) wsAnalysisDefinition;
            HeatMapDefinitionState heatMapDefinitionState = new HeatMapDefinitionState();
            heatMapDefinitionState.setLatitude(heatMap.getLatitude());
            heatMapDefinitionState.setLongitude(heatMap.getLongitude());
            heatMapDefinitionState.setMapType(heatMap.getMapType());
            heatMapDefinitionState.setDisplayType(heatMap.getDisplayType());
            heatMapDefinitionState.setZoomLevel(heatMap.getZoomLevel());
            heatMapDefinitionState.setHeatMapDefinitionID(heatMap.getHeatMapID());
            heatMapDefinitionState.setMinLat(heatMap.getMinLat());
            heatMapDefinitionState.setMinLong(heatMap.getMinLong());
            heatMapDefinitionState.setMaxLat(heatMap.getMaxLat());
            heatMapDefinitionState.setMaxLong(heatMap.getMaxLong());
            analysisDefinitionState = heatMapDefinitionState;
        } else if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.GANTT)) {
            WSGanttChartDefinition gantt = (WSGanttChartDefinition) wsAnalysisDefinition;
            GanttChartDefinitionState ganttChartDefinitionState = new GanttChartDefinitionState();
            ganttChartDefinitionState.setGanttDefinitionID(gantt.getGanttDefinitionID());
            analysisDefinitionState = ganttChartDefinitionState;
        } else if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.VERTICAL_LIST)) {
            WSVerticalListDefinition verticalListDefinition = (WSVerticalListDefinition) wsAnalysisDefinition;
            VerticalListDefinitionState verticalListDefinitionState = new VerticalListDefinitionState();
            verticalListDefinitionState.setVerticalListID(verticalListDefinition.getVerticalListID());
            analysisDefinitionState = verticalListDefinitionState;
        } else if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.YTD)) {
            WSYTDDefinition ytdReport = (WSYTDDefinition) wsAnalysisDefinition;
            YTDDefinitionState ytdDefinitionState = new YTDDefinitionState();
            ytdDefinitionState.setYtdID(ytdReport.getYtdID());
            analysisDefinitionState = ytdDefinitionState;
        } else if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.COMPARE_YEARS)) {
            WSCompareYearsDefinition ytdReport = (WSCompareYearsDefinition) wsAnalysisDefinition;
            CompareYearsDefinitionState ytdDefinitionState = new CompareYearsDefinitionState();
            ytdDefinitionState.setYtdID(ytdReport.getYtdID());
            analysisDefinitionState = ytdDefinitionState;
        } else if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.COMBINED_VERTICAL_LIST)) {
            WSCombinedVerticalListDefinition verticalListDefinition = (WSCombinedVerticalListDefinition) wsAnalysisDefinition;
            CombinedVerticalListDefinitionState verticalListDefinitionState = new CombinedVerticalListDefinitionState();
            verticalListDefinitionState.setCombinedVerticalListID(verticalListDefinition.getCombinedVerticalListDefinitionID());
            List<AnalysisDefinition> reports = new ArrayList<AnalysisDefinition>();
            for (WSAnalysisDefinition child : verticalListDefinition.getReports()) {
                reports.add(AnalysisDefinitionFactory.fromWSDefinition(child));
            }
            verticalListDefinitionState.setChildReports(reports);
            analysisDefinitionState = verticalListDefinitionState;
        } else if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.TREND)) {
            WSTrendDefinition wsTrendDefinition = (WSTrendDefinition) wsAnalysisDefinition;
            TrendDefinitionState trendDefinitionState = new TrendDefinitionState();
            trendDefinitionState.setTrendReportID(wsTrendDefinition.getTrendReportID());
            trendDefinitionState.setFilterName(wsTrendDefinition.getFilterName());
            trendDefinitionState.setDayWindow(String.valueOf(wsTrendDefinition.getDayWindow()));
            analysisDefinitionState = trendDefinitionState;
        } else if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.TREND_GRID)) {
            WSTrendGridDefinition wsTrendDefinition = (WSTrendGridDefinition) wsAnalysisDefinition;
            TrendGridDefinitionState trendDefinitionState = new TrendGridDefinitionState();
            trendDefinitionState.setTrendReportID(wsTrendDefinition.getTrendReportID());
            trendDefinitionState.setSortDirection(wsTrendDefinition.isSortAscending());
            trendDefinitionState.setSortIndex(wsTrendDefinition.getSortIndex());
            trendDefinitionState.setFilterName(wsTrendDefinition.getFilterName());
            trendDefinitionState.setDayWindow(String.valueOf(wsTrendDefinition.getDayWindow()));
            analysisDefinitionState = trendDefinitionState;
        } else if (wsAnalysisDefinition.getDataFeedType().equals(AnalysisTypes.DIAGRAM)) {
            WSDiagramDefinition wsTrendDefinition = (WSDiagramDefinition) wsAnalysisDefinition;
            DiagramDefinitionState trendDefinitionState = new DiagramDefinitionState();
            trendDefinitionState.setDiagramReportID(wsTrendDefinition.getDiagramReportID());
            trendDefinitionState.setFilterName(wsTrendDefinition.getFilterName());
            trendDefinitionState.setDayWindow(String.valueOf(wsTrendDefinition.getDayWindow()));
            trendDefinitionState.setLinks(wsTrendDefinition.getLinks());
            analysisDefinitionState = trendDefinitionState;
        } else {
            throw new RuntimeException("Unknown data feed type " + wsAnalysisDefinition.getDataFeedType());
        }
        analysisDefinitionState.setId(wsAnalysisDefinition.getReportStateID());
        AnalysisDefinition analysisDefinition = new AnalysisDefinition();
        analysisDefinition.setProperties(wsAnalysisDefinition.createProperties());
        for (ReportProperty reportProperty : analysisDefinition.getProperties()) {
            reportProperty.cleanup();
        }
        analysisDefinition.setUrlKey(wsAnalysisDefinition.getUrlKey());
        analysisDefinition.setJoinOverrides(wsAnalysisDefinition.getJoinOverrides());
        analysisDefinition.setAnalysisDefinitionState(analysisDefinitionState);
        analysisDefinition.setDescription(wsAnalysisDefinition.getDescription());
        analysisDefinition.setAuthorName(wsAnalysisDefinition.getAuthorName());
        analysisDefinition.setMarmotScript(wsAnalysisDefinition.getMarmotScript());
        analysisDefinition.setReportRunMarmotScript(wsAnalysisDefinition.getReportRunMarmotScript());
        analysisDefinition.setAccountVisible(wsAnalysisDefinition.isAccountVisible());
        analysisDefinition.setFolder(wsAnalysisDefinition.getFolder());
        analysisDefinition.setDateCreated(wsAnalysisDefinition.getDateCreated());
        analysisDefinition.setDateUpdated(wsAnalysisDefinition.getDateUpdated());
        analysisDefinition.setReportType(wsAnalysisDefinition.getReportType());
        analysisDefinition.setReportStructure(wsAnalysisDefinition.createStructure());
        analysisDefinition.setAddedItems(wsAnalysisDefinition.getAddedItems());
        analysisDefinition.setFilterDefinitions(FilterDefinitionConverter.fromFilters(wsAnalysisDefinition.getFilterDefinitions()));
        analysisDefinition.setAnalysisID(wsAnalysisDefinition.getAnalysisID());
        analysisDefinition.setTitle(wsAnalysisDefinition.getName());
        analysisDefinition.setAnalysisPolicy(wsAnalysisDefinition.getPolicy());
        analysisDefinition.setDataFeedID(wsAnalysisDefinition.getDataFeedID());
        analysisDefinition.setPubliclyVisible(wsAnalysisDefinition.isPubliclyVisible());
        analysisDefinition.setMarketplaceVisible(wsAnalysisDefinition.isMarketplaceVisible());
        analysisDefinition.setTemporaryReport(wsAnalysisDefinition.isTemporaryReport());
        analysisDefinition.setSolutionVisible(wsAnalysisDefinition.isSolutionVisible());
        analysisDefinition.setVisibleAtFeedLevel(wsAnalysisDefinition.isVisibleAtFeedLevel());
        return analysisDefinition;
    }
}
