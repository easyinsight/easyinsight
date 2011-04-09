package com.easyinsight.datafeeds.highrise;

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
 * Date: Jun 24, 2010
 * Time: 10:08:53 AM
 */
public class HighRiseCompany3To4 extends DataSourceMigration {
    public HighRiseCompany3To4(FeedDefinition dataSource) {
        super(dataSource);
    }

    @Override
    public void migrate(Map<String, Key> keys, EIConnection conn) throws Exception {
        addAnalysisItem(new AnalysisDimension(new NamedKey(HighRiseCompanySource.COUNTRY)));
        addAnalysisItem(new AnalysisDimension(new NamedKey(HighRiseCompanySource.STATE)));
        addAnalysisItem(new AnalysisDimension(new NamedKey(HighRiseCompanySource.CITY)));
    }

    @Override
    public int fromVersion() {
        return 3;
    }

    @Override
    public int toVersion() {
        return 4;
    }
}
