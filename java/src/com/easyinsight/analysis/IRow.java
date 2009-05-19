package com.easyinsight.analysis;

import com.easyinsight.core.Key;
import com.easyinsight.core.Value;

import java.util.Collection;
import java.util.Map;
import java.util.Date;

/**
 * User: jboe
* Date: Dec 22, 2007
* Time: 10:35:33 AM
*/
public interface IRow {
    Value getValue(Key rowName);

    Value getValue(AnalysisItem analysisItem);

    void addValue(Key tag, Value value);

    void addValue(String tag, String value);

    void addValue(Key tag, String value);
    
    void addValue(String tag, Value value);

    void addValue(Key tag, Number value);

    void addValue(Key tag, Date value);

    IRow merge(IRow row);

    Collection<Key> getKeys();

    void addValues(Map<Key, Value> valueMap);

    Map<Key, Value> getValues();

    void replaceKey(Key existingKey, Key newKey);

    void removeValue(Key key);
}
