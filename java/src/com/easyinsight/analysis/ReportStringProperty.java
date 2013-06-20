package com.easyinsight.analysis;

import nu.xom.Attribute;
import nu.xom.Element;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * User: jamesboe
 * Date: Jun 2, 2010
 * Time: 9:40:34 AM
 */
@Entity
@Table(name="report_string_property")
public class ReportStringProperty extends ReportProperty {
    @Column(name="property_value")
    private String value = "";

    public ReportStringProperty(String propertyName, String value) {
        super(propertyName);
        this.value = value;
    }

    public ReportStringProperty(String propertyName, String value, boolean enabled) {
        super(propertyName);
        this.value = value;
        setEnabled(enabled);
    }

    public ReportStringProperty() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void cleanup() {
        if (value == null) {
            value = "";
        }
    }

    @Override
    public Element toXML() {
        Element element = new Element("reportStringProperty");
        element.addAttribute(new Attribute("propertyName", getPropertyName()));
        element.appendChild(value);
        return element;
    }

    protected void customFromXML(Element element) {
        if(element.getAttribute("propertyName") != null) {
            setPropertyName(element.getAttribute("propertyName").getValue());
        }
        if(element.getChildCount() > 0) {
            value = element.getChild(0).getValue();
        }
    }
}
