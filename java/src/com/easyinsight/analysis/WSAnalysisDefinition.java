package com.easyinsight.analysis;

import com.easyinsight.dataset.DataSet;
import com.easyinsight.dataset.LimitsResults;
import com.easyinsight.core.Key;
import com.easyinsight.pipeline.ResultsBridge;
import com.easyinsight.scrubbing.DataScrub;

import java.util.*;
import java.io.Serializable;

import org.jetbrains.annotations.Nullable;

/**
 * User: James Boe
 * Date: Jan 10, 2008
 * Time: 7:35:07 PM
 */
public abstract class WSAnalysisDefinition implements Serializable {

    public static final int LIST = 1;
    public static final int CROSSTAB = 2;
    public static final int MAP = 3;
    public static final int COLUMN = 4;
    public static final int COLUMN3D = 5;
    public static final int BAR = 6;
    public static final int BAR3D = 7;
    public static final int PIE = 8;
    public static final int PIE3D = 9;
    public static final int LINE = 10;
    public static final int LINE3D = 11;
    public static final int AREA = 12;
    public static final int AREA3D = 13;
    public static final int PLOT = 14;
    public static final int BUBBLE = 15;
    public static final int GAUGE = 16;
    public static final int TREE = 17;
    public static final int TREE_MAP = 18;
    public static final int MM_LINE = 18;
    public static final int MAP_WORLD = 20;
    public static final int MAP_USA = 21;
    public static final int MAP_ASIA = 22;
    public static final int MAP_AMERICAS = 23;
    public static final int MAP_EUROPE = 24;
    public static final int MAP_MIDDLE_EAST = 25;
    public static final int TIMELINE = 26;
    public static final int HEATMAP = 27;

    private String name;
    private String authorName;
    private long analysisID;
    private long dataFeedID;
    private int reportType;
    private long reportStateID;
    private List<FilterDefinition> filterDefinitions;
    private List<DataScrub> dataScrubs;
    private int policy;
    private List<Tag> tagCloud = new ArrayList<Tag>();
    private List<AnalysisItem> addedItems = new ArrayList<AnalysisItem>();
    private boolean rootDefinition;
    private boolean canSaveDirectly;
    private boolean publiclyVisible;
    private boolean marketplaceVisible;
    private boolean solutionVisible;
    private boolean visibleAtFeedLevel;
    private Date dateCreated;
    private Date dateUpdated;
    private String description;
    private boolean temporaryReport;

    public boolean isTemporaryReport() {
        return temporaryReport;
    }

    public void setTemporaryReport(boolean temporaryReport) {
        this.temporaryReport = temporaryReport;
    }

    public boolean isSolutionVisible() {
        return solutionVisible;
    }

    public void setSolutionVisible(boolean solutionVisible) {
        this.solutionVisible = solutionVisible;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public long getReportStateID() {
        return reportStateID;
    }

    public void setReportStateID(long reportStateID) {
        this.reportStateID = reportStateID;
    }

    public int getReportType() {
        return reportType;
    }

    public void setReportType(int reportType) {
        this.reportType = reportType;
    }

    /**
     * Clears out collections to the minimum necessary for data display. Do not use this
     * if you're returning the report definition for the report editor.
     */
    public void optimizeSize() {
        filterDefinitions = null;
        dataScrubs = null;
        tagCloud = null;
        addedItems = null;
    }

    public boolean isVisibleAtFeedLevel() {
        return visibleAtFeedLevel;
    }

    public void setVisibleAtFeedLevel(boolean visibleAtFeedLevel) {
        this.visibleAtFeedLevel = visibleAtFeedLevel;
    }

    public boolean isPubliclyVisible() {
        return publiclyVisible;
    }

    public void setPubliclyVisible(boolean publiclyVisible) {
        this.publiclyVisible = publiclyVisible;
    }

    public boolean isMarketplaceVisible() {
        return marketplaceVisible;
    }

    public void setMarketplaceVisible(boolean marketplaceVisible) {
        this.marketplaceVisible = marketplaceVisible;
    }

    //private LimitsMetadata limitsMetadata;

    /*public LimitsMetadata getLimitsMetadata() {
        return limitsMetadata;
    }

    public void setLimitsMetadata(LimitsMetadata limitsMetadata) {
        this.limitsMetadata = limitsMetadata;
    }*/

    public boolean isCanSaveDirectly() {
        return canSaveDirectly;
    }

    public void setCanSaveDirectly(boolean canSaveDirectly) {
        this.canSaveDirectly = canSaveDirectly;
    }

    public boolean isRootDefinition() {
        return rootDefinition;
    }

    public void setRootDefinition(boolean rootDefinition) {
        this.rootDefinition = rootDefinition;
    }

    public List<AnalysisItem> getAddedItems() {
        return addedItems;
    }

    public void setAddedItems(List<AnalysisItem> addedItems) {
        this.addedItems = addedItems;
    }

    public List<Tag> getTagCloud() {
        return tagCloud;
    }

    public void setTagCloud(List<Tag> tagCloud) {
        this.tagCloud = tagCloud;
    }

    public int getPolicy() {
        return policy;
    }

    public void setPolicy(int policy) {
        this.policy = policy;
    }

    public List<DataScrub> getDataScrubs() {
        return dataScrubs;
    }

    public void setDataScrubs(List<DataScrub> dataScrubs) {
        this.dataScrubs = dataScrubs;
    }

    public List<FilterDefinition> getFilterDefinitions() {
        return filterDefinitions;
    }

    public void setFilterDefinitions(List<FilterDefinition> filterDefinitions) {
        this.filterDefinitions = filterDefinitions;
    }

    public abstract String getDataFeedType();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAnalysisID() {
        return analysisID;
    }

    public void setAnalysisID(long analysisID) {
        this.analysisID = analysisID;
    }

    public long getDataFeedID() {
        return dataFeedID;
    }

    public void setDataFeedID(long dataFeedID) {
        this.dataFeedID = dataFeedID;
    }

    public abstract Set<AnalysisItem> getAllAnalysisItems();

    public List<Key> getColumnKeys(List<AnalysisItem> allItems) {
        Set<Key> columnSet = new HashSet<Key>();
        Set<AnalysisItem> analysisItems = getAllAnalysisItems();
        for (AnalysisItem analysisItem : analysisItems) {
            List<AnalysisItem> items = analysisItem.getAnalysisItems(allItems, analysisItems, false);
            for (AnalysisItem item : items) {
                //if (item.getAnalysisItemID()) {
                    columnSet.add(item.getKey());
                //}
            }
        }
        if (retrieveFilterDefinitions() != null) {
            for (FilterDefinition filterDefinition : retrieveFilterDefinitions()) {
                List<AnalysisItem> items = filterDefinition.getField().getAnalysisItems(allItems, analysisItems, false);
                for (AnalysisItem item : items) {
                    //if (item.getAnalysisItemID() != 0) {
                        columnSet.add(item.getKey());
                    //}
                }
                //columnSet.add(filterDefinition.getField().getKey());
            }
        }
        if (getDataScrubs() != null) {
            for (DataScrub dataScrub : getDataScrubs()) {
                columnSet.addAll(dataScrub.createNeededKeys(analysisItems));
            }
        }
        for (AnalysisItem analysisItem : getLimitFields()) {
            columnSet.add(analysisItem.getKey());
        }
        return new ArrayList<Key>(columnSet);
    }

    public List<FilterDefinition> retrieveFilterDefinitions() {
        return filterDefinitions;
    }

    public Set<AnalysisItem> getColumnItems(List<AnalysisItem> allItems) {
        Set<AnalysisItem> columnSet = new HashSet<AnalysisItem>();
        Set<AnalysisItem> analysisItems = getAllAnalysisItems();
        for (AnalysisItem analysisItem : analysisItems) {
            if (analysisItem.isValid()) {
                List<AnalysisItem> items = analysisItem.getAnalysisItems(allItems, analysisItems, false);
                for (AnalysisItem item : items) {
                    //if (item.getAnalysisItemID()) {
                        columnSet.add(item);
                    //}
                }
                List<AnalysisItem> linkItems = analysisItem.addLinkItems(allItems, analysisItems);
                for (AnalysisItem item : linkItems) {
                    columnSet.add(item);
                }
            }
        }
        if (retrieveFilterDefinitions() != null) {
            for (FilterDefinition filterDefinition : retrieveFilterDefinitions()) {
                List<AnalysisItem> items = filterDefinition.getField().getAnalysisItems(allItems, analysisItems, false);
                for (AnalysisItem item : items) {
                    //if (item.getAnalysisItemID() != 0) {
                        columnSet.add(item);
                    //}
                }
                //columnSet.add(filterDefinition.getField().getKey());
            }
        }
        /*if (getDataScrubs() != null) {
            for (DataScrub dataScrub : getDataScrubs()) {
                columnSet.addAll(dataScrub.createNeededKeys(analysisItems));
            }
        }*/
        for (AnalysisItem analysisItem : getLimitFields()) {
            columnSet.add(analysisItem);
        }
        return columnSet;
    }

    public LimitsResults applyLimits(DataSet dataSet) {
        return new LimitsResults(false, dataSet.getRows().size(), dataSet.getRows().size());
    }

    public List<AnalysisItem> getLimitFields() {
        return new ArrayList<AnalysisItem>();
    }

    public Map<String, AnalysisItem> createStructure() {
        Map<String, AnalysisItem> structure = new HashMap<String, AnalysisItem>();
        createReportStructure(structure);
        return structure;
    }

    public abstract void createReportStructure(Map<String, AnalysisItem> structure);

    public abstract void populateFromReportStructure(Map<String, AnalysisItem> structure);

    @Nullable
    protected AnalysisItem firstItem(String key, Map<String, AnalysisItem> structure) {
        String compositeKey = key + "-" + 0;
        return structure.get(compositeKey);
    }

    protected List<AnalysisItem> items(String key, Map<String, AnalysisItem> structure) {
        List<AnalysisItem> items = new ArrayList<AnalysisItem>();
        boolean found = true;
        int i = 0;
        while (found) {
            String compositeKey = key + "-" + i;
            AnalysisItem value = structure.get(compositeKey);
            if (value == null) {
                found = false;
            } else {
                items.add(value);
            }
            i++;
        }
        Collections.sort(items, new Comparator<AnalysisItem>() {

            public int compare(AnalysisItem analysisItem, AnalysisItem analysisItem1) {
                return new Integer(analysisItem.getItemPosition()).compareTo(analysisItem1.getItemPosition());
            }
        });
        return items;
    }

    protected void addItems(String key, List<AnalysisItem> items, Map<String, AnalysisItem> structure) {
        for (int i = 0; i < items.size(); i++) {
            String compositeKey = key + "-" + i;
            structure.put(compositeKey, items.get(i));
            items.get(i).setItemPosition(i);
        }
    }

    public void applyFilters(List<FilterDefinition> drillThroughFilters) {
        for (FilterDefinition filterDefinition : drillThroughFilters) {
            for (FilterDefinition ourDefinition : retrieveFilterDefinitions()) {
                if (ourDefinition.getField().getKey().equals(filterDefinition.getField().getKey())) {
                    // match the filter?
                    if (ourDefinition instanceof FilterValueDefinition && filterDefinition instanceof FilterValueDefinition) {
                        FilterValueDefinition ourFilterValueDefinition = (FilterValueDefinition) ourDefinition;
                        FilterValueDefinition sourceFilterValueDefinition = (FilterValueDefinition) filterDefinition;
                        ourFilterValueDefinition.setFilteredValues(sourceFilterValueDefinition.getFilteredValues());
                    }
                }
            }
        }
    }

    public boolean hasCustomResultsBridge() {
        return false;
    }

    public ResultsBridge getCustomResultsBridge() {
        return null;
    }
}
