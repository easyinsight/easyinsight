Chart = {
    getCallback:function (target, params, showLabels, styleProps, extras) {
        return function (data) {
            Utils.noData(data["values"].flatten(), function () {
                if (showLabels) {
                    var labels = data["labels"];
                    params.jqplotOptions.legend = $.extend({}, params.jqplotOptions.legend, {show:true, labels:labels});
                }

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

                if (data["drillthrough"]) {
                    var dtOptions = $.extend(true, {}, data["drillthrough"], params.drillthrough);
                    var tt = $("#" + target);
                    tt.bind("jqplotDataClick", function (event, seriesIndex, pointIndex, curData) {
                        var drillthrough = data["drillthrough"];
                        var s = 'reportID=' + dtOptions["reportID"] + '&drillthroughID=' + dtOptions["id"] + '&embedded=' + dtOptions["embedded"] + '&sourceField=' +
                            dtOptions["source"] + '&f' + dtOptions["xaxis"] + "=" + encodeURI(data["ticks"][pointIndex]);
                        if (drillthrough["stack"]) {
                            s = s + "&f" + drillthrough["stack"] + "=" + encodeURI(data["series"][seriesIndex].label);
                        }
                        drillThrough(s);

                    });
                }
                if (data["params"]) {
                    params.jqplotOptions = $.extend(true, {}, params.jqplotOptions, data["params"]);
                }
                Chart.charts[target] = $.jqplot(target + 'ReportArea', s1, params.jqplotOptions);

            }, Chart.cleanup, target);
        };
    },
    charts:{},

    cleanup:function (target) {
        if (Chart.charts[target]) {
            Chart.charts[target].destroy();
            delete Chart.charts[target];
            Chart.charts[target] = null;
        }
    },

    getStackedBarChart:function (target, params, styleProps) {
        return Chart.getCallback(target, params, true, styleProps, function (data) {
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
            tt.bind("jqplotDataHighlight", Chart.stackColumnToolTipHover(data["ticks"], data["series"], sums, 0));
            tt.bind("jqplotDataUnhighlight", Chart.columnToolTipOut);
        })
    },

    getStackedColumnChart:function (target, params, styleProps) {
        return Chart.getCallback(target, params, true, styleProps, function (data) {
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
            tt.bind("jqplotDataHighlight", Chart.stackColumnToolTipHover(data["ticks"], data["series"], sums, 1));
            tt.bind("jqplotDataUnhighlight", Chart.columnToolTipOut);
        })
    },

    getColumnChartCallback:function (target, params, styleProps) {
        return Chart.getCallback(target, params, false, styleProps, function (data) {
            var tt = $("#" + target);
            tt.bind("jqplotDataHighlight", Chart.columnToolTipHover(data["ticks"]));
            tt.bind("jqplotDataUnhighlight", Chart.columnToolTipOut);
        })
    },

    columnToolTipHover:function (ticks) {
        return function (ev, seriesIndex, pointIndex, data) {
            var mouseX = ev.pageX; //these are going to be how jquery knows where to put the div that will be our tooltip
            var mouseY = ev.pageY;
            $('#chartpseudotooltip').html(ticks[pointIndex] + ', ' + data[1]);

            var cssObj = {
                'position':'absolute',
                'font-weight':'bold',
                'left':mouseX + 'px',
                'top':mouseY + 'px'
            };

            $('#chartpseudotooltip').css(cssObj);
            $('#chartpseudotooltip').show();
        };
    },
    stackColumnToolTipHover:function (ticks, stack, sums, index) {
        return function (ev, seriesIndex, pointIndex, data) {
            var mouseX = ev.pageX; //these are going to be how jquery knows where to put the div that will be our tooltip
            var mouseY = ev.pageY;

            $('#chartpseudotooltip').html("<b>" + stack[seriesIndex].label + "</b><br/>" + ticks[pointIndex] + '<br />' + data[index] + "(" + ((data[index] / sums[pointIndex + 1]) * 100).toFixed(2) + "%)<br />" + sums[pointIndex + 1] + " Total");

            var cssObj = {
                'position':'absolute',
                'left':mouseX + 'px',
                'top':mouseY + 'px'
            };

            $('#chartpseudotooltip').css(cssObj);
            $('#chartpseudotooltip').show();
        }
    },
    columnToolTipOut:function (ev) {
        $('#chartpseudotooltip').html('');
        $('#chartpseudotooltip').hide();
    },

    getPieChartCallback:function (target, params, styleProps) {
        return Chart.getCallback(target, params, false, styleProps, function (data) {
            var tt = $("#" + target);
            tt.bind("jqplotDataHighlight", Chart.pieToolTipHover);
            tt.bind("jqplotDataUnhighlight", Chart.pieToolTipOut);
        });
    },

    pieToolTipHover:function (ev, seriesIndex, pointIndex, data) {
        var $this = $(this);
        $this.attr('title', data[0] + ": " + data[1]);
    },
    pieToolTipOut:function (ev) {
        var $this = $(this);
        $this.attr('title', "");
    }

};

(function ($) {
    $.jqplot.tickNumberFormatter = function (format, val) {
        return numberWithCommas($.jqplot.sprintf(format, val));
    };

    function numberWithCommas(x) {
        return x.toString().replace(/\B(?=(?:\d{3})+(?!\d))/g, ",");
    }
})(jQuery);

(function ($) {
    $.jqplot.currencyTickNumberFormatter = function (format, val) {
        return "$" + numberWithCommas($.jqplot.sprintf(format, val));
    };

    function numberWithCommas(x) {
        return x.toString().replace(/\B(?=(?:\d{3})+(?!\d))/g, ",");
    }
})(jQuery);