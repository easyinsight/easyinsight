package com.easyinsight.connections.database.data;

import com.easyinsight.connections.database.DataConnection;

import javax.persistence.*;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import java.util.*;
import java.util.Date;
import java.sql.*;

import com.easyinsight.helper.*;
import org.hibernate.Session;

/**
 * Created by IntelliJ IDEA.
 * User: abaldwin
 * Date: Apr 27, 2010
 * Time: 9:17:54 AM
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class Query {

    public static Timer getScheduler() {
        return scheduler;
    }
    
    private static Timer scheduler = startTimer();

    private static Timer startTimer() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Timer t = new Timer(true);
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                Session s = null;
                try {
                    s = DataConnection.getSession();
                    List<Query> queries = s.createQuery("from Query where schedule = true").list();
                    for(Query query : queries) {
                         try {
                            query.doUpload();
                        } catch (DatatypeConfigurationException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if(s != null)
                        s.close();
                }
            }
        };
        t.scheduleAtFixedRate(tt, c.getTime(), 24 * 60 * 60 * 1000);
        return t;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;
    @Column(length = 1024, precision = 1024)
    private String query;
    private boolean schedule;
    private boolean append;
    private String dataSource;
    private String name;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    private ConnectionInfo connectionInfo;

    public Query() {
    }

    public Query(Map<String, String[]> parameterMap) {
        update(parameterMap);
    }

    public void update(Map<String, String[]> parameterMap) {
        setQuery(parameterMap.get("queryValue")[0]);
        setDataSource(parameterMap.get("queryDataSource")[0]);
        setName(parameterMap.get("queryName")[0]);
        if(parameterMap.get("schedule") != null && parameterMap.get("schedule")[0].equals("on")) {
            setSchedule(true);
        } else {
            setSchedule(false);
        }
        if(parameterMap.get("uploadType") != null && parameterMap.get("uploadType")[0].equals("append")) {
            setAppend(true);
        } else {
            setAppend(false);
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public ResultSet executeQuery(Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(getQuery());
        return statement.executeQuery();
    }

    public ResultSet executeQuery(Connection conn, int limit) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(getQuery());
        statement.setMaxRows(limit);
        return statement.executeQuery();
    }


    public static List<Query> all() throws SQLException {
        Session session = DataConnection.getSession();
        try {
            return session.createQuery("from Query").list();
        }
        finally {
            session.close();
        }
    }

    public boolean isSchedule() {
        return schedule;
    }

    public void setSchedule(boolean schedule) {
        this.schedule = schedule;
    }

    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public void doUpload() throws SQLException, DatatypeConfigurationException {
        Connection conn = null;
        ResultSet rs = null;
        TransactionTarget dataSourceTarget = null;
        try {
            EIUser user = EIUser.instance();
            if(user == null)
                throw new RuntimeException("You need to enter your credentials first!");
            conn = this.getConnectionInfo().createConnection();
            rs = this.executeQuery(conn);
            DataSourceFactory dataSourceFactory = APIUtil.defineDataSource(this.getDataSource(), user.getPublicKey(), user.getSecretKey());
            for(int column = 1;column <= rs.getMetaData().getColumnCount();column++) {
                switch (rs.getMetaData().getColumnType(column)) {
                    case Types.BIGINT:
                        case Types.TINYINT:
                        case Types.SMALLINT:
                        case Types.INTEGER:
                        case Types.NUMERIC:
                        case Types.FLOAT:
                        case Types.DOUBLE:
                        case Types.DECIMAL:
                        case Types.REAL:
                            dataSourceFactory.addMeasure(rs.getMetaData().getColumnName(column));
                            break;

                        case Types.BOOLEAN:
                        case Types.BIT:
                        case Types.CHAR:
                        case Types.NCHAR:
                        case Types.NVARCHAR:
                        case Types.VARCHAR:
                            dataSourceFactory.addGrouping(rs.getMetaData().getColumnName(column));
                            break;

                        case Types.DATE:
                        case Types.TIME:
                        case Types.TIMESTAMP:
                            dataSourceFactory.addDate(rs.getMetaData().getColumnName(column));
                            break;
                        default:
                            throw new RuntimeException("This data type (" + rs.getMetaData().getColumnTypeName(column) + ") is not supported in Easy Insight.");
                }
            }

            DataSourceOperationFactory operationFactory = dataSourceFactory.defineDataSource();
            dataSourceTarget = this.isAppend() ? operationFactory.addRowsTransaction() : operationFactory.replaceRowsTransaction();
            dataSourceTarget.beginTransaction();
            int i = 0;
            while(rs.next()) {
                System.out.println("row: " + i++);
                DataRow row = dataSourceTarget.newRow();
                for(int column = 1;column <= rs.getMetaData().getColumnCount();column++) {
                    String key = rs.getMetaData().getColumnName(column);
                    switch (rs.getMetaData().getColumnType(column)) {
                        case Types.BIGINT:
                        case Types.TINYINT:
                        case Types.SMALLINT:
                        case Types.INTEGER:
                        case Types.NUMERIC:
                        case Types.FLOAT:
                        case Types.DOUBLE:
                        case Types.DECIMAL:
                        case Types.REAL:
                            row.addValue(key, rs.getDouble(column));
                            break;

                        case Types.BOOLEAN:
                        case Types.BIT:
                            row.addValue(key,String.valueOf(rs.getBoolean(column)));
                            break;

                        case Types.CHAR:
                        case Types.NCHAR:
                        case Types.NVARCHAR:
                        case Types.VARCHAR:
                            row.addValue(key, rs.getString(column));
                            break;

                        case Types.DATE:
                        case Types.TIME:
                        case Types.TIMESTAMP:
                            Date date = rs.getTimestamp(column);
                            row.addValue(key, date);
                            break;
                        default:
                            throw new RuntimeException("This data type (" + rs.getMetaData().getColumnTypeName(column) + ") is not supported in Easy Insight.");
                    }
                }
            }
            dataSourceTarget.commit();

        } catch(SQLException e) {
            if(dataSourceTarget != null)
                dataSourceTarget.rollback();
            throw e;
        } finally {
            if(rs != null)
                rs.close();
            if(conn != null)
                conn.close();
        }

    }
}
