package com.easyinsight.datafeeds.quickbase;

import com.easyinsight.analysis.*;
import com.easyinsight.core.Key;
import com.easyinsight.core.NamedKey;
import com.easyinsight.core.Value;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.*;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.logging.LogClass;
import com.easyinsight.pipeline.CompositeReportPipeline;
import com.easyinsight.pipeline.Pipeline;
import com.easyinsight.storage.*;
import com.easyinsight.users.Account;
import nu.xom.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;

/**
 * User: jamesboe
 * Date: 12/15/10
 * Time: 3:52 PM
 */
public class QuickbaseDatabaseSource extends ServerDataSourceDefinition {

    private static final String REQUEST = "<qdbapi><ticket>{0}</ticket><apptoken>{1}</apptoken><clist>{2}</clist><fmt>structured</fmt><options>num-1000</options></qdbapi>";
    private static final String REQUEST_2 = "<qdbapi><ticket>{0}</ticket><apptoken>{1}</apptoken><clist>{2}</clist><fmt>structured</fmt><options>num-1000.skp-{3}</options></qdbapi>";

    private static final String REQUESTP = "<qdbapi><ticket>{0}</ticket><apptoken>{1}</apptoken><clist>{2}</clist><query>{3}</query><fmt>structured</fmt><options>num-1000</options></qdbapi>";
    private static final String REQUESTP_2 = "<qdbapi><ticket>{0}</ticket><apptoken>{1}</apptoken><clist>{2}</clist><query>{4}</query><fmt>structured</fmt><options>num-1000.skp-{3}</options></qdbapi>";

    private String databaseID;
    private boolean indexEnabled;
    private long weightsID;

    public boolean isIndexEnabled() {
        return indexEnabled;
    }

    public void setIndexEnabled(boolean indexEnabled) {
        this.indexEnabled = indexEnabled;
    }

    public long getWeightsID() {
        return weightsID;
    }

    public void setWeightsID(long weightsID) {
        this.weightsID = weightsID;
    }

    @Override
    public int getDataSourceType() {
        return DataSourceInfo.STORED_PULL;
    }

    @Override
    public List<AnalysisItem> createAnalysisItems(Map<String, Key> keys, Connection conn, FeedDefinition parentDefinition) {
        return new ArrayList<AnalysisItem>();
    }

    @Override
    public void customStorage(Connection conn) throws SQLException {
        super.customStorage(conn);
        PreparedStatement clearStmt = conn.prepareStatement("DELETE FROM QUICKBASE_DATA_SOURCE where data_source_id = ?");
        clearStmt.setLong(1, getDataFeedID());
        clearStmt.executeUpdate();
        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO QUICKBASE_DATA_SOURCE (DATA_SOURCE_ID, DATABASE_ID, support_index, weights_id) " +
                "VALUES (?, ?, ?, ?)");
        insertStmt.setLong(1, getDataFeedID());
        insertStmt.setString(2, databaseID);
        insertStmt.setBoolean(3, indexEnabled);
        insertStmt.setLong(4, weightsID);
        insertStmt.execute();
    }

    @Override
    public void customLoad(Connection conn) throws SQLException {
        super.customLoad(conn);
        PreparedStatement queryStmt = conn.prepareStatement("SELECT database_id, support_index, weights_id FROM " +
                "QUICKBASE_DATA_SOURCE where data_source_id = ?");
        queryStmt.setLong(1, getDataFeedID());
        ResultSet rs = queryStmt.executeQuery();
        rs.next();
        databaseID = rs.getString(1);
        indexEnabled = rs.getBoolean(2);
        weightsID = rs.getLong(3);
        queryStmt.close();
    }

    protected void clearData(DataStorage dataStorage) throws SQLException {
        if (databaseID.equals("beutk2zd6")) {
            Key dateKey = null;
            for (AnalysisItem analysisItem : getFields()) {
                if ("Date".equals(analysisItem.toDisplay())) {
                    dateKey = analysisItem.getKey();
                }
            }
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -60);
            Date boundaryTime = cal.getTime();
            IWhere where = new DateWhere(dateKey, boundaryTime, Comparison.GREATER_THAN);
            dataStorage.deleteData(Arrays.asList(where));
        } else {
            dataStorage.truncate();
        }
    }

    public String getDatabaseID() {
        return databaseID;
    }

    public void setDatabaseID(String databaseID) {
        this.databaseID = databaseID;
    }

    @Override
    public Feed createFeedObject(FeedDefinition parent) {
        return new QuickbaseFeed(databaseID);
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.QUICKBASE_CHILD;
    }

    @Override
    public int getRequiredAccountTier() {
        return Account.PROFESSIONAL;
    }

    @NotNull
    @Override
    protected List<String> getKeys(FeedDefinition parentDefinition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataSet getDataSet(Map<String, Key> keys, Date now, FeedDefinition parentDefinition, IDataStorage IDataStorage, EIConnection conn, String callDataID, Date lastRefreshDate) throws ReportException {
        QuickbaseCompositeSource quickbaseCompositeSource = (QuickbaseCompositeSource) parentDefinition;
        String sessionTicket = quickbaseCompositeSource.getSessionTicket();
        String applicationToken = quickbaseCompositeSource.getApplicationToken();
        String host = quickbaseCompositeSource.getHost();
        String fullPath = "https://" + host + "/db/" + databaseID;
        /*if (databaseID.equals("beutk2zd6")) {
            try {
                return acsDataLogRetrieval(IDataStorage, conn, quickbaseCompositeSource, sessionTicket, applicationToken, fullPath, lastRefreshDate);
            } catch (ReportException e) {
                if (quickbaseCompositeSource.isPreserveCredentials()) {
                    try {
                        quickbaseCompositeSource.exchangeTokens(conn, null, null);
                    } catch (Exception e1) {
                        throw new RuntimeException(e1);
                    }
                    return acsDataLogRetrieval(IDataStorage, conn, quickbaseCompositeSource, quickbaseCompositeSource.getSessionTicket(), applicationToken, fullPath, lastRefreshDate);
                } else {
                    throw e;
                }
            }
        } else {*/
            try {
                return normalRetrieval(IDataStorage, conn, quickbaseCompositeSource, sessionTicket, applicationToken, fullPath);
            } catch (ReportException e) {
                if (quickbaseCompositeSource.isPreserveCredentials()) {
                    try {
                        quickbaseCompositeSource.exchangeTokens(conn, null, null);
                    } catch (Exception e1) {
                        throw new RuntimeException(e1);
                    }
                    return normalRetrieval(IDataStorage, conn, quickbaseCompositeSource, quickbaseCompositeSource.getSessionTicket(), applicationToken, fullPath);
                } else {
                    throw e;
                }
            }
        //}
    }

    @Override
    protected boolean clearsData(FeedDefinition parentSource) {
        /*if (databaseID.equals("beutk2zd6")) {
            return false;
        } else {
            return true;
        }*/
        return true;
    }

    @Override
    protected String getUpdateKeyName() {
        return "beutk2zd6.3";
    }

    public boolean hasNewData(Date lastRefreshDate, FeedDefinition parent, EIConnection conn) {
        QuickbaseCompositeSource quickbaseCompositeSource = (QuickbaseCompositeSource) parent;
        String fullPath = "https://" + quickbaseCompositeSource.getHost() + "/db/" + databaseID;
        if (databaseID.equals("beutk2zd6")) {
            try {
                return checkForNewData(fullPath, quickbaseCompositeSource.getSessionTicket(),
                        quickbaseCompositeSource.getApplicationToken(), lastRefreshDate, quickbaseCompositeSource);
            } catch (Exception e) {
                try {
                    quickbaseCompositeSource.exchangeTokens(conn, null, null);
                    return checkForNewData(fullPath, quickbaseCompositeSource.getSessionTicket(),
                        quickbaseCompositeSource.getApplicationToken(), lastRefreshDate, quickbaseCompositeSource);
                } catch (Exception e1) {
                    throw new RuntimeException(e1);
                }
            }
        } else {
            return false;
        }
    }

    private boolean checkForNewData(String fullPath, String sessionTicket, String applicationToken, Date lastRefreshDate, QuickbaseCompositeSource quickbaseCompositeSource) throws IOException, ParsingException {
        HttpPost httpRequest = new HttpPost(fullPath);
        httpRequest.setHeader("Accept", "application/xml");
        httpRequest.setHeader("Content-Type", "application/xml");
        httpRequest.setHeader("QUICKBASE-ACTION", "API_DoQuery");
        BasicHttpEntity entity = new BasicHttpEntity();
        String columns = "3";
        System.out.println("looking for after " + lastRefreshDate);
        String query = ("{'2'.AF.'" + lastRefreshDate.getTime() + "'}");
        String requestBody = MessageFormat.format(REQUESTP, sessionTicket, applicationToken, columns, query);
        System.out.println(requestBody);
        byte[] contentBytes = requestBody.getBytes();
        entity.setContent(new ByteArrayInputStream(contentBytes));
        entity.setContentLength(contentBytes.length);
        httpRequest.setEntity(entity);
        HttpClient client = new DefaultHttpClient();
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String string = client.execute(httpRequest, responseHandler);
        Document doc = new Builder().build(new ByteArrayInputStream(string.getBytes("UTF-8")));
        Nodes errors = doc.query("/qdbapi/errcode/text()");
        if (errors.size() > 0) {
            Node error = errors.get(0);
            if (!"0".equals(error.getValue())) {
                String errorDetail = doc.query("/qdbapi/errdetail/text()").get(0).getValue();
                throw new ReportException(new DataSourceConnectivityReportFault(errorDetail, quickbaseCompositeSource));
            }
        }
        Nodes records = doc.query("/qdbapi/table/records/record");
        System.out.println("checking...");
        if (records.size() > 0) {
            System.out.println("found new record");
            return true;
        }
        System.out.println("no new records");
        return false;
    }

    private DataSet acsDataLogRetrieval(IDataStorage IDataStorage, EIConnection conn, QuickbaseCompositeSource quickbaseCompositeSource,
                                        String sessionTicket, String applicationToken, String fullPath, Date lastRefreshDate) {
        try {
            /*DataStorage readStorage = DataStorage.readConnection(getFields(), getDataFeedID());
            DataSet dataSet = readStorage.allData(new ArrayList<FilterDefinition>(), null);*/
            Feed weightsFeed = FeedRegistry.instance().getFeed(weightsID, conn);
            List<AnalysisItem> weightFields = weightsFeed.getFields();
            DataSet weights = weightsFeed.getAggregateDataSet(new HashSet<AnalysisItem>(weightFields), new ArrayList<FilterDefinition>(), new InsightRequestMetadata(), weightFields, false, conn);
            HttpPost httpRequest = new HttpPost(fullPath);
            httpRequest.setHeader("Accept", "application/xml");
            httpRequest.setHeader("Content-Type", "application/xml");
            httpRequest.setHeader("QUICKBASE-ACTION", "API_DoQuery");
            BasicHttpEntity entity = new BasicHttpEntity();


            AnalysisItem wtdProcedures = null;
            AnalysisItem hoursItem = null;
            StringBuilder columnBuilder = new StringBuilder();
            Map<String, AnalysisItem> map = new HashMap<String, AnalysisItem>();
            for (AnalysisItem analysisItem : getFields()) {
                if ("Wtd Procedures".equals(analysisItem.toDisplay())) {
                    wtdProcedures = analysisItem;
                } else if ("Hr".equals(analysisItem.toDisplay())) {
                    hoursItem = analysisItem;
                }
                if (analysisItem.getKey().indexed()) {
                    String fieldID = analysisItem.getKey().toBaseKey().toKeyString().split("\\.")[1];
                    map.put(fieldID, analysisItem);
                    columnBuilder.append(fieldID).append(".");
                }
            }
            columnBuilder.append("3.");

            int count;


            int masterCount;
            masterCount = 0;
            int newRows = 0;
            String query;
            if (lastRefreshDate == null) {
                query = "";
            } else {
                query = ("{'2'.AF.'" + lastRefreshDate.getTime() + "'}");
            }


            DataSet dataSet = new DataSet();
            do {
                count = 0;
                String requestBody;
                if (masterCount == 0) {
                    requestBody = MessageFormat.format(REQUESTP, sessionTicket, applicationToken, columnBuilder.toString(), query);
                } else {
                    requestBody = MessageFormat.format(REQUESTP_2, sessionTicket, applicationToken, columnBuilder.toString(), String.valueOf(masterCount), query);
                }
                byte[] contentBytes = requestBody.getBytes();

                entity.setContent(new ByteArrayInputStream(contentBytes));
                entity.setContentLength(contentBytes.length);
                httpRequest.setEntity(entity);
                HttpClient client = new DefaultHttpClient();
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String string = client.execute(httpRequest, responseHandler);
                Document doc = new Builder().build(new ByteArrayInputStream(string.getBytes("UTF-8")));

                Nodes errors = doc.query("/qdbapi/errcode/text()");
                if (errors.size() > 0) {
                    Node error = errors.get(0);
                    if (!"0".equals(error.getValue())) {
                        String errorDetail = doc.query("/qdbapi/errdetail/text()").get(0).getValue();
                        throw new ReportException(new DataSourceConnectivityReportFault(errorDetail, quickbaseCompositeSource));
                    }
                }



                Nodes records = doc.query("/qdbapi/table/records/record");
                for (int i = 0; i < records.size(); i++) {
                    newRows++;
                    Element record = (Element) records.get(i);
                    IRow row = dataSet.createRow();
                    count++;
                    masterCount++;
                    Elements childElements = record.getChildElements();
                    double hrOverride = 0;
                    double hrWkOverride = 0;
                    double hrPatientFD = 0;
                    double hrWeekPerFD = 0;
                    for (int j = 0; j < childElements.size(); j++) {
                        Element childElement = childElements.get(j);
                        if (childElement.getLocalName().equals("f")) {
                            String fieldID = childElement.getAttribute("id").getValue();
                            AnalysisItem analysisItem = map.get(fieldID);
                            if (analysisItem == null) {
                                continue;
                            }
                            String value = childElement.getValue();
                            if ("445".equals(fieldID)) {
                                try {
                                    hrOverride = Double.parseDouble(value);
                                } catch (NumberFormatException e) {
                                }
                            } else if ("446".equals(fieldID)) {
                                try {
                                    hrWkOverride = Double.parseDouble(value);
                                } catch (NumberFormatException e) {
                                }
                            } else if ("109".equals(fieldID)) {
                                try {
                                    hrPatientFD = Double.parseDouble(value);
                                } catch (NumberFormatException e) {
                                }
                            } else if ("112".equals(fieldID)) {
                                try {
                                    hrWeekPerFD = Double.parseDouble(value);
                                } catch (NumberFormatException e) {
                                }
                            }
                            if (analysisItem.hasType(AnalysisItemTypes.DATE_DIMENSION) && !"".equals(value)) {
                                Date shiftedDate = new Date(Long.parseLong(value));
                                row.addValue(analysisItem.createAggregateKey(), shiftedDate);
                            } else {
                                row.addValue(analysisItem.createAggregateKey(), value);
                            }
                        }
                    }
                    double hours;
                    if (hrOverride > 0) {
                        hours = hrOverride;
                    } else if (hrWkOverride > 0) {
                        hours = hrWkOverride;
                    } else if (hrPatientFD > 0) {
                        hours = hrPatientFD;
                    } else {
                        hours = hrWeekPerFD;
                    }
                    row.addValue(hoursItem.createAggregateKey(), hours);
                }

                if (newRows > 0) {
                    Pipeline pipeline = new ACSPipeline();
                    WSListDefinition analysisDefinition = new WSListDefinition();
                    List<AnalysisItem> columns = new ArrayList<AnalysisItem>(map.values());

                    for (IRow weightRow : weights.getRows()) {
                        for (AnalysisItem weightField : weightFields) {
                            if (weightField.hasType(AnalysisItemTypes.MEASURE)) {
                                for (IRow row : dataSet.getRows()) {
                                    row.addValue(weightField.createAggregateKey(), weightRow.getValue(weightField));
                                }
                            }
                        }
                    }
                    columns.addAll(weightFields);

                    AnalysisCalculation cachedWeightedProcedures = new AnalysisCalculation();
                    cachedWeightedProcedures.setKey(new NamedKey("Calc Wtd Procedures"));
                    cachedWeightedProcedures.setCalculationString("([97001-PT/OT] * [97001WPT/OT])+([97002-PT/OT] * [97002WPT/OT])+([97003-PT/OT] * [97003WPT/OT])+([97004-PT/OT] * [97004WPT/OT])+([97010-PT/OT] * [97010WPT/OT])+([97012-PT/OT] * [97012WPT/OT])+([97014-PT/OT] * [97014WPT/OT])+([97016-PT/OT] * [97016WPT/OT])+([97018-PT/OT] * [97018WPT/OT])+([97022-PT/OT] * [97022WPT/OT])+([97024-PT/OT] * [97024WPT/OT])+([97026-PT/OT] * [97026WPT/OT])+([97032-PT/OT] * [97032WPT/OT])+([97033-PT/OT] * [97033WPT/OT])+([97034-PT/OT] * [97034WPT/OT])+([97035-PT/OT] * [97035WPT/OT])+([97036-PT/OT] * [97036WPT/OT])+([97039-PT/OT] * [97039WPT/OT])+([97110-PT/OT] * [97110WPT/OT])+([97112-PT/OT] * [97112WPT/OT])+([97113-PT/OT] * [97113WPT/OT])+([97116-PT/OT] * [97116WPT/OT])+([97124-PT/OT] * [97124WPT/OT])+([97139-PT/OT] * [97139WPT/OT])+([97140-PT/OT] * [97140WPT/OT])+([97150-PT/OT] * [97150WPT/OT])+([97530-PT/OT] * [97530WPT/OT])+([97532-PT/OT] * [97532WPT/OT])+([97533-PT/OT] * [97533WPT/OT])+([97535-PT/OT] * [97535WPT/OT])+([97537-PT/OT] * [97537WPT/OT])+([97542-PT/OT] * [97542WPT/OT])+([97545-PT/OT] * [97545WPT/OT])+([97546-PT/OT] * [97546WPT/OT])+([97597-PT/OT] * [97597WPT/OT])+([97598-PT/OT] * [97598WPT/OT])+([97602-PT/OT] * [97602WPT/OT])+([97605-PT/OT] * [97605WPT/OT])+([97606-PT/OT] * [97606WPT/OT])+([97750-PT/OT] * [97750WPT/OT])+([97760-PT/OT] * [97760WPT/OT])+([97761-PT/OT] * [97761WPT/OT])+([97762-PT/OT] * [97762WPT/OT])+([95831-PT/OT] * [95831WPT/OT])+([95832-PT/OT] * [95832WPT/OT])+([95852-PT/OT] * [95852WPT/OT])+([95900-PT/OT] * [95900WPT/OT])+([PT100-PT/OT] * [PT100WPT/OT])+([90911-PT/OT] * [90911WPT/OT])+([G0283-PT/OT] * [G0283WPT/OT])+([16020-PT/OT] * [16020WPT/OT])+([16025-PT/OT] * [16025WPT/OT])+([64550-PT/OT] * [64550WPT/OT])+([29105-PT/OT] * [29105WPT/OT])+([29125-PT/OT] * [29125WPT/OT])+([29126-PT/OT] * [29126WPT/OT])+([29130-PT/OT] * [29130WPT/OT])+([29131-PT/OT] * [29131WPT/OT])+([29200-PT/OT] * [29200WPT/OT])+([29220-PT/OT] * [29220WPT/OT])+([29240-PT/OT] * [29240WPT/OT])+([29260-PT/OT] * [29260WPT/OT])+([29280-PT/OT] * [29280WPT/OT])+([29505-PT/OT] * [29505WPT/OT])+([29515-PT/OT] * [29515WPT/OT])+([29520-PT/OT] * [29520WPT/OT])+([29530-PT/OT] * [29530WPT/OT])+([29540-PT/OT] * [29540WPT/OT])+([29550-PT/OT] * [29550WPT/OT])+([29580-PT/OT] * [29580WPT/OT])+([TTP-PT/OT] * [TTPWPT/OT])+([L Codes-Orthotics] * [L CodesWOrthotics])+([L1846-Orthotics] * [L1846WOrthotics])+([L1850-Orthotics] * [L1850WOrthotics])+([L1900-Orthotics] * [L1900WOrthotics])+([L1901-Orthotics] * [L1901WOrthotics])+([L1902-Orthotics] * [L1902WOrthotics])+([L1904-Orthotics] * [L1904WOrthotics])+([L1906-Orthotics] * [L1906WOrthotics])+([L1907-Orthotics] * [L1907WOrthotics])+([L1910-Orthotics] * [L1910WOrthotics])+([L1920-Orthotics] * [L1920WOrthotics])+([L1930-Orthotics] * [L1930WOrthotics])+([L1906-Orthotics] * [L1906WOrthotics])+([L3700-Orthotics] * [L3700WOrthotics])+([L3701-Orthotics] * [L3701WOrthotics])+([L3702-Orthotics] * [L3702WOrthotics])+([L3710-Orthotics] * [L3710WOrthotics])+([L3720-Orthotics] * [L3720WOrthotics])+([L3730-Orthotics] * [L3730WOrthotics])+([L3740-Orthotics] * [L3740WOrthotics])+([L3760-Orthotics] * [L3760WOrthotics])+([L3762-Orthotics] * [L3762WOrthotics])+([L3763-Orthotics] * [L3763WOrthotics])+([L3764-Orthotics] * [L3764WOrthotics])+([L3765-Orthotics] * [L3765WOrthotics])+([L3766-Orthotics] * [L3766WOrthotics])+([L3806-Orthotics] * [L3806WOrthotics])+([L3807-Orthotics] * [L3807WOrthotics])+([L3808-Orthotics] * [L3808WOrthotics])+([L3900-Orthotics] * [L3900WOrthotics])+([L3901-Orthotics] * [L3901WOrthotics])+([L3905-Orthotics] * [L3905WOrthotics])+([L3906-Orthotics] * [L3906WOrthotics])+([L3908-Orthotics] * [L3908WOrthotics])+([L3909-Orthotics] * [L3909WOrthotics])+([L3911-Orthotics] * [L3911WOrthotics])+([L3912-Orthotics] * [L3912WOrthotics])+([L3913-Orthotics] * [L3913WOrthotics])+([L3915-Orthotics] * [L3915WOrthotics])+([L3917-Orthotics] * [L3917WOrthotics])+([L3919-Orthotics] * [L3919WOrthotics])+([L3921-Orthotics] * [L3921WOrthotics])+([L3923-Orthotics] * [L3923WOrthotics])+([L3925-Orthotics] * [L3925WOrthotics])+([L3927-Orthotics] * [L3927WOrthotics])+([L3929-Orthotics] * [L3929WOrthotics])+([L3931-Orthotics] * [L3931WOrthotics])+([L3933-Orthotics] * [L3933WOrthotics])+([L3935-Orthotics] * [L3935WOrthotics])+([L3956-Orthotics] * [L3956WOrthotics])+([L4350-Orthotics] * [L4350WOrthotics])+([Q4049-Orthotics] * [Q4049WOrthotics])+([S8450-Orthotics] * [S8450WOrthotics])+([S8452-Orthotics] * [S8452WOrthotics])+([S8451-Orthotics] * [S8451WOrthotics])+([98770-CA WC] * [98770WCA WC])+([98771-CA WC] * [98771WCA WC])+([98772-CA WC] * [98772WCA WC])+([98773-CA WC] * [98773WCA WC])+([98774-CA WC] * [98774WCA WC])+([98775-CA WC] * [98775WCA WC])+([98776-CA WC] * [98776WCA WC])+([98777-CA WC] * [98777WCA WC])+([98778-CA WC] * [98778WCA WC])+([95831-CA WC] * [95831WCA WC])+([95832-CA WC] * [95832WCA WC])+([95851-CA WC] * [95851WCA WC])+([95852-CA WC] * [95852WCA WC])+([97690-CA WC] * [97690WCA WC])+([97691-CA WC] * [97691WCA WC])+([11000-CA WC] * [11000WCA WC])+([95900-CA WC] * [95900WCA WC])+([95904-CA WC] * [95904WCA WC])+([97012-CA WC] * [97012WCA WC])+([97014-CA WC] * [97014WCA WC])+([97016-CA WC] * [97016WCA WC])+([97018-CA WC] * [97018WCA WC])+([97022-CA WC] * [97022WCA WC])+([97250-CA WC] * [97250WCA WC])+([97110-CA WC] * [97110WCA WC])+([97112-CA WC] * [97112WCA WC])+([97116-CA WC] * [97116WCA WC])+([97118-CA WC] * [97118WCA WC])+([97120-CA WC] * [97120WCA WC])+([97124-CA WC] * [97124WCA WC])+([97126-CA WC] * [97126WCA WC])+([97128-CA WC] * [97128WCA WC])+([97145-CA WC] * [97145WCA WC])+([97240-CA WC] * [97240WCA WC])+([97241-CA WC] * [97241WCA WC])+([97500-CA WC] * [97500WCA WC])+([97501-CA WC] * [97501WCA WC])+([97530-CA WC] * [97530WCA WC])+([97531-CA WC] * [97531WCA WC])+([97540-CA WC] * [97540WCA WC])+([97541-CA WC] * [97541WCA WC])+([97614-CA WC] * [97614WCA WC])+([97616-CA WC] * [97616WCA WC])+([97700-CA WC] * [97700WCA WC])+([x3900-MediCal] * [x3900WMediCal])+([x3902-MediCal] * [x3902WMediCal])+([x3904-MediCal] * [x3904WMediCal])+([x3906-MediCal] * [x3906WMediCal])+([x3908-MediCal] * [x3908WMediCal])+([x3910-MediCal] * [x3910WMediCal])+([x3920-MediCal] * [x3920WMediCal])+([x3922-MediCal] * [x3922WMediCal])+([92506-SPEECH] * [92506WSPEECH])+([92507-SPEECH] * [92507WSPEECH])+([92508-SPEECH] * [92508WSPEECH])+([92511-SPEECH] * [92511WSPEECH])+([92520-SPEECH] * [92520WSPEECH])+([92626-SPEECH] * [92626WSPEECH])+([92627-SPEECH] * [92627WSPEECH])+([92630-SPEECH] * [92630WSPEECH])+([92633-SPEECH] * [92633WSPEECH])+([96105-SPEECH] * [96105WSPEECH])+([96110-SPEECH] * [96110WSPEECH])+([96125-SPEECH] * [96125WSPEECH])+([31575-SPEECH] * [31575WSPEECH])+([31579-SPEECH] * [31579WSPEECH])+([92526-SPEECH] * [92526WSPEECH])+([92610-SPEECH] * [92610WSPEECH])+([92611-SPEECH] * [92611WSPEECH])+([92613-SPEECH] * [92613WSPEECH])+([92616-SPEECH] * [92616WSPEECH])+([92617-SPEECH] * [92617WSPEECH])+([92597-SPEECH] * [92597WSPEECH])+([92605-SPEECH] * [92605WSPEECH])+([92606-SPEECH] * [92606WSPEECH])+([92607-SPEECH] * [92607WSPEECH])+([92608-SPEECH] * [92608WSPEECH])+([92609-SPEECH] * [92609WSPEECH])+([V5336-SPEECH] * [V5336WSPEECH])+([92700-SPEECH] * [92700WSPEECH])+([98966-SPEECH] * [98966WSPEECH])+([98967-SPEECH] * [98967WSPEECH])+([98968-SPEECH] * [98968WSPEECH])+([98969-SPEECH] * [98969WSPEECH])+([99366-SPEECH] * [99366WSPEECH])+([99368-SPEECH] * [99368WSPEECH])");
                    columns.add(cachedWeightedProcedures);

                    analysisDefinition.setColumns(columns);

                    pipeline.setup(analysisDefinition, new InsightRequestMetadata(), columns);
                    pipeline.getPipelineData().getAllItems().addAll(weightFields);
                    dataSet = pipeline.toDataSet(dataSet);
                    for (AnalysisItem analysisItem : map.values()) {
                        dataSet.getDataSetKeys().replaceKey(analysisItem.createAggregateKey(), analysisItem.getKey());
                    }
                    for (IRow row : dataSet.getRows()) {
                        row.addValue(wtdProcedures.getKey(), row.getValue(cachedWeightedProcedures.createAggregateKey()));
                    }
                }

                if (lastRefreshDate == null) {
                    IDataStorage.insertData(dataSet);
                    dataSet = new DataSet();
                }

            } while (count == 1000);



            if (lastRefreshDate == null) {
//                IDataStorage.insertData(dataSet);
            } else {

                Key noteKey = quickbaseCompositeSource.getField("beutk2zd6.3").toBaseKey();

                for (IRow row : dataSet.getRows()) {
                    StringWhere userWhere = new StringWhere(noteKey, row.getValue(noteKey).toString());
                    IDataStorage.updateData(row, Arrays.asList((IWhere) userWhere));
                }
            }
            //dataSet = new DataSet();
            //}
            return null;
        } catch (ReportException re) {
            throw re;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    private DataSet normalRetrieval(IDataStorage IDataStorage, EIConnection conn, QuickbaseCompositeSource quickbaseCompositeSource, String sessionTicket, String applicationToken, String fullPath) {
        HttpPost httpRequest = new HttpPost(fullPath);
        httpRequest.setHeader("Accept", "application/xml");
        httpRequest.setHeader("Content-Type", "application/xml");
        httpRequest.setHeader("QUICKBASE-ACTION", "API_DoQuery");
        BasicHttpEntity entity = new BasicHttpEntity();
        StringBuilder columnBuilder = new StringBuilder();
        Map<String, Collection<AnalysisItem>> map = new HashMap<String, Collection<AnalysisItem>>();
        for (AnalysisItem analysisItem : getFields()) {
            if (analysisItem.hasType(AnalysisItemTypes.CALCULATION)) {
                continue;
            }
            if (analysisItem.getKey().indexed()) {
                String fieldID = analysisItem.getKey().toBaseKey().toKeyString().split("\\.")[1];
                Collection<AnalysisItem> items = map.get(fieldID);
                if (items == null) {
                    items = new ArrayList<AnalysisItem>(1);
                    map.put(fieldID, items);
                }
                items.add(analysisItem);
                columnBuilder.append(fieldID).append(".");
            }
        }
        DataSet dataSet = new DataSet();
        if (map.size() == 0) {
            return dataSet;
        }
        int count;
        int masterCount = 0;

        try {
            do {
                count = 0;
                String requestBody;
                if (masterCount == 0) {
                    requestBody = MessageFormat.format(REQUEST, sessionTicket, applicationToken, columnBuilder.toString());
                } else {
                    requestBody = MessageFormat.format(REQUEST_2, sessionTicket, applicationToken, columnBuilder.toString(), String.valueOf(masterCount));
                }
                System.out.println(requestBody);
                byte[] contentBytes = requestBody.getBytes();
                entity.setContent(new ByteArrayInputStream(contentBytes));
                entity.setContentLength(contentBytes.length);
                httpRequest.setEntity(entity);
                HttpClient client = new DefaultHttpClient();
                ResponseHandler<String> responseHandler = new BasicResponseHandler();

                String string = client.execute(httpRequest, responseHandler);
                Document doc = new Builder().build(new ByteArrayInputStream(string.getBytes("UTF-8")));

                Nodes errors = doc.query("/qdbapi/errcode/text()");
                if (errors.size() > 0) {
                    Node error = errors.get(0);
                    if (!"0".equals(error.getValue())) {
                        String errorDetail = doc.query("/qdbapi/errdetail/text()").get(0).getValue();
                        throw new ReportException(new DataSourceConnectivityReportFault(errorDetail, quickbaseCompositeSource));
                    }
                }

                Nodes records = doc.query("/qdbapi/table/records/record");
                for (int i = 0; i < records.size(); i++) {
                    Element record = (Element) records.get(i);
                    IRow row = dataSet.createRow();
                    count++;
                    masterCount++;
                    Elements childElements = record.getChildElements();
                    for (int j = 0; j < childElements.size(); j++) {
                        Element childElement = childElements.get(j);
                        if (childElement.getLocalName().equals("f")) {
                            String fieldID = childElement.getAttribute("id").getValue();
                            Collection<AnalysisItem> items = map.get(fieldID);
                            for (AnalysisItem analysisItem : items) {    
                                String value = childElement.getValue();
                                if (analysisItem.hasType(AnalysisItemTypes.DATE_DIMENSION) && !"".equals(value)) {
                                    row.addValue(analysisItem.getKey(), new Date(Long.parseLong(value)));
                                } else {
                                    row.addValue(analysisItem.getKey(), value);
                                }
                            }
                        }
                    }
                }
                IDataStorage.insertData(dataSet);
                dataSet = new DataSet();
                System.out.println("For " + getFeedName() + " count = " + count);
            } while (count == 1000);
            return null;
        } catch (ReportException re) {
            throw re;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }
}
