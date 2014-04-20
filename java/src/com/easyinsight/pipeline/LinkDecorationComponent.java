package com.easyinsight.pipeline;

import com.easyinsight.dataset.DataSet;
import com.easyinsight.analysis.*;
import com.easyinsight.core.Value;

import java.util.Map;
import java.util.HashMap;

/**
 * User: jamesboe
 * Date: Aug 24, 2009
 * Time: 7:34:27 AM
 */
public class LinkDecorationComponent implements IComponent {


    public DataSet apply(DataSet dataSet, PipelineData pipelineData) {
        for (AnalysisItem analysisItem : pipelineData.getReportItems()) {
            for (Link link : analysisItem.getLinks()) {
                if (link.generatesURL()) {
                    for (IRow row : dataSet.getRows()) {
                        String url = link.generateLink(row, pipelineData.getDataSourceProperties(), pipelineData.getAllItems());
                        if (url != null) {
                            Value value = row.getValue(analysisItem.createAggregateKey());
                            try {
                                if (pipelineData.getInsightRequestMetadata().isLogReport()) {
                                    System.out.println("Assigning " + url + " to " + value.toString() + " with qn " + analysisItem.qualifiedName());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Map<String, String> links = value.getLinks();
                            if (links == null) {
                                links = new HashMap<String, String>(1);
                                value.setLinks(links);
                            }
                            links.put(analysisItem.qualifiedName(), url);
                        }
                    }
                }
            }
        }

        return dataSet;
    }

    public void decorate(DataResults listDataResults) {

    }
}
