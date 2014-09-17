Utils = {
    noData:function (data, f, cleanup, target) {
        if(cleanup)
            cleanup(target);
        if ((data instanceof Array && data.length > 0) || (typeof data == "object" && Object.keys(data).length > 0) || typeof data == "number" || (typeof data == "string" && data != "")) {
            $("#" + target + " .reportArea").show();
            $("#" + target + " .noData").hide();
            f();
        } else {
            $("#" + target + " .reportArea").hide();
            $("#" + target + " .noData").show();
        }
        if(typeof(afterRefresh) != "undefined") {
            if (afterRefresh.length > 1) {
                afterRefresh($("#" + target + " .loading"), target)();
            } else if(afterRefresh.length > 0) {
                afterRefresh($("#" + target + " .loading"))();
            } else {
                afterRefresh();
            }
        }
    },

    noDataD3:function (data, f, cleanup, target) {
        if ((data instanceof Array && data.length > 0)) {
            $("#" + target + " .reportArea").show();
            $("#" + target + " .noData").hide();
            f();
        } else {
            $("#" + target + " .reportArea").hide();
            $("#" + target + " .noData").show();
        }
        if(typeof(afterRefresh) != "undefined") {
            if (afterRefresh.length > 1) {
                afterRefresh($("#" + target + " .loading"), target)();
            } else if(afterRefresh.length > 0) {
                afterRefresh($("#" + target + " .loading"))();
            } else {
                afterRefresh();
            }
        }
    }
};

Array.prototype.flatten = function flatten() {
    var flat = [];
    for (var i = 0, l = this.length; i < l; i++) {
        var type = Object.prototype.toString.call(this[i]).split(' ').pop().split(']').shift().toLowerCase();
        if (type) {
            flat = flat.concat(/^(array|collection|arguments|object)$/.test(type) ? flatten.call(this[i]) : this[i]);
        }
    }
    return flat;
};