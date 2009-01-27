package com.easyinsight.api;

import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.AnalysisItem;
import com.easyinsight.AnalysisItemTypes;
import com.easyinsight.IRow;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.core.Value;
import com.easyinsight.core.StringValue;
import com.easyinsight.core.NumericValue;
import com.easyinsight.core.DateValue;

import java.util.Map;
import java.util.HashMap;

/**
 * User: James Boe
 * Date: Jan 16, 2009
 * Time: 7:18:24 PM
 */
public class DataTransformation {
    private Map<String, Integer> typeMap = new HashMap<String, Integer>();

    public DataTransformation(FeedDefinition feedDefinition) {
        for (AnalysisItem field : feedDefinition.getFields()) {
            typeMap.put(field.getKey().toKeyString(), field.hasType(AnalysisItemTypes.DATE_DIMENSION) ? Value.DATE :
                field.hasType(AnalysisItemTypes.MEASURE) ? Value.NUMBER : Value.STRING);
        }
    }

    public final DataSet toDataSet(Row row) {
        DataSet dataSet = new DataSet();
        dataSet.addRow(toRow(row));
        return dataSet;
    }

    public final DataSet toDataSet(Row[] rows) {
        DataSet dataSet = new DataSet();
        for (Row row : rows) {
            dataSet.addRow(toRow(row));
        }
        return dataSet;
    }

    public IRow toRow(Row row) {
        IRow transformedRow = new com.easyinsight.Row();
        StringPair[] stringPairs = row.getStringPairs();
        if (stringPairs != null) {
            for (StringPair stringPair : stringPairs) {
                Integer type = typeMap.get(stringPair.getKey());
                if (type == null) {
                    throw new RuntimeException("Unrecognized field " + stringPair.getKey() + " passed as a StringPair.");
                }
                if (type != Value.STRING) {
                    throw new RuntimeException("Field " + stringPair.getKey() + " was passed as a StringPair value when Easy Insight was expecting a " +
                        (type == Value.NUMBER ? "NumberPair." : "DatePair."));
                }
                transformedRow.addValue(stringPair.getKey(), new StringValue(stringPair.getValue()));
            }
        }
        NumberPair[] numberPairs = row.getNumberPairs();
        if (numberPairs != null) {
            for (NumberPair numberPair : numberPairs) {
                Integer type = typeMap.get(numberPair.getKey());
                if (type == null) {
                    throw new RuntimeException("Unrecognized field " + numberPair.getKey() + " passed as a NumberPair.");
                }
                if (type != Value.NUMBER) {
                    throw new RuntimeException("Field " + numberPair.getKey() + " was passed as a NumberPair value when Easy Insight was expecting a " +
                        (type == Value.DATE ? "DatePair." : "StringPair."));
                }
                transformedRow.addValue(numberPair.getKey(), new NumericValue(numberPair.getValue()));
            }
        }
        DatePair[] datePairs = row.getDatePairs();
        if (datePairs != null) {
            for (DatePair datePair : datePairs) {
                Integer type = typeMap.get(datePair.getKey());
                if (type == null) {
                    throw new RuntimeException("Unrecognized field " + datePair.getKey() + " passed as a DatePair.");
                }
                if (type != Value.DATE) {
                    throw new RuntimeException("Field " + datePair.getKey() + " was passed as a DatePair value when Easy Insight was expecting a " +
                        (type == Value.NUMBER ? "NumberPair." : "StringPair."));
                }
                transformedRow.addValue(datePair.getKey(), new DateValue(datePair.getValue()));
            }
        }
        return transformedRow;
    }
}
