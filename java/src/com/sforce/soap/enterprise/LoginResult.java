
package com.sforce.soap.enterprise;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LoginResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LoginResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="passwordExpired" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="serverUrl" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sessionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="userId" type="{urn:enterprise.soap.sforce.com}ID"/>
 *         &lt;element name="userInfo" type="{urn:enterprise.soap.sforce.com}GetUserInfoResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LoginResult", propOrder = {
    "passwordExpired",
    "serverUrl",
    "sessionId",
    "userId",
    "userInfo"
})
public class LoginResult {

    protected boolean passwordExpired;
    @XmlElement(required = true, nillable = true)
    protected String serverUrl;
    @XmlElement(required = true, nillable = true)
    protected String sessionId;
    @XmlElement(required = true, nillable = true)
    protected String userId;
    protected GetUserInfoResult userInfo;

    /**
     * Gets the value of the passwordExpired property.
     * 
     */
    public boolean isPasswordExpired() {
        return passwordExpired;
    }

    /**
     * Sets the value of the passwordExpired property.
     * 
     */
    public void setPasswordExpired(boolean value) {
        this.passwordExpired = value;
    }

    /**
     * Gets the value of the serverUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * Sets the value of the serverUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServerUrl(String value) {
        this.serverUrl = value;
    }

    /**
     * Gets the value of the sessionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the value of the sessionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionId(String value) {
        this.sessionId = value;
    }

    /**
     * Gets the value of the userId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the value of the userId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserId(String value) {
        this.userId = value;
    }

    /**
     * Gets the value of the userInfo property.
     * 
     * @return
     *     possible object is
     *     {@link GetUserInfoResult }
     *     
     */
    public GetUserInfoResult getUserInfo() {
        return userInfo;
    }

    /**
     * Sets the value of the userInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetUserInfoResult }
     *     
     */
    public void setUserInfo(GetUserInfoResult value) {
        this.userInfo = value;
    }

}
