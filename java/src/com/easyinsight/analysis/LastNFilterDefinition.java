package com.easyinsight.analysis;

import com.easyinsight.core.XMLMetadata;
import com.easyinsight.database.Database;
import nu.xom.Element;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * User: James Boe
 * Date: Feb 27, 2009
 * Time: 8:30:42 AM
 */
public class LastNFilterDefinition extends FilterDefinition {

    private int limit;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public MaterializedFilterDefinition materialize(InsightRequestMetadata insightRequestMetadata) {
        return new MaterializedLastNFilterDefinition(getField(), limit);
    }

    public String toQuerySQL(String tableName, Database database, InsightRequestMetadata insightRequestMetadata) {
        // TODO: implement
        StringBuilder queryBuilder = new StringBuilder();
        return queryBuilder.toString();
    }

    public int populatePreparedStatement(PreparedStatement preparedStatement, int start, int type, InsightRequestMetadata insightRequestMetadata) throws SQLException {
        // TODO: implement
        return start;
    }

    @Override
    public Element toXML(XMLMetadata xmlMetadata) {
        Element element = super.toXML(xmlMetadata);
        return element;
    }
}
