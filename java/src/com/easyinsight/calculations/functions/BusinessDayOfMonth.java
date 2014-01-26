package com.easyinsight.calculations.functions;

import com.easyinsight.calculations.Function;
import com.easyinsight.core.DateValue;
import com.easyinsight.core.EmptyValue;
import com.easyinsight.core.NumericValue;
import com.easyinsight.core.Value;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * User: jamesboe
 * Date: 12/29/11
 * Time: 1:05 PM
 */
public class BusinessDayOfMonth extends Function {
    public Value evaluate() {
        Date startDate = null;
        if (params.size() == 0) {
            startDate = new Date();
        } else {
            Value start = params.get(0);
            if (start.type() == Value.DATE) {
                DateValue dateValue = (DateValue) start;
                startDate = dateValue.getDate();
            }
        }
        if (startDate != null) {
            Calendar calendar = Calendar.getInstance();
            int time = calculationMetadata.getInsightRequestMetadata().getUtcOffset() / 60;
            String string;
            if (time > 0) {
                string = "GMT-"+Math.abs(time);
            } else if (time < 0) {
                string = "GMT+"+Math.abs(time);
            } else {
                string = "GMT";
            }
            TimeZone timeZone = TimeZone.getTimeZone(string);
            calendar.setTimeZone(timeZone);
            calendar.setTimeInMillis(startDate.getTime());
            int targetDay = calendar.get(Calendar.DAY_OF_MONTH);
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(startDate);
            cal1.set(Calendar.DAY_OF_MONTH, 1);
            boolean daysFound = false;
            int i = 0;
            while (!daysFound) {
                int dayOfWeek = cal1.get(Calendar.DAY_OF_WEEK);
                if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) {

                } else {
                    i++;
                }
                cal1.add(Calendar.DAY_OF_YEAR, 1);
                if (cal1.get(Calendar.DAY_OF_MONTH) == targetDay) {
                    daysFound = true;
                }
            }
            return new NumericValue(i);
        } else {
            return new EmptyValue();
        }
    }

    public int getParameterCount() {
        return -1;
    }
}
