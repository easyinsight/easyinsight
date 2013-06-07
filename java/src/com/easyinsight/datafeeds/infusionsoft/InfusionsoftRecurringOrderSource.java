package com.easyinsight.datafeeds.infusionsoft;

import com.easyinsight.analysis.AnalysisDimension;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.ReportException;
import com.easyinsight.core.Key;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.logging.LogClass;
import com.easyinsight.storage.IDataStorage;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.*;

/**
 * User: jamesboe
 * Date: 4/24/13
 * Time: 11:30 AM
 */
public class InfusionsoftRecurringOrderSource extends InfusionsoftTableSource {

    public static final String ID = "Id";
    public static final String CONTACT_ID = "ContactId";
    public static final String ORIGINATING_ORDER_ID = "OriginatingOrderId";
    public static final String PRODUCT_ID = "ProductId";

    public InfusionsoftRecurringOrderSource() {
        setFeedName("Recurring Orders");
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.INFUSIONSOFT_RECURRING_ORDERS;
    }

    @NotNull
    @Override
    protected List<String> getKeys(FeedDefinition parentDefinition) {
        return Arrays.asList(ID, CONTACT_ID, ORIGINATING_ORDER_ID, PRODUCT_ID);
    }

    @Override
    public List<AnalysisItem> createAnalysisItems(Map<String, Key> keys, Connection conn, FeedDefinition parentDefinition) {
        List<AnalysisItem> analysisitems = new ArrayList<AnalysisItem>();
        analysisitems.add(new AnalysisDimension(keys.get(ID), "Recurring Order ID"));
        analysisitems.add(new AnalysisDimension(keys.get(CONTACT_ID), "Recurring Order Contact ID"));
        analysisitems.add(new AnalysisDimension(keys.get(PRODUCT_ID), "Recurring Order Product ID"));
        return analysisitems;
    }

    @Override
    public DataSet getDataSet(Map<String, Key> keys, Date now, FeedDefinition parentDefinition, IDataStorage IDataStorage, EIConnection conn, String callDataID, Date lastRefreshDate) throws ReportException {
        try {
            return query("RecurringOrder", createAnalysisItems(keys, conn, parentDefinition), (InfusionsoftCompositeSource) parentDefinition);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }
}
