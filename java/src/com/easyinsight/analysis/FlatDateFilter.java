package com.easyinsight.analysis;

import com.easyinsight.database.Database;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * User: jamesboe
 * Date: 7/15/11
 * Time: 11:03 PM
 */
@Entity
@Table(name = "flat_date_filter")
@PrimaryKeyJoinColumn(name = "filter_id")
public class FlatDateFilter extends FilterDefinition {
    @Column(name = "date_level")
    private int dateLevel;

    @Column(name = "selected_value")
    private int value;

    @Column(name="start_year")
    private int startYear;

    @Column(name="all_option")
    private boolean allOption;

    @Transient
    private AnalysisItemResultMetadata cachedValues;

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public boolean isAllOption() {
        return allOption;
    }

    public void setAllOption(boolean allOption) {
        this.allOption = allOption;
    }

    public int getDateLevel() {
        return dateLevel;
    }

    public void setDateLevel(int dateLevel) {
        this.dateLevel = dateLevel;
    }

    public AnalysisItemResultMetadata getCachedValues() {
        return cachedValues;
    }

    public void setCachedValues(AnalysisItemResultMetadata cachedValues) {
        this.cachedValues = cachedValues;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public MaterializedFilterDefinition materialize(InsightRequestMetadata insightRequestMetadata) {
        return new MaterializedFlatDateFilter(getField(), dateLevel, value);
    }

    @Override
    public String toQuerySQL(String tableName, Database database) {
        if (database.getDialect() == Database.MYSQL) {
            if (dateLevel == AnalysisDateDimension.MONTH_LEVEL)
                return "month(" + getField().toKeySQL() + ") = (? + 1)";
            else
                return "year(" + getField().toKeySQL() + ") = ?";
        } else {
            return "EXTRACT(year FROM " + getField().toKeySQL() + ") = ?";
        }
    }

    @Override
    public boolean validForQuery() {
        return true;
    }

    @Override
    public int populatePreparedStatement(PreparedStatement preparedStatement, int start, int type, InsightRequestMetadata insightRequestMetadata) throws SQLException {
        preparedStatement.setInt(start++, value);
        return start;
    }

    @Override
    public String toHTML(FilterHTMLMetadata filterHTMLMetadata) {
        StringBuilder sb = new StringBuilder();
        String filterName = "filter" + getFilterID();
        String onChange = "updateFilter('filter" + getFilterID() + "','" + filterHTMLMetadata.getFilterKey() + "'," + filterHTMLMetadata.createOnChange() + ")";
        sb.append(label(true));
        sb.append("<select style=\"margin-left:5px;margin-top:5px;margin-right:5px\" id=\"").append(filterName).append("\" onchange=\"").append(onChange).append("\">");
        if (dateLevel == AnalysisDateDimension.MONTH_LEVEL) {
            List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
            for (int i = 0; i < 12; i++) {
                sb.append("<option value=\"");
                sb.append(i);
                sb.append("\"");
                if (i == this.getValue())
                    sb.append(" selected=\"selected\"");
                sb.append(">");
                sb.append(months.get(i));
                sb.append("</option>");
            }
        } else {
            AnalysisDateDimensionResultMetadata metadata = (AnalysisDateDimensionResultMetadata) new DataService().getAnalysisItemMetadata(filterHTMLMetadata.getDataSourceID(),
                    getField(), 0, 0, 0, filterHTMLMetadata.getReport());
            Date earliestDate = metadata.getEarliestDate();
            Date latestDate = metadata.getLatestDate();
            Calendar cal = Calendar.getInstance();

            cal.setTime(earliestDate);
            int startYear = cal.get(Calendar.YEAR);
            cal.setTime(latestDate);
            int endYear = cal.get(Calendar.YEAR);
            List<String> stringList = new ArrayList<String>();
            for (int i = startYear; i <= endYear; i++) {
                stringList.add(String.valueOf(i));
            }

            for (String value : stringList) {
                if (value.equals(String.valueOf(value))) {
                    sb.append("<option selected=\"selected\">").append(value).append("</option>");
                } else {
                    sb.append("<option>").append(value).append("</option>");
                }
            }
        }
        sb.append("</select>");
        return sb.toString();
    }

    @Override
    public JSONObject toJSON(FilterHTMLMetadata filterHTMLMetadata) throws JSONException {
        JSONObject jo = super.toJSON(filterHTMLMetadata);
        jo.put("selected", String.valueOf(value));
        if (dateLevel == AnalysisDateDimension.MONTH_LEVEL) {
            jo.put("type", "flat_date_month");
        } else {
            jo.put("type", "flat_date_year");

            AnalysisDateDimensionResultMetadata metadata = (AnalysisDateDimensionResultMetadata) new DataService().getAnalysisItemMetadata(filterHTMLMetadata.getDataSourceID(),
                    getField(), 0, 0, 0, filterHTMLMetadata.getReport());
            Date earliestDate = metadata.getEarliestDate();
            Date latestDate = metadata.getLatestDate();
            Calendar cal = Calendar.getInstance();

            cal.setTime(earliestDate);
            int startYear = cal.get(Calendar.YEAR);
            cal.setTime(latestDate);
            int endYear = cal.get(Calendar.YEAR);
            List<String> stringList = new ArrayList<String>();
            for (int i = startYear; i <= endYear; i++) {
                stringList.add(String.valueOf(i));
            }
            jo.put("years", new JSONArray(stringList));
        }
        return jo;
    }

    @Override
    public void override(FilterDefinition overrideFilter) {
        FlatDateFilter f = (FlatDateFilter) overrideFilter;
        this.setValue(f.getValue());
    }

    @Override
    public String asString(InsightRequestMetadata insightRequestMetadata) {
        return String.valueOf(value);
    }
}
