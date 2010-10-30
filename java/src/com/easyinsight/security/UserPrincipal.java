package com.easyinsight.security;

import com.easyinsight.users.Scenario;

import java.security.Principal;
import java.io.Serializable;

/**
 * User: James Boe
* Date: Aug 12, 2008
* Time: 10:04:42 AM
*/
public class UserPrincipal implements Principal, Serializable {

    private String userName;
    private long accountID;
    private long userID;
    private int accountType;
    private boolean accountAdmin;
    private boolean guestUser;
    private Scenario scenario;

    public UserPrincipal(String userName, long accountID, long userID, int accountType, boolean accountAdmin, boolean guestUser, Scenario scenario) {
        this.userName = userName;
        this.accountID = accountID;
        this.userID = userID;
        this.accountType = accountType;
        this.accountAdmin = accountAdmin;
        this.guestUser = guestUser;
        this.scenario = scenario;
    }

    public boolean isGuestUser() {
        return guestUser;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public boolean isAccountAdmin() {
        return accountAdmin;
    }

    public String getName() {
        return userName;
    }

    public String getUserName() {
        return userName;
    }

    public long getAccountID() {
        return accountID;
    }

    public long getUserID() {
        return userID;
    }

    public int getAccountType() {
        return accountType;
    }
}
