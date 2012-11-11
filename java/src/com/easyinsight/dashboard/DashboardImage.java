package com.easyinsight.dashboard;

import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.database.EIConnection;
import com.easyinsight.preferences.ImageDescriptor;
import com.easyinsight.scorecard.Scorecard;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: jamesboe
 * Date: Nov 26, 2010
 * Time: 1:25:20 PM
 */
public class DashboardImage extends DashboardElement {
    private ImageDescriptor imageDescriptor;

    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    public void setImageDescriptor(ImageDescriptor imageDescriptor) {
        this.imageDescriptor = imageDescriptor;
    }

    @Override
    public int getType() {
        return DashboardElement.IMAGE;
    }

    @Override
    public long save(EIConnection conn) throws SQLException {
        long id = super.save(conn);
        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO DASHBOARD_IMAGE (DASHBOARD_ELEMENT_ID, user_image_id) " +
                "VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
        insertStmt.setLong(1, getElementID());
        if (imageDescriptor == null) {
            insertStmt.setNull(2, Types.BIGINT);
        } else {
            insertStmt.setLong(2, imageDescriptor.getId());
        }
        insertStmt.execute();
        return id;
    }

    @Override
    public Set<Long> containedReports() {
        return new HashSet<Long>();
    }

    @Override
    public Set<Long> containedScorecards() {
        return new HashSet<Long>();
    }

    @Override
    public void updateScorecardIDs(Map<Long, Scorecard> scorecardReplacementMap) {
    }

    @Override
    public void visit(IDashboardVisitor dashboardVisitor) {
        dashboardVisitor.accept(this);
    }

    @Override
    public void updateReportIDs(Map<Long, AnalysisDefinition> reportReplacementMap) {

    }

    public static DashboardElement loadImage(long elementID, EIConnection conn) throws SQLException {
        DashboardImage dashboardImage;
        PreparedStatement queryStmt = conn.prepareStatement("SELECT DASHBOARD_IMAGE.user_image_id, USER_IMAGE.image_name from dashboard_image, user_image " +
                "where dashboard_element_id = ? and dashboard_image.user_image_id = user_image.user_image_id");
        queryStmt.setLong(1, elementID);
        ResultSet rs = queryStmt.executeQuery();
        if (rs.next()) {
            dashboardImage = new DashboardImage();
            ImageDescriptor imageDescriptor = new ImageDescriptor();
            imageDescriptor.setId(rs.getLong(1));
            imageDescriptor.setName(rs.getString(2));
            dashboardImage.setImageDescriptor(imageDescriptor);
        } else {
            dashboardImage = new DashboardImage();
        }
        return dashboardImage;
    }
}
