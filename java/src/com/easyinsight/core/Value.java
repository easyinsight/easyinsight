package com.easyinsight.core;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * User: James Boe
 * Date: Jul 1, 2008
 * Time: 11:00:34 AM
 */
public abstract class Value implements Serializable, Comparable<Value>, Cloneable {
    public static final int STRING = 1;
    public static final int NUMBER = 2;
    public static final int DATE = 3;
    public static final int EMPTY = 4;
    public static final int COORDINATE = 5;
    public static final int AGGREGATION = 6;
    public static final int TEXT = 7;
    private static final long serialVersionUID = 5584087693730331068L;

    // in theory, we have URL, we have image

    public Value() {

    }

    public Value(Value sortValue) {
        this.sortValue = sortValue;
    }

    public Value clone() throws CloneNotSupportedException {
        Value value = (Value) super.clone();
        if (originalValue != null) {
            value.setOriginalValue(originalValue.clone());
        }
        if (sortValue != null && sortValue != value) {
            value.setSortValue(sortValue.clone());
        }
        return value;
    }

    private Map<String, String> links;

    private Value originalValue;
    private List<Value> otherValues;
    private Value sortValue;
    private ValueExtension valueExtension;

    public List<Value> getOtherValues() {
        return otherValues;
    }

    public void setOtherValues(List<Value> otherValues) {
        this.otherValues = otherValues;
    }

    public ValueExtension getValueExtension() {
        return valueExtension;
    }

    public void setValueExtension(ValueExtension valueExtension) {
        this.valueExtension = valueExtension;
    }
    
    public Value toSortValue() {
        if (sortValue != null) {
            return sortValue;
        }
        return this;
    }

    public Value getSortValue() {
        return sortValue;
    }

    public void setSortValue(Value sortValue) {
        this.sortValue = sortValue;
    }

    public Value getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(Value originalValue) {
        this.originalValue = originalValue;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }

    public String toHTMLString() {
        return toString();
    }

    public abstract int type();

    public abstract Double toDouble();
}
