package com.easyinsight.datafeeds.highrise;

import com.easyinsight.core.NamedKey;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.DataSourceMigration;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.storage.IDataStorage;
import com.easyinsight.users.Token;
import com.easyinsight.users.TokenStorage;
import org.jetbrains.annotations.NotNull;
import org.apache.commons.httpclient.HttpClient;

import java.util.*;
import java.sql.Connection;

import com.easyinsight.dataset.DataSet;
import com.easyinsight.core.Key;
import com.easyinsight.core.DateValue;
import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.analysis.*;

/**
 * User: jamesboe
 * Date: Sep 2, 2009
 * Time: 11:50:45 PM
 */
public class HighRiseCompanySource extends HighRiseBaseSource {

    public static final String COMPANY_NAME = "Company Name";
    public static final String COMPANY_ID = "Company ID";
    public static final String TAGS = "Tags";
    public static final String OWNER = "Account Owner";
    public static final String CREATED_AT = "Created At";
    public static final String UPDATED_AT = "Company Updated At";
    public static final String COUNT = "Count";

    public static final String ZIP_CODE = "Company Zip Code";
    public static final String BACKGROUND = "Company Background";

    public static final String COUNTRY = "Company Country";
    public static final String STATE = "Company State";
    public static final String CITY = "Company City";

    public static final String COMPANY_WORK_EMAIL = "Company Work Email";
    public static final String COMPANY_HOME_EMAIL = "Company Home Email";
    public static final String COMPANY_OTHER_EMAIL = "Company Other Email";
    public static final String COMPANY_MOBILE_PHONE = "Company Mobile Phone";
    public static final String COMPANY_OFFICE_PHONE = "Company Work Phone";
    public static final String COMPANY_HOME_PHONE = "Company Home Phone";
    public static final String COMPANY_FAX_PHONE = "Company Fax Phone";
    public static final String COMPANY_STREET = "Company Street";

    public HighRiseCompanySource() {
        setFeedName("Company");
    }

    @NotNull
    protected List<String> getKeys(FeedDefinition parentDefinition) {
        return Arrays.asList(COMPANY_NAME, COMPANY_ID, TAGS, OWNER, CREATED_AT, COUNT, ZIP_CODE, BACKGROUND, UPDATED_AT,
                COUNTRY, STATE, CITY, COMPANY_WORK_EMAIL, COMPANY_HOME_EMAIL, COMPANY_OTHER_EMAIL, COMPANY_MOBILE_PHONE,
                COMPANY_OFFICE_PHONE, COMPANY_HOME_PHONE, COMPANY_FAX_PHONE, COMPANY_STREET);
    }

    public List<AnalysisItem> createAnalysisItems(Map<String, Key> keys, Connection conn, FeedDefinition parentDefinition) {
        List<AnalysisItem> analysisItems = new ArrayList<AnalysisItem>();
        analysisItems.add(new AnalysisDimension(keys.get(COMPANY_NAME), true));
        analysisItems.add(new AnalysisDimension(keys.get(ZIP_CODE), true));
        analysisItems.add(new AnalysisDimension(keys.get(BACKGROUND), true));
        analysisItems.add(new AnalysisDimension(keys.get(COMPANY_ID), true));
        analysisItems.add(new AnalysisDimension(keys.get(COUNTRY), true));
        analysisItems.add(new AnalysisDimension(keys.get(STATE), true));
        analysisItems.add(new AnalysisDimension(keys.get(CITY), true));
        analysisItems.add(new AnalysisDimension(keys.get(COMPANY_WORK_EMAIL), true));
        analysisItems.add(new AnalysisDimension(keys.get(COMPANY_HOME_EMAIL), true));
        analysisItems.add(new AnalysisDimension(keys.get(COMPANY_OTHER_EMAIL), true));
        analysisItems.add(new AnalysisDimension(keys.get(COMPANY_FAX_PHONE), true));
        analysisItems.add(new AnalysisDimension(keys.get(COMPANY_MOBILE_PHONE), true));
        analysisItems.add(new AnalysisDimension(keys.get(COMPANY_HOME_PHONE), true));
        analysisItems.add(new AnalysisDimension(keys.get(COMPANY_OFFICE_PHONE), true));
        analysisItems.add(new AnalysisDimension(keys.get(COMPANY_STREET), true));
        analysisItems.add(new AnalysisList(keys.get(TAGS), true, ","));
        analysisItems.add(new AnalysisDimension(keys.get(OWNER), true));
        analysisItems.add(new AnalysisDateDimension(keys.get(CREATED_AT), true, AnalysisDateDimension.DAY_LEVEL));
        analysisItems.add(new AnalysisDateDimension(keys.get(UPDATED_AT), true, AnalysisDateDimension.DAY_LEVEL));
        analysisItems.add(new AnalysisMeasure(keys.get(COUNT), AggregationTypes.SUM));
        try {
            HighRiseCompositeSource compositeSource = (HighRiseCompositeSource) parentDefinition;
            Token token = new TokenStorage().getToken(SecurityUtil.getUserID(), TokenStorage.HIGHRISE_TOKEN, parentDefinition.getDataFeedID(), false, (EIConnection) conn);
            HttpClient client = getHttpClient(token.getTokenValue(), "");
            HighriseCustomFieldsCache cache = compositeSource.getOrCreateCustomFieldCache(client, null);
            for (Map.Entry<String, String> entry : cache.getCustomFields().entrySet()) {
                Key key = keys.get(entry.getKey());
                if (key == null) {
                    key = new NamedKey(entry.getKey());
                }
                analysisItems.add(new AnalysisDimension(key, "Company " + entry.getValue()));
            }
        } catch (ReportException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return analysisItems;
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.HIGHRISE_COMPANY;
    }

    public DataSet getDataSet(Map<String, Key> keys, Date now, FeedDefinition parentDefinition, IDataStorage IDataStorage, EIConnection conn, String callDataID, Date lastRefreshDate) {
        HighRiseCompositeSource highRiseCompositeSource = (HighRiseCompositeSource) parentDefinition;


        DataSet ds = new DataSet();
        Token token = new TokenStorage().getToken(SecurityUtil.getUserID(), TokenStorage.HIGHRISE_TOKEN, parentDefinition.getDataFeedID(), false, conn);
        HttpClient client = getHttpClient(token.getTokenValue(), "");
        try {
            HighriseCompanyCache highriseCompanyCache = highRiseCompositeSource.getOrCreateCompanyCache(client, lastRefreshDate);
            for (HighriseCompany highriseCompany : highriseCompanyCache.getCompanyList()) {
                IRow row = ds.createRow();
                row.addValue(BACKGROUND, highriseCompany.getBackground());
                row.addValue(COMPANY_ID, highriseCompany.getCompanyID());
                row.addValue(COMPANY_NAME, highriseCompany.getCompanyName());
                row.addValue(ZIP_CODE, highriseCompany.getZipCode());
                row.addValue(COUNTRY, highriseCompany.getCountry());
                row.addValue(STATE, highriseCompany.getState());
                row.addValue(CITY, highriseCompany.getCity());
                row.addValue(COMPANY_STREET, highriseCompany.getStreet());
                row.addValue(COMPANY_FAX_PHONE, highriseCompany.getFaxPhone());
                row.addValue(COMPANY_HOME_EMAIL, highriseCompany.getHomeEmail());
                row.addValue(COMPANY_WORK_EMAIL, highriseCompany.getOfficeEmail());
                row.addValue(COMPANY_MOBILE_PHONE, highriseCompany.getMobilePhone());
                row.addValue(COMPANY_OFFICE_PHONE, highriseCompany.getOfficePhone());
                row.addValue(COMPANY_OTHER_EMAIL, highriseCompany.getOtherEmail());
                row.addValue(COMPANY_HOME_PHONE, highriseCompany.getHomePhone());
                row.addValue(CREATED_AT, new DateValue(highriseCompany.getCreatedAt()));
                row.addValue(UPDATED_AT, new DateValue(highriseCompany.getUpdatedAt()));
                row.addValue(TAGS, highriseCompany.getTags());
                row.addValue(OWNER, highriseCompany.getOwner());
                row.addValue(COUNT, 1);
                for (Map.Entry<String, String> entry : highriseCompany.getCustomFields().entrySet()) {
                    row.addValue(keys.get(entry.getKey()), entry.getValue());
                }
            }
        } catch (ReportException re) {
            throw re;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return ds;
    }

    @Override
    public int getVersion() {
        return 5;
    }

    @Override
    public List<DataSourceMigration> getMigrations() {
        return Arrays.asList(new HighRiseCompany1To2(this), new HighRiseCompany2To3(this), new HighRiseCompany3To4(this),
                new HighRiseCompany4To5(this));
    }
}
