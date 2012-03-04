package com.easyinsight.datafeeds.freshbooks;

import com.easyinsight.analysis.AnalysisDateDimension;
import com.easyinsight.analysis.AnalysisDimension;
import com.easyinsight.core.Key;
import com.easyinsight.core.NamedKey;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.DataSourceMigration;
import com.easyinsight.datafeeds.FeedDefinition;

import java.util.Map;

/**
 * User: jamesboe
 * Date: 3/1/12
 * Time: 2:55 PM
 */
public class FreshbooksEstimate1To2 extends DataSourceMigration {
    public FreshbooksEstimate1To2(FeedDefinition dataSource) {
        super(dataSource);
    }

    @Override
    public void migrate(Map<String, Key> keys, EIConnection conn) throws Exception {
        addAnalysisItem(new AnalysisDimension(new NamedKey(FreshbooksEstimateSource.STATUS), true));
        addAnalysisItem(new AnalysisDateDimension(new NamedKey(FreshbooksEstimateSource.DATE), true, AnalysisDateDimension.DAY_LEVEL));
    }

    @Override
    public int fromVersion() {
        return 1;
    }

    @Override
    public int toVersion() {
        return 2;
    }
}
