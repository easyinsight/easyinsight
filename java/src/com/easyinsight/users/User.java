package com.easyinsight.users;

import com.easyinsight.export.ExportMetadata;
import com.easyinsight.preferences.UISettings;
import com.easyinsight.util.RandomTextGenerator;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.*;
import java.util.Date;

/**
 * User: jboe
 * Date: Jan 2, 2008
 * Time: 5:33:36 PM
 */

@Entity
@Table(name="user")
public class User {
    @Column(name="username")
    private String userName;
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="user_id")
    private long userID;
    @Column(name="password")
    private String password;
    @Column(name="email")
    private String email;
    @Column(name="name")
    private String name;

    @Column(name="consultant")
    private boolean consultant;

    @Column(name="refresh_reports")
    private boolean refreshReports;

    @Column(name="initial_setup_done")
    private boolean initialSetupDone;

    @Transient
    private UISettings uiSettings;

    @Column(name="guest_user")
    private boolean guestUser;

    @Column(name="first_name")
    private String firstName;

    @Column(name="title")
    private String title;

    @Column(name="persona_id")
    private Long personaID;

    @Column(name="account_admin")
    private boolean accountAdmin;

    @Column(name="invoice_recipient")
    private boolean invoiceRecipient;

    @Column(name="hash_type")
    private String hashType;

    @Column(name="hash_salt")
    private String hashSalt;

    @Column(name="analyst")
    private boolean analyst = true;

    @Column(name="user_key")
    private String userKey;
    @Column(name="user_secret_key")
    private String userSecretKey;

    @Column(name="last_login_date")
    private Date lastLoginDate;

    @Column(name="opt_in_email")
    private boolean optInEmail;

    @Column(name="fixed_dashboard_id")
    private Long fixedDashboardID;

    @Column(name="created_on")
    private Date createdOn = new Date();

    @Column(name="updated_on")
    private Date updatedOn = new Date();

    @ManyToOne
    @JoinColumn (name="account_id")
    private Account account;

    @Column(name="time_zone")
    private int timezone;

    @Column(name="html_or_flex")
    private boolean htmlOrFlex;

    @Column(name="news_dismiss_date")
    private Date newsDismissDate;

    @Column(name="test_account_visible")
    private boolean testAccountVisible = true;

    @Column(name="user_currency")
    private int currency;
    @Column(name="user_locale")
    private String userLocale = "0";
    @Column(name="user_date_format")
    private int dateFormat = 6;

    public User() {
    }

    public User(String userName, String password, String name, String email) {
        this.userName = userName;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    public boolean isConsultant() {
        return consultant;
    }

    public void setConsultant(boolean consultant) {
        this.consultant = consultant;
    }

    public UserTransferObject toUserTransferObject() {
        UserTransferObject userTransferObject = new UserTransferObject();
        userTransferObject.setUserID(userID);
        userTransferObject.setEmail(email);
        userTransferObject.setUserName(userName);
        userTransferObject.setName(name);
        userTransferObject.setAccountAdmin(accountAdmin);
        userTransferObject.setTitle(title);
        userTransferObject.setFirstName(firstName);
        userTransferObject.setPersonaID(personaID != null ? personaID : 0);
        userTransferObject.setFixedDashboardID(fixedDashboardID != null ? fixedDashboardID : 0);
        userTransferObject.setAutoRefreshReports(refreshReports);
        userTransferObject.setInvoiceRecipient(invoiceRecipient);
        userTransferObject.setAnalyst(analyst);
        userTransferObject.setLastLoginDate(lastLoginDate);
        userTransferObject.setTestAccountVisible(testAccountVisible);
        userTransferObject.setUserLocale(userLocale);
        userTransferObject.setDateFormat(dateFormat);
        userTransferObject.setCurrency(currency);
        return userTransferObject;
    }

    public boolean isTestAccountVisible() {
        return testAccountVisible;
    }

    public void setTestAccountVisible(boolean testAccountVisible) {
        this.testAccountVisible = testAccountVisible;
    }

    public int getCurrency() {
        return currency;
    }

    public void setCurrency(int currency) {
        this.currency = currency;
    }

    public String getUserLocale() {
        return userLocale;
    }

    public void setUserLocale(String userLocale) {
        this.userLocale = userLocale;
    }

    public int getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(int dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Date getNewsDismissDate() {
        return newsDismissDate;
    }

    public void setNewsDismissDate(Date newsDismissDate) {
        this.newsDismissDate = newsDismissDate;
    }

    public int getTimezone() {
        return timezone;
    }

    public void setTimezone(int timezone) {
        this.timezone = timezone;
    }

    public boolean isHtmlOrFlex() {
        return htmlOrFlex;
    }

    public void setHtmlOrFlex(boolean htmlOrFlex) {
        this.htmlOrFlex = htmlOrFlex;
    }

    public boolean isRefreshReports() {
        return refreshReports;
    }

    public void setRefreshReports(boolean refreshReports) {
        this.refreshReports = refreshReports;
    }

    public boolean isInvoiceRecipient() {
        return invoiceRecipient;
    }

    public void setInvoiceRecipient(boolean invoiceRecipient) {
        this.invoiceRecipient = invoiceRecipient;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Long getFixedDashboardID() {
        return fixedDashboardID;
    }

    public void setFixedDashboardID(Long fixedDashboardID) {
        this.fixedDashboardID = fixedDashboardID;
    }

    public boolean isAnalyst() {
        return analyst;
    }

    public void setAnalyst(boolean analyst) {
        this.analyst = analyst;
    }

    public String getHashType() {
        return hashType;
    }

    public void setHashType(String hashType) {
        this.hashType = hashType;
    }

    public boolean isGuestUser() {
        return guestUser;
    }

    public void setGuestUser(boolean guestUser) {
        this.guestUser = guestUser;
    }

    public boolean isOptInEmail() {
        return optInEmail;
    }

    public void setOptInEmail(boolean optInEmail) {
        this.optInEmail = optInEmail;
    }

    public String getHashSalt() {
        return hashSalt;
    }

    public void setHashSalt(String hashSalt) {
        this.hashSalt = hashSalt;
    }

    public boolean isInitialSetupDone() {
        return initialSetupDone;
    }

    public void setInitialSetupDone(boolean initialSetupDone) {
        this.initialSetupDone = initialSetupDone;
    }

    public UISettings getUiSettings() {
        return uiSettings;
    }

    public void setUiSettings(UISettings uiSettings) {
        this.uiSettings = uiSettings;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getUserSecretKey() {
        return userSecretKey;
    }

    public void setUserSecretKey(String userSecretKey) {
        this.userSecretKey = userSecretKey;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getPersonaID() {
        return personaID;
    }

    public void setPersonaID(Long personaID) {
        this.personaID = personaID;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAccountAdmin() {
        return accountAdmin;
    }

    public void setAccountAdmin(boolean accountAdmin) {
        this.accountAdmin = accountAdmin;
    }

    public void update(UserTransferObject transferObject) {
        setUserName(transferObject.getUserName());
        setAccountAdmin(transferObject.isAccountAdmin());
        setEmail(transferObject.getEmail());
        setFirstName(transferObject.getFirstName());
        setPersonaID(transferObject.getPersonaID() > 0 ? transferObject.getPersonaID() : null);
        setName(transferObject.getName());
        setTitle(transferObject.getTitle());
        setOptInEmail(transferObject.isOptInEmail());
        setInvoiceRecipient(transferObject.isInvoiceRecipient());
        setTestAccountVisible(transferObject.isTestAccountVisible());
        setRefreshReports(transferObject.isAutoRefreshReports());
        setAnalyst(transferObject.isAnalyst());
        setCurrency(transferObject.getCurrency());
        setUserLocale(transferObject.getUserLocale());
        setDateFormat(transferObject.getDateFormat());
        if (transferObject.getFixedDashboardID() == 0) {
            setFixedDashboardID(null);
        } else {
            setFixedDashboardID(transferObject.getFixedDashboardID());
        }
    }
}
