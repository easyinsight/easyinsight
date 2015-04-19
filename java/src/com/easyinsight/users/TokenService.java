package com.easyinsight.users;

import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.datafeeds.FeedStorage;
import com.easyinsight.datafeeds.basecampnext.BasecampNextCompositeSource;
import com.easyinsight.datafeeds.constantcontact.ConstantContactCompositeSource;
import com.easyinsight.datafeeds.freshbooks.FreshbooksCompositeSource;
import com.easyinsight.datafeeds.github.GithubCompositeSource;
import com.easyinsight.datafeeds.harvest.HarvestCompositeSource;
import com.easyinsight.datafeeds.redbooth.RedboothCompositeSource;
import com.easyinsight.datafeeds.salesforce.SalesforceBaseDataSource;
//import com.easyinsight.datafeeds.xero.XeroCompositeSource;
import com.easyinsight.datafeeds.smartsheet.SmartsheetBaseSource;
import com.easyinsight.datafeeds.surveygizmo.SurveyGizmoCompositeSource;
import com.easyinsight.datafeeds.trello.TrelloCompositeSource;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.logging.LogClass;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.config.ConfigLoader;
import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import flex.messaging.FlexContext;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.signature.HmacSha1MessageSigner;
import oauth.signpost.signature.PlainTextMessageSigner;
import org.apache.amber.oauth2.client.request.OAuthClientRequest;

import javax.servlet.http.HttpSession;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.ArrayList;

/**
 * User: jamesboe
 * Date: Aug 24, 2009
 * Time: 2:25:19 PM
 */
public class TokenService {

    public static final int CONNECTION_SETUP = 1;
    public static final int NO_CONNECTION = 2;
    public static final int USER_SOURCE = 3;
    public static final int HTML_SETUP = 4;

    public static final String SALESFORCE_CONSUMER_KEY = "3MVG9VmVOCGHKYBQUAbz7d7kk6x2g29kEbyFhTBt7u..yutNvp7evoFyWTm2q4tZfWRdxekrK6fhhwf5BN4Tq";

    public OAuthResponse getOAuthURL(int type, boolean redirect, String host, FeedDefinition dataSource, int redirectType) {
        return getOAuthResponse(type, redirect, dataSource, redirectType, FlexContext.getHttpRequest().getSession());
    }

    public OAuthResponse getOAuthURL(int type, boolean redirect, String host, FeedDefinition dataSource, int redirectType, boolean reconfig) {
        return getOAuthResponse(type, redirect, dataSource, redirectType, FlexContext.getHttpRequest().getSession(), reconfig);
    }

    public OAuthResponse getHttpOAuthURL(int type, boolean redirect, int redirectType, HttpSession session) {
        return getOAuthResponse(type, redirect, null, redirectType, session);
    }

    public OAuthResponse getOAuthResponse(int type, boolean redirect, FeedDefinition dataSource, int redirectType, HttpSession session) {
        return getOAuthResponse(type, redirect, dataSource, redirectType, session, false);
    }

    public OAuthResponse getOAuthResponse(int type, boolean redirect, FeedDefinition dataSource, int redirectType, HttpSession session, boolean reconfig) {
        try {
            OAuthConsumer consumer;
            OAuthProvider provider;
            String dataSourceId = null;

            if (dataSource != null) {
                new FeedStorage().updateDataFeedConfiguration(dataSource);
                dataSourceId = dataSource.getApiKey();
            }


            if (type == FeedType.LINKEDIN.getType() || type == FeedType.LINKEDIN_COMPOSITE.getType()) {
                consumer = new DefaultOAuthConsumer("pMAaMYgowzMITTDFzMoaIbHsCni3iBZKzz3bEvUYoIHlaSAEv78XoOsmpch9YkLq",
                        "leKpqRVV3M8CMup_x6dY8THBiKT-T4PXSs3cpSVXp0kaMS4AiZYW830yRvH6JU2O");
                provider = new DefaultOAuthProvider(
                        "https://api.linkedin.com/uas/oauth/requestToken", "https://api.linkedin.com/uas/oauth/accessToken",
                        "https://api.linkedin.com/uas/oauth/authorize");
            } else if (type == FeedType.SURVEYGIZMO_COMPOSITE.getType()) {
                consumer = new DefaultOAuthConsumer(SurveyGizmoCompositeSource.CONSUMER_KEY,
                        SurveyGizmoCompositeSource.CONSUMER_SECRET);
                provider = new DefaultOAuthProvider(
                        "http://restapi.surveygizmo.com/head/oauth/request_token", "http://restapi.surveygizmo.com/head/oauth/access_token",
                        "http://restapi.surveygizmo.com/head/oauth/authenticate");
            } else if (type == FeedType.TWITTER.getType()) {
                consumer = new DefaultOAuthConsumer("Kb9mqPL8TlaJB3lZHK8Fpw",
                        "q7W04Nth2vZYOvOfiiLfTZNdE83sPDpI2uGSAtJhKnM");
                provider = new DefaultOAuthProvider(
                        "http://twitter.com/oauth/request_token", "http://twitter.com/oauth/access_token",
                        "http://twitter.com/oauth/authorize");
            } else if (type == FeedType.FRESHBOOKS_COMPOSITE.getType()) {
                consumer = new CommonsHttpOAuthConsumer(FreshbooksCompositeSource.CONSUMER_KEY,
                        FreshbooksCompositeSource.CONSUMER_SECRET);
                consumer.setMessageSigner(new PlainTextMessageSigner());
                FreshbooksCompositeSource freshbooksCompositeSource = (FreshbooksCompositeSource) dataSource;
                provider = new CommonsHttpOAuthProvider(
                        freshbooksCompositeSource.getUrl() + "/oauth/oauth_request.php", freshbooksCompositeSource.getUrl() + "/oauth/oauth_access.php",
                        freshbooksCompositeSource.getUrl() + "/oauth/oauth_authorize.php");
            } else if (type == FeedType.CONSTANT_CONTACT.getType()) {
                ConstantContactCompositeSource constantContactCompositeSource = (ConstantContactCompositeSource) dataSource;
                OAuthClientRequest request;
                if (ConfigLoader.instance().isProduction()) {
                    request = OAuthClientRequest
                            .authorizationLocation("https://oauth2.constantcontact.com/oauth2/oauth/siteowner/authorize")
                            .setClientId(ConstantContactCompositeSource.KEY)
                            .setRedirectURI("https://www.easy-insight.com/app/oauth").setResponseType("code")
                            .buildQueryMessage();
                } else {
                    request = OAuthClientRequest
                            .authorizationLocation("https://oauth2.constantcontact.com/oauth2/oauth/siteowner/authorize")
                            .setClientId(ConstantContactCompositeSource.KEY)
                            .setRedirectURI("https://www.easy-insight.com/app/oauth").setResponseType("code")
                            .buildQueryMessage();
                }
                session.setAttribute("redirectTarget", redirectType);
                session.setAttribute("dataSourceID", dataSource.getApiKey());
                // https://oauth2.constantcontact.com/oauth2/oauth/siteowner/authorize?response_type=code&redirect_uri=https%3A%2F%2Fstaging.easy-insight.com%2Fapp%2Foauth&client_id=hzt8g9gd27c7fbge3qyscwku
                return new OAuthResponse(request.getLocationUri(), true);
            } else if (type == FeedType.GOOGLE.getType()) {
                /*consumer = new CommonsHttpOAuthConsumer("www.easy-insight.com", "OG0zlkZFPIe7JdHfLB8qXXYv");
                consumer.setMessageSigner(new HmacSha1MessageSigner());
                String scope = "https://spreadsheets.google.com/feeds/ https://docs.google.com/feeds/";
                provider = new CommonsHttpOAuthProvider(
                        "https://www.google.com/accounts/OAuthGetRequestToken?scope=" + URLEncoder.encode(scope, "utf-8"), "https://www.google.com/accounts/OAuthGetAccessToken",
                        "https://www.google.com/accounts/OAuthAuthorizeToken?hd=default");*/
                OAuthClientRequest request = OAuthClientRequest
                        .authorizationLocation("https://accounts.google.com/o/oauth2/auth")
                        .setClientId("196763839405.apps.googleusercontent.com")
                        .setRedirectURI("https://www.easy-insight.com/app/oauth").setResponseType("code").
                                setScope("https://spreadsheets.google.com/feeds/ https://docs.google.com/feeds/").setParameter("access_type", "offline")
                        .buildQueryMessage();
                System.out.println("we're putting access type of offline?");
                session.setAttribute("redirectTarget", redirectType);
                session.setAttribute("dataSourceID", dataSource.getApiKey());
                return new OAuthResponse(request.getLocationUri(), true);
            } else if (type == FeedType.GOOGLE_ANALYTICS.getType()) {
                OAuthClientRequest request = OAuthClientRequest
                        .authorizationLocation("https://accounts.google.com/o/oauth2/auth")
                        .setClientId("196763839405.apps.googleusercontent.com")
                        .setRedirectURI("https://www.easy-insight.com/app/oauth").setResponseType("code").
                                setScope("https://www.google.com/analytics/feeds/").setParameter("access_type", "offline")
                        .buildQueryMessage();
                session.setAttribute("redirectTarget", redirectType);
                session.setAttribute("dataSourceID", dataSource.getApiKey());
                return new OAuthResponse(request.getLocationUri(), true);
            } else if (type == FeedType.GOOGLE_PROVISIONING.getType()) {
                String scope = "https://apps-apis.google.com/a/feeds/user/";
                consumer = new CommonsHttpOAuthConsumer("119099431019.apps.googleusercontent.com", "UuuYup6nE4M2PjnOv_jEg8Ki");
                consumer.setMessageSigner(new HmacSha1MessageSigner());
                provider = new CommonsHttpOAuthProvider(
                        "https://www.google.com/accounts/OAuthGetRequestToken?scope=" + URLEncoder.encode(scope, "utf-8"), "https://www.google.com/accounts/OAuthGetAccessToken",
                        "https://www.google.com/accounts/OAuthAuthorizeToken?hd=default");
            } else if (type == FeedType.HARVEST_COMPOSITE.getType()) {
                HarvestCompositeSource harvestCompositeSource = (HarvestCompositeSource) dataSource;
                OAuthClientRequest request;
                if (ConfigLoader.instance().isProduction()) {
                    request = OAuthClientRequest
                            .authorizationLocation(harvestCompositeSource.getUrl() + "/oauth2/authorize")
                            .setClientId("7wBqPVAr2om0aWwNbHjFHQ==")
                            .setRedirectURI("https://www.easy-insight.com/app/oauth").setResponseType("code")
                            .buildQueryMessage();
                } else {
                    request = OAuthClientRequest
                            .authorizationLocation(harvestCompositeSource.getUrl() + "/oauth2/authorize")
                            .setClientId("7wBqPVAr2om0aWwNbHjFHQ==")
                            .setRedirectURI("https://www.easy-insight.com/app/oauth").setResponseType("code")
                            .buildQueryMessage();
                }
                session.setAttribute("redirectTarget", redirectType);
                session.setAttribute("dataSourceID", dataSource.getApiKey());
                return new OAuthResponse(request.getLocationUri(), true);
            } else if (type == FeedType.REDBOOTH_COMPOSITE.getType()) {
                OAuthClientRequest request;
                request = OAuthClientRequest
                            .authorizationLocation("https://redbooth.com/oauth2/authorize")
                            .setClientId(RedboothCompositeSource.CLIENT_KEY)
                            .setRedirectURI("https://www.easy-insight.com/app/oauth").setResponseType("code")
                            .buildQueryMessage();
                session.setAttribute("redirectTarget", redirectType);
                session.setAttribute("dataSourceID", dataSource.getApiKey());
                String uri = request.getLocationUri();
                //uri = uri + "&scope=read_projects";
                return new OAuthResponse(uri, true);
            } else if (type == FeedType.GITHUB_COMPOSITE.getType()) {
                GithubCompositeSource githubSource = (GithubCompositeSource) dataSource;
                OAuthClientRequest request;
                if (ConfigLoader.instance().isProduction()) {
                    request = OAuthClientRequest
                            .authorizationLocation("https://github.com/login/oauth/authorize")
                            .setClientId("57a7af824803bc0fe694")
                            .setRedirectURI("https://www.easy-insight.com/app/oauth").setResponseType("code")
                            .buildQueryMessage();
                } else {
                    request = OAuthClientRequest
                            .authorizationLocation("https://github.com/login/oauth/authorize")
                            .setClientId("57a7af824803bc0fe694")
                            .setRedirectURI("https://www.easy-insight.com/app/oauth").setResponseType("code")
                            .buildQueryMessage();
                }
                session.setAttribute("redirectTarget", redirectType);
                session.setAttribute("dataSourceID", dataSource.getApiKey());
                String uri = request.getLocationUri();
                uri = uri + "&scope=user,repo";
                return new OAuthResponse(uri, true);
            } else if (type == FeedType.BASECAMP_NEXT_COMPOSITE.getType()) {
                OAuthClientRequest request;
                if (ConfigLoader.instance().isProduction()) {
                    request = OAuthClientRequest
                            .authorizationLocation("https://launchpad.37signals.com/authorization/new")
                            .setClientId(BasecampNextCompositeSource.CLIENT_ID)
                            .setRedirectURI("https://www.easy-insight.com/app/oauth")
                            .buildQueryMessage();
                } else {
                    request = OAuthClientRequest
                            .authorizationLocation("https://launchpad.37signals.com/authorization/new")
                            .setClientId(BasecampNextCompositeSource.CLIENT_ID)
                            .setRedirectURI("https://www.easy-insight.com/app/oauth")
                            .buildQueryMessage();
                }

                session.setAttribute("redirectTarget", redirectType);
                session.setAttribute("dataSourceID", dataSource.getApiKey());
                String uri = request.getLocationUri();
                uri = uri + "&type=web_server";
                return new OAuthResponse(uri, true);
            } else if (type == FeedType.SALESFORCE.getType()) {

                OAuthClientRequest clientRequest = OAuthClientRequest.authorizationLocation("https://na1.salesforce.com/services/oauth2/authorize").
                        setClientId(SalesforceBaseDataSource.SALESFORCE_CONSUMER_KEY).
                        setRedirectURI("https://www.easy-insight.com/app/oauth").
                        setResponseType("code").
                        //setRedirectURI("https://staging.easy-insight.com/app/oauth").
                                buildQueryMessage();
                session.setAttribute("redirectTarget", redirectType);
                session.setAttribute("dataSourceID", dataSource.getApiKey());
                return new OAuthResponse(clientRequest.getLocationUri(), true);
            } else if (type == FeedType.TRELLO_COMPOSITE.getType()) {
                consumer = new CommonsHttpOAuthConsumer(TrelloCompositeSource.KEY,
                        TrelloCompositeSource.SECRET_KEY);
                consumer.setMessageSigner(new HmacSha1MessageSigner());
                provider = new CommonsHttpOAuthProvider(
                        "https://trello.com/1/OAuthGetRequestToken", "https://trello.com/1/OAuthGetAccessToken", "https://trello.com/1/OAuthAuthorizeToken?name=EasyInsight&expiration=never");
                //return new OAuthResponse("https://trello.com/1/authorize?key="+TrelloCompositeSource.KEY+"&name=EasyInsight&expiration=never&response_type=token", true);
            } else if (type == FeedType.SMARTSHEET_TABLE.getType()) {
                OAuthClientRequest request;
                if (ConfigLoader.instance().isProduction()) {
                    request = OAuthClientRequest
                            .authorizationLocation("https://www.smartsheet.com/b/authorize")
                            .setClientId(SmartsheetBaseSource.CLIENT_ID)
                            .setRedirectURI("https://www.easy-insight.com/app/oauth").setResponseType("code")
                            .buildQueryMessage();
                } else {
                    request = OAuthClientRequest
                            .authorizationLocation("https://www.smartsheet.com/b/authorize")
                            .setClientId(SmartsheetBaseSource.CLIENT_ID)
                            .setRedirectURI("https://www.easy-insight.com/app/oauth").setResponseType("code")
                            .buildQueryMessage();
                }
                session.setAttribute("redirectTarget", redirectType);
                session.setAttribute("dataSourceID", dataSource.getApiKey());
                String uri = request.getLocationUri();
                uri = uri + "&scope=READ_SHEETS&state=EI";
                return new OAuthResponse(uri, true);
                // https://www.smartsheet.com/b/authorize?response_type=code&client_id=dheu3dmkd32fhxme&redirect_uri=https%3A%2F%2Fmyapp.smartsheet.com%2Ftarget&scope=READ_SHEETS,WRITE_SHEETS&state=MY_STATE
                // https://app.smartsheet.com/b/authorize?formName=fn_authorize&formAction=fa_loadAuthorize&response_type=code&client_id=ykzjc8ug4gz2wl85ct&redirect_uri=https%3A%2F%2Fwww.easy-insight.com%2Fapp%2Foauth&scope=READ_SHEETS

                // https://www.smartsheet.com/b/authorize?response_type=code&redirect_uri=https%3A%2F%2Fwww.easy-insight.com%2Fapp%2Foauth&client_id=ykzjc8ug4gz2wl85ct&scope=READ_SHEETS
            } else {
                throw new RuntimeException();
            }
            session.setAttribute("oauthConsumer", consumer);
            session.setAttribute("oauthProvider", provider);
            session.setAttribute("repoint", reconfig);

            String requestToken;
            if (redirect) {
                if (ConfigLoader.instance().isProduction()) {
                    //requestToken = provider.retrieveRequestToken(consumer, "https://staging.easy-insight.comasy-insight.com/app/oauth?redirectTarget="+redirectType+"&dataSourceID=" + dataSource.getApiKey());
                    if (dataSourceId != null) {
                        requestToken = provider.retrieveRequestToken(consumer, "https://www.easy-insight.com/app/oauth?redirectTarget=" + redirectType + "&dataSourceID=" + dataSourceId);
                    } else {
                        requestToken = provider.retrieveRequestToken(consumer, "https://www.easy-insight.com/app/oauth?redirectTarget=" + redirectType + "&type=googleProvider");
                    }

                } else {
                    //requestToken = provider.retrieveRequestToken(consumer, "https://staging.easy-insight.com/app/oauth?redirectTarget="+redirectType+"&dataSourceID=" + dataSource.getApiKey());
                    if (dataSourceId != null) {
                        requestToken = provider.retrieveRequestToken(consumer, "https://j8staging.easy-insight.com/app/oauth?redirectTarget=" + redirectType + "&dataSourceID=" + dataSourceId);
                    } else {
                        requestToken = provider.retrieveRequestToken(consumer, "https://j8staging.easy-insight.com/app/oauth?redirectTarget=" + redirectType + "&type=googleProvider");
                    }
                }
            } else {
                requestToken = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
            }

            // Salesforce requires that the request passes along your oauth_consumer_key
            if (type == FeedType.SALESFORCE.getType()) {
                requestToken = requestToken + "&oauth_consumer_key=" + SALESFORCE_CONSUMER_KEY;
            }
            return new OAuthResponse(requestToken, true);
        } catch (OAuthCommunicationException oauthException) {
            LogClass.error(oauthException);
            if (oauthException.getMessage().indexOf("302") != -1) {
                return new OAuthResponse(false, OAuthResponse.BAD_HOST);
            } else {
                return new OAuthResponse(false, OAuthResponse.OTHER_OAUTH_PROBLEM);
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public void deleteToken(int type) {
        try {
            Token token = new TokenStorage().getToken(SecurityUtil.getUserID(), type);
            if (token != null) {
                new TokenStorage().deleteToken(token);
            }
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public String setToken(int type, String sessionToken) {
        /*System.out.println("got URL " + url);
        String queryString = url.substring(url.indexOf("?"));*/

        Token tokenObject = new Token();
        tokenObject.setUserID(SecurityUtil.getUserID());
        tokenObject.setTokenType(type);
        tokenObject.setTokenValue(sessionToken);
        new TokenStorage().saveToken(tokenObject, 0);
        return null;
    }

    public boolean isTokenEstablished(int type) {
        try {
            Token token = new TokenStorage().getToken(SecurityUtil.getUserID(), type);
            return token != null;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }
}
