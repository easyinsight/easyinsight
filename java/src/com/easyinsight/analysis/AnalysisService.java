package com.easyinsight.analysis;

import com.easyinsight.analysis.definitions.WSStackedBarChartDefinition;
import com.easyinsight.analysis.definitions.WSStackedColumnChartDefinition;
import com.easyinsight.calculations.*;
import com.easyinsight.calculations.generated.CalculationsParser;
import com.easyinsight.calculations.generated.CalculationsLexer;
import com.easyinsight.core.*;
import com.easyinsight.dashboard.Dashboard;
import com.easyinsight.dashboard.DashboardDescriptor;
import com.easyinsight.dashboard.DashboardStorage;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.*;
import com.easyinsight.datafeeds.composite.FederatedDataSource;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.intention.Intention;
import com.easyinsight.intention.IntentionSuggestion;
import com.easyinsight.preferences.ApplicationSkin;
import com.easyinsight.preferences.ApplicationSkinSettings;
import com.easyinsight.security.*;
import com.easyinsight.security.SecurityException;
import com.easyinsight.logging.LogClass;
import com.easyinsight.database.Database;
import com.easyinsight.cache.Cache;

import java.io.ByteArrayInputStream;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import com.easyinsight.solutions.SolutionService;
import com.easyinsight.storage.CachedCalculationTransform;
import com.easyinsight.storage.DataStorage;
import com.easyinsight.storage.DatabaseManager;
import com.easyinsight.storage.IDataTransform;
import nu.xom.Builder;
import nu.xom.Document;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.apache.jcs.access.exception.CacheException;

/**
 * User: James Boe
 * Date: Jan 10, 2008
 * Time: 7:32:24 PM
 */
public class AnalysisService {

    private AnalysisStorage analysisStorage = new AnalysisStorage();

    /*public String generateDescription(WSAnalysisDefinition report) {
        try {
            return report.generateDescription();
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }*/

    public void updateReportStylings(List<ReportProperty> reportProperties) {
        EIConnection conn = Database.instance().getConnection();
        try {

            for (ReportProperty reportProperty : reportProperties) {

            }
            PreparedStatement reportStmt = conn.prepareStatement("SELECT ANALYSIS_ID FROM USER_TO_ANALYSIS WHERE USER_ID = ?");
            reportStmt.setLong(1, SecurityUtil.getUserID());
            ResultSet rs = reportStmt.executeQuery();
            while (rs.next()) {
                long reportID = rs.getLong(1);
                AnalysisDefinition analysisDefinition = analysisStorage.getPersistableReport(reportID, conn);
                List<ReportProperty> reportPropertyList = analysisDefinition.getProperties();
                for (ReportProperty reportProperty : reportPropertyList) {

                }
            }

        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public List<Revision> showHistory(String urlKey) {
        List<Revision> revisions = new ArrayList<Revision>();
        EIConnection conn = Database.instance().getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT REPORT_HISTORY_ID, INSERT_TIME FROM REPORT_HISTORY WHERE URL_KEY = ?");
            stmt.setString(1, urlKey);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                long historyID = rs.getLong(1);
                Date insertTime = new Date(rs.getTimestamp(2).getTime());
                Revision revision = new Revision();
                revision.setId(historyID);
                revision.setDate(insertTime);
                revisions.add(revision);
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
        return revisions;
    }

    public WSAnalysisDefinition fromHistory(long id) {
        WSAnalysisDefinition returnReport = null;
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            PreparedStatement stmt = conn.prepareStatement("SELECT REPORT_XML FROM REPORT_HISTORY WHERE REPORT_HISTORY_ID = ?");
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            String xml = rs.getString(1);
            XMLImportMetadata xmlImportMetadata = new XMLImportMetadata();
            xmlImportMetadata.setConn(conn);
            Document doc = new Builder().build(new ByteArrayInputStream(xml.getBytes()));
            AnalysisDefinition report = AnalysisDefinition.fromXML(doc.getRootElement(), xmlImportMetadata);
            new AnalysisStorage().saveAnalysis(report, conn);
            conn.commit();
            returnReport = report.createBlazeDefinition();
        } catch (Exception e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
        return returnReport;
    }

    public ReportInfo getReportInfo(long reportID) {
        return getReportInfo(reportID, false);
    }

    public ReportInfo getReportInfo(long reportID, boolean obtainHeader) {
        try {
            SecurityUtil.authorizeInsight(reportID);
        } catch (Exception e) {
            ReportInfo reportInfo = new ReportInfo();
            reportInfo.setAccessDenied(true);
            return reportInfo;
        }
        try {
            WSAnalysisDefinition report = openAnalysisDefinition(reportID);
            boolean dataSourceAccessible;
            try {
                SecurityUtil.authorizeFeedAccess(report.getDataFeedID());
                dataSourceAccessible = true;
            } catch (Exception e) {
                dataSourceAccessible = false;
            }
            ReportInfo reportInfo = new ReportInfo();
            if (obtainHeader) {
                EIConnection conn = Database.instance().getConnection();
                Session session = Database.instance().createSession(conn);
                try {
                    PreparedStatement stmt = conn.prepareStatement("SELECT USER.USER_ID, USER.ACCOUNT_ID FROM USER_TO_ANALYSIS, USER WHERE USER_TO_ANALYSIS.ANALYSIS_ID = ? AND " +
                            "USER_TO_ANALYSIS.USER_ID = USER.USER_ID");
                    stmt.setLong(1, report.getAnalysisID());
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        long userID = rs.getLong(1);
                        long accountID = rs.getLong(2);
                        ApplicationSkin skin = ApplicationSkinSettings.retrieveSkin(userID, session, accountID);
                        reportInfo.setHeaderImage(skin.getReportHeaderImage());
                        reportInfo.setBackgroundColor(skin.getReportBackgroundColor());
                        reportInfo.setTextColor(skin.getReportTextColor());
                    }

                } finally {
                    session.close();
                    Database.closeConnection(conn);
                }

            }

            reportInfo.setAdmin(dataSourceAccessible);
            reportInfo.setReport(report);
            return reportInfo;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public List<JoinOverride> generateForAddons(List<JoinOverride> existingOverrides, long dataSourceID, List<AnalysisItem> items,
                                                List<AddonReport> newAddonReports, List<AddonReport> removedAddonReports) {
        if (existingOverrides == null || existingOverrides.size() == 0) {
            existingOverrides = new ArrayList<JoinOverride>();
            ReportJoins reportJoins = determineOverrides(dataSourceID, items);
            for (List<JoinOverride> overrides : reportJoins.getJoinOverrideMap().values()) {
                existingOverrides.addAll(overrides);
            }
        }
        EIConnection conn = Database.instance().getConnection();
        try {
            for (AddonReport newAddonReport : newAddonReports) {
                // get fields for addon report
                List<AnalysisItem> addonFields = new ArrayList<AnalysisItem>();
                for (AnalysisItem item : items) {
                    Key key = item.getKey();
                    if (key.hasReport(newAddonReport.getReportID())) {
                        addonFields.add(item);
                    }
                }
                int dimensionCount = 0;
                AnalysisItem dimension = null;
                for (AnalysisItem item : addonFields) {
                    if (item.getType() == AnalysisItemTypes.DIMENSION) {
                        dimensionCount++;
                        dimension = item;
                    }
                }
                if (dimensionCount == 1) {
                    ReportKey key = (ReportKey) dimension.getKey();
                    Key parentKey = key.getParentKey();
                    AnalysisItem matchItem = null;
                    for (AnalysisItem item : items) {
                        if (item.getType() == AnalysisItemTypes.DIMENSION && item.getKey().equals(parentKey)) {
                            matchItem = item;
                            break;
                        }
                    }
                    if (matchItem != null) {
                        JoinOverride joinOverride = new JoinOverride();
                        joinOverride.setDataSourceID(dataSourceID);
                        joinOverride.setSourceItem(matchItem);
                        joinOverride.setTargetItem(dimension);
                        existingOverrides.add(joinOverride);
                    }
                }
            }
            return existingOverrides;
        } finally {
            Database.closeConnection(conn);
        }
    }

    public ReportJoins determineOverrides(long dataSourceID, List<AnalysisItem> items) {
        SecurityUtil.authorizeFeedAccess(dataSourceID);
        ReportJoins reportJoins = new ReportJoins();
        EIConnection conn = Database.instance().getConnection();
        try {
            FeedDefinition dataSource = new FeedStorage().getFeedDefinitionData(dataSourceID, conn);
            if (dataSource instanceof CompositeFeedDefinition) {
                CompositeFeedDefinition compositeFeedDefinition = (CompositeFeedDefinition) dataSource;
                Map<String, List<JoinOverride>> map = new HashMap<String, List<JoinOverride>>();
                Map<String, List<DataSourceDescriptor>> dataSourceMap = new HashMap<String, List<DataSourceDescriptor>>();
                List<DataSourceDescriptor> configurableDataSources = new ArrayList<DataSourceDescriptor>();
                populateMap(map, dataSourceMap, configurableDataSources, compositeFeedDefinition, items, conn);
                reportJoins.setJoinOverrideMap(map);
                reportJoins.setDataSourceMap(dataSourceMap);
                reportJoins.setConfigurableDataSources(configurableDataSources);
                reportJoins.setDataSourceAddonReports(dataSource.getAddonReports());
            } else if (dataSource instanceof FederatedDataSource) {
                FederatedDataSource federatedDataSource = (FederatedDataSource) dataSource;
                Map<String, List<JoinOverride>> map = new HashMap<String, List<JoinOverride>>();
                Map<String, List<DataSourceDescriptor>> dataSourceMap = new HashMap<String, List<DataSourceDescriptor>>();
                List<DataSourceDescriptor> configurableDataSources = new ArrayList<DataSourceDescriptor>();
                CompositeFeedDefinition compositeFeedDefinition = (CompositeFeedDefinition) new FeedStorage().getFeedDefinitionData(federatedDataSource.getSources().get(0).getDataSourceID(), conn);
                List<JoinOverride> joinOverrides = new ArrayList<JoinOverride>();
                for (CompositeFeedConnection connection : compositeFeedDefinition.obtainChildConnections()) {
                    JoinOverride joinOverride = new JoinOverride();
                    FeedDefinition source = new FeedStorage().getFeedDefinitionData(connection.getSourceFeedID());
                    FeedDefinition target = new FeedStorage().getFeedDefinitionData(connection.getTargetFeedID());
                    joinOverride.setSourceItem(findSourceItem(connection, items == null ? compositeFeedDefinition.getFields() : items));
                    joinOverride.setTargetItem(findTargetItem(connection, items == null ? compositeFeedDefinition.getFields() : items));
                    joinOverride.setSourceName(source.getFeedName());
                    joinOverride.setTargetName(target.getFeedName());
                    joinOverrides.add(joinOverride);
                }
                List<DataSourceDescriptor> dataSources = new ArrayList<DataSourceDescriptor>();
                for (CompositeFeedNode child : compositeFeedDefinition.getCompositeFeedNodes()) {
                    FeedDefinition childDataSource = new FeedStorage().getFeedDefinitionData(child.getDataFeedID(), conn);
                    dataSources.add(new DataSourceDescriptor(childDataSource.getFeedName(), childDataSource.getDataFeedID(), childDataSource.getFeedType().getType(), false,
                            childDataSource.getDataSourceBehavior()));
                }
                dataSourceMap.put(String.valueOf(dataSourceID), dataSources);
                map.put(String.valueOf(dataSourceID), joinOverrides);
                configurableDataSources.add(new DataSourceDescriptor(dataSource.getFeedName(), dataSource.getDataFeedID(),
                        dataSource.getFeedType().getType(), false, dataSource.getDataSourceBehavior()));
                reportJoins.setJoinOverrideMap(map);
                reportJoins.setDataSourceMap(dataSourceMap);
                reportJoins.setConfigurableDataSources(configurableDataSources);
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
        return reportJoins;
    }

    private void populateMap(Map<String, List<JoinOverride>> map, Map<String, List<DataSourceDescriptor>> dataSourceMap, List<DataSourceDescriptor> configurableDataSources,
                             CompositeFeedDefinition compositeFeedDefinition, List<AnalysisItem> items, EIConnection conn) throws SQLException, CloneNotSupportedException {
        List<JoinOverride> joinOverrides = new ArrayList<JoinOverride>();

        configurableDataSources.add(new DataSourceDescriptor(compositeFeedDefinition.getFeedName(), compositeFeedDefinition.getDataFeedID(), compositeFeedDefinition.getFeedType().getType(),
                false, compositeFeedDefinition.getDataSourceBehavior()));
        Feed feed = FeedRegistry.instance().getFeed(compositeFeedDefinition.getDataFeedID(), conn);
        for (CompositeFeedConnection connection : compositeFeedDefinition.obtainChildConnections()) {
            JoinOverride joinOverride = new JoinOverride();
            String sourceName;
            String targetName;
            if (connection.getSourceFeedID() != null && connection.getSourceFeedID() > 0) {
                PreparedStatement stmt = conn.prepareStatement("SELECT FEED_NAME FROM DATA_FEED WHERE DATA_FEED_ID = ?");
                stmt.setLong(1, connection.getSourceFeedID());
                ResultSet rs = stmt.executeQuery();
                rs.next();
                sourceName = rs.getString(1);
                stmt.close();
            } else {
                PreparedStatement stmt = conn.prepareStatement("SELECT TITLE FROM ANALYSIS WHERE ANALYSIS_ID = ?");
                stmt.setLong(1, connection.getSourceReportID());
                ResultSet rs = stmt.executeQuery();
                rs.next();
                sourceName = rs.getString(1);
                stmt.close();
            }
            if (connection.getTargetFeedID() != null && connection.getTargetFeedID() > 0) {
                PreparedStatement stmt = conn.prepareStatement("SELECT FEED_NAME FROM DATA_FEED WHERE DATA_FEED_ID = ?");
                stmt.setLong(1, connection.getTargetFeedID());
                ResultSet rs = stmt.executeQuery();
                rs.next();
                targetName = rs.getString(1);
                stmt.close();
            } else {
                PreparedStatement stmt = conn.prepareStatement("SELECT TITLE FROM ANALYSIS WHERE ANALYSIS_ID = ?");
                stmt.setLong(1, connection.getTargetReportID());
                ResultSet rs = stmt.executeQuery();
                rs.next();
                targetName = rs.getString(1);
                stmt.close();
            }
            joinOverride.setDataSourceID(compositeFeedDefinition.getDataFeedID());
            joinOverride.setSourceItem(findSourceItem(connection, items == null ? feed.getFields() : items));
            joinOverride.setTargetItem(findTargetItem(connection, items == null ? feed.getFields() : items));
            if (joinOverride.getSourceItem() != null && joinOverride.getTargetItem() != null) {
                joinOverride.setSourceName(sourceName);
                joinOverride.setTargetName(targetName);
                joinOverrides.add(joinOverride);
            }
        }

        map.put(String.valueOf(compositeFeedDefinition.getDataFeedID()), joinOverrides);
        List<DataSourceDescriptor> dataSources = new ArrayList<DataSourceDescriptor>();
        for (CompositeFeedNode child : compositeFeedDefinition.getCompositeFeedNodes()) {
            FeedDefinition childDataSource = new FeedStorage().getFeedDefinitionData(child.getDataFeedID(), conn);
            dataSources.add(new DataSourceDescriptor(childDataSource.getFeedName(), childDataSource.getDataFeedID(), childDataSource.getFeedType().getType(), false,
                    childDataSource.getDataSourceBehavior()));
            if (childDataSource instanceof CompositeFeedDefinition) {
                populateMap(map, dataSourceMap, configurableDataSources, (CompositeFeedDefinition) childDataSource, null, conn);
            }
        }
        dataSourceMap.put(String.valueOf(compositeFeedDefinition.getDataFeedID()), dataSources);
    }

    private AnalysisItem findSourceItem(CompositeFeedConnection connection, List<AnalysisItem> items) throws CloneNotSupportedException {
        AnalysisItem analysisItem = null;
        for (AnalysisItem item : items) {
            Key key = item.getKey();
            if (connection.getSourceFeedID() != null && connection.getSourceFeedID() > 0) {
                if (key instanceof DerivedKey) {
                    DerivedKey derivedKey = (DerivedKey) key;
                    if (derivedKey.getFeedID() == connection.getSourceFeedID()) {
                        if (connection.getSourceJoin() != null) {
                            if (item.hasType(AnalysisItemTypes.DIMENSION) && item.getKey().toKeyString().equals(connection.getSourceJoin().toKeyString())) {
                                analysisItem = item;
                                break;
                            }
                        } else {
                            if (connection.getSourceItem() != null) {
                                if (item.hasType(AnalysisItemTypes.DIMENSION) && connection.getSourceItem().toDisplay().equals(item.toDisplay())) {
                                    analysisItem = item;
                                    break;
                                }
                            }
                        }
                    }
                }
            } else if (connection.getSourceReportID() != null && connection.getSourceReportID() > 0) {
                if (key instanceof ReportKey) {
                    ReportKey reportKey = (ReportKey) key;
                    if (reportKey.getReportID() == connection.getSourceReportID()) {
                        if (connection.getSourceJoin() != null) {
                            if (item.hasType(AnalysisItemTypes.DIMENSION) && item.getKey().toKeyString().equals(connection.getSourceJoin().toKeyString())) {
                                analysisItem = item;
                                break;
                            }
                        } else {
                            if (connection.getSourceItem() != null) {
                                if (item.hasType(AnalysisItemTypes.DIMENSION) && connection.getSourceItem().toDisplay().equals(item.toDisplay())) {
                                    analysisItem = item;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

        }
        if (analysisItem == null && connection.getSourceItem() != null) {
            for (AnalysisItem item : items) {
                Key key = item.getKey();
                if (connection.getSourceFeedID() != null && connection.getSourceFeedID() > 0) {
                    if (key instanceof DerivedKey) {
                        DerivedKey derivedKey = (DerivedKey) key;
                        if (derivedKey.getFeedID() == connection.getSourceFeedID()) {
                            if (connection.getSourceItem().getKey().toBaseKey().toKeyString().equals(item.getKey().toBaseKey().toKeyString())) {
                                analysisItem = item;
                                break;
                            }
                        }
                    }
                } else if (connection.getSourceReportID() != null && connection.getSourceReportID() > 0) {
                    if (key instanceof ReportKey) {
                        ReportKey derivedKey = (ReportKey) key;
                        if (derivedKey.getReportID() == connection.getSourceReportID()) {
                            if (connection.getSourceItem().getKey().toBaseKey().toKeyString().equals(item.getKey().toBaseKey().toKeyString())) {
                                analysisItem = item;
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (analysisItem != null) {
            analysisItem = analysisItem.clone();
        }
        return analysisItem;
    }

    private AnalysisItem findLabelItem(List<AnalysisItem> items) {
        for (AnalysisItem item : items) {
            if (item.isLabelColumn()) {
                return item;
            }
        }
        return null;
    }

    private AnalysisItem findTargetItem(CompositeFeedConnection connection, List<AnalysisItem> items) throws CloneNotSupportedException {
        AnalysisItem analysisItem = null;
        for (AnalysisItem item : items) {
            Key key = item.getKey();
            if (connection.getTargetFeedID() != null && connection.getTargetFeedID() > 0) {
                if (key instanceof DerivedKey) {
                    DerivedKey derivedKey = (DerivedKey) key;
                    if (derivedKey.getFeedID() == connection.getTargetFeedID()) {
                        if (connection.getTargetJoin() != null) {
                            if (item.hasType(AnalysisItemTypes.DIMENSION) && item.getKey().toKeyString().equals(connection.getTargetJoin().toKeyString())) {
                                analysisItem = item;
                                break;
                            }
                        } else {
                            if (connection.getTargetItem() != null) {
                                if (item.hasType(AnalysisItemTypes.DIMENSION) && connection.getTargetItem().toDisplay().equals(item.toDisplay())) {
                                    analysisItem = item;
                                    break;
                                }
                            }
                        }
                    }
                }
            } else if (connection.getTargetReportID() != null && connection.getTargetReportID() > 0) {
                if (key instanceof ReportKey) {
                    ReportKey reportKey = (ReportKey) key;
                    if (reportKey.getReportID() == connection.getTargetReportID()) {
                        if (connection.getTargetJoin() != null) {
                            if (item.hasType(AnalysisItemTypes.DIMENSION) && item.getKey().toKeyString().equals(connection.getTargetJoin().toKeyString())) {
                                analysisItem = item;
                                break;
                            }
                        } else {
                            if (connection.getTargetItem() != null) {
                                if (item.hasType(AnalysisItemTypes.DIMENSION) && connection.getTargetItem().toDisplay().equals(item.toDisplay())) {
                                    analysisItem = item;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

        }
        if (analysisItem == null && connection.getTargetItem() != null) {
            for (AnalysisItem item : items) {
                Key key = item.getKey();
                if (connection.getTargetFeedID() != null && connection.getTargetFeedID() > 0) {
                    if (key instanceof DerivedKey) {
                        DerivedKey derivedKey = (DerivedKey) key;
                        if (derivedKey.getFeedID() == connection.getTargetFeedID()) {
                            if (connection.getTargetItem().getKey().toBaseKey().toKeyString().equals(item.getKey().toBaseKey().toKeyString())) {
                                analysisItem = item;
                                break;
                            }
                        }
                    }
                } else if (connection.getTargetReportID() != null && connection.getTargetReportID() > 0) {
                    if (key instanceof ReportKey) {
                        ReportKey derivedKey = (ReportKey) key;
                        if (derivedKey.getReportID() == connection.getTargetReportID()) {
                            if (connection.getTargetItem().getKey().toBaseKey().toKeyString().equals(item.getKey().toBaseKey().toKeyString())) {
                                analysisItem = item;
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (analysisItem != null) {
            analysisItem = analysisItem.clone();
        }
        return analysisItem;
    }

    public static final class ImportKey {
        private String providerID;
        private Date date;

        public ImportKey(String providerID, Date date) {
            this.providerID = providerID;
            this.date = date;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ImportKey importKey = (ImportKey) o;

            if (!date.equals(importKey.date)) return false;
            if (!providerID.equals(importKey.providerID)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = providerID.hashCode();
            result = 31 * result + date.hashCode();
            return result;
        }
    }

    public String importData(byte[] bytes, long dataSourceID) {
        SecurityUtil.authorizeFeedAccess(dataSourceID);
        // have to translate provider name -> related provider
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            FeedDefinition useSource = new FeedStorage().getFeedDefinitionData(dataSourceID, conn);
            FeedDefinition dataSource = new FeedStorage().getFeedDefinitionData(useSource.getParentSourceID(), conn);

            /*// TODO: fix
            FeedDefinition useSource;
            if (dataSource.getFeedName().equals("Therapy Works")) {
                useSource = resolveToName(dataSource, "Data Log");
            } else {
                useSource = dataSource;
            }*/

            Map<String, Key> keyMap = new HashMap<String, Key>();

            for (AnalysisItem analysisItem : useSource.getFields()) {
                if (analysisItem.isConcrete()) {
                    keyMap.put(analysisItem.toDisplay(), analysisItem.getKey());
                }
            }

            //AnalysisItemFormatMapper mapper = new AnalysisItemFormatMapper(useSource.getFields());
            Key providerPseudoField = new NamedKey("Provider Name");
            Key locationPseudoField = new NamedKey("Location Name");
            keyMap.put("Provider Name", providerPseudoField);
            keyMap.put("Location Name", locationPseudoField);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            XSSFWorkbook wb = new XSSFWorkbook(bais);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<org.apache.poi.ss.usermodel.Row> rit = sheet.rowIterator();
            DataSet dataSet = new DataSet();

            // identify which rows we're adding data onto

            Map<Short, IRow> rowMap = new HashMap<Short, IRow>();
            for (; rit.hasNext(); ) {
                org.apache.poi.ss.usermodel.Row excelRow = rit.next();
                Cell headerCell = excelRow.getCell(excelRow.getFirstCellNum());
                String headerValue = headerCell.toString().trim();
                Key key = keyMap.get(headerValue);
                if (key == null) {
                    return "We couldn't find a field by the name of " + headerValue + ".";
                }
                for (short i = (short) (excelRow.getFirstCellNum() + 1); i < excelRow.getLastCellNum(); i++) {
                    IRow row = rowMap.get(i);
                    if (row == null) {
                        row = dataSet.createRow();
                        rowMap.put(i, row);
                    }
                    Cell cell = excelRow.getCell(i);
                    if (cell != null) {
                        if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
                            if (DateUtil.isCellDateFormatted(cell)) {
                                row.addValue(key, cell.getDateCellValue());
                            } else {
                                row.addValue(key, cell.getNumericCellValue());
                            }
                        } else {
                            row.addValue(key, cell.toString());
                        }
                    }
                }
            }
            Map<List<String>, String> map = new HashMap<List<String>, String>();

            AnalysisItem relatedProviderField = null;
            AnalysisItem locationField = null;
            AnalysisItem providerField = null;
            AnalysisItem recordID = null;
            AnalysisItem providerRecordID = null;

            for (AnalysisItem field : dataSource.getFields()) {
                if ("Related Provider".equals(field.toDisplay())) {
                    relatedProviderField = field;
                } else if ("Providers - Record ID#".equals(field.toDisplay())) {
                    providerRecordID = field;
                } else if ("Provider Name".equals(field.toDisplay())) {
                    providerField = field;
                } else if ("Location Name".equals(field.toDisplay())) {
                    locationField = field;
                } else if ("Data Log - Record ID#".equals(field.toDisplay())) {
                    recordID = field;
                    recordID = recordID.clone();
                    recordID.setKeyColumn(true);
                }
            }
            AnalysisItem useSourceRelatedProviderField = null;
            AnalysisItem date = null;
            for (AnalysisItem field : useSource.getFields()) {
                if ("Date".equals(field.toDisplay())) {
                    date = field;
                } else if ("Related Provider".equals(field.toDisplay())) {
                    useSourceRelatedProviderField = field;
                }
            }

            WSListDefinition list = new WSListDefinition();
            list.setDataFeedID(dataSource.getDataFeedID());
            list.setFilterDefinitions(new ArrayList<FilterDefinition>());
            list.setColumns(Arrays.asList(locationField, providerField, providerRecordID));
            DataSet dataSet1 = DataService.listDataSet(list, new InsightRequestMetadata(), conn);

            //Collection<Object> relatedProviders = new HashSet<Object>();
            for (IRow row : dataSet1.getRows()) {
                List<String> key = new ArrayList<String>();
                key.add(row.getValue(providerField.createAggregateKey()).toString());
                key.add(row.getValue(locationField.createAggregateKey()).toString());
                String relatedProviderString = row.getValue(providerRecordID.createAggregateKey()).toString();
                map.put(key, relatedProviderString);

                //  relatedProviders.add(relatedProviderString);
            }



            List<Object> filteredValues = new ArrayList<Object>();
            Iterator<IRow> iter = dataSet.getRows().iterator();
            while (iter.hasNext()) {
                IRow row = iter.next();
                List<String> pair = new ArrayList<String>();
                Value provider = row.getValue(providerPseudoField);
                Value location = row.getValue(locationPseudoField);
                if (provider.type() == Value.EMPTY || location.type() == Value.EMPTY || "".equals(provider.toString().trim())) {
                    iter.remove();
                    continue;
                }
                pair.add(provider.toString());
                pair.add(location.toString());
                String relatedProvider = map.get(pair);
                if (relatedProvider == null || "(Empty)".equals(relatedProvider)) {
                    // ignore
                    //return "We couldn't find " + location.toString() + " - " + provider.toString() + ".";
                } else {
                    row.addValue(new NamedKey("beutk2zd6.6"), relatedProvider);
                    row.addValue(useSourceRelatedProviderField.getKey(), relatedProvider);
                    filteredValues.add(relatedProvider);
                }
                //Value providerID = row.getValue(relatedProviderField);

            }

            WSListDefinition existingReport = new WSListDefinition();
            existingReport.setDataFeedID(dataSource.getDataFeedID());

            existingReport.setColumns(Arrays.asList(recordID));
            FilterDefinition filterDefinition = new FilterValueDefinition(relatedProviderField, true, filteredValues);
            existingReport.setFilterDefinitions(Arrays.asList(filterDefinition));
            DataSet existing = DataService.listDataSet(existingReport, new InsightRequestMetadata(), conn);

            List<FilterDefinition> recordIDFilters = new ArrayList<FilterDefinition>();
            FilterValueDefinition filterValueDefinition = new FilterValueDefinition();
            filterValueDefinition.setField(recordID);
            filterValueDefinition.setInclusive(true);
            filterValueDefinition.setEnabled(true);
            filterValueDefinition.setApplyBeforeAggregation(true);
            recordIDFilters.add(filterValueDefinition);

            List<Object> recordIDValues = new ArrayList<Object>();
            for (IRow row : existing.getRows()) {
                recordIDValues.add(row.getValue(recordID));
            }
            filterValueDefinition.setFilteredValues(recordIDValues);
            DataStorage readStorage = DataStorage.readConnection(useSource.getFields(), useSource.getDataFeedID());
            ActualRowSet rowSet = readStorage.allData(recordIDFilters, useSource.getFields(), null, new InsightRequestMetadata());
            readStorage.closeConnection();


            Map<ImportKey, ActualRow> existingMap = new HashMap<ImportKey, ActualRow>();
            for (ActualRow actualRow : rowSet.getRows()) {
                //String recordIDValue = actualRow.getValues().get(recordID.qualifiedName()).toString();
                String provider = actualRow.getValues().get(useSourceRelatedProviderField.qualifiedName()).toString();
                Value dVal = actualRow.getValues().get(date.qualifiedName());
                if (dVal.type() == Value.DATE) {
                    DateValue dVal1 = (DateValue) dVal;
                    existingMap.put(new ImportKey(provider, dVal1.getDate()), actualRow);
                }

            }



            List<IRow> endTargets = new ArrayList<IRow>();
            List<ActualRow> actualTargets = new ArrayList<ActualRow>();
            iter = dataSet.getRows().iterator();
            while (iter.hasNext()) {
                IRow row = iter.next();
                Value provider = row.getValue(providerPseudoField);
                Value providerID = row.getValue(new NamedKey("beutk2zd6.6"));
                DateValue dateValue = (DateValue) row.getValue(date);

                ImportKey importKey = new ImportKey(providerID.toString(), dateValue.getDate());
                ActualRow actualRow = existingMap.get(importKey);
                if (actualRow == null) {
                    endTargets.add(row);
                } else {
                    // update actualRow with the value from row
                    for (AnalysisItem analysisItem : useSource.getFields()) {
                        Value value = row.getValue(analysisItem);
                        if (value.type() != Value.EMPTY) {
                            actualRow.getValues().put(analysisItem.qualifiedName(), value);
                        }
                    }

                    actualTargets.add(actualRow);
                }

            }

            List<IDataTransform> transforms = new ArrayList<IDataTransform>();
            if (useSource.getMarmotScript() != null && !"".equals(useSource.getMarmotScript())) {
                StringTokenizer toker = new StringTokenizer(useSource.getMarmotScript(), "\r\n");
                while (toker.hasMoreTokens()) {
                    String line = toker.nextToken();
                    transforms.addAll(new ReportCalculation(line).apply(useSource));
                }
            }
            transforms.add(new CachedCalculationTransform(useSource));
            DataStorage dataStorage = DataStorage.writeConnection(useSource, conn);
            try {
                for (IRow row : endTargets) {
                    dataStorage.addRow(row, useSource.getFields(), transforms);
                }
                for (ActualRow row : actualTargets) {
                    dataStorage.updateRow(row, useSource.getFields(), transforms);
                }
                dataStorage.commit();
            } catch (Exception e) {
                dataStorage.rollback();
                throw e;
            } finally {
                dataStorage.closeConnection();
            }
            conn.commit();
            return null;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
    }

    public void addRow(ActualRow actualRow, long dataSourceID) {
        SecurityUtil.authorizeFeedAccess(dataSourceID);
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            FeedDefinition dataSource = new FeedStorage().getFeedDefinitionData(dataSourceID, conn);
            List<IDataTransform> transforms = new ArrayList<IDataTransform>();
            if (dataSource.getMarmotScript() != null && !"".equals(dataSource.getMarmotScript())) {
                StringTokenizer toker = new StringTokenizer(dataSource.getMarmotScript(), "\r\n");
                while (toker.hasMoreTokens()) {
                    String line = toker.nextToken();
                    transforms.addAll(new ReportCalculation(line).apply(dataSource));
                }
            }
            transforms.add(new CachedCalculationTransform(dataSource));
            DataStorage dataStorage = DataStorage.writeConnection(dataSource, conn);
            try {
                dataStorage.addRow(actualRow, dataSource.getFields(), transforms);
                dataStorage.commit();
            } catch (Exception e) {
                dataStorage.rollback();
                throw e;
            } finally {
                dataStorage.closeConnection();
            }
            conn.commit();
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public void deleteRow(ActualRow actualRow, long dataSourceID) {
        SecurityUtil.authorizeFeedAccess(dataSourceID);
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            FeedDefinition dataSource = new FeedStorage().getFeedDefinitionData(dataSourceID, conn);
            DataStorage dataStorage = DataStorage.writeConnection(dataSource, conn);
            try {
                dataStorage.deleteRow(actualRow.getRowID());
                dataStorage.commit();
            } catch (Exception e) {
                dataStorage.rollback();
                throw e;
            } finally {
                dataStorage.closeConnection();
            }
            conn.commit();
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public void updateRow(ActualRow actualRow, long dataSourceID) {
        SecurityUtil.authorizeFeedAccess(dataSourceID);
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            FeedDefinition dataSource = new FeedStorage().getFeedDefinitionData(dataSourceID, conn);
            List<IDataTransform> transforms = new ArrayList<IDataTransform>();
            if (dataSource.getMarmotScript() != null && !"".equals(dataSource.getMarmotScript())) {
                StringTokenizer toker = new StringTokenizer(dataSource.getMarmotScript(), "\r\n");
                while (toker.hasMoreTokens()) {
                    String line = toker.nextToken();
                    transforms.addAll(new ReportCalculation(line).apply(dataSource));
                }
            }
            transforms.add(new CachedCalculationTransform(dataSource));
            DataStorage dataStorage = DataStorage.writeConnection(dataSource, conn);
            try {
                dataStorage.updateRow(actualRow, dataSource.getFields(), transforms);
                dataStorage.commit();
            } catch (Exception e) {
                dataStorage.rollback();
                throw e;
            } finally {
                dataStorage.closeConnection();
            }
            conn.commit();
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public ActualRowSet setupAddRow(long dataSourceID, int offset) {
        SecurityUtil.authorizeFeedAccess(dataSourceID);
        InsightRequestMetadata insightRequestMetadata = new InsightRequestMetadata();
        insightRequestMetadata.setUtcOffset(offset);
        try {
            FeedDefinition dataSource = new FeedStorage().getFeedDefinitionData(dataSourceID);
            // TODO: fix
            FeedDefinition useSource;
            if (dataSource.getFeedName().equals("Therapy Works")) {
                useSource = resolveToName(dataSource, "Data Log");
            } else {
                useSource = dataSource;
            }
            Map<String, Collection<JoinLabelOption>> optionMap = new HashMap<String, Collection<JoinLabelOption>>();
            if ("Data Log".equals(useSource.getFeedName())) {
                createOptionMap(dataSource, useSource, optionMap);
            }
            ActualRowSet actualRowSet = new ActualRowSet();
            actualRowSet.setDataSourceID(useSource.getDataFeedID());
            List<AnalysisItem> validFields = new ArrayList<AnalysisItem>();
            ActualRow actualRow = new ActualRow();
            Map<String, Value> map = new HashMap<String, Value>();
            for (AnalysisItem field : useSource.getFields()) {
                if (field.isConcrete() && !field.isDerived()) {
                    validFields.add(field);
                }
                map.put(field.qualifiedName(), new EmptyValue());
            }

            List<AnalysisItem> pool = new ArrayList<AnalysisItem>(validFields);

            List<ActualRowLayoutItem> forms;
            if ("Data Log".equals(useSource.getFeedName())) {
                forms = createForms(pool);
            } else {
                forms = new ArrayList<ActualRowLayoutItem>();
                ActualRowLayoutItem actualRowLayoutItem = new ActualRowLayoutItem();
                actualRowLayoutItem.setAnalysisItems(pool);
                actualRowLayoutItem.setColumns(3);
                actualRowLayoutItem.setColumnWidth(120);
                actualRowLayoutItem.setFormLabelWidth(140);
                forms.add(actualRowLayoutItem);
            }

            actualRow.setValues(map);
            actualRowSet.setRows(Arrays.asList(actualRow));
            actualRowSet.setAnalysisItems(validFields);
            actualRowSet.setForms(forms);
            actualRowSet.setOptions(optionMap);
            return actualRowSet;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    private void createOptionMap(FeedDefinition dataSource, FeedDefinition useSource, Map<String, Collection<JoinLabelOption>> optionMap) throws CloneNotSupportedException {
        DerivedAnalysisDimension calculation = new DerivedAnalysisDimension();
        NamedKey key = new NamedKey("TmpCalculation");
        calculation.setKey(key);
        calculation.setDerivationCode("[Provider Name] + \"-\" + [Location Name]");
        List<AnalysisItem> matchItems = new ArrayList<AnalysisItem>();
        matchItems.add(calculation);

        if (dataSource instanceof CompositeFeedDefinition) {
            CompositeFeedDefinition compositeFeedDefinition = (CompositeFeedDefinition) dataSource;
            for (IJoin connection : compositeFeedDefinition.getConnections()) {
                CompositeFeedConnection compositeFeedConnection = (CompositeFeedConnection) connection;
                AnalysisItem sourceItem = null;
                AnalysisItem targetItem = null;
                if (connection.getSourceFeedID() == useSource.getDataFeedID()) {
                    sourceItem = findSourceItem(compositeFeedConnection, dataSource.getFields());
                    targetItem = findTargetItem(compositeFeedConnection, dataSource.getFields());
                } else if (connection.getTargetFeedID() == useSource.getDataFeedID()) {
                    sourceItem = findTargetItem(compositeFeedConnection, dataSource.getFields());
                    targetItem = findSourceItem(compositeFeedConnection, dataSource.getFields());
                }
                if (targetItem == null) {
                    if (compositeFeedConnection.getTargetItem() != null) {
                        System.out.println("Could not find " + compositeFeedConnection.getTargetItem().toDisplay() + " for data source " + compositeFeedConnection.getTargetFeedName());
                    } else {
                        System.out.println("Could not find " + compositeFeedConnection.getTargetJoin().toKeyString() + " for data source " + compositeFeedConnection.getTargetFeedName());
                    }
                }
                if (targetItem != null) {
                    matchItems.add(targetItem);
                }
                if (sourceItem != null && targetItem != null) {
                    List<JoinLabelOption> options = new ArrayList<JoinLabelOption>();
                    WSListDefinition target = new WSListDefinition();
                    target.setDataFeedID(dataSource.getDataFeedID());
                    target.setColumns(matchItems);
                    target.setFilterDefinitions(new ArrayList<FilterDefinition>());
                    EIConnection conn = Database.instance().getConnection();
                    try {
                        DataSet dataSet = DataService.listDataSet(target, new InsightRequestMetadata(), conn);
                        for (IRow row : dataSet.getRows()) {
                            Value sourceValue = row.getValue(targetItem);
                            Value calcValue = row.getValue(calculation);
                            if (sourceValue.type() == Value.EMPTY || calcValue.type() == Value.EMPTY || sourceValue.toString().equals("(Empty)") ||
                                    calcValue.toString().contains("(Empty)") || sourceValue.toString().equals("") || calcValue.toString().equals("")) {
                            } else {
                                options.add(new JoinLabelOption(sourceValue, calcValue.toString()));

                            }
                        }
                    } finally {
                        Database.closeConnection(conn);
                    }
                    Collections.sort(options, new Comparator<JoinLabelOption>() {

                        public int compare(JoinLabelOption joinLabelOption, JoinLabelOption joinLabelOption1) {
                            return joinLabelOption.getDisplayName().compareTo(joinLabelOption1.getDisplayName());
                        }
                    });
                    optionMap.put(sourceItem.toDisplay(), options);
                }
            }
        }
    }

    public ActualRowSet getActualRows(Map<String, Object> data, AnalysisItem analysisItem, WSAnalysisDefinition report, int offset) {
        SecurityUtil.authorizeFeedAccess(report.getDataFeedID());
        InsightRequestMetadata insightRequestMetadata = new InsightRequestMetadata();
        insightRequestMetadata.setUtcOffset(offset);
        try {
            FeedDefinition dataSource = new FeedStorage().getFeedDefinitionData(report.getDataFeedID());
            FeedDefinition useSource = resolveToDataSource(dataSource, analysisItem.getKey());
            Map<String, Collection<JoinLabelOption>> optionMap = new HashMap<String, Collection<JoinLabelOption>>();
            if ("Data Log".equals(useSource.getFeedName())) {
                createOptionMap(dataSource, useSource, optionMap);
            }
            List<FilterDefinition> filters = new ArrayList<FilterDefinition>();
            FilterValueDefinition filterValueDefinition = new FilterValueDefinition();
            filterValueDefinition.setField(analysisItem);
            filterValueDefinition.setSingleValue(true);
            filterValueDefinition.setEnabled(true);
            filterValueDefinition.setInclusive(true);
            filterValueDefinition.setToggleEnabled(true);
            Value value = (Value) data.get(analysisItem.qualifiedName());
            if (value instanceof NumericValue) {
                String stringValue = ((Integer) value.toDouble().intValue()).toString();
                value = new StringValue(stringValue);
            }
            filterValueDefinition.setFilteredValues(Arrays.asList((Object) value));
            filters.add(filterValueDefinition);
            List<AnalysisItem> validFields = new ArrayList<AnalysisItem>();
            for (AnalysisItem field : useSource.getFields()) {
                if (field.isConcrete() && !field.isDerived()) {
                    validFields.add(field);
                }
            }
            DataStorage dataStorage = DataStorage.readConnection(useSource.getFields(), useSource.getDataFeedID());
            try {
                ActualRowSet rowSet = dataStorage.allData(filters, useSource.getFields(), null, insightRequestMetadata);
                rowSet.setOptions(optionMap);
                rowSet.setDataSourceID(useSource.getDataFeedID());
                List<AnalysisItem> pool = new ArrayList<AnalysisItem>(validFields);

                List<ActualRowLayoutItem> forms;
                if ("Data Log".equals(useSource.getFeedName())) {
                    forms = createForms(pool);
                } else {
                    forms = new ArrayList<ActualRowLayoutItem>();
                    ActualRowLayoutItem actualRowLayoutItem = new ActualRowLayoutItem();
                    actualRowLayoutItem.setColumns(3);
                    actualRowLayoutItem.setColumnWidth(120);
                    actualRowLayoutItem.setFormLabelWidth(140);
                    actualRowLayoutItem.setAnalysisItems(pool);
                    forms.add(actualRowLayoutItem);
                }

                rowSet.setForms(forms);
                return rowSet;
            } finally {
                dataStorage.closeConnection();
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    private List<ActualRowLayoutItem> createForms(List<AnalysisItem> pool) throws SQLException {
        List<ActualRowLayoutItem> forms = new ArrayList<ActualRowLayoutItem>();
        forms.addAll(new ReportCalculation("defineform(2, 0, 300, 110, \"Related Provider\", \"Date\")").apply(pool));
        forms.addAll(new ReportCalculation("defineform(3, 0, 120, 140, \"Visits Schd\", \"Walk-Ins\", \"MC/WC Schd\", \"Init Evals Schd\"," +
                "\"Init Evals CX/NS\", \"FUV CX/NS\", \"Hr-WK per FD\", \"Hr-Patient per FD\", \"Procedures/Day-FD\", \"Notes\")").apply(pool));
        forms.addAll(new ReportCalculation("defineform(3, 0, 120, 140, \"Visits per PMR\", \"Visits-Override\", \"Charges-Override\", \"Adjustments\"," +
                "\"Payments-Override\", \"Expenses\", \"Net Income\", \"wRVU-Override\", \"RVU-Override\", \"Gross Payroll\", \"FTE Hrs for MNT\"," +
                "\"Payments-Bonus Override\", \"Bonus Base Override\", \"Bonus % Override\", \"Bonus Override\", \"Bonus-Splints\", \"HR-Override\"," +
                "\"Hr-Admin\", \"Hr-CME\", \"HR-PTO\", \"Hr-HOL\", \"Hr-Patient per PMR\", \"Hr-WK per PMR\", \"HR-WK-Override\", \"Hr-Paid\"," +
                "\"FUV per PMR\", \"FUV-Override\", \"Charges-CAP\", \"Charges-FFS\", \"Charges-FFS\", \"Charges-Supplies\", \"Charges-Treatment\", \"A/R\"," +
                "\"Payments-CAP\", \"Payments-FFS\", \"Payments-FFS\", \"Payments-Supplies\", \"Payments-Treatment\", \"Referral-HT\", \"Referral-OT\"," +
                "\"Referral-PT\", \"Referral-SP\", \"Referral-Override\")").apply(pool));
        forms.addAll(new ReportCalculation("defineform(3, 1, 120, 140, \"*PT/OT*\")").apply(pool));
        forms.addAll(new ReportCalculation("defineform(3, 1, 120, 140, \"*Orthotics*\")").apply(pool));
        forms.addAll(new ReportCalculation("defineform(3, 1, 120, 140, \"*CA WC*\")").apply(pool));
        forms.addAll(new ReportCalculation("defineform(3, 1, 120, 140, \"*MediCal*\")").apply(pool));
        forms.addAll(new ReportCalculation("defineform(3, 1, 120, 140, \"*SPEECH*\")").apply(pool));
        return forms;
    }

    private FeedDefinition resolveToDataSource(FeedDefinition dataSource, Key key) throws SQLException {
        if (key instanceof NamedKey) {
            return dataSource;
        } else {
            DerivedKey derivedKey = (DerivedKey) key;
            long parentID = derivedKey.getFeedID();
            FeedDefinition parent = new FeedStorage().getFeedDefinitionData(parentID);
            return resolveToDataSource(parent, derivedKey.getParentKey());
        }
    }

    private FeedDefinition resolveToName(FeedDefinition dataSource, String name) throws SQLException {
        if (dataSource.getFeedName().equals(name)) {
            return dataSource;
        } else {
            CompositeFeedDefinition def = (CompositeFeedDefinition) dataSource;
            FeedDefinition match = null;
            for (CompositeFeedNode node : def.getCompositeFeedNodes()) {
                if (node.getDataSourceName().equals(name)) {
                    match = new FeedStorage().getFeedDefinitionData(node.getDataFeedID());
                }
            }
            if (match == null) {
                for (CompositeFeedNode node : def.getCompositeFeedNodes()) {
                    match = resolveToName(new FeedStorage().getFeedDefinitionData(node.getDataFeedID()), name);
                    if (match != null) {
                        break;
                    }
                }
            }
            return match;
        }
    }

    public FilterDateTest generate(String startDate, String endDate) {
        try {
            FilterDateTest filterDateTest = new FilterDateTest();
            InsightRequestMetadata insightRequestMetadata = new InsightRequestMetadata();
            if (startDate != null) {
                Value value = new ReportCalculation(startDate).filterApply(null, null, new HashMap<String, List<AnalysisItem>>(), new HashMap<String, List<AnalysisItem>>(),
                        null, null, new ArrayList<FilterDefinition>(), insightRequestMetadata);
                filterDateTest.setStartDate(value.toString());
            } else {
                filterDateTest.setStartDate("");
            }
            if (endDate != null) {
                Value value = new ReportCalculation(endDate).filterApply(null, null, new HashMap<String, List<AnalysisItem>>(), new HashMap<String, List<AnalysisItem>>(),
                        null, null, new ArrayList<FilterDefinition>(), insightRequestMetadata);
                filterDateTest.setEndDate(value.toString());
            } else {
                filterDateTest.setEndDate("");
            }
            return filterDateTest;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public DrillThroughResponse drillThrough(DrillThrough drillThrough, Object dataObj, AnalysisItem analysisItem,
                                             WSAnalysisDefinition report, String altKey) {
        try {
            Map<String, Object> data;
            if (dataObj instanceof Map) {
                data = (Map<String, Object>) dataObj;
            } else if (dataObj instanceof TrendOutcome) {
                TrendOutcome trendOutcome = (TrendOutcome) dataObj;
                data = new HashMap<String, Object>(trendOutcome.getDimensions());
            } else if (dataObj instanceof TreeRow) {
                data = ((TreeRow) dataObj).getValues();
            } else {
                throw new RuntimeException();
            }
            List<FilterDefinition> filters = new ArrayList<FilterDefinition>();

            List<AnalysisItem> additionalAnalysisItems = new ArrayList<AnalysisItem>();
            Set<AnalysisItem> used = new HashSet<AnalysisItem>();
            if (drillThrough.getMarmotScript() != null && !"".equals(drillThrough.getMarmotScript())) {

                filters = new ArrayList<FilterDefinition>();
                StringTokenizer toker = new StringTokenizer(drillThrough.getMarmotScript(), "\r\n");
                while (toker.hasMoreTokens()) {
                    String line = toker.nextToken();
                    filters.addAll(new ReportCalculation(line).apply(data, new ArrayList<AnalysisItem>(report.getAllAnalysisItems()), report,
                            analysisItem));
                }
            } else {
                if (analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
                    if (analysisItem.hasType(AnalysisItemTypes.CALCULATION)) {
                        //
                    }
                } else {
                    if (report.getReportType() == WSAnalysisDefinition.HEATMAP) {
                        CoordinateValue coordinateValue = (CoordinateValue) data.get(analysisItem.qualifiedName());
                        FilterValueDefinition filterValueDefinition = new FilterValueDefinition();
                        filterValueDefinition.setField(analysisItem);
                        filterValueDefinition.setSingleValue(true);
                        filterValueDefinition.setEnabled(true);
                        filterValueDefinition.setInclusive(true);
                        filterValueDefinition.setToggleEnabled(true);
                        filterValueDefinition.setShowOnReportView(drillThrough.isShowDrillThroughFilters());
                        filterValueDefinition.setFilteredValues(Arrays.asList((Object) coordinateValue.getZip()));
                        filters.add(filterValueDefinition);
                    } else if (report.getReportType() == WSAnalysisDefinition.STACKED_COLUMN) {
                        WSStackedColumnChartDefinition stackedColumnChartDefinition = (WSStackedColumnChartDefinition) report;
                        {
                            Object object = altKey;
                            Value val;
                            if (object instanceof Value) {
                                val = (Value) object;
                            } else {
                                val = new StringValue(object.toString());
                            }
                            FilterDefinition filter = constructDrillthroughFilter(drillThrough, stackedColumnChartDefinition.getStackItem(), data, val,
                                    false, additionalAnalysisItems);
                            if (filter != null) {
                                used.add(stackedColumnChartDefinition.getStackItem());
                                filters.add(filter);
                            }
                        }
                        {
                            Object object = data.get(stackedColumnChartDefinition.getXaxis().qualifiedName() + "_ORIGINAL");
                            Value val;
                            if (object instanceof Value) {
                                val = (Value) object;
                            } else if(object == null) {
                                val = new EmptyValue();
                            } else {
                                val = new StringValue(object.toString());
                            }
                            FilterDefinition filter = constructDrillthroughFilter(drillThrough, stackedColumnChartDefinition.getXaxis(), data, val,
                                    false, additionalAnalysisItems);
                            if (filter != null) {
                                used.add(stackedColumnChartDefinition.getXaxis());
                                filters.add(filter);
                            }
                        }
                    } else if (report.getReportType() == WSAnalysisDefinition.STACKED_BAR) {
                        WSStackedBarChartDefinition stackedColumnChartDefinition = (WSStackedBarChartDefinition) report;
                        {
                            Object object = altKey;
                            Value val;
                            if (object instanceof Value) {
                                val = (Value) object;
                            } else {
                                val = new StringValue(object.toString());
                            }
                            FilterDefinition filter = constructDrillthroughFilter(drillThrough, stackedColumnChartDefinition.getStackItem(), data, val,
                                    false, additionalAnalysisItems);
                            if (filter != null) {
                                used.add(stackedColumnChartDefinition.getStackItem());
                                filters.add(filter);
                            }
                        }
                        {
                            Object object = data.get(stackedColumnChartDefinition.getYaxis().qualifiedName() + "_ORIGINAL");
                            Value val;
                            if (object instanceof Value) {
                                val = (Value) object;
                            } else if (object == null) {
                                val = new EmptyValue();
                            } else {
                                val = new StringValue(object.toString());
                            }
                            FilterDefinition filter = constructDrillthroughFilter(drillThrough, stackedColumnChartDefinition.getYaxis(), data, val,
                                    false, additionalAnalysisItems);
                            if (filter != null) {
                                filters.add(filter);
                            }
                        }
                    } else {
                        Object target = data.get(analysisItem.qualifiedName());
                        boolean multiValue = false;
                        if (target instanceof Value) {
                            Value value = (Value) target;
                            if (value.getOtherValues() != null) {
                                multiValue = true;
                            }
                        }
                        Value val;
                        if (target instanceof Value) {
                            val = (Value) target;
                        } else {
                            val = new StringValue(target.toString());
                        }
                        FilterDefinition filter = constructDrillthroughFilter(drillThrough, analysisItem, data, val, multiValue, additionalAnalysisItems);
                        if (filter != null) {
                            filters.add(filter);
                        }
                    }

                }
                if (drillThrough.isAddAllFilters()) {
                    filters.addAll(new ReportCalculation("drillthroughAddFilters()").apply(data, new ArrayList<AnalysisItem>(report.getAllAnalysisItems()), report,
                            analysisItem));
                }
                if (drillThrough.isFilterRowGroupings()) {

                    /*for (FilterDefinition filter : filters) {
                        if (filter.getField() != null) {
                            used.add(filter.getField());
                        }
                    }*/
                    List<FilterDefinition> fieldFilters = analysisItem.getFilters();
                    for (FilterDefinition filter : fieldFilters) {
                        FilterDefinition clone;
                        try {
                            clone = filter.clone();
                        } catch (CloneNotSupportedException e) {
                            throw new RuntimeException(e);
                        }
                        clone.setToggleEnabled(true);
                        clone.setShowOnReportView(drillThrough.isShowDrillThroughFilters());
                        filters.add(clone);
                    }
                    for (AnalysisItem grouping : report.getAllAnalysisItems()) {
                        if (!used.contains(grouping)) {
                            if (grouping.hasType(AnalysisItemTypes.DIMENSION)) {
                                /*FilterValueDefinition filterValueDefinition = new FilterValueDefinition();
                                filterValueDefinition.setField(grouping);
                                filterValueDefinition.setSingleValue(true);
                                filterValueDefinition.setEnabled(true);
                                filterValueDefinition.setShowOnReportView(drillThrough.isShowDrillThroughFilters());
                                filterValueDefinition.setToggleEnabled(true);
                                filterValueDefinition.setInclusive(true);
                                filterValueDefinition.setFilteredValues(Arrays.asList(data.get(grouping.qualifiedName())));
                                filters.add(filterValueDefinition);*/
                                Object target = data.get(grouping.qualifiedName());
                                boolean multiValue = false;
                                if (target instanceof Value) {
                                    Value value = (Value) target;
                                    if (value.getOtherValues() != null) {
                                        multiValue = true;
                                    }
                                }
                                Value val;
                                if (target instanceof Value) {
                                    val = (Value) target;
                                } else {
                                    val = new StringValue(target.toString());
                                }
                                FilterDefinition filter = constructDrillthroughFilter(drillThrough, grouping, data, val, multiValue, additionalAnalysisItems);
                                if (filter != null) {
                                    filters.add(filter);
                                }
                            }
                        }
                    }
                }
            }
            DrillThroughResponse drillThroughResponse = new DrillThroughResponse();
            EIDescriptor descriptor;
            if (drillThrough.getReportID() != null && drillThrough.getReportID() != 0) {
                InsightResponse insightResponse = openAnalysisIfPossibleByID(drillThrough.getReportID());
                descriptor = insightResponse.getInsightDescriptor();
            } else {
                DashboardDescriptor dashboardDescriptor = new DashboardDescriptor();
                String urlKey = new DashboardStorage().urlKeyForID(drillThrough.getDashboardID());
                dashboardDescriptor.setId(drillThrough.getDashboardID());
                dashboardDescriptor.setUrlKey(urlKey);
                descriptor = dashboardDescriptor;
            }
            drillThroughResponse.setDescriptor(descriptor);
            drillThroughResponse.setFilters(filters);
            drillThroughResponse.setAdditionalFields(report.getAddedItems());
            return drillThroughResponse;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    private FilterDefinition constructDrillthroughFilter(DrillThrough drillThrough, AnalysisItem analysisItem, Map<String, Object> data, Value value, boolean multiValue, List<AnalysisItem> additionalAnalysisItems) {
        FilterDefinition filterDefinition;
        if (analysisItem.hasType(AnalysisItemTypes.DATE_DIMENSION)) {
            AnalysisDateDimension dateDimension = (AnalysisDateDimension) analysisItem;
            DerivedAnalysisDimension asTextDimension = new DerivedAnalysisDimension();
            additionalAnalysisItems.add(asTextDimension);
            asTextDimension.setKey(new NamedKey(dateDimension.toDisplay() + " for Drillthrough"));
            asTextDimension.setApplyBeforeAggregation(true);
            asTextDimension.setDerivationCode("dateformatnoshift(datelevel([" + dateDimension.toDisplay() + "], \"" + dateDimension.getDateLevel()+"\"), \"yyyy-MM-dd\")");
            FilterValueDefinition filterValueDefinition = new FilterValueDefinition();
            filterValueDefinition.setField(asTextDimension);
            filterValueDefinition.setShowOnReportView(drillThrough.isShowDrillThroughFilters());

            filterValueDefinition.setEnabled(true);
            filterValueDefinition.setInclusive(true);
            filterValueDefinition.setToggleEnabled(true);

            filterValueDefinition.setSingleValue(true);

            filterValueDefinition.setFilteredValues(Arrays.asList((Object) value.toString()));
            filterDefinition = filterValueDefinition;
        } else {
            FilterValueDefinition filterValueDefinition = new FilterValueDefinition();
            filterValueDefinition.setField(analysisItem);
            filterValueDefinition.setShowOnReportView(drillThrough.isShowDrillThroughFilters());

            filterValueDefinition.setEnabled(true);
            filterValueDefinition.setInclusive(true);
            filterValueDefinition.setToggleEnabled(true);
            if (multiValue) {
                filterValueDefinition.setSingleValue(false);

                List<Object> objs = new ArrayList<Object>();
                for (Value val : value.getOtherValues()) {
                    objs.add(val);
                }
                filterValueDefinition.setFilteredValues(objs);
            } else {
                filterValueDefinition.setSingleValue(true);
                filterValueDefinition.setFilteredValues(Arrays.asList((Object) value));
            }
            filterDefinition = filterValueDefinition;
        }
        return filterDefinition;
    }

    public List<Date> testFormat(String format, String value1, String value2, String value3) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(format);
            Date date1 = null;
            Date date2 = null;
            Date date3 = null;
            if (value1 != null) date1 = transform(dateFormat, value1);
            if (value2 != null) date2 = transform(dateFormat, value2);
            if (value3 != null) date3 = transform(dateFormat, value3);
            return Arrays.asList(date1, date2, date3);
        } catch (IllegalArgumentException iae) {
            return new ArrayList<Date>();
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    private Date transform(DateFormat dateFormat, String value) {
        try {
            return dateFormat.parse(value);
        } catch (ParseException e) {
            return null;
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    public AnalysisItem cloneItem(AnalysisItem analysisItem) {
        try {
            AnalysisItem copy = analysisItem.clone();
            if (copy instanceof AnalysisHierarchyItem ||
                    copy instanceof AnalysisCalculation ||
                    copy instanceof DerivedAnalysisDateDimension ||
                    copy instanceof DerivedAnalysisDimension) {
                Key key = new NamedKey("Copy of " + analysisItem.toDisplay());
                copy.setKey(key);
                copy.setDisplayName("Copy of " + analysisItem.toDisplay());
            } else {
                if (!(analysisItem.getKey() instanceof ReportKey)) {
                    copy.setOriginalDisplayName(analysisItem.toDisplay());
                }
                copy.setDisplayName("Copy of " + analysisItem.toDisplay());
            }
            copy.setConcrete(false);
            return copy;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public List<AnalysisItem> cloneItem(AnalysisItem analysisItem, int copies) {
        try {
            List<AnalysisItem> clones = new ArrayList<AnalysisItem>();
            for (int i = 0; i < copies; i++) {
                AnalysisItem copy = analysisItem.clone();
                if (copy instanceof AnalysisHierarchyItem ||
                        copy instanceof AnalysisCalculation ||
                        copy instanceof DerivedAnalysisDateDimension ||
                        copy instanceof DerivedAnalysisDimension) {
                    Key key = new NamedKey("Copy of " + analysisItem.toDisplay());
                    copy.setKey(key);
                    copy.setDisplayName("Copy of " + analysisItem.toDisplay());
                } else {
                    copy.setOriginalDisplayName(analysisItem.toDisplay());
                    copy.setDisplayName("Copy of " + analysisItem.toDisplay());
                }
                copy.setConcrete(false);
                clones.add(copy);
            }
            return clones;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public List<InsightDescriptor> getInsightDescriptorsForDataSource(long dataSourceID) {
        long userID = SecurityUtil.getUserID();
        EIConnection conn = Database.instance().getConnection();
        try {
            return analysisStorage.getInsightDescriptorsForDataSource(userID, SecurityUtil.getAccountID(), dataSourceID, conn);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public Collection<InsightDescriptor> getInsightDescriptors() {
        long userID = SecurityUtil.getUserID();
        EIConnection conn = Database.instance().getConnection();
        try {
            boolean testAccountVisible = FeedService.testAccountVisible(conn);
            return analysisStorage.getReports(userID, SecurityUtil.getAccountID(), conn, testAccountVisible).values();
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public WSAnalysisDefinition saveAs(WSAnalysisDefinition saveDefinition, String newName) {
        SecurityUtil.authorizeInsight(saveDefinition.getAnalysisID());
        EIConnection conn = Database.instance().getConnection();
        long reportID;
        try {
            conn.setAutoCommit(false);
            Session session = Database.instance().createSession(conn);
            AnalysisDefinition analysisDefinition = AnalysisDefinitionFactory.fromWSDefinition(saveDefinition);
            Feed feed = FeedRegistry.instance().getFeed(analysisDefinition.getDataFeedID(), conn);
            AnalysisDefinition clone = analysisDefinition.clone(null, feed.getFields(), false);
            clone.setAuthorName(SecurityUtil.getUserName());
            clone.setTitle(newName);
            List<UserToAnalysisBinding> bindings = new ArrayList<UserToAnalysisBinding>();
            bindings.add(new UserToAnalysisBinding(SecurityUtil.getUserID(), UserPermission.OWNER));
            clone.setUserBindings(bindings);
            session.close();
            session = Database.instance().createSession(conn);
            analysisStorage.saveAnalysis(clone, session);
            session.flush();
            session.close();
            reportID = clone.getAnalysisID();
        } catch (Exception e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
        Session session = Database.instance().createSession();
        try {
            session.beginTransaction();
            AnalysisDefinition savedReport = analysisStorage.getPersistableReport(reportID, session);
            WSAnalysisDefinition result = savedReport.createBlazeDefinition();
            session.getTransaction().commit();
            return result;
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    public WSAnalysisDefinition copyReport(WSAnalysisDefinition saveDefinition, long targetID) {
        SecurityUtil.authorizeInsight(saveDefinition.getAnalysisID());
        EIConnection conn = Database.instance().getConnection();
        long reportID;
        try {
            conn.setAutoCommit(false);
            FeedDefinition targetDataSource = new FeedStorage().getFeedDefinitionData(targetID);
            Session session = Database.instance().createSession(conn);
            AnalysisDefinition analysisDefinition = AnalysisDefinitionFactory.fromWSDefinition(saveDefinition);
            AnalysisDefinition clone = analysisDefinition.clone(targetDataSource, targetDataSource.getFields(), true);
            clone.setDataFeedID(targetDataSource.getDataFeedID());
            clone.setAuthorName(SecurityUtil.getUserName());
            List<UserToAnalysisBinding> bindings = new ArrayList<UserToAnalysisBinding>();
            bindings.add(new UserToAnalysisBinding(SecurityUtil.getUserID(), UserPermission.OWNER));
            clone.setUserBindings(bindings);
            session.close();
            session = Database.instance().createSession(conn);
            analysisStorage.saveAnalysis(clone, session);
            session.flush();
            session.close();
            reportID = clone.getAnalysisID();
        } catch (Exception e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
        Session session = Database.instance().createSession();
        try {
            session.beginTransaction();
            AnalysisDefinition savedReport = analysisStorage.getPersistableReport(reportID, session);
            WSAnalysisDefinition result = savedReport.createBlazeDefinition();
            session.getTransaction().commit();
            return result;
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    public void keepReport(long reportID, long sourceReportID) {
        SecurityUtil.authorizeInsight(reportID);
        EIConnection conn = Database.instance().getConnection();
        Session session = Database.instance().createSession(conn);
        try {
            AnalysisDefinition baseReport = analysisStorage.getPersistableReport(reportID, session);
            Map<Long, AnalysisDefinition> reports = new HashMap<Long, AnalysisDefinition>();
            Map<Long, Dashboard> dashboards = new HashMap<Long, Dashboard>();
            SolutionService.recurseReport(reports, dashboards, baseReport, session, conn);

            for (AnalysisDefinition report : reports.values()) {
                report.setTemporaryReport(false);
                new AnalysisStorage().clearCache(report.getAnalysisID(), report.getDataFeedID());
                session.update(report);
            }
            session.flush();
            for (Dashboard tDashboard : dashboards.values()) {
                PreparedStatement updateStmt = conn.prepareStatement("UPDATE DASHBOARD SET TEMPORARY_DASHBOARD = ? where dashboard_id = ?");
                updateStmt.setBoolean(1, false);
                updateStmt.setLong(2, tDashboard.getId());
                updateStmt.executeUpdate();
            }
            PreparedStatement queryStmt = conn.prepareStatement("SELECT EXCHANGE_REPORT_INSTALL_ID FROM EXCHANGE_REPORT_INSTALL WHERE USER_ID = ? AND " +
                    "REPORT_ID = ?");
            queryStmt.setLong(1, SecurityUtil.getUserID());
            queryStmt.setLong(2, sourceReportID);
            ResultSet rs = queryStmt.executeQuery();
            if (rs.next()) {
                long id = rs.getLong(1);
                PreparedStatement updateTimeStmt = conn.prepareStatement("UPDATE EXCHANGE_REPORT_INSTALL SET install_date = ? WHERE EXCHANGE_REPORT_INSTALL_ID = ?");
                updateTimeStmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                updateTimeStmt.setLong(2, id);
                updateTimeStmt.executeUpdate();
            } else {
                PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO EXCHANGE_REPORT_INSTALL (USER_ID, REPORT_ID, INSTALL_DATE) VALUES (?, ?, ?)");
                insertStmt.setLong(1, SecurityUtil.getUserID());
                insertStmt.setLong(2, sourceReportID);
                insertStmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                insertStmt.execute();
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            session.close();
            Database.closeConnection(conn);
        }
    }

    public void shareReport(long reportID) {
        SecurityUtil.authorizeInsight(reportID);
        Connection conn = Database.instance().getConnection();
        try {
            new AnalysisStorage().clearCache(reportID, 0);
            PreparedStatement updateStmt = conn.prepareStatement("UPDATE ANALYSIS SET ACCOUNT_VISIBLE = ? WHERE ANALYSIS_ID = ?");
            updateStmt.setBoolean(1, true);
            updateStmt.setLong(2, reportID);
            updateStmt.executeUpdate();
        } catch (SQLException e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public String validateCalculation(String calculationString, long dataSourceID, List<AnalysisItem> reportItems, WSAnalysisDefinition report) {
        SecurityUtil.authorizeFeed(dataSourceID, Roles.SUBSCRIBER);
        EIConnection conn = Database.instance().getConnection();
        try {
            Feed feed = FeedRegistry.instance().getFeed(dataSourceID, conn);
            List<AnalysisItem> allItems = new ArrayList<AnalysisItem>(feed.getFields());
            allItems.addAll(reportItems);
            CalculationTreeNode tree;
            ICalculationTreeVisitor visitor;
            CalculationsParser.startExpr_return ret;
            CalculationsLexer lexer = new CalculationsLexer(new ANTLRStringStream(calculationString));
            CommonTokenStream tokes = new CommonTokenStream();
            tokes.setTokenSource(lexer);
            CalculationsParser parser = new CalculationsParser(tokes);
            parser.setTreeAdaptor(new NodeFactory());
            Map<String, List<AnalysisItem>> keyMap = new HashMap<String, List<AnalysisItem>>();
            Map<String, List<AnalysisItem>> displayMap = new HashMap<String, List<AnalysisItem>>();
            try {
                ret = parser.startExpr();
                tree = (CalculationTreeNode) ret.getTree();

                if (allItems != null) {
                    KeyDisplayMapper mapper = KeyDisplayMapper.create(allItems);
                    keyMap = mapper.getKeyMap();
                    displayMap = mapper.getDisplayMap();
                }
                if (report != null && report.getFilterDefinitions() != null) {
                    for (FilterDefinition filter : report.getFilterDefinitions()) {
                        filter.calculationItems(displayMap);
                    }
                }
                visitor = new ResolverVisitor(keyMap, displayMap, new FunctionFactory());
                tree.accept(visitor);
            } catch (RecognitionException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            VariableListVisitor variableVisitor = new VariableListVisitor();
            tree.accept(variableVisitor);

            Set<KeySpecification> specs = variableVisitor.getVariableList();

            for (KeySpecification spec : specs) {
                try {
                    spec.findAnalysisItem(keyMap, displayMap);
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }
            }

            return null;
        } catch (ClassCastException cce) {
            if ("org.antlr.runtime.tree.CommonErrorNode cannot be cast to com.easyinsight.calculations.CalculationTreeNode".equals(cce.getMessage())) {
                return "There was a syntax error in your expression.";
            } else {
                return cce.getMessage();
            }
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            Database.closeConnection(conn);
        }
    }

    public ReportMetrics getReportMetrics(long reportID) {
        EIConnection conn = Database.instance().getConnection();
        try {
            return analysisStorage.getRating(reportID, conn);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    public ReportMetrics rateReport(long reportID, int rating) {
        long userID = SecurityUtil.getUserID();
        double ratingAverage;
        int ratingCount;
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement getExistingRatingStmt = conn.prepareStatement("SELECT user_report_rating_id FROM " +
                    "user_report_rating WHERE user_id = ? AND report_id = ?");
            getExistingRatingStmt.setLong(1, userID);
            getExistingRatingStmt.setLong(2, reportID);
            ResultSet rs = getExistingRatingStmt.executeQuery();
            if (rs.next()) {
                PreparedStatement updateRatingStmt = conn.prepareStatement("UPDATE user_report_rating " +
                        "SET RATING = ? WHERE user_report_rating_id = ?");
                updateRatingStmt.setInt(1, rating);
                updateRatingStmt.setLong(2, rs.getLong(1));
                updateRatingStmt.executeUpdate();
            } else {
                PreparedStatement insertRatingStmt = conn.prepareStatement("INSERT INTO user_report_rating " +
                        "(USER_ID, report_id, rating) values (?, ?, ?)");
                insertRatingStmt.setLong(1, userID);
                insertRatingStmt.setLong(2, reportID);
                insertRatingStmt.setInt(3, rating);
                insertRatingStmt.execute();
            }
            PreparedStatement queryStmt = conn.prepareStatement("SELECT AVG(RATING), COUNT(RATING) FROM USER_REPORT_RATING WHERE " +
                    "REPORT_ID = ?");
            queryStmt.setLong(1, reportID);
            ResultSet queryRS = queryStmt.executeQuery();
            queryRS.next();
            ratingAverage = queryRS.getDouble(1);
            ratingCount = queryRS.getInt(2);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }

        return new ReportMetrics(ratingCount, ratingAverage, rating);
    }

    public WSAnalysisDefinition saveAnalysisDefinition(WSAnalysisDefinition wsAnalysisDefinition) {

        long userID = SecurityUtil.getUserID();
        if (wsAnalysisDefinition.getAnalysisID() > 0) {
            SecurityUtil.authorizeInsight(wsAnalysisDefinition.getAnalysisID());
        }
        try {
            Cache.getCache(Cache.EMBEDDED_REPORTS).remove(wsAnalysisDefinition.getDataFeedID());
        } catch (CacheException e) {
            LogClass.error(e);
        }
        long reportID;
        EIConnection conn = Database.instance().getConnection();
        Session session = Database.instance().createSession(conn);
        try {
            conn.setAutoCommit(false);
            PreparedStatement getBindingsStmt = conn.prepareStatement("SELECT USER_ID, RELATIONSHIP_TYPE FROM USER_TO_ANALYSIS WHERE ANALYSIS_ID = ?");
            getBindingsStmt.setLong(1, wsAnalysisDefinition.getAnalysisID());
            ResultSet rs = getBindingsStmt.executeQuery();
            List<UserToAnalysisBinding> bindings = new ArrayList<UserToAnalysisBinding>();
            while (rs.next()) {
                long bindingUserID = rs.getLong(1);
                int relationshipType = rs.getInt(2);
                bindings.add(new UserToAnalysisBinding(bindingUserID, relationshipType));
            }
            if (bindings.isEmpty()) {
                bindings.add(new UserToAnalysisBinding(userID, UserPermission.OWNER));
            }
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM USER_TO_ANALYSIS WHERE ANALYSIS_ID = ?");
            stmt.setLong(1, wsAnalysisDefinition.getAnalysisID());
            stmt.executeUpdate();

            AnalysisDefinition analysisDefinition = AnalysisDefinitionFactory.fromWSDefinition(wsAnalysisDefinition);
            analysisDefinition.setUserBindings(bindings);
            analysisDefinition.setAuthorName(SecurityUtil.getUserName());
            analysisStorage.saveAnalysis(analysisDefinition, session);
            XMLMetadata xmlMetadata = new XMLMetadata();
            xmlMetadata.setConn(conn);
            /*String xml = analysisDefinition.toXML(xmlMetadata);
            PreparedStatement saveStmt = conn.prepareStatement("INSERT INTO REPORT_HISTORY (INSERT_TIME, URL_KEY, REPORT_XML, ACCOUNT_ID) VALUES (?, ?, ?, ?)");
            saveStmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            saveStmt.setString(2, analysisDefinition.getUrlKey());
            saveStmt.setString(3, xml);
            saveStmt.setLong(4, SecurityUtil.getAccountID());
            saveStmt.execute();*/
            session.flush();
            conn.commit();
            reportID = analysisDefinition.getAnalysisID();
        } catch (NonUniqueObjectException noe) {
            if (noe.getMessage().contains("Analysis")) {
                String idString = noe.getMessage().substring(noe.getMessage().lastIndexOf("#") + 1, noe.getMessage().length() - 1);
                long id = Long.parseLong(idString);
                try {
                    Feed feed = FeedRegistry.instance().getFeed(wsAnalysisDefinition.getDataFeedID());
                    Set<AnalysisItem> items = wsAnalysisDefinition.getColumnItems(feed.getFields(), new AnalysisItemRetrievalStructure(null), new InsightRequestMetadata());
                    for (AnalysisItem item : items) {
                        if (item.getAnalysisItemID() == id) {
                            LogClass.error("Error included item " + item.toDisplay() + " - " + item.getAnalysisItemID());
                        }
                    }
                } catch (Throwable t) {
                    LogClass.error(t);
                }
            }
            LogClass.error(noe);
            conn.rollback();
            throw new RuntimeException(noe);
        } catch (Exception e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
            conn.setAutoCommit(true);
            Database.closeConnection(conn);
        }
        session = Database.instance().createSession();
        try {
            session.beginTransaction();
            AnalysisDefinition savedReport = analysisStorage.getPersistableReport(reportID, session);
            WSAnalysisDefinition result = savedReport.createBlazeDefinition();
            session.getTransaction().commit();
            return result;
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    public void deleteAnalysisDefinition(long reportID) {
        int role = SecurityUtil.authorizeInsight(reportID);
        EIConnection conn = Database.instance().getConnection();
        Session session = Database.instance().createSession(conn);
        try {
            conn.setAutoCommit(false);
            AnalysisDefinition dbAnalysisDef = analysisStorage.getPersistableReport(reportID, session);
            boolean canDelete = role == Roles.OWNER;
            if (canDelete) {
                try {
                    session.delete(dbAnalysisDef);
                    session.flush();
                } catch (Exception e) {

                    // hibernate not cooperating, so delete it the hard way

                    PreparedStatement manualDeleteStmt = conn.prepareStatement("DELETE FROM ANALYSIS WHERE ANALYSIS_ID = ?");
                    manualDeleteStmt.setLong(1, reportID);
                    manualDeleteStmt.executeUpdate();
                }
            }
            new AnalysisStorage().clearCache(reportID, dbAnalysisDef.getDataFeedID());
            conn.commit();
        } catch (Exception e) {
            LogClass.error(e);
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
            Database.closeConnection(conn);
        }
    }

    public List<FilterDefinition> getFilters(long reportID) {
        try {
            List<FilterDefinition> filters = new ArrayList<FilterDefinition>();
            SecurityUtil.authorizeInsight(reportID);
            WSAnalysisDefinition report = analysisStorage.getAnalysisDefinition(reportID);
            for (FilterDefinition filterDefinition : report.getFilterDefinitions()) {
                FilterDefinition clone = filterDefinition.clone();
                filters.add(clone);
            }
            return filters;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public WSAnalysisDefinition openAnalysisDefinition(long analysisID) {
        try {
            SecurityUtil.authorizeInsight(analysisID);
            return analysisStorage.getAnalysisDefinition(analysisID);
        } catch (Exception e) {
            LogClass.error(e);
            return null;
        }
    }

    public InsightResponse openAnalysisIfPossible(String urlKey) {
        InsightResponse insightResponse = null;
        try {
            try {
                Connection conn = Database.instance().getConnection();
                try {
                    PreparedStatement queryStmt = conn.prepareStatement("SELECT ANALYSIS_ID, TITLE, DATA_FEED_ID, REPORT_TYPE FROM ANALYSIS WHERE URL_KEY = ?");
                    queryStmt.setString(1, urlKey);
                    ResultSet rs = queryStmt.executeQuery();
                    if (rs.next()) {
                        long analysisID = rs.getLong(1);
                        SecurityUtil.authorizeInsight(analysisID);
                        insightResponse = new InsightResponse(InsightResponse.SUCCESS, new InsightDescriptor(analysisID, rs.getString(2),
                                rs.getLong(3), rs.getInt(4), urlKey, Roles.NONE, false));
                    } else {
                        insightResponse = new InsightResponse(InsightResponse.REJECTED, null);
                    }
                } finally {
                    Database.closeConnection(conn);
                }

            } catch (SecurityException e) {
                long userID = SecurityUtil.getUserID(false);
                if (userID > 0) {
                    Connection conn = Database.instance().getConnection();
                    try {
                        PreparedStatement accountStmt = conn.prepareStatement("SELECT ACCOUNT_ID FROM USER, USER_TO_ANALYSIS, ANALYSIS WHERE ANALYSIS.URL_KEY = ? AND " +
                                "ANALYSIS.ANALYSIS_ID = USER_TO_ANALYSIS.ANALYSIS_ID AND USER_TO_ANALYSIS.USER_ID = USER.USER_ID");
                        accountStmt.setString(1, urlKey);
                        ResultSet rs = accountStmt.executeQuery();
                        if (rs.next()) {
                            long accountID = rs.getLong(1);
                            if (accountID == SecurityUtil.getAccountID()) {
                                insightResponse = new InsightResponse(InsightResponse.PRIVATE_ACCESS, null);
                            } else {
                                insightResponse = new InsightResponse(InsightResponse.REJECTED, null);
                            }
                        }
                    } finally {
                        Database.closeConnection(conn);
                    }
                }
                if (insightResponse == null) {
                    if (e.getReason() == InsightResponse.NEED_LOGIN)
                        insightResponse = new InsightResponse(InsightResponse.NEED_LOGIN, null);
                    else
                        insightResponse = new InsightResponse(InsightResponse.REJECTED, null);
                }
            }
        } catch (Exception e) {
            LogClass.error(e);
            return null;
        }
        return insightResponse;
    }

    public InsightResponse openAnalysisIfPossibleByID(long analysisID) {
        InsightResponse insightResponse;
        try {
            try {
                SecurityUtil.authorizeInsight(analysisID);
                Connection conn = Database.instance().getConnection();
                try {
                    PreparedStatement queryStmt = conn.prepareStatement("SELECT TITLE, DATA_FEED_ID, REPORT_TYPE, URL_KEY FROM ANALYSIS WHERE ANALYSIS_ID = ?");
                    queryStmt.setLong(1, analysisID);
                    ResultSet rs = queryStmt.executeQuery();
                    rs.next();
                    insightResponse = new InsightResponse(InsightResponse.SUCCESS, new InsightDescriptor(analysisID, rs.getString(1),
                            rs.getLong(2), rs.getInt(3), rs.getString(4), Roles.NONE, false));
                } finally {
                    Database.closeConnection(conn);
                }

            } catch (SecurityException e) {
                if (e.getReason() == InsightResponse.NEED_LOGIN)
                    insightResponse = new InsightResponse(InsightResponse.NEED_LOGIN, null);
                else
                    insightResponse = new InsightResponse(InsightResponse.REJECTED, null);
            }
        } catch (Exception e) {
            LogClass.error(e);
            return null;
        }
        return insightResponse;
    }

    public UserCapabilities getUserCapabilitiesForFeed(long feedID) {
        if (SecurityUtil.getUserID(false) == 0) {
            return new UserCapabilities(Roles.NONE, Roles.NONE, false, false);
        }
        long userID = SecurityUtil.getUserID();
        int feedRole = Integer.MAX_VALUE;
        boolean groupMember = false;
        boolean hasReports = false;
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement existingLinkQuery = conn.prepareStatement("SELECT ROLE FROM UPLOAD_POLICY_USERS WHERE " +
                    "USER_ID = ? AND FEED_ID = ?");
            existingLinkQuery.setLong(1, userID);
            existingLinkQuery.setLong(2, feedID);
            ResultSet rs = existingLinkQuery.executeQuery();
            if (rs.next()) {
                feedRole = rs.getInt(1);
            }
            PreparedStatement queryStmt = conn.prepareStatement("SELECT COUNT(COMMUNITY_GROUP.COMMUNITY_GROUP_ID) FROM COMMUNITY_GROUP, GROUP_TO_USER_JOIN WHERE " +
                    "USER_ID = ? AND GROUP_TO_USER_JOIN.GROUP_ID = COMMUNITY_GROUP.COMMUNITY_GROUP_ID");
            queryStmt.setLong(1, userID);
            ResultSet groupRS = queryStmt.executeQuery();
            groupRS.next();
            groupMember = groupRS.getInt(1) > 0;
            PreparedStatement analysisCount = conn.prepareStatement("SELECT COUNT(analysis_id) from analysis WHERE data_feed_id = ?");
            analysisCount.setLong(1, feedID);
            ResultSet analysisCountRS = analysisCount.executeQuery();
            analysisCountRS.next();
            hasReports = analysisCountRS.getInt(1) > 0;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
        return new UserCapabilities(Integer.MAX_VALUE, feedRole, groupMember, hasReports);
    }

    public UserCapabilities getUserCapabilitiesForInsight(long feedID, long insightID) {
        if (SecurityUtil.getUserID(false) == 0) {
            return new UserCapabilities(Roles.NONE, Roles.NONE, false, false);
        }
        UserCapabilities userCapabilities = getUserCapabilitiesForFeed(feedID);
        try {
            SecurityUtil.authorizeInsight(insightID);
            userCapabilities.setAnalysisRole(Roles.OWNER);
        } catch (SecurityException se) {
            userCapabilities.setAnalysisRole(Roles.NONE);
        }
        return userCapabilities;
    }

    public List<IntentionSuggestion> generatePossibleIntentions(WSAnalysisDefinition report, EIConnection conn) throws SQLException {
        List<IntentionSuggestion> suggestions = new ArrayList<IntentionSuggestion>();
        Feed feed = FeedRegistry.instance().getFeed(report.getDataFeedID());
        suggestions.addAll(commonIntentions());
        DataSourceInfo dataSourceInfo = feed.createSourceInfo(conn);
        FeedDefinition dataSource = new FeedStorage().getFeedDefinitionData(report.getDataFeedID(), conn);
        suggestions.addAll(dataSource.suggestIntentions(report, dataSourceInfo));
        suggestions.addAll(report.suggestIntentions(report));
        Collections.sort(suggestions, new Comparator<IntentionSuggestion>() {

            public int compare(IntentionSuggestion intentionSuggestion, IntentionSuggestion intentionSuggestion1) {
                return intentionSuggestion.getPriority().compareTo(intentionSuggestion1.getPriority());
            }
        });
        return suggestions;
    }

    private List<IntentionSuggestion> commonIntentions() {
        List<IntentionSuggestion> suggestions = new ArrayList<IntentionSuggestion>();
        suggestions.add(new IntentionSuggestion("Create a Filtered Field",
                "Create a copy of the specified measure for each value for the specified grouping.",
                IntentionSuggestion.SCOPE_REPORT, IntentionSuggestion.FILTERED_FIELD, IntentionSuggestion.OTHER, false));
        suggestions.add(new IntentionSuggestion("Create a Distinct Count",
                "Create a distinct count of the specified grouping.",
                IntentionSuggestion.SCOPE_REPORT, IntentionSuggestion.DISTINCT_COUNT, IntentionSuggestion.OTHER, false));
        return suggestions;
    }

    public List<IntentionSuggestion> generatePossibleIntentions(WSAnalysisDefinition report) {
        EIConnection conn = Database.instance().getConnection();
        try {
            List<IntentionSuggestion> suggestions = new ArrayList<IntentionSuggestion>();
            Feed feed = FeedRegistry.instance().getFeed(report.getDataFeedID());
            DataSourceInfo dataSourceInfo = feed.createSourceInfo(conn);
            FeedDefinition dataSource = new FeedStorage().getFeedDefinitionData(report.getDataFeedID(), conn);
            suggestions.addAll(dataSource.suggestIntentions(report, dataSourceInfo));
            suggestions.addAll(report.suggestIntentions(report));
            Collections.sort(suggestions, new Comparator<IntentionSuggestion>() {

                public int compare(IntentionSuggestion intentionSuggestion, IntentionSuggestion intentionSuggestion1) {
                    return intentionSuggestion.getPriority().compareTo(intentionSuggestion1.getPriority());
                }
            });
            return suggestions;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            Database.closeConnection(conn);
        }
    }
    /*
    class com.easyinsight.pipeline.FilterComponent -
    [{Subject=filtering duplicate data, Organization Name=Easy Insight, Comment Created At=2012-11-05},
    {Subject=Problems setting up application, Organization Name=Easy Insight, Comment Created At=2012-11-05},
    {Subject=Zendesk Integration, Organization Name=Easy Insight, Comment Created At=2012-11-05},
    {Subject=Help Doc and Reseller, Organization Name=(Empty), Comment Created At=2012-11-01},
    {Subject=Zendesk Integration, Organization Name=(Empty), Comment Created At=2012-11-05}, {Subject=Problems setting up application, Organization Name=(Empty), Comment Created At=2012-11-04}, {Subject=Help Doc and Reseller, Organization Name=Easy Insight, Comment Created At=2012-11-05}, {Subject=filtering duplicate data, Organization Name=(Empty), Comment Created At=2012-11-04}]
     */

    public List<Intention> getIntentions(WSAnalysisDefinition report, List<AnalysisItem> fields, int scope, int type) {
        try {
            if (scope == IntentionSuggestion.SCOPE_DATA_SOURCE) {
                FeedDefinition dataSource = new FeedStorage().getFeedDefinitionData(report.getDataFeedID());
                return dataSource.createIntentions(report, fields, type);
            } else if (scope == IntentionSuggestion.SCOPE_REPORT) {
                return report.createIntentions(fields, type);
            } else {
                throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
            LogClass.error(e);
            return new ArrayList<Intention>();
        }
    }
}
