package com.easyinsight.analysis {
public interface IReportController {
    function createDataView():DataViewFactory;
    function createEmbeddedView():EmbeddedViewFactory;
}
}