package com.easyinsight.core;

import javax.persistence.*;
import java.io.Serializable;

/**
 * User: James Boe
 * Date: Jun 12, 2008
 * Time: 10:16:45 AM
 */
@Entity
@Table(name="item_key")
@Inheritance(strategy= InheritanceType.JOINED)
public class Key implements Comparable<Key>, Serializable, Cloneable {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="item_key_id")
    private long keyID;

    public String toSQL() { throw new UnsupportedOperationException(); }

    public Key toBaseKey() { throw new UnsupportedOperationException(); }

    public boolean hasDataSource(long dataSourceID) {
        return false;
    }

    public long getKeyID() {
        return keyID;
    }

    public void setKeyID(long keyID) {
        this.keyID = keyID;
    }

    public String toDisplayName() { throw new UnsupportedOperationException(); }

    public boolean indexed() { throw new UnsupportedOperationException(); }

    public String toKeyString() { throw new UnsupportedOperationException(); }

    public String internalString() { throw new UnsupportedOperationException(); }

    public int compareTo(Key o) {
        return toDisplayName().compareTo(o.toDisplayName());
    }
    
    public Key clone() throws CloneNotSupportedException {
        Key key = (Key) super.clone();
        key.keyID = 0;
        return key;
    }
}
