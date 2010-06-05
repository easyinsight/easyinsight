package com.easyinsight.scheduler;

import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.logging.LogClass;

import java.util.concurrent.*;
import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.Session;
import org.hibernate.Query;

/**
 * User: James Boe
 * Date: Apr 15, 2009
 * Time: 4:38:42 PM
 */
public class Scheduler {

    public static final String[] taskClassArray = { "com.easyinsight.users.AccountTimeScheduler",
        "com.easyinsight.database.DatabaseVolumeScheduler", "com.easyinsight.billing.BillingTaskGenerator"};

    public static final int TASK_LIMIT = 5;

    public static final int ONE_MINUTE = 60000;

    public static final String SCHEDULE_LOCK = "SCHEDULE";

    private ThreadPoolExecutor executor;
    private Timer timer;
    private boolean running = false;
    private Thread thread;

    private static Scheduler instance = new Scheduler();

    public static Scheduler instance() {
        return instance;
    }

    // simplest level...

    private Scheduler() {
        executor = new ThreadPoolExecutor(5, 5, 5000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        timer = new Timer();        
    }

    public void start() {
        running = true;        
        long nextMinute = System.currentTimeMillis() / ONE_MINUTE * ONE_MINUTE + ONE_MINUTE;
        timer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                if (running) {
                    scheduleTasks();
                }
            }
        }, new Date(nextMinute), ONE_MINUTE);
        launchThread();
        assignDefaultGenerators();
    }

    public void stop() {
        timer.cancel();
        executor.shutdown();
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    private void launchThread() {
        this.thread = new Thread() {
            @Override
            public void run() {
                executeScheduledTasks();
            }
        };
        thread.start();
    }

    public List<IGenerator> getGenerators() {
        return Arrays.asList();
    }

    private void scheduleTasks() {
        boolean locked = false;
        locked = obtainLock(locked);
        if (locked) {
            try {
                Date now = new Date();
                // retrieve the task generators
                EIConnection conn = Database.instance().getConnection();
                Session session = Database.instance().createSession(conn);
                try {
                    conn.setAutoCommit(false);
                    List<TaskGenerator> taskGenerators = retrieveTaskGenerators(session);
                    for (TaskGenerator taskGenerator : taskGenerators) {
                        List<ScheduledTask> tasks = taskGenerator.generateTasks(now, conn);
                        for (ScheduledTask task : tasks) {
                            LogClass.info("Scheduling " + task.getClass().getName() + " for execution on " + task.getExecutionDate());
                            session.save(task);
                        }
                        if (!tasks.isEmpty()) {
                            taskGenerator.setLastTaskDate(now);                            
                        }
                        session.update(taskGenerator);
                    }
                    session.flush();
                    conn.commit();
                } catch (Exception e) {
                    LogClass.error(e);
                    conn.rollback();
                } finally {
                    session.close();
                    conn.setAutoCommit(true);
                    Database.closeConnection(conn);
                }
            } finally {
                releaseLock();
            }
        }
    }

    private List<TaskGenerator> retrieveTaskGenerators(Session session) {
        return session.createQuery("from TaskGenerator").list();
    }

    private void releaseLock() {
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement lockStmt = conn.prepareStatement("DELETE FROM DISTRIBUTED_LOCK WHERE LOCK_NAME = ?");
            lockStmt.setString(1, SCHEDULE_LOCK);
            lockStmt.executeUpdate();
            lockStmt.close();
        } catch (SQLException e) {
            LogClass.error(e);
        } finally {
            Database.closeConnection(conn);
        }
    }

    private boolean obtainLock(boolean locked) {
        Connection conn = Database.instance().getConnection();
        try {
            PreparedStatement lockStmt = conn.prepareStatement("INSERT INTO DISTRIBUTED_LOCK (LOCK_NAME) VALUES (?)");
            lockStmt.setString(1, SCHEDULE_LOCK);
            lockStmt.execute();
            locked = true;
            lockStmt.close();
        } catch (SQLException e) {
            LogClass.debug("Failed to obtain distributed lock, assuming another app server has it.");
        } finally {
            Database.closeConnection(conn);
        }
        return locked;
    }

    public void executeScheduledTasks() {
        // retrieve the set of tasks which are in the SCHEDULED state, limit N
        // add them to the thread pool
        while (running) {
            List<ScheduledTask> tasks = claimScheduledTasks();
            if (tasks.isEmpty()) {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    // we don't care
                }
            } else {
                for (ScheduledTask task : tasks) {
                    executor.execute(task);
                }
            }
        }
    }

    private List<ScheduledTask> claimScheduledTasks() {
        List<ScheduledTask> results;
        Session session = Database.instance().createSession();
        try {
            session.getTransaction().begin();
            Query query = session.createQuery("from ScheduledTask where status = ?").
                setInteger(0, ScheduledTask.SCHEDULED);
            query.setMaxResults(TASK_LIMIT);
            results = query.list();
            for (ScheduledTask task : results) {
                task.setStatus(ScheduledTask.RUNNING);
                session.update(task);
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
        return results;
    }

    public void assignDefaultGenerators() {
        Set<String> taskClasses = new HashSet<String>(Arrays.asList(taskClassArray));
        Session session = Database.instance().createSession();
        try {
            session.getTransaction().begin();
            List<TaskGenerator> generators = session.createQuery("from TaskGenerator").list();
            for (TaskGenerator generator : generators) {
                taskClasses.remove(generator.getClass().getName());
            }
            for (String taskClass : taskClasses) {
                TaskGenerator taskGenerator = (TaskGenerator) Class.forName(taskClass).newInstance();
                taskGenerator.setRequiresBackfill(false);
                taskGenerator.setStartTaskDate(new Date());                
                session.save(taskGenerator);
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }

    }

    public void saveTask(ScheduledTask t) {
        EIConnection conn = Database.instance().getConnection();

        try {
            conn.setAutoCommit(false);
            saveTask(t, conn);
            conn.commit();
        }
        catch(Exception e) {
            conn.rollback();
            LogClass.error(e);
            throw new RuntimeException(e);
        }
        finally {
            Database.closeConnection(conn);
        }
    }

    public void saveTask(ScheduledTask t, Connection conn) {
        t.setStartedDate(new Date());
        Session session = Database.instance().createSession(conn);
        try {
            session.save(t);
            session.flush();
            if(t.getStatus() == ScheduledTask.INMEMORY)
                executor.execute(t);
        }
        catch(Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

}
