
package com.sforce.soap.partner;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.1 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebFault(name = "InvalidQueryLocatorFault", targetNamespace = "urn:fault.partner.soap.sforce.com")
public class InvalidQueryLocatorFault
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private com.sforce.soap.partner.fault.InvalidQueryLocatorFault faultInfo;

    /**
     * 
     * @param message
     * @param faultInfo
     */
    public InvalidQueryLocatorFault(String message, com.sforce.soap.partner.fault.InvalidQueryLocatorFault faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public InvalidQueryLocatorFault(String message, com.sforce.soap.partner.fault.InvalidQueryLocatorFault faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: com.sforce.soap.partner.fault.InvalidQueryLocatorFault
     */
    public com.sforce.soap.partner.fault.InvalidQueryLocatorFault getFaultInfo() {
        return faultInfo;
    }

}
