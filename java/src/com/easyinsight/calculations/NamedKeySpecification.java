package com.easyinsight.calculations;

import com.easyinsight.analysis.AnalysisItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * User: jamesboe
 * Date: Feb 8, 2010
 * Time: 11:05:02 AM
 */
public class NamedKeySpecification implements KeySpecification {
    private String key;

    public NamedKeySpecification(String key) {
        this.key = key;
    }

    @Nullable
    public AnalysisItem findAnalysisItem(List<AnalysisItem> currentItems) {
        for (AnalysisItem item : currentItems) {
            if (item.getKey().toKeyString().equals(key)) {
                return item;
            }
        }
        return null;
    }
}
