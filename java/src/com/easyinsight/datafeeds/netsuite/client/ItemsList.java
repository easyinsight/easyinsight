
package com.easyinsight.datafeeds.netsuite.client;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ItemsList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ItemsList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="items" type="{urn:core_2014_1.platform.webservices.netsuite.com}RecordRef" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemsList", namespace = "urn:customization_2014_1.setup.webservices.netsuite.com", propOrder = {
    "items"
})
public class ItemsList {

    @XmlElement(required = true)
    protected List<RecordRef> items;

    /**
     * Gets the value of the items property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the items property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItems().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RecordRef }
     * 
     * 
     */
    public List<RecordRef> getItems() {
        if (items == null) {
            items = new ArrayList<RecordRef>();
        }
        return this.items;
    }

}
