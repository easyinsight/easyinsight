package com.easyinsight.export;

import com.easyinsight.database.EIConnection;
import net.minidev.json.JSONObject;
import nu.xom.Attribute;
import nu.xom.Element;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

/**
 * User: jamesboe
 * Date: Jun 2, 2010
 * Time: 9:36:15 PM
 */
public class MonthlyScheduleType extends ScheduleType {

    private int dayOfMonth;

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    @Override
    public int retrieveType() {
        return ScheduleType.MONTHLY;
    }

    @Override
    public Element toXML() {
        Element element = super.toXML();
        element.addAttribute(new Attribute("dayOfMonth", String.valueOf(dayOfMonth)));
        return element;
    }

    @Override
    protected void subclassFromXML(Element element) {
        super.subclassFromXML(element);
        dayOfMonth = Integer.parseInt(element.getAttribute("dayOfMonth").getValue());
    }

    @Nullable
    public Date runTime(Date lastTime, Date now) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(cal.getTimeInMillis() - (getTimeOffset() * 60 * 1000));
        cal.set(Calendar.HOUR_OF_DAY, getHour());
        cal.set(Calendar.MINUTE, getMinute());
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTimeInMillis(cal.getTimeInMillis() + (getTimeOffset() * 60 * 1000));
        if (dayOfMonth == this.dayOfMonth) {
            if (cal.getTime().getTime() > lastTime.getTime() && cal.getTime().getTime() < now.getTime()) {
                return cal.getTime();
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public void customLoad(EIConnection conn) throws SQLException {
        PreparedStatement queryStmt = conn.prepareStatement("SELECT DAY_OF_MONTH FROM monthly_schedule WHERE SCHEDULE_ID = ?");
        queryStmt.setLong(1, getScheduleID());
        ResultSet rs = queryStmt.executeQuery();
        if (rs.next()) {
            this.dayOfMonth = rs.getInt(1);
        }
        queryStmt.close();
    }

    @Override
    protected void childSave(EIConnection conn) throws SQLException {
        PreparedStatement nukeStmt = conn.prepareStatement("DELETE FROM monthly_schedule WHERE SCHEDULE_ID = ?");
        nukeStmt.setLong(1, getScheduleID());
        nukeStmt.executeUpdate();
        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO monthly_schedule (DAY_OF_MONTH, SCHEDULE_ID) VALUES (?, ?)");
        insertStmt.setInt(1, dayOfMonth);
        insertStmt.setLong(2, getScheduleID());
        insertStmt.execute();
    }

    @Override
    public String when() {
        return "Monthly on " +dayOfMonth + " day of month at " + getHour() + ":" + String.format("%02d", getMinute()) + " GMT";
    }

    @Override
    public org.json.JSONObject toJSON(ExportMetadata md) throws JSONException {
        org.json.JSONObject jo = super.toJSON(md);
        jo.put("day_of_month", getDayOfMonth());
        return jo;
    }

    public MonthlyScheduleType() {
    }

    public MonthlyScheduleType(JSONObject jsonObject) {
        super(jsonObject);
        setDayOfMonth(Integer.parseInt(String.valueOf(jsonObject.get("day_of_month"))));
    }
}