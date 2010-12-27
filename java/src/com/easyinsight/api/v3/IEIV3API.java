package com.easyinsight.api.v3;

import com.easyinsight.api.Row;
import com.easyinsight.api.Where;
import com.easyinsight.api.v2.CommitResult;
import com.easyinsight.api.v2.DataSourceInfo;

import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * User: jamesboe
 * Date: Jun 21, 2010
 * Time: 12:15:12 PM
 */
@WebService(name="EIDataV3", portName = "EIDataV3Port", serviceName = "EIDataV3")
public interface IEIV3API {
    public boolean validateCredentials();

    public String defineCompositeDataSource(@WebParam(name="dataSources") String[] dataSources, @WebParam(name="connections") DataSourceConnection[] connections,
                                          @WebParam(name="dataSourceName") String dataSourceName, @WebParam(name="externalConnectionKey") String externalConnectionKey);

    public String defineDataSource(@WebParam(name = "dataSourceName") String dataSourceName, @WebParam(name="fields") FieldDefinition[] fields,
                                   @WebParam(name="externalConnectionKey") String externalConnectionKey);

    public void replaceRows(@WebParam(name = "dataSourceName") String dataSourceName,
                            @WebParam(name = "rows") Row[] rows,
                            @WebParam(name = "changeDataSourceToMatch") boolean changeDataSourceToMatch);

    void addRow(@WebParam(name = "dataSourceName") String dataSourceName, @WebParam(name = "row") Row row,
                @WebParam(name = "changeDataSourceToMatch") boolean changeDataSourceToMatch);

    void addRows(@WebParam(name = "dataSourceName") String dataSourceName,
                 @WebParam(name = "rows") Row[] rows,
                 @WebParam(name = "changeDataSourceToMatch") boolean changeDataSourceToMatch);

    void updateRow(@WebParam(name = "dataSourceName") String dataSourceName,
                   @WebParam(name = "row") Row row, @WebParam(name = "where") Where where,
                   @WebParam(name = "changeDataSourceToMatch") boolean changeDataSourceToMatch);

    void updateRows(@WebParam(name = "dataSourceName") String dataSourceName,
                    @WebParam(name = "rows") Row[] rows, @WebParam(name = "where") Where where,
                    @WebParam(name = "changeDataSourceToMatch") boolean changeDataSourceToMatch);

    void deleteRows(@WebParam(name = "dataSourceName") String dataSourceName, @WebParam(name = "where") Where where);

    DataSourceInfo getSourceInfo(@WebParam(name = "dataSourceName") String dataSourceName);

    public String beginTransaction(@WebParam(name="dataSourceName") String dataSourceName,
                                   @WebParam(name="transactionOperation") boolean replaceData,
                                   @WebParam(name = "changeDataSourceToMatch") boolean changeDataSourceToMatch);

    public CommitResult commit(@WebParam(name="transactionID") String transactionID);

    public void rollback(@WebParam(name="transactionID") String transactionID);

    void loadRows(@WebParam(name="rows") Row[] rows, @WebParam(name="transactionID") String transactionID);
}
