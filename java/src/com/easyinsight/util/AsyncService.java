package com.easyinsight.util;

/**
 * User: jamesboe
 * Date: 1/3/11
 * Time: 9:20 AM
 */
public class AsyncService {
    public CallData getCallData(String callID) {
        return ServiceUtil.instance().getCallData(callID);
    }
}
