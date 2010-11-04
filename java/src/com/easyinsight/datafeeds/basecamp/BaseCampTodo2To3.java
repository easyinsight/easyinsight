package com.easyinsight.datafeeds.basecamp;

import com.easyinsight.analysis.AnalysisDateDimension;
import com.easyinsight.core.Key;
import com.easyinsight.core.NamedKey;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.DataSourceMigration;
import com.easyinsight.datafeeds.FeedDefinition;

import java.util.Map;

/**
 * User: jamesboe
 * Date: Apr 7, 2010
 * Time: 8:59:27 AM
 */
public class BaseCampTodo2To3 extends DataSourceMigration {
    public BaseCampTodo2To3(FeedDefinition dataSource) {
        super(dataSource);
    }

    @Override
    public void migrate(Map<String, Key> keys, EIConnection conn) throws Exception {
        addAnalysisItem(new AnalysisDateDimension(new NamedKey(BaseCampTodoSource.DUEON), true, AnalysisDateDimension.DAY_LEVEL));
        addAnalysisItem(new AnalysisDateDimension(new NamedKey(BaseCampTodoSource.MILESTONE_CREATED_ON), true, AnalysisDateDimension.DAY_LEVEL));
    }

    @Override
    public int fromVersion() {
        return 2;
    }

    @Override
    public int toVersion() {
        return 3;
    }
}