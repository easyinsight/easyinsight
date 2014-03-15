package com.easyinsight.calculations.functions;

import com.easyinsight.analysis.AnalysisCalculation;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.calculations.DataSourceCalculationMetadata;
import com.easyinsight.calculations.Function;
import com.easyinsight.calculations.FunctionException;
import com.easyinsight.core.Value;
import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.datafeeds.FeedStorage;
import com.easyinsight.logging.LogClass;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.storage.CacheDataTransform;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * User: jamesboe
 * Date: 4/3/12
 * Time: 11:26 AM
 */
public class CacheFunction extends Function {
    public Value evaluate() {
        EIConnection conn = Database.instance().getConnection();
        try {
            if (!(calculationMetadata instanceof DataSourceCalculationMetadata)) {
                return null;
            }
            DataSourceCalculationMetadata dataSourceCalculationMetadata = (DataSourceCalculationMetadata) calculationMetadata;

            String dataSourceName = minusQuotes(0);
            PreparedStatement stmt = conn.prepareStatement("SELECT DATA_FEED_ID FROM DATA_FEED, UPLOAD_POLICY_USERS, USER WHERE " +
                    "FEED_NAME = ? AND UPLOAD_POLICY_USERS.FEED_ID = DATA_FEED.DATA_FEED_ID AND UPLOAD_POLICY_USERS.USER_ID = USER.USER_ID AND " +
                    "USER.ACCOUNT_ID = ?");
            stmt.setString(1, dataSourceName);
            stmt.setLong(2, SecurityUtil.getAccountID());
            ResultSet rs = stmt.executeQuery();
            rs.next();
            long id = rs.getLong(1);
            FeedDefinition dataSource = new FeedStorage().getFeedDefinitionData(id, conn);
            FeedDefinition baseSource = dataSourceCalculationMetadata.getDataSource();
            String sourceCalculationFieldName = minusQuotes(1);
            AnalysisCalculation sourceCalculation = null;
            for (AnalysisItem field : dataSource.getFields()) {
                if (sourceCalculationFieldName.equals(field.toDisplay())) {
                    sourceCalculation = (AnalysisCalculation) field;
                    break;
                }
            }
            String targetFieldName = minusQuotes(2);
            AnalysisItem targetField = null;
            for (AnalysisItem field : baseSource.getFields()) {
                if (targetFieldName.equals(field.toDisplay())) {
                    targetField = field;
                    break;
                }
            }
            dataSourceCalculationMetadata.getTransforms().add(new CacheDataTransform(sourceCalculation, targetField, dataSource, baseSource));
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        } catch (Exception e) {
            LogClass.error(e);
            throw new FunctionException(e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    public int getParameterCount() {
        return 3;
    }
}
