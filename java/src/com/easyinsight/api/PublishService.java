package com.easyinsight.api;

import com.easyinsight.dataset.DataSet;
import com.easyinsight.analysis.IRow;
import com.easyinsight.storage.IWhere;
import com.easyinsight.core.*;

import java.util.List;
import java.util.ArrayList;

/**
 * User: James Boe
 * Date: Jan 15, 2009
 * Time: 2:34:49 PM
 */
public abstract class PublishService {

    protected final DataSet toDataSet(Row row) {
        DataSet dataSet = new DataSet();
        toRow(row, dataSet);
        return dataSet;
    }

    protected final DataSet toDataSet(Row[] rows) {
        DataSet dataSet = new DataSet();
        for (Row row : rows) {
            toRow(row, dataSet);
        }
        return dataSet;
    }

    private IRow toRow(Row row, DataSet dataSet) {
        IRow transformedRow = dataSet.createRow();
        StringPair[] stringPairs = row.getStringPairs();
        if (stringPairs != null) {
            for (StringPair stringPair : stringPairs) {
                if (stringPair.getKey() == null) {
                    throw new ServiceRuntimeException("StringPair with value " + stringPair.getValue() + " had no key--key is a required field.");
                }
                transformedRow.addValue(stringPair.getKey(), stringPair.getValue() == null ? new EmptyValue() : new StringValue(stringPair.getValue()));
            }
        }
        NumberPair[] numberPairs = row.getNumberPairs();
        if (numberPairs != null) {
            for (NumberPair numberPair : numberPairs) {
                if (numberPair.getKey() == null) {
                    throw new ServiceRuntimeException("NumberPair with value " + numberPair.getValue() + " had no key--key is a required field.");
                }
                transformedRow.addValue(numberPair.getKey(), new NumericValue(numberPair.getValue()));
            }
        }
        DatePair[] datePairs = row.getDatePairs();
        if (datePairs != null) {
            for (DatePair datePair : datePairs) {
                if (datePair.getKey() == null) {
                    throw new ServiceRuntimeException("DatePair with value " + datePair.getValue() + " had no key--key is a required field.");
                }
                transformedRow.addValue(datePair.getKey(), datePair.getValue() == null ? new EmptyValue() : new DateValue(datePair.getValue()));
            }
        }
        return transformedRow;
    }

    protected final List<IWhere> createWheres(Where where) {
        List<IWhere> wheres = new ArrayList<IWhere>();
        StringWhere[] stringWheres = where.getStringWheres();
        if (stringWheres != null) {
            for (StringWhere stringWhere : stringWheres) {
                if (stringWhere.getKey() == null) {
                    throw new ServiceRuntimeException("StringWhere with value " + stringWhere.getValue() + " had no key--key is a required field.");
                }
                wheres.add(new com.easyinsight.storage.StringWhere(new NamedKey(stringWhere.getKey()), stringWhere.getValue()));
            }
        }
        NumberWhere[] numberWheres = where.getNumberWheres();
        if (numberWheres != null) {
            for (NumberWhere numberWhere : numberWheres) {
                if (numberWhere.getKey() == null) {
                    throw new ServiceRuntimeException("NumberWhere with value " + numberWhere.getValue() + " had no key--key is a required field.");
                }
                if (numberWhere.getComparison() == null) {
                    throw new ServiceRuntimeException("NumberWhere with key " + numberWhere.getKey() + " had no comparison--comparison is a required field.");
                }
                wheres.add(new com.easyinsight.storage.NumericWhere(new NamedKey(numberWhere.getKey()), numberWhere.getValue(), numberWhere.getComparison().createStorageComparison()));
            }
        }
        DateWhere[] dateWheres = where.getDateWheres();
        if (dateWheres != null) {
            for (DateWhere dateWhere : dateWheres) {
                if (dateWhere.getKey() == null) {
                    throw new ServiceRuntimeException("DateWhere with value " + dateWhere.getValue() + " had no key--key is a required field.");
                }
                if (dateWhere.getComparison() == null) {
                    throw new ServiceRuntimeException("DateWhere with key " + dateWhere.getKey() + " had no comparison--comparison is a required field.");
                }
                wheres.add(new com.easyinsight.storage.DateWhere(new NamedKey(dateWhere.getKey()), dateWhere.getValue(), dateWhere.getComparison().createStorageComparison()));
            }
        }
        DayWhere[] dayWheres = where.getDayWheres();
        if (dayWheres != null) {
            for (DayWhere dayWhere : dayWheres) {
                if (dayWhere.getKey() == null) {
                    throw new ServiceRuntimeException("DayWhere with value " + dayWhere.getDayOfYear() + " had no key--key is a required field.");
                }
                if (dayWhere.getYear() == 0) {
                    throw new ServiceRuntimeException("DayWhere with key " + dayWhere.getKey() + " was set to year 0.");
                }
                wheres.add(new com.easyinsight.storage.DayWhere(new NamedKey(dayWhere.getKey()), dayWhere.getYear(), dayWhere.getDayOfYear()));
            }
        }
        return wheres;
    }
}
