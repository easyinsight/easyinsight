package com.easyinsight.eventing;

import com.easyinsight.scheduler.RefreshEventInfo;
import com.easyinsight.scheduler.ScheduledTask;
import com.easyinsight.datafeeds.FeedDefinition;

/**
 * Created by IntelliJ IDEA.
 * User: abaldwin
 * Date: Jun 12, 2009
 * Time: 2:29:00 PM
 */
public class AsyncCompletedListener extends EIEventListener {
    public void execute(EIEvent e) {
        AsyncCompletedEvent event = (AsyncCompletedEvent) e;
        RefreshEventInfo info = new RefreshEventInfo();
        ScheduledTask t = event.getTask();
        info.setFeedName(event.getFeedName());
        info.setFeedId(event.getFeedID());
        info.setMessage("Completed!");
        info.setAction(RefreshEventInfo.COMPLETE);
        info.setTaskId(t.getScheduledTaskID());
        info.setUserId(event.getUserID());
        MessageUtils.sendMessage("generalNotifications", info);
    }
}
