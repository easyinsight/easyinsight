
package com.easyinsight.datafeeds.netsuite.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AppPackageSearchBasic complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AppPackageSearchBasic">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:core_2014_1.platform.webservices.netsuite.com}SearchRecordBasic">
 *       &lt;sequence>
 *         &lt;element name="appDefinition" type="{urn:core_2014_1.platform.webservices.netsuite.com}SearchMultiSelectField" minOccurs="0"/>
 *         &lt;element name="description" type="{urn:core_2014_1.platform.webservices.netsuite.com}SearchStringField" minOccurs="0"/>
 *         &lt;element name="externalId" type="{urn:core_2014_1.platform.webservices.netsuite.com}SearchMultiSelectField" minOccurs="0"/>
 *         &lt;element name="externalIdString" type="{urn:core_2014_1.platform.webservices.netsuite.com}SearchStringField" minOccurs="0"/>
 *         &lt;element name="internalId" type="{urn:core_2014_1.platform.webservices.netsuite.com}SearchMultiSelectField" minOccurs="0"/>
 *         &lt;element name="internalIdNumber" type="{urn:core_2014_1.platform.webservices.netsuite.com}SearchLongField" minOccurs="0"/>
 *         &lt;element name="version" type="{urn:core_2014_1.platform.webservices.netsuite.com}SearchStringField" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AppPackageSearchBasic", namespace = "urn:common_2014_1.platform.webservices.netsuite.com", propOrder = {
    "appDefinition",
    "description",
    "externalId",
    "externalIdString",
    "internalId",
    "internalIdNumber",
    "version"
})
public class AppPackageSearchBasic
    extends SearchRecordBasic
{

    protected SearchMultiSelectField appDefinition;
    protected SearchStringField description;
    protected SearchMultiSelectField externalId;
    protected SearchStringField externalIdString;
    protected SearchMultiSelectField internalId;
    protected SearchLongField internalIdNumber;
    protected SearchStringField version;

    /**
     * Gets the value of the appDefinition property.
     * 
     * @return
     *     possible object is
     *     {@link SearchMultiSelectField }
     *     
     */
    public SearchMultiSelectField getAppDefinition() {
        return appDefinition;
    }

    /**
     * Sets the value of the appDefinition property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchMultiSelectField }
     *     
     */
    public void setAppDefinition(SearchMultiSelectField value) {
        this.appDefinition = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link SearchStringField }
     *     
     */
    public SearchStringField getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchStringField }
     *     
     */
    public void setDescription(SearchStringField value) {
        this.description = value;
    }

    /**
     * Gets the value of the externalId property.
     * 
     * @return
     *     possible object is
     *     {@link SearchMultiSelectField }
     *     
     */
    public SearchMultiSelectField getExternalId() {
        return externalId;
    }

    /**
     * Sets the value of the externalId property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchMultiSelectField }
     *     
     */
    public void setExternalId(SearchMultiSelectField value) {
        this.externalId = value;
    }

    /**
     * Gets the value of the externalIdString property.
     * 
     * @return
     *     possible object is
     *     {@link SearchStringField }
     *     
     */
    public SearchStringField getExternalIdString() {
        return externalIdString;
    }

    /**
     * Sets the value of the externalIdString property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchStringField }
     *     
     */
    public void setExternalIdString(SearchStringField value) {
        this.externalIdString = value;
    }

    /**
     * Gets the value of the internalId property.
     * 
     * @return
     *     possible object is
     *     {@link SearchMultiSelectField }
     *     
     */
    public SearchMultiSelectField getInternalId() {
        return internalId;
    }

    /**
     * Sets the value of the internalId property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchMultiSelectField }
     *     
     */
    public void setInternalId(SearchMultiSelectField value) {
        this.internalId = value;
    }

    /**
     * Gets the value of the internalIdNumber property.
     * 
     * @return
     *     possible object is
     *     {@link SearchLongField }
     *     
     */
    public SearchLongField getInternalIdNumber() {
        return internalIdNumber;
    }

    /**
     * Sets the value of the internalIdNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchLongField }
     *     
     */
    public void setInternalIdNumber(SearchLongField value) {
        this.internalIdNumber = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link SearchStringField }
     *     
     */
    public SearchStringField getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchStringField }
     *     
     */
    public void setVersion(SearchStringField value) {
        this.version = value;
    }

}
