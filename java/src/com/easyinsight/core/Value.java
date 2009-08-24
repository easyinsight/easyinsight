package com.easyinsight.core;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

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

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public abstract int type();

    public abstract Double toDouble();
}
