package com.easyinsight.analysis;

import com.easyinsight.core.*;

import javax.persistence.*;
import java.util.List;
import java.util.Collection;

import com.easyinsight.database.Database;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Nodes;
import org.hibernate.Session;

/**
 * User: James Boe
 * Date: Jan 20, 2008
 * Time: 11:08:54 PM
 */
@Entity
@Table(name="analysis_dimension")
@PrimaryKeyJoinColumn(name="analysis_item_id")
public class AnalysisDimension extends AnalysisItem {
    @Column(name="group_by")
    private boolean group = true;

    @OneToOne(cascade=CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name="key_dimension_id")
    private AnalysisDimension keyDimension;

    @Column(name="summary")
    private boolean summary = false;

    @Override
    public Element toXML(XMLMetadata xmlMetadata) {
        Element element = super.toXML(xmlMetadata);
        element.addAttribute(new Attribute("groupBy", String.valueOf(group)));
        element.addAttribute(new Attribute("summary", String.valueOf(summary)));
        if (keyDimension != null) {
            Element keyDimensionElement = new Element("keyDimension");
            keyDimensionElement.appendChild(keyDimension.toXML(xmlMetadata));
        }
        return element;
    }

    @Override
    protected void subclassFromXML(Element fieldNode, XMLImportMetadata xmlImportMetadata) {
        super.subclassFromXML(fieldNode, xmlImportMetadata);
        setGroup(Boolean.parseBoolean(fieldNode.getAttribute("groupBy").getValue()));
        setSummary(Boolean.parseBoolean(fieldNode.getAttribute("summary").getValue()));
        Nodes keys = fieldNode.query("keyDimension");
        if (keys.size() == 1) {
            setKeyDimension((AnalysisDimension) AnalysisItem.fromXML((Element) keys.get(0), xmlImportMetadata));
        }
    }

    public AnalysisDimension() {
    }

    

    public AnalysisDimension getKeyDimension() {
        return keyDimension;
    }

    public AnalysisDimension(Key key) {
        super(key);
    }

    public static AnalysisDimension withFolder(Key key, String folder) {
        AnalysisDimension dim = new AnalysisDimension(key);
        dim.setFolder(folder);
        return dim;
    }

    public void setKeyDimension(AnalysisDimension keyDimension) {
        this.keyDimension = keyDimension;
    }

    public boolean isSummary() {
        return summary;
    }

    public void setSummary(boolean summary) {
        this.summary = summary;
    }

    public List<AnalysisItem> getAnalysisItems(List<AnalysisItem> allItems, Collection<AnalysisItem> insightItems, boolean getEverything, boolean includeFilters, Collection<AnalysisItem> analysisItemSet, AnalysisItemRetrievalStructure structure) {
        List<AnalysisItem> analysisItems = super.getAnalysisItems(allItems, insightItems, getEverything, includeFilters, analysisItemSet, structure);
        if (keyDimension != null) {
            analysisItems.add(keyDimension);
        }
        return analysisItems;
    }

    public int getType() {
        return AnalysisItemTypes.DIMENSION;
    }

    @Override
    public int actualType() {
        return AnalysisItemTypes.DIMENSION;
    }

    public AnalysisItemResultMetadata createResultMetadata() {
        AnalysisDimensionResultMetadata metadata = new AnalysisDimensionResultMetadata();
        metadata.addValue(this, new EmptyValue(), null);
        return metadata;
    }

    public AnalysisDimension(Key key, boolean group) {
        super(key);
        this.group = group;
    }

    public AnalysisDimension(Key key, String displayName) {
        super(key, displayName);
    }

    public AnalysisDimension(String key, boolean group) {
        super(key);
        this.group = group;
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    @Override
    public void afterLoad(boolean optimized) {
        super.afterLoad(optimized);
        if (keyDimension != null) {
            setKeyDimension((AnalysisDimension) Database.deproxy(getKeyDimension()));
            keyDimension.afterLoad();
        }
    }

    public Value polishValue(Value value) {
        if (value.type() == Value.STRING) {
            StringValue stringValue = (StringValue) value;
            try {
                value = new NumericValue(Integer.parseInt(stringValue.toString()));
            } catch (NumberFormatException e) {
            }
        }
        return value;
    }

    public void resetIDs() {
        super.resetIDs();
    }

    @Override
    public void updateIDs(ReplacementMap replacementMap) {
        super.updateIDs(replacementMap);
        if (getKeyDimension() != null) {
            setKeyDimension((AnalysisDimension) replacementMap.getField(getKeyDimension()));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AnalysisDimension that = (AnalysisDimension) o;

        return group == that.group;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (group ? 1 : 0);
        result = 31 * result + (keyDimension != null ? keyDimension.hashCode() : 0);
        return result;
    }

    public void reportSave(Session session) {
        super.reportSave(session);
        if (keyDimension != null && keyDimension.getAnalysisItemID() == 0) {
            keyDimension.reportSave(session);
            session.save(keyDimension);
        }
    }
}
