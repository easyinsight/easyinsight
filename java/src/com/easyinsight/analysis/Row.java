package com.easyinsight.analysis;

import com.easyinsight.core.*;
import com.easyinsight.dataset.DataSet;

import java.util.*;
import java.io.Serializable;

/**
 * User: jboe
 * Date: Dec 22, 2007
 * Time: 12:07:59 PM
 */
public class Row implements IRow, Serializable, Cloneable {

    private Value[] valueMap;

    private int joinCount;

    private DataSetKeys dataSetKeys;

    private long rowID;

    private boolean marked;

    private Map<String, Set<Value>> passthroughRow;

    public Map<String, Set<Value>> getPassthroughRow() {
        return passthroughRow;
    }

    public void incrementJoinCount() {
        joinCount++;
    }

    public int getJoinCount() {
        return joinCount;
    }

    public void resetJoinCount() {
        joinCount = 0;
    }

    public void setPassthroughRow(Map<String, Set<Value>> passthroughRow) {
        this.passthroughRow = passthroughRow;
    }

    public Row clone() throws CloneNotSupportedException {
        Row row = (Row) super.clone();
        Value[] copyMap = new Value[valueMap.length];
        for (int i = 0; i < valueMap.length; i++) {
            if (valueMap[i] != null) {
                copyMap[i] = valueMap[i].clone();
            }
        }
        row.valueMap = copyMap;

        return row;
    }

    public void setDataSetKeys(DataSetKeys dataSetKeys) {
        this.dataSetKeys = dataSetKeys;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public long getRowID() {
        return rowID;
    }

    public void setRowID(long rowID) {
        this.rowID = rowID;
    }

    public Row(DataSetKeys dataSetKeys) {
        valueMap = new Value[1];
        this.dataSetKeys = dataSetKeys;
    }

    public DataSetKeys getDataSetKeys() {
        return dataSetKeys;
    }

    public Row(int size, DataSetKeys dataSetKeys) {
        valueMap = new Value[size];
        this.dataSetKeys = dataSetKeys;
    }

    public Value getValue(Key rowName) {
        Short key = dataSetKeys.getKey(rowName);
        if (key >= valueMap.length) {
            return new EmptyValue();
        }
        Value value = valueMap[key];
        if (value == null) {
            return new EmptyValue();
        }
        return value;
    }

    public Value getValueNullOnEmpty(Key rowName) {
        Short key = dataSetKeys.getKeyNoAdd(rowName);
        if (key == null) {
            return null;
        }
        if (key >= valueMap.length) {
            return null;
        }
        Value value = valueMap[key];
        if (value == null) {
            return null;
        }
        return value;
    }

    public Value getValueNoAdd(Key rowName) {
        Short key = dataSetKeys.getKeyNoAdd(rowName);
        if (key == null) {
            return new EmptyValue();
        }
        if (key >= valueMap.length) {
            return new EmptyValue();
        }
        Value value = valueMap[key];
        if (value == null) {
            return new EmptyValue();
        }
        return value;
    }

    public Value getValue(AnalysisItem analysisItem) {
        Short key = dataSetKeys.getKey(analysisItem);
        if (key >= valueMap.length) {
            return new EmptyValue();
        }
        Value value = valueMap[key];
        if (value == null) {
            return new EmptyValue();
        }
        return value;
    }

    public void addValue(String tag, Value value) {
        addValue(new NamedKey(tag), value);
    }

    public void addValue(Key tag, Date value) {
        addValue(tag, new DateValue(value));
    }

    public void addValue(Key tag, Number value) {
        addValue(tag, new NumericValue(value));
    }

    public void addValue(Key tag, Value value) {
        int key = dataSetKeys.getKey(tag);
        if (key >= valueMap.length) {
            Value[] newVals = new Value[key + 1];
            System.arraycopy(valueMap, 0, newVals, 0, valueMap.length);
            valueMap = newVals;
        }
        valueMap[key] = value;
    }

    public void addValue(String tag, String value) {
        addValue(new NamedKey(tag), new StringValue(value));
    }

    public void addValue(Key tag, String value) {
        if (value == null) {
            addValue(tag, new EmptyValue());
        } else {
            addValue(tag, new StringValue(value));
        }
    }

    public List<Key> getKeys() {
        return dataSetKeys.getKeys();
    }

    public void addValues(Map<Key, Value> valueMap) {
        for (Map.Entry<Key, Value> entry : valueMap.entrySet()) {
            addValue(entry.getKey(), entry.getValue());
        }
    }

    public void addValues(IRow row) {
        for (int i = 0; i < row.getKeys().size(); i++) {
            Key key = row.getKeys().get(i);
            Value value = row.getValue(key);
            if (value != null) {
                addValue(key, value);
            }
        }
    }

    public Map<Key, Value> getValues() {
        Map<Key, Value> values = new HashMap<Key, Value>();
        for (int i = 0; i < dataSetKeys.getKeys().size(); i++) {
            Key key = dataSetKeys.getKeys().get(i);
            Value value = getValue(key);
            if (value != null) {
                values.put(key, value);
            }
        }
        return values;
    }

    public void replaceKey(Key existingKey, Key newKey) {
        Value existing = getValue(existingKey);
        if (existing != null) {
            addValue(newKey, existing);
        }
    }

    public void removeValue(Key key) {
        int position = dataSetKeys.getKey(key);
        if (position < valueMap.length) {
            valueMap[position] = null;
        }
    }

    public void addValue(String s, Number value) {
        addValue(new NamedKey(s), new NumericValue(value));
    }

    public IRow merge(IRow row, DataSet dataSet) {
        Row otherRow = (Row) row;
        IRow mergedRow;
        if (dataSet == null) {
            mergedRow = new Row(dataSetKeys.getKeys().size() + otherRow.dataSetKeys.getKeys().size(), dataSetKeys);
        } else {
            mergedRow = dataSet.createRow(dataSetKeys.getKeys().size() + otherRow.dataSetKeys.getKeys().size());
        }
        for (Key key : dataSetKeys.getKeys()) {
            Value value = getValue(key);
            mergedRow.addValue(key, value);
        }
        for (Key key : otherRow.dataSetKeys.getKeys()) {
            Value value = otherRow.getValue(key);
            if (value.type() != Value.EMPTY && (value.type() == Value.DATE || !"(Empty)".equals(value.toString()))) {
                mergedRow.addValue(key, value);
            }
        }
        return mergedRow;
    }

    public String toString() {
        Map<Key, Value> map = new HashMap<Key, Value>();
        for (Key key : dataSetKeys.getKeys()) {
            map.put(key, getValue(key));
        }
        return map.toString();
    }
}
