package com.easyinsight.pipeline;

import com.easyinsight.analysis.*;
import com.easyinsight.calculations.CalcGraph;
import com.easyinsight.datafeeds.FeedService;
import com.easyinsight.etl.LookupTable;

import java.util.*;

/**
 * User: jamesboe
 * Date: 3/13/11
 * Time: 12:16 PM
 */
public class AltCompositeReportPipeline extends Pipeline {
    private Collection<AnalysisItem> joinItems;

    public AltCompositeReportPipeline(Collection<AnalysisItem> joinItems) {
        this.joinItems = joinItems;
    }

    @Override
    protected List<IComponent> generatePipelineCommands(Set<AnalysisItem> allNeededAnalysisItems, Set<AnalysisItem> reportItems, Collection<FilterDefinition> filters, WSAnalysisDefinition report, List<AnalysisItem> allItems) {
        List<IComponent> components = new ArrayList<IComponent>();
        for (AnalysisItem analysisItem : allNeededAnalysisItems) {
            if (analysisItem.getLookupTableID() != null && analysisItem.getLookupTableID() > 0) {
                LookupTable lookupTable = new FeedService().getLookupTable(analysisItem.getLookupTableID());
                if (lookupTable.getSourceField().hasType(AnalysisItemTypes.LISTING)) {
                    AnalysisList analysisList = (AnalysisList) lookupTable.getSourceField();
                    if (analysisList.isMultipleTransform()) components.add(new TagTransformComponent(analysisList));
                } else if (lookupTable.getSourceField().hasType(AnalysisItemTypes.DERIVED_DIMENSION)) {
                    Set<AnalysisItem> analysisItems = new HashSet<AnalysisItem>();
                    analysisItems.add(lookupTable.getSourceField());
                    components.addAll(new CalcGraph().doFunGraphStuff(analysisItems, allItems, reportItems, true));
                }
                components.add(new LookupTableComponent(lookupTable));
            }
        }
        for (AnalysisItem range : items(AnalysisItemTypes.RANGE_DIMENSION, allNeededAnalysisItems)) {
            components.add(new RangeComponent((AnalysisRangeDimension) range));
        }
        components.addAll(new CalcGraph().doFunGraphStuff(allNeededAnalysisItems, allItems, reportItems, true));
        for (AnalysisItem item : joinItems) {
            components.add(new DateTransformComponent(item));
        }
        return components;
    }
}
