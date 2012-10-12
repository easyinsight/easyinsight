package com.easyinsight.analysis;

import nu.xom.Attribute;
import nu.xom.Element;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * User: jamesboe
 * Date: Jun 2, 2010
 * Time: 9:40:42 AM
 */
@Entity
@Table(name="report_numeric_property")
public class ReportNumericProperty extends ReportProperty {
    @Column(name="property_value")
    private double value;

    public ReportNumericProperty(String propertyName, double value) {
        super(propertyName);
        this.value = value;
    }

    public ReportNumericProperty(String propertyName, double value, boolean enabled) {
        super(propertyName);
        this.value = value;
        setEnabled(enabled);
    }

    public ReportNumericProperty() {
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void cleanup() {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            value = 0;
        }
    }

    @Override
    public Element toXML() {
        Element element = new Element("reportNumericProperty");
        element.addAttribute(new Attribute("propertyName", getPropertyName()));
        element.appendChild(String.valueOf(value));
        return element;
    }

    protected void customFromXML(Element element) {
        setPropertyName(element.getAttribute("propertyName").getValue());
        value = Double.parseDouble(element.getChild(0).getValue());
    }
}
