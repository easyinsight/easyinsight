package com.easyinsight.calculations.functions;

import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.AnalysisItemTypes;
import com.easyinsight.analysis.IRow;
import com.easyinsight.calculations.Function;
import com.easyinsight.calculations.FunctionException;
import com.easyinsight.calculations.ProcessCacheBuilder;
import com.easyinsight.calculations.ProcessCalculationCache;
import com.easyinsight.core.EmptyValue;
import com.easyinsight.core.Value;

import java.util.List;

/**
 * User: jamesboe
 * Date: 9/5/12
 * Time: 7:58 PM
 */
public class LastRecord extends Function {

    public Value evaluate() {
        String instanceIDName = minusBrackets(getParameterName(0));
        String targetName = minusBrackets(getParameterName(3));
        String sortName = minusBrackets(getParameterName(2));
        AnalysisItem instanceIDField = findDataSourceItem(0);
        AnalysisItem targetField = findDataSourceItem(3);
        AnalysisItem sortField = findDataSourceItem(2);
        for (AnalysisItem analysisItem : calculationMetadata.getDataSourceFields()) {
            if (instanceIDField == null && instanceIDName.equals(analysisItem.getKey().toKeyString())) {
                instanceIDField = analysisItem;
            }
            if (targetField == null && targetName.equals(analysisItem.getKey().toKeyString())) {
                targetField = analysisItem;
            }
            if (sortField == null && sortName.equals(analysisItem.getKey().toKeyString())) {
                sortField = analysisItem;
            }
        }
        if (instanceIDField == null) {
            throw new FunctionException("Could not find the specified field " + instanceIDName);
        }
        if (targetField == null) {
            throw new FunctionException("Could not find the specified field " + targetName);
        }
        if (sortField == null) {
            throw new FunctionException("Could not find the specified field " + sortName);
        }
        String processName = minusQuotes(getParameter(1)).toString();
        ProcessCalculationCache processCalculationCache = (ProcessCalculationCache) calculationMetadata.getCache(new ProcessCacheBuilder(instanceIDField, sortField), processName);
        Value instanceValue = getParameter(0);
        List<IRow> rows = processCalculationCache.rowsForValue(instanceValue);
        if (rows == null || rows.size() == 0) {
            return new EmptyValue();
        }
        Value sortValue = getParameter(2);
        IRow row = rows.get(rows.size() - 1);
        if (targetField.hasType(AnalysisItemTypes.MEASURE)) {
            Value measureValue = row.getValue(targetField);
            Value rowSortValue = row.getValue(sortField);
            if (sortValue.equals(rowSortValue)) {
                return measureValue;
            } else {
                return new EmptyValue();
            }
        } else {
            return row.getValue(targetField);
        }
    }

    @Override
    public boolean onDemand() {
        return true;
    }

    public int getParameterCount() {
        return 4;
    }
}
