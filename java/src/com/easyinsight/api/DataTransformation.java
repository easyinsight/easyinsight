package com.easyinsight.api;

import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.AnalysisItemTypes;
import com.easyinsight.analysis.*;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.core.*;

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
        IRow transformedRow = new com.easyinsight.analysis.Row();
        StringPair[] stringPairs = row.getStringPairs();
        if (stringPairs != null) {
            for (StringPair stringPair : stringPairs) {
                Integer type = typeMap.get(stringPair.getKey());
                if (type == null) {
                    throw new RuntimeException("Unrecognized field " + stringPair.getKey() + " passed as a StringPair.");
                }
                if (type != Value.STRING) {
                    if (stringPair.getValue() == null || "".equals(stringPair.getValue())) {
                        transformedRow.addValue(stringPair.getKey(), new EmptyValue());
                    } else {
                        throw new RuntimeException("Field " + stringPair.getKey() + " was passed as a StringPair value when Easy Insight was expecting a " +
                            (type == Value.NUMBER ? "NumberPair." : "DatePair."));
                    }
                } else {
                    transformedRow.addValue(stringPair.getKey(), new StringValue(stringPair.getValue()));
                }
            }
        }
        NumberPair[] numberPairs = row.getNumberPairs();
        if (numberPairs != null) {
            for (NumberPair numberPair : numberPairs) {
                Integer type = typeMap.get(numberPair.getKey());
                if (type == null) {
                    throw new RuntimeException("Unrecognized field " + numberPair.getKey() + " passed as a NumberPair.");
                }
                if (type == Value.STRING) {
                    transformedRow.addValue(numberPair.getKey(), new StringValue(String.valueOf(numberPair.getValue())));
                } else if (type == Value.DATE) {
                    throw new RuntimeException("Field " + numberPair.getKey() + " was passed as a NumberPair value when Easy Insight was expecting a " +
                        ("DatePair."));
                } else {
                    transformedRow.addValue(numberPair.getKey(), new NumericValue(numberPair.getValue()));
                }
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
                if (datePair.getValue() == null) {
                    transformedRow.addValue(datePair.getKey(), new EmptyValue());
                } else {
                    transformedRow.addValue(datePair.getKey(), new DateValue(datePair.getValue()));
                }
            }
        }
        return transformedRow;
    }
}
