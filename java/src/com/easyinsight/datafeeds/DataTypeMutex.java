package com.easyinsight.datafeeds;

import com.easyinsight.eventing.MessageUtils;
import com.easyinsight.scorecard.DataSourceRefreshEvent;
import com.easyinsight.security.SecurityUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * User: jamesboe
 * Date: Apr 26, 2010
 * Time: 10:54:19 AM
 */
public class DataTypeMutex {

    private static final DataTypeMutex mutex = new DataTypeMutex();

    private Map<FeedType, Semaphore> mutexMap = new HashMap<FeedType, Semaphore>();

    private Set<FeedType> lockRequiredTypes;
    private Set<FeedType> superLockRequiredTypes;

    private DataTypeMutex() {
        lockRequiredTypes = new HashSet<FeedType>();
        superLockRequiredTypes = new HashSet<FeedType>();
        lockRequiredTypes.add(FeedType.BASECAMP_MASTER);
        lockRequiredTypes.add(FeedType.HIGHRISE_COMPOSITE);
        lockRequiredTypes.add(FeedType.PIVOTAL_TRACKER);
        lockRequiredTypes.add(FeedType.WHOLE_FOODS);
        lockRequiredTypes.add(FeedType.CONSTANT_CONTACT);
        superLockRequiredTypes.add(FeedType.ZENDESK_COMPOSITE);
        for (FeedType feedType : lockRequiredTypes) {
            Semaphore semaphore = new Semaphore(3, true);
            mutexMap.put(feedType, semaphore);
        }
        for (FeedType feedType : superLockRequiredTypes) {
            Semaphore semaphore = new Semaphore(1, true);
            mutexMap.put(feedType, semaphore);
        }
    }

    public static DataTypeMutex mutex() {
        return mutex;
    }

    public void lock(FeedType feedType, long dataSourceID) {
        if (lockRequiredTypes.contains(feedType) || superLockRequiredTypes.contains(feedType)) {
            Semaphore semaphore = mutexMap.get(feedType);
            if (semaphore.tryAcquire()) {
            } else {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                }
            }

        }
    }

    public void unlock(FeedType feedType) {
        if (lockRequiredTypes.contains(feedType) || superLockRequiredTypes.contains(feedType)) {
            Semaphore semaphore = mutexMap.get(feedType);
            semaphore.release();
        }
    }
}