package com.easyinsight.preferences;

import com.easyinsight.analysis.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: jamesboe
 * Date: Nov 17, 2010
 * Time: 10:26:46 PM
 */
public class ApplicationSkin implements Serializable {
    
    public static final int APPLICATION = 1;
    public static final int ACCOUNT = 2;
    public static final int USER = 3;

    private ImageDescriptor coreAppBackgroundImage;
    private boolean coreAppBackgroundImageEnabled;
    private int coreAppBackgroundColor;
    private boolean coreAppBackgroundColorEnabled;
    private String coreAppBackgroundSize;
    private boolean coreAppBackgroundSizeEnabled;
    private int headerBarBackgroundColor;
    private boolean headerBarBackgroundColorEnabled;
    private ImageDescriptor headerBarLogo;
    private ImageDescriptor reportHeaderImage;
    private int headerBarDividerColor;
    private boolean headerBarDividerColorEnabled;
    private int centerCanvasBackgroundColor;
    private boolean centerCanvasBackgroundColorEnabled;
    private double centerCanvasBackgroundAlpha;
    private boolean centerCanvasBackgroundAlphaEnabled;
    private boolean reportBackgroundEnabled;
    private boolean reportBackgroundSizeEnabled;
    private int reportBackgroundColor;
    private int reportTextColor;
    private boolean reportHeader;
    private boolean myDataName;
    private boolean myDataSize;
    private boolean myDataOwner;
    private boolean myDataCreationDate;
    private boolean myDataLastTime;
    private boolean myDataCombine;
    private boolean myDataNewScorecard;
    private boolean myDataNewDashboard;
    private boolean myDataLookupTable;
    private boolean myDataAccountVisible;

    private boolean dataSourceTags = false;
    private boolean dataSourceAPIKey = false;
    private boolean reportTags = false;
    private boolean reportType = false;
    private boolean reportAPIKey = false;
    private boolean reportModified = false;
    private boolean reportCached = false;
    private boolean reportPersistedCached = false;

    private int customChartColor;
    private boolean customChartColorEnabled;
    private int gradientChartColor;
    private boolean gradientChartColorEnabled;
    private int secondaryColor;
    private boolean secondaryColorEnabled;
    private int tertiaryColor;
    private boolean tertiaryColorEnabled;
    private int summaryBackgroundColor;
    private boolean summaryBackgroundColorEnabled;
    private int summaryTextColor;
    private boolean summaryTextColorEnabled;

    private int crosstabHeaderBackgroundColor;
    private boolean crosstabHeaderBackgroundColorEnabled;
    private int crosstabHeaderTextColor;
    private boolean crosstabHeaderTextColorEnabled;

    private int headerStart;
    private boolean headerStartEnabled;
    private int headerEnd;
    private boolean headerEndEnabled;
    private int reportHeaderTextColor;
    private boolean reportHeaderTextColorEnabled;
    private int tableColorStart;
    private boolean tableColorStartEnabled;
    private int tableColorEnd;
    private boolean tableColorEndEnabled;
    private int textColor;
    private boolean textColorEnabled;

    private int dashboardStack1ColorStart;
    private int dashboardStack1ColorEnd;
    private int dashboardStackColor2Start;
    private int dashboardStackColor2End;
    private List<MultiColor> multiColors;
    private List<MultiColor> secondaryMultiColors;

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getReportHeaderTextColor() {
        return reportHeaderTextColor;
    }

    public void setReportHeaderTextColor(int reportHeaderTextColor) {
        this.reportHeaderTextColor = reportHeaderTextColor;
    }

    public boolean isReportHeaderTextColorEnabled() {
        return reportHeaderTextColorEnabled;
    }

    public void setReportHeaderTextColorEnabled(boolean reportHeaderTextColorEnabled) {
        this.reportHeaderTextColorEnabled = reportHeaderTextColorEnabled;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public boolean isTextColorEnabled() {
        return textColorEnabled;
    }

    public void setTextColorEnabled(boolean textColorEnabled) {
        this.textColorEnabled = textColorEnabled;
    }

    public List<MultiColor> getSecondaryMultiColors() {
        return secondaryMultiColors;
    }

    public void setSecondaryMultiColors(List<MultiColor> secondaryMultiColors) {
        this.secondaryMultiColors = secondaryMultiColors;
    }

    public boolean isCrosstabHeaderBackgroundColorEnabled() {
        return crosstabHeaderBackgroundColorEnabled;
    }

    public void setCrosstabHeaderBackgroundColorEnabled(boolean crosstabHeaderBackgroundColorEnabled) {
        this.crosstabHeaderBackgroundColorEnabled = crosstabHeaderBackgroundColorEnabled;
    }

    public boolean isCrosstabHeaderTextColorEnabled() {
        return crosstabHeaderTextColorEnabled;
    }

    public void setCrosstabHeaderTextColorEnabled(boolean crosstabHeaderTextColorEnabled) {
        this.crosstabHeaderTextColorEnabled = crosstabHeaderTextColorEnabled;
    }

    public int getTableColorStart() {
        return tableColorStart;
    }

    public void setTableColorStart(int tableColorStart) {
        this.tableColorStart = tableColorStart;
    }

    public boolean isTableColorStartEnabled() {
        return tableColorStartEnabled;
    }

    public void setTableColorStartEnabled(boolean tableColorStartEnabled) {
        this.tableColorStartEnabled = tableColorStartEnabled;
    }

    public int getTableColorEnd() {
        return tableColorEnd;
    }

    public void setTableColorEnd(int tableColorEnd) {
        this.tableColorEnd = tableColorEnd;
    }

    public boolean isTableColorEndEnabled() {
        return tableColorEndEnabled;
    }

    public void setTableColorEndEnabled(boolean tableColorEndEnabled) {
        this.tableColorEndEnabled = tableColorEndEnabled;
    }

    public int getHeaderStart() {
        return headerStart;
    }

    public void setHeaderStart(int headerStart) {
        this.headerStart = headerStart;
    }

    public boolean isHeaderStartEnabled() {
        return headerStartEnabled;
    }

    public void setHeaderStartEnabled(boolean headerStartEnabled) {
        this.headerStartEnabled = headerStartEnabled;
    }

    public int getHeaderEnd() {
        return headerEnd;
    }

    public void setHeaderEnd(int headerEnd) {
        this.headerEnd = headerEnd;
    }

    public boolean isHeaderEndEnabled() {
        return headerEndEnabled;
    }

    public void setHeaderEndEnabled(boolean headerEndEnabled) {
        this.headerEndEnabled = headerEndEnabled;
    }

    public ApplicationSkinSettings toSettings(int mode) {
        ApplicationSkinSettings settings = new ApplicationSkinSettings();
        List<ReportProperty> properties = new ArrayList<ReportProperty>();
        if (mode == APPLICATION || mode == ACCOUNT) {
            if (coreAppBackgroundImage != null) {
                properties.add(new ReportImageProperty("coreAppBackgroundImage", coreAppBackgroundImage, coreAppBackgroundImageEnabled));
            }
            if (headerBarLogo != null) {
                properties.add(new ReportImageProperty("headerBarLogo", headerBarLogo, true));
            }
            if (reportHeaderImage != null) {
                properties.add(new ReportImageProperty("reportHeaderImage", reportHeaderImage, true));
            }
            properties.add(new ReportNumericProperty("coreAppBackgroundColor", coreAppBackgroundColor, coreAppBackgroundColorEnabled));
            properties.add(new ReportNumericProperty("headerBarBackgroundColor", headerBarBackgroundColor, headerBarBackgroundColorEnabled));
            properties.add(new ReportNumericProperty("headerBarDividerColor", headerBarDividerColor, headerBarDividerColorEnabled));
            properties.add(new ReportNumericProperty("centerCanvasBackgroundColor", centerCanvasBackgroundColor, centerCanvasBackgroundColorEnabled));
            properties.add(new ReportNumericProperty("centerCanvasBackgroundAlpha", centerCanvasBackgroundAlpha, centerCanvasBackgroundAlphaEnabled));
            properties.add(new ReportStringProperty("coreAppBackgroundSize", coreAppBackgroundSize, coreAppBackgroundSizeEnabled));
            properties.add(new ReportNumericProperty("reportBackgroundColor", reportBackgroundColor, true));
            properties.add(new ReportNumericProperty("reportTextColor", reportTextColor, true));
            properties.add(new ReportBooleanProperty("reportHeader", reportHeader, true));

            properties.add(new ReportNumericProperty("customChartColor", customChartColor, customChartColorEnabled));
            properties.add(new ReportNumericProperty("gradientChartColor", gradientChartColor, gradientChartColorEnabled));
            properties.add(new ReportNumericProperty("secondaryColor", secondaryColor, secondaryColorEnabled));
            properties.add(new ReportNumericProperty("tertiaryColor", secondaryColor, tertiaryColorEnabled));
            properties.add(new ReportNumericProperty("summaryBackgroundColor", summaryBackgroundColor, summaryBackgroundColorEnabled));
            properties.add(new ReportNumericProperty("summaryTextColor", summaryTextColor, summaryTextColorEnabled));
            properties.add(new ReportNumericProperty("crosstabHeaderBackgroundColor", crosstabHeaderBackgroundColor, crosstabHeaderBackgroundColorEnabled));
            properties.add(new ReportNumericProperty("crosstabHeaderTextColor", crosstabHeaderTextColor, crosstabHeaderTextColorEnabled));
            properties.add(new ReportNumericProperty("headerStart", headerStart, headerStartEnabled));
            properties.add(new ReportNumericProperty("headerEnd", headerEnd, headerEndEnabled));
            properties.add(new ReportNumericProperty("reportHeaderTextColor", reportHeaderTextColor, reportHeaderTextColorEnabled));

            properties.add(new ReportNumericProperty("tableColorStart", tableColorStart, tableColorStartEnabled));
            properties.add(new ReportNumericProperty("tableColorEnd", tableColorEnd, tableColorEndEnabled));
            properties.add(new ReportNumericProperty("textColor", textColor, textColorEnabled));

            properties.add(new ReportNumericProperty("dashboardStack1ColorStart", dashboardStack1ColorStart));
            properties.add(new ReportNumericProperty("dashboardStack1ColorEnd", dashboardStack1ColorEnd));
            properties.add(new ReportNumericProperty("dashboardStack2ColorStart", dashboardStackColor2Start));
            properties.add(new ReportNumericProperty("dashboardStack2ColorEnd", dashboardStackColor2End));
            properties.add(ReportMultiColorProperty.fromColors(multiColors, "multiColors"));
            properties.add(ReportMultiColorProperty.fromColors(secondaryMultiColors, "secondaryMultiColors"));
        }
        if (mode == APPLICATION || mode == USER) {
            properties.add(new ReportBooleanProperty("myDataName", myDataName));
            properties.add(new ReportBooleanProperty("myDataSize", myDataSize));
            properties.add(new ReportBooleanProperty("myDataOwner", myDataOwner));
            properties.add(new ReportBooleanProperty("myDataCreationDate", myDataCreationDate));
            properties.add(new ReportBooleanProperty("myDataLastTime", myDataLastTime));
            properties.add(new ReportBooleanProperty("myDataCombine", myDataCombine));
            properties.add(new ReportBooleanProperty("myDataScorecard", myDataNewScorecard));
            properties.add(new ReportBooleanProperty("myDataDashboard", myDataNewDashboard));
            properties.add(new ReportBooleanProperty("myDataLookupTable", myDataLookupTable));
            properties.add(new ReportBooleanProperty("myDataAccountVisible", myDataAccountVisible));

            properties.add(new ReportBooleanProperty("dataSourceTags", dataSourceTags));
            properties.add(new ReportBooleanProperty("dataSourceAPIKey", dataSourceAPIKey));
            properties.add(new ReportBooleanProperty("reportTags", reportTags));
            properties.add(new ReportBooleanProperty("reportType", reportType));
            properties.add(new ReportBooleanProperty("reportAPIKey", reportAPIKey));
            properties.add(new ReportBooleanProperty("reportModified", reportModified));
            properties.add(new ReportBooleanProperty("reportCached", reportCached));
            properties.add(new ReportBooleanProperty("reportPersistedCached", reportPersistedCached));
            /*
            dataSourceTags = findBooleanProperty(properties, "dataSourceTags", false);
        dataSourceAPIKey = findBooleanProperty(properties, "dataSourceAPIKey", false);
        reportTags = findBooleanProperty(properties, "reportTags", false);
        reportType = findBooleanProperty(properties, "reportType", false);
        reportAPIKey = findBooleanProperty(properties, "reportAPIKey", false);
        reportModified = findBooleanProperty(properties, "reportModified", false);
        reportCached = findBooleanProperty(properties, "reportCached", false);
        reportPersistedCached = findBooleanProperty(properties, "reportPersistedCached", false);
             */
        }
        settings.setSkinID(id);
        settings.setProperties(properties);
        return settings;
    }

    public void populateProperties(List<ReportProperty> properties) {
        try {
            coreAppBackgroundImage = findImage(properties, "coreAppBackgroundImage", null);
        } catch (Exception e) {
        }
        try {
            headerBarLogo = findImage(properties, "headerBarLogo", null);
        } catch (Exception e) {
        }
        coreAppBackgroundImageEnabled = propertyEnabled(properties, "coreAppBackgroundImage");
        coreAppBackgroundColor = (int) findNumberProperty(properties, "coreAppBackgroundColor", 0);
        coreAppBackgroundColorEnabled = propertyEnabled(properties, "coreAppBackgroundColor");
        headerBarBackgroundColor = (int) findNumberProperty(properties, "headerBarBackgroundColor", 0);
        headerBarBackgroundColorEnabled = propertyEnabled(properties, "headerBarBackgroundColor");
        centerCanvasBackgroundColor = (int) findNumberProperty(properties, "centerCanvasBackgroundColor", 0);
        centerCanvasBackgroundColorEnabled = propertyEnabled(properties, "centerCanvasBackgroundColor");
        centerCanvasBackgroundAlpha = findNumberProperty(properties, "centerCanvasBackgroundAlpha", 1);
        centerCanvasBackgroundAlphaEnabled = propertyEnabled(properties, "centerCanvasBackgroundAlpha");
        headerBarDividerColor = (int) findNumberProperty(properties, "headerBarDividerColor", 0);
        headerBarDividerColorEnabled = propertyEnabled(properties, "headerBarDividerColor");
        coreAppBackgroundSize = findStringProperty(properties, "coreAppBackgroundSize", "100%");
        coreAppBackgroundSizeEnabled = propertyEnabled(properties, "coreAppBackgroundSize");
        reportBackgroundEnabled = propertyEnabled(properties, "reportBackground");
        reportBackgroundSizeEnabled = propertyEnabled(properties, "reportBackgroundSize");
        reportBackgroundColor = (int) findNumberProperty(properties, "reportBackgroundColor", 0);
        reportTextColor = (int) findNumberProperty(properties, "reportTextColor", 0);
        reportHeader = findBooleanProperty(properties, "reportHeader", false);
        try {
            reportHeaderImage = findImage(properties, "reportHeaderImage", null);
        } catch (Exception e) {
        }
        myDataName = findBooleanProperty(properties, "myDataName", true);
        myDataSize = findBooleanProperty(properties, "myDataSize", false);
        myDataOwner = findBooleanProperty(properties, "myDataOwner", false);
        myDataCreationDate = findBooleanProperty(properties, "myDataCreationDate", false);
        myDataLastTime = findBooleanProperty(properties, "myDataLastTime", false);
        myDataCombine = findBooleanProperty(properties, "myDataCombine", false);
        myDataNewScorecard = findBooleanProperty(properties, "myDataScorecard", false);
        myDataNewDashboard = findBooleanProperty(properties, "myDataDashboard", true);
        myDataLookupTable = findBooleanProperty(properties, "myDataLookupTable", false);
        myDataAccountVisible = findBooleanProperty(properties, "myDataAccountVisible", false);

        customChartColor = (int) findNumberProperty(properties, "customChartColor", 0);
        gradientChartColor = (int) findNumberProperty(properties, "gradientChartColor", 0);
        secondaryColor = (int) findNumberProperty(properties, "secondaryColor", 0);
        tertiaryColor = (int) findNumberProperty(properties, "tertiaryColor", 0);
        summaryBackgroundColor = (int) findNumberProperty(properties, "summaryBackgroundColor", 0);
        summaryTextColor = (int) findNumberProperty(properties, "summaryTextColor", 0);
        customChartColorEnabled = propertyEnabled(properties, "customChartColor");
        gradientChartColorEnabled = propertyEnabled(properties, "gradientChartColor");
        secondaryColorEnabled = propertyEnabled(properties, "secondaryColor");
        tertiaryColorEnabled = propertyEnabled(properties, "tertiaryColor");
        summaryBackgroundColorEnabled = propertyEnabled(properties, "summaryBackgroundColor");
        summaryTextColorEnabled = propertyEnabled(properties, "summaryTextColor");
        crosstabHeaderBackgroundColor = (int) findNumberProperty(properties, "crosstabHeaderBackgroundColor", 0);
        crosstabHeaderBackgroundColorEnabled = propertyEnabled(properties, "crosstabHeaderBackgroundColor");
        crosstabHeaderTextColor = (int) findNumberProperty(properties, "crosstabHeaderTextColor", 0);
        crosstabHeaderTextColorEnabled = propertyEnabled(properties, "crosstabHeaderTextColor");
        headerStart = (int) findNumberProperty(properties, "headerStart", 0);
        headerStartEnabled = propertyEnabled(properties, "headerStart");
        headerEnd = (int) findNumberProperty(properties, "headerEnd", 0);
        headerEndEnabled = propertyEnabled(properties, "headerEnd");
        reportHeaderTextColor = (int) findNumberProperty(properties, "reportHeaderTextColor", 0);
        reportHeaderTextColorEnabled = propertyEnabled(properties, "reportHeaderTextColor");
        tableColorStart = (int) findNumberProperty(properties, "tableColorStart", 0);
        tableColorStartEnabled = propertyEnabled(properties, "tableColorStart");
        tableColorEnd = (int) findNumberProperty(properties, "tableColorEnd", 0);
        tableColorEndEnabled = propertyEnabled(properties, "tableColorEnd");
        textColor = (int) findNumberProperty(properties, "textColor", 0);
        textColorEnabled = propertyEnabled(properties, "textColor");
        dashboardStack1ColorStart = (int) findNumberProperty(properties, "dashboardStack1ColorStart", 0);
        dashboardStack1ColorEnd = (int) findNumberProperty(properties, "dashboardStack1ColorEnd", 0);
        dashboardStackColor2Start = (int) findNumberProperty(properties, "dashboardStack2ColorStart", 0);
        dashboardStackColor2End = (int) findNumberProperty(properties, "dashboardStack2ColorEnd", 0);
        multiColors = multiColorProperty(properties, "multiColors");
        secondaryMultiColors = multiColorProperty(properties, "secondaryMultiColors");
        dataSourceTags = findBooleanProperty(properties, "dataSourceTags", false);
        dataSourceAPIKey = findBooleanProperty(properties, "dataSourceAPIKey", false);
        reportTags = findBooleanProperty(properties, "reportTags", false);
        reportType = findBooleanProperty(properties, "reportType", false);
        reportAPIKey = findBooleanProperty(properties, "reportAPIKey", false);
        reportModified = findBooleanProperty(properties, "reportModified", false);
        reportCached = findBooleanProperty(properties, "reportCached", false);
        reportPersistedCached = findBooleanProperty(properties, "reportPersistedCached", false);
    }

    public boolean isSecondaryColorEnabled() {
        return secondaryColorEnabled;
    }

    public void setSecondaryColorEnabled(boolean secondaryColorEnabled) {
        this.secondaryColorEnabled = secondaryColorEnabled;
    }

    public boolean isTertiaryColorEnabled() {
        return tertiaryColorEnabled;
    }

    public void setTertiaryColorEnabled(boolean tertiaryColorEnabled) {
        this.tertiaryColorEnabled = tertiaryColorEnabled;
    }

    public boolean isSummaryBackgroundColorEnabled() {
        return summaryBackgroundColorEnabled;
    }

    public void setSummaryBackgroundColorEnabled(boolean summaryBackgroundColorEnabled) {
        this.summaryBackgroundColorEnabled = summaryBackgroundColorEnabled;
    }

    public boolean isSummaryTextColorEnabled() {
        return summaryTextColorEnabled;
    }

    public void setSummaryTextColorEnabled(boolean summaryTextColorEnabled) {
        this.summaryTextColorEnabled = summaryTextColorEnabled;
    }

    public int getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(int secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public int getTertiaryColor() {
        return tertiaryColor;
    }

    public void setTertiaryColor(int tertiaryColor) {
        this.tertiaryColor = tertiaryColor;
    }

    public int getSummaryBackgroundColor() {
        return summaryBackgroundColor;
    }

    public void setSummaryBackgroundColor(int summaryBackgroundColor) {
        this.summaryBackgroundColor = summaryBackgroundColor;
    }

    public int getSummaryTextColor() {
        return summaryTextColor;
    }

    public void setSummaryTextColor(int summaryTextColor) {
        this.summaryTextColor = summaryTextColor;
    }

    public int getCrosstabHeaderBackgroundColor() {
        return crosstabHeaderBackgroundColor;
    }

    public void setCrosstabHeaderBackgroundColor(int crosstabHeaderBackgroundColor) {
        this.crosstabHeaderBackgroundColor = crosstabHeaderBackgroundColor;
    }

    public int getCrosstabHeaderTextColor() {
        return crosstabHeaderTextColor;
    }

    public void setCrosstabHeaderTextColor(int crosstabHeaderTextColor) {
        this.crosstabHeaderTextColor = crosstabHeaderTextColor;
    }

    public List<MultiColor> getMultiColors() {
        return multiColors;
    }

    public void setMultiColors(List<MultiColor> multiColors) {
        this.multiColors = multiColors;
    }

    public int getCustomChartColor() {
        return customChartColor;
    }

    public void setCustomChartColor(int customChartColor) {
        this.customChartColor = customChartColor;
    }

    public boolean isCustomChartColorEnabled() {
        return customChartColorEnabled;
    }

    public void setCustomChartColorEnabled(boolean customChartColorEnabled) {
        this.customChartColorEnabled = customChartColorEnabled;
    }

    public int getGradientChartColor() {
        return gradientChartColor;
    }

    public void setGradientChartColor(int gradientChartColor) {
        this.gradientChartColor = gradientChartColor;
    }

    public boolean isGradientChartColorEnabled() {
        return gradientChartColorEnabled;
    }

    public void setGradientChartColorEnabled(boolean gradientChartColorEnabled) {
        this.gradientChartColorEnabled = gradientChartColorEnabled;
    }

    public boolean isDataSourceTags() {
        return dataSourceTags;
    }

    public void setDataSourceTags(boolean dataSourceTags) {
        this.dataSourceTags = dataSourceTags;
    }

    public boolean isDataSourceAPIKey() {
        return dataSourceAPIKey;
    }

    public void setDataSourceAPIKey(boolean dataSourceAPIKey) {
        this.dataSourceAPIKey = dataSourceAPIKey;
    }

    public boolean isReportTags() {
        return reportTags;
    }

    public void setReportTags(boolean reportTags) {
        this.reportTags = reportTags;
    }

    public boolean isReportType() {
        return reportType;
    }

    public void setReportType(boolean reportType) {
        this.reportType = reportType;
    }

    public boolean isReportAPIKey() {
        return reportAPIKey;
    }

    public void setReportAPIKey(boolean reportAPIKey) {
        this.reportAPIKey = reportAPIKey;
    }

    public boolean isReportModified() {
        return reportModified;
    }

    public void setReportModified(boolean reportModified) {
        this.reportModified = reportModified;
    }

    public boolean isReportCached() {
        return reportCached;
    }

    public void setReportCached(boolean reportCached) {
        this.reportCached = reportCached;
    }

    public boolean isReportPersistedCached() {
        return reportPersistedCached;
    }

    public void setReportPersistedCached(boolean reportPersistedCached) {
        this.reportPersistedCached = reportPersistedCached;
    }

    public int getDashboardStack1ColorStart() {
        return dashboardStack1ColorStart;
    }

    public void setDashboardStack1ColorStart(int dashboardStack1ColorStart) {
        this.dashboardStack1ColorStart = dashboardStack1ColorStart;
    }

    public int getDashboardStack1ColorEnd() {
        return dashboardStack1ColorEnd;
    }

    public void setDashboardStack1ColorEnd(int dashboardStack1ColorEnd) {
        this.dashboardStack1ColorEnd = dashboardStack1ColorEnd;
    }

    public int getDashboardStackColor2Start() {
        return dashboardStackColor2Start;
    }

    public void setDashboardStackColor2Start(int dashboardStackColor2Start) {
        this.dashboardStackColor2Start = dashboardStackColor2Start;
    }

    public int getDashboardStackColor2End() {
        return dashboardStackColor2End;
    }

    public void setDashboardStackColor2End(int dashboardStackColor2End) {
        this.dashboardStackColor2End = dashboardStackColor2End;
    }

    public ImageDescriptor getReportHeaderImage() {
        return reportHeaderImage;
    }

    public void setReportHeaderImage(ImageDescriptor reportHeaderImage) {
        this.reportHeaderImage = reportHeaderImage;
    }

    public boolean isReportHeader() {
        return reportHeader;
    }

    public void setReportHeader(boolean reportHeader) {
        this.reportHeader = reportHeader;
    }

    public int getReportBackgroundColor() {
        return reportBackgroundColor;
    }

    public void setReportBackgroundColor(int reportBackgroundColor) {
        this.reportBackgroundColor = reportBackgroundColor;
    }

    public int getReportTextColor() {
        return reportTextColor;
    }

    public void setReportTextColor(int reportTextColor) {
        this.reportTextColor = reportTextColor;
    }

    public ImageDescriptor getCoreAppBackgroundImage() {
        return coreAppBackgroundImage;
    }

    public void setCoreAppBackgroundImage(ImageDescriptor coreAppBackgroundImage) {
        this.coreAppBackgroundImage = coreAppBackgroundImage;
    }

    public int getCoreAppBackgroundColor() {
        return coreAppBackgroundColor;
    }

    public void setCoreAppBackgroundColor(int coreAppBackgroundColor) {
        this.coreAppBackgroundColor = coreAppBackgroundColor;
    }

    public String getCoreAppBackgroundSize() {
        return coreAppBackgroundSize;
    }

    public void setCoreAppBackgroundSize(String coreAppBackgroundSize) {
        this.coreAppBackgroundSize = coreAppBackgroundSize;
    }

    public int getHeaderBarBackgroundColor() {
        return headerBarBackgroundColor;
    }

    public void setHeaderBarBackgroundColor(int headerBarBackgroundColor) {
        this.headerBarBackgroundColor = headerBarBackgroundColor;
    }

    public ImageDescriptor getHeaderBarLogo() {
        return headerBarLogo;
    }

    public void setHeaderBarLogo(ImageDescriptor headerBarLogo) {
        this.headerBarLogo = headerBarLogo;
    }

    public int getHeaderBarDividerColor() {
        return headerBarDividerColor;
    }

    public void setHeaderBarDividerColor(int headerBarDividerColor) {
        this.headerBarDividerColor = headerBarDividerColor;
    }

    public int getCenterCanvasBackgroundColor() {
        return centerCanvasBackgroundColor;
    }

    public void setCenterCanvasBackgroundColor(int centerCanvasBackgroundColor) {
        this.centerCanvasBackgroundColor = centerCanvasBackgroundColor;
    }

    public double getCenterCanvasBackgroundAlpha() {
        return centerCanvasBackgroundAlpha;
    }

    public void setCenterCanvasBackgroundAlpha(double centerCanvasBackgroundAlpha) {
        this.centerCanvasBackgroundAlpha = centerCanvasBackgroundAlpha;
    }

    public boolean isCoreAppBackgroundImageEnabled() {
        return coreAppBackgroundImageEnabled;
    }

    public void setCoreAppBackgroundImageEnabled(boolean coreAppBackgroundImageEnabled) {
        this.coreAppBackgroundImageEnabled = coreAppBackgroundImageEnabled;
    }

    public boolean isCoreAppBackgroundColorEnabled() {
        return coreAppBackgroundColorEnabled;
    }

    public void setCoreAppBackgroundColorEnabled(boolean coreAppBackgroundColorEnabled) {
        this.coreAppBackgroundColorEnabled = coreAppBackgroundColorEnabled;
    }

    public boolean isCoreAppBackgroundSizeEnabled() {
        return coreAppBackgroundSizeEnabled;
    }

    public void setCoreAppBackgroundSizeEnabled(boolean coreAppBackgroundSizeEnabled) {
        this.coreAppBackgroundSizeEnabled = coreAppBackgroundSizeEnabled;
    }

    public boolean isHeaderBarBackgroundColorEnabled() {
        return headerBarBackgroundColorEnabled;
    }

    public void setHeaderBarBackgroundColorEnabled(boolean headerBarBackgroundColorEnabled) {
        this.headerBarBackgroundColorEnabled = headerBarBackgroundColorEnabled;
    }

    public boolean isHeaderBarDividerColorEnabled() {
        return headerBarDividerColorEnabled;
    }

    public void setHeaderBarDividerColorEnabled(boolean headerBarDividerColorEnabled) {
        this.headerBarDividerColorEnabled = headerBarDividerColorEnabled;
    }

    public boolean isCenterCanvasBackgroundColorEnabled() {
        return centerCanvasBackgroundColorEnabled;
    }

    public void setCenterCanvasBackgroundColorEnabled(boolean centerCanvasBackgroundColorEnabled) {
        this.centerCanvasBackgroundColorEnabled = centerCanvasBackgroundColorEnabled;
    }

    public boolean isCenterCanvasBackgroundAlphaEnabled() {
        return centerCanvasBackgroundAlphaEnabled;
    }

    public void setCenterCanvasBackgroundAlphaEnabled(boolean centerCanvasBackgroundAlphaEnabled) {
        this.centerCanvasBackgroundAlphaEnabled = centerCanvasBackgroundAlphaEnabled;
    }

    public boolean isReportBackgroundEnabled() {
        return reportBackgroundEnabled;
    }

    public void setReportBackgroundEnabled(boolean reportBackgroundEnabled) {
        this.reportBackgroundEnabled = reportBackgroundEnabled;
    }

    public boolean isReportBackgroundSizeEnabled() {
        return reportBackgroundSizeEnabled;
    }

    public void setReportBackgroundSizeEnabled(boolean reportBackgroundSizeEnabled) {
        this.reportBackgroundSizeEnabled = reportBackgroundSizeEnabled;
    }

    public boolean isMyDataName() {
        return myDataName;
    }

    public void setMyDataName(boolean myDataName) {
        this.myDataName = myDataName;
    }

    public boolean isMyDataSize() {
        return myDataSize;
    }

    public void setMyDataSize(boolean myDataSize) {
        this.myDataSize = myDataSize;
    }

    public boolean isMyDataOwner() {
        return myDataOwner;
    }

    public void setMyDataOwner(boolean myDataOwner) {
        this.myDataOwner = myDataOwner;
    }

    public boolean isMyDataCreationDate() {
        return myDataCreationDate;
    }

    public void setMyDataCreationDate(boolean myDataCreationDate) {
        this.myDataCreationDate = myDataCreationDate;
    }

    public boolean isMyDataLastTime() {
        return myDataLastTime;
    }

    public void setMyDataLastTime(boolean myDataLastTime) {
        this.myDataLastTime = myDataLastTime;
    }

    public boolean isMyDataCombine() {
        return myDataCombine;
    }

    public void setMyDataCombine(boolean myDataCombine) {
        this.myDataCombine = myDataCombine;
    }

    public boolean isMyDataNewScorecard() {
        return myDataNewScorecard;
    }

    public void setMyDataNewScorecard(boolean myDataNewScorecard) {
        this.myDataNewScorecard = myDataNewScorecard;
    }

    public boolean isMyDataAccountVisible() {
        return myDataAccountVisible;
    }

    public void setMyDataAccountVisible(boolean myDataAccountVisible) {
        this.myDataAccountVisible = myDataAccountVisible;
    }

    public boolean isMyDataNewDashboard() {
        return myDataNewDashboard;
    }

    public void setMyDataNewDashboard(boolean myDataNewDashboard) {
        this.myDataNewDashboard = myDataNewDashboard;
    }

    public boolean isMyDataLookupTable() {
        return myDataLookupTable;
    }

    public void setMyDataLookupTable(boolean myDataLookupTable) {
        this.myDataLookupTable = myDataLookupTable;
    }

    protected boolean propertyEnabled(List<ReportProperty> properties, String property) {
        for (ReportProperty reportProperty : properties) {
            if (reportProperty.getPropertyName().equals(property)) {
                return reportProperty.isEnabled();
            }
        }
        return false;
    }

    protected String findStringProperty(List<ReportProperty> properties, String property, String defaultValue) {
        for (ReportProperty reportProperty : properties) {
            if (reportProperty.getPropertyName().equals(property)) {
                ReportStringProperty reportStringProperty = (ReportStringProperty) reportProperty;
                return reportStringProperty.getValue() != null ? reportStringProperty.getValue() : defaultValue;
            }
        }
        return defaultValue;
    }

    protected boolean findBooleanProperty(List<ReportProperty> properties, String property, boolean defaultValue) {
        for (ReportProperty reportProperty : properties) {
            if (reportProperty.getPropertyName().equals(property)) {
                ReportBooleanProperty reportBooleanProperty = (ReportBooleanProperty) reportProperty;
                return reportBooleanProperty.getValue();
            }
        }
        return defaultValue;
    }

    protected double findNumberProperty(List<ReportProperty> properties, String property, double defaultValue) {
        for (ReportProperty reportProperty : properties) {
            if (reportProperty.getPropertyName().equals(property)) {
                ReportNumericProperty reportNumericProperty = (ReportNumericProperty) reportProperty;
                return reportNumericProperty.getValue();
            }
        }
        return defaultValue;
    }

    protected List<MultiColor> multiColorProperty(List<ReportProperty> properties, String property) {
        for (ReportProperty reportProperty : properties) {
            if (reportProperty.getPropertyName().equals(property)) {
                ReportMultiColorProperty reportMultiColorProperty = (ReportMultiColorProperty) reportProperty;
                return reportMultiColorProperty.toMultiColorList();
            }
        }
        return new ArrayList<MultiColor>();
    }

    protected ImageDescriptor findImage(List<ReportProperty> properties, String property, ImageDescriptor defaultValue) {
        for (ReportProperty reportProperty : properties) {
            if (reportProperty.getPropertyName().equals(property)) {
                ReportImageProperty reportImageProperty = (ReportImageProperty) reportProperty;
                return reportImageProperty.createImageDescriptor();
            }
        }
        return defaultValue;
    }
}
