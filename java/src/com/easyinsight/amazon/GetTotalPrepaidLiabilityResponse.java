/**
 * GetTotalPrepaidLiabilityResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.easyinsight.amazon;

public class GetTotalPrepaidLiabilityResponse  implements java.io.Serializable {
    private com.easyinsight.amazon.OutstandingPrepaidLiability outstandingPrepaidLiability;

    private com.easyinsight.amazon.ResponseStatus status;

    private com.easyinsight.amazon.ServiceError[] errors;

    private java.lang.String requestId;

    public GetTotalPrepaidLiabilityResponse() {
    }

    public GetTotalPrepaidLiabilityResponse(
           com.easyinsight.amazon.OutstandingPrepaidLiability outstandingPrepaidLiability,
           com.easyinsight.amazon.ResponseStatus status,
           com.easyinsight.amazon.ServiceError[] errors,
           java.lang.String requestId) {
           this.outstandingPrepaidLiability = outstandingPrepaidLiability;
           this.status = status;
           this.errors = errors;
           this.requestId = requestId;
    }


    /**
     * Gets the outstandingPrepaidLiability value for this GetTotalPrepaidLiabilityResponse.
     * 
     * @return outstandingPrepaidLiability
     */
    public com.easyinsight.amazon.OutstandingPrepaidLiability getOutstandingPrepaidLiability() {
        return outstandingPrepaidLiability;
    }


    /**
     * Sets the outstandingPrepaidLiability value for this GetTotalPrepaidLiabilityResponse.
     * 
     * @param outstandingPrepaidLiability
     */
    public void setOutstandingPrepaidLiability(com.easyinsight.amazon.OutstandingPrepaidLiability outstandingPrepaidLiability) {
        this.outstandingPrepaidLiability = outstandingPrepaidLiability;
    }


    /**
     * Gets the status value for this GetTotalPrepaidLiabilityResponse.
     * 
     * @return status
     */
    public com.easyinsight.amazon.ResponseStatus getStatus() {
        return status;
    }


    /**
     * Sets the status value for this GetTotalPrepaidLiabilityResponse.
     * 
     * @param status
     */
    public void setStatus(com.easyinsight.amazon.ResponseStatus status) {
        this.status = status;
    }


    /**
     * Gets the errors value for this GetTotalPrepaidLiabilityResponse.
     * 
     * @return errors
     */
    public com.easyinsight.amazon.ServiceError[] getErrors() {
        return errors;
    }


    /**
     * Sets the errors value for this GetTotalPrepaidLiabilityResponse.
     * 
     * @param errors
     */
    public void setErrors(com.easyinsight.amazon.ServiceError[] errors) {
        this.errors = errors;
    }


    /**
     * Gets the requestId value for this GetTotalPrepaidLiabilityResponse.
     * 
     * @return requestId
     */
    public java.lang.String getRequestId() {
        return requestId;
    }


    /**
     * Sets the requestId value for this GetTotalPrepaidLiabilityResponse.
     * 
     * @param requestId
     */
    public void setRequestId(java.lang.String requestId) {
        this.requestId = requestId;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetTotalPrepaidLiabilityResponse)) return false;
        GetTotalPrepaidLiabilityResponse other = (GetTotalPrepaidLiabilityResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.outstandingPrepaidLiability==null && other.getOutstandingPrepaidLiability()==null) || 
             (this.outstandingPrepaidLiability!=null &&
              this.outstandingPrepaidLiability.equals(other.getOutstandingPrepaidLiability()))) &&
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus()))) &&
            ((this.errors==null && other.getErrors()==null) || 
             (this.errors!=null &&
              java.util.Arrays.equals(this.errors, other.getErrors()))) &&
            ((this.requestId==null && other.getRequestId()==null) || 
             (this.requestId!=null &&
              this.requestId.equals(other.getRequestId())));
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
        if (getOutstandingPrepaidLiability() != null) {
            _hashCode += getOutstandingPrepaidLiability().hashCode();
        }
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getErrors() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getErrors());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getErrors(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getRequestId() != null) {
            _hashCode += getRequestId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetTotalPrepaidLiabilityResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://fps.amazonaws.com/doc/2007-01-08/", ">GetTotalPrepaidLiabilityResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("outstandingPrepaidLiability");
        elemField.setXmlName(new javax.xml.namespace.QName("", "OutstandingPrepaidLiability"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://fps.amazonaws.com/doc/2007-01-08/", "OutstandingPrepaidLiability"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://fps.amazonaws.com/doc/2007-01-08/", "ResponseStatus"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errors");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Errors"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://fps.amazonaws.com/doc/2007-01-08/", "ServiceError"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("", "Errors"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("requestId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "RequestId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
