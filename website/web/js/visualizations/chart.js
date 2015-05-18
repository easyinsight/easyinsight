Chart = {

    getTree: function (target, params, showLabels, styleProps, filters, drillthroughKey, dashboardID) {
        return function (data) {
            Utils.noDataD3(data["values"], function () {
                nv.addGraph({
                    generate: function () {
                        var chart = nv.models.indentedTree()
                            .tableClass('table table-bordered table-hover table-condensed') //for bootstrap styling
                            .columns(data["columns"]);
                        Chart.createClasses(params, target);

                        d3.select("#" + target + " .reportArea").datum(data["values"]).call(chart);

                        chart.dispatch.on("elementClick", function (e) {
                            var dt = e.dt;
                            var f = {
                                "reportID": dt["data-reportid"],
                                "drillthroughID": dt["data-drillthroughid"],
                                "source": dt["data-source"],
                                "drillthroughKey": drillthroughKey,
                                "filters": filters,
                                "drillthrough_values": {}
                            };
                            if (dashboardID != -1) {
                                f["dashboardID"] = dashboardID;
                            }
                            f["drillthrough_values"] = _.inject(dt, function (m, e, i, l) {

                                    if (i.match(/^dtfield/))
                                        m[i.replace(/^dtfield/, "")] = decodeURI(e);
                                    return m;
                                },
                                {});
                            drillThrough(f);
                        });
                    }
                });

            }, Chart.cleanup, target);
        };
    },

    createClasses: function (properties, target) {
        var curStyleSheet = Chart.findDynamicStyleSheet();
        if (curStyleSheet == null) {
            var s = document.createElement("style");
            s.title = "customDataGridValues";
            $("head").append(s);
            curStyleSheet = Chart.findDynamicStyleSheet();
        }

        curStyleSheet.insertRule("#" + target + " table tr.even {background-color:" + Color.numToStr(properties["rowColor2"]) + ";}", 0);

        // rowColor1
        curStyleSheet.insertRule("#" + target + " table tr.odd {background-color:" + Color.numToStr(properties["rowColor1"]) + ";}", 0);
        // rowColor2
        curStyleSheet.insertRule("#" + target + " table tr.even {background-color:" + Color.numToStr(properties["rowColor2"]) + ";}", 0);
        curStyleSheet.insertRule("#" + target + " table td {padding: 3px}", 0);


        var gradientString = "background-color: " + Color.numToStr(properties["headerColor1"]) + ";background-image: linear-gradient(bottom, " + Color.numToStr(properties["headerColor1"]) + " 30%, " + Color.numToStr(properties["headerColor2"]) + " 70%);" +
            "background-image: -o-linear-gradient(bottom, " + Color.numToStr(properties["headerColor1"]) + " 30%, " + Color.numToStr(properties["headerColor2"]) + " 70%);" +
            "background-image: -moz-linear-gradient(bottom, " + Color.numToStr(properties["headerColor1"]) + " 30%, " + Color.numToStr(properties["headerColor2"]) + " 70%);" +
            "background-image: -webkit-linear-gradient(bottom, " + Color.numToStr(properties["headerColor1"]) + " 30%, " + Color.numToStr(properties["headerColor2"]) + " 70%);" +
            "background-image: -ms-linear-gradient(bottom, " + Color.numToStr(properties["headerColor1"]) + " 30%, " + Color.numToStr(properties["headerColor2"]) + " 70%);" +
            "background-image: -webkit-gradient(linear,left bottom,left top,color-stop(0.3, " + Color.numToStr(properties["headerColor1"]) + "),color-stop(0.7, " + Color.numToStr(properties["headerColor2"]) + "));";
        curStyleSheet.insertRule("#" + target + " table thead tr {" + gradientString + "color:" + Color.numToStr(properties["headerTextColor"]) + ";}", 0);
    },
    findDynamicStyleSheet: function () {
        var i = 0;
        var curStyleSheet = null;
        for (i = 0; i < document.styleSheets.length; i++) {
            if (document.styleSheets[i].title == "customDataGridValues") {
                curStyleSheet = document.styleSheets[i];
            }
        }
        return curStyleSheet;
    },

    getD3StackedColumnChart: function (target, params, showLabels, styleProps, filters, drillthroughKey, dashboardID) {
        return function (data) {
            Utils.noDataD3(data["values"], function () {
                nv.addGraph({
                    generate: function () {
                        var height = Chart.chartHeight(target, styleProps);
                        if (Chart.ieTest()) {
                            $("#d3Div" + target).height(height);
                        }
                        var s1 = data["values"];
                        var maxLabelSize = d3.max(s1, function (d) {
                            return d3.max(d.values, function (e) {
                                return e.x.length
                            });
                        });
                        var rWidth = $("#d3Div" + target).width() - 80 - 40;
                        var maxTextSize = maxLabelSize * 7 * s1[0].values.length;

                        var factorWidth = maxLabelSize * 4 * s1[0].values.length;
                        var useRotate = factorWidth > rWidth;
                        var needStagger = maxTextSize > rWidth;
                        var charLimit = useRotate ? 15 : 0;

                        var chart = nv.models.multiBarChart()
                            .height(height)
                            .reduceXTicks(false)
                            .showControls(false)
                            .stacked(true)
                            .staggerLabels(!useRotate && needStagger)
                            .transitionDuration(350)  //how fast do you want the lines to transition?
                            .showYAxis(true)        //Show the y-axis
                            .showXAxis(true)        //Show the x-axis
                            .margin({top: 20, right: 40, bottom: useRotate ? 130 : (needStagger ? 60 : 55), left: 80});

                        if (data["valueLabel"]) {
                            chart.showValues(true);
                            if (data["yFormat"]) {
                                chart.valueFormat(Chart.createFormat(data["yFormat"]));
                            }
                        }

                        /*var customWidth = styleProps != null ? styleProps["preferredWidth"] : -1;
                         if (customWidth > -1) {
                         chart.width(customWidth);
                         }*/

                        if (data["drillthrough"]) {
                            var dtOptions = $.extend(true, {}, data["drillthrough"]);
                            chart.multibar.dispatch.on("elementClick", function (e) {
                                var drillthrough = data["drillthrough"];
                                var f = {
                                    "reportID": dtOptions["reportID"],
                                    "drillthroughID": dtOptions["id"],
                                    "embedded": dtOptions["embedded"],
                                    "source": dtOptions["source"],
                                    "drillthroughKey": drillthroughKey,
                                    "filters": filters,
                                    "drillthrough_values": {}
                                };
                                f["drillthrough_values"][dtOptions["xaxis"]] = e.point.x;
                                f["drillthrough_values"][drillthrough["stack"]] = e.series.key;
                                if (dashboardID != -1) {
                                    f["dashboardID"] = dashboardID;
                                }
                                drillThrough(f);
                            });
                        }

                        Chart.assignAxisLabels(chart.xAxis, chart.yAxis, data, 50, -70, charLimit);
                        Chart.assignAxisMinMaxValues(chart, data, true);


                        if (useRotate) {
                            chart.xAxis.rotateLabels(-45);
                        }

                        d3.select('#d3Div' + target)
                            .attr('height', height)
                            .datum(s1)
                            .call(chart);

                        d3.select('#d3Div' + target + " .stackLabel").style('fill', '#000000');

                        Chart.canvasHeights(target, styleProps);


                        //nv.utils.windowResize(function() { chart.update() });
                        return chart;
                    }
                });

            }, Chart.cleanup, target);
        };
    },


    getD3StackedBarChart: function (target, params, showLabels, styleProps, filters, drillthroughKey, iframedInUI, dashboardID) {
        return function (data) {
            Utils.noDataD3(data["values"], function () {
                nv.addGraph({
                    generate: function () {

                        var height = Chart.chartHeightWithIFrame(target, styleProps, iframedInUI);

                        if (Chart.ieTest()) {
                            $("#d3Div" + target).height(height);
                        }

                        var s1 = data["values"];

                        var maxLen = 0;
                        for (var l = 0; l < s1.length; l++) {
                            var lenVals = s1[l].values;
                            for (var k = 0; k < lenVals.length; k++) {
                                var x = lenVals[k].x;
                                if (x.length > maxLen) {
                                    maxLen = x.length;
                                }
                            }
                        }

                        var rWidth = $("#d3Div" + target).width();
                        var maxLeft = 150;
                        var maxChars = 15;
                        if (rWidth < 300) {
                            maxLeft = 70;
                            maxChars = maxLeft / 16;
                        }

                        var leftNeeded = maxLen * 16;
                        if (leftNeeded > maxLeft) {
                            leftNeeded = maxLeft;
                        } else if (leftNeeded < 50) {
                            leftNeeded = 50;
                        }

                        var chart = nv.models.multiBarHorizontalChart()
                            .x(function (d) {
                                if (d.x.length > maxChars) {
                                    return d.x.substring(0, maxChars) + "...";
                                } else {
                                    return d.x;
                                }
                            })
                            .height(height)
                            .showControls(false)
                            .stacked(true)
                            .transitionDuration(350)  //how fast do you want the lines to transition?
                            .margin({top: 20, right: 40, bottom: 60, left: leftNeeded});

                        if (data["valueLabel"]) {
                            chart.showValues(true);
                            if (data["yFormat"]) {
                                chart.valueFormat(Chart.createFormat(data["yFormat"]));
                            }
                        }

                        /*var customWidth = styleProps != null ? styleProps["preferredWidth"] : -1;
                         if (customWidth > -1) {
                         chart.width(customWidth);
                         }*/

                        var floatingY = data["floatingY"];
                        if (floatingY) {
                            var vals = s1[0].values;
                            var minX = d3.min(vals, function (d) {
                                return d.xMin;
                            });
                            if (data["dateAxis"]) {
                                chart.cumulativeDateAxis(true);
                                var maxX = data["maxY"];
                                chart.minX(function (d) {
                                    return d.xMin;
                                }).forceY([minX, maxX]);
                            } else {
                                chart.minX(function (d) {
                                    return d.xMin;
                                }).forceY([minX]);
                            }
                        }

                        if (data["drillthrough"]) {
                            var dtOptions = $.extend(true, {}, data["drillthrough"]);
                            chart.multibar.dispatch.on("elementClick", function (e) {
                                var drillthrough = data["drillthrough"];
                                var f = {
                                    "reportID": dtOptions["reportID"],
                                    "drillthroughID": dtOptions["id"],
                                    "embedded": dtOptions["embedded"],
                                    "source": dtOptions["source"],
                                    "drillthroughKey": drillthroughKey,
                                    "filters": filters,
                                    "drillthrough_values": {}
                                };
                                f["drillthrough_values"][dtOptions["xaxis"]] = e.point.x;
                                f["drillthrough_values"][drillthrough["stack"]] = e.series.key;
                                if (dashboardID != -1) {
                                    f["dashboardID"] = dashboardID;
                                }
                                drillThrough(f);
                            });
                        }

                        if (data["dateAxis"]) {
                            data["yFormat"].type = "msToDate";
                            var msFormat = {type: "measure", precision: 0, numberFormat: 4};
                            chart.valueFormat(Chart.createFormat(msFormat));
                        }

                        Chart.assignAxisLabels(chart.xAxis, chart.yAxis, data, -leftNeeded + 10, 30);
                        Chart.assignAxisMinMaxValues(chart, data, true);

                        d3.select('#d3Div' + target)
                            //.attr('width', width)
                            .attr('height', height)
                            .datum(s1)
                            .call(chart);

                        Chart.canvasHeights(target, styleProps);

                        nv.utils.windowResize(function () {
                            chart.update()
                        });


                        return chart;
                    }
                });

            }, Chart.cleanup, target);
        };
    },

    getBulletChartCallback: function (target, params, showLabels, styleProps, filters, drillthroughKey, dashboardID) {
        return function (data) {
            Utils.noDataD3(data["values"], function () {
                nv.addGraph({
                    generate: function () {
                        var s1 = data["values"][0];

                        var title = s1.title;
                        var titleLength = title.length;
                        if (titleLength > 15) {
                            title = title.substring(0, 13) + "...";
                            titleLength = title.length;
                        }
                        var leftMargin = titleLength * 10;


                        var chart = nv.models.bulletChart()
                            .margin({top: 20, right: 20, bottom: 20, left: leftMargin});

                        d3.select('#d3Div' + target)
                            .datum(s1)
                            .call(chart);

                        Chart.canvasHeights(target, styleProps);

                        //nv.utils.windowResize(function() { chart.update() });
                        return chart;
                    }
                });

            }, Chart.cleanup, target);
        };
    },

    getD3PieChartCallback:function (target, params, showLabels, styleProps, filters, drillthroughKey, dashboardID) {
        return function (data) {
            Utils.noDataD3(data["values"], function () {
                nv.addGraph({
                    generate: function () {
                        var height = Chart.chartHeight(target, styleProps);
                        if (Chart.ieTest()) {
                            $("#d3Div" + target).height(height);
                        }
                        var s1 = data["values"];

                        var colors = [];


                        for (var i = 0; i < s1.length; i++) {
                            var point = s1[i];
                            var c = point.color;
                            colors.push(point.color);
                        }

                        var labelType = data["pieLabelStyle"];
                        var pieLabelType;
                        if (labelType == "Label") {
                            pieLabelType = "key";
                        } else if (labelType == "Percentage") {
                            pieLabelType = "percent";
                        } else if (labelType == "Value with Percentage") {
                            pieLabelType = "custom";
                        } else {
                            pieLabelType = "value";
                        }

                        var showLegend = data["showLegend"];

                        var chart = nv.models.pieChart()
                            //.width(width)
                            .x(function (d) {
                                return d.label
                            })
                            .y(function (d) {
                                return d.value
                            })
                            .showLabels(true)
                            .color(colors)
                            .height(height)
                            .showLegend(showLegend)
                            .valueFormat(Chart.createFormat(data["yFormat"]))
                            .labelThreshold(0.02)
                            .labelType(pieLabelType)
                            /*.donut(true)          //Turn on Donut mode. Makes pie chart look tasty!
                             .donutRatio(0.35)*/
                            .margin({top: 20, right: 20, bottom: 20, left: 20})
                            .tooltipContent(function (key, x, e, graph) {
                                return '<h3>' + key + '</h3>' +
                                    '<p><b>' + x + '</b></p>' +
                                    '<h4><b>' + e.point.percent + '%</b> of <b>' + Chart.createFormat(data["yFormat"])(e.point.total) + '</b></h4>';
                            });
                        var customWidth = styleProps != null ? styleProps["preferredWidth"] : -1;
                        if (customWidth > -1) {
                            chart.width(customWidth);
                        }

                        if (data["donut"]) {
                            chart.donut(true).donutRatio(data["donutRatio"]);
                        }

                        if (data["drillthrough"]) {
                            var dtOptions = $.extend(true, {}, data["drillthrough"]);
                            chart.pie.dispatch.on("elementClick", function (e) {
                                var drillthrough = data["drillthrough"];
                                var f = {
                                    "reportID": dtOptions["reportID"],
                                    "drillthroughID": dtOptions["id"],
                                    "embedded": dtOptions["embedded"],
                                    "source": dtOptions["source"],
                                    "drillthroughKey": drillthroughKey,
                                    "filters": filters,
                                    "drillthrough_values": {}
                                };
                                f["drillthrough_values"][dtOptions["xaxis"]] = e.label;
                                if (dashboardID != -1) {
                                    f["dashboardID"] = dashboardID;
                                }
                                drillThrough(f);
                            });
                        }


                        d3.select('#d3Div' + target)
                            //.attr('width', width)
                            .attr('height', height)
                            .datum(s1)
                            .call(chart);

                        Chart.canvasHeights(target, styleProps);

                        //nv.utils.windowResize(function() { chart.update() });
                        return chart;
                    }
                });

            }, Chart.cleanup, target);
        };
    },

    getD3ColumnChartCallback: function (target, params, showLabels, styleProps, filters, drillthroughKey, dashboardID) {
        return function (data) {
            Utils.noDataD3(data["values"], function () {
                nv.addGraph({
                    generate: function () {
                        var height = Chart.chartHeight(target, styleProps);
                        if (Chart.ieTest()) {
                            $("#d3Div" + target).height(height);
                        }
                        var chart;

                        var s1 = data["values"];

                        var maxLabelSize = d3.max(s1, function (d) {
                            return d3.max(d.values, function (e) {
                                return e.x.length
                            });
                        });
                        var rWidth = $("#d3Div" + target).width() - 85 - 40;
                        var maxTextSize = maxLabelSize * 7 * s1[0].values.length;

                        var factorWidth = maxLabelSize * 4 * s1[0].values.length;
                        var useRotate = factorWidth > rWidth;
                        var needStagger = maxTextSize > rWidth;
                        var charLimit = useRotate ? 15 : 0;

                        if (data["oneMeasure"]) {
                            var colors = [];


                            for (var i = 0; i < s1.length; i++) {
                                var point = s1[i];
                                colors.push(point.color);
                            }
                            chart = nv.models.discreteBarChart()
                                .height(height)
                                .color(colors)
                                .staggerLabels(!useRotate && needStagger)
                                .transitionDuration(350)  //how fast do you want the lines to transition?
                                .tooltipContent(function (key, x, y, e, graph) {
                                    return '<b>' + x + '</b>' +
                                        '<p>' + y + '</p>'
                                })
                                .showYAxis(true)        //Show the y-axis
                                .showXAxis(true)        //Show the x-axis
                                .margin({
                                    top: 20,
                                    right: 40,
                                    bottom: useRotate ? 120 : (needStagger ? 60 : 60),
                                    left: 85
                                });

                            if (data["valueLabel"]) {
                                chart.showValues(true);
                                if (data["yFormat"]) {
                                    chart.valueFormat(Chart.createFormat(data["yFormat"]));
                                }
                            }
                            if (data["drillthrough"]) {
                                var dtOptions = $.extend(true, {}, data["drillthrough"]);
                                chart.discretebar.dispatch.on("elementClick", function (e) {
                                    var drillthrough = data["drillthrough"];
                                    var f = {
                                        "reportID": dtOptions["reportID"],
                                        "drillthroughID": dtOptions["id"],
                                        "embedded": dtOptions["embedded"],
                                        "source": dtOptions["source"],
                                        "drillthroughKey": drillthroughKey,
                                        "filters": filters,
                                        "drillthrough_values": {}
                                    };
                                    f["drillthrough_values"][dtOptions["xaxis"]] = e.point.x;
                                    if (dashboardID != -1) {
                                        f["dashboardID"] = dashboardID;
                                    }
                                    drillThrough(f);
                                });
                            }
                            Chart.assignAxisMinMaxValues(chart, data, false);
                        } else {
                            chart = nv.models.multiBarChart()
                                //.width(width)
                                .height(height)
                                .reduceXTicks(false)
                                .showControls(false)
                                .staggerLabels(!useRotate && needStagger)
                                .transitionDuration(350)  //how fast do you want the lines to transition?
                                .showYAxis(true)        //Show the y-axis
                                .showXAxis(true)        //Show the x-axis
                                .margin({
                                    top: 20,
                                    right: 40,
                                    bottom: useRotate ? 120 : (needStagger ? 60 : 60),
                                    left: 80
                                });
                            if (data["drillthrough"]) {
                                var dtOptions = $.extend(true, {}, data["drillthrough"]);
                                if (dtOptions["id"]) {
                                }
                                chart.multibar.dispatch.on("elementClick", function (e) {
                                    var drillthrough = data["drillthrough"];
                                    var f = {
                                        "reportID": dtOptions["reportID"],
                                        "drillthroughID": dtOptions["id"],
                                        "embedded": dtOptions["embedded"],
                                        "source": dtOptions["source"],
                                        "drillthroughKey": drillthroughKey,
                                        "filters": filters,
                                        "drillthrough_values": {}
                                    };
                                    f["drillthrough_values"][dtOptions["xaxis"]] = e.point.x;
                                    if (dashboardID != -1) {
                                        f["dashboardID"] = dashboardID;
                                    }
                                    drillThrough(f);
                                });
                            }
                            /*var customWidth = styleProps != null ? styleProps["preferredWidth"] : -1;
                             if (customWidth > -1) {
                             chart.width(customWidth);
                             }*/
                            Chart.assignAxisMinMaxValues(chart, data, true);
                        }


                        Chart.assignAxisLabels(chart.xAxis, chart.yAxis, data, 30, -70, charLimit);

                        if (useRotate) {
                            chart.xAxis.rotateLabels(-45);
                        }


                        d3.select('#d3Div' + target)
                            //.attr('width', width)
                            .attr('height', height)
                            .datum(s1)
                            .call(chart);

                        Chart.canvasHeights(target, styleProps);

                        //nv.utils.windowResize(function() { chart.update() });
                        return chart;
                    }
                });

            }, Chart.cleanup, target);
        };
    },

    getD3BarChartCallback: function (target, params, showLabels, styleProps, filters, drillthroughKey, dashboardID) {
        return function (data) {
            Utils.noDataD3(data["values"], function () {
                nv.addGraph({
                    generate: function () {
                        var s1 = data["values"];

                        var maxLen = 0;
                        for (var l = 0; l < s1.length; l++) {
                            var lenVals = s1[l].values;
                            for (var k = 0; k < lenVals.length; k++) {
                                var x = lenVals[k].x;
                                if (x.length > maxLen) {
                                    maxLen = x.length;
                                }
                            }
                        }

                        var rWidth = $("#d3Div" + target).width();
                        var maxLeft = 150;
                        var maxChars = 15;
                        if (rWidth < 480) {
                            maxLeft = 100;
                            maxChars = maxLeft / 10;
                        }

                        var leftNeeded = maxLen * 10;
                        if (leftNeeded > maxLeft) {
                            leftNeeded = maxLeft;
                        } else if (leftNeeded < 50) {
                            leftNeeded = 50;
                        }

                        var height = Chart.chartHeight(target, styleProps);

                        if (Chart.ieTest()) {
                            $("#d3Div" + target).height(height);
                        }


                        if (data["variableHeight"]) {
                            var dLen = s1[0].values.length;
                            height = dLen * 25 + 60;
                        }

                        var customWidth = -1;
                        var chart = nv.models.multiBarHorizontalChart()
                            .x(function (d) {
                                if (d.x.length > maxChars) {
                                    return d.x.substring(0, maxChars) + "...";
                                } else {
                                    return d.x;
                                }
                            })
                            .height(height)
                            .showControls(false)
                            .transitionDuration(350)  //how fast do you want the lines to transition?
                            //.margin({top: 10, right: 30, bottom: 45, left: leftNeeded});
                            .margin({top: 10, right: 30, bottom: 45, left: leftNeeded});
                        chart.multibar.barColor(function (d, i) {
                            return d.color
                        });
                        var floatingY = data["floatingY"];
                        if (floatingY) {
                            var vals = s1[0].values;
                            var minX = d3.min(vals, function (d) {
                                return d.minY;
                            });
                            chart.minX(function (d) {
                                return d.minY;
                            }).forceY([minX]);
                        }
                        if (customWidth > -1) {
                            chart.width(customWidth);
                        }
                        if (data["valueLabel"]) {
                            chart.showValues(true);
                            if (data["yFormat"]) {
                                chart.valueFormat(Chart.createFormat(data["yFormat"]));
                            }
                        }
                        if (data["drillthrough"]) {
                            var dtOptions = $.extend(true, {}, data["drillthrough"]);
                            chart.multibar.dispatch.on("elementClick", function (e) {
                                var drillthrough = data["drillthrough"];
                                var f = {
                                    "reportID": dtOptions["reportID"],
                                    "drillthroughID": dtOptions["id"],
                                    "embedded": dtOptions["embedded"],
                                    "source": dtOptions["source"],
                                    "drillthroughKey": drillthroughKey,
                                    "filters": filters,
                                    "drillthrough_values": {}
                                };

                                if (dashboardID != -1) {
                                    f["dashboardID"] = dashboardID;
                                }
                                f["drillthrough_values"][dtOptions["xaxis"]] = e.point.x;
                                if (dashboardID != -1) {
                                    f["dashboardID"] = dashboardID;
                                }
                                drillThrough(f);
                            });
                        }

                        if (data["dateAxis"]) {
                            data["yFormat"].type = "msToDate";
                            var msFormat = {type: "measure", precision: 0, numberFormat: 4};
                            chart.valueFormat(Chart.createFormat(msFormat));
                            chart.tooltipContent(function (key, x, e, graph) {
                                return '<h3>' + key + '</h3>';
                            });
                        }

                        Chart.assignAxisLabels(chart.xAxis, chart.yAxis, data, -leftNeeded + 10, 40);
                        Chart.assignAxisMinMaxValues(chart, data, true);


                        d3.select('#d3Div' + target)
                            .attr('height', height)
                            .datum(s1)
                            .call(chart);

                        Chart.canvasHeights(target, styleProps);

                        nv.utils.windowResize(function () {
                            chart.update()
                        });
                        return chart;
                    }
                });

            }, Chart.cleanup, target);
        };
    },

    canvasHeights: function (target, styleProps) {
        var h = $("#d3Div" + target).height();
        var customWidth = -1;
        var w;
        if (customWidth > -1) {
            w = customWidth;
        } else {
            w = $("#d3Div" + target).width();
        }
        $("#d3Canvas" + target).attr('height', h);
        $("#d3Canvas" + target).attr('width', w);
    },

    chartHeightWithIFrame: function (target, styleProps, iframedInUI) {
        var height;
        var customHeight = styleProps != null ? styleProps["customHeight"] : -1;
        if (customHeight > -1) {

            if (customHeight > 0) {
                height = customHeight;
            } else {
                var verticalMargin = styleProps["verticalMargin"];
                height = $(document).height() - $('#filterRow').height() - $('#reportHeader').height() - verticalMargin;
                if (height < 400) {
                    height = 400;
                }
            }

        } else {
            var raHeight = $('#' + target + 'ReportArea').height();
            if (typeof(raHeight) != "undefined" && raHeight > 150) {
                height = raHeight;
            } else {
                height = nv.utils.windowSize().height - $('#filterRow').height() - $('#reportHeader').height() - (iframedInUI ? 100 : 250);
            }
        }
        return height;
    },

    chartHeight: function (target, styleProps) {
        var height;
        var customHeight = styleProps != null ? styleProps["customHeight"] : -1;
        if (customHeight > -1) {

            if (customHeight > 0) {
                height = customHeight;
            } else {
                var verticalMargin = styleProps["verticalMargin"];
                height = $(document).height() - $('#filterRow').height() - $('#reportHeader').height() - verticalMargin;
                if (height < 400) {
                    height = 400;
                }
            }

        } else {
            var customPreferredHeight = styleProps != null ? styleProps["preferredHeight"] : -1;
            if (customPreferredHeight > -1) {
                height = customPreferredHeight;
            } else {
                var raHeight = $('#' + target + 'ReportArea').height();
                if (typeof(raHeight) != "undefined" && raHeight > 150) {
                    height = raHeight;
                } else {
                    height = nv.utils.windowSize().height - $('#filterRow').height() - $('#reportHeader').height() - 250;
                    if (height < 200) {
                        height = 200;
                    }
                }
            }
        }
        return height;
    },

    getD3ScatterCallback: function (target, params, showLabels, styleProps, filters, drillthroughKey, dashboardID) {
        return function (data) {
            Utils.noDataD3(data["values"], function () {
                nv.addGraph({
                    generate: function () {
                        var s1 = data["values"];

                        var minY = null;
                        var maxY = null;
                        var minX = null;
                        var maxX = null;
                        for (var i = 0; i < s1.length; i++) {
                            var keyVals = s1[i];
                            for (var j = 0; j < keyVals.values.length; j++) {
                                var row = keyVals.values[j];
                                if (minY == null || minY > row.y) {
                                    minY = row.y;
                                }
                                if (maxY == null || row.y > maxY) {
                                    maxY = row.y;
                                }
                                if (minX == null || minX > row.x) {
                                    minX = row.x;
                                }
                                if (maxX == null || row.x > maxX) {
                                    maxX = row.x;
                                }
                            }
                        }

                        var height = Chart.chartHeight(target, styleProps);
                        if (Chart.ieTest()) {
                            $("#d3Div" + target).height(height);
                        }

                        var chart = nv.models.scatterChart()
                            .height(height)
                            .transitionDuration(350)  //how fast do you want the lines to transition?
                            .margin({top: 20, right: 40, bottom: 60, left: 80});
                        chart.tooltipX(null);
                        chart.tooltipY(null);
                        chart.tooltip(function (key, x, y, e, c, a) {
                            return '<h3>' + key + '</h3>' + '<h3>' + a + "</h3>" + '<p>' + data["xTitle"] + ': <b>' + x + '</b></p>' + '<p>' + data["yTitle"] + ': <b>' + y + '</b></p>'
                        });

                        if (data["point"]) {
                            chart.sizeRange([200, 200]);
                            chart.sizeDomain([200, 200]);
                        } else {
                            chart.sizeRange([16, 1024]);
                        }


                        chart.scatter.onlyCircles(false);

                        if (data["showLabels"]) {
                            chart.scatter.showLabels(true);
                        }

                        if (data["drillthrough"]) {
                            var dtOptions = $.extend(true, {}, data["drillthrough"]);
                            if (dtOptions["id"]) {

                            }
                            chart.scatter.dispatch.on("elementClick", function (e) {
                                var drillthrough = data["drillthrough"];
                                var f = {
                                    "reportID": dtOptions["reportID"],
                                    "drillthroughID": dtOptions["id"],
                                    "embedded": dtOptions["embedded"],
                                    "source": dtOptions["source"],
                                    "drillthroughKey": drillthroughKey,
                                    "filters": filters,
                                    "drillthrough_values": {}
                                };
                                f["drillthrough_values"][dtOptions["xaxis"]] = e.point.a;
                                if (dashboardID != -1) {
                                    f["dashboardID"] = dashboardID;
                                }
                                drillThrough(f);
                            });
                        }

                        Chart.assignAxisLabels(chart.xAxis, chart.yAxis, data, 50, -70);
                        Chart.assignAxisMinMaxValues(chart, data, true);


                        d3.select('#d3Div' + target)
                            //.attr('width', width)
                            .attr('height', height)
                            .datum(s1)
                            .call(chart);


                        var calcYMax = chart.yAxis.scale()(maxY);
                        var calcYMin = chart.yAxis.scale()(minY);
                        var midPointY = (calcYMin - calcYMax) / 2;
                        var calcXMax = chart.xAxis.scale()(maxX);
                        var calcXMin = chart.xAxis.scale()(minX);
                        var midPointX = (calcXMax - calcXMin) / 2;

                        var targ = d3.select('#d3Div' + target + " .nv-scatterWrap");
                        targ.append("g").append("rect").attr("height", (calcYMin - calcYMax)).attr("width", 1).style("fill", "#000000").attr("x", midPointX).attr("y", 0);
                        targ.append("g").append("rect").attr("width", (calcXMax - calcXMin)).attr("height", 1).style("fill", "#000000").attr("y", midPointY).attr("x", 0);
                        //targ.append("g").append("rect").attr("width", midPointX).attr("height", midPointY).style("fill", "#AACCAA").style('fill-opacity',.1).attr("y", 0).attr("x", midPointX);

                        Chart.canvasHeights(target, styleProps);

                        //nv.utils.windowResize(function() { chart.update() });
                        return chart;
                    }
                });

            }, Chart.cleanup, target);
        };
    },

    assignAxisLabels: function (xAxis, yAxis, data, xLabelDistance, yLabelDistance, limit, yDefault) {
        xAxis.axisLabel(data["xTitle"]);
        if (typeof(data["xFormat"]) != "undefined") {
            xAxis.tickFormat(Chart.createFormat(data["xFormat"], limit));
        }
        xAxis.axisLabelDistance(xLabelDistance);
        yAxis.axisLabel(data["yTitle"]);
        if (typeof(data["yFormat"]) != "undefined") {
            yAxis.tickFormat(Chart.createFormat(data["yFormat"], limit));
        } else if (yDefault) {
            yAxis.tickFormat(Chart.createMeasureFormat());
        }
        yAxis.axisLabelDistance(yLabelDistance);
    },

    assignAxisMinMaxValues: function (chart, data, showLegend) {

        if (showLegend) {
            if (data["showLegend"]) {
                chart.showLegend(true);
            } else {
                chart.showLegend(false);
            }
        }

        var xMin = data["xMin"];
        var xMax = data["xMax"];
        if (typeof(xMin) != "undefined" && typeof(xMax) != "undefined") {
            chart.forceX([xMin, xMax]);
        }
        var yMin = data["yMin"];
        var yMax = data["yMax"];
        if (typeof(yMin) != "undefined" && typeof(yMax) != "undefined") {
            chart.forceY([yMin, yMax]);
        }
    },

    createMeasureFormat: function (formatInfo) {
        return function (d) {
            var precision = 2;
            var numberFormatter = d3.format(",." + precision + "f");
            return numberFormatter(d);
        }
    },

    createFormat: function (formatInfo, limit) {
        return function (d) {
            if (formatInfo.type == "msToDate") {
                var format = d3.time.format("%m/%d/%Y");
                return format(new Date(d));
            } else if (formatInfo.type == "measure") {
                var precision = formatInfo.precision;
                var numberFormat = formatInfo.numberFormat;
                var numberFormatter = d3.format(",." + precision + "f");
                if (numberFormat == 2) {
                    var currencySymbol = formatInfo.currencySymbol;
                    return currencySymbol + numberFormatter(d);
                } else if (numberFormat == 3) {
                    return numberFormatter(d) + "%";
                } else if (numberFormat == 4) {
                    return Chart.millisecond("m", d);
                } else if (numberFormat == 5) {
                    return Chart.millisecond("s", d);
                } else {

                }
                return numberFormatter(d);
            } else {
                if (limit > 0) {
                    return Chart.truncate(limit, d);
                }
            }
            return d;
        }
    },

    truncate: function (limit, val) {
        if (val.length > limit) {
            return val.substring(0, limit) + "...";
        } else {
            return val;
        }
    },

    ieTest: function () {
        var rv = -1;
        if (navigator.appName == 'Microsoft Internet Explorer') {
            var ua = navigator.userAgent;
            var re = new RegExp("MSIE ([0-9]{1,}[\\.0-9]{0,})");
            if (re.exec(ua) != null)
                rv = parseFloat(RegExp.$1);
        }
        else if (navigator.appName == 'Netscape') {
            var ua = navigator.userAgent;
            var re = new RegExp("Trident/.*rv:([0-9]{1,}[\\.0-9]{0,})");
            if (re.exec(ua) != null)
                rv = parseFloat(RegExp.$1);
        }
        return rv > -1;
    },

    millisecond: function (format, val, precision) {
        if (val == 0)
            return String("");
        if (typeof(precision) == "undefined") {
            precision = 0;
        }
        var result = "";
        if (format == "s")
            val = val * 1000;
        var unsigned = Math.abs(val);
        var milliseconds, seconds, minutes, hours, days;
        if (unsigned < 60000) {
            seconds = Math.floor(unsigned / 1000);
            milliseconds = (val % 1000);
            result = seconds + "s:";
            if (format == "ms")
                result = result + milliseconds + "ms";
        } else if (unsigned < (60000 * 60)) {
            minutes = Math.floor(unsigned / 60000);
            seconds = Math.floor(unsigned / 1000) % 60;
            if (precision == 0) {
                result = minutes + "m";
            } else {
                result = minutes + "m: " + seconds + "s";
            }
        } else if (unsigned < (60000 * 60 * 24)) {
            hours = Math.floor(unsigned / (60000 * 60));
            minutes = Math.floor(unsigned % 24);
            if (precision == 0) {
                result = hours + "h";
            } else {
                result = hours + "h:" + minutes + "m";
            }
        } else {
            days = Math.floor(unsigned / (60000 * 60 * 24));
            hours = Math.floor(unsigned / (60000 * 60) % 24);
            if (precision == 0) {
                result = days + "d";
            } else {
                result = days + "d:" + hours + "h";
            }
        }
        if (val < 0) {
            result = "(" + result + ")";
        }
        return String(result);
    },

    getD3LineMeasureCallback: function (target, params, showLabels, styleProps, filters, drillthroughKey, dashboardID) {
        return function (data) {
            Utils.noDataD3(data["values"], function () {
                nv.addGraph({
                    generate: function () {
                        var s1 = data["values"];


                        var height = Chart.chartHeight(target, styleProps);
                        if (Chart.ieTest()) {
                            $("#d3Div" + target).height(height);
                        }

                        var chart = nv.models.lineChart()
                            //.width(width)
                            .height(height)
                            .useInteractiveGuideline(true)  //We want nice looking tooltips and a guideline!
                            .transitionDuration(350)  //how fast do you want the lines to transition?
                            .showLegend(true)       //Show the legend, allowing users to turn on/off line series.
                            .showYAxis(true)        //Show the y-axis
                            .showXAxis(true)        //Show the x-axis
                            .margin({top: 20, right: 40, bottom: 40, left: 80});

                        Chart.assignAxisLabels(chart.xAxis, chart.yAxis, data, 40, -65, 0, true);
                        Chart.assignAxisMinMaxValues(chart, data);


                        var dateFormat = data["date_format"];


                        var svg = d3.select('#d3Div' + target);

                        svg.attr('height', height)
                            .datum(s1)
                            .call(chart);

                        Chart.canvasHeights(target, styleProps);
                        return chart;
                    }
                });

            }, Chart.cleanup, target);
        };
    },

    getD3LineCallback: function (target, params, showLabels, styleProps, filters, drillthroughKey, dashboardID) {
        return function (data) {
            Utils.noDataD3(data["values"], function () {
                nv.addGraph({
                    generate: function () {
                        var s1 = data["values"];

                        var format = d3.time.format("%m/%d/%Y");

                        var map = [];
                        var minY = null;
                        var maxY = null;
                        var minX = null;
                        var maxX = null;
                        for (var i = 0; i < s1.length; i++) {
                            var keyVals = s1[i];
                            map[i] = {};
                            for (var j = 0; j < keyVals.values.length; j++) {
                                var row = keyVals.values[j];
                                map[i][row.y] = row.sy;
                                row.x = format.parse(row.x);
                                if (minY == null || minY > row.y) {
                                    minY = row.y;
                                }
                                if (maxY == null || row.y > maxY) {
                                    maxY = row.y;
                                }
                                if (minX == null || minX > row.t) {
                                    minX = row.x;
                                }
                                if (maxX == null || row.t > maxX) {
                                    maxX = row.x;
                                }
                            }
                        }

                        var singleGoalValue = data["single_goal_value"];
                        if (typeof(singleGoalValue) != "undefined") {
                            var g = singleGoalValue["goal"];
                            if (g < minY) {
                                minY = g;
                            } else if (g > maxY) {
                                maxY = g;
                            }
                        }

                        var axisMinY = 0;
                        var axisMaxY = maxY + (maxY * .05);

                        var height = Chart.chartHeight(target, styleProps);
                        if (Chart.ieTest()) {
                            $("#d3Div" + target).height(height);
                        }

                        var chart = nv.models.lineChart()
                            //.width(width)
                            .height(height)
                            .useInteractiveGuideline(true)  //We want nice looking tooltips and a guideline!
                            .transitionDuration(350)  //how fast do you want the lines to transition?
                            .showLegend(true)       //Show the legend, allowing users to turn on/off line series.
                            .showYAxis(true)        //Show the y-axis
                            .showXAxis(true)        //Show the x-axis
                            .margin({top: 20, right: 40, bottom: 40, left: 80});

                        /*var customWidth = styleProps != null ? styleProps["preferredWidth"] : -1;
                         if (customWidth > -1) {
                         chart.width(customWidth);
                         }*/


                        if (data["relative_line"]) {
                            chart.showYAxis(false);
                            chart.yAxis.tickFormat(function (d, i) {
                                return map[i][d];
                            });
                        }

                        Chart.assignAxisLabels(chart.xAxis, chart.yAxis, data, 40, -65, 0, true);
                        Chart.assignAxisMinMaxValues(chart, data);

                        chart.forceY([axisMinY, axisMaxY]);

                        var dateFormat = data["date_format"];

                        chart.xAxis.tickFormat(function (d) {
                            return d3.time.format(dateFormat)(new Date(d))
                        });

                        var svg = d3.select('#d3Div' + target);

                        svg.attr('height', height)
                            .datum(s1)
                            .call(chart);

                        var events = data["events"];
                        if (typeof(events) != "undefined") {
                            var calcYMax = chart.yAxis.scale()(maxY);
                            var calcYMin = chart.yAxis.scale()(minY);
                            var targ = d3.select('#d3Div' + target + " .nv-linesWrap");
                            for (var eventIdx = 0; eventIdx < events.length; eventIdx++) {
                                var event = events[eventIdx];
                                var time = format.parse(event.date);
                                var calcX = chart.xAxis.scale()(time);
                                targ.append("g").append("rect").attr("height", (calcYMin - calcYMax)).attr("width", 3).style("fill", "#0000FF").attr("x", calcX).attr("y", 0);
                                targ.append("foreignObject").attr("width", 100).attr("height", 100).attr("y", (calcYMin - calcYMax) / 2).attr("x", calcX + 5).append("xhtml:body").attr("class", "report_annotation").style("font", "12px 'Helvetica Neue'").html("<p>" + event.label + "</p>");
                            }

                        }


                        if (typeof(singleGoalValue) != "undefined") {
                            var goal = singleGoalValue["goal"];
                            var calcXMax = chart.xAxis.scale()(maxX);
                            var calcXMin = chart.xAxis.scale()(minX);
                            var calcY = chart.yAxis.scale()(goal);
                            var goalTarg = d3.select('#d3Div' + target + " .nv-linesWrap");
                            var calcXWidth = (calcXMax - calcXMin);
                            goalTarg.append("g").append("rect").attr("width", calcXWidth).attr("height", 2).style("fill", "#88AACC").attr("x", 0).attr("y", calcY);
                            goalTarg.append("text").attr("y", calcY + 5).attr("x", calcXWidth / 2).style("font", "12px 'Helvetica Neue'").text("Goal");
                        }
                        //goalTarg.append("foreignObject").attr("width", 100).attr("height", 100).attr("y", (calcYMin - calcYMax) / 2).attr("x", calcX + 5).append("xhtml:body").attr("class", "report_annotation").style("font", "12px 'Helvetica Neue'").html("<p>"+event.label+"</p>");

                        /*var seriesIndex = 0;
                         var selector = 'g.nv-series-'+seriesIndex+' circle';
                         d3.selectAll(selector).classed("hover",true);*/
                        //d3.select('#d3Div' + target + ' g.nv-scatterwrap g.nv-series-0 path.nv-point').style('fill-opacity', 1).style('stroke-opacity', 1);

                        Chart.canvasHeights(target, styleProps);

                        //nv.utils.windowResize(function() { chart.update() });
                        return chart;
                    }
                });

            }, Chart.cleanup, target);
        };
    },

    getD3AreaCallback: function (target, params, showLabels, styleProps, filters, drillthroughKey, dashboardID) {
        return function (data) {
            Utils.noDataD3(data["values"], function () {
                nv.addGraph({
                    generate: function () {
                        var height = Chart.chartHeight(target, styleProps);
                        if (Chart.ieTest()) {
                            $("#d3Div" + target).height(height);
                        }

                        var s1 = data["values"];

                        var format = d3.time.format("%m/%d/%Y");

                        for (var i = 0; i < s1.length; i++) {
                            var keyVals = s1[i];
                            for (var j = 0; j < keyVals.values.length; j++) {
                                var row = keyVals.values[j];
                                row.x = format.parse(row.x);
                            }
                        }

                        var showLegend = data["showLegend"];

                        var chart = nv.models.stackedAreaChart()
                            //.width(width)
                            //.height(height)
                            .useInteractiveGuideline(true)  //We want nice looking tooltips and a guideline!
                            .transitionDuration(350)  //how fast do you want the lines to transition?
                            .showLegend(showLegend)       //Show the legend, allowing users to turn on/off line series.
                            .showYAxis(true)        //Show the y-axis
                            .showXAxis(true)        //Show the x-axis
                            .margin({top: 20, right: 40, bottom: 50, left: 76});

                        /*var customWidth = styleProps != null ? styleProps["preferredWidth"] : -1;
                         if (customWidth > -1) {
                         chart.width(customWidth);
                         }*/

                        Chart.assignAxisLabels(chart.xAxis, chart.yAxis, data, 40, -65);
                        Chart.assignAxisMinMaxValues(chart, data);

                        var dateFormat = data["date_format"];

                        chart.xAxis.tickFormat(function (d) {
                            return d3.time.format(dateFormat)(new Date(d))
                        });


                        d3.select('#d3Div' + target)
                            //.attr('width', width)
                            .attr('height', height)
                            .datum(s1)
                            .call(chart);

                        Chart.canvasHeights(target, styleProps);

                        //nv.utils.windowResize(function() { chart.update() });
                        return chart;
                    }
                });

            }, Chart.cleanup, target);
        };
    },

    cleanup: function (target) {
        if (Chart.charts[target]) {
            var tt = $("#" + target);
            tt.unbind("jqplotDataClick");
            tt.unbind("jqplotDataClick");
            Chart.charts[target].destroy();
            delete Chart.charts[target];
            Chart.charts[target] = null;
        }
    }
};