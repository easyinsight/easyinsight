
package com.sforce.soap.enterprise;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;


/**
 * Sforce SOAP API
 * 
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.1-hudson-2079-RC1
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "SforceService", targetNamespace = "urn:enterprise.soap.sforce.com", wsdlLocation = "file:/files/hudson/workspace/SalesforceToolkit/quickstart_jaxws21/etc/enterprise.wsdl")
public class SforceService
    extends Service
{

    private final static URL SFORCESERVICE_WSDL_LOCATION;

    static {
        URL url = null;
        try {
            url = new URL("file:/files/hudson/workspace/SalesforceToolkit/quickstart_jaxws21/etc/enterprise.wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        SFORCESERVICE_WSDL_LOCATION = url;
    }

    public SforceService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public SforceService() {
        super(SFORCESERVICE_WSDL_LOCATION, new QName("urn:enterprise.soap.sforce.com", "SforceService"));
    }

    /**
     * 
     * @return
     *     returns Soap
     */
    @WebEndpoint(name = "Soap")
    public Soap getSoap() {
        // TODO: ?
        return (Soap)super.getPort(new QName("urn:enterprise.soap.sforce.com", "Soap"), Soap.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns Soap
     */
    @WebEndpoint(name = "Soap")
    public Soap getSoap(WebServiceFeature... features) {
        return (Soap)super.getPort(new QName("urn:enterprise.soap.sforce.com", "Soap"), Soap.class);
    }

}
