package com.easyinsight.analysis;

import com.easyinsight.database.Database;
import org.hibernate.Session;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

/**
 * User: jamesboe
 * Date: 3/1/11
 * Time: 2:24 PM
 */
@Entity
@Table(name="join_override")
public class JoinOverride implements Cloneable, Serializable {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="join_override_id")
    private long joinOverrideID;
    @Transient
    private String sourceName;
    @Transient
    private String targetName;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="source_analysis_item_id")
    private AnalysisItem sourceItem;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="target_analysis_item_id")
    private AnalysisItem targetItem;
    @Column(name="data_source_id")
    private Long dataSourceID;

    public JoinOverride clone() throws CloneNotSupportedException {
        JoinOverride joinOverride = (JoinOverride) super.clone();
        joinOverride.setJoinOverrideID(0);
        return joinOverride;
    }

    public Long getDataSourceID() {
        return dataSourceID;
    }

    public void setDataSourceID(Long dataSourceID) {
        this.dataSourceID = dataSourceID;
    }

    public void reportSave(Session session) {
        if (dataSourceID == 0) {
            dataSourceID = null;
        }
        sourceItem.reportSave(session);
        targetItem.reportSave(session);
    }

    public void afterLoad() {
        if (sourceItem != null) {
            sourceItem = (AnalysisItem) Database.deproxy(sourceItem);
            sourceItem.afterLoad();
        }
        if (targetItem != null) {
            targetItem = (AnalysisItem) Database.deproxy(targetItem);
            targetItem.afterLoad();
        }
    }

    public void updateIDs(Map<Long, AnalysisItem> replacementMap) {
        sourceItem = replacementMap.get(sourceItem.getAnalysisItemID());
        targetItem = replacementMap.get(targetItem.getAnalysisItemID());
    }

    public long getJoinOverrideID() {
        return joinOverrideID;
    }

    public void setJoinOverrideID(long joinOverrideID) {
        this.joinOverrideID = joinOverrideID;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public AnalysisItem getSourceItem() {
        return sourceItem;
    }

    public void setSourceItem(AnalysisItem sourceItem) {
        this.sourceItem = sourceItem;
    }

    public AnalysisItem getTargetItem() {
        return targetItem;
    }

    public void setTargetItem(AnalysisItem targetItem) {
        this.targetItem = targetItem;
    }
}
