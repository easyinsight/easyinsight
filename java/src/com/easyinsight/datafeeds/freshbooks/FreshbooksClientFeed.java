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
 * Time: 1:46:46 PM
 */
public class FreshbooksClientFeed extends FreshbooksFeed {
    public FreshbooksClientFeed(String url, String tokenKey, String tokenSecretKey, FreshbooksCompositeSource parentSource) {
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

            int requestPage = 1;
            int pages;
            int currentPage;
            do {
                Document invoicesDoc = query("client.list", "<page>" + requestPage + "</page>", conn);
                Nodes nodes = invoicesDoc.query("/response/clients");
                if (nodes.size() > 0) {
                    Node invoicesSummaryNode = nodes.get(0);
                    String pageString = invoicesSummaryNode.query("@pages").get(0).getValue();
                    String currentPageString = invoicesSummaryNode.query("@page").get(0).getValue();
                    pages = Integer.parseInt(pageString);
                    currentPage = Integer.parseInt(currentPageString);
                    Nodes invoices = invoicesDoc.query("/response/clients/client");
                    for (int i = 0; i < invoices.size(); i++) {
                        Node invoice = invoices.get(i);
                        String firstName = queryField(invoice, "first_name/text()");
                        String lastName = queryField(invoice, "last_name/text()");
                        String name = firstName + " " + lastName;
                        String userName = queryField(invoice, "username/text()");
                        String email = queryField(invoice, "email/text()");
                        String clientID = queryField(invoice, "client_id/text()");
                        String workPhone = queryField(invoice, "work_phone/text()");
                        String address1 = queryField(invoice, "p_street1/text()");
                        String address2 = queryField(invoice, "p_street2/text()");
                        String city = queryField(invoice, "p_city/text()");
                        String state = queryField(invoice, "p_state/text()");
                        String zip = queryField(invoice, "p_code/text()");
                        String country = queryField(invoice, "p_country/text()");
                        String organization = queryField(invoice, "organization/text()");
                        String folder = queryField(invoice, "folder/text()");

                        IRow row = dataSet.createRow();
                        addValue(row, FreshbooksClientSource.FIRST_NAME, firstName, keys);
                        addValue(row, FreshbooksClientSource.FOLDER, folder, keys);
                        addValue(row, FreshbooksClientSource.CLIENT_ID, clientID, keys);
                        addValue(row, FreshbooksClientSource.LAST_NAME, lastName, keys);
                        addValue(row, FreshbooksClientSource.NAME, name, keys);
                        addValue(row, FreshbooksClientSource.USERNAME, userName, keys);
                        addValue(row, FreshbooksClientSource.EMAIL, email, keys);
                        addValue(row, FreshbooksClientSource.WORK_PHONE, workPhone, keys);
                        addValue(row, FreshbooksClientSource.PRIMARY_STREET1, address1, keys);
                        addValue(row, FreshbooksClientSource.PRIMARY_STREET2, address2, keys);
                        addValue(row, FreshbooksClientSource.CITY, city, keys);
                        addValue(row, FreshbooksClientSource.STATE, state, keys);
                        addValue(row, FreshbooksClientSource.POSTAL, zip, keys);
                        addValue(row, FreshbooksClientSource.COUNTRY, country, keys);
                        addValue(row, FreshbooksClientSource.ORGANIZATION, organization, keys);
                        addValue(row, FreshbooksClientSource.COUNT, 1, keys);
                    }
                    requestPage++;
                } else {
                    break;
                }
            } while (currentPage < pages);
            return dataSet;
        } catch (ReportException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
