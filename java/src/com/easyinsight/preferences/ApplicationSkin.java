package com.easyinsight.preferences;

import com.easyinsight.analysis.*;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jamesboe
 * Date: Nov 17, 2010
 * Time: 10:26:46 PM
 */
public class ApplicationSkin {
    private ImageDescriptor coreAppBackgroundImage;
    private boolean coreAppBackgroundImageEnabled;
    private int coreAppBackgroundColor;
    private boolean coreAppBackgroundColorEnabled;
    private String coreAppBackgroundSize;
    private boolean coreAppBackgroundSizeEnabled;
    private int headerBarBackgroundColor;
    private boolean headerBarBackgroundColorEnabled;
    private ImageDescriptor headerBarLogo;
    private boolean headerBarLogoEnabled;
    private int headerBarDividerColor;
    private boolean headerBarDividerColorEnabled;
    private int centerCanvasBackgroundColor;
    private boolean centerCanvasBackgroundColorEnabled;
    private double centerCanvasBackgroundAlpha;
    private boolean centerCanvasBackgroundAlphaEnabled;
    private ImageDescriptor reportBackground;
    private boolean reportBackgroundEnabled;
    private String reportBackgroundSize;
    private boolean reportBackgroundSizeEnabled;
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ApplicationSkinSettings toSettings() {
        ApplicationSkinSettings settings = new ApplicationSkinSettings();
        List<ReportProperty> properties = new ArrayList<ReportProperty>();
        if (coreAppBackgroundImage != null) {
            properties.add(new ReportImageProperty("coreAppBackgroundImage", coreAppBackgroundImage, coreAppBackgroundImageEnabled));
        }
        if (headerBarLogo != null) {
            properties.add(new ReportImageProperty("headerBarLogo", headerBarLogo, headerBarLogoEnabled));
        }
        if (reportBackground != null) {
            properties.add(new ReportImageProperty("reportBackground", reportBackground, reportBackgroundEnabled));
        }
        properties.add(new ReportNumericProperty("coreAppBackgroundColor", coreAppBackgroundColor, coreAppBackgroundColorEnabled));
        properties.add(new ReportNumericProperty("headerBarBackgroundColor", headerBarBackgroundColor, headerBarBackgroundColorEnabled));
        properties.add(new ReportNumericProperty("headerBarDividerColor", headerBarDividerColor, headerBarDividerColorEnabled));
        properties.add(new ReportNumericProperty("centerCanvasBackgroundColor", centerCanvasBackgroundColor, centerCanvasBackgroundColorEnabled));
        properties.add(new ReportNumericProperty("centerCanvasBackgroundAlpha", centerCanvasBackgroundAlpha, centerCanvasBackgroundAlphaEnabled));
        properties.add(new ReportStringProperty("coreAppBackgroundSize", coreAppBackgroundSize, coreAppBackgroundSizeEnabled));
        properties.add(new ReportStringProperty("reportBackgroundSize", reportBackgroundSize, reportBackgroundSizeEnabled));
        settings.setSkinID(id);
        settings.setProperties(properties);
        return settings;
    }

    public void populateProperties(List<ReportProperty> properties) {
        coreAppBackgroundImage = findImage(properties, "coreAppBackgroundImage", null);
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
        reportBackground = findImage(properties, "reportBackground", null);
        reportBackgroundEnabled = propertyEnabled(properties, "reportBackground");
        reportBackgroundSize = findStringProperty(properties, "reportBackgroundSize", "100%");
        reportBackgroundSizeEnabled = propertyEnabled(properties, "reportBackgroundSize");
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

    public ImageDescriptor getReportBackground() {
        return reportBackground;
    }

    public void setReportBackground(ImageDescriptor reportBackground) {
        this.reportBackground = reportBackground;
    }

    public String getReportBackgroundSize() {
        return reportBackgroundSize;
    }

    public void setReportBackgroundSize(String reportBackgroundSize) {
        this.reportBackgroundSize = reportBackgroundSize;
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

    public boolean isHeaderBarLogoEnabled() {
        return headerBarLogoEnabled;
    }

    public void setHeaderBarLogoEnabled(boolean headerBarLogoEnabled) {
        this.headerBarLogoEnabled = headerBarLogoEnabled;
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
