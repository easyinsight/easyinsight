package com.easyinsight.reportpackage;

import com.easyinsight.core.EIDescriptor;

/**
 * User: jamesboe
 * Date: Dec 9, 2009
 * Time: 10:41:48 AM
 */
public class ReportPackageDescriptor extends EIDescriptor {

    public ReportPackageDescriptor(String name, long id) {
        super(name, id);
    }

    public ReportPackageDescriptor() {
    }

    @Override
    public int getType() {
        return EIDescriptor.PACKAGE;
    }
}
