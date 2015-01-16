
package com.easyinsight.datafeeds.netsuite.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AsyncSearchResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AsyncSearchResult">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:messages_2014_1.platform.webservices.netsuite.com}AsyncResult">
 *       &lt;sequence>
 *         &lt;element ref="{urn:core_2014_1.platform.webservices.netsuite.com}searchResult"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AsyncSearchResult", namespace = "urn:messages_2014_1.platform.webservices.netsuite.com", propOrder = {
    "searchResult"
})
public class AsyncSearchResult
    extends AsyncResult
{

    @XmlElement(namespace = "urn:core_2014_1.platform.webservices.netsuite.com", required = true)
    protected SearchResult searchResult;

    /**
     * Gets the value of the searchResult property.
     * 
     * @return
     *     possible object is
     *     {@link SearchResult }
     *     
     */
    public SearchResult getSearchResult() {
        return searchResult;
    }

    /**
     * Sets the value of the searchResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchResult }
     *     
     */
    public void setSearchResult(SearchResult value) {
        this.searchResult = value;
    }

}
