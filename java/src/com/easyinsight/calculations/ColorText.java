package com.easyinsight.calculations;

import com.easyinsight.analysis.TextValueExtension;
import com.easyinsight.core.Value;

/**
 * User: jamesboe
 * Date: 10/6/11
 * Time: 12:01 PM
 */
public class ColorText extends Function {
    public Value evaluate() {
        Value target = params.get(0);
        String colorString = minusQuotes(1);
        int color = Integer.parseInt(colorString, 16);
        TextValueExtension textValueExtension = (TextValueExtension) target.getValueExtension();
        if (textValueExtension == null) {
            textValueExtension = new TextValueExtension();
            target.setValueExtension(textValueExtension);
        }
        textValueExtension.setColor(color);
        return target;
    }

    public int getParameterCount() {
        return 2;
    }
}
