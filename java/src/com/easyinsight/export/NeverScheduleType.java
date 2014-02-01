package com.easyinsight.export;

import java.util.Date;

/**
 * User: jamesboe
 * Date: 1/30/14
 * Time: 9:12 AM
 */
public class NeverScheduleType extends ScheduleType {
    @Override
    public int retrieveType() {
        return ScheduleType.NEVER;
    }

    @Override
    public Date runTime(Date lastTime, Date now) {
        return null;
    }
}
