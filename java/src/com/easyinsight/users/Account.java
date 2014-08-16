package com.easyinsight.users;

import com.easyinsight.billing.BillingSystem;
import com.easyinsight.billing.BillingSystemFactory;
import com.easyinsight.billing.BrainTreeBlueBillingSystem;
import com.easyinsight.email.SendGridEmail;
import com.easyinsight.logging.LogClass;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.Days;

import javax.persistence.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * User: James Boe
 * Date: Jun 23, 2008
 * Time: 6:48:32 PM
 */
@Entity
@Table(name="account")
public class Account {

    public static final int PERSONAL = 1;
    public static final int BASIC = 2;
    public static final int PLUS = 3;
    public static final int PROFESSIONAL = 4;
    public static final int PREMIUM = 5;
    public static final int ENTERPRISE = 6;
    public static final int ADMINISTRATOR = 7;

    public static final int INACTIVE = 1;
    public static final int ACTIVE = 2;
    public static final int DELINQUENT = 3;
    public static final int SUSPENDED = 4;
    public static final int CLOSED = 5;
    public static final int PENDING_BILLING = 6;
    public static final int PREPARING = 7;
    public static final int BETA = 8;
    public static final int TRIAL = 9;
    public static final int CLOSING = 10;
    public static final int REACTIVATION_POSSIBLE = 11;
    public static final int BILLING_FAILED = 12;

    public static final int WEBSITE = 1;
    public static final int SNAPPCLOUD = 2;

    public static final long PERSONAL_MAX = 5000000;
    public static final long BASIC_MAX = 35000000;
    public static final long BASIC_MAX2 = 50000000;
    public static final long BASIC_MAX3 = 65000000;
    public static final long PLUS_MAX = 90000000;
    public static final long PLUS_MAX2 = 120000000;
    public static final long PLUS_MAX3 = 150000000;
    public static final long PROFESSIONAL_MAX   = 250000000L;
    public static final long PROFESSIONAL_MAX_2 = 500000000L;
    public static final long PROFESSIONAL_MAX_3 = 750000000L;
    public static final long PROFESSIONAL_MAX_4 = 1000000000L;

    public static final long PREMIUM_MAX = 10000000000L;
    public static final long ENTERPRISE_MAX = 1000000000;
    public static final long ADMINISTRATOR_MAX = Long.MAX_VALUE;

    public static final int TIERED = 0;
    public static final int NEW = 1;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="account_id")
    private long accountID;

    @Column(name="core_small_biz_connections")
    private int coreSmallBizConnections;

    @Column(name="addon_small_biz_connections")
    private int addonSmallBizConnections;

    @Column(name="addon_designers")
    private int addonDesigners;

    @Column(name="core_designers")
    private int coreDesigners;

    @Column(name="core_storage")
    private long coreStorage;

    @Column(name="default_font_family")
    private String defaultFontFamily;

    @Column(name="addon_storage_units")
    private int addonStorageUnits;

    @Column(name="unlimited_quickbase_connections")
    private boolean unlimitedQuickbaseConnections;
    @Column(name="addon_quickbase_connections")
    private int addonQuickbaseConnections;
    @Column(name="field_model")
    private boolean fieldModel;

    @Column(name="timezone")
    private String timezone;

    @Column(name="exchange_author")
    private boolean exchangeAuthor;

    @Column(name="enterprise_addon_cost")
    private int enterpriseAddonCost;

    @Column(name="tags_and_copy_enabled")
    private boolean tagsAndCopyEnabled;

    @Column(name="google_domain_name")
    private String googleDomainName;

    @Column(name="google_oauth_secret_token")
    private String googleSecretToken;

    @Column(name="google_oauth_token")
    private String googleToken;

    @Column(name="manual_invoicing")
    private boolean manualInvoicing;

    @JoinColumn(name="account_id")
    @OneToMany (cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<User>();

    @Column(name="upgraded")
    private boolean upgraded;

    @Column(name="current_size")
    private long usedSize;

    @Column(name="group_id")
    private Long groupID;

    @Column(name="account_type")
    private int accountType;

    @Column(name="first_day_of_week")
    private int firstDayOfWeek = 1;

    @Column(name="account_reactivation_date")
    private Date accountReactivationDate;

    @Column(name="source")
    private int accountSource;

    @Column(name="max_size")
    private long maxSize;

    @Column(name="max_users")
    private int maxUsers;

    @Column(name="api_enabled")
    private boolean apiEnabled = true;

    @Column(name="name")
    private String name;

    @Column(name="account_state")
    private int accountState;

    @Column(name="account_locale")
    private String accountLocale = "EN";

    @Column(name="account_key")
    private String accountKey;

    @Column(name="account_secret_key")
    private String accountSecretKey;

    @Column(name="billing_information_given")
    private Boolean billingInformationGiven;
    
    @Column(name="billing_failures")
    private int billingFailures;

    @Column(name="marketplace_enabled")
    private boolean marketplaceEnabled;

    @Column(name="public_data_enabled")
    private boolean publicDataEnabled;

    @Column(name="report_share_enabled")
    private boolean reportSharingEnabled;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="creation_date")
    private Date creationDate;

    @Column(name="billing_day_of_month")
    private Integer billingDayOfMonth;

    @Column(name="billing_month_of_year")
    private Integer billingMonthOfYear;

    @Column(name="date_format")
    private int dateFormat;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="external_login_id")
    private ExternalLogin externalLogin;

    @Column(name="currency_symbol")
    private String currencySymbol = "$";

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="account_id")
    private List<BandwidthUsage> historicBandwidthUsage = new ArrayList<BandwidthUsage>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="account_id")
    private List<AccountCreditCardBillingInfo> billingInfo = new ArrayList<AccountCreditCardBillingInfo>();

    @Column(name="opt_in_email")
    private boolean optInEmail;

    @Column(name="snappcloud_id")
    private String snappCloudId;

    @Column(name="subdomain")
    private String subdomain;

    @Column(name="subdomain_enabled")
    private boolean subdomainEnabled;

    @Column(name="ilog_enabled")
    private boolean ilogEnabled;

    @Column(name="heat_map_Enabled")
    private boolean heatMapEnabled = false;

    @Column(name="pricing_model")
    private int pricingModel;

    @Column(name="days_over_size_boundary")
    private int daysOverSizeBoundary;

    @Column(name="max_days_over_size_boundary")
    private int maxDaysOverSizeBoundary = 3;

    @Column(name="last_boundary_date")
    private Date lastBoundaryDate;

    @Column(name="address_line1")
    private String addressLine1;
    @Column(name="address_line2")
    private String addressLine2;
    @Column(name="city")
    private String city;
    @Column(name="state")
    private String state;
    @Column(name="postal_code")
    private String postalCode;
    @Column(name="country")
    private String country;
    @Column(name="vat")
    private String vat;

    @Column(name="special_storage")
    private String specialStorage;

    @Column(name="special_storage_caching")
    private String cachingStorage;

    @Column(name="new_pricing_model_invoice")
    private boolean newPricingModelInvoice;

    @Column(name="default_max_records")
    private int maxRecords;

    @Column(name="hourly_refresh_enabled")
    private boolean hourlyRefreshEnabled;

    @Column(name="send_emails_to_new_users")
    private boolean sendEmailsToNewUsers = true;

    @Column(name="look_and_feel_customized")
    private boolean lookAndFeelCustomized;

    @Column(name="use_html_version")
    private boolean useHTMLVersion = false;

    @Column(name="cancelling_user")
    private String cancellingUser;

    @Column(name="cancelling_reason")
    private String cancellingReason;

    @Column(name="next_bill_date")
    private Date nextBillDate;

    @Column(name="next_bill_amount")
    private Double nextBillAmount;

    public String getCachingStorage() {
        return cachingStorage;
    }

    public void setCachingStorage(String cachingStorage) {
        this.cachingStorage = cachingStorage;
    }

    public Date getNextBillDate() {
        return nextBillDate;
    }

    public void setNextBillDate(Date nextBillDate) {
        this.nextBillDate = nextBillDate;
    }

    public Double getNextBillAmount() {
        return nextBillAmount;
    }

    public void setNextBillAmount(Double nextBillAmount) {
        this.nextBillAmount = nextBillAmount;
    }

    public String getCancellingReason() {
        return cancellingReason;
    }

    public void setCancellingReason(String cancellingReason) {
        this.cancellingReason = cancellingReason;
    }

    public String getCancellingUser() {
        return cancellingUser;
    }

    public void setCancellingUser(String cancellingUser) {
        this.cancellingUser = cancellingUser;
    }

    public boolean isUseHTMLVersion() {
        return useHTMLVersion;
    }

    public void setUseHTMLVersion(boolean useHTMLVersion) {
        this.useHTMLVersion = useHTMLVersion;
    }

    public String getAccountLocale() {
        return accountLocale;
    }

    public void setAccountLocale(String accountLocale) {
        this.accountLocale = accountLocale;
    }

    public boolean isSendEmailsToNewUsers() {
        return sendEmailsToNewUsers;
    }

    public void setSendEmailsToNewUsers(boolean sendEmailsToNewUsers) {
        this.sendEmailsToNewUsers = sendEmailsToNewUsers;
    }

    public boolean isFieldModel() {
        return fieldModel;
    }

    public void setFieldModel(boolean fieldModel) {
        this.fieldModel = fieldModel;
    }

    public boolean isHourlyRefreshEnabled() {
        return hourlyRefreshEnabled;
    }

    public void setHourlyRefreshEnabled(boolean hourlyRefreshEnabled) {
        this.hourlyRefreshEnabled = hourlyRefreshEnabled;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getSpecialStorage() {
        return specialStorage;
    }

    public void setSpecialStorage(String specialStorage) {
        this.specialStorage = specialStorage;
    }

    public boolean isNewPricingModelInvoice() {
        return newPricingModelInvoice;
    }

    public void setNewPricingModelInvoice(boolean newPricingModelInvoice) {
        this.newPricingModelInvoice = newPricingModelInvoice;
    }

    public String getDefaultFontFamily() {
        return defaultFontFamily;
    }

    public void setDefaultFontFamily(String defaultFontFamily) {
        this.defaultFontFamily = defaultFontFamily;
    }

    public int getMaxRecords() {
        return maxRecords;
    }

    public void setMaxRecords(int maxRecords) {
        this.maxRecords = maxRecords;
    }

    public boolean isTagsAndCopyEnabled() {
        return tagsAndCopyEnabled;
    }

    public void setTagsAndCopyEnabled(boolean tagsAndCopyEnabled) {
        this.tagsAndCopyEnabled = tagsAndCopyEnabled;
    }

    public int getEnterpriseAddonCost() {
        return enterpriseAddonCost;
    }

    public void setEnterpriseAddonCost(int enterpriseAddonCost) {
        this.enterpriseAddonCost = enterpriseAddonCost;
    }

    public boolean isExchangeAuthor() {
        return exchangeAuthor;
    }

    public void setExchangeAuthor(boolean exchangeAuthor) {
        this.exchangeAuthor = exchangeAuthor;
    }

    public boolean isUnlimitedQuickbaseConnections() {
        return unlimitedQuickbaseConnections;
    }

    public void setUnlimitedQuickbaseConnections(boolean unlimitedQuickbaseConnections) {
        this.unlimitedQuickbaseConnections = unlimitedQuickbaseConnections;
    }

    public int getAddonQuickbaseConnections() {
        return addonQuickbaseConnections;
    }

    public void setAddonQuickbaseConnections(int addonQuickbaseConnections) {
        this.addonQuickbaseConnections = addonQuickbaseConnections;
    }

    public boolean isLookAndFeelCustomized() {
        return lookAndFeelCustomized;
    }

    public void setLookAndFeelCustomized(boolean lookAndFeelCustomized) {
        this.lookAndFeelCustomized = lookAndFeelCustomized;
    }

    public int getAddonDesigners() {
        return addonDesigners;
    }

    public void setAddonDesigners(int addonDesigners) {
        this.addonDesigners = addonDesigners;
    }

    public int getCoreSmallBizConnections() {
        return coreSmallBizConnections;
    }

    public void setCoreSmallBizConnections(int coreSmallBizConnections) {
        this.coreSmallBizConnections = coreSmallBizConnections;
    }

    public int getAddonSmallBizConnections() {
        return addonSmallBizConnections;
    }

    public void setAddonSmallBizConnections(int addonSmallBizConnections) {
        this.addonSmallBizConnections = addonSmallBizConnections;
    }

    public int getCoreDesigners() {
        return coreDesigners;
    }

    public void setCoreDesigners(int coreDesigners) {
        this.coreDesigners = coreDesigners;
    }

    public long getCoreStorage() {
        return coreStorage;
    }

    public void setCoreStorage(long coreStorage) {
        this.coreStorage = coreStorage;
    }

    public int getAddonStorageUnits() {
        return addonStorageUnits;
    }

    public void setAddonStorageUnits(int addonStorageUnits) {
        this.addonStorageUnits = addonStorageUnits;
    }

    public long getUsedSize() {
        return usedSize;
    }

    public void setUsedSize(long usedSize) {
        this.usedSize = usedSize;
    }

    public Date getLastBoundaryDate() {
        return lastBoundaryDate;
    }

    public void setLastBoundaryDate(Date lastBoundaryDate) {
        this.lastBoundaryDate = lastBoundaryDate;
    }

    public String getGoogleSecretToken() {
        return googleSecretToken;
    }

    public void setGoogleSecretToken(String googleSecretToken) {
        this.googleSecretToken = googleSecretToken;
    }

    public String getGoogleToken() {
        return googleToken;
    }

    public void setGoogleToken(String googleToken) {
        this.googleToken = googleToken;
    }

    public String getGoogleDomainName() {
        return googleDomainName;
    }

    public void setGoogleDomainName(String googleDomainName) {
        this.googleDomainName = googleDomainName;
    }

    public int getDaysOverSizeBoundary() {
        return daysOverSizeBoundary;
    }

    public void setDaysOverSizeBoundary(int daysOverSizeBoundary) {
        this.daysOverSizeBoundary = daysOverSizeBoundary;
    }

    public int getBillingFailures() {
        return billingFailures;
    }

    public void setBillingFailures(int billingFailures) {
        this.billingFailures = billingFailures;
    }

    public int getPricingModel() {
        return pricingModel;
    }

    public void setPricingModel(int pricingModel) {
        this.pricingModel = pricingModel;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getVat() {
        return vat;
    }

    public void setVat(String vat) {
        this.vat = vat;
    }

    public boolean isIlogEnabled() {
        return ilogEnabled;
    }

    public void setIlogEnabled(boolean ilogEnabled) {
        this.ilogEnabled = ilogEnabled;
    }

    public boolean isHeatMapEnabled() {
        return heatMapEnabled;
    }

    public void setHeatMapEnabled(boolean heatMapEnabled) {
        this.heatMapEnabled = heatMapEnabled;
    }

    public int getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        this.firstDayOfWeek = firstDayOfWeek;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public ExternalLogin getExternalLogin() {
        return externalLogin;
    }

    public void setExternalLogin(ExternalLogin externalLogin) {
        this.externalLogin = externalLogin;
    }

    public boolean isManualInvoicing() {
        return manualInvoicing;
    }

    public void setManualInvoicing(boolean manualInvoicing) {
        this.manualInvoicing = manualInvoicing;
    }

    public boolean isUpgraded() {
        return upgraded;
    }

    public void setUpgraded(boolean upgraded) {
        this.upgraded = upgraded;
    }

    public boolean isMarketplaceEnabled() {
        return marketplaceEnabled;
    }

    public void setMarketplaceEnabled(boolean marketplaceEnabled) {
        this.marketplaceEnabled = marketplaceEnabled;
    }

    public boolean isPublicDataEnabled() {
        return publicDataEnabled;
    }

    public void setPublicDataEnabled(boolean publicDataEnabled) {
        this.publicDataEnabled = publicDataEnabled;
    }

    public int getMaxDaysOverSizeBoundary() {
        return maxDaysOverSizeBoundary;
    }

    public void setMaxDaysOverSizeBoundary(int maxDaysOverSizeBoundary) {
        this.maxDaysOverSizeBoundary = maxDaysOverSizeBoundary;
    }

    public boolean isReportSharingEnabled() {
        return reportSharingEnabled;
    }

    public void setReportSharingEnabled(boolean reportSharingEnabled) {
        this.reportSharingEnabled = reportSharingEnabled;
    }

    public List<AccountCreditCardBillingInfo> getBillingInfo() {
        return billingInfo;
    }

    public void setBillingInfo(List<AccountCreditCardBillingInfo> billingInfo) {
        this.billingInfo = billingInfo;
    }

    public boolean isApiEnabled() {
        return apiEnabled;
    }

    public void setApiEnabled(boolean apiEnabled) {
        this.apiEnabled = apiEnabled;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isOptInEmail() {
        return optInEmail;
    }

    public void setOptInEmail(boolean optInEmail) {
        this.optInEmail = optInEmail;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }

    public String getAccountSecretKey() {
        return accountSecretKey;
    }

    public void setAccountSecretKey(String accountSecretKey) {
        this.accountSecretKey = accountSecretKey;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public long getAccountID() {
        return accountID;
    }

    public void setAccountID(long accountID) {
        this.accountID = accountID;
    }

    public int getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(int dateFormat) {
        this.dateFormat = dateFormat;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public void removeUser(User user) {
        this.users.remove(user);
    }

    public int getAccountState() {
        return accountState;
    }

    public void setAccountState(int accountState) {
        this.accountState = accountState;
    }

    public AccountTransferObject toTransferObject() {
        AccountTransferObject transfer = new AccountTransferObject();
        transfer.setAccountID(accountID);
        //transfer.setUsers(users);
        transfer.setAccountType(accountType);
        transfer.setMaxSize(maxSize);
        transfer.setCreationDate(creationDate);
        transfer.setName(name);
        transfer.setMaxUsers(maxUsers);
        transfer.setAccountState(accountState);
        transfer.setApiEnabled(apiEnabled);
        transfer.setPricingModel(pricingModel);
        transfer.setNextBillDate(nextBillDate);
        transfer.setNextBillAmount(nextBillAmount);
        return transfer;
    }

    public AccountAdminTO toAdminTO() {
        AccountAdminTO transfer = new AccountAdminTO();
        transfer.setAccountID(accountID);
        //transfer.setUsers(users);
        transfer.setAccountType(accountType);
        transfer.setMaxSize(maxSize);
        transfer.setName(name);
        transfer.setMaxUsers(maxUsers);        
        if (groupID != null) {
            transfer.setGroupID(groupID);
        }
        long latestLoginDate = 0;
        List<UserTransferObject> adminUsers = new ArrayList<UserTransferObject>();
        List<UserTransferObject> allUsers = new ArrayList<UserTransferObject>();
        for (User user : getUsers()) {
            if (user.isAccountAdmin()) {
                adminUsers.add(user.toUserTransferObject());
            }
            if (user.getLastLoginDate() != null) {
                if (user.getLastLoginDate().getTime() > latestLoginDate) {
                    latestLoginDate = user.getLastLoginDate().getTime();
                }
            }
            allUsers.add(user.toUserTransferObject());
        }
        transfer.setCreationDate(getCreationDate());
        transfer.setAllUsers(allUsers);
        transfer.setLastUserLoginDate(new Date(latestLoginDate));
        transfer.setAdminUsers(adminUsers);
        transfer.setAccountState(accountState);
        transfer.setApiEnabled(apiEnabled);
        transfer.setOptIn(optInEmail);
        return transfer;
    }
    
    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    public Long getGroupID() {
        return groupID;
    }

    public void setGroupID(Long groupID) {
        this.groupID = groupID;
    }

    public int getAccountSource() {
        return accountSource;
    }

    public void setAccountSource(int accountSource) {
        this.accountSource = accountSource;
    }

    public static long getMaxCount(int tier) {
        switch(tier) {
            case Account.PERSONAL:
                return PERSONAL_MAX;
            case Account.BASIC:
                return BASIC_MAX;
            case Account.PLUS:
                return PLUS_MAX;
            case Account.PROFESSIONAL:
                return PROFESSIONAL_MAX;
            case Account.PREMIUM:
                return PREMIUM_MAX;
            case Account.ENTERPRISE:
                return ENTERPRISE_MAX;
            case Account.ADMINISTRATOR:
                return ADMINISTRATOR_MAX;
            default:
                throw new RuntimeException("Unknown account type " + tier);
        }
    }

    public Boolean isBillingInformationGiven() {
        return billingInformationGiven;
    }

    public void setBillingInformationGiven(Boolean billingInformationGiven) {
        this.billingInformationGiven = billingInformationGiven;
    }

    public Integer getBillingDayOfMonth() {
        return billingDayOfMonth;
    }

    public void setBillingDayOfMonth(Integer billingDayOfMonth) {
        this.billingDayOfMonth = billingDayOfMonth;
    }

    public AccountCreditCardBillingInfo upgradeBill(AccountTypeChange accountTypeChange, double charge, Session session) {
        LogClass.info("Starting billing for account ID:" + this.getAccountID());
        // the indirection here is to support invoice billingSystem later
//        BillingSystem billingSystem = new BrainTreeBillingSystem(ConfigLoader.instance().getBillingUsername(), ConfigLoader.instance().getBillingPassword());
        BillingSystem billingSystem = BillingSystemFactory.getBillingSystem();
        AccountCreditCardBillingInfo info = billingSystem.billAccount(getAccountID(), charge);
        boolean successful;
        info.setDays(accountTypeChange.isYearly() ? 365 : 28);
        if(!info.isSuccessful()) {
            successful = false;
        }
        else {
            billingFailures = 0;
            LogClass.info("Success!");
            successful = true;
        }

        if (successful) {
            accountTypeChange.apply(this, session);
            String invoiceBody = info.toInvoiceText(this);
            for (User user : getUsers()) {
                if (user.isInvoiceRecipient()) {
                    try {
                        new SendGridEmail().sendEmail(user.getEmail(), "Easy Insight - New Invoice", invoiceBody, "support@easy-insight.com", false, "Easy Insight");
                    } catch (Exception e) {
                        LogClass.error(e);
                    }
                }
            }
        }
        LogClass.info("Completed billing Account ID:" + this.getAccountID());
        return info;
    }

    public AccountCreditCardBillingInfo bill() {

        LogClass.info("Starting billing for account ID:" + this.getAccountID());

        //double credit = calculateCredit(this);
        double credit = 0;
        double cost = createTotalCost();

        /*if (credit >= cost) {
            info.setAmount(String.valueOf(cost));
            info.setTransactionTime(new Date());
            info.setResponseCode("100");
            info.setDays(getBillingDayOfMonth() != null ? 365 : 28);
            info.setAgainstCredit(true);
        } else {*/

        double amount = cost - credit;
        BillingSystem billingSystem = BillingSystemFactory.getBillingSystem();
        AccountCreditCardBillingInfo info = billingSystem.billAccount(getAccountID(), amount);

        boolean successful;
        if(!info.isSuccessful()) {
            if (getBillingFailures() >= 7) {
                setAccountState(Account.BILLING_FAILED);
            } else {
                billingFailures++;
            }
            LogClass.info("Billing failed on " + accountID + ".");
            successful = false;
        }
        else {
            billingFailures = 0;
            LogClass.info("Successfully billed " + accountID + " for " + amount + ".");
            successful = true;
        }

        info.setDays(getBillingMonthOfYear() != null ? 365 : 28);

        if (successful) {
            String invoiceBody = info.toInvoiceText(this);
            for (User user : getUsers()) {
                if (user.isInvoiceRecipient()) {
                    try {
                        new SendGridEmail().sendEmail(user.getEmail(), "Easy Insight - New Invoice", invoiceBody, "support@easy-insight.com", false, "Easy Insight");
                    } catch (Exception e) {
                        LogClass.error(e);
                    }
                }
            }
        } else {
            if (accountState == Account.BILLING_FAILED) {
                String failureBody = "We were unable to successfully bill your Easy Insight account because of difficulties with the credit card on file. You will need to log in and update your billing information to resume service.\r\n\r\nIf you have any questions, please contact support at support@easy-insight.com.";
                for (User user : getUsers()) {
                    if (user.isInvoiceRecipient()) {
                        try {
                            new SendGridEmail().sendEmail(user.getEmail(), "Easy Insight - Failed Recurring Billing", failureBody, "support@easy-insight.com", false, "Easy Insight");
                        } catch (Exception e) {
                            LogClass.error(e);
                        }
                    }
                }
            } else {
                if (billingFailures == 1) {
                    String failureBody = "We were unable to successfully bill your Easy Insight account because of difficulties with the credit card on file. You will need to log in and update your billing information within the next seven days.\r\n\r\nIf you have any questions, please contact support at support@easy-insight.com.";
                    for (User user : getUsers()) {
                        if (user.isInvoiceRecipient()) {
                            try {
                                new SendGridEmail().sendEmail(user.getEmail(), "Easy Insight - Failed Recurring Billing", failureBody, "support@easy-insight.com", false, "Easy Insight");
                            } catch (Exception e) {
                                LogClass.error(e);
                            }
                        }
                    }
                }
            }
        }
        LogClass.info("Completed billing Account ID:" + this.getAccountID());
        return info;
    }

    public void syncState() {
        new BrainTreeBlueBillingSystem().setSubscribedStatus(this);
    }

    public Integer getBillingMonthOfYear() {
        return billingMonthOfYear;
    }

    public void setBillingMonthOfYear(Integer billingMonthOfYear) {
        this.billingMonthOfYear = billingMonthOfYear;
    }

    public String getSnappCloudId() {
        return snappCloudId;
    }

    public void setSnappCloudId(String snappCloudId) {
        this.snappCloudId = snappCloudId;
    }

    public boolean isSubdomainEnabled() {
        return subdomainEnabled;
    }

    public void setSubdomainEnabled(boolean subdomainEnabled) {
        this.subdomainEnabled = subdomainEnabled;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    public String billingIntroParagraph() {
        if (accountState == Account.TRIAL) {
            return "Please input your billing information below. Your first billing cycle will start upon completion of any remaining trial time. Easy Insight does not offer any type of refund after billing.";
        } else if (accountState == Account.DELINQUENT || accountState == Account.CLOSING || accountState == Account.CLOSED) {
            return "Your free 30 day trial has expired. Please input your billing information below. Your card will be billed upon submit. Easy Insight does not offer any type of refund after billing.";
        } else if (accountState == Account.BILLING_FAILED) {
            return "Recurring billing for your account failed. Please input updated billing information below. Your card will be billed upon submit. Easy Insight does not offer any type of refund after billing.";
        } else if (accountState == Account.ACTIVE) {
            return "Please input your updated billing information below. The new card will be billed as per your normal billing cycle. Easy Insight does not offer any type of refund after billing.";
        }
        return "Please input your billing information below. Your card will be billed upon submit. Easy Insight does not offer any type of refund after billing.";
    }

    public String billingHeader() {
        if (accountState == Account.TRIAL) {
            return "";
        } else if (accountState == Account.DELINQUENT) {
            return "Your Free Trial Has Expired";
        } else if (accountState == Account.BILLING_FAILED) {
            return "Recurring Billing Failed";
        } else if (accountState == Account.ACTIVE) {
            return "";
        }
        return "";
    }

    public String successMessage() {
        if (accountState == Account.TRIAL) {
            return "You have successfully set up your billing account. You will not be billed until your free trial has expired.";
        } else {
            return "You have successfully set up your billing account. Your account is now active and accessible again!";
        }
    }

    public String planName() {
        if (getAccountType() == Account.BASIC) {
            return "Basic Plan";
        } else if (getAccountType() == Account.PLUS) {
            return "Plus Plan";
        } else if (getAccountType() == Account.PROFESSIONAL) {
            return "Professional Plan";
        } else if (getAccountType() == Account.ENTERPRISE) {
            return "Enterprise Plan";
        } else if (getAccountType() == Account.ADMINISTRATOR) {
            return "Administrator";
        }
        throw new UnsupportedOperationException();
    }

    public String designers() {
        if (maxUsers == 1) {
            return maxUsers + " Designer";
        } else {
            return maxUsers + " Designers";
        }
    }

    public String billingInterval() {
        if (getBillingMonthOfYear() != null) {
            return "Annually";
        } else {
            return "Monthly";
        }
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public String createCostString() {
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        return nf.format(createCost());
    }

    public String enterpriseCostString() {
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        return nf.format(enterpriseAddonCost);
    }

    public String addonCostString() {
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        return nf.format(createCost() - 50);
    }

    public double createCost() {
        return createBaseCost(pricingModel, accountType, addonDesigners, addonSmallBizConnections, addonStorageUnits, getBillingMonthOfYear() != null);
    }

    public String createTotalCostString() {
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        return nf.format(createTotalCost() + enterpriseAddonCost);
    }

    public double createTotalCost() {
        return createTotalCost(pricingModel, accountType, addonDesigners, addonSmallBizConnections, addonStorageUnits, getBillingMonthOfYear() != null);
    }

    public static double createBaseCost(int pricingModel, int tier, int designers, int smallBizConnections, int storageAddons, boolean yearly) {
        double cost;
        if (pricingModel == 0) {
            if (tier == Account.BASIC) {
                cost = (25 * (yearly ? 12 : 1));
            } else if (tier == Account.PLUS) {
                cost = (75 * (yearly ? 12 : 1));
            } else if (tier == Account.PROFESSIONAL) {
                cost = (200 * (yearly ? 12 : 1));
            } else {
                throw new RuntimeException();
            }
        } else {
            cost = 50 + (designers * 15) + (smallBizConnections * 15) + (storageAddons * 150);
        }
        return cost;
    }

    public static double createTotalCost(int pricingModel, int tier, int designers, int smallBizConnections, int storageAddons, boolean yearly) {
        double cost = createBaseCost(pricingModel, tier, designers, smallBizConnections, storageAddons, yearly);
        cost = cost - (createDiscount(cost, yearly));
        return cost;
    }

    public static double createDiscount(double baseCost, boolean yearly) {
        double discount = 0;
        if (yearly) {
            discount = baseCost / 12;
        }
        return discount;
    }

    public static double calculateCredit(Account account) {
        double totalCredit = 0;
        for (AccountCreditCardBillingInfo info : account.getBillingInfo()) {
            if ("100".equals(info.getResponseCode()) || info.isSuccessful()) {
                double amount = Double.parseDouble(info.getAmount());
                DateTime lastTime = new DateTime(info.getTransactionTime());
                DateTime now = new DateTime(System.currentTimeMillis());
                int daysBetween = Days.daysBetween(lastTime, now).getDays();
                int period = info.getDays();
                int elapsed = period - daysBetween;
                if (elapsed > 0) {
                    double multiple = (double) elapsed / (double) period;
                    double credit = amount * multiple;
                    totalCredit += credit;
                }
            }

        }
        return totalCredit;
    }

    public String storageString() {
        return humanReadableByteCount(maxSize, true);
    }
}
