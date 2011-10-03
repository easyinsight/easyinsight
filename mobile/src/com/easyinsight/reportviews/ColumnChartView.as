/**
 * Created by IntelliJ IDEA.
 * User: jamesboe
 * Date: 6/13/11
 * Time: 2:57 PM
 * To change this template use File | Settings | File Templates.
 */
package com.easyinsight.reportviews {
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.AnalysisMeasure;
import com.easyinsight.analysis.ChartDefinition;
import com.easyinsight.analysis.SortFunctionFactory;
import com.easyinsight.analysis.charts.xaxisbased.column.ColumnChartDefinition;

import mx.charts.CategoryAxis;
import mx.charts.ChartItem;

import mx.charts.ColumnChart;
import mx.charts.HitData;
import mx.charts.LinearAxis;
import mx.charts.chartClasses.CartesianChart;
import mx.charts.chartClasses.CartesianTransform;

import mx.charts.chartClasses.ChartBase;
import mx.charts.chartClasses.DataTransform;
import mx.charts.chartClasses.Series;
import mx.charts.series.ColumnSeries;
import mx.charts.series.items.ColumnSeriesItem;

import mx.collections.ArrayCollection;

import spark.collections.Sort;

public class ColumnChartView extends CartesianChartView {
    public function ColumnChartView() {
    }

    override protected function getDimensionItem():AnalysisItem {
        return ColumnChartDefinition(chartDef).xaxis;
    }

    override protected function getMeasures():ArrayCollection {
        return ColumnChartDefinition(chartDef).measures;
    }

    override protected function createChartObject():ChartBase {
        return new ColumnChart();
    }

    override protected function useChartColor():Boolean {
        return ColumnChartDefinition(chartDef).useChartColor;
    }

    override protected function getChartColor():uint {
        return ColumnChartDefinition(chartDef).chartColor;
    }

    override protected function getColorScheme():String {
        return ColumnChartDefinition(chartDef).colorScheme;
    }

    override protected function sortData(dataSet:ArrayCollection):void {
        var columnChartDef:ColumnChartDefinition = chartDef as ColumnChartDefinition;
        var firstMeasure:AnalysisMeasure = columnChartDef.measures.getItemAt(0) as AnalysisMeasure;
        if (columnChartDef.columnSort != ChartDefinition.SORT_UNSORTED) {
            var sort:Sort = new Sort();
            if (columnChartDef.columnSort == ChartDefinition.SORT_X_ASCENDING) {
                sort.compareFunction = SortFunctionFactory.createSortFunction(getDimensionItem(), false);
            } else if (columnChartDef.columnSort == ChartDefinition.SORT_X_DESCENDING) {
                sort.compareFunction = SortFunctionFactory.createSortFunction(getDimensionItem(), true);
            } else if (columnChartDef.columnSort == ChartDefinition.SORT_Y_ASCENDING) {
                sort.compareFunction = SortFunctionFactory.createSortFunction(firstMeasure, false);
            } else if (columnChartDef.columnSort == ChartDefinition.SORT_Y_DESCENDING) {
                sort.compareFunction = SortFunctionFactory.createSortFunction(firstMeasure, true);
            }
            dataSet.sort = sort;
            dataSet.refresh();
        }
    }

    override protected function formatDataTip(hd:HitData):String {
        var dt:String = "";
        var columnSeriesItem:ColumnSeriesItem = hd.chartItem as ColumnSeriesItem;
        var series:Series = columnSeriesItem.element as Series;
        var dataTransform:DataTransform = series.dataTransform;
        var n:String = series.displayName;
        if (n != null && n.length > 0)
            dt += "<b>" + n + "</b><BR/>";

        var xName:String = dataTransform.getAxis(CartesianTransform.HORIZONTAL_AXIS).displayName;
        if (xName != "")
            dt += "<i>" + xName + ":</i> ";
        dt += dataTransform.getAxis(CartesianTransform.HORIZONTAL_AXIS).formatForScreen(ColumnSeriesItem(hd.chartItem).xValue) + "\n";

        var yName:String = dataTransform.getAxis(CartesianTransform.VERTICAL_AXIS).displayName;

        if (yName != "")
            dt += "<i>" + yName + ":</i> ";
        dt += measureFormatter.format(ColumnSeriesItem(hd.chartItem).yValue) + "\n";

        return dt;
    }

    override protected function assignDimensionAxis(axis:CategoryAxis, chart:ChartBase):void {
        CartesianChart(chart).horizontalAxis = axis;
    }

    override protected function assignMeasureAxis(axis:LinearAxis, chart:ChartBase):void {
        CartesianChart(chart).verticalAxis = axis;
    }

    override protected function createSeries(measure:AnalysisMeasure, dataSet:ArrayCollection, xField:String, measureNumber:int):Series {
        var columnSeries:ColumnSeries = new ColumnSeries();
        columnSeries.xField = xField;
        columnSeries.yField = measure.qualifiedName();
        columnSeries.labelFunction = function (element:ChartItem, series:Series):String {
            var columnSeriesItem:ColumnSeriesItem = element as ColumnSeriesItem;
            return measure.getFormatter().format(columnSeriesItem.xNumber);
        };
        columnSeries.dataProvider = dataSet;
        columnSeries.dataFunction = function(series:Series, item:Object, fieldName:String):Object {
            if (fieldName == 'yValue')
                return(item[measure.qualifiedName()].toNumber());
            else if (fieldName == "xValue")
                return(item[xField].toString());
            else
                return null;
        };
        columnSeries.displayName = measure.display;
        return columnSeries;
    }
}
}
