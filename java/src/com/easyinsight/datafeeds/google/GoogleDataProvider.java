package com.easyinsight.datafeeds.google;

import com.easyinsight.webservice.google.Worksheet;
import com.easyinsight.datafeeds.*;
import com.easyinsight.users.Credentials;
import com.easyinsight.userupload.UserUploadService;
import com.easyinsight.userupload.UploadPolicy;
import com.easyinsight.userupload.UploadResponse;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.dataset.ColumnSegmentFactory;
import com.easyinsight.dataset.PersistableDataSetForm;
import com.easyinsight.IRow;
import com.easyinsight.AnalysisItem;
import com.easyinsight.stream.google.IDataTypeGuesser;
import com.easyinsight.stream.google.DataTypeGuesser;
import com.easyinsight.logging.LogClass;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.security.Roles;
import com.easyinsight.analysis.*;
import com.easyinsight.core.*;
import com.easyinsight.database.Database;
import com.easyinsight.storage.DataRetrievalManager;
import com.easyinsight.storage.TableDefinitionMetadata;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URL;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * User: jboe
 * Date: Jan 6, 2008
 * Time: 7:44:31 PM
 */
public class GoogleDataProvider implements IGoogleStream {

    private Map<Credentials, Worksheet[]> cachedSpreadsheetResults = new WeakHashMap<Credentials, Worksheet[]>();
    
    private static GoogleDataProvider instance = null;
    private FeedStorage feedStorage = new FeedStorage();

    public GoogleDataProvider() {
        instance = this;
    }

    public FeedType getDataFeedType() {
        return FeedType.GOOGLE;
    }

    public static GoogleDataProvider instance() {
        return instance;
    }

    public boolean testGoogleConnect(Credentials credentials) {
        boolean success = true;
        try {
            GoogleSpreadsheetAccess.getOrCreateSpreadsheetService(credentials);
        } catch (Exception e) {
            LogClass.error(e);
            success = false;
        }
        return success;
    }

    public FeedDescriptor createFeed(Credentials credentials, String title, String url) {
        Connection conn = Database.instance().getConnection();
        TableDefinitionMetadata tableDef = null;
        try {
            GoogleFeedDefinition googleFeedDefinition = new GoogleFeedDefinition();
            googleFeedDefinition.setWorksheetURL(url);
            DataSet dataSet = createDataSet(credentials, url);
            googleFeedDefinition.setFeedName(title);
            googleFeedDefinition.setFields(populateFields(dataSet));
            googleFeedDefinition.setUploadPolicy(new UploadPolicy(SecurityUtil.getUserID()));
            FeedCreationResult result = new FeedCreation().createFeed(googleFeedDefinition, conn, dataSet, SecurityUtil.getUserID());
            tableDef = result.getTableDefinitionMetadata();
            tableDef.commit();
            FeedDescriptor feedDescriptor = new FeedDescriptor();
            feedDescriptor.setPolicy(new UploadPolicy(SecurityUtil.getUserID()));
            feedDescriptor.setDataFeedID(googleFeedDefinition.getDataFeedID());
            new UserUploadService().createUserFeedLink(SecurityUtil.getUserID(), googleFeedDefinition.getDataFeedID(), Roles.OWNER);
            return feedDescriptor;
        } catch (Exception e) {
            LogClass.error(e);
            if (tableDef != null) {
                tableDef.rollback();
            }
            throw new RuntimeException(e);
        } finally {
            Database.instance().closeConnection(conn);
        }
    }

    private List<AnalysisItem> populateFields(DataSet dataSet) {
        IDataTypeGuesser guesser = new DataTypeGuesser();
        for (IRow row : dataSet.getRows()) {
            for (Key key : row.getKeys()) {
                Value value = row.getValue(key);
                if (value == null) {
                    value = new EmptyValue();
                }
                guesser.addValue(key, value);
            }
        }
        return guesser.createFeedItems();
    }

    public Worksheet[] getAvailableGoogleSpreadsheets(Credentials credentials) {
        Worksheet[] spreadsheetArray = cachedSpreadsheetResults.get(credentials);
        if (spreadsheetArray == null) {
            try {
                Collection<Worksheet> worksheets = getSpreadsheets(credentials);
                spreadsheetArray = new Worksheet[worksheets.size()];
                worksheets.toArray(spreadsheetArray);
                cachedSpreadsheetResults.put(credentials, spreadsheetArray);
                return spreadsheetArray;
            } catch (Exception e) {
                LogClass.error(e);
                throw new RuntimeException(e);
            }
        }
        return spreadsheetArray;
    }

    public static DataSet createDataSet(Credentials credentials, String url) {
        DataSet dataSet;
        try {
            SpreadsheetService myService = GoogleSpreadsheetAccess.getOrCreateSpreadsheetService(credentials);
            URL listFeedUrl = new URL(url);
            ListFeed feed = myService.getFeed(listFeedUrl, ListFeed.class);
            dataSet = new DataSet();
            for (ListEntry listEntry : feed.getEntries()) {
                IRow row = dataSet.createRow();
                boolean atLeastOneValue = false;
                for (String tag : listEntry.getCustomElements().getTags()) {
                    Value value;
                    String string = listEntry.getCustomElements().getValue(tag);
                    if (string == null) {
                        value = new EmptyValue();
                    } else {
                        if (string.length() > 0) {
                            atLeastOneValue = true;
                        }
                        value = new StringValue(string);
                    }
                    row.addValue(new NamedKey(tag), value);
                }
                if (!atLeastOneValue) {
                    dataSet.removeRow(row);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
        return dataSet;
    }

    private List<Worksheet> getSpreadsheets(Credentials credentials) throws AuthenticationException {
        List<Worksheet> worksheets = new ArrayList<Worksheet>();
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement existsStmt = conn.prepareStatement("SELECT DATA_FEED.DATA_FEED_ID, WORKSHEETURL FROM GOOGLE_FEED, DATA_FEED, UPLOAD_POLICY_USERS " +
                    "WHERE GOOGLE_FEED.DATA_FEED_ID = DATA_FEED.DATA_FEED_ID AND UPLOAD_POLICY_USERS.FEED_ID = " +
                    "UPLOAD_POLICY_USERS.FEED_ID AND USER_ID = ? AND UPLOAD_POLICY_USERS.USER_ID = ?");
            existsStmt.setLong(1, SecurityUtil.getUserID());
            existsStmt.setInt(2, Roles.OWNER);
            ResultSet rs = existsStmt.executeQuery();
            Map<String, FeedDescriptor> worksheetToFeedMap = new HashMap<String, FeedDescriptor>();
            while (rs.next()) {
                long dataFeedID = rs.getLong(1);
                String worksheetURL = rs.getString(2);
                FeedDescriptor feedDescriptor = new FeedDescriptor();
                feedDescriptor.setDataFeedID(dataFeedID);
                worksheetToFeedMap.put(worksheetURL, feedDescriptor);
            }
            existsStmt.close();            
            URL feedUrl = new URL("http://spreadsheets.google.com/feeds/spreadsheets/private/full");
            SpreadsheetService myService = GoogleSpreadsheetAccess.getOrCreateSpreadsheetService(credentials);
            SpreadsheetFeed spreadsheetFeed = myService.getFeed(feedUrl, SpreadsheetFeed.class);
            for (SpreadsheetEntry entry : spreadsheetFeed.getEntries()) {
                List<WorksheetEntry> worksheetEntries = entry.getWorksheets();
                for (WorksheetEntry worksheetEntry : worksheetEntries) {
                    String title = worksheetEntry.getTitle().getPlainText();
                    Worksheet worksheet = new Worksheet();
                    worksheet.setSpreadsheet(entry.getTitle().getPlainText());
                    worksheet.setTitle(title);
                    String url = worksheetEntry.getListFeedUrl().toString();
                    worksheet.setUrl(url);
                    worksheet.setFeedDescriptor(worksheetToFeedMap.get(url));
                    worksheets.add(worksheet);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.instance().closeConnection(conn);
        }
        return worksheets;
    }

    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");
        Matcher matcher = pattern.matcher("   � 3 months Ending 2008-07-26");
        System.out.println(matcher.find());
    }
}
