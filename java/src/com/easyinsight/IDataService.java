package com.easyinsight;

import com.easyinsight.webservice.google.CrossTabDataResults;
import com.easyinsight.webservice.google.ListDataResults;
import com.easyinsight.analysis.WSAnalysisDefinition;
import com.easyinsight.analysis.AnalysisItemResultMetadata;
import com.easyinsight.analysis.InsightRequestMetadata;
import com.easyinsight.users.Credentials;

/**
 * User: jboe
 * Date: Dec 22, 2007
 * Time: 9:50:01 AM
 */

public interface IDataService {
    public FeedMetadata getFeedMetadata(long dataFeed, boolean preview);
    public ListDataResults list(WSAnalysisDefinition analysisDefinition, boolean preview, InsightRequestMetadata insightRequestMetadata);
    public CrossTabDataResults pivot(WSAnalysisDefinition analysisDefinition, boolean preview, InsightRequestMetadata insightRequestMetadata);
    public FeedMetadata getFeedMetadata(long dataFeed, Credentials credentials);
    public ListDataResults list(WSAnalysisDefinition analysisDefinition, InsightRequestMetadata insightRequestMetadata);
    public CrossTabDataResults pivot(WSAnalysisDefinition analysisDefinition, InsightRequestMetadata insightRequestMetadata);
    public AnalysisItemResultMetadata getAnalysisItemMetadata(long feedID, AnalysisItem analysisItem);
}
