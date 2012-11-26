package com.easyinsight.dashboard;

import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.FilterDefinition;
import com.easyinsight.analysis.FilterHTMLMetadata;
import com.easyinsight.core.EIDescriptor;
import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.scorecard.Scorecard;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * User: jamesboe
 * Date: Nov 26, 2010
 * Time: 1:25:12 PM
 */
public class DashboardGrid extends DashboardElement {
    private int rows;
    private int columns;
    private int width;
    private int backgroundColor;
    private double backgroundAlpha;

    private List<DashboardGridItem> gridItems;

    @Override
    public DashboardElement clone() throws CloneNotSupportedException {
        DashboardGrid dashboardGrid = (DashboardGrid) super.clone();
        List<DashboardGridItem> cloneItems = new ArrayList<DashboardGridItem>();
        for (DashboardGridItem gridItem : gridItems) {
            cloneItems.add(gridItem.clone());
        }
        dashboardGrid.setGridItems(cloneItems);
        return dashboardGrid;
    }

    public List<DashboardGridItem> getGridItems() {
        return gridItems;
    }

    public void setGridItems(List<DashboardGridItem> gridItems) {
        this.gridItems = gridItems;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    @Override
    public int getType() {
        return DashboardElement.GRID;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public double getBackgroundAlpha() {
        return backgroundAlpha;
    }

    public void setBackgroundAlpha(double backgroundAlpha) {
        this.backgroundAlpha = backgroundAlpha;
    }

    @Override
    public long save(EIConnection conn) throws SQLException {
        long id = super.save(conn);
        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO DASHBOARD_GRID (DASHBOARD_ELEMENT_ID, NUMBER_ROWS, NUMBER_COLUMNS, WIDTH, BACKGROUND_COLOR," +
                "background_alpha) VALUES (?, ?, ?, ?, ?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS);
        insertStmt.setLong(1, getElementID());
        insertStmt.setInt(2, rows);
        insertStmt.setInt(3, columns);
        insertStmt.setInt(4, width);
        insertStmt.setInt(5, backgroundColor);
        insertStmt.setDouble(6, backgroundAlpha);
        insertStmt.execute();
        long gridID = Database.instance().getAutoGenKey(insertStmt);
        insertStmt.close();
        for (DashboardGridItem gridItem : gridItems) {
            gridItem.save(conn, gridID);
        }
        return id;
    }

    public static DashboardElement loadGrid(long elementID, EIConnection conn) throws SQLException {
        DashboardGrid dashboardGrid = null;
        PreparedStatement queryStmt = conn.prepareStatement("SELECT NUMBER_ROWS, NUMBER_COLUMNS, DASHBOARD_GRID_ID, WIDTH, BACKGROUND_COLOR, BACKGROUND_ALPHA " +
                "FROM DASHBOARD_GRID  WHERE DASHBOARD_GRID.DASHBOARD_ELEMENT_ID = ?");
        queryStmt.setLong(1, elementID);
        ResultSet rs = queryStmt.executeQuery();
        if (rs.next()) {
            dashboardGrid = new DashboardGrid();
            dashboardGrid.setRows(rs.getInt(1));
            dashboardGrid.setColumns(rs.getInt(2));
            long gridID = rs.getLong(3);
            dashboardGrid.setWidth(rs.getInt(4));
            dashboardGrid.setBackgroundColor(rs.getInt(5));
            dashboardGrid.setBackgroundAlpha(rs.getDouble(6));
            dashboardGrid.loadElement(elementID, conn);
            PreparedStatement gridItemStmt = conn.prepareStatement("SELECT DASHBOARD_ELEMENT.DASHBOARD_ELEMENT_ID, DASHBOARD_ELEMENT.element_type, " +
                    "ROW_POSITION, COLUMN_POSITION FROM DASHBOARD_GRID_ITEM, DASHBOARD_ELEMENT WHERE DASHBOARD_GRID_ID = ? AND DASHBOARD_GRID_ITEM.dashboard_element_id = dashboard_element.dashboard_element_id");
            gridItemStmt.setLong(1, gridID);
            ResultSet itemRS = gridItemStmt.executeQuery();
            List<DashboardGridItem> items = new ArrayList<DashboardGridItem>();
            while (itemRS.next()) {
                long gridElementID = itemRS.getLong(1);
                int elementType = itemRS.getInt(2);
                DashboardGridItem item = new DashboardGridItem();
                item.setRowIndex(itemRS.getInt(3));
                item.setColumnIndex(itemRS.getInt(4));
                DashboardElement element = DashboardStorage.getElement(conn, gridElementID, elementType);
                item.setDashboardElement(element);
                if (element != null) {
                    element.setParentElement(dashboardGrid);
                }
                items.add(item);
            }
            dashboardGrid.setGridItems(items);
            gridItemStmt.close();
        }
        queryStmt.close();
        return dashboardGrid;
    }

    @Override
    public Set<Long> containedScorecards() {
        Set<Long> reports = new HashSet<Long>();
        for (DashboardGridItem gridItem : gridItems) {
            DashboardElement element = gridItem.getDashboardElement();
            if (element != null) {
                reports.addAll(element.containedScorecards());
            }
        }
        return reports;
    }

    @Override
    public void updateScorecardIDs(Map<Long, Scorecard> scorecardReplacementMap) {
        for (DashboardGridItem gridItem : gridItems) {
            DashboardElement element = gridItem.getDashboardElement();
            if (element != null) {
                element.updateScorecardIDs(scorecardReplacementMap);
            }
        }
    }

    @Override
    public Set<Long> containedReports() {
        Set<Long> reports = new HashSet<Long>();
        for (DashboardGridItem gridItem : gridItems) {
            DashboardElement element = gridItem.getDashboardElement();
            if (element != null) {
                reports.addAll(element.containedReports());
            }
        }
        return reports;
    }

    @Override
    public void visit(IDashboardVisitor dashboardVisitor) {
        dashboardVisitor.accept(this);
        for (DashboardGridItem gridItem : gridItems) {
            if (gridItem == null || gridItem.getDashboardElement() == null) {
                continue;
            }
            DashboardElement element = gridItem.getDashboardElement();
            if (element != null) {
                element.visit(dashboardVisitor);
            }
        }
    }

    @Override
    public void updateReportIDs(Map<Long, AnalysisDefinition> reportReplacementMap) {
        for (DashboardGridItem gridItem : gridItems) {
            DashboardElement element = gridItem.getDashboardElement();
            if (element != null) {
                element.updateReportIDs(reportReplacementMap);
            }
        }
    }

    @Override
    public List<EIDescriptor> allItems(List<AnalysisItem> dataSourceItems) {
        List<EIDescriptor> descs = new ArrayList<EIDescriptor>();
        for (DashboardGridItem gridItem : gridItems) {
            DashboardElement element = gridItem.getDashboardElement();
            if (element != null) {
                descs.addAll(element.allItems(dataSourceItems));
            }
        }
        return descs;
    }

    @Override
    public Collection<? extends FilterDefinition> filtersForReport(long reportID) {
        for (DashboardGridItem gridItem : getGridItems()) {
            DashboardElement element = gridItem.getDashboardElement();
            if (element != null) {
                Collection<? extends FilterDefinition> filters = element.filtersForReport(reportID);
                if (filters != null && !filters.isEmpty()) {
                    return filters;
                }
            }

        }
        return new ArrayList<FilterDefinition>();
    }

    @Override
    public String refreshFunction() {
        return "updateGrid" + getElementID() + "()";
    }

    public String toHTML(FilterHTMLMetadata filterHTMLMetadata) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n<script type=\"text/javascript\">\n");
        sb.append("function updateGrid").append(getElementID()).append("() {\n");
        for (DashboardGridItem e : gridItems) {
            DashboardElement element = e.getDashboardElement();
            if (element != null) {
                sb.append("\t").append(element.refreshFunction()).append(";\n");
            }
        }
        sb.append("}\n");
        sb.append("</script>\n");
        sb.append("<div class=\"container-fluid\">\n");
        for (int i = 0; i < rows; i++) {
            if (i < (rows - 1)) {
                sb.append("<div class=\"row-fluid\" style=\"margin-bottom:20px\">\n");
            } else {
                sb.append("<div class=\"row-fluid\">\n");
            }

            //sb.append("<tr style=\"width:100%\">\r\n");
            for (int j = 0; j < columns; j++) {
                int span;
                if (columns == 1) {
                    span = 12;
                } else if (columns == 2) {
                    span = 6;
                } else if (columns == 3) {
                    span = 4;
                } else if (columns == 4) {
                    span = 3;
                } else {
                    span = 2;
                }
                sb.append("<div class=\"span").append(span).append("\" style=\"background-color:#FFFFFF\">");
                //sb.append("<td style=\"width:"+(100 / columns)+"%\">\r\n");
                DashboardGridItem item = findItem(i, j);
                DashboardElement element = item.getDashboardElement();
                if (element != null) {
                    sb.append(element.toHTML(filterHTMLMetadata));
                }
                sb.append("</div>\r\n");
            }
            sb.append("</div>\r\n");
        }
        sb.append("</div>\n");
        return sb.toString();
    }

    private DashboardGridItem findItem(int x, int y) {
        for (DashboardGridItem e : gridItems) {
            if (e.getRowIndex() == x && e.getColumnIndex() == y) {
                return e;
            }
        }
        return null;
    }

    public List<String> jsIncludes() {
        List<String> includes = super.jsIncludes();
        for (DashboardGridItem stackItem : getGridItems()) {
            DashboardElement element = stackItem.getDashboardElement();
            if (element != null) {
                includes.addAll(element.jsIncludes());
            }
        }
        return includes;
    }

    public List<String> cssIncludes() {
        List<String> includes = super.cssIncludes();
        for (DashboardGridItem stackItem : getGridItems()) {
            DashboardElement element = stackItem.getDashboardElement();
            if (element != null) {
                includes.addAll(element.cssIncludes());
            }
        }
        return includes;
    }

    public Collection<? extends FilterDefinition> filtersToRender() {
        List<FilterDefinition> includes = new ArrayList<FilterDefinition>();
        for (DashboardGridItem stackItem : getGridItems()) {
            DashboardElement element = stackItem.getDashboardElement();
            if (element != null) {
                includes.addAll(element.filtersToRender());
            }
        }
        return includes;
    }

    public DashboardElement findElement(long dashboardElementID) {
        DashboardElement element = super.findElement(dashboardElementID);
        if (element != null) {
            return element;
        }
        for (DashboardGridItem stackItem : getGridItems()) {
            DashboardElement stackElement = stackItem.getDashboardElement();
            if (stackElement != null) {
                element = stackElement.findElement(dashboardElementID);
                if (element != null) {
                    return element;
                }
            }
        }
        return null;
    }

    public DashboardUIProperties findHeaderImage() {
        if (getHeaderBackground() != null) {
            return new DashboardUIProperties(getHeaderBackgroundColor(), getHeaderBackground());
        }
        for (DashboardGridItem stackItem : getGridItems()) {
            DashboardElement element = stackItem.getDashboardElement();
            if (element != null) {
                DashboardUIProperties imageDescriptor = element.findHeaderImage();
                if (imageDescriptor != null) {
                    return imageDescriptor;
                }
            }
        }
        return null;
    }

    public int requiredInitCount() {
        int count = 0;
        for (DashboardGridItem stackItem : getGridItems()) {
            DashboardElement element = stackItem.getDashboardElement();
            if (element != null) {
                count += element.requiredInitCount();
            }
        }
        return count;
    }
}
