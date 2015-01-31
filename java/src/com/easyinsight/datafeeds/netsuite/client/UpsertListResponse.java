
package com.easyinsight.datafeeds.netsuite.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UpsertListResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UpsertListResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:messages_2014_1.platform.webservices.netsuite.com}writeResponseList"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpsertListResponse", namespace = "urn:messages_2014_1.platform.webservices.netsuite.com", propOrder = {
    "writeResponseList"
})
public class UpsertListResponse {

    @XmlElement(required = true)
    protected WriteResponseList writeResponseList;

    /**
     * Gets the value of the writeResponseList property.
     * 
     * @return
     *     possible object is
     *     {@link WriteResponseList }
     *     
     */
    public WriteResponseList getWriteResponseList() {
        return writeResponseList;
    }

    /**
     * Sets the value of the writeResponseList property.
     * 
     * @param value
     *     allowed object is
     *     {@link WriteResponseList }
     *     
     */
    public void setWriteResponseList(WriteResponseList value) {
        this.writeResponseList = value;
    }

}