package com.easyinsight.analysis;

import javax.persistence.*;
import java.util.List;

/**
 * User: James Boe
 * Date: Mar 31, 2009
 * Time: 10:06:02 AM
 */
@Entity
@Table(name="treemap_report")
public class TreeMapDefinitionState extends AnalysisDefinitionState {

    @Column(name="color_scheme")
    private int colorScheme;

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="treemap_report_id")
    private long definitionID;

    public long getDefinitionID() {
        return definitionID;
    }

    public void setDefinitionID(long definitionID) {
        this.definitionID = definitionID;
    }

    public int getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(int colorScheme) {
        this.colorScheme = colorScheme;
    }

    @Override
    public AnalysisDefinitionState clone(List<AnalysisItem> allFields) throws CloneNotSupportedException {
        TreeMapDefinitionState treeMap = (TreeMapDefinitionState) super.clone(allFields);
        treeMap.setDefinitionID(0);
        return treeMap;
    }

    public WSAnalysisDefinition createWSDefinition() {
        WSTreeMapDefinition wsTreeMapDefinition = new WSTreeMapDefinition();
        wsTreeMapDefinition.setTreeMapDefinitionID(definitionID);
        return wsTreeMapDefinition;
    }
}