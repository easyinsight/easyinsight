package com.easyinsight.core;

import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.Nullable;
import org.owasp.html.*;

import java.io.Serializable;

/**
 * User: James Boe
 * Date: Jul 1, 2008
 * Time: 11:01:28 AM
 */
public class StringValue extends Value implements Serializable {    

    private String value;
    private static final long serialVersionUID = -3662307504638205531L;

    private static PolicyFactory policy = new HtmlPolicyBuilder()
            .allowElements("li")
            .allowElements("ul")
            .toFactory();

    public StringValue() {
    }

    @Override
    public String toHTMLString() {


// Sanitize your output.
        //return policy.sanitize(value);
        /*HtmlSanitizer.sanitize(value);*/
        return StringEscapeUtils.escapeHtml(value);
    }

    @Override
    public String toString() {
        return value;
    }

    public StringValue(String value) {
        this.value = value;
    }

    public StringValue(String value, Value originalValue) {
        super(originalValue);
        setSortValue(originalValue);
        this.value = value;
    }

    public StringValue(String value, Value originalValue, Value sortValue) {
        this.value = value;
        setOriginalValue(originalValue);
        setSortValue(sortValue);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public int type() {
        return Value.STRING;
    }

    @Nullable
    public Double toDouble() {
        try {
            return NumericValue.produceDoubleValue(value);
        } catch (NumberFormatException e) {
            return 0.;
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) {
            return false;
        }
        if (o instanceof EmptyValue) {
            return value.equals("");
        }
        if (getClass() != o.getClass()) return false;

        StringValue that = (StringValue) o;

        return value.equals(that.value);

    }

    public int hashCode() {
        return value.hashCode();
    }

    public int compareTo(Value value) {
        if (value.type() == Value.STRING) {
            StringValue stringValue2 = (StringValue) value;
            return this.getValue().compareTo(stringValue2.getValue());
        } else if (value.type() == Value.EMPTY) {
            return -1;
        }
        return 0;
    }
}
