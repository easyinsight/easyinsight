package com.easyinsight.watchdog.updatetask;

/**
 * Created by IntelliJ IDEA.
 * User: abaldwin
 * Date: Aug 5, 2009
 * Time: 3:19:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShutdownJava8Task extends ShutdownAppInstanceTask {
    @Override
    protected String getAMI() {
        return JAVA_8_AMI;
    }
}