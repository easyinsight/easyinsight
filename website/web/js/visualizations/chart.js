Chart = {
    getCallback:function (target, params, showLabels, styleProps, filters, extras, drillthroughKey) {
        return function (data) {
            Utils.noData(data["values"].flatten(), function () {
                if (showLabels) {
                    var labels = data["labels"];
                    params.jqplotOptions.legend = $.extend({}, params.jqplotOptions.legend, {show:true, labels:labels});
                }
                params.jqplotOptions.grid = $.extend({}, params.jqplotOptions.grid, {borderWidth:0, shadow:false});
                var s1 = data["values"];
                var customHeight = styleProps["customHeight"];
                if (customHeight > -1) {
                    var height;
                    if (customHeight > 0) {
                        height = customHeight;
                    } else {
                        var verticalMargin = styleProps["verticalMargin"];
                        height = $(document).height() - $('#filterRow').height() - $('#reportHeader').height() - verticalMargin;
                        if (height < 400) {
                            height = 400;
                        }
                    }

                    var selector = "#" + target;
                    var selector2 = "#" + target + 'ReportArea';
                    $(selector).height(height);
                    $(selector2).height(height);

                }

                if (extras) {
                    extras(data);
                }

                if (data["drillthrough"] || params.drillthrough) {
                    var dtOptions = $.extend(true, {}, data["drillthrough"], params.drillthrough);
                    if (dtOptions["id"]) {
                        var tt = $("#" + target);
                        tt.bind("jqplotDataClick", function (event, seriesIndex, pointIndex, curData) {
                            var drillthrough = data["drillthrough"];
                            var f = {"reportID": dtOptions["reportID"], "drillthroughID": dtOptions["id"], "embedded": dtOptions["embedded"], "source": dtOptions["source"], "drillthroughKey": drillthroughKey, "filters": filters,
                            "drillthrough_values": {}};
                            f["drillthrough_values"][dtOptions["xaxis"]] = data["ticks"][pointIndex];
                            if(drillthrough["stack"])
                                f["drillthrough_values"][drillthrough["stack"]] = data["series"][seriesIndex].label;
                            drillThrough(f);
                        });
                    }
                }
                if (data["params"]) {
                    var v = JSON.stringify(data["params"]).replace(/\"/g, "");
                    eval("var w = " + v);
                    params.jqplotOptions = $.extend(true, {}, params.jqplotOptions, w);
                }
                Chart.charts[target] = $.jqplot(target + 'ReportArea', s1, params.jqplotOptions);

            }, Chart.cleanup, target);
        };
    },
    charts:{},
    prevTooltip:{chart:null, text:""},

    cleanup:function (target) {
        if (Chart.charts[target]) {
            var tt = $("#" + target);
            tt.unbind("jqplotDataClick");
            Chart.charts[target].destroy();
            delete Chart.charts[target];
            Chart.charts[target] = null;
        }
    },

    getStackedBarChart:function (target, params, styleProps, filters, drillthroughKey) {
        return Chart.getCallback(target, params, true, styleProps, filters, function (data) {
            params.jqplotOptions.series = data["series"];
            params.jqplotOptions.axes.yaxis.ticks = data["ticks"];
            var sums = {};
            $.each(data["values"], function (stackIndex, value) {
                $.each(value, function (i, v) {
                    if (typeof(sums[v[1]]) === "undefined")
                        sums[v[1]] = 0;

                    sums[v[1]] = sums[v[1]] + v[0];
                })
            })

            var tt = $("#" + target);
            tt.bind("jqplotDataMouseOver", Chart.stackColumnToolTipHover(data["ticks"], data["series"], sums, 0));
            tt.bind("jqplotMouseLeave", Chart.columnToolTipOut);
        }, drillthroughKey)
    },

    getBarChartCallback:function (target, params, styleProps, filters, drillthroughKey) {
        return Chart.getCallback(target, params, false, styleProps, filters, function (data) {
            var tt = $("#" + target);
            tt.bind("jqplotDataMouseOver", Chart.columnToolTipHover(data["ticks"], 0));
            tt.bind("jqplotMouseLeave", Chart.columnToolTipOut);
        }, drillthroughKey)
    },

    getStackedColumnChart:function (target, params, styleProps, filters, drillthroughKey) {
        return Chart.getCallback(target, params, true, styleProps, filters, function (data) {
            params.jqplotOptions.series = data["series"];
            params.jqplotOptions.axes.xaxis.ticks = data["ticks"];
            var tt = $("#" + target);
            var sums = {};
            $.each(data["values"], function (stackIndex, value) {
                $.each(value, function (i, v) {
                    if (typeof(sums[v[0]]) === "undefined")
                        sums[v[0]] = 0;
                    sums[v[0]] = sums[v[0]] + v[1];
                })
            })
            tt.bind("jqplotDataMouseOver", Chart.stackColumnToolTipHover(data["ticks"], data["series"], sums, 1));
            tt.bind("jqplotMouseLeave", Chart.columnToolTipOut);
        }, drillthroughKey)
    },

    getColumnChartCallback:function (target, params, styleProps, filters, drillthroughKey) {
        return Chart.getCallback(target, params, false, styleProps, filters, function (data) {
            var tt = $("#" + target);
            tt.bind("jqplotDataMouseOver", Chart.columnToolTipHover(data["ticks"], 1));
            tt.bind("jqplotMouseLeave", Chart.columnToolTipOut);
        }, drillthroughKey)
    },

    columnToolTipHover:function (ticks, i) {
        return function (ev, seriesIndex, pointIndex, data) {
            var mouseX = ev.pageX; //these are going to be how jquery knows where to put the div that will be our tooltip
            var mouseY = ev.pageY;

            var s = ticks[pointIndex] + ', ' + data[i];
            if (!(ev.target == Chart.prevTooltip.chart && s == Chart.prevTooltip.text)) {
                $('#chartpseudotooltip').html(s);
                Chart.prevTooltip = { chart:ev.target, text:s };

                var cssObj = {
                    'position':'absolute',
                    'font-weight':'bold',
                    'left':mouseX + 'px',
                    'top':mouseY + 'px'
                };

                $('#chartpseudotooltip').css(cssObj);
                $('#chartpseudotooltip').show();
            }
        };
    },
    stackColumnToolTipHover:function (ticks, stack, sums, index) {
        return function (ev, seriesIndex, pointIndex, data) {
            var mouseX = ev.pageX; //these are going to be how jquery knows where to put the div that will be our tooltip
            var mouseY = ev.pageY;

            var s = "<b>" + stack[seriesIndex].label + "</b><br/>" + ticks[pointIndex] + '<br />' + data[index] + "(" + ((data[index] / sums[pointIndex + 1]) * 100).toFixed(2) + "%)<br />" + sums[pointIndex + 1] + " Total";
            if (!(ev.target == Chart.prevTooltip.chart && s == Chart.prevTooltip.text)) {
                $('#chartpseudotooltip').html(s);
                Chart.prevTooltip = { chart:ev.target, text:s };
                var cssObj = {
                    'position':'absolute',
                    'left':mouseX + 'px',
                    'top':mouseY + 'px'
                };

                $('#chartpseudotooltip').css(cssObj);
                $('#chartpseudotooltip').show();
            }
        }
    },
    columnToolTipOut:function (ev) {

        if (ev.relatedTarget == $("#chartpseudotooltip")[0])
            $("#chartpseudotooltip").bind("mouseout", function f() {
                e = event.toElement || event.relatedTarget;
                if (e && (e.parentNode == this || e && (e.parentNode && e.parentNode.parentNode == this) ||
                    e == this)) {
                    return;
                }
                $("#chartpseudotooltip").unbind("mouseout", f);
            });
        else {
            $('#chartpseudotooltip').html('');
            $('#chartpseudotooltip').hide();
            Chart.prevTooltip = {chart:null, text:""};
        }

    },

    getPieChartCallback:function (target, params, styleProps, filters, drillthroughKey) {
        return Chart.getCallback(target, params, false, styleProps, function (data) {
            var tt = $("#" + target);
            tt.bind("jqplotDataMouseOver", Chart.pieToolTipHover(target));
            tt.bind("jqplotMouseLeave", Chart.columnToolTipOut);
        }, drillthroughKey);
    },

    pieToolTipHover:function (target) {
        return function (ev, seriesIndex, pointIndex, data) {
            var c = Chart.charts[target]["axes"]["xaxis"]["_series"][0]._center;
            var r = Chart.charts[target]["axes"]["xaxis"]["_series"][0]._radius;
            var o = $(Chart.charts[target]["axes"]["xaxis"]["_series"][0].canvas._ctx.canvas).offset();
            var x = c[0] + o.left;
            var y = c[1] + o.top;

            var mouseX = ev.pageX; //these are going to be how jquery knows where to put the div that will be our tooltip
            var mouseY = ev.pageY;


            var dx = x - mouseX;
            var dy = y - mouseY;
            var t = Math.atan2(dy, dx);
            var finalX = -Math.cos(t) * r + x;
            var finalY = -Math.sin(t) * r + y;
            var topBottom = (finalY - y) > 0 ? "top" : "bottom";
            var rightLeft = (finalX - x) > 0 ? "left" : "right";


            var s = "<b>" + data[0] + "</b>: " + data[1];

            if (!(ev.target == Chart.prevTooltip.chart && s == Chart.prevTooltip.text)) {
                $("#chartpseudotooltip").html(s);
                Chart.prevTooltip = { chart:ev.target, text:s }
                if(topBottom == "bottom")
                    finalY = finalY - $("#chartpseudotooltip").outerHeight();
                if(rightLeft == "right")
                    finalX = finalX - $("#chartpseudotooltip").outerWidth();
                var cssObj = {
                    'position':'absolute',
                    'left': finalX + 'px',
                    'top': finalY + 'px'
                };



                $("#chartpseudotooltip").css(cssObj);
                $('#chartpseudotooltip').show();
            }
        }
    }

};