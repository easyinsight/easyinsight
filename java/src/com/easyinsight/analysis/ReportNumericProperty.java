package com.easyinsight.analysis;

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
}
