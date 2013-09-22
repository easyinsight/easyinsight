package com.easyinsight.analysis;

import com.easyinsight.core.XMLImportMetadata;
import com.easyinsight.core.XMLMetadata;
import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.Feed;
import com.easyinsight.pipeline.FilterComponent;
import com.easyinsight.pipeline.IComponent;
import com.easyinsight.pipeline.Pipeline;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Nodes;
import org.hibernate.Session;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * User: James Boe
 * Date: Jan 12, 2008
 * Time: 9:47:18 PM
 */
@Entity
@Table(name = "filter")
@Inheritance(strategy = InheritanceType.JOINED)
public class FilterDefinition implements Serializable, Cloneable {

    public static final int VALUE = 1;
    public static final int RANGE = 2;
    public static final int DATE = 3;
    public static final int ROLLING_DATE = 4;
    public static final int LAST_VALUE = 5;
    public static final int PATTERN = 6;
    public static final int FIRST_VALUE = 7;
    public static final int ORDERED = 8;
    public static final int OR = 9;
    public static final int NULL = 10;
    public static final int NAMED_REF = 11;
    public static final int FLAT_DATE = 12;
    public static final int ANALYSIS_ITEM = 13;
    public static final int MULTI_FLAT_DATE = 14;
    public static final int MONTH_CUTOFF = 15;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_item_id")
    private AnalysisItem field;
    @Column(name = "apply_before_aggregation")
    private boolean applyBeforeAggregation = true;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "filter_id")
    private long filterID;
    @Column(name = "intrinsic")
    private boolean intrinsic;
    @Column(name = "enabled")
    private boolean enabled = true;
    @Column(name = "show_on_report_view")
    private boolean showOnReportView = true;
    @Column(name = "filter_name")
    private String filterName;
    @Column(name = "column_level_template")
    private boolean templateFilter;
    @Column(name = "toggle_enabled")
    private boolean toggleEnabled;
    @Column(name = "minimum_role")
    private int minimumRole = 4;

    @Column(name = "parent_filters")
    private String parentFilters;

    @Column(name = "not_condition")
    private boolean notCondition;

    @Column(name = "marmotscript")
    private String marmotScript;

    @Column(name = "trend_filter")
    private boolean trendFilter;

    @Column(name = "field_choice_filter_label")
    private String fieldChoiceFilterLabel;

    @Column(name = "section")
    private int section;

    @Column(name = "flex_id")
    private long flexID;

    @Transient
    transient private String pipelineName;

    public long getFlexID() {
        return flexID;
    }

    public void setFlexID(long flexID) {
        this.flexID = flexID;
    }

    public void applyCalculationsBeforeRun(WSAnalysisDefinition report, List<AnalysisItem> allFields, Map<String, List<AnalysisItem>> keyMap, Map<String, List<AnalysisItem>> displayMap,
                                           Feed feed, EIConnection conn, List<FilterDefinition> dlsFilters, InsightRequestMetadata insightRequestMetadata) {

    }

    public int getSection() {
        return section;
    }

    public void setSection(int section) {
        this.section = section;
    }

    public String getFieldChoiceFilterLabel() {
        return fieldChoiceFilterLabel;
    }

    public void setFieldChoiceFilterLabel(String fieldChoiceFilterLabel) {
        this.fieldChoiceFilterLabel = fieldChoiceFilterLabel;
    }

    public boolean isNotCondition() {
        return notCondition;
    }

    public void setNotCondition(boolean notCondition) {
        this.notCondition = notCondition;
    }

    public String getPipelineName() {
        if (pipelineName == null) {
            if (applyBeforeAggregation) {
                pipelineName = Pipeline.BEFORE;
            } else {
                pipelineName = Pipeline.LAST_FILTERS;
            }
        }
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public FilterDefinition() {
    }

    public int type() {
        return 0;
    }

    public String getParentFilters() {
        return parentFilters;
    }

    public void setParentFilters(String parentFilters) {
        this.parentFilters = parentFilters;
    }

    public boolean isTrendFilter() {
        return trendFilter;
    }

    public void setTrendFilter(boolean trendFilter) {
        this.trendFilter = trendFilter;
    }

    public String getMarmotScript() {
        return marmotScript;
    }

    public void setMarmotScript(String marmotScript) {
        this.marmotScript = marmotScript;
    }

    public int getMinimumRole() {
        return minimumRole;
    }

    public void setMinimumRole(int minimumRole) {
        this.minimumRole = minimumRole;
    }

    public boolean isToggleEnabled() {
        return toggleEnabled;
    }

    public void setToggleEnabled(boolean toggleEnabled) {
        this.toggleEnabled = toggleEnabled;
    }

    public boolean isTemplateFilter() {
        return templateFilter;
    }

    public void setTemplateFilter(boolean templateFilter) {
        this.templateFilter = templateFilter;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public boolean isShowOnReportView() {
        return showOnReportView;
    }

    public void setShowOnReportView(boolean showOnReportView) {
        this.showOnReportView = showOnReportView;
    }

    public boolean isSingleSource() {
        return true;
    }

    public FilterDefinition(AnalysisItem field) {
        this.field = field;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isIntrinsic() {
        return intrinsic;
    }

    public void setIntrinsic(boolean intrinsic) {
        this.intrinsic = intrinsic;
    }

    public long getFilterID() {
        return filterID;
    }

    public void setFilterID(long filterID) {
        this.filterID = filterID;
    }

    public AnalysisItem getField() {
        return field;
    }

    public void setField(AnalysisItem field) {
        this.field = field;
    }

    public boolean isApplyBeforeAggregation() {
        return applyBeforeAggregation;
    }

    public void setApplyBeforeAggregation(boolean applyBeforeAggregation) {
        this.applyBeforeAggregation = applyBeforeAggregation;
    }

    public List<AnalysisItem> getAnalysisItems(List<AnalysisItem> allItems, Collection<AnalysisItem> insightItems, boolean getEverything, boolean includeFilters, Collection<AnalysisItem> analysisItemSet, AnalysisItemRetrievalStructure structure) {
        return getField().getAnalysisItems(allItems, insightItems, getEverything, includeFilters, analysisItemSet, structure);
    }

    public MaterializedFilterDefinition materialize(InsightRequestMetadata insightRequestMetadata) {
        throw new UnsupportedOperationException();
    }

    public String toQuerySQL(String tableName) {
        throw new UnsupportedOperationException();
    }

    public String toQuerySQL(String tableName, String sourceTableName, String sourceKeyName) {
        throw new UnsupportedOperationException();
    }

    public int populatePreparedStatement(PreparedStatement preparedStatement, int start, int type, InsightRequestMetadata insightRequestMetadata) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean validForQuery() {
        if (notCondition) {
            return false;
        }
        if (getField() != null) {
            if (getField().hasType(AnalysisItemTypes.STEP) || getField().hasType(AnalysisItemTypes.RANGE_DIMENSION)) {
                return false;
            }
            if (getField().getLookupTableID() != null && getField().getLookupTableID() > 0) {
                return false;
            }
            if (getField().isDerived()) {
                return false;
            }
            if (isTemplateFilter()) {
                return false;
            }
        }
        return enabled;
    }

    public FilterDefinition clone() throws CloneNotSupportedException {
        FilterDefinition filter = (FilterDefinition) super.clone();
        filter.setFilterID(0);
        return filter;
    }

    public void updateIDs(ReplacementMap replacementMap) {
        if (field != null) {
            setField(replacementMap.getField(field));
        }
    }

    public void beforeSave(Session session) {
        if (getField() != null) {
            getField().reportSave(session);
            if (getField().getKey().getKeyID() == 0) {
                session.save(getField().getKey());
            }
            if (getField().getAnalysisItemID() == 0) {
                session.save(getField());
            } else {
                session.update(getField());
            }
        }
    }

    public void afterLoad() {
        if (getField() != null) {
            setField((AnalysisItem) Database.deproxy(getField()));
            getField().afterLoad();
        }
    }

    public List<IComponent> createComponents(String pipelineName, IFilterProcessor filterProcessor, AnalysisItem sourceItem, boolean columnLevel) {
        List<IComponent> components = new ArrayList<IComponent>();
        if (isEnabled() && pipelineName.equals(getPipelineName())) {
            if (!isTemplateFilter() || columnLevel) {
                components.add(new FilterComponent(this, filterProcessor));
            }
        }
        /*if (beforeAggregation == isApplyBeforeAggregation()) {
            components.add(new FilterPipelineCleanupComponent(this));
        }*/
        return components;
    }

    public void customFromXML(Element element, XMLImportMetadata xmlImportMetadata) {

    }

    public static FilterDefinition fromXML(Element element, XMLImportMetadata xmlImportMetadata) {
        int filterType = Integer.parseInt(element.getAttribute("type").getValue());
        FilterDefinition filterDefinition;
        switch (filterType) {
            case FilterDefinition.VALUE:
                filterDefinition = new FilterValueDefinition();
                break;
            case FilterDefinition.ANALYSIS_ITEM:
                filterDefinition = new AnalysisItemFilterDefinition();
                break;
            case FilterDefinition.FIRST_VALUE:
                filterDefinition = new FirstValueFilter();
                break;
            case FilterDefinition.LAST_VALUE:
                filterDefinition = new LastValueFilter();
                break;
            case FilterDefinition.MULTI_FLAT_DATE:
                filterDefinition = new MultiFlatDateFilter();
                break;
            case FilterDefinition.NAMED_REF:
                filterDefinition = new NamedFilterReference();
                break;
            case FilterDefinition.NULL:
                filterDefinition = new NullFilter();
                break;
            case FilterDefinition.OR:
                filterDefinition = new OrFilter();
                break;
            case FilterDefinition.PATTERN:
                filterDefinition = new FilterPatternDefinition();
                break;
            case FilterDefinition.RANGE:
                filterDefinition = new FilterRangeDefinition();
                break;
            case FilterDefinition.ROLLING_DATE:
                filterDefinition = new RollingFilterDefinition();
                break;
            case FilterDefinition.DATE:
                filterDefinition = new FilterDateRangeDefinition();
                break;
            default:
                throw new RuntimeException();
        }
        Nodes fieldNodes = element.query("field/analysisItem");
        if (fieldNodes.size() == 1) {
            Element field = (Element) fieldNodes.get(0);
            filterDefinition.setField(AnalysisItem.fromXML(field, xmlImportMetadata));
        }
        filterDefinition.setEnabled(Boolean.parseBoolean(element.getAttribute("enabled").getValue()));
        filterDefinition.setApplyBeforeAggregation(Boolean.parseBoolean(element.getAttribute("applyBeforeAggregation").getValue()));
        filterDefinition.setIntrinsic(Boolean.parseBoolean(element.getAttribute("intrinsic").getValue()));
        filterDefinition.setShowOnReportView(Boolean.parseBoolean(element.getAttribute("showOnReportView").getValue()));
        filterDefinition.setTemplateFilter(Boolean.parseBoolean(element.getAttribute("templateFilter").getValue()));
        filterDefinition.setToggleEnabled(Boolean.parseBoolean(element.getAttribute("toggleEnabled").getValue()));
        filterDefinition.setTrendFilter(Boolean.parseBoolean(element.getAttribute("trendFilter").getValue()));
        filterDefinition.setMinimumRole(Integer.parseInt(element.getAttribute("minimumRole").getValue()));
        filterDefinition.setFilterName(element.getAttribute("filterName").getValue());
        filterDefinition.customFromXML(element, xmlImportMetadata);
        return filterDefinition;
    }

    public Element toXML(XMLMetadata xmlMetadata) {
        Element filterElement = new Element("filter");
        filterElement.addAttribute(new Attribute("type", String.valueOf(type())));
        filterElement.addAttribute(new Attribute("enabled", String.valueOf(enabled)));
        filterElement.addAttribute(new Attribute("applyBeforeAggregation", String.valueOf(applyBeforeAggregation)));
        filterElement.addAttribute(new Attribute("intrinsic", String.valueOf(intrinsic)));
        filterElement.addAttribute(new Attribute("showOnReportView", String.valueOf(showOnReportView)));
        filterElement.addAttribute(new Attribute("templateFilter", String.valueOf(templateFilter)));
        filterElement.addAttribute(new Attribute("toggleEnabled", String.valueOf(toggleEnabled)));
        filterElement.addAttribute(new Attribute("trendFilter", String.valueOf(trendFilter)));
        filterElement.addAttribute(new Attribute("minimumRole", String.valueOf(minimumRole)));
        filterElement.addAttribute(new Attribute("filterName", xmlMetadata.value(filterName)));
        filterElement.addAttribute(new Attribute("notCondition", String.valueOf(notCondition)));
        if (getField() != null) {
            Element fieldElement = new Element("field");
            filterElement.appendChild(fieldElement);
            fieldElement.appendChild(getField().toXML(xmlMetadata));
        }
        return filterElement;
    }

    public void timeshift(Feed dataSource, Collection<FilterDefinition> filters) {
        if (getField() != null) {
            if (getField().hasType(AnalysisItemTypes.DATE_DIMENSION)) {
                AnalysisDateDimension dateDim = (AnalysisDateDimension) getField();
                boolean dateTime = !dateDim.isDateOnlyField() && dataSource.getDataSource().checkDateTime(getField().toOriginalDisplayName(), getField().getKey());
                dateDim.setTimeshift(dateTime);
            }
        }               // https://www.pivotaltracker.com/story/show/37900405
    }

    public void calculationItems(Map<String, List<AnalysisItem>> map) {

    }

    public JSONObject toJSON(FilterHTMLMetadata filterHTMLMetadata) throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("id", getFilterID());
        jo.put("checkbox", isToggleEnabled());
        jo.put("enabled", isEnabled());
        jo.put("type", "other");
        jo.put("label", label(true));
        jo.put("visible", isShowOnReportView());
        return jo;
    }


    public String toHTML(FilterHTMLMetadata filterHTMLMetadata) {
        if (!isToggleEnabled()) {
            StringBuilder sb = new StringBuilder();
            String key = filterHTMLMetadata.getFilterKey();
            String function = filterHTMLMetadata.createOnChange();
            sb.append("<div class=\"filterLabel\">");
            sb.append(checkboxHTML(key, function));
            sb.append(label(false));
            sb.append("</div>");
            return sb.toString();
        } else {
            return "";
        }
    }

    protected String checkboxHTML(String key, String function) {
        String str = "<input style=\"margin-right:5px\" type=\"checkbox\" id=\"filter" + getFilterID() + "enabled\" onchange=\"filterEnable('filter" + getFilterID() + "', '" + key + "'," + function + ")\"";
        if (isEnabled()) {
            str += " checked=\"on\"";
        }
        str += "/>";
        return str;
    }

    public String label(boolean colon) {
        if (getFilterName() != null && !"".equals(getFilterName())) {
            return getFilterName() + (colon ? ":" : "");
        }
        if (getField() != null) {
            return getField().toDisplay() + (colon ? ":" : "");
        }
        return "";
    }

    public boolean sameFilter(FilterDefinition targetDefinition) {
        return getField().qualifiedName().equals(targetDefinition.getField().qualifiedName()) && type() == targetDefinition.type();
    }
}
