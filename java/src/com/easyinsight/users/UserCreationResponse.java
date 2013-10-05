package com.easyinsight.users;

/**
 * User: James Boe
 * Date: Jul 10, 2008
 * Time: 1:23:29 PM
 */
public class UserCreationResponse {
    private boolean successful;
    private long userID;
    private String failureMessage;
    private String token;
    private String url;

    public UserCreationResponse(String failureMessage) {
        this.failureMessage = failureMessage;
        successful = false;
    }

    public UserCreationResponse(long userID) {
        this.userID = userID;
        successful = true;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }
}
