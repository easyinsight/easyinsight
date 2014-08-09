package com.easyinsight.pipeline;

import com.easyinsight.analysis.DataResults;
import com.easyinsight.analysis.IRow;
import com.easyinsight.analysis.TextReportFieldExtension;
import com.easyinsight.core.DateValue;
import com.easyinsight.core.StringValue;
import com.easyinsight.core.Value;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.analysis.AnalysisItem;

import java.util.*;

/**
 * User: jamesboe
 * Date: Sep 4, 2009
 * Time: 11:10:39 AM
 */
public class SortComponent implements IComponent {
    public DataSet apply(DataSet dataSet, PipelineData pipelineData) {
        final List<AnalysisItem> sortItems = new ArrayList<AnalysisItem>(pipelineData.getReportItems());

        int fixedSequenceMax = 0;
        Set<AnalysisItem> setByFixed = new HashSet<>();
        for (AnalysisItem test : sortItems) {
            if (test.getReportFieldExtension() != null && test.getReportFieldExtension() instanceof TextReportFieldExtension) {
                TextReportFieldExtension ext = (TextReportFieldExtension) test.getReportFieldExtension();
                if (!"Default".equals(ext.getFixedSort())) {
                    test.setSortSequence(ext.getFixedSortOrder() + 1);
                    fixedSequenceMax = Math.max(ext.getFixedSortOrder(), fixedSequenceMax);
                    test.setSort("Ascending".equals(ext.getFixedSort()) ? 1 : 2);
                    setByFixed.add(test);
                }
            }
        }

        Iterator<AnalysisItem> iter = sortItems.iterator();
        while (iter.hasNext()) {
            AnalysisItem item = iter.next();

            if (item.getSortSequence() == 0 || item.getSort() == 0) {
                iter.remove();
            }
        }



        boolean needToSort = !sortItems.isEmpty();

        if (!needToSort) {
            return dataSet;
        }

        for (AnalysisItem item : sortItems) {
            if (!setByFixed.contains(item)) {
                item.setSortSequence(item.getSortSequence() + fixedSequenceMax);
            }
        }

        Collections.sort(sortItems, new Comparator<AnalysisItem>() {

            public int compare(AnalysisItem analysisItem, AnalysisItem analysisItem1) {
                int sortSequence = analysisItem.getSortSequence();
                if (sortSequence == 0) {
                    sortSequence = Integer.MAX_VALUE;
                }
                int sortSequence2 = analysisItem1.getSortSequence();
                if (sortSequence2 == 0) {
                    sortSequence2 = Integer.MAX_VALUE;
                }
                if (sortSequence < sortSequence2) {
                    return -1;
                }
                if (sortSequence > sortSequence2) {
                    return 1;
                }
                return 0;
            }
        });
        Collections.sort(dataSet.getRows(), new Comparator<IRow>() {

            public int compare(IRow row1, IRow row2) {
                int i = 0;
                int comparison = 0;
                while (comparison == 0 && i < sortItems.size()) {
                    AnalysisItem field = sortItems.get(i);
                    comparison = getComparison(field, row1, row2);
                    i++;
                }
                return comparison;
            }
        });
        return dataSet;
    }

    private int getComparison(AnalysisItem field, IRow row1, IRow row2) {
        int comparison = 0;
        int ascending = field.getSort() == 2 ? -1 : 1;
        Value value1;

            value1 = row1.getValue(field);

        Value value2;

            value2 = row2.getValue(field);


        if (value1.type() == Value.NUMBER && value2.type() == Value.NUMBER) {
            comparison = value1.toDouble().compareTo(value2.toDouble()) * ascending;
        } else if (value1.type() == Value.DATE && value2.type() == Value.DATE) {
            DateValue date1 = (DateValue) value1;
            DateValue date2 = (DateValue) value2;
            comparison = date1.getDate().compareTo(date2.getDate()) * ascending;
        } else if (value1.type() == Value.DATE && value2.type() == Value.EMPTY) {
            comparison = ascending;
        } else if (value1.type() == Value.EMPTY && value2.type() == Value.DATE) {
            comparison = -ascending;
        } else if (value1.type() == Value.STRING && value2.type() == Value.STRING) {
            StringValue stringValue1 = (StringValue) value1;
            StringValue stringValue2 = (StringValue) value2;
            comparison = stringValue1.getValue().compareTo(stringValue2.getValue()) * ascending;
        } else if (value1.type() == Value.STRING && value2.type() == Value.EMPTY) {
            comparison = -ascending;
        } else if (value1.type() == Value.EMPTY && value2.type() == Value.STRING) {
            comparison = ascending;
        }
        return comparison;
    }

    public void decorate(DataResults listDataResults) {
        
    }
}
