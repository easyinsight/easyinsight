
package com.easyinsight.api.unchecked;

import java.util.List;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.1-02/02/2007 03:56 AM(vivekp)-FCS
 * Generated source version: 2.1
 * 
 */
@WebService(name = "BasicAuthUncheckedPublish", targetNamespace = "http://basicauth.api.easyinsight.com/")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface BasicAuthUncheckedPublish {


    /**
     * 
     * @param where
     * @param dataSourceName
     * @param row
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "updateRow", targetNamespace = "http://basicauth.api.easyinsight.com/", className = "com.easyinsight.api.unchecked.UpdateRow")
    @ResponseWrapper(localName = "updateRowResponse", targetNamespace = "http://basicauth.api.easyinsight.com/", className = "com.easyinsight.api.unchecked.UpdateRowResponse")
    public String updateRow(
        @WebParam(name = "dataSourceName", targetNamespace = "")
        String dataSourceName,
        @WebParam(name = "row", targetNamespace = "")
        Row row,
        @WebParam(name = "where", targetNamespace = "")
        Where where);

    /**
     * 
     * @param dataSourceName
     * @param rows
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "addRows", targetNamespace = "http://basicauth.api.easyinsight.com/", className = "com.easyinsight.api.unchecked.AddRows")
    @ResponseWrapper(localName = "addRowsResponse", targetNamespace = "http://basicauth.api.easyinsight.com/", className = "com.easyinsight.api.unchecked.AddRowsResponse")
    public String addRows(
        @WebParam(name = "dataSourceName", targetNamespace = "")
        String dataSourceName,
        @WebParam(name = "rows", targetNamespace = "")
        List<Row> rows);

    /**
     * 
     * @param dataSourceName
     * @param rows
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "replaceRows", targetNamespace = "http://basicauth.api.easyinsight.com/", className = "com.easyinsight.api.unchecked.ReplaceRows")
    @ResponseWrapper(localName = "replaceRowsResponse", targetNamespace = "http://basicauth.api.easyinsight.com/", className = "com.easyinsight.api.unchecked.ReplaceRowsResponse")
    public String replaceRows(
        @WebParam(name = "dataSourceName", targetNamespace = "")
        String dataSourceName,
        @WebParam(name = "rows", targetNamespace = "")
        List<Row> rows);

    /**
     * 
     * @return
     *     returns boolean
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "validateCredentials", targetNamespace = "http://basicauth.api.easyinsight.com/", className = "com.easyinsight.api.unchecked.ValidateCredentials")
    @ResponseWrapper(localName = "validateCredentialsResponse", targetNamespace = "http://basicauth.api.easyinsight.com/", className = "com.easyinsight.api.unchecked.ValidateCredentialsResponse")
    public boolean validateCredentials();

    /**
     * 
     * @param where
     * @param dataSourceName
     */
    @WebMethod
    @RequestWrapper(localName = "deleteRows", targetNamespace = "http://basicauth.api.easyinsight.com/", className = "com.easyinsight.api.unchecked.DeleteRows")
    @ResponseWrapper(localName = "deleteRowsResponse", targetNamespace = "http://basicauth.api.easyinsight.com/", className = "com.easyinsight.api.unchecked.DeleteRowsResponse")
    public void deleteRows(
        @WebParam(name = "dataSourceName", targetNamespace = "")
        String dataSourceName,
        @WebParam(name = "where", targetNamespace = "")
        Where where);

    /**
     * 
     * @param where
     * @param dataSourceName
     * @param rows
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "updateRows", targetNamespace = "http://basicauth.api.easyinsight.com/", className = "com.easyinsight.api.unchecked.UpdateRows")
    @ResponseWrapper(localName = "updateRowsResponse", targetNamespace = "http://basicauth.api.easyinsight.com/", className = "com.easyinsight.api.unchecked.UpdateRowsResponse")
    public String updateRows(
        @WebParam(name = "dataSourceName", targetNamespace = "")
        String dataSourceName,
        @WebParam(name = "rows", targetNamespace = "")
        List<Row> rows,
        @WebParam(name = "where", targetNamespace = "")
        Where where);

    /**
     * 
     * @param dataSourceName
     * @param row
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "addRow", targetNamespace = "http://basicauth.api.easyinsight.com/", className = "com.easyinsight.api.unchecked.AddRow")
    @ResponseWrapper(localName = "addRowResponse", targetNamespace = "http://basicauth.api.easyinsight.com/", className = "com.easyinsight.api.unchecked.AddRowResponse")
    public String addRow(
        @WebParam(name = "dataSourceName", targetNamespace = "")
        String dataSourceName,
        @WebParam(name = "row", targetNamespace = "")
        Row row);

    /**
     * 
     * @param dataSourceKey
     */
    @WebMethod
    @RequestWrapper(localName = "disableUnchecked", targetNamespace = "http://basicauth.api.easyinsight.com/", className = "com.easyinsight.api.unchecked.DisableUnchecked")
    @ResponseWrapper(localName = "disableUncheckedResponse", targetNamespace = "http://basicauth.api.easyinsight.com/", className = "com.easyinsight.api.unchecked.DisableUncheckedResponse")
    public void disableUnchecked(
        @WebParam(name = "dataSourceKey", targetNamespace = "")
        String dataSourceKey);

}
