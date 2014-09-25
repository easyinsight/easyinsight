package com.easyinsight.calculations;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.IRow;
import com.easyinsight.core.Value;
import com.easyinsight.dataset.DataSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jamesboe
 * Date: 9/11/12
 * Time: 2:16 PM
 */
public class StatCalculationCache implements ICalculationCache {
    private AnalysisItem instanceID;
    private AnalysisItem statMeasure;
    private StatFunction statFunction;

    private Map<Value, List<IRow>> rowMap = new HashMap<>();
    //private Map<Value, DoubleArrayList> coltMap = new HashMap<>();
    private Map<Value, Double> resultMap = new HashMap<>();

    public StatCalculationCache(AnalysisItem instanceID, AnalysisItem statMeasure, StatFunction statFunction) {
        this.instanceID = instanceID;
        this.statMeasure = statMeasure;
        this.statFunction = statFunction;
    }

    @FunctionalInterface
    public interface StatFunction {
        double calculate(DoubleArrayList data);
    }

    public void fromDataSet(DataSet dataSet) {
        for (IRow row : dataSet.getRows()) {
            Value instanceIDValue = row.getValue(instanceID);
            List<IRow> rows = rowMap.get(instanceIDValue);
            if (rows == null) {
                rows = new ArrayList<IRow>();
                rowMap.put(instanceIDValue, rows);
            }
            rows.add(row);
        }
        for (Map.Entry<Value, List<IRow>> entry : rowMap.entrySet()) {
            List<IRow> list = entry.getValue();
            DoubleArrayList data = new DoubleArrayList();
            for (IRow row : list) {
                Value value = row.getValue(statMeasure);
                if (value.type() != Value.EMPTY) {
                    data.add(value.toDouble());
                }
            }
            double sumOfSquares = Descriptive.sumOfSquares(data);
            int size = data.size();
            double sum = Descriptive.sum(data);
            System.out.println(entry.getKey() + " - " + sum + " - " + size + " - " + sumOfSquares);
            double variance = Descriptive.variance(size, sum, sumOfSquares);
            System.out.println("\t" + variance);
            double result = statFunction.calculate(data);
            resultMap.put(entry.getKey(), result);
        }
    }

    public Double getResultForValue(Value value) {
        return resultMap.get(value);
    }
}
