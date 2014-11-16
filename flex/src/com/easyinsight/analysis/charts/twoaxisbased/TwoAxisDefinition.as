package com.easyinsight.analysis.charts.twoaxisbased {
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.AnalysisItemTypes;
import com.easyinsight.analysis.AnalysisMeasure;
import com.easyinsight.analysis.ChartDefinition;

import mx.collections.ArrayCollection;
[Bindable]
[RemoteClass(alias="com.easyinsight.analysis.definitions.WSTwoAxisDefinition")]
public class TwoAxisDefinition extends ChartDefinition{
    public var measure:AnalysisItem;
    public var xaxis:AnalysisItem;
    public var yaxis:AnalysisItem;
    public var measures:ArrayCollection;
    public var eventPoint:AnalysisItem;
    public var eventPointLabel:AnalysisItem;
    public var eventPointLineColor:uint;
    public var goalLineColor:uint;
    public var goal:AnalysisItem;
    public var goalDate:AnalysisItem;
    public var goalDateLevel:int;

    public var multiMeasure:Boolean = false;

    public var relativeScale:Boolean;

    public var form:String = "segment";
    public var baseAtZero:String = "true";
    public var interpolateValues:String = "false";

    public function TwoAxisDefinition() {
        super();
    }

    override public function populate(fields:ArrayCollection):void {
        var measures:ArrayCollection = findItems(fields, AnalysisItemTypes.MEASURE);
        if (measures.length > 0) {
            measure = measures.getItemAt(0) as AnalysisItem;
        }
        var dimensions:ArrayCollection = findItems(fields, AnalysisItemTypes.DIMENSION);
        if (dimensions.length > 0) {
            yaxis = dimensions.getItemAt(0) as AnalysisItem;
            if (dimensions.length > 1) {
                xaxis = dimensions.getItemAt(1) as AnalysisItem;
            }
        }
    }

    public function populateData(dataSet:ArrayCollection, seriesData:Object, uniques:ArrayCollection):ArrayCollection {
        if (!multiMeasure) {
            return populateGroupings(dataSet, seriesData, uniques);
        } else {
            return populateMeasures(dataSet, seriesData, uniques);
        }
    }

    private function populateGroupings(dataSet:ArrayCollection, seriesData:Object, uniques:ArrayCollection):ArrayCollection {
        var masterData:Object = new Object();
        for (var i:int = 0; i < dataSet.length; i++) {
            var object:Object = dataSet.getItemAt(i);
            if (object[xaxis.qualifiedName()] == null ||
                    object[xaxis.qualifiedName()] == "") {
                continue;
            }
            var dimensionValue:String = object[yaxis.qualifiedName()];
            if (dimensionValue == null || dimensionValue == "") {
                dimensionValue = "[ No Value ]";
            }
            var xAxis:String = object[xaxis.qualifiedName()].toString();
            var dataPoint:Object = masterData[xAxis];
            if (dataPoint == null) {
                dataPoint = new Object();
                masterData[xAxis] = dataPoint;
            }
            dataPoint[xaxis.qualifiedName()] = object[xaxis.qualifiedName()];
            dataPoint[dimensionValue] = object[measure.qualifiedName()];
            dataPoint[yaxis.qualifiedName()] = object[yaxis.qualifiedName()];
            var newSeriesData:ArrayCollection = seriesData[dimensionValue];
            if (newSeriesData == null) {
                newSeriesData = new ArrayCollection();
                seriesData[dimensionValue] = newSeriesData;
            }
            var newObject:Object = new Object();
            newObject[xaxis.qualifiedName()] = object[xaxis.qualifiedName()];
            newObject[dimensionValue] = object[measure.qualifiedName()];
            newSeriesData.addItem(newObject);
            if (!uniques.contains(dimensionValue)) {
                uniques.addItem(dimensionValue);
            }
        }
        var points:ArrayCollection = new ArrayCollection();
        for each (var obj:Object in masterData) {
            points.addItem(obj);
        }
        return points;
    }

    private function populateMeasures(dataSet:ArrayCollection, seriesData:Object, uniques:ArrayCollection):ArrayCollection {
        var masterData:Object = new Object();
        for each (var nameMeasure:AnalysisMeasure in measures) {
            uniques.addItem(nameMeasure.display);
            seriesData[nameMeasure.display] = new ArrayCollection();
        }
        for (var i:int = 0; i < dataSet.length; i++) {
            var object:Object = dataSet.getItemAt(i);
            if (object[xaxis.qualifiedName()] == null ||
                    object[xaxis.qualifiedName()] == "") {
                continue;
            }
            for each (var measure:AnalysisMeasure in measures) {
                var xAxis:String = object[xaxis.qualifiedName()].toString();
                var dataPoint:Object = masterData[xAxis];
                if (dataPoint == null) {
                    dataPoint = new Object();
                    masterData[xAxis] = dataPoint;
                }
                dataPoint[xaxis.qualifiedName()] = object[xaxis.qualifiedName()];
                dataPoint[measure.display] = object[measure.qualifiedName()];
                //dataPoint[yaxis.qualifiedName()] = object[yaxis.qualifiedName()];
                var newObject:Object = new Object();

                newObject[xaxis.qualifiedName()] = object[xaxis.qualifiedName()];
                newObject[measure.display] = object[measure.qualifiedName()];
                seriesData[measure.display].addItem(newObject);
            }            
        }
        var points:ArrayCollection = new ArrayCollection();
        for each (var obj:Object in masterData) {
            points.addItem(obj);
        }
        return points;
    }


    override public function getFields():ArrayCollection {
        var fields:ArrayCollection = new ArrayCollection([ yaxis, xaxis, measure]);
        if (measures != null) {
            for each (var measure:AnalysisItem in measures) {
                fields.addItem(measure);
            }
        }
        return fields;
    }
}
}