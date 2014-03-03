package com.easyinsight.analysis;

import com.easyinsight.core.XMLImportMetadata;
import com.easyinsight.core.XMLMetadata;
import com.easyinsight.database.Database;
import com.easyinsight.pipeline.DateRangePluginComponent;
import com.easyinsight.pipeline.IComponent;
import nu.xom.Attribute;
import nu.xom.Element;
import org.hibernate.Session;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * User: James Boe
 * Date: Jul 8, 2008
 * Time: 2:24:20 PM
 */
@Entity
@Table(name="date_range_filter")
@PrimaryKeyJoinColumn(name="filter_id")
public class FilterDateRangeDefinition extends FilterDefinition {
    @Column(name="low_value")
    private Date startDate;

    @Column(name="bounding_start_date")
    private Date boundingStartDate;

    @OneToOne(cascade = CascadeType.MERGE, fetch=FetchType.LAZY)
    @JoinColumn(name="start_dimension")
    private AnalysisDateDimension startDateDimension;

    @Column(name="bounding_end_date")
    private Date boundingEndDate;

    @OneToOne(cascade = CascadeType.MERGE, fetch=FetchType.LAZY)
    @JoinColumn(name="end_dimension")
    private AnalysisDateDimension endDateDimension;

    @Column(name="high_value")
    private Date endDate;
    @Column(name="sliding")
    private boolean sliding;

    @Column(name="start_date_enabled")
    private boolean startDateEnabled = true;

    @Column(name="end_date_enabled")
    private boolean endDateEnabled = true;

    @Column(name="slider_range")
    private boolean sliderRange = true;

    @Override
    public int type() {
        return FilterDefinition.DATE;
    }

    public void customFromXML(Element element, XMLImportMetadata xmlImportMetadata) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            startDate = df.parse(element.getAttribute("startDate").getValue());
            endDate = df.parse(element.getAttribute("endDate").getValue());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isStartDateEnabled() {
        return startDateEnabled;
    }

    public void setStartDateEnabled(boolean startDateEnabled) {
        this.startDateEnabled = startDateEnabled;
    }

    public boolean isEndDateEnabled() {
        return endDateEnabled;
    }

    public void setEndDateEnabled(boolean endDateEnabled) {
        this.endDateEnabled = endDateEnabled;
    }

    public boolean isSliderRange() {
        return sliderRange;
    }

    public void setSliderRange(boolean sliderRange) {
        this.sliderRange = sliderRange;
    }

    @Override
    public Element toXML(XMLMetadata xmlMetadata) {
        Element element = super.toXML(xmlMetadata);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        element.addAttribute(new Attribute("startDate", df.format(startDate)));
        element.addAttribute(new Attribute("endDate", df.format(endDate)));
        return element;
    }

    @Override
    public void beforeSave(Session session) {
        super.beforeSave(session);
        if (startDateDimension != null) {
            startDateDimension.reportSave(session);
            if (startDateDimension.getKey().getKeyID() == 0) {
                session.save(getField().getKey());
            }
            if (startDateDimension.getAnalysisItemID() == 0) {
                session.save(startDateDimension);
            }
        }
        if (endDateDimension != null) {
            endDateDimension.reportSave(session);
            if (endDateDimension.getKey().getKeyID() == 0) {
                session.save(getField().getKey());
            }
            if (endDateDimension.getAnalysisItemID() == 0) {
                session.save(endDateDimension);
            }
        }
    }

    @Override
    public void afterLoad() {
        super.afterLoad();
        if (startDateDimension != null) {
            startDateDimension = (AnalysisDateDimension) Database.deproxy(startDateDimension);
            startDateDimension.afterLoad();
        }
        if (endDateDimension != null) {
            endDateDimension = (AnalysisDateDimension) Database.deproxy(endDateDimension);
            endDateDimension.afterLoad();            
        }
    }

    @Override
    public void updateIDs(ReplacementMap replacementMap) {
        super.updateIDs(replacementMap);
        if (startDateDimension != null) {
            startDateDimension = (AnalysisDateDimension) replacementMap.getField(startDateDimension);
        }
        if (endDateDimension != null) {
            endDateDimension = (AnalysisDateDimension) replacementMap.getField(endDateDimension);
        }
    }

    public Date getBoundingStartDate() {
        return boundingStartDate;
    }

    public void setBoundingStartDate(Date boundingStartDate) {
        this.boundingStartDate = boundingStartDate;
    }

    public AnalysisDateDimension getStartDateDimension() {
        return startDateDimension;
    }

    public void setStartDateDimension(AnalysisDateDimension startDateDimension) {
        this.startDateDimension = startDateDimension;
    }

    public Date getBoundingEndDate() {
        return boundingEndDate;
    }

    public void setBoundingEndDate(Date boundingEndDate) {
        this.boundingEndDate = boundingEndDate;
    }

    public AnalysisDateDimension getEndDateDimension() {
        return endDateDimension;
    }

    public void setEndDateDimension(AnalysisDateDimension endDateDimension) {
        this.endDateDimension = endDateDimension;
    }

    public boolean isSliding() {
        return sliding;
    }

    public void setSliding(boolean sliding) {
        this.sliding = sliding;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public MaterializedFilterDefinition materialize(InsightRequestMetadata insightRequestMetadata) {
        Date workingEndDate = new Date(endDate.getTime() - insightRequestMetadata.getUtcOffset() * 1000 * 60);
        Date workingStartDate = new Date(startDate.getTime() - insightRequestMetadata.getUtcOffset() * 1000 * 60);
        // but now it's in the app transformed into the user time!
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(workingStartDate);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        workingStartDate = startCal.getTime();
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(workingEndDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 0);
        workingEndDate = endCal.getTime();
        AnalysisDateDimension date = (AnalysisDateDimension) getField();
        if (date.isTimeshift()) {
            workingEndDate = new Date(workingEndDate.getTime() + insightRequestMetadata.getUtcOffset() * 1000 * 60);
            workingStartDate = new Date(workingStartDate.getTime() + insightRequestMetadata.getUtcOffset() * 1000 * 60);
        }
        return new MaterializedFilterDateRangeDefinition(getField(), workingStartDate, workingEndDate, sliding);
    }

    @Override
    public List<AnalysisItem> getAnalysisItems(List<AnalysisItem> allItems, Collection<AnalysisItem> insightItems, boolean getEverything, boolean includeFilters, Collection<AnalysisItem> analysisItemSet, AnalysisItemRetrievalStructure structure) {
        List<AnalysisItem> items = super.getAnalysisItems(allItems, insightItems, getEverything, includeFilters, analysisItemSet, structure);
        if (getStartDateDimension() != null) {
            items.add(getStartDateDimension());
        }
        if (getEndDateDimension() != null) {
            items.add(getEndDateDimension());
        }
        return items;
    }

    public String toQuerySQL(String tableName) {
        StringBuilder queryBuilder = new StringBuilder();
        String columnName = "k" + getField().getKey().toBaseKey().getKeyID();


        queryBuilder.append(columnName);

        queryBuilder.append(" >= ? AND ");
        queryBuilder.append(columnName);
        queryBuilder.append(" <= ?");
        return queryBuilder.toString();
    }

    public int populatePreparedStatement(PreparedStatement preparedStatement, int start, int type, InsightRequestMetadata insightRequestMetadata) throws SQLException {
        Date workingEndDate;
        Date workingStartDate;
        // scale the query time back to UTC because it's in the database as UTC

        AnalysisDateDimension date = (AnalysisDateDimension) getField();
        /*System.out.println("shift = " + date.isTimeshift());
        System.out.println("initial dates = " + endDate + " and " + startDate);
        System.out.println("utc offset = " + insightRequestMetadata.getUtcOffset());*/

        if (endDate == null) {
            endDate = new Date();
        }
        if (startDate == null) {
            startDate = new Date();
        }
        workingEndDate = new Date(endDate.getTime() - insightRequestMetadata.getUtcOffset() * 1000 * 60);
        workingStartDate = new Date(startDate.getTime() - insightRequestMetadata.getUtcOffset() * 1000 * 60);
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(workingStartDate);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        workingStartDate = startCal.getTime();
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(workingEndDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 0);
        workingEndDate = endCal.getTime();
        /*System.out.println("end date = " + new Date(workingEndDate.getTime()));
        System.out.println("start date = " + new Date(workingStartDate.getTime()));*/
        /*if (date.isTimeshift()) {

        } else {
            workingEndDate = endDate;
            workingStartDate = startDate;
        }*/
        preparedStatement.setTimestamp(start++, new java.sql.Timestamp(workingStartDate.getTime()));
        preparedStatement.setTimestamp(start++, new java.sql.Timestamp(workingEndDate.getTime()));
        return start;
    }

    @Override
    public List<IComponent> createComponents(String pipelineName, IFilterProcessor filterProcessor, AnalysisItem sourceItem, boolean columnLevel) {
        if (getStartDateDimension() != null || getEndDateDimension() != null) {
            return Arrays.asList((IComponent) new DateRangePluginComponent(this));
        } else {
            return super.createComponents(pipelineName, filterProcessor, sourceItem, columnLevel);
        }
    }

    @Override
    public String toHTML(FilterHTMLMetadata filterHTMLMetadata) {
        StringBuilder sb = new StringBuilder();
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        String startName = "filter" + getFilterID() + "start";
        String endName = "filter" + getFilterID() + "end";
        String onChange = "updateRangeFilter('filter" + getFilterID() + "'," + filterHTMLMetadata.createOnChange() + ")";
        sb.append("<script type=\"text/javascript\">\n" +
                "\t$(function() {\n" +
                "\t\t$( \"#"+startName+"\" ).datePicker({clickInput:true, startDate:'1900/01/01'}).bind('dateSelected', function(e, selectedDate, $td) {" + onChange + "});\n" +
                "\t\t$( \"#"+endName+"\" ).datePicker({clickInput:true, startDate:'1900/01/01'}).bind('dateSelected', function(e, selectedDate, $td) {" + onChange + "});\n" +
                "\t});\n" +
                "\t</script>");
        sb.append("<div>");
        if (!isToggleEnabled()) {
            sb.append(checkboxHTML(filterHTMLMetadata.getFilterKey(), filterHTMLMetadata.createOnChange()));
        }
        sb.append(label(true));
        sb.append("<input readonly=\"readonly\" type=\"text\" id=\""+startName+"\" value=\"").append(df.format(getStartDate())).append("\"/>").
                append("<input readonly=\"readonly\" type=\"text\" id=\""+endName+"\" value=\"").append(df.format(getEndDate())).append("\"/>");
        sb.append("</div>");
        return sb.toString();
    }

    @Override
    public JSONObject toJSON(FilterHTMLMetadata filterHTMLMetadata) throws JSONException {
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        JSONObject jo = super.toJSON(filterHTMLMetadata);

        jo.put("type", "date_range");
        jo.put("start", df.format(getStartDate()));
        jo.put("end", df.format(getEndDate()));
        return jo;
    }

    @Override
    public void override(FilterDefinition overrideFilter) {
        FilterDateRangeDefinition f = (FilterDateRangeDefinition) overrideFilter;
        this.setStartDate(f.getStartDate());
        f.setEndDate(f.getEndDate());
    }

    @Override
    public String asString(InsightRequestMetadata insightRequestMetadata) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String startString = df.format(getStartDate());
        String endString = df.format(getStartDate());
        return "between " + startString + " and " + endString;
    }
}
