
package com.easyinsight.datafeeds.netsuite.client;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ExpenseCategorySearchRow complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ExpenseCategorySearchRow">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:core_2014_1.platform.webservices.netsuite.com}SearchRow">
 *       &lt;sequence>
 *         &lt;element name="basic" type="{urn:common_2014_1.platform.webservices.netsuite.com}ExpenseCategorySearchRowBasic" minOccurs="0"/>
 *         &lt;element name="userJoin" type="{urn:common_2014_1.platform.webservices.netsuite.com}EmployeeSearchRowBasic" minOccurs="0"/>
 *         &lt;element name="customSearchJoin" type="{urn:common_2014_1.platform.webservices.netsuite.com}CustomSearchRowBasic" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExpenseCategorySearchRow", propOrder = {
    "basic",
    "userJoin",
    "customSearchJoin"
})
public class ExpenseCategorySearchRow
    extends SearchRow
{

    protected ExpenseCategorySearchRowBasic basic;
    protected EmployeeSearchRowBasic userJoin;
    protected List<CustomSearchRowBasic> customSearchJoin;

    /**
     * Gets the value of the basic property.
     * 
     * @return
     *     possible object is
     *     {@link ExpenseCategorySearchRowBasic }
     *     
     */
    public ExpenseCategorySearchRowBasic getBasic() {
        return basic;
    }

    /**
     * Sets the value of the basic property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExpenseCategorySearchRowBasic }
     *     
     */
    public void setBasic(ExpenseCategorySearchRowBasic value) {
        this.basic = value;
    }

    /**
     * Gets the value of the userJoin property.
     * 
     * @return
     *     possible object is
     *     {@link EmployeeSearchRowBasic }
     *     
     */
    public EmployeeSearchRowBasic getUserJoin() {
        return userJoin;
    }

    /**
     * Sets the value of the userJoin property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmployeeSearchRowBasic }
     *     
     */
    public void setUserJoin(EmployeeSearchRowBasic value) {
        this.userJoin = value;
    }

    /**
     * Gets the value of the customSearchJoin property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the customSearchJoin property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCustomSearchJoin().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CustomSearchRowBasic }
     * 
     * 
     */
    public List<CustomSearchRowBasic> getCustomSearchJoin() {
        if (customSearchJoin == null) {
            customSearchJoin = new ArrayList<CustomSearchRowBasic>();
        }
        return this.customSearchJoin;
    }

}
