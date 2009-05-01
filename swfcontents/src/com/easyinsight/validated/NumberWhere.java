/**
 * NumberWhere.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.easyinsight.validated;

public class NumberWhere  implements java.io.Serializable {
    private com.easyinsight.validated.Comparison comparison;

    private java.lang.String key;

    private double value;

    public NumberWhere() {
    }

    public NumberWhere(
           com.easyinsight.validated.Comparison comparison,
           java.lang.String key,
           double value) {
           this.comparison = comparison;
           this.key = key;
           this.value = value;
    }


    /**
     * Gets the comparison value for this NumberWhere.
     * 
     * @return comparison
     */
    public com.easyinsight.validated.Comparison getComparison() {
        return comparison;
    }


    /**
     * Sets the comparison value for this NumberWhere.
     * 
     * @param comparison
     */
    public void setComparison(com.easyinsight.validated.Comparison comparison) {
        this.comparison = comparison;
    }


    /**
     * Gets the key value for this NumberWhere.
     * 
     * @return key
     */
    public java.lang.String getKey() {
        return key;
    }


    /**
     * Sets the key value for this NumberWhere.
     * 
     * @param key
     */
    public void setKey(java.lang.String key) {
        this.key = key;
    }


    /**
     * Gets the value value for this NumberWhere.
     * 
     * @return value
     */
    public double getValue() {
        return value;
    }


    /**
     * Sets the value value for this NumberWhere.
     * 
     * @param value
     */
    public void setValue(double value) {
        this.value = value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NumberWhere)) return false;
        NumberWhere other = (NumberWhere) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.comparison==null && other.getComparison()==null) || 
             (this.comparison!=null &&
              this.comparison.equals(other.getComparison()))) &&
            ((this.key==null && other.getKey()==null) || 
             (this.key!=null &&
              this.key.equals(other.getKey()))) &&
            this.value == other.getValue();
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
        if (getComparison() != null) {
            _hashCode += getComparison().hashCode();
        }
        if (getKey() != null) {
            _hashCode += getKey().hashCode();
        }
        _hashCode += new Double(getValue()).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(NumberWhere.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://basicauth.api.easyinsight.com/", "numberWhere"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("comparison");
        elemField.setXmlName(new javax.xml.namespace.QName("", "comparison"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://basicauth.api.easyinsight.com/", "comparison"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("key");
        elemField.setXmlName(new javax.xml.namespace.QName("", "key"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("value");
        elemField.setXmlName(new javax.xml.namespace.QName("", "value"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
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
