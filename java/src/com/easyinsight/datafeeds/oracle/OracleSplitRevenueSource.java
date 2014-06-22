package com.easyinsight.datafeeds.oracle;

import com.easyinsight.analysis.AnalysisDimension;
import com.easyinsight.analysis.AnalysisMeasure;
import com.easyinsight.analysis.AnalysisDateDimension;
import com.easyinsight.analysis.IRow;
import com.easyinsight.analysis.ReportException;
import com.easyinsight.core.Key;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.datafeeds.oracle.client.*;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.logging.LogClass;
import com.easyinsight.storage.IDataStorage;

import javax.xml.ws.BindingProvider;
import java.net.URL;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class OracleSplitRevenueSource extends OracleBaseSource {
	public OracleSplitRevenueSource() {
        setFeedName("SplitRevenue");
    }

	public static final String TARGETPARTYID = "TargetPartyId";
	public static final String REVNID = "RevnId";
	public static final String BUORGID = "BUOrgId";
	public static final String CUSTOMERACCOUNTID = "CustomerAccountId";
	public static final String COMMENTS = "Comments";
	public static final String RESOURCEPARTYID = "ResourcePartyId";
	public static final String OWNERRESOURCEORGID = "OwnerResourceOrgId";
	public static final String EFFECTIVEDATE = "EffectiveDate";
	public static final String OPTYID = "OptyId";
	public static final String INVENTORYITEMID = "InventoryItemId";
	public static final String INVENTORYORGID = "InventoryOrgId";
	public static final String REVNAMOUNTCURCYCODE = "RevnAmountCurcyCode";
	public static final String TYPECODE = "TypeCode";
	public static final String WINPROB = "WinProb";
	public static final String EXPECTDLVRYDATE = "ExpectDlvryDate";
	public static final String UOMCODE = "UOMCode";
	public static final String CREATIONDATE = "CreationDate";
	public static final String CREATEDBY = "CreatedBy";
	public static final String LASTUPDATEDBY = "LastUpdatedBy";
	public static final String CONFLICTID = "ConflictId";
	public static final String LASTUPDATELOGIN = "LastUpdateLogin";
	public static final String LASTUPDATEDATE = "LastUpdateDate";
	public static final String USERLASTUPDATEDATE = "UserLastUpdateDate";
	public static final String PRODGROUPID = "ProdGroupId";
	public static final String REVNNUMBER = "RevnNumber";
	public static final String RECURFREQUENCYCODE = "RecurFrequencyCode";
	public static final String RECURENDDATE = "RecurEndDate";
	public static final String RECURPARENTREVNID = "RecurParentRevnId";
	public static final String RECURTYPECODE = "RecurTypeCode";
	public static final String RECURSTARTDATE = "RecurStartDate";
	public static final String SALESCREDITTYPECODE = "SalesCreditTypeCode";
	public static final String SPLITPARENTREVNID = "SplitParentRevnId";
	public static final String SPLITPERCENT = "SplitPercent";
	public static final String SPLITTYPECODE = "SplitTypeCode";
	public static final String STATUSCODE = "StatusCode";
	public static final String CLOSEREASONCODE = "CloseReasonCode";
	public static final String PRCMPTPARTYID = "PrCmptPartyId";
	public static final String PARTYNAME = "PartyName";
	public static final String PARTYID = "PartyId";
	public static final String CONVERSIONRATE = "ConversionRate";
	public static final String CONVERSIONRATETYPE = "ConversionRateType";
	public static final String OWNERDEALEXPIRATIONDATE = "OwnerDealExpirationDate";
	public static final String OWNERDEALPROTECTEDDATE = "OwnerDealProtectedDate";
	public static final String NAME = "Name";
	public static final String EFFECTIVESTARTDATE = "EffectiveStartDate";
	public static final String EFFECTIVEENDDATE = "EffectiveEndDate";
	public static final String PRTERRITORYVERSIONID = "PrTerritoryVersionId";
	public static final String FORECASTOVERRIDECODE = "ForecastOverrideCode";
	public static final String OWNERORGTREECODE = "OwnerOrgTreeCode";
	public static final String OWNERORGTREESTRUCTCODE = "OwnerOrgTreeStructCode";
	public static final String CREDITRCPTPARTORGPARTYID = "CreditRcptPartOrgPartyId";
	public static final String CREDITRCPTPARTORGPARTYNAME = "CreditRcptPartOrgPartyName";
	public static final String ASSIGNMENTTYPE = "AssignmentType";
	public static final String PRTERRITORYUPDATEDBYID = "PrTerritoryUpdatedById";
	public static final String PRTERRITORYUPDATEDBYPARTYNAME = "PrTerritoryUpdatedByPartyName";
	public static final String SALESACCOUNTID = "SalesAccountId";

	protected void createFields(FieldBuilder fieldBuilder, Connection conn, FeedDefinition parentDefinition) {		fieldBuilder.addField(TARGETPARTYID, new AnalysisDimension());
		fieldBuilder.addField(REVNID, new AnalysisDimension());
		fieldBuilder.addField(BUORGID, new AnalysisDimension());
		fieldBuilder.addField(CUSTOMERACCOUNTID, new AnalysisDimension());
		fieldBuilder.addField(COMMENTS, new AnalysisDimension());
		fieldBuilder.addField(RESOURCEPARTYID, new AnalysisDimension());
		fieldBuilder.addField(OWNERRESOURCEORGID, new AnalysisDimension());
		fieldBuilder.addField(EFFECTIVEDATE, new AnalysisDateDimension());
		fieldBuilder.addField(OPTYID, new AnalysisDimension());
		fieldBuilder.addField(INVENTORYITEMID, new AnalysisDimension());
		fieldBuilder.addField(INVENTORYORGID, new AnalysisDimension());
		fieldBuilder.addField(REVNAMOUNTCURCYCODE, new AnalysisDimension());
		fieldBuilder.addField(TYPECODE, new AnalysisDimension());
		fieldBuilder.addField(WINPROB, new AnalysisMeasure());
		fieldBuilder.addField(EXPECTDLVRYDATE, new AnalysisDateDimension());
		fieldBuilder.addField(UOMCODE, new AnalysisDimension());
		fieldBuilder.addField(CREATIONDATE, new AnalysisDateDimension());
		fieldBuilder.addField(CREATEDBY, new AnalysisDimension());
		fieldBuilder.addField(LASTUPDATEDBY, new AnalysisDimension());
		fieldBuilder.addField(CONFLICTID, new AnalysisDimension());
		fieldBuilder.addField(LASTUPDATELOGIN, new AnalysisDimension());
		fieldBuilder.addField(LASTUPDATEDATE, new AnalysisDateDimension());
		fieldBuilder.addField(USERLASTUPDATEDATE, new AnalysisDateDimension());
		fieldBuilder.addField(PRODGROUPID, new AnalysisDimension());
		fieldBuilder.addField(REVNNUMBER, new AnalysisDimension());
		fieldBuilder.addField(RECURFREQUENCYCODE, new AnalysisDimension());
		fieldBuilder.addField(RECURENDDATE, new AnalysisDateDimension());
		fieldBuilder.addField(RECURPARENTREVNID, new AnalysisDimension());
		fieldBuilder.addField(RECURTYPECODE, new AnalysisDimension());
		fieldBuilder.addField(RECURSTARTDATE, new AnalysisDateDimension());
		fieldBuilder.addField(SALESCREDITTYPECODE, new AnalysisDimension());
		fieldBuilder.addField(SPLITPARENTREVNID, new AnalysisDimension());
		fieldBuilder.addField(SPLITPERCENT, new AnalysisMeasure());
		fieldBuilder.addField(SPLITTYPECODE, new AnalysisDimension());
		fieldBuilder.addField(STATUSCODE, new AnalysisDimension());
		fieldBuilder.addField(CLOSEREASONCODE, new AnalysisDimension());
		fieldBuilder.addField(PRCMPTPARTYID, new AnalysisDimension());
		fieldBuilder.addField(PARTYNAME, new AnalysisDimension());
		fieldBuilder.addField(PARTYID, new AnalysisDimension());
		fieldBuilder.addField(CONVERSIONRATE, new AnalysisMeasure());
		fieldBuilder.addField(CONVERSIONRATETYPE, new AnalysisDimension());
		fieldBuilder.addField(OWNERDEALEXPIRATIONDATE, new AnalysisDateDimension());
		fieldBuilder.addField(OWNERDEALPROTECTEDDATE, new AnalysisDateDimension());
		fieldBuilder.addField(NAME, new AnalysisDimension());
		fieldBuilder.addField(EFFECTIVESTARTDATE, new AnalysisDateDimension());
		fieldBuilder.addField(EFFECTIVEENDDATE, new AnalysisDateDimension());
		fieldBuilder.addField(PRTERRITORYVERSIONID, new AnalysisDimension());
		fieldBuilder.addField(FORECASTOVERRIDECODE, new AnalysisDimension());
		fieldBuilder.addField(OWNERORGTREECODE, new AnalysisDimension());
		fieldBuilder.addField(OWNERORGTREESTRUCTCODE, new AnalysisDimension());
		fieldBuilder.addField(CREDITRCPTPARTORGPARTYID, new AnalysisDimension());
		fieldBuilder.addField(CREDITRCPTPARTORGPARTYNAME, new AnalysisDimension());
		fieldBuilder.addField(ASSIGNMENTTYPE, new AnalysisDimension());
		fieldBuilder.addField(PRTERRITORYUPDATEDBYID, new AnalysisDimension());
		fieldBuilder.addField(PRTERRITORYUPDATEDBYPARTYNAME, new AnalysisDimension());
		fieldBuilder.addField(SALESACCOUNTID, new AnalysisDimension());
	}

@Override
    public DataSet getDataSet(Map<String, Key> keys, Date now, FeedDefinition parentDefinition, IDataStorage IDataStorage, EIConnection conn, String callDataID, Date lastRefreshDate) throws ReportException {
	try {
		DataSet dataSet = new DataSet();
		OracleDataSource oracleDataSource = (OracleDataSource) parentDefinition;
		List<com.easyinsight.datafeeds.oracle.client.SplitRevenue> list = oracleDataSource.getSplitRevenue();
		for (com.easyinsight.datafeeds.oracle.client.SplitRevenue o : list) {
		IRow row = dataSet.createRow();
		row.addValue(keys.get(TARGETPARTYID), String.valueOf(o.getTargetPartyId().getValue()));
		row.addValue(keys.get(REVNID), String.valueOf(o.getRevnId()));
		row.addValue(keys.get(BUORGID), String.valueOf(o.getBUOrgId().getValue()));
		row.addValue(keys.get(CUSTOMERACCOUNTID), String.valueOf(o.getCustomerAccountId().getValue()));
		row.addValue(keys.get(COMMENTS), o.getComments().getValue());
		row.addValue(keys.get(RESOURCEPARTYID), String.valueOf(o.getResourcePartyId().getValue()));
		row.addValue(keys.get(OWNERRESOURCEORGID), String.valueOf(o.getOwnerResourceOrgId().getValue()));
		row.addValue(keys.get(EFFECTIVEDATE), getDate(o.getEffectiveDate()));
		row.addValue(keys.get(OPTYID), String.valueOf(o.getOptyId().getValue()));
		row.addValue(keys.get(INVENTORYITEMID), String.valueOf(o.getInventoryItemId().getValue()));
		row.addValue(keys.get(INVENTORYORGID), String.valueOf(o.getInventoryOrgId().getValue()));
		row.addValue(keys.get(REVNAMOUNTCURCYCODE), o.getRevnAmountCurcyCode().getValue());
		row.addValue(keys.get(TYPECODE), o.getTypeCode().getValue());
		row.addValue(keys.get(WINPROB), getMeasureValue(o.getWinProb()));
		row.addValue(keys.get(EXPECTDLVRYDATE), getDate(o.getExpectDlvryDate()));
		row.addValue(keys.get(UOMCODE), o.getUOMCode().getValue());
		row.addValue(keys.get(CREATIONDATE), getDate(o.getCreationDate()));
		row.addValue(keys.get(CREATEDBY), o.getCreatedBy());
		row.addValue(keys.get(LASTUPDATEDBY), o.getLastUpdatedBy());
		row.addValue(keys.get(CONFLICTID), String.valueOf(o.getConflictId()));
		row.addValue(keys.get(LASTUPDATELOGIN), o.getLastUpdateLogin().getValue());
		row.addValue(keys.get(LASTUPDATEDATE), getDate(o.getLastUpdateDate()));
		row.addValue(keys.get(USERLASTUPDATEDATE), getDate(o.getUserLastUpdateDate()));
		row.addValue(keys.get(PRODGROUPID), String.valueOf(o.getProdGroupId().getValue()));
		row.addValue(keys.get(REVNNUMBER), o.getRevnNumber());
		row.addValue(keys.get(RECURFREQUENCYCODE), o.getRecurFrequencyCode().getValue());
		row.addValue(keys.get(RECURENDDATE), getDate(o.getRecurEndDate()));
		row.addValue(keys.get(RECURPARENTREVNID), String.valueOf(o.getRecurParentRevnId().getValue()));
		row.addValue(keys.get(RECURTYPECODE), o.getRecurTypeCode().getValue());
		row.addValue(keys.get(RECURSTARTDATE), getDate(o.getRecurStartDate()));
		row.addValue(keys.get(SALESCREDITTYPECODE), o.getSalesCreditTypeCode().getValue());
		row.addValue(keys.get(SPLITPARENTREVNID), String.valueOf(o.getSplitParentRevnId().getValue()));
		row.addValue(keys.get(SPLITPERCENT), getMeasureValue(o.getSplitPercent()));
		row.addValue(keys.get(SPLITTYPECODE), o.getSplitTypeCode().getValue());
		row.addValue(keys.get(STATUSCODE), o.getStatusCode().getValue());
		row.addValue(keys.get(CLOSEREASONCODE), o.getCloseReasonCode().getValue());
		row.addValue(keys.get(PRCMPTPARTYID), String.valueOf(o.getPrCmptPartyId().getValue()));
		row.addValue(keys.get(PARTYNAME), o.getPartyName());
		row.addValue(keys.get(PARTYID), String.valueOf(o.getPartyId()));
		row.addValue(keys.get(CONVERSIONRATE), getMeasureValue(o.getConversionRate()));
		row.addValue(keys.get(CONVERSIONRATETYPE), o.getConversionRateType().getValue());
		row.addValue(keys.get(OWNERDEALEXPIRATIONDATE), getDate(o.getOwnerDealExpirationDate()));
		row.addValue(keys.get(OWNERDEALPROTECTEDDATE), getDate(o.getOwnerDealProtectedDate()));
		row.addValue(keys.get(NAME), o.getName());
		row.addValue(keys.get(EFFECTIVESTARTDATE), getDate(o.getEffectiveStartDate()));
		row.addValue(keys.get(EFFECTIVEENDDATE), getDate(o.getEffectiveEndDate()));
		row.addValue(keys.get(PRTERRITORYVERSIONID), String.valueOf(o.getPrTerritoryVersionId().getValue()));
		row.addValue(keys.get(FORECASTOVERRIDECODE), o.getForecastOverrideCode().getValue());
		row.addValue(keys.get(OWNERORGTREECODE), o.getOwnerOrgTreeCode().getValue());
		row.addValue(keys.get(OWNERORGTREESTRUCTCODE), o.getOwnerOrgTreeStructCode().getValue());
		row.addValue(keys.get(CREDITRCPTPARTORGPARTYID), String.valueOf(o.getCreditRcptPartOrgPartyId().getValue()));
		row.addValue(keys.get(CREDITRCPTPARTORGPARTYNAME), o.getCreditRcptPartOrgPartyName());
		row.addValue(keys.get(ASSIGNMENTTYPE), o.getAssignmentType().getValue());
		row.addValue(keys.get(PRTERRITORYUPDATEDBYID), String.valueOf(o.getPrTerritoryUpdatedById().getValue()));
		row.addValue(keys.get(PRTERRITORYUPDATEDBYPARTYNAME), o.getPrTerritoryUpdatedByPartyName());
		row.addValue(keys.get(SALESACCOUNTID), String.valueOf(o.getSalesAccountId().getValue()));
		}

		return dataSet;
	} catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
}

	@Override
    public FeedType getFeedType() {
        return FeedType.ORACLE_SPLITREVENUE;
    }

}