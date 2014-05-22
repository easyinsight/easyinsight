package com.easyinsight.analysis;

import com.easyinsight.core.*;
import com.easyinsight.dashboard.Dashboard;
import com.easyinsight.database.Database;
import com.easyinsight.logging.LogClass;
import com.easyinsight.servlet.SystemSettings;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Nodes;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.*;
import java.sql.Types;
import java.util.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * User: James Boe
 * Date: Jul 8, 2008
 * Time: 2:57:56 PM
 */
@Entity
@Table(name = "value_based_filter")
@PrimaryKeyJoinColumn(name = "filter_id")
public class FilterValueDefinition extends FilterDefinition {
    @Column(name = "inclusive")
    private boolean inclusive = true;
    @Transient
    private List<Object> filteredValues;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "filter_to_value",
            joinColumns = @JoinColumn(name = "filter_id"),
            inverseJoinColumns = @JoinColumn(name = "value_id"))
    private Set<PersistableValue> persistedValues;
    @Column(name = "single_value")
    private boolean singleValue;

    @Column(name = "auto_complete")
    private boolean autoComplete;

    @Column(name = "exclude_empty")
    private boolean excludeEmpty;

    @Column(name = "all_option")
    private boolean allOption = true;

    @Column(name = "new_type")
    private boolean newType = false;

    @Transient
    private AnalysisItemResultMetadata cachedValues;

    public FilterValueDefinition() {
    }

    public FilterValueDefinition(AnalysisItem field, boolean inclusive, List<Object> filteredValues) {
        super(field);
        this.inclusive = inclusive;
        this.filteredValues = filteredValues;
    }

    public boolean isNewType() {
        return newType;
    }

    public void setNewType(boolean newType) {
        this.newType = newType;
    }

    public AnalysisItemResultMetadata getCachedValues() {
        return cachedValues;
    }

    public void setCachedValues(AnalysisItemResultMetadata cachedValues) {
        this.cachedValues = cachedValues;
    }

    public boolean isExcludeEmpty() {
        return excludeEmpty;
    }

    public void setExcludeEmpty(boolean excludeEmpty) {
        this.excludeEmpty = excludeEmpty;
    }

    public boolean isAllOption() {
        return allOption;
    }

    public void setAllOption(boolean allOption) {
        this.allOption = allOption;
    }

    public boolean isAutoComplete() {
        return autoComplete;
    }

    public void setAutoComplete(boolean autoComplete) {
        this.autoComplete = autoComplete;
    }

    public Set<PersistableValue> getPersistedValues() {
        return persistedValues;
    }

    public void setPersistedValues(Set<PersistableValue> persistedValues) {
        this.persistedValues = persistedValues;
    }

    public boolean isSingleValue() {
        return singleValue;
    }

    public void setSingleValue(boolean singleValue) {
        this.singleValue = singleValue;
    }

    public boolean isInclusive() {
        return inclusive;
    }

    public void setInclusive(boolean inclusive) {
        this.inclusive = inclusive;
    }

    public List<Object> getFilteredValues() {
        return filteredValues;
    }

    public void setFilteredValues(List<Object> filteredValues) {
        this.filteredValues = filteredValues;
    }

    public FilterDefinition clone() throws CloneNotSupportedException {
        FilterValueDefinition filter = (FilterValueDefinition) super.clone();
        Set<PersistableValue> values = new HashSet<PersistableValue>();
        if (persistedValues == null) {
            Set<Value> valueSet = new HashSet<Value>();
            for (Object valueObject : filteredValues) {
                Value value;
                if (valueObject instanceof Value) {
                    value = (Value) valueObject;
                } else if (valueObject instanceof String) {
                    value = new StringValue((String) valueObject);
                } else if (valueObject instanceof Number) {
                    value = new NumericValue((Number) valueObject);
                } else if (valueObject instanceof Date) {
                    value = new DateValue((Date) valueObject);
                } else {
                    throw new RuntimeException("Unexpected value class " + valueObject.getClass().getName());
                }
                valueSet.add(value);
            }
            Set<PersistableValue> filterDefinitionValues = PersistableValueFactory.fromValue(valueSet);
            filter.setPersistedValues(filterDefinitionValues);
        } else {
            for (PersistableValue value : persistedValues) {
                values.add(value.clone());
            }
            filter.setPersistedValues(values);
        }
        List<Object> transferValues = new ArrayList<Object>();
        for (PersistableValue filterDefinitionValue : filter.getPersistedValues()) {
            transferValues.add(filterDefinitionValue.toValue());
        }
        filter.setFilteredValues(transferValues);
        return filter;
    }

    public void beforeSave(Session session) {
        super.beforeSave(session);
        Set<Value> valueSet = new HashSet<Value>();
        if (filteredValues != null) {
            for (Object valueObject : filteredValues) {
                Value value;
                if (valueObject instanceof Value) {
                    value = (Value) valueObject;
                } else if (valueObject instanceof String) {
                    value = new StringValue((String) valueObject);
                } else if (valueObject instanceof Number) {
                    value = new NumericValue((Number) valueObject);
                } else if (valueObject instanceof Date) {
                    value = new DateValue((Date) valueObject);
                } else {
                    throw new RuntimeException("Unexpected value class " + valueObject.getClass().getName());
                }
                valueSet.add(value);
            }
        }
        Set<PersistableValue> filterDefinitionValues = PersistableValueFactory.fromValue(valueSet);
        setPersistedValues(filterDefinitionValues);
        for (PersistableValue persistableValue : getPersistedValues()) {
            persistableValue.truncate();
            session.saveOrUpdate(persistableValue);
        }
    }

    public void afterLoad() {
        super.afterLoad();
        List<Object> values = new ArrayList<Object>();
        if (getPersistedValues() != null) {
            for (PersistableValue filterDefinitionValue : getPersistedValues()) {
                values.add(filterDefinitionValue.toValue());
            }
        }
        setFilteredValues(values);
    }

    public String toQuerySQL(String tableName, Database database) {
        StringBuilder queryBuilder = new StringBuilder();

        String columnName = getField().toKeySQL();
        queryBuilder.append(columnName);
        if (inclusive) {
            queryBuilder.append(" in (");
        } else {
            queryBuilder.append(" not in (");
        }
        for (int i = 0; i < getFilteredValues().size(); i++) {
            queryBuilder.append("?,");
        }
        queryBuilder = queryBuilder.deleteCharAt(queryBuilder.length() - 1);
        queryBuilder.append(")");
        return queryBuilder.toString();
    }

    public String toQuerySQL(String tableName, String sourceTableName, String sourceKeyName) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append(sourceTableName);
        queryBuilder.append(".");
        queryBuilder.append(sourceKeyName);

        queryBuilder.append(" = ");
        String columnName = getField().toKeySQL();
        queryBuilder.append(columnName);

        return queryBuilder.toString();
    }

    public int populatePreparedStatement(PreparedStatement preparedStatement, int start, int type, InsightRequestMetadata insightRequestMetadata) throws SQLException {
        if (insightRequestMetadata.getFilterPropertiesMap().containsKey(this)) {
            return start;
        }
        List<Value> valueSet = new ArrayList<Value>();
        for (Object valueObject : filteredValues) {
            Value value;
            if (valueObject instanceof Value) {
                value = (Value) valueObject;
            } else if (valueObject instanceof String) {
                value = new StringValue((String) valueObject);
            } else if (valueObject instanceof Number) {
                value = new NumericValue((Number) valueObject);
            } else if (valueObject instanceof Date) {
                value = new DateValue((Date) valueObject);
            } else if (valueObject == null) {
                value = new EmptyValue();
            } else {
                throw new RuntimeException("Unexpected value class " + valueObject.getClass().getName());
            }
            if (value instanceof StringValue) {
                StringValue stringValue = (StringValue) value;
                if ("(No Value)".equals(stringValue.getValue())) {
                    value = new EmptyValue();
                }
            }
            valueSet.add(value);
        }
        if (type == Value.NUMBER) {
            for (Value value : valueSet) {
                preparedStatement.setDouble(start++, value.toDouble());
            }
        } else if (type == Value.DATE) {
            for (Value value : valueSet) {
                if (value.type() == Value.DATE) {
                    DateValue dateValue = (DateValue) value;
                    preparedStatement.setTimestamp(start++, new java.sql.Timestamp(dateValue.getDate().getTime()));
                } else {
                    preparedStatement.setNull(start++, Types.TIMESTAMP);
                }
            }
        } else if (type == Value.STRING) {
            for (Value value : valueSet) {
                preparedStatement.setString(start++, value.toString());
            }
        } else if (type == Value.EMPTY) {
            for (Value value : valueSet) {
                preparedStatement.setString(start++, value.toString());
            }
        } else {
            for (Value value : valueSet) {
                preparedStatement.setString(start++, value.toString());
            }
        }
        return start;
    }

    @Override
    public boolean validForQuery() {
        if (getField().isMultipleTransform()) {
            return false;
        }
        if (super.validForQuery() && filteredValues.size() == 1) {
            Object value = filteredValues.get(0);
            if (value instanceof String) {
                String string = (String) value;
                return !"".equals(string) && !"All".equals(string);
            } else if (value instanceof StringValue) {
                StringValue stringValue = (StringValue) value;
                return !"".equals(stringValue.toString()) && !"All".equals(stringValue.toString());
            } else if (value instanceof EmptyValue) {
                return false;
            }
        }
        if (filteredValues.size() > SystemSettings.instance().getMaxFilterValues()) {
            return false;
        }
        return super.validForQuery() && filteredValues.size() > 0;
    }

    public MaterializedFilterDefinition materialize(InsightRequestMetadata insightRequestMetadata) {
        Set<Value> valueSet = new HashSet<Value>();
        for (Object valueObject : filteredValues) {
            Value value;
            if (valueObject instanceof String) {
                value = new StringValue((String) valueObject);
            } else if (valueObject instanceof Number) {
                value = new NumericValue((Number) valueObject);
            } else if (valueObject instanceof Date) {
                value = new DateValue((Date) valueObject);
            } else if (valueObject instanceof Value) {
                value = (Value) valueObject;
            } else {
                value = new EmptyValue();
            }
            if (value instanceof StringValue) {
                StringValue stringValue = (StringValue) value;
                if ("(No Value)".equals(stringValue.getValue())) {
                    value = new EmptyValue();
                }
            }
            valueSet.add(value);
        }
        return new MaterializedValueFilterDefinition(getField(), valueSet, inclusive);
    }

    @Override
    public int type() {
        return FilterDefinition.VALUE;
    }

    public void customFromXML(Element element, XMLImportMetadata xmlImportMetadata) {
        setInclusive(Boolean.parseBoolean(element.getAttribute("inclusive").getValue()));
        setSingleValue(Boolean.parseBoolean(element.getAttribute("singleValue").getValue()));
        setAutoComplete(Boolean.parseBoolean(element.getAttribute("autoComplete").getValue()));
        setAllOption(Boolean.parseBoolean(element.getAttribute("allOption").getValue()));
        setExcludeEmpty(Boolean.parseBoolean(element.getAttribute("excludeEmpty").getValue()));
        Nodes valueNodes = element.query("values/value/text()");
        filteredValues = new ArrayList<Object>();
        for (int i = 0; i < valueNodes.size(); i++) {
            filteredValues.add(valueNodes.get(i).getValue());
        }
    }

    @Override
    public Element toXML(XMLMetadata xmlMetadata) {
        Element element = super.toXML(xmlMetadata);
        element.addAttribute(new Attribute("inclusive", String.valueOf(inclusive)));
        element.addAttribute(new Attribute("singleValue", String.valueOf(singleValue)));
        element.addAttribute(new Attribute("autoComplete", String.valueOf(autoComplete)));
        element.addAttribute(new Attribute("allOption", String.valueOf(allOption)));
        element.addAttribute(new Attribute("excludeEmpty", String.valueOf(excludeEmpty)));
        Element values = new Element("values");
        element.appendChild(values);
        if (persistedValues != null) {
            for (PersistableValue valueObject : persistedValues) {
                Element valueElement = new Element("value");
                valueElement.appendChild(valueObject.toValue().toString());
                values.appendChild(valueElement);
            }
        } else if (filteredValues != null) {
            Set<Value> valueSet = new LinkedHashSet<Value>();
            for (Object valueObject : filteredValues) {
                Value value;
                if (valueObject instanceof String) {
                    value = new StringValue((String) valueObject);
                } else if (valueObject instanceof Number) {
                    value = new NumericValue((Number) valueObject);
                } else if (valueObject instanceof Date) {
                    value = new DateValue((Date) valueObject);
                } else if (valueObject instanceof Value) {
                    value = (Value) valueObject;
                } else {
                    value = new EmptyValue();
                }
                if (value instanceof StringValue) {
                    StringValue stringValue = (StringValue) value;
                    if ("(No Value)".equals(stringValue.getValue())) {
                        value = new EmptyValue();
                    }
                }
                valueSet.add(value);
            }
            for (Value value : valueSet) {
                Element valueElement = new Element("value");
                valueElement.appendChild(value.toString());
                values.appendChild(valueElement);
            }
        }
        return element;
    }

    @Override
    public String toHTML(FilterHTMLMetadata filterHTMLMetadata) {
        StringBuilder sb = new StringBuilder();
        AnalysisItemResultMetadata metadata = new DataService().getAnalysisItemMetadata(filterHTMLMetadata.getDataSourceID(), getField(), 0, 0, 0, filterHTMLMetadata.getReport());
        if (metadata.getReportFault() != null) {
            return "";
        }
        AnalysisDimensionResultMetadata dimensionMetadata = (AnalysisDimensionResultMetadata) metadata;
        String filterName = "filter" + getFilterID();
        if (singleValue) {

            String onChange;
            String key = filterHTMLMetadata.getFilterKey();
            String function = filterHTMLMetadata.createOnChange();
            onChange = "updateFilter('" + filterName + "','" + key + "', " + function + ")";

            if (!isToggleEnabled()) {
                sb.append(checkboxHTML(filterHTMLMetadata.getFilterKey(), filterHTMLMetadata.createOnChange()));
            }
            sb.append(label(true));
            sb.append("<select class=\"filterSelect\" id=\"" + filterName + "\" onchange=\"" + onChange + "\">");


            List<String> stringList = dimensionMetadata.getStrings();
            for (Value value : dimensionMetadata.getValues()) {
                stringList.add(value.toHTMLString());
            }
            Collections.sort(stringList);
            if (isAllOption()) {
                stringList.add(0, "All");
            }
            if (isExcludeEmpty()) {
                stringList.remove("");
            }
            String existingChoice = null;
            if (!getFilteredValues().isEmpty()) {
                Object obj = getFilteredValues().get(0);
                if (obj != null) {
                    existingChoice = obj.toString();
                }
            }
            for (String value : stringList) {
                if (value.equals(existingChoice)) {
                    sb.append("<option selected=\"selected\">").append(value).append("</option>");
                } else {
                    sb.append("<option>").append(value).append("</option>");
                }
            }
            sb.append("</select>");

            if (!filterHTMLMetadata.isFromStack()) {
                sb.append("<script type=\"text/javascript\">\n");
                sb.append("updateFilter('" + filterName + "','" + key + "', " + function + ");\n");
                sb.append("</script>");
            }
        } else {
            String divID = "filter" + getFilterID() + "div";
            sb.append("<div id=\"").append(divID).append("\" class=\"modal\">");
            sb.append("<div class=\"modal-dialog\"><div class=\"modal-content\"><div class=\"modal-body\">");
            sb.append("<div class=\"control-group\">");
            sb.append("<label class=\"control-label\" for=\"" + filterName + "\">Available Values</label>");
            sb.append("<div class=\"controls\">");
            int size = Math.min(15, dimensionMetadata.getValues().size());
            sb.append("<ul class=\"unstyled\" id=\"");
            sb.append(filterName);
            sb.append("\">");

            for (Value value : dimensionMetadata.getValues()) {
                sb.append("<li><input type='checkbox'");
                if (filteredValues.contains(value)) {
                    sb.append(" checked='checked'");
                }
                sb.append(" /> <span class='cb_filter_value'>");
                sb.append(value);
                sb.append("</span></li>");
            }
            sb.append("</ul>");
            sb.append("</div>");
            sb.append("</div>");
            sb.append("</div>");

            sb.append("<div class=\"modal-footer\">\n" +
                    "        <button class=\"btn\" data-dismiss=\"modal\" onclick=\"updateMultiFilter('" + filterName + "','" + filterHTMLMetadata.getFilterKey() + "'," + filterHTMLMetadata.createOnChange() + ")\">Save</button>\n" +
                    "        <button class=\"btn\" data-dismiss=\"modal\" type=\"button\">Cancel</button>\n" +
                    "    </div>");
            sb.append("</div>");
            sb.append("</div>");
            sb.append("</div>");
            sb.append("<div class=\"filterLabel\">");
            if (!isToggleEnabled()) {
                sb.append(checkboxHTML(filterHTMLMetadata.getFilterKey(), filterHTMLMetadata.createOnChange()));
            }
            sb.append("<a href=\"#" + divID + "\" data-toggle=\"modal\">").append(label(false)).append("</a></div>");
        }
        return sb.toString();
    }

    @Override
    public JSONObject toJSON(FilterHTMLMetadata filterHTMLMetadata) throws JSONException {
        JSONObject jo = super.toJSON(filterHTMLMetadata);
        Dashboard db = filterHTMLMetadata.getDashboard();
        WSAnalysisDefinition report = filterHTMLMetadata.getReport();
        long dashboardID = db == null ? 0 : db.getId();
        long reportID = report == null ? 0 : report.getAnalysisID();
        AnalysisItemResultMetadata metadata = null;
        try {
            metadata = new DataService().getAnalysisItemMetadata(filterHTMLMetadata.getDataSourceID(), getField(), 0, reportID, dashboardID, filterHTMLMetadata.getReport());
        } catch (Exception e) {
            LogClass.error(e);
            return null;
        }
        if (metadata.getReportFault() != null) {
            return null;
        }
        AnalysisDimensionResultMetadata dimensionMetadata = (AnalysisDimensionResultMetadata) metadata;
        if (singleValue) {
            jo.put("type", "single");
            List<String> stringList = dimensionMetadata.getStrings();
            if (isAllOption()) {
                stringList.add(0, "All");
            }
            if (isExcludeEmpty()) {
                stringList.remove("");
            }
            String existingChoice = null;
            if (!getFilteredValues().isEmpty()) {
                Object obj = getFilteredValues().get(0);
                if (obj != null) {
                    existingChoice = obj.toString();
                }
            }

            JSONArray arr = new JSONArray(stringList);
            jo.put("selected", existingChoice);
            jo.put("values", arr);
        } else {
            jo.put("type", "multiple");
            jo.put("count", dimensionMetadata.getStrings().size());

            List<String> stringList = dimensionMetadata.getStrings();
            if (isAllOption()) {
                if (!stringList.contains("All")) {
                    stringList.add(0, "All");
                }
            }
            if (isExcludeEmpty()) {
                stringList.remove("");
            }
            JSONObject existingChoices = new JSONObject();
            if(stringList.size() > 100) {
                jo.put("values", new JSONArray());
                jo.put("error", "Too many values, please refine your search.");
            } else if(getParentFilters() != null && !getParentFilters().isEmpty()) {
                jo.put("values", new JSONArray());
                jo.put("error", "Loading...");
            } else {
                JSONArray arr = new JSONArray(stringList);
                jo.put("values", arr);
            }

            for(Object obj : getFilteredValues()) {
                if (obj != null) {
                    existingChoices.put(obj.toString(), true);
                }
            }

            jo.put("selected", existingChoices);
            if(getParentFilters() != null && !getParentFilters().isEmpty()) {
                String[] parentFilters = getParentFilters().split(",");
                jo.put("parents", new JSONArray(parentFilters));
            }
            jo.put("name", getFilterName());
        }
        return jo;
    }

    @Override
    public boolean sameFilter(FilterDefinition targetDefinition) {
        return super.sameFilter(targetDefinition) && ((FilterValueDefinition) targetDefinition).isSingleValue() == isSingleValue();
    }

    public void override(FilterDefinition overrideFilter) {
        FilterValueDefinition filterValueDefinition = (FilterValueDefinition) overrideFilter;
        setFilteredValues(filterValueDefinition.getFilteredValues());
    }

    @Override
    public String asString(InsightRequestMetadata insightRequestMetadata) {
        if (filteredValues.size() == 1 && "All".equals(filteredValues.get(0).toString())) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filteredValues.size(); i++) {
            sb.append(filteredValues.get(i));
            if (i < (filteredValues.size() - 2)) {
                sb.append(", ");
            } else if (i == (filteredValues.size() - 2)) {
                sb.append(" or ");
            }
        }
        return sb.toString();
    }

    @Override
    public String fullString(InsightRequestMetadata insightRequestMetadata) {
        return getField().toDisplay() + " is " + asString(insightRequestMetadata);
    }
}