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
 * Time: 10:06:38 AM
 */
public class FreshbooksInvoiceFeed extends FreshbooksFeed {
    protected FreshbooksInvoiceFeed(String url, String tokenKey, String tokenSecretKey, FreshbooksCompositeSource parentSource) {
        super(url, tokenKey, tokenSecretKey);
    }

    @Override
    public DataSet getAggregateDataSet(Set<AnalysisItem> analysisItems, Collection<FilterDefinition> filters, InsightRequestMetadata insightRequestMetadata, List<AnalysisItem> allAnalysisItems, boolean adminMode, EIConnection conn) throws ReportException {

        try {
            FreshbooksCompositeSource parent = (FreshbooksCompositeSource) getParentSource(conn);
            if (!parent.isLiveDataSource()) {
                return super.getAggregateDataSet(analysisItems, filters, insightRequestMetadata, allAnalysisItems, adminMode, conn);
            }
            Map<String, Collection<Key>> keys = new HashMap<String, Collection<Key>>();
            for (AnalysisItem analysisItem : analysisItems) {
                if (analysisItem.isDerived()) {
                    continue;
                }
                Collection<Key> keyColl = keys.get(analysisItem.getKey().toKeyString());
                if (keyColl == null) {
                    keyColl = new ArrayList<Key>();
                    keys.put(analysisItem.getKey().toKeyString(), keyColl);
                }
                keyColl.add(analysisItem.createAggregateKey());
            }
            DataSet dataSet = new DataSet();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            int requestPage = 1;
            int pages;
            int currentPage;
            do {
                String string = "<page>" + requestPage + "</page>";
                Document invoicesDoc = query("invoice.list", string, conn);
                Nodes invoiceList = invoicesDoc.query("/response/invoices");
                if (invoiceList.size() > 0) {
                    Node invoicesSummaryNode = invoiceList.get(0);
                    String pageString = invoicesSummaryNode.query("@pages").get(0).getValue();
                    String currentPageString = invoicesSummaryNode.query("@page").get(0).getValue();
                    pages = Integer.parseInt(pageString);
                    currentPage = Integer.parseInt(currentPageString);
                    Nodes invoices = invoicesDoc.query("/response/invoices/invoice");

                    for (int i = 0; i < invoices.size(); i++) {
                        Node invoice = invoices.get(i);
                        String invoiceID = queryField(invoice, "invoice_id/text()");
                        String invoiceNumber = queryField(invoice, "number/text()");
                        String clientID = queryField(invoice, "client_id/text()");
                        String status = queryField(invoice, "status/text()");
                        String poNumber = queryField(invoice, "po_number/text()");
                        String amountString = queryField(invoice, "amount/text()");
                        String amountOutstandingString = queryField(invoice, "amount_outstanding/text()");
                        String paidString = queryField(invoice, "paid/text()");
                        String invoiceDateString = queryField(invoice, "date/text()");
                        Date invoiceDate = df.parse(invoiceDateString);
                        IRow row = dataSet.createRow();
                        String discount = queryField(invoice, "discount/text()");
                        addValue(row, FreshbooksInvoiceSource.INVOICE_ID, invoiceID, keys);
                        addValue(row, FreshbooksInvoiceSource.INVOICE_NUMBER, invoiceNumber, keys);
                        addValue(row, FreshbooksInvoiceSource.CLIENT_ID, clientID, keys);
                        addValue(row, FreshbooksInvoiceSource.STATUS, status, keys);
                        addValue(row, FreshbooksInvoiceSource.PO_NUMBER, poNumber, keys);
                        if (amountString != null) addValue(row, FreshbooksInvoiceSource.AMOUNT, Double.parseDouble(amountString), keys);
                        if (amountString != null) addValue(row, FreshbooksInvoiceSource.AMOUNT_OUTSTANDING, Double.parseDouble(amountOutstandingString), keys);
                        if (amountString != null) addValue(row, FreshbooksInvoiceSource.AMOUNT_PAID, Double.parseDouble(paidString), keys);
                        if (discount != null) addValue(row, FreshbooksInvoiceSource.DISCOUNT, Double.parseDouble(discount), keys);
                        addValue(row, FreshbooksInvoiceSource.INVOICE_DATE, invoiceDate, keys);
                        addValue(row, FreshbooksInvoiceSource.COUNT, 1, keys);
                    }
                } else {
                    break;
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
