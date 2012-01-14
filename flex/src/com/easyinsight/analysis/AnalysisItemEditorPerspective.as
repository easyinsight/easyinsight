package com.easyinsight.analysis {
import com.easyinsight.framework.ModulePerspective;
import com.easyinsight.framework.PerspectiveInfo;

import mx.collections.ArrayCollection;

public class AnalysisItemEditorPerspective extends ModulePerspective implements IAnalysisItemEditor {
    public function AnalysisItemEditorPerspective(perspectiveInfo:PerspectiveInfo) {
        super(perspectiveInfo);
    }

    public function set analysisItem(analysisItem:AnalysisItem):void {
    }

    public function set analysisItems(analysisItems:ArrayCollection):void {
    }

    public function set dataSourceID(dataSourceID:int):void {
    }

    public function save(dataSourceID:int):void {
        var editor:IAnalysisItemEditor = getChildAt(0) as IAnalysisItemEditor;
        editor.save(dataSourceID);
    }

    public function validate():Boolean {
        return true;
    }

    public function higlight():void {
    }

    public function normal():void {
    }
}
}