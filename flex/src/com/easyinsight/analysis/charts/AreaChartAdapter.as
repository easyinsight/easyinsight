package com.easyinsight.analysis.charts
{
	import com.easyinsight.analysis.AnalysisDateDimension;
	import com.easyinsight.analysis.AnalysisDimension;
	import com.easyinsight.analysis.AnalysisItem;
	import com.easyinsight.analysis.AnalysisItemTypes;
	import com.easyinsight.analysis.AnalysisMeasure;
	
	import mx.charts.AreaChart;
	import mx.charts.CategoryAxis;
	import mx.charts.DateTimeAxis;
	import mx.charts.Legend;
	import mx.charts.LineChart;
	import mx.charts.chartClasses.IAxis;
	import mx.charts.series.AreaSeries;
	import mx.charts.series.LineSeries;
	import mx.collections.ArrayCollection;
	
	public class AreaChartAdapter extends ChartAdapter
	{
		[Bindable]
		private var chartData:ArrayCollection;
		
		private var areaChart:AreaChart;
		
		private var legend:Legend;
		
		public function AreaChartAdapter()
		{
			super();
			this.percentHeight = 100;
			this.percentWidth = 100;
		}
		
		override public function get chartType():int {
			return ChartTypes.AREA_2D;
		}
		
		override protected function createChildren():void {
			super.createChildren();
			if (areaChart == null) {
				areaChart = new AreaChart();
				areaChart.percentHeight = 100;
				areaChart.percentWidth = 100;
				//areaChart.dataProvider = chartData;
				areaChart.selectionMode = "multiple";				
			}
			addChild(areaChart);
			if (legend == null) {
				legend = new Legend();
				legend.direction = "vertical";
				legend.percentHeight = 100;				
			}
			addChild(legend);
		}
		
		override public function dataChange(dataSet:ArrayCollection, dimensions:Array, measures:Array):void {
			
			this.chartData = dataSet;
			
			if (measures.length >= 2 || dimensions.length == 2) {
				
				removeChild(areaChart);
				
				areaChart = new AreaChart();
				areaChart.percentHeight = 100;
				areaChart.percentWidth = 100;
				areaChart.dataProvider = chartData;
				areaChart.selectionMode = "multiple";		
			
				var xAxisDimension:AnalysisItem;
				if (measures.length >= 2) {
					xAxisDimension = dimensions[0] as AnalysisItem;
				} else {
					xAxisDimension = dimensions[1] as AnalysisItem;
				}
				
				var xAxis:IAxis;
				
				if (xAxisDimension.hasType(AnalysisItemTypes.DATE)) {
					var dateDimension:AnalysisDateDimension = xAxisDimension as AnalysisDateDimension;
					var dateAxis:DateTimeAxis = new DateTimeAxis();
					switch (dateDimension.dateLevel) {
						case AnalysisItemTypes.YEAR_LEVEL:
							dateAxis.dataUnits = "years";
							break;
						case AnalysisItemTypes.MONTH_LEVEL:
							dateAxis.dataUnits = "months";
							break;
						case AnalysisItemTypes.DAY_LEVEL:
							dateAxis.dataUnits = "days";
							break;
                        case AnalysisItemTypes.HOUR_LEVEL:
                            dateAxis.dataUnits = "hours";
                            break;
                        case AnalysisItemTypes.MINUTE_LEVEL:
                            dateAxis.dataUnits = "minutes";
                            break;
					}					
					dateAxis.displayName = dateDimension.display;
					xAxis = dateAxis;
				} else {
					var categoryAxis:CategoryAxis = new CategoryAxis();
					categoryAxis.categoryField = xAxisDimension.key.createString();
					categoryAxis.displayName = xAxisDimension.display;
					categoryAxis.dataProvider = chartData;
					xAxis = categoryAxis;
				}
				
				/*var xAxisRenderer:AxisRenderer = new AxisRenderer();
		        xAxisRenderer.axis = xAxis;
		        xAxisRenderer.setStyle("color", "#FFFFFF");
		        xAxisRenderer.placement = "bottom";*/
				
				areaChart.horizontalAxis = xAxis;
				
				//areaChart.horizontalAxisRenderers = [ xAxisRenderer ];
				
				/*var xAxisRenderer:AxisRenderer = new AxisRenderer();
				xAxisRenderer.setStyle("color", "#FFFFFF");
				xAxisRenderer.axis = xAxis;
				areaChart.horizontalAxisRenderers = [ xAxisRenderer ];*/
				
				/*var yCategoryAxis:CategoryAxis = new CategoryAxis();
				areaChart.verticalAxis = yCategoryAxis;									
				
				var yAxisRenderer:AxisRenderer = new AxisRenderer();
				yAxisRenderer.setStyle("color", "#FFFFFF");
				yAxisRenderer.axis = yCategoryAxis;  			
				
				areaChart.horizontalAxis = xAxis;*/
				
				var mySeries:Array = new Array();
				
				if (measures.length > 1) {
					// we'll render multiple measures
					var measureSeries:Array = [];
					
					for each (var measureItem:AnalysisItem in measures) {
						var areaSeries:LineSeries = new LineSeries();
						areaSeries.yField = measureItem.key.createString();
						areaSeries.xField = xAxisDimension.key.createString();
						//areaSeries.setStyle("form", "curve");
						areaSeries.displayName = measureItem.display;					
						measureSeries.push(areaSeries);
					}
					
					areaChart.series = measureSeries;
					var measureCategoryAxis:CategoryAxis = new CategoryAxis();
					 
					areaChart.verticalAxis = measureCategoryAxis;
					
				} else {
					var analysisMeasure:AnalysisMeasure = measures[0];
					var dimension:AnalysisDimension = dimensions[0];
					var uniques:ArrayCollection = new ArrayCollection();
					
					var allItems:ArrayCollection = new ArrayCollection();					
					
					var seriesData:Object = new Object();
					for (var i:int = 0; i < dataSet.length; i++) {
						var object:Object = dataSet.getItemAt(i);
						var dimensionValue:String = object[dimensions[0].key.createString()];
						var newSeriesData:ArrayCollection = seriesData[dimensionValue];
						if (newSeriesData == null) {
							newSeriesData = new ArrayCollection();
							seriesData[dimensionValue] = newSeriesData;
						}
						var newObject:Object = new Object();
						newObject[dimensions[1].key.createString()] = object[dimensions[1].key.createString()];
						newObject[dimensionValue] = object[measures[0].key.createString()];
						newSeriesData.addItem(newObject);
						//allItems.addItem(newObject);
						if (!uniques.contains(dimensionValue)) {
							uniques.addItem(dimensionValue);
						}
					}
					for (i = 0; i < uniques.length; i++) {
						var key:String = uniques.getItemAt(i) as String;
						var uniqueLineSeries:AreaSeries = new AreaSeries();
						uniqueLineSeries.xField = dimensions[1].key.createString();
						uniqueLineSeries.yField = key;
						uniqueLineSeries.displayName = key;
						uniqueLineSeries.dataProvider = seriesData[key];
						//uniqueLineSeries.labelFunction = renderChartLabel; 
						mySeries.push(uniqueLineSeries);	
					}
					
					
					/* var categoryAxis:CategoryAxis = new CategoryAxis();
					var yAxisDimension:AnalysisItem = dimensions[1] as AnalysisItem;
					categoryAxis.categoryField = yAxisDimension.qualifiedName();
					areaChart.verticalAxis = categoryAxis;*/
					//areaChart.dataProvider = seriesData[uniques.getItemAt(0)] as ArrayCollection;
				}
				areaChart.series = mySeries;
				//areaChart.dataProvider = this.chartData;
				
				legend.dataProvider = areaChart;
				legend.direction = "vertical";
				addChildAt(areaChart, 0);
			}
		}

		override public function getMaxMeasures():int {
			return 2;
		}
		
		override public function getMaxDimensions():int {
			return 2;
		}
	}
}