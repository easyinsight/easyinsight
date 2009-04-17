package com.easyinsight.scheduler;

import com.easyinsight.logging.LogClass;
import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;

import javax.persistence.*;
import java.util.Date;
import java.sql.Connection;

import org.hibernate.Session;

/**
 * User: James Boe
 * Date: Apr 15, 2009
 * Time: 5:00:01 PM
 */
@Entity
@Table(name="scheduled_task")
@Inheritance(strategy= InheritanceType.JOINED)
public abstract class ScheduledTask implements Runnable {
    public static final int SCHEDULED = 1;
    public static final int RUNNING = 2;
    public static final int COMPLETED = 3;
    public static final int FAILED = 4;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="scheduled_task_id")
    private long scheduledTaskID;
    @Column(name="status")
    private int status;
    @Column(name="scheduled_time")
    private Date executionDate;
    @Column(name="stopped_time")
    private Date completionDate;
    @Column(name="started_time")
    private Date startedDate;
    @Column(name="task_generator_id")
    private long taskGeneratorID;

    public void run() {
        boolean failed = false;
        EIConnection conn = Database.instance().getConnection();
        try {
            conn.setAutoCommit(false);
            execute(executionDate, conn);
            setStatus(COMPLETED);
        } catch (Exception e) {
            LogClass.error(e);
            failed = true;
            conn.rollback();
        } finally {
            conn.setAutoCommit(true);
            Database.instance().closeConnection(conn);
        }
        if (failed) {
            Session session = Database.instance().createSession();
            try {
                session.getTransaction().begin();
                setStatus(FAILED);
                session.getTransaction().commit();
            } finally {
                session.close();
            }
        }
    }

    public long getScheduledTaskID() {
        return scheduledTaskID;
    }

    public void setScheduledTaskID(long scheduledTaskID) {
        this.scheduledTaskID = scheduledTaskID;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(Date startedDate) {
        this.startedDate = startedDate;
    }

    public long getTaskGeneratorID() {
        return taskGeneratorID;
    }

    public void setTaskGeneratorID(long taskGeneratorID) {
        this.taskGeneratorID = taskGeneratorID;
    }

    protected abstract void execute(Date now, Connection conn) throws Exception;
}
