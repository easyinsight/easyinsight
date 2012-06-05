package com.easyinsight.core;

import javax.persistence.*;

/**
 * User: James Boe
 * Date: Jun 12, 2008
 * Time: 11:14:16 AM
 */
@Entity
@Table(name="named_item_key")
@PrimaryKeyJoinColumn(name="item_key_id")
public class NamedKey extends Key {
    @Column(name="name")
    private String name;

    @Column(name="indexed")
    private boolean indexed;

    public NamedKey() {
    }

    public NamedKey(String name) {
        this.name = name;
    }

    public String urlKeyString(XMLMetadata xmlMetadata) {
        return name;
    }

    @Override
    public boolean matchesOrContains(Key key) {
        return name.equals(key.toKeyString());
    }

    public boolean hasDataSource(long dataSourceID) {
        return false;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Transient
    private transient String pkName;

    public void setPkName(String pkName) {
        this.pkName = pkName;
    }

    @Override
    public String toSQL() {
        if (pkName != null) {
            return pkName;
        }
        return "k" + getKeyID();
    }

    @Override
    public Key toBaseKey() {
        return this;
    }

    public String toDisplayName() {
        return name;
    }

    @Override
    public boolean indexed() {
        return indexed;
    }

    public String toKeyString() {
        return name;
    }

    @Override
    public String internalString() {
        return name;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NamedKey)) return false;

        NamedKey namedKey = (NamedKey) o;

        return name.equals(namedKey.name);

    }

    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
