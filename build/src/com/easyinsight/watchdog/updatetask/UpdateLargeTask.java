package com.easyinsight.watchdog.updatetask;

/**
 * Created by IntelliJ IDEA.
 * User: abaldwin
 * Date: Aug 5, 2009
 * Time: 3:20:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateLargeTask extends UpdateAppInstanceTask {
    @Override
    protected String getAMI() {
        return LARGE_AMI;
    }
    @Override
    protected String getRole() {
        return "Frontend";
    }
}
