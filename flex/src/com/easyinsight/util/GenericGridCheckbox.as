/**
 * Created with IntelliJ IDEA.
 * User: jamesboe
 * Date: 11/13/12
 * Time: 1:56 PM
 * To change this template use File | Settings | File Templates.
 */
package com.easyinsight.util {

public class GenericGridCheckbox extends GridCheckbox {

    override protected function updateSelectedOnObject(selected:Boolean):void {
        data["selected"] = selected;
    }

    override protected function isSelected():Boolean {
        return data["selected"];
    }
}
}
