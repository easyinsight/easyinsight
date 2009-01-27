package com.easyinsight.dataengine;

import com.easyinsight.IDataService;
import com.easyinsight.FeedMetadata;
import com.easyinsight.AnalysisItem;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.analysis.WSAnalysisDefinition;
import com.easyinsight.analysis.AnalysisItemResultMetadata;
import com.easyinsight.analysis.InsightRequestMetadata;
import com.easyinsight.webservice.google.ListDataResults;
import com.easyinsight.webservice.google.CrossTabDataResults;
import com.easyinsight.users.Credentials;

/**
 * User: James Boe
 * Date: Aug 5, 2008
 * Time: 11:30:42 PM
 */
public class ParameterizedDataService implements IDataService {

    private EngineRequestHandler engineRequestHandler = EngineRequestHandler.instance();

    public FeedMetadata getFeedMetadata(long feedID, boolean preview) {
        // authenticate needs to handle non-logged in, with access at this case
        SecurityUtil.authorizeFeedAccess(feedID, preview);
        return (FeedMetadata) engineRequestHandler.addRequest(new FeedMetadataRequest(feedID));
    }

    public ListDataResults list(WSAnalysisDefinition analysisDefinition, boolean preview, InsightRequestMetadata insightRequestMetadata) {
        SecurityUtil.authorizeFeedAccess(analysisDefinition.getDataFeedID(), preview);
        return (ListDataResults) engineRequestHandler.addRequest(new ListRequest(analysisDefinition, preview, insightRequestMetadata));
    }

    public CrossTabDataResults pivot(WSAnalysisDefinition analysisDefinition, boolean preview, InsightRequestMetadata insightRequestMetadata) {
        SecurityUtil.authorizeFeedAccess(analysisDefinition.getDataFeedID(), preview);
        return (CrossTabDataResults) engineRequestHandler.addRequest(new CrosstabRequest(analysisDefinition, preview, insightRequestMetadata));
    }

    public FeedMetadata getFeedMetadata(long feedID, Credentials credentials) {
        return getFeedMetadata(feedID, false);
    }

    public ListDataResults list(WSAnalysisDefinition analysisDefinition, InsightRequestMetadata insightRequestMetadata) {
        return list(analysisDefinition, false, insightRequestMetadata);
    }

    public CrossTabDataResults pivot(WSAnalysisDefinition analysisDefinition, InsightRequestMetadata insightRequestMetadata) {
        return pivot(analysisDefinition, false, insightRequestMetadata);
    }

    public AnalysisItemResultMetadata getAnalysisItemMetadata(long feedID, AnalysisItem analysisItem) {
        SecurityUtil.authorizeFeedAccess(feedID);
        return (AnalysisItemResultMetadata) engineRequestHandler.addRequest(new AnalysisItemMetadataRequest(analysisItem, feedID));
    }
}
