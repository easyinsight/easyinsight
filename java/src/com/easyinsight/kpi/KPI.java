package com.easyinsight.kpi;

import com.easyinsight.analysis.*;
import com.easyinsight.core.InsightDescriptor;
import com.easyinsight.core.Key;
import com.easyinsight.datafeeds.FeedDefinition;

import java.util.*;

/**
 * User: jamesboe
 * Date: Jan 18, 2010
 * Time: 2:38:30 PM
 */
public class KPI implements Cloneable {

    public static final int GOOD = 2;
    public static final int BAD = 1;

    private long kpiID;

    private long coreFeedID;
    private String coreFeedName;
    private String coreFeedUrlKey;
    private AnalysisMeasure analysisMeasure;
    private AnalysisDateDimension dateDimension;
    private List<FilterDefinition> filters = new ArrayList<FilterDefinition>();
    
    private boolean goalDefined;

    private int dayWindow;
    private double threshold;
    
    private List<FilterDefinition> problemConditions = new ArrayList<FilterDefinition>();
    private int highIsGood;
    private double goalValue;

    private String name;
    private String description;
    private String iconImage;

    private boolean temporary;

    private KPIOutcome kpiOutcome;

    private List<InsightDescriptor> reports = new ArrayList<InsightDescriptor>();

    private List<KPIUser> kpiUsers = new ArrayList<KPIUser>();

    public List<KPIUser> getKpiUsers() {
        return kpiUsers;
    }

    public void setKpiUsers(List<KPIUser> kpiUsers) {
        this.kpiUsers = kpiUsers;
    }

    public List<InsightDescriptor> getReports() {
        return reports;
    }

    public void setReports(List<InsightDescriptor> reports) {
        this.reports = reports;
    }

    public boolean isGoalDefined() {
        return goalDefined;
    }

    public void setGoalDefined(boolean goalDefined) {
        this.goalDefined = goalDefined;
    }

    public AnalysisDateDimension getDateDimension() {
        return dateDimension;
    }

    public void setDateDimension(AnalysisDateDimension dateDimension) {
        this.dateDimension = dateDimension;
    }

    public List<FilterDefinition> getProblemConditions() {
        return problemConditions;
    }

    public void setProblemConditions(List<FilterDefinition> problemConditions) {
        this.problemConditions = problemConditions;
    }

    public int getDayWindow() {
        return dayWindow;
    }

    public void setDayWindow(int dayWindow) {
        this.dayWindow = dayWindow;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int getHighIsGood() {
        return highIsGood;
    }

    public void setHighIsGood(int highIsGood) {
        this.highIsGood = highIsGood;
    }

    public double getGoalValue() {
        return goalValue;
    }

    public void setGoalValue(double goalValue) {
        this.goalValue = goalValue;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    private void cleanup(AnalysisItem analysisItem, boolean changingDataSource) {
        if (changingDataSource) {
            // TODO: validate calculations and lookup tables--if necessary to create, should emit something with the report
            analysisItem.setLookupTableID(null);
            analysisItem.setBasedOnReportField(null);
        }
    }

    public KPI clone(FeedDefinition target, List<AnalysisItem> allFields, boolean changingDataSource) {
        try {
            Map<Long, AnalysisItem> replacementMap = new HashMap<Long, AnalysisItem>();
            AnalysisItem clonedItem = getAnalysisMeasure().clone();
            cleanup(clonedItem, changingDataSource);
            replacementMap.put(getAnalysisMeasure().getAnalysisItemID(), clonedItem);
            KPI clonedKPI = (KPI) super.clone();
            clonedKPI.setKpiID(0);
            clonedKPI.setKpiOutcome(null);
            clonedKPI.setCoreFeedID(target.getDataFeedID());
            clonedKPI.setCoreFeedUrlKey(target.getApiKey());
            clonedKPI.setCoreFeedName(target.getFeedName());
            clonedKPI.setAnalysisMeasure((AnalysisMeasure) clonedItem);
            List<FilterDefinition> filterDefinitions = new ArrayList<FilterDefinition>();
            if (this.getFilters() != null) {
                for (FilterDefinition persistableFilterDefinition : this.getFilters()) {
                    filterDefinitions.add(persistableFilterDefinition.clone());
                    List<AnalysisItem> filterItems = persistableFilterDefinition.getAnalysisItems(allFields, new ArrayList<AnalysisItem>(), true, true, new HashSet<AnalysisItem>(), new AnalysisItemRetrievalStructure(null));
                    for (AnalysisItem item : filterItems) {
                        if (replacementMap.get(item.getAnalysisItemID()) == null) {
                            AnalysisItem clonedFilterItem = item.clone();
                            cleanup(clonedFilterItem, changingDataSource);
                            replacementMap.put(item.getAnalysisItemID(), clonedFilterItem);
                        }
                    }
                }
            }
            for (AnalysisItem analysisItem : replacementMap.values()) {
                Key key = null;
                AnalysisItem dataSourceItem = target.findAnalysisItemByDisplayName(analysisItem.toDisplay());
                if (dataSourceItem != null) {
                    key = dataSourceItem.getKey();
                } else {
                    if (analysisItem.getOriginalDisplayName() != null) {
                        dataSourceItem = target.findAnalysisItemByDisplayName(analysisItem.getOriginalDisplayName());
                    }
                    if (dataSourceItem != null) {
                        key = dataSourceItem.getKey();
                    } else {
                        dataSourceItem = target.findAnalysisItem(analysisItem.getKey().toKeyString());
                        if (dataSourceItem != null) {
                            key = dataSourceItem.getKey();
                        }
                    }
                }
                if (key != null) {
                    analysisItem.setKey(key);
                } else {
                    Key clonedKey = analysisItem.getKey().clone();
                    analysisItem.setKey(clonedKey);
                }
            }
            ReplacementMap replacements = ReplacementMap.fromMap(replacementMap);
            for (AnalysisItem analysisItem : replacementMap.values()) {
                analysisItem.updateIDs(replacements);
            }
            for (FilterDefinition filter : filterDefinitions) {
                filter.updateIDs(replacements);
            }
            clonedKPI.setFilters(filterDefinitions);
            return clonedKPI;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateIDs(ReplacementMap replacementMap) {
        for (FilterDefinition filter : filters) {
            filter.updateIDs(replacementMap);
        }
    }

    public List<FilterDefinition> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterDefinition> filters) {
        this.filters = filters;
    }

    public KPIOutcome getKpiOutcome() {
        return kpiOutcome;
    }

    public void setKpiOutcome(KPIOutcome kpiOutcome) {
        this.kpiOutcome = kpiOutcome;
    }

    public long getKpiID() {
        return kpiID;
    }

    public void setKpiID(long kpiID) {
        this.kpiID = kpiID;
    }

    public long getCoreFeedID() {
        return coreFeedID;
    }

    public void setCoreFeedID(long coreFeedID) {
        this.coreFeedID = coreFeedID;
    }

    public String getCoreFeedName() {
        return coreFeedName;
    }

    public void setCoreFeedName(String coreFeedName) {
        this.coreFeedName = coreFeedName;
    }

    public AnalysisMeasure getAnalysisMeasure() {
        return analysisMeasure;
    }

    public void setAnalysisMeasure(AnalysisMeasure analysisMeasure) {
        this.analysisMeasure = analysisMeasure;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconImage() {
        return iconImage;
    }

    public void setIconImage(String iconImage) {
        this.iconImage = iconImage;
    }

    public String createIconText() {
        if(getKpiOutcome().isProblemEvaluated()) {
            return "bullet_square_glass_red.png";
        } else if (getProblemConditions().size() > 0) {
            return "bullet_ball_green.png";
        }
        switch(getKpiOutcome().getOutcomeState()) {
            case KPIOutcome.EXCEEDING_GOAL:
            case KPIOutcome.POSITIVE:
                if (getKpiOutcome().getDirection() == KPIOutcome.UP_DIRECTION) {
                    return "arrow2_up_green.png";
                } else if (getKpiOutcome().getDirection() == KPIOutcome.DOWN_DIRECTION) {
                    return "arrow2_down_green.png";
                } else {
                    return "bullet_ball_green.png";
                }
            case KPIOutcome.NEGATIVE:
                if (getKpiOutcome().getDirection() == KPIOutcome.UP_DIRECTION) {
                    return "arrow2_up_red.png";
                } else if (getKpiOutcome().getDirection() == KPIOutcome.DOWN_DIRECTION) {
                    return "arrow2_down_red.png";
                } else {
                    return "bullet_square_glass_red.png";
                }
            case KPIOutcome.NEUTRAL:
                if (getKpiOutcome().getDirection() == KPIOutcome.UP_DIRECTION) {
                    return "arrow2_up_blue.png";
                } else if (getKpiOutcome().getDirection() == KPIOutcome.DOWN_DIRECTION) {
                    return "arrow2_down_blue.png";
                } else {
                    return "bullet_ball_blue.png";
                }
            case KPIOutcome.NO_DATA:
            default:
                return "bullet_square_grey.png";
        }
    }

    public String getCoreFeedUrlKey() {
        return coreFeedUrlKey;
    }

    public void setCoreFeedUrlKey(String coreFeedUrlKey) {
        this.coreFeedUrlKey = coreFeedUrlKey;
    }
    
    public AnalysisMeasure createMeasure() throws CloneNotSupportedException {
        AnalysisMeasure analysisMeasure1 = (AnalysisMeasure) getAnalysisMeasure().clone();
        if (analysisMeasure1.getFilters() == null) {
            analysisMeasure1.setFilters(new ArrayList<FilterDefinition>());
        }
        TrendReportFieldExtension trendReportFieldExtension = new TrendReportFieldExtension();
        for (FilterDefinition filter : getFilters()) {
            if (filter instanceof RollingFilterDefinition) {
                RollingFilterDefinition rollingFilterDefinition = (RollingFilterDefinition) filter;
                trendReportFieldExtension.setDate(rollingFilterDefinition.getField());
            } else {
                analysisMeasure1.getFilters().add(filter);
            }
        }
        trendReportFieldExtension.setIconImage(iconImage);
        trendReportFieldExtension.setHighLow(highIsGood);
        analysisMeasure1.setReportFieldExtension(trendReportFieldExtension);
        return analysisMeasure1;
    }
}
