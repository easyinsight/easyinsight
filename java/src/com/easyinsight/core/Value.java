package com.easyinsight.core;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Map;

/**
 * User: James Boe
 * Date: Jul 1, 2008
 * Time: 11:00:34 AM
 */
public abstract class Value implements Serializable {
    public static final int STRING = 1;
    public static final int NUMBER = 2;
    public static final int DATE = 3;
    public static final int AGGREGATION = 4;
    public static final int EMPTY = 4;
    private static final long serialVersionUID = 5584087693730331068L;

    // in theory, we have URL, we have image

    public Value() {

    }

    public Value(Value sortValue) {
        this.sortValue = sortValue;
    }

    private Map<String, String> links;

    private Value originalValue;
    private Value sortValue;

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

    public abstract int type();

    public abstract Double toDouble();
}
