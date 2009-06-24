package com.easyinsight.analysis;

import com.easyinsight.datafeeds.CredentialRequirement;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class EmbeddedDataResults implements Serializable {
    private AnalysisItem[] headers;
    private WSAnalysisDefinition definition;
    private ListRow[] rows;
    private boolean dataSourceAccessible;
    private Date lastDataTime;
    private String attribution;
    private List<CredentialRequirement> credentialRequirements;

    public EmbeddedDataResults() {
    }

    public EmbeddedDataResults(EmbeddedDataResults results) {
        this.headers = results.headers;
        this.definition = results.definition;
        this.rows = results.rows;
        this.lastDataTime = results.lastDataTime;
        this.attribution = results.attribution;
    }

    public List<CredentialRequirement> getCredentialRequirements() {
        return credentialRequirements;
    }

    public void setCredentialRequirements(List<CredentialRequirement> credentialRequirements) {
        this.credentialRequirements = credentialRequirements;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public Date getLastDataTime() {
        return lastDataTime;
    }

    public void setLastDataTime(Date lastDataTime) {
        this.lastDataTime = lastDataTime;
    }

    public boolean isDataSourceAccessible() {
        return dataSourceAccessible;
    }

    public void setDataSourceAccessible(boolean dataSourceAccessible) {
        this.dataSourceAccessible = dataSourceAccessible;
    }

    public WSAnalysisDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(WSAnalysisDefinition definition) {
        this.definition = definition;
    }

    public AnalysisItem[] getHeaders() {
        return headers;
    }

    public void setHeaders(AnalysisItem[] headers) {
        this.headers = headers;
    }

    public ListRow[] getRows() {
        return rows;
    }

    public void setRows(ListRow[] rows) {
        this.rows = rows;
    }
}