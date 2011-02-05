package com.easyinsight.datafeeds.freshbooks;

import com.easyinsight.analysis.*;
import com.easyinsight.core.Key;
import com.easyinsight.database.EIConnection;
import com.easyinsight.dataset.DataSet;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;

import java.util.*;

/**
 * User: jamesboe
 * Date: Jul 29, 2010
 * Time: 3:26:33 PM
 */
public class FreshbooksStaffFeed extends FreshbooksFeed {
    protected FreshbooksStaffFeed(String url, String tokenKey, String tokenSecretKey, FreshbooksCompositeSource parentSource) {
        super(url, tokenKey, tokenSecretKey);
    }

    @Override
    public DataSet getAggregateDataSet(Set<AnalysisItem> analysisItems, Collection<FilterDefinition> filters, InsightRequestMetadata insightRequestMetadata, List<AnalysisItem> allAnalysisItems, boolean adminMode, EIConnection conn) throws ReportException {
        try {
            Map<String, Key> keys = new HashMap<String, Key>();
            for (AnalysisItem analysisItem : analysisItems) {
                keys.put(analysisItem.getKey().toKeyString(), analysisItem.createAggregateKey());
            }
            DataSet dataSet = new DataSet();


                Document invoicesDoc = query("staff.list", "", conn);
                Node invoicesSummaryNode = invoicesDoc.query("/response/staff_members").get(0);


                Nodes invoices = invoicesDoc.query("/response/staff_members/member");
                for (int i = 0; i < invoices.size(); i++) {
                    Node invoice = invoices.get(i);
                    String firstName = queryField(invoice, "first_name/text()");
                    String lastName = queryField(invoice, "last_name/text()");
                    String name = firstName + " " + lastName;
                    String userName = queryField(invoice, "username/text()");
                    String email = queryField(invoice, "email/text()");
                    String staffID = queryField(invoice, "staff_id/text()");

                    IRow row = dataSet.createRow();
                    row.addValue(keys.get(FreshbooksStaffSource.FIRST_NAME), firstName);
                    row.addValue(keys.get(FreshbooksStaffSource.STAFF_ID), staffID);
                    row.addValue(keys.get(FreshbooksStaffSource.LAST_NAME), lastName);
                    row.addValue(keys.get(FreshbooksStaffSource.NAME), name);
                    row.addValue(keys.get(FreshbooksStaffSource.USERNAME), userName);
                    row.addValue(keys.get(FreshbooksStaffSource.EMAIL), email);
                }

            return dataSet;
        } catch (ReportException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
