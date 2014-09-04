package com.easyinsight.preferences;

import com.easyinsight.analysis.ReportMultiColorProperty;
import com.easyinsight.analysis.ReportProperty;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jamesboe
 * Date: Nov 17, 2010
 * Time: 1:32:30 PM
 */
@Entity
@Table(name="application_skin")
public class ApplicationSkinSettings {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="application_skin_id")
    private long skinID;

    public long getSkinID() {
        return skinID;
    }

    public void setSkinID(long skinID) {
        this.skinID = skinID;
    }

    @Column(name="global_skin")
    private boolean globalSkin;

    public boolean isGlobalSkin() {
        return globalSkin;
    }

    public void setGlobalSkin(boolean globalSkin) {
        this.globalSkin = globalSkin;
    }

    @Column(name="connection_type")
    private int connectionType;

    @Column(name="user_id")
    private Long userID;

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    @Column(name="account_id")
    private Long accountID;

    public Long getAccountID() {
        return accountID;
    }

    public void setAccountID(Long accountID) {
        this.accountID = accountID;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "application_skin_to_report_property",
            joinColumns = @JoinColumn(name = "application_skin_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "report_property_id", nullable = false))
    private List<ReportProperty> properties = new ArrayList<ReportProperty>();

    public List<ReportProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<ReportProperty> properties) {
        this.properties = properties;
    }

    public int getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(int connectionType) {
        this.connectionType = connectionType;
    }

    public ApplicationSkin toSkin() {
        ApplicationSkin skin = new ApplicationSkin();
        skin.populateProperties(properties);
        return skin;
    }

    public static ApplicationSkin retrieveSkin(long userID, Session session, long accountID) {
        ApplicationSkinSettings globalSkin;
        List results = session.createQuery("from ApplicationSkinSettings where globalSkin = ?").setBoolean(0, true).list();
        if (results.size() > 0) {
            globalSkin = (ApplicationSkinSettings) results.get(0);
        } else {
            globalSkin = new ApplicationSkinSettings();
        }
        ApplicationSkinSettings accountSkin;
        results = session.createQuery("from ApplicationSkinSettings where accountID = ?").setLong(0, accountID).list();
        if (results.size() > 0) {
            accountSkin = (ApplicationSkinSettings) results.get(0);
        } else {
            accountSkin = new ApplicationSkinSettings();
        }
        ApplicationSkinSettings userSkin;
        results = session.createQuery("from ApplicationSkinSettings where userID = ?").setLong(0, userID).list();
        if (results.size() > 0) {
            userSkin = (ApplicationSkinSettings) results.get(0);
        } else {
            userSkin = new ApplicationSkinSettings();
        }
        return globalSkin.toSkin().toSettings(ApplicationSkin.APPLICATION).override(accountSkin.toSkin().toSettings(ApplicationSkin.ACCOUNT)).override(userSkin.toSkin().toSettings(ApplicationSkin.USER)).toSkin();
    }

    public ApplicationSkinSettings override(ApplicationSkinSettings settings) {
        return override(settings, true);
    }

    public ApplicationSkinSettings override(ApplicationSkinSettings settings, boolean checkEnabled) {
        Map<String, ReportProperty> propertyMap = new HashMap<String, ReportProperty>();
        for (ReportProperty reportProperty : settings.getProperties()) {
            if (reportProperty instanceof ReportMultiColorProperty) {
                ReportMultiColorProperty multiColorProperty = (ReportMultiColorProperty) reportProperty;
                if (multiColorProperty.isColor1StartEnabled()  || !checkEnabled) {
                    propertyMap.put(reportProperty.getPropertyName(), reportProperty);
                }
            } else if (reportProperty.isEnabled() || !checkEnabled) {
                propertyMap.put(reportProperty.getPropertyName(), reportProperty);
            }
        }

        for (ReportProperty reportProperty : getProperties()) {
            if (!propertyMap.containsKey(reportProperty.getPropertyName())) {
                if (reportProperty instanceof ReportMultiColorProperty) {
                    ReportMultiColorProperty multiColorProperty = (ReportMultiColorProperty) reportProperty;
                    if (multiColorProperty.isColor1StartEnabled()  || !checkEnabled) {
                        propertyMap.put(reportProperty.getPropertyName(), reportProperty);
                    }
                } else if (reportProperty.isEnabled()  || !checkEnabled) {
                    propertyMap.put(reportProperty.getPropertyName(), reportProperty);
                }

            }
        }

        ApplicationSkinSettings result = new ApplicationSkinSettings();
        result.setProperties(new ArrayList<ReportProperty>(propertyMap.values()));
        return result;
    }
}