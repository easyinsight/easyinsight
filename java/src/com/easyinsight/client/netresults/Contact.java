/**
 * Contact.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.easyinsight.client.netresults;

public class Contact  implements java.io.Serializable {
    /* The Contact's Email Address */
    private java.lang.String contact_email_address;

    /* The Contact's First Name */
    private java.lang.String contact_first_name;

    /* The Contact's Last Name */
    private java.lang.String contact_last_name;

    /* The Contact's Title */
    private java.lang.String contact_title;

    private com.easyinsight.client.netresults.Company company;

    /* The Contact's Address 1 */
    private java.lang.String contact_address_1;

    /* The Contact's Address 2 */
    private java.lang.String contact_address_2;

    private com.easyinsight.client.netresults.City city;

    private com.easyinsight.client.netresults.State state;

    private com.easyinsight.client.netresults.Country country;

    /* The Contact's Postal/Zip code. */
    private java.lang.String contact_postalcode;

    /* The Contact's Home Phone. */
    private java.lang.String contact_home_phone;

    /* The Contact's Work Phone. */
    private java.lang.String contact_work_phone;

    /* The Contact's Mobile Phone. */
    private java.lang.String contact_mobile_phone;

    /* The Contact's Fax. */
    private java.lang.String contact_fax;

    /* The Contact's IP Address. */
    private java.lang.String contact_ip_address;

    /* The Contact's Id. */
    private int contact_id;

    /* The Contact's Lead Score. */
    private int contact_lead_score;

    public Contact() {
    }

    public Contact(
           java.lang.String contact_email_address,
           java.lang.String contact_first_name,
           java.lang.String contact_last_name,
           java.lang.String contact_title,
           com.easyinsight.client.netresults.Company company,
           java.lang.String contact_address_1,
           java.lang.String contact_address_2,
           com.easyinsight.client.netresults.City city,
           com.easyinsight.client.netresults.State state,
           com.easyinsight.client.netresults.Country country,
           java.lang.String contact_postalcode,
           java.lang.String contact_home_phone,
           java.lang.String contact_work_phone,
           java.lang.String contact_mobile_phone,
           java.lang.String contact_fax,
           java.lang.String contact_ip_address,
           int contact_id,
           int contact_lead_score) {
           this.contact_email_address = contact_email_address;
           this.contact_first_name = contact_first_name;
           this.contact_last_name = contact_last_name;
           this.contact_title = contact_title;
           this.company = company;
           this.contact_address_1 = contact_address_1;
           this.contact_address_2 = contact_address_2;
           this.city = city;
           this.state = state;
           this.country = country;
           this.contact_postalcode = contact_postalcode;
           this.contact_home_phone = contact_home_phone;
           this.contact_work_phone = contact_work_phone;
           this.contact_mobile_phone = contact_mobile_phone;
           this.contact_fax = contact_fax;
           this.contact_ip_address = contact_ip_address;
           this.contact_id = contact_id;
           this.contact_lead_score = contact_lead_score;
    }


    /**
     * Gets the contact_email_address value for this Contact.
     * 
     * @return contact_email_address   * The Contact's Email Address
     */
    public java.lang.String getContact_email_address() {
        return contact_email_address;
    }


    /**
     * Sets the contact_email_address value for this Contact.
     * 
     * @param contact_email_address   * The Contact's Email Address
     */
    public void setContact_email_address(java.lang.String contact_email_address) {
        this.contact_email_address = contact_email_address;
    }


    /**
     * Gets the contact_first_name value for this Contact.
     * 
     * @return contact_first_name   * The Contact's First Name
     */
    public java.lang.String getContact_first_name() {
        return contact_first_name;
    }


    /**
     * Sets the contact_first_name value for this Contact.
     * 
     * @param contact_first_name   * The Contact's First Name
     */
    public void setContact_first_name(java.lang.String contact_first_name) {
        this.contact_first_name = contact_first_name;
    }


    /**
     * Gets the contact_last_name value for this Contact.
     * 
     * @return contact_last_name   * The Contact's Last Name
     */
    public java.lang.String getContact_last_name() {
        return contact_last_name;
    }


    /**
     * Sets the contact_last_name value for this Contact.
     * 
     * @param contact_last_name   * The Contact's Last Name
     */
    public void setContact_last_name(java.lang.String contact_last_name) {
        this.contact_last_name = contact_last_name;
    }


    /**
     * Gets the contact_title value for this Contact.
     * 
     * @return contact_title   * The Contact's Title
     */
    public java.lang.String getContact_title() {
        return contact_title;
    }


    /**
     * Sets the contact_title value for this Contact.
     * 
     * @param contact_title   * The Contact's Title
     */
    public void setContact_title(java.lang.String contact_title) {
        this.contact_title = contact_title;
    }


    /**
     * Gets the company value for this Contact.
     * 
     * @return company
     */
    public com.easyinsight.client.netresults.Company getCompany() {
        return company;
    }


    /**
     * Sets the company value for this Contact.
     * 
     * @param company
     */
    public void setCompany(com.easyinsight.client.netresults.Company company) {
        this.company = company;
    }


    /**
     * Gets the contact_address_1 value for this Contact.
     * 
     * @return contact_address_1   * The Contact's Address 1
     */
    public java.lang.String getContact_address_1() {
        return contact_address_1;
    }


    /**
     * Sets the contact_address_1 value for this Contact.
     * 
     * @param contact_address_1   * The Contact's Address 1
     */
    public void setContact_address_1(java.lang.String contact_address_1) {
        this.contact_address_1 = contact_address_1;
    }


    /**
     * Gets the contact_address_2 value for this Contact.
     * 
     * @return contact_address_2   * The Contact's Address 2
     */
    public java.lang.String getContact_address_2() {
        return contact_address_2;
    }


    /**
     * Sets the contact_address_2 value for this Contact.
     * 
     * @param contact_address_2   * The Contact's Address 2
     */
    public void setContact_address_2(java.lang.String contact_address_2) {
        this.contact_address_2 = contact_address_2;
    }


    /**
     * Gets the city value for this Contact.
     * 
     * @return city
     */
    public com.easyinsight.client.netresults.City getCity() {
        return city;
    }


    /**
     * Sets the city value for this Contact.
     * 
     * @param city
     */
    public void setCity(com.easyinsight.client.netresults.City city) {
        this.city = city;
    }


    /**
     * Gets the state value for this Contact.
     * 
     * @return state
     */
    public com.easyinsight.client.netresults.State getState() {
        return state;
    }


    /**
     * Sets the state value for this Contact.
     * 
     * @param state
     */
    public void setState(com.easyinsight.client.netresults.State state) {
        this.state = state;
    }


    /**
     * Gets the country value for this Contact.
     * 
     * @return country
     */
    public com.easyinsight.client.netresults.Country getCountry() {
        return country;
    }


    /**
     * Sets the country value for this Contact.
     * 
     * @param country
     */
    public void setCountry(com.easyinsight.client.netresults.Country country) {
        this.country = country;
    }


    /**
     * Gets the contact_postalcode value for this Contact.
     * 
     * @return contact_postalcode   * The Contact's Postal/Zip code.
     */
    public java.lang.String getContact_postalcode() {
        return contact_postalcode;
    }


    /**
     * Sets the contact_postalcode value for this Contact.
     * 
     * @param contact_postalcode   * The Contact's Postal/Zip code.
     */
    public void setContact_postalcode(java.lang.String contact_postalcode) {
        this.contact_postalcode = contact_postalcode;
    }


    /**
     * Gets the contact_home_phone value for this Contact.
     * 
     * @return contact_home_phone   * The Contact's Home Phone.
     */
    public java.lang.String getContact_home_phone() {
        return contact_home_phone;
    }


    /**
     * Sets the contact_home_phone value for this Contact.
     * 
     * @param contact_home_phone   * The Contact's Home Phone.
     */
    public void setContact_home_phone(java.lang.String contact_home_phone) {
        this.contact_home_phone = contact_home_phone;
    }


    /**
     * Gets the contact_work_phone value for this Contact.
     * 
     * @return contact_work_phone   * The Contact's Work Phone.
     */
    public java.lang.String getContact_work_phone() {
        return contact_work_phone;
    }


    /**
     * Sets the contact_work_phone value for this Contact.
     * 
     * @param contact_work_phone   * The Contact's Work Phone.
     */
    public void setContact_work_phone(java.lang.String contact_work_phone) {
        this.contact_work_phone = contact_work_phone;
    }


    /**
     * Gets the contact_mobile_phone value for this Contact.
     * 
     * @return contact_mobile_phone   * The Contact's Mobile Phone.
     */
    public java.lang.String getContact_mobile_phone() {
        return contact_mobile_phone;
    }


    /**
     * Sets the contact_mobile_phone value for this Contact.
     * 
     * @param contact_mobile_phone   * The Contact's Mobile Phone.
     */
    public void setContact_mobile_phone(java.lang.String contact_mobile_phone) {
        this.contact_mobile_phone = contact_mobile_phone;
    }


    /**
     * Gets the contact_fax value for this Contact.
     * 
     * @return contact_fax   * The Contact's Fax.
     */
    public java.lang.String getContact_fax() {
        return contact_fax;
    }


    /**
     * Sets the contact_fax value for this Contact.
     * 
     * @param contact_fax   * The Contact's Fax.
     */
    public void setContact_fax(java.lang.String contact_fax) {
        this.contact_fax = contact_fax;
    }


    /**
     * Gets the contact_ip_address value for this Contact.
     * 
     * @return contact_ip_address   * The Contact's IP Address.
     */
    public java.lang.String getContact_ip_address() {
        return contact_ip_address;
    }


    /**
     * Sets the contact_ip_address value for this Contact.
     * 
     * @param contact_ip_address   * The Contact's IP Address.
     */
    public void setContact_ip_address(java.lang.String contact_ip_address) {
        this.contact_ip_address = contact_ip_address;
    }


    /**
     * Gets the contact_id value for this Contact.
     * 
     * @return contact_id   * The Contact's Id.
     */
    public int getContact_id() {
        return contact_id;
    }


    /**
     * Sets the contact_id value for this Contact.
     * 
     * @param contact_id   * The Contact's Id.
     */
    public void setContact_id(int contact_id) {
        this.contact_id = contact_id;
    }


    /**
     * Gets the contact_lead_score value for this Contact.
     * 
     * @return contact_lead_score   * The Contact's Lead Score.
     */
    public int getContact_lead_score() {
        return contact_lead_score;
    }


    /**
     * Sets the contact_lead_score value for this Contact.
     * 
     * @param contact_lead_score   * The Contact's Lead Score.
     */
    public void setContact_lead_score(int contact_lead_score) {
        this.contact_lead_score = contact_lead_score;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Contact)) return false;
        Contact other = (Contact) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.contact_email_address==null && other.getContact_email_address()==null) || 
             (this.contact_email_address!=null &&
              this.contact_email_address.equals(other.getContact_email_address()))) &&
            ((this.contact_first_name==null && other.getContact_first_name()==null) || 
             (this.contact_first_name!=null &&
              this.contact_first_name.equals(other.getContact_first_name()))) &&
            ((this.contact_last_name==null && other.getContact_last_name()==null) || 
             (this.contact_last_name!=null &&
              this.contact_last_name.equals(other.getContact_last_name()))) &&
            ((this.contact_title==null && other.getContact_title()==null) || 
             (this.contact_title!=null &&
              this.contact_title.equals(other.getContact_title()))) &&
            ((this.company==null && other.getCompany()==null) || 
             (this.company!=null &&
              this.company.equals(other.getCompany()))) &&
            ((this.contact_address_1==null && other.getContact_address_1()==null) || 
             (this.contact_address_1!=null &&
              this.contact_address_1.equals(other.getContact_address_1()))) &&
            ((this.contact_address_2==null && other.getContact_address_2()==null) || 
             (this.contact_address_2!=null &&
              this.contact_address_2.equals(other.getContact_address_2()))) &&
            ((this.city==null && other.getCity()==null) || 
             (this.city!=null &&
              this.city.equals(other.getCity()))) &&
            ((this.state==null && other.getState()==null) || 
             (this.state!=null &&
              this.state.equals(other.getState()))) &&
            ((this.country==null && other.getCountry()==null) || 
             (this.country!=null &&
              this.country.equals(other.getCountry()))) &&
            ((this.contact_postalcode==null && other.getContact_postalcode()==null) || 
             (this.contact_postalcode!=null &&
              this.contact_postalcode.equals(other.getContact_postalcode()))) &&
            ((this.contact_home_phone==null && other.getContact_home_phone()==null) || 
             (this.contact_home_phone!=null &&
              this.contact_home_phone.equals(other.getContact_home_phone()))) &&
            ((this.contact_work_phone==null && other.getContact_work_phone()==null) || 
             (this.contact_work_phone!=null &&
              this.contact_work_phone.equals(other.getContact_work_phone()))) &&
            ((this.contact_mobile_phone==null && other.getContact_mobile_phone()==null) || 
             (this.contact_mobile_phone!=null &&
              this.contact_mobile_phone.equals(other.getContact_mobile_phone()))) &&
            ((this.contact_fax==null && other.getContact_fax()==null) || 
             (this.contact_fax!=null &&
              this.contact_fax.equals(other.getContact_fax()))) &&
            ((this.contact_ip_address==null && other.getContact_ip_address()==null) || 
             (this.contact_ip_address!=null &&
              this.contact_ip_address.equals(other.getContact_ip_address()))) &&
            this.contact_id == other.getContact_id() &&
            this.contact_lead_score == other.getContact_lead_score();
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getContact_email_address() != null) {
            _hashCode += getContact_email_address().hashCode();
        }
        if (getContact_first_name() != null) {
            _hashCode += getContact_first_name().hashCode();
        }
        if (getContact_last_name() != null) {
            _hashCode += getContact_last_name().hashCode();
        }
        if (getContact_title() != null) {
            _hashCode += getContact_title().hashCode();
        }
        if (getCompany() != null) {
            _hashCode += getCompany().hashCode();
        }
        if (getContact_address_1() != null) {
            _hashCode += getContact_address_1().hashCode();
        }
        if (getContact_address_2() != null) {
            _hashCode += getContact_address_2().hashCode();
        }
        if (getCity() != null) {
            _hashCode += getCity().hashCode();
        }
        if (getState() != null) {
            _hashCode += getState().hashCode();
        }
        if (getCountry() != null) {
            _hashCode += getCountry().hashCode();
        }
        if (getContact_postalcode() != null) {
            _hashCode += getContact_postalcode().hashCode();
        }
        if (getContact_home_phone() != null) {
            _hashCode += getContact_home_phone().hashCode();
        }
        if (getContact_work_phone() != null) {
            _hashCode += getContact_work_phone().hashCode();
        }
        if (getContact_mobile_phone() != null) {
            _hashCode += getContact_mobile_phone().hashCode();
        }
        if (getContact_fax() != null) {
            _hashCode += getContact_fax().hashCode();
        }
        if (getContact_ip_address() != null) {
            _hashCode += getContact_ip_address().hashCode();
        }
        _hashCode += getContact_id();
        _hashCode += getContact_lead_score();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Contact.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("https://apps.net-results.com/soap/v1/NRAPI.xsd", "Contact"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contact_email_address");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contact_email_address"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contact_first_name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contact_first_name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contact_last_name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contact_last_name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contact_title");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contact_title"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("company");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Company"));
        elemField.setXmlType(new javax.xml.namespace.QName("https://apps.net-results.com/soap/v1/NRAPI.xsd", "Company"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contact_address_1");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contact_address_1"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contact_address_2");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contact_address_2"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("city");
        elemField.setXmlName(new javax.xml.namespace.QName("", "City"));
        elemField.setXmlType(new javax.xml.namespace.QName("https://apps.net-results.com/soap/v1/NRAPI.xsd", "City"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("state");
        elemField.setXmlName(new javax.xml.namespace.QName("", "State"));
        elemField.setXmlType(new javax.xml.namespace.QName("https://apps.net-results.com/soap/v1/NRAPI.xsd", "State"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("country");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Country"));
        elemField.setXmlType(new javax.xml.namespace.QName("https://apps.net-results.com/soap/v1/NRAPI.xsd", "Country"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contact_postalcode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contact_postalcode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contact_home_phone");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contact_home_phone"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contact_work_phone");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contact_work_phone"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contact_mobile_phone");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contact_mobile_phone"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contact_fax");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contact_fax"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contact_ip_address");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contact_ip_address"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contact_id");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contact_id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contact_lead_score");
        elemField.setXmlName(new javax.xml.namespace.QName("", "contact_lead_score"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
