package com.easyinsight.core;

import com.easyinsight.analysis.Aggregation;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * User: James Boe
 * Date: Jul 1, 2008
 * Time: 11:00:51 AM
 */
public class NumericValue extends Value implements Serializable {

    private Double value;
    private transient Aggregation aggregation;
    private static final long serialVersionUID = -9170406442789546755L;

    private int calendarType;
    private int calendarValue;

    public NumericValue() {
    }

    public NumericValue(Number value) {
        this.value = value.doubleValue();
    }

    public NumericValue(Double value, Aggregation aggregation) {
        this.value = value;
        this.aggregation = aggregation;
    }

    public int getCalendarType() {
        return calendarType;
    }

    public void setCalendarType(int calendarType) {
        this.calendarType = calendarType;
    }

    public int getCalendarValue() {
        return calendarValue;
    }

    public void setCalendarValue(int calendarValue) {
        this.calendarValue = calendarValue;
    }

    @Nullable
    public Aggregation getAggregation() {
        return aggregation;
    }

    public void setAggregation(Aggregation aggregation) {
        this.aggregation = aggregation;
    }

    @Override
    public String toString() {
        /*if (value.longValue() - value < .001) {
            return String.valueOf(value.longValue());
        }*/
        return String.valueOf(value);
    }

    public NumericValue(Double value) {
        this.value = value;
    }

    public void setValue(String valueObj) {
        this.value = produceDoubleValue(valueObj);
    }

    public static double produceDoubleValue(String valueObj) {
        Double value;
        boolean negative = false;
        if (valueObj == null || "".equals(valueObj)) {
            value = 0.;
        } else {
            try {
                value = Double.parseDouble(valueObj);
            } catch (NumberFormatException e) {
                // see if we can find a # in there somewhere...
                char[] transferArray = new char[valueObj.length()];
                int i = 0;
                boolean hitNumber = false;
                boolean seemsValid = true;

                for (char character : valueObj.toCharArray()) {
                    if (Character.isDigit(character) || character == '.') {
                        transferArray[i++] = character;
                        hitNumber = true;
                    } else {
                        if (character == '(' || character == ')') {
                            negative = true;
                        } else if (hitNumber && !(character == ',' || character == '%')) {
                            seemsValid = false;
                        }
                    }
                }
                if (seemsValid && transferArray.length > 0) {
                    try {
                        value = Double.parseDouble(new String(transferArray));
                    } catch (NumberFormatException e1) {
                        value = 0.;
                    }
                } else {
                    value = 0.;
                }
            }
        }
        if (negative) {
            value = -value;
        }
        return value;
    }

    public static double produceDoubleValueStrict(String valueObj) {
        Double value;
        if (valueObj == null || "".equals(valueObj)) {
            value = 0.;
        } else {
            try {
                value = Double.parseDouble(valueObj);
            } catch (NumberFormatException e) {
                // see if we can find a # in there somewhere...
                char[] transferArray = new char[valueObj.length()];
                int i = 0;
                boolean hitNumber = false;
                boolean seemsValid = true;
                for (char character : valueObj.toCharArray()) {
                    if (Character.isLetter(character)) {
                        seemsValid = false;
                    } else if (Character.isDigit(character) || character == '.') {
                        transferArray[i++] = character;
                        hitNumber = true;
                    } else {
                        if (hitNumber && !(character == ',' || character == '%')) {
                            seemsValid = false;
                        }
                    }
                }
                if (seemsValid && transferArray.length > 0) {
                    try {
                        value = Double.parseDouble(new String(transferArray));
                    } catch (NumberFormatException e1) {
                        value = 0.;
                    }
                } else {
                    value = 0.;
                }
            }
        }
        return value;
    }

    public static boolean testValue(String valueObj) {
        boolean value;
        if (valueObj == null || "".equals(valueObj)) {
            value = false;
        } else {
            try {
                Double.parseDouble(valueObj);
                value = true;
            } catch (NumberFormatException e) {
                // see if we can find a # in there somewhere...
                char[] transferArray = new char[valueObj.length()];
                int i = 0;
                boolean hitNumber = false;
                boolean seemsValid = true;
                for (char character : valueObj.toCharArray()) {
                    if (Character.isDigit(character) || character == '.') {
                        transferArray[i++] = character;
                        hitNumber = true;
                    } else {
                        if (hitNumber) {
                            seemsValid = false;
                        }
                    }
                }
                if (seemsValid && transferArray.length > 0) {
                    try {
                        Double.parseDouble(new String(transferArray));
                        value = true;
                    } catch (NumberFormatException e1) {
                        value = false;
                    }
                } else {
                    value = false;
                }
            }
        }
        return value;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public int type() {
        return Value.NUMBER;
    }

    public Double toDouble() {
        if (value != null) {
            if (value.isInfinite() || value.isNaN()) {
                value = 0.;
            }
        }
        return value;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NumericValue that = (NumericValue) o;

        return value.equals(that.value);

    }

    public int hashCode() {
        return value.hashCode();
    }

    public int compareTo(Value value) {
        if (value.type() == Value.NUMBER) {
            return this.toDouble().compareTo(value.toDouble());
        }
        return 0;
    }
}
