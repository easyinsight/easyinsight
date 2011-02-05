package com.easyinsight.datafeeds.freshbooks;

import com.easyinsight.analysis.*;
import com.easyinsight.core.Key;
import com.easyinsight.database.EIConnection;
import com.easyinsight.dataset.DataSet;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: jamesboe
 * Date: Jul 29, 2010
 * Time: 3:27:08 PM
 */
public class FreshbooksProjectFeed extends FreshbooksFeed {
    protected FreshbooksProjectFeed(String url, String tokenKey, String tokenSecretKey, FreshbooksCompositeSource parentSource) {
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
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            int requestPage = 1;
            int pages;
            int currentPage;
            do {
                Document invoicesDoc = query("project.list", "<page>" + requestPage + "</page>", conn);
                Node invoicesSummaryNode = invoicesDoc.query("/response/projects").get(0);
                String pageString = invoicesSummaryNode.query("@pages").get(0).getValue();
                String currentPageString = invoicesSummaryNode.query("@page").get(0).getValue();
                pages = Integer.parseInt(pageString);
                currentPage = Integer.parseInt(currentPageString);
                Nodes invoices = invoicesDoc.query("/response/projects/projects");

                for (int i = 0; i < invoices.size(); i++) {
                    Node invoice = invoices.get(i);
                    String projectID = queryField(invoice, "project_id/text()");
                    String name = queryField(invoice, "name/text()");
                    String description = queryField(invoice, "description/text()");
                    String billMethod = queryField(invoice, "bill_method/text()");
                    String clientID = queryField(invoice, "client_id/text()");
                    String rateString = queryField(invoice, "rate/text()");
                    IRow row = dataSet.createRow();
                    row.addValue(keys.get(FreshbooksProjectSource.PROJECT_ID), projectID);
                    row.addValue(keys.get(FreshbooksProjectSource.CLIENT_ID), clientID);
                    row.addValue(keys.get(FreshbooksProjectSource.DESCRIPTION), description);
                    row.addValue(keys.get(FreshbooksProjectSource.NAME), name);
                    row.addValue(keys.get(FreshbooksProjectSource.BILL_METHOD), billMethod);
                    if (rateString != null) row.addValue(keys.get(FreshbooksProjectSource.RATE), Double.parseDouble(rateString));
                    row.addValue(keys.get(FreshbooksInvoiceSource.COUNT), 1);
                }
                requestPage++;
            } while (currentPage < pages);
            return dataSet;
        } catch (ReportException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
