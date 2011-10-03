/**
 * Created by IntelliJ IDEA.
 * User: jamesboe
 * Date: 8/30/11
 * Time: 8:47 AM
 * To change this template use File | Settings | File Templates.
 */
package com.easyinsight.reportviews {
import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.EmbeddedCrosstabDataResults;
import com.easyinsight.analysis.EmbeddedDataResults;
import com.easyinsight.analysis.Value;
import com.easyinsight.analysis.verticallist.EmbeddedVerticalDataResults;
import com.easyinsight.framework.InsightRequestMetadata;
import com.easyinsight.solutions.InsightDescriptor;

import flash.events.EventDispatcher;

import mx.collections.ArrayCollection;

import mx.collections.ArrayCollection;
import mx.core.UIComponent;

import mx.rpc.events.ResultEvent;
import mx.rpc.remoting.RemoteObject;

public class ReportComponent extends EventDispatcher {

    private var report:InsightDescriptor;
    private var dataService:RemoteObject;
    private var reportView:IReportView;
    private var _preserveValues:Boolean;


    private var vertService:Boolean;
    private var crosstabService:Boolean;

    private var parent:UIComponent;

    private var _actualReport:AnalysisDefinition;

    public function ReportComponent(insightDescriptor:InsightDescriptor, parent:UIComponent) {
        this.report = insightDescriptor;
        this.parent = parent;
        dataService = new RemoteObject();
        dataService.destination = "data";
        dataService.endpoint = "https://www.easy-insight.com/app/messagebroker/amfsecure";
        dataService.getEmbeddedResults.addEventListener(ResultEvent.RESULT, gotResults);
        dataService.getEmbeddedVerticalDataResults.addEventListener(ResultEvent.RESULT, gotVerticalResults);
        dataService.getEmbeddedCrosstabResults.addEventListener(ResultEvent.RESULT, gotCrosstabResults);
        var type:int = insightDescriptor.reportType;
        var reportView:IReportView;
        if (type == AnalysisDefinition.LIST) {
            reportView = new ListReportView();
        } else if (type == AnalysisDefinition.PIE || type == AnalysisDefinition.PIE3D) {
            reportView = new PieChartView();
        } else if (type == AnalysisDefinition.COLUMN || type == AnalysisDefinition.COLUMN3D) {
            reportView = new ColumnChartView();
        } else if (type == AnalysisDefinition.BAR || type == AnalysisDefinition.BAR3D) {
            reportView = new BarChartView();
        } else if (type == AnalysisDefinition.PLOT) {
            reportView = new PlotChartView();
        } else if (type == AnalysisDefinition.LINE) {
            reportView = new LineChartView();
        } else if (type == AnalysisDefinition.AREA) {
            reportView = new AreaChartView();
        } else if (type == AnalysisDefinition.BUBBLE) {
            reportView = new BubbleChartView();
        } else if (type == AnalysisDefinition.TREE) {
        } else if (type == AnalysisDefinition.VERTICAL_LIST) {
            reportView = new VerticalListReportView();
        } else if (type == AnalysisDefinition.COMBINED_VERTICAL_LIST) {
            reportView = new CombinedVerticalListReportView();
            vertService = true;
        } else if (type == AnalysisDefinition.GAUGE) {
            reportView = new GaugeView();
        } else if (type == AnalysisDefinition.CROSSTAB) {
            reportView = new CrosstabView();
            crosstabService = true;
        } else {

        }
        _preserveValues = reportView.preserveValues();
        this.reportView = reportView;
    }

    public function get actualReport():AnalysisDefinition {
        return _actualReport;
    }

    public function getReportView():IReportView {
        return reportView;
    }

    public function retrieveData(filters:ArrayCollection):void {
        var requestMetadata:InsightRequestMetadata = new InsightRequestMetadata();
        requestMetadata.utcOffset = new Date().getTimezoneOffset();
        if (vertService) {
            // dataRemoteSource.getEmbeddedVerticalDataResults.send(reportID, dataSourceID, filters, metadata);
            dataService.getEmbeddedVerticalDataResults.send(report.id, report.dataFeedID, filters, requestMetadata);
        } else if (crosstabService) {
            dataService.getEmbeddedCrosstabResults.send(report.id,  report.dataFeedID, filters, requestMetadata);
        } else {
            dataService.getEmbeddedResults.send(report.id, report.dataFeedID, filters, requestMetadata, new ArrayCollection());
        }
    }

    private function gotCrosstabResults(event:ResultEvent):void {
        var results:EmbeddedCrosstabDataResults = dataService.getEmbeddedCrosstabResults.lastResult as EmbeddedCrosstabDataResults;
        var props:Object = new Object();
        _actualReport = results.definition;
        props["columnCount"] = results.columnCount;
        reportView.renderReport(results.dataSet, results.definition, props);
        dispatchEvent(new ReportComponentEvent(ReportComponentEvent.GOT_DATA, results.definition,results.dataSet));
    }

    private function gotResults(event:ResultEvent):void {
        var results:EmbeddedDataResults = dataService.getEmbeddedResults.lastResult as EmbeddedDataResults;
        _actualReport = results.definition;
        var data:ArrayCollection = translateResults(results);
        reportView.renderReport(data, results.definition, results.additionalProperties);
        dispatchEvent(new ReportComponentEvent(ReportComponentEvent.GOT_DATA, results.definition, data));
    }

    private function gotVerticalResults(event:ResultEvent):void {
        var verticalResults:EmbeddedVerticalDataResults = dataService.getEmbeddedVerticalDataResults.lastResult as EmbeddedVerticalDataResults;
        var results:ArrayCollection = new ArrayCollection();
        for (var i:int = 0; i < verticalResults.list.length; i++) {
            var dataResults:EmbeddedDataResults = verticalResults.list.getItemAt(i) as EmbeddedDataResults;
            results.addItem(translateResults(dataResults));
        }
        _actualReport = verticalResults.report;
        reportView.renderReport(results, verticalResults.report, null);
        dispatchEvent(new ReportComponentEvent(ReportComponentEvent.GOT_DATA, verticalResults.report, results));
    }

    private function translateResults(listData:EmbeddedDataResults):ArrayCollection {
        var headers:ArrayCollection = new ArrayCollection(listData.headers);
        var rows:ArrayCollection = new ArrayCollection(listData.rows);
        var data:ArrayCollection = new ArrayCollection();
        for (var i:int = 0; i < rows.length; i++) {
            var row:Object = rows.getItemAt(i);
            var values:Array = row.values as Array;
            var endObject:Object = new Object();
            for (var j:int = 0; j < headers.length; j++) {
                var headerDimension:AnalysisItem = headers[j] as AnalysisItem;
                var value:Value = values[j];
                var key:String = headerDimension.qualifiedName();
                if (_preserveValues) {
                    endObject[key] = value;
                } else {
                    endObject[key] = value.getValue();
                }
            }
            data.addItem(endObject);
        }
        return data;
    }
}
}
