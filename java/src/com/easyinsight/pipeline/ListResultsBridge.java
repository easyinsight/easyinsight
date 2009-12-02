package com.easyinsight.pipeline;

import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.DataResults;
import com.easyinsight.dataset.DataSet;

import java.util.List;

/**
 * User: jamesboe
 * Date: Nov 30, 2009
 * Time: 9:39:31 AM
 */
public class ListResultsBridge implements ResultsBridge {

    public DataResults toDataResults(DataSet dataSet, List<AnalysisItem> columns) {
        return dataSet.toListDataResults(columns);
    }
}
