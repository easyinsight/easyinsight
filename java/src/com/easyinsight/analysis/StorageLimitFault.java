package com.easyinsight.analysis;

/**
 * User: jamesboe
 * Date: Nov 3, 2010
 * Time: 11:01:28 AM
 */
public class StorageLimitFault extends ReportFault {
    private String message;

    public StorageLimitFault() {
    }

    public StorageLimitFault(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
