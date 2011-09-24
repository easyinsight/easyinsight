package com.easyinsight.calculations;

import com.easyinsight.core.*;

/**
 * User: jamesboe
 * Date: 9/22/11
 * Time: 2:59 PM
 */
public class TagsFunction extends Function {
    public Value evaluate() {
        Value tagValue = params.get(0);
        Value originalTagValue = tagValue.getOriginalValue();
        if (originalTagValue.type() == Value.STRING) {
            StringValue stringValue = (StringValue) originalTagValue;
            String[] tokens = stringValue.getValue().split(",");

            for (int i = 0; i < tokens.length; i++) {
                String tag = tokens[i].trim();
                for (int j = 1; j < params.size(); j++) {
                    String param = minusQuotes(j);
                    if (tag.equals(param)) {
                        return new StringValue(tag);
                    }
                }
            }
        }
        return new EmptyValue();
    }

    public int getParameterCount() {
        return -1;
    }
}
