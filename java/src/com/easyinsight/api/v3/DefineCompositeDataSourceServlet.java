package com.easyinsight.api.v3;


import com.easyinsight.analysis.*;
import com.easyinsight.api.ServiceRuntimeException;
import com.easyinsight.core.Key;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.*;
import com.easyinsight.logging.LogClass;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.storage.DataStorage;
import com.easyinsight.userupload.UploadPolicy;
import com.easyinsight.util.RandomTextGenerator;
import nu.xom.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jamesboe
 * Date: 1/3/11
 * Time: 1:35 PM
 */
public class DefineCompositeDataSourceServlet extends APIServlet {

    @Override
    protected ResponseInfo processXML(Document document, EIConnection conn, HttpServletRequest request) throws Exception {

        DataStorage dataStorage = null;
        try {

            Nodes dataSourceNameNodes = document.query("/defineCompositeDataSource/dataSourceName/text()");
            String dataSourceName;
            if (dataSourceNameNodes.size() == 0) {
                return new ResponseInfo(ResponseInfo.BAD_REQUEST, "<message>You need to specify a data source name.</message>");
            } else {
                dataSourceName = dataSourceNameNodes.get(0).getValue();
            }
            Map<Long, Boolean> dataSourceMap = findDataSourceIDsByName(dataSourceName, conn);
            CompositeFeedDefinition compositeFeedDefinition;
            if (dataSourceMap.size() == 0) {
                compositeFeedDefinition = new CompositeFeedDefinition();
                compositeFeedDefinition.setFeedName(dataSourceName);
                compositeFeedDefinition.setUploadPolicy(new UploadPolicy(SecurityUtil.getUserID(), SecurityUtil.getAccountID()));
                compositeFeedDefinition.setApiKey(RandomTextGenerator.generateText(12));
            } else if (dataSourceMap.size() == 1) {
                compositeFeedDefinition = (CompositeFeedDefinition) new FeedStorage().getFeedDefinitionData(dataSourceMap.keySet().iterator().next(), conn);
            } else {
                throw new ServiceRuntimeException("More than one data source was found by that name.");
            }

            Nodes dataSources = document.query("/defineCompositeDataSource/dataSources/dataSource");
            Map<String, CompositeFeedNode> compositeNodes = new HashMap<String, CompositeFeedNode>();

            PreparedStatement queryStmt = conn.prepareStatement("SELECT DATA_FEED_ID, FEED_NAME, FEED_TYPE, REFRESH_BEHAVIOR FROM DATA_FEED WHERE " +
                    "DATA_FEED.DATA_FEED_ID = ?");
            List<CompositeFeedConnection> compositeConnections = new ArrayList<CompositeFeedConnection>();
            for (int i = 0; i < dataSources.size(); i++) {
                Node dataSourceNode = dataSources.get(i);
                String dataSource = dataSourceNode.getValue();
                Map<Long, Boolean> map = findDataSourceIDsByName(dataSource, conn);
                if (map.size() == 0) {
                    throw new ServiceRuntimeException("We couldn't find a data source with the key of " + dataSource + ".");
                } else {
                    Long dataSourceID = map.keySet().iterator().next();
                    queryStmt.setLong(1, dataSourceID);
                    ResultSet dataSetRS = queryStmt.executeQuery();
                    dataSetRS.next();
                    compositeNodes.put(dataSource, new CompositeFeedNode(dataSourceID, 0, 0, dataSetRS.getString(2), dataSetRS.getInt(3), dataSetRS.getInt(4)));
                }
            }
            queryStmt.close();

            Nodes connectionNodes = document.query("/defineCompositeDataSource/connections/connection");

            for (int i = 0; i < connectionNodes.size(); i++) {
                Node connectionNode = connectionNodes.get(i);

                String sourceDataSource;
                String targetDataSource;
                String sourceDataSourceField;
                String targetDataSourceField;
                int sourceCardinality = IJoin.ONE;
                int targetCardinality = IJoin.ONE;
                int outerJoin = 0;

                try {
                    Element connectionElement = (Element) connectionNode;
                    Attribute outerJoinAttribute = connectionElement.getAttribute("outerJoin");
                    if (outerJoinAttribute != null) {
                        String value = outerJoinAttribute.getValue();
                        if ("true".equals(value)) {
                            outerJoin = 1;
                        }
                    }
                } catch (Exception e) {
                    LogClass.error(e);
                }

                Nodes sourceDateSourceNodes = connectionNode.query("sourceDataSource/text()");
                if (sourceDateSourceNodes.size() == 0) {
                    throw new ServiceRuntimeException("You need to specify a source data source for each connection.");
                }
                sourceDataSource = sourceDateSourceNodes.get(0).getValue();
                try {
                    if (sourceDateSourceNodes.get(0) instanceof Element) {
                        Element sourceElement = (Element) sourceDateSourceNodes.get(0);
                        Attribute attribute = sourceElement.getAttribute("cardinality");
                        if (attribute != null) {
                            String cardinality = attribute.getValue();
                            if ("many".equals(cardinality.toLowerCase())) {
                                sourceCardinality = IJoin.MANY;
                            }
                        }
                    }
                } catch (Exception e) {
                    LogClass.error(e);
                }

                Nodes targetDataSourceNodes = connectionNode.query("targetDataSource/text()");
                if (targetDataSourceNodes.size() == 0) {
                    throw new ServiceRuntimeException("You need to specify a target data source for each connection.");
                }
                targetDataSource = targetDataSourceNodes.get(0).getValue();

                Nodes sourceDateSourceFieldNodes = connectionNode.query("sourceDataSourceField/text()");
                if (sourceDateSourceFieldNodes.size() == 0) {
                    throw new ServiceRuntimeException("You need to specify a source data source field for each connection.");
                }
                sourceDataSourceField = sourceDateSourceFieldNodes.get(0).getValue();

                Nodes targetDataSourceFieldNodes = connectionNode.query("targetDataSourceField/text()");
                if (targetDataSourceFieldNodes.size() == 0) {
                    throw new ServiceRuntimeException("You need to specify a target data source field for each connection.</message></response>");
                }
                targetDataSourceField = targetDataSourceFieldNodes.get(0).getValue();
                try {
                    if (sourceDateSourceNodes.get(0) instanceof Element) {
                        Element targetElement = (Element) targetDataSourceFieldNodes.get(0);
                        Attribute attribute = targetElement.getAttribute("cardinality");
                        if (attribute != null) {
                            String cardinality = attribute.getValue();
                            if ("many".equals(cardinality.toLowerCase())) {
                                targetCardinality = IJoin.MANY;
                            }
                        }
                    }
                } catch (Exception e) {
                    LogClass.error(e);
                }

                CompositeFeedNode source = compositeNodes.get(sourceDataSource);
                CompositeFeedNode target = compositeNodes.get(targetDataSource);
                if (source == null) {
                    throw new ServiceRuntimeException("We couldn't find a data source by the name of " + source + ".");
                }
                if (target == null) {
                    throw new ServiceRuntimeException("We couldn't find a data source by the name of " + target + ".");
                }
                FeedDefinition sourceFeed = new FeedStorage().getFeedDefinitionData(source.getDataFeedID(), conn);
                FeedDefinition targetFeed = new FeedStorage().getFeedDefinitionData(target.getDataFeedID(), conn);
                Key sourceKey = findKey(sourceDataSourceField, sourceFeed);
                if (sourceKey == null) {
                    throw new ServiceRuntimeException("We couldn't find a field by the key of " + sourceDataSourceField + " in " + sourceDataSource + ".");
                }
                Key targetKey = findKey(targetDataSourceField, targetFeed);
                if (targetKey == null) {
                    throw new ServiceRuntimeException("We couldn't find a field by the key of " + targetDataSourceField + " in " + targetDataSource + ".");
                }
                CompositeFeedConnection connection = new CompositeFeedConnection(source.getDataFeedID(), target.getDataFeedID(),
                        sourceKey, targetKey, sourceFeed.getFeedName(), targetFeed.getFeedName(), false, false, false, false);
                connection.setSourceCardinality(sourceCardinality);
                connection.setTargetCardinality(targetCardinality);
                connection.setForceOuterJoin(outerJoin);
                compositeConnections.add(connection);
            }

            compositeFeedDefinition.setCompositeFeedNodes(new ArrayList<CompositeFeedNode>(compositeNodes.values()));
            compositeFeedDefinition.setConnections(compositeConnections);

            compositeFeedDefinition.populateFields(conn);

            if (compositeFeedDefinition.getDataFeedID() == 0) {
                long feedID = new FeedStorage().addFeedDefinitionData(compositeFeedDefinition, conn);
                DataStorage.liveDataSource(feedID, conn, compositeFeedDefinition.getFeedType().getType());
            } else {
                new FeedStorage().updateDataFeedConfiguration(compositeFeedDefinition, conn);
            }

            return new ResponseInfo(ResponseInfo.ALL_GOOD, "<dataSourceKey>" + compositeFeedDefinition.getApiKey() + "</dataSourceKey>");
        } finally {
            if (dataStorage != null) {
                dataStorage.closeConnection();
            }
        }
    }

    private Key findKey(String fieldName, FeedDefinition dataSource) {
        for (AnalysisItem field : dataSource.getFields()) {
            if (fieldName.equals(field.getKey().toKeyString())) {
                return field.getKey();
            }
        }
        return findByDisplayName(fieldName, dataSource);
    }

    private Key findByDisplayName(String fieldName, FeedDefinition dataSource) {
        for (AnalysisItem field : dataSource.getFields()) {
            if (fieldName.equals(field.toOriginalDisplayName())) {
                return field.getKey();
            }
        }
        return null;
    }
}
