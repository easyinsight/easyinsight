package com.easyinsight.analysis;

import com.easyinsight.analysis.definitions.WSTimeline;
import com.easyinsight.core.Key;
import com.easyinsight.sequence.Sequence;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * User: jamesboe
 * Date: Nov 30, 2009
 * Time: 8:24:40 AM
 */
@Entity
@Table(name="timeline_report")
@PrimaryKeyJoinColumn(name="report_state_id")
public class TimelineDefinitionState extends AnalysisDefinitionState {

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name="contained_report_id")
    private AnalysisDefinition containedReport;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="report_sequence_id")
    private Sequence filter;

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="timeline_report_id")
    private long definitionID;

    public void setDefinitionID(long definitionID) {
        this.definitionID = definitionID;
    }

    public long getDefinitionID() {
        return definitionID;
    }

    public AnalysisDefinition getContainedReport() {
        return containedReport;
    }

    public void setContainedReport(AnalysisDefinition containedReport) {
        this.containedReport = containedReport;
    }

    public Sequence getFilter() {
        return filter;
    }

    public void setFilter(Sequence filter) {
        this.filter = filter;
    }

    @Override
    public WSAnalysisDefinition createWSDefinition() {
        WSTimeline timeline = new WSTimeline();
        timeline.setSequence(filter);
        timeline.setReport(containedReport.createBlazeDefinition());
        return timeline;
    }

    @Override
    public AnalysisDefinitionState clone(Map<Key, Key> keyMap, List<AnalysisItem> allFields) throws CloneNotSupportedException {
        TimelineDefinitionState timelineDefinitionState = (TimelineDefinitionState) super.clone(keyMap, allFields);
        timelineDefinitionState.setDefinitionID(0);
        timelineDefinitionState.setFilter(filter.clone());
        //timelineDefinitionState.setContainedReport(containedReport.clone(keyMap, allFields));
        return timelineDefinitionState;
    }

    @Override
    public void updateIDs(Map<Long, AnalysisItem> replacementMap, Map<Key, Key> keyMap) throws CloneNotSupportedException {
        super.updateIDs(replacementMap, keyMap);
        filter.updateIDs(replacementMap, keyMap);
    }

    @Override
    public Collection<? extends AnalysisDefinition> containedReports() {
        return Arrays.asList(containedReport);
    }

    public void updateReportIDs(Map<Long, AnalysisDefinition> reportReplacementMap) {
        containedReport = reportReplacementMap.get(containedReport.getAnalysisID());
    }

    public void afterLoad() {
        filter.afterLoad();
    }
}
