package com.easyinsight.api.v2;

import java.util.Date;

/**
 * User: jamesboe
 * Date: Sep 3, 2010
 * Time: 12:48:23 PM
 */
public class SerialDateValue extends SerialValue {
    private Date value;
    private static final long serialVersionUID = -565182371910649050L;


    public Date getValue() {
        return value;
    }

    public void setValue(Date value) {
        this.value = value;
    }
}
