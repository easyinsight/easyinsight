package com.easyinsight.datafeeds;

import com.easyinsight.userupload.UploadPolicy;
import com.easyinsight.userupload.CredentialsResponse;
import com.easyinsight.AnalysisItem;
import com.easyinsight.AnalysisItemTypes;
import com.easyinsight.users.Credentials;
import com.easyinsight.analysis.AnalysisStorage;
import com.easyinsight.analysis.Tag;
import com.easyinsight.analysis.AnalysisHierarchyItem;
import com.easyinsight.analysis.HierarchyLevel;

import java.util.*;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * User: James Boe
 * Date: Jan 29, 2008
 * Time: 10:57:24 PM
 */
public class FeedDefinition implements Cloneable {
    private String feedName;
    private String genre;
    private List<AnalysisItem> fields;
    private UploadPolicy uploadPolicy = new UploadPolicy();
    private boolean publiclyVisible;
    private boolean marketplaceVisible;
    private Long dataFeedID;
    private long size;
    private Date dateCreated;
    private Date dateUpdated;
    private int viewCount;
    private int ratingCount;
    private double ratingAverage;
    private String ratingSource;
    private boolean dataPersisted;
    private long analysisDefinitionID;
    private Collection<Tag> tags = new HashSet<Tag>();
    private String ownerName;
    private String attribution;
    private String description;
    private long dynamicServiceDefinitionID;
    private String apiKey;
    

    public FeedDefinition() {
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public long getDynamicServiceDefinitionID() {
        return dynamicServiceDefinitionID;
    }

    public void setDynamicServiceDefinitionID(long dynamicServiceDefinitionID) {
        this.dynamicServiceDefinitionID = dynamicServiceDefinitionID;
    }

    public long getAnalysisDefinitionID() {
        return analysisDefinitionID;
    }

    public void setAnalysisDefinitionID(long analysisDefinitionID) {
        this.analysisDefinitionID = analysisDefinitionID;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDataPersisted() {
        return dataPersisted;
    }

    public void setDataPersisted(boolean dataPersisted) {
        this.dataPersisted = dataPersisted;
    }

    public Collection<Tag> getTags() {
        return tags;
    }

    public void setTags(Collection<Tag> tags) {
        this.tags = tags;
    }

    public String getRatingSource() {
        return ratingSource;
    }

    public void setRatingSource(String ratingSource) {
        this.ratingSource = ratingSource;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public double getRatingAverage() {
        return ratingAverage;
    }

    public void setRatingAverage(double ratingAverage) {
        this.ratingAverage = ratingAverage;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public List<AnalysisItem> getFields() {
        return fields;
    }

    public void setFields(List<AnalysisItem> fields) {
        this.fields = fields;
    }

    public UploadPolicy getUploadPolicy() {
        return uploadPolicy;
    }

    public void setUploadPolicy(UploadPolicy uploadPolicy) {
        this.uploadPolicy = uploadPolicy;
    }

    public Long getDataFeedID() {
        return dataFeedID;
    }

    public void setDataFeedID(Long dataFeedID) {
        this.dataFeedID = dataFeedID;
    }

    public String getFeedName() {
        return feedName;
    }

    public FeedType getFeedType() {
        return FeedType.DEFAULT;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public Feed createFeedObject() {
        return new StaticFeed();
    }

    public Feed createFeed() {
        Feed feed = createFeedObject();
        feed.setFeedID(getDataFeedID());
        feed.setFields(getFields());
        feed.setAnalysisDefinition(new AnalysisStorage().getAnalysisDefinition(analysisDefinitionID).createBlazeDefinition());
        return feed;
    }

    public void customStorage(Connection conn) throws SQLException {
        
    }

    public void customLoad(Connection conn) throws SQLException {
        
    }

    public FeedDefinition clone() throws CloneNotSupportedException {
        FeedDefinition feedDefinition = (FeedDefinition) super.clone();
        List<AnalysisItem> clonedFields = new ArrayList<AnalysisItem>();
        for (AnalysisItem analysisItem : fields) {
            clonedFields.add(analysisItem.clone());
        }
        feedDefinition.setFields(clonedFields);
        List<Tag> clonedTags = new ArrayList<Tag>();
        for (Tag tag : tags) {
            clonedTags.add(tag);
        }
        feedDefinition.setTags(clonedTags);
        feedDefinition.setDataFeedID(null);
        return feedDefinition;
    }

    public InitialAnalysis initialAnalysisDefinition() {
        return new InitialAnalysis();
    }

    public int getCredentialsDefinition() {
        return CredentialsDefinition.NO_CREDENTIALS;
    }

    public CredentialsResponse refresh(Credentials credentials) {
        throw new UnsupportedOperationException();
    }

    public boolean isPubliclyVisible() {
        return publiclyVisible;
    }

    public void setPubliclyVisible(boolean publiclyVisible) {
        this.publiclyVisible = publiclyVisible;
    }

    public boolean isMarketplaceVisible() {
        return marketplaceVisible;
    }

    public void setMarketplaceVisible(boolean marketplaceVisible) {
        this.marketplaceVisible = marketplaceVisible;
    }
}
