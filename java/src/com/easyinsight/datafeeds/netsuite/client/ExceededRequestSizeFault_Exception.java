
package com.easyinsight.datafeeds.netsuite.client;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebFault(name = "exceededRequestSizeFault", targetNamespace = "urn:faults_2014_1.platform.webservices.netsuite.com")
public class ExceededRequestSizeFault_Exception
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private ExceededRequestSizeFault faultInfo;

    /**
     * 
     * @param message
     * @param faultInfo
     */
    public ExceededRequestSizeFault_Exception(String message, ExceededRequestSizeFault faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public ExceededRequestSizeFault_Exception(String message, ExceededRequestSizeFault faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: com.easyinsight.datafeeds.netsuite.client.ExceededRequestSizeFault
     */
    public ExceededRequestSizeFault getFaultInfo() {
        return faultInfo;
    }

}