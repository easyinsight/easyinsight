package com.easyinsight.admin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.MessageFormat;

/**
 * User: James Boe
 * Date: Aug 17, 2008
 * Time: 1:49:49 PM
 */
public class HealthInfo implements Serializable {

    private static final String HEALTH_XML = "\t\t<health>\r\n\t\t\t<currentMemory>{0}</currentMemory>\r\n" +
            "\t\t\t<freeMemory>{1}</freeMemory>\r\n\t\t\t<maxMemory>{2}</maxMemory>\r\n\t\t\t<threadCount>{3}</threadCount>\r\n" +
            "\t\t\t<activeDBConnections>{4}</activeDBConnections>\r\n\t\t\t<activeUsers>{5}</activeUsers>\r\n\t\t\t<systemLoadAverage>{6}</systemLoadAverage>\r\n" +
            "\t\t\t<clientCount>{7}</clientCount>\r\n\t\t\t<server>{8}</server>\r\n\t\t\t<schedulerThreads>{9}/{10}</schedulerThreads>\n" +
            "\t\t\t<claimed>{11}</claimed>\r\n\t\t</health>\r\n";

    private long currentMemory;
    private long freeMemory;
    private long maxMemory;
    private int threadCount;
    private int activeDBConnections;
    private int idleDBConnections;
    private int activeUsers;
    private long majorCollectionCount;
    private long majorCollectionTime;
    private long minorCollectionCount;
    private long minorCollectionTime;
    private double systemLoadAverage;
    private long compilationTime;
    private int clientCount;

    private int maxSchedulerThreads;
    private int currentSchedulerThreads;
    private String server;
    private int claimedTasks;

    public String toXML() {
        return MessageFormat.format(HEALTH_XML, currentMemory, freeMemory, maxMemory, threadCount, activeDBConnections,
                activeUsers, systemLoadAverage, clientCount, server, currentSchedulerThreads, maxSchedulerThreads, claimedTasks);
    }

    public int getClaimedTasks() {
        return claimedTasks;
    }

    public void setClaimedTasks(int claimedTasks) {
        this.claimedTasks = claimedTasks;
    }

    public int getMaxSchedulerThreads() {
        return maxSchedulerThreads;
    }

    public void setMaxSchedulerThreads(int maxSchedulerThreads) {
        this.maxSchedulerThreads = maxSchedulerThreads;
    }

    public int getCurrentSchedulerThreads() {
        return currentSchedulerThreads;
    }

    public void setCurrentSchedulerThreads(int currentSchedulerThreads) {
        this.currentSchedulerThreads = currentSchedulerThreads;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public int getClientCount() {
        return clientCount;
    }

    public void setClientCount(int clientCount) {
        this.clientCount = clientCount;
    }

    public long getCompilationTime() {
        return compilationTime;
    }

    public void setCompilationTime(long compilationTime) {
        this.compilationTime = compilationTime;
    }

    public long getMajorCollectionCount() {
        return majorCollectionCount;
    }

    public void setMajorCollectionCount(long majorCollectionCount) {
        this.majorCollectionCount = majorCollectionCount;
    }

    public long getMajorCollectionTime() {
        return majorCollectionTime;
    }

    public void setMajorCollectionTime(long majorCollectionTime) {
        this.majorCollectionTime = majorCollectionTime;
    }

    public long getMinorCollectionCount() {
        return minorCollectionCount;
    }

    public void setMinorCollectionCount(long minorCollectionCount) {
        this.minorCollectionCount = minorCollectionCount;
    }

    public long getMinorCollectionTime() {
        return minorCollectionTime;
    }

    public void setMinorCollectionTime(long minorCollectionTime) {
        this.minorCollectionTime = minorCollectionTime;
    }

    public double getSystemLoadAverage() {
        return systemLoadAverage;
    }

    public void setSystemLoadAverage(double systemLoadAverage) {
        this.systemLoadAverage = systemLoadAverage;
    }

    public long getCurrentMemory() {
        return currentMemory;
    }

    public void setCurrentMemory(long currentMemory) {
        this.currentMemory = currentMemory;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public void setFreeMemory(long freeMemory) {
        this.freeMemory = freeMemory;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getActiveDBConnections() {
        return activeDBConnections;
    }

    public void setActiveDBConnections(int activeDBConnections) {
        this.activeDBConnections = activeDBConnections;
    }

    public int getIdleDBConnections() {
        return idleDBConnections;
    }

    public void setIdleDBConnections(int idleDBConnections) {
        this.idleDBConnections = idleDBConnections;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("current_memory", currentMemory);
        jo.put("free_memory", freeMemory);
        jo.put("max_memory", maxMemory);
        jo.put("thread_count", threadCount);
        jo.put("active_db_connections", activeDBConnections);
        jo.put("idle_db_connections", idleDBConnections);
        jo.put("active_users", activeUsers);
        jo.put("major_collection_count", majorCollectionCount);
        jo.put("major_collection_time", majorCollectionTime);
        jo.put("minor_collection_count", minorCollectionCount);
        jo.put("minor_collection_time", minorCollectionTime);
        jo.put("system_load_average", systemLoadAverage);
        jo.put("compilation_time", compilationTime);
        jo.put("client_count", clientCount);
        jo.put("server", server);
        jo.put("current_scheduler_threads", currentSchedulerThreads);
        jo.put("max_scheduler_threads", maxSchedulerThreads);
        jo.put("claimed_tasks", claimedTasks);
        return jo;
    }
}
