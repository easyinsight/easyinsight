package com.easyinsight.users;

import com.easyinsight.database.Database;
import com.easyinsight.security.PasswordService;
import com.easyinsight.security.UserPrincipal;
import com.easyinsight.security.SecurityUtil;
import com.easyinsight.security.DefaultSecurityProvider;
import com.easyinsight.logging.LogClass;
import com.easyinsight.email.UserStub;
import com.easyinsight.email.AccountMemberInvitation;
import com.easyinsight.util.RandomTextGenerator;
import com.easyinsight.groups.Group;
import com.easyinsight.groups.GroupStorage;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import flex.messaging.FlexContext;

/**
 * User: jboe
 * Date: Jan 2, 2008
 * Time: 5:34:56 PM
 */
public class UserService implements IUserService {

    public void salesRequest(String userName, String email, String company, String additionalInfo) {
        try {
            new AccountMemberInvitation().salesNotification(userName, email, company, additionalInfo);
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public AccountAPISettings regenerateSecretKey() {
        long userID = SecurityUtil.getUserID();
        Session session = Database.instance().createSession();
        try {
            session.getTransaction().begin();
            User user = (User) session.createQuery("from User where userID = ?").setLong(0, userID).list().get(0);
            String accountSecretKey = RandomTextGenerator.generateText(16);
            user.setUserSecretKey(accountSecretKey);
            session.save(user);
            AccountAPISettings settings = new AccountAPISettings(user.getUserKey(), user.getUserSecretKey(),
                    user.getAccount().isUncheckedAPIEnabled(), user.getAccount().isValidatedAPIEnabled(),
                    user.getAccount().isDynamicAPIAllowed());
            session.getTransaction().commit();
            return settings;
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    public boolean remindPassword(String emailAddress) {
        boolean success;
        Session session = Database.instance().createSession();
        try {
            session.getTransaction().begin();
            List results = session.createQuery("from User where email = ?").setString(0, emailAddress).list();
            if (results.size() == 0) {
                success = false;
            } else {
                User user = (User) results.get(0);
                String password = RandomTextGenerator.generateText(12);
                user.setPassword(password);
                new AccountMemberInvitation().resetPassword(emailAddress, user.getPassword());
                success = true;
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
        return success;
    }

    public boolean remindUserName(String emailAddress) {
        boolean success;
        Session session = Database.instance().createSession();
        try {
            session.getTransaction().begin();
            List results = session.createQuery("from User where email = ?").setString(0, emailAddress).list();
            if (results.size() == 0) {
                success = false;
            } else {
                User user = (User) results.get(0);
                new AccountMemberInvitation().remindUserName(emailAddress, user.getUserName());
                success = true;
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
        return success;
    }


    @Nullable
    public String doesUserExist(String userName, String email) {
        Session session = Database.instance().createSession();
        String message = null;
        List results;
        try {
            session.beginTransaction();
            results = session.createQuery("from User where userName = ?").setString(0, userName).list();
            if (results.size() > 0) {
                message = "A user already exists by that name.";
            } else {
                results = session.createQuery("from User where email = ?").setString(0, email).list();
                if (results.size() > 0) {
                    message = "That email address is already used.";
                }
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
        return message;
    }

    public boolean doesAccountExist(String accountName) {
        Session session = Database.instance().createSession();
        List results;
        try {
            session.beginTransaction();
            results = session.createQuery("from Account where name = ?").setString(0, accountName).list();
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
        return (results.size() > 0);
    }

    private User retrieveUser() {
        long userID = SecurityUtil.getUserID();
        try {
            User user = null;
            Session session = Database.instance().createSession();
            List results;
            try {
                session.beginTransaction();
                results = session.createQuery("from User where userID = ?").setLong(0, userID).list();
                session.getTransaction().commit();
            } catch (Exception e) {
                session.getTransaction().rollback();
                throw new RuntimeException(e);
            } finally {
                session.close();
            }
            if (results.size() > 0) {
                user = (User) results.get(0);
                user.setLicenses(new ArrayList<SubscriptionLicense>());
            }
            return user;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }    

    public void updateUserLabels(String userName, String fullName, String email) {
        User user = retrieveUser();
        if (SecurityUtil.getAccountID() != user.getAccount().getAccountID()) {
            throw new SecurityException();
        }
        if (!SecurityUtil.isAccountAdmin() && (SecurityUtil.getUserID() != user.getUserID())) {
            throw new SecurityException();
        }
        user.setUserName(userName);
        user.setName(fullName);
        user.setEmail(email);
        Session session = Database.instance().createSession();
        try {
            session.beginTransaction();
            session.saveOrUpdate(user);
            session.getTransaction().commit();
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    public boolean updatePassword(String existingPassword, String password) {
        Session session = Database.instance().createSession();
        try {
            session.beginTransaction();
            User user = (User) session.createQuery("from User where userID = ?").setLong(0, SecurityUtil.getUserID()).list().get(0);
            if (!PasswordService.getInstance().encrypt(existingPassword).equals(user.getPassword())) {
                return false;
            }
            user.setPassword(PasswordService.getInstance().encrypt(password));
            session.update(user);
            session.getTransaction().commit();
            return true;
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    /*public void addLicenses(long feedID, int numberOfLicenses) {
        long accountID = SecurityUtil.getAccountID();
        Account account = getAccount(accountID);
        for (int i = 0; i < numberOfLicenses; i++) {
            SubscriptionLicense subscriptionLicense = new SubscriptionLicense();
            subscriptionLicense.setFeedID(feedID);
            account.addLicense(subscriptionLicense);
        }
        updateAccount(account);
    } */

    public long createAccount(UserTransferObject userTransferObject, AccountTransferObject accountTransferObject, String password) {
        return createAccount(userTransferObject, accountTransferObject, password, null);
    }

    public long createAccount(UserTransferObject userTransferObject, AccountTransferObject accountTransferObject, String password, String sourceURL) {
        Connection conn = Database.instance().getConnection();
        Session session = Database.instance().createSession(conn);
        try {
            conn.setAutoCommit(false);
            Account account = accountTransferObject.toAccount();
            configureNewAccount(account);
            User user = createInitialUser(userTransferObject, password, account);
            account.addUser(user);
            session.save(account);
            user.setAccount(account);
            session.update(user);
            if (account.getAccountType() == Account.PROFESSIONAL || account.getAccountType() == Account.ENTERPRISE) {
                Group group = new Group();
                group.setName(account.getName());
                group.setPubliclyVisible(false);
                group.setPubliclyJoinable(false);
                group.setDescription("This group was automatically created to act as a location for exposing data to all users in the account.");
                account.setGroupID(new GroupStorage().addGroup(group, user.getUserID(), conn));
                session.update(account);
            }
            //if (account.getAccountType() == Account.FREE || account.getAccountType() == Account.INDIVIDUAL) {
            String activationKey = RandomTextGenerator.generateText(12);
            PreparedStatement insertActivationStmt = conn.prepareStatement("INSERT INTO ACCOUNT_ACTIVATION (ACCOUNT_ID, ACTIVATION_KEY, CREATION_DATE, target_url) VALUES (?, ?, ?, ?)");
            insertActivationStmt.setLong(1, account.getAccountID());
            insertActivationStmt.setString(2, activationKey);
            insertActivationStmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            insertActivationStmt.setString(4, sourceURL);
            insertActivationStmt.execute();
            new AccountActivityStorage().saveAccountActivity(new AccountActivity(account.getAccountType(),
                    new Date(), account.getAccountID(), 0, AccountActivity.ACCOUNT_CREATED, "", 0, 0, Account.INACTIVE), conn);
            //}
            session.flush();
            conn.commit();
            if (SecurityUtil.getSecurityProvider() instanceof DefaultSecurityProvider) {
                new AccountMemberInvitation().sendActivationEmail(user.getEmail(), activationKey);
            }
            return account.getAccountID();
        } catch (Exception e) {
            LogClass.error(e);
            try {
                conn.rollback();
            } catch (SQLException e1) {
                LogClass.error(e1);
            }
            throw new RuntimeException(e);
        } finally {
            session.close();
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LogClass.error(e);
            }
            Database.instance().closeConnection(conn);
        }
    }

    private User createInitialUser(UserTransferObject userTransferObject, String password, Account account) {
        User user = new User();
        user.setAccountAdmin(userTransferObject.isAccountAdmin());
        user.setAccount(account);
        user.setDataSourceCreator(userTransferObject.isDataSourceCreator());
        user.setEmail(userTransferObject.getEmail());
        user.setInsightCreator(userTransferObject.isInsightCreator());
        user.setName(userTransferObject.getName());
        user.setUserName(userTransferObject.getUserName());
        user.setPassword(PasswordService.getInstance().encrypt(password));
        return user;
    }

    private void configureNewAccount(Account account) {
        account.setAccountState(Account.INACTIVE);
        if (account.getAccountType() == Account.ENTERPRISE) {
            account.setMaxUsers(500);
            account.setMaxSize(1000000000);
        } else if (account.getAccountType() == Account.PROFESSIONAL) {
            account.setMaxUsers(50);
            account.setMaxSize(200000000);
        } else if (account.getAccountType() == Account.INDIVIDUAL) {
            account.setMaxUsers(1);
            account.setMaxSize(20000000);
        } else if (account.getAccountType() == Account.FREE) {
            account.setMaxUsers(1);
            account.setMaxSize(1000000);
        } else if (account.getAccountType() == Account.GROUP) {
            account.setMaxUsers(10);
            account.setMaxSize(10000000);
        }
    }

    /*public Account getAccount(long accountID) {
        Session session = Database.instance().createSession();
        try {
            session.beginTransaction();
            List results = session.createQuery("from Account where accountID = ?").setLong(0, accountID).list();
            Account account = (Account) results.get(0);
            account.setLicenses(new ArrayList<SubscriptionLicense>(account.getLicenses()));
            for (User user : account.getUsers()) {
                List<SubscriptionLicense> userLicenses = new ArrayList<SubscriptionLicense>(user.getLicenses());
                user.setLicenses(userLicenses);            
            }
            session.getTransaction().commit();
            return account;
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }*/

    /*public void createAccount(Account account) {
        Session session = Database.instance().createSession();
        try {
            session.beginTransaction();
            session.save(account);
            session.getTransaction().commit();
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }*/

    

    public List<ConsultantTO> getConsultants() {
        SecurityUtil.authorizeAccountAdmin();
        List<ConsultantTO> consultants = new ArrayList<ConsultantTO>();
        long accountID = SecurityUtil.getAccountID();
        Session session = Database.instance().createSession();
        try {
            session.getTransaction().begin();
            List results = session.createQuery("from Account where accountID = ?").setLong(0, accountID).list();
            Account account = (Account) results.get(0);
            for (Consultant consultant : account.getGuestUsers()) {
                consultants.add(consultant.toConsultantTO());
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
        return consultants;
    }

    public UserCreationResponse addConsultant(UserTransferObject userTransferObject) {
        SecurityUtil.authorizeAccountAdmin();
        long accountID = SecurityUtil.getAccountID();
        String message = doesUserExist(userTransferObject.getUserName(), userTransferObject.getEmail());
        UserCreationResponse userCreationResponse;
        if (message != null) {
            userCreationResponse = new UserCreationResponse(message);
        } else {
            Session session = Database.instance().createSession();
            try {
                session.getTransaction().begin();
                List results = session.createQuery("from Account where accountID = ?").setLong(0, accountID).list();
                Account account = (Account) results.get(0);
                User user = userTransferObject.toUser();
                user.setAccount(account);
                session.save(user);
                Consultant consultant = new Consultant();
                consultant.setUser(user);
                consultant.setState(Consultant.PENDING_EI_APPROVAL);
                session.save(consultant);
                session.getTransaction().commit();
                userCreationResponse = new UserCreationResponse(user.getUserID());
            } catch (Exception e) {
                LogClass.error(e);
                session.getTransaction().rollback();
                throw new RuntimeException(e);
            } finally {
                session.close();
            }
        }
        return userCreationResponse;
    }


    public void deactivateConsultant(long consultantID) {
        SecurityUtil.authorizeAccountAdmin();
        Session session = Database.instance().createSession();
        try {
            session.getTransaction().begin();
            List results = session.createQuery("from Consultant where guestUserID = ?").setLong(0, consultantID).list();
            Consultant consultant = (Consultant) results.get(0);
            consultant.setState(Consultant.DISABLED);
            session.update(consultant);
            session.getTransaction().commit();
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    public void removeConsultant(long consultantID) {
        SecurityUtil.authorizeAccountAdmin();
        Session session = Database.instance().createSession();
        try {
            session.getTransaction().begin();
            List results = session.createQuery("from Consultant where guestUserID = ?").setLong(0, consultantID).list();
            Consultant consultant = (Consultant) results.get(0);
            session.delete(consultant);
            session.getTransaction().commit();
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    
    public AccountAPISettings getUserAPISettings() {
        AccountAPISettings accountAPISettings;
        long userID = SecurityUtil.getUserID();
        Session session = Database.instance().createSession();
        try {
            session.getTransaction().begin();
            List results = session.createQuery("from User where userID = ?").setLong(0, userID).list();
            User user = (User) results.get(0);
            Account account = user.getAccount();
            boolean changed = false;
            if (user.getUserKey() == null) {
                String accountKey = RandomTextGenerator.generateText(12);
                user.setUserKey(accountKey);
                changed = true;
            }
            if (user.getUserSecretKey() == null) {
                String accountSecretKey = RandomTextGenerator.generateText(16);
                user.setUserSecretKey(accountSecretKey);
                changed = true;
            }
            if (changed) {
                session.update(account);
            }
            accountAPISettings = new AccountAPISettings(user.getUserKey(), user.getUserSecretKey(),
                    account.isUncheckedAPIEnabled(), account.isValidatedAPIEnabled(), account.isDynamicAPIAllowed());
            session.getTransaction().commit();
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
        return accountAPISettings;
    }

    



    public void deleteAccount() {
        long accountID = SecurityUtil.getAccountID();
        Session session = Database.instance().createSession();
        try {
            session.beginTransaction();
            Account account = (Account) session.createQuery("from Account where accountID = ?").setLong(0, accountID).list().get(0);
            session.delete(account);
            session.getTransaction().commit();
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    public UserServiceResponse isSessionLoggedIn() {
        UserPrincipal existing = (UserPrincipal) FlexContext.getFlexSession().getUserPrincipal();
        if (existing == null) {
            return null;
        } else {
            User user = retrieveUser();
            Account account = user.getAccount();
            return new UserServiceResponse(true, user.getUserID(), user.getAccount().getAccountID(), user.getName(),
                                account.getAccountType(), account.getMaxSize(), user.getEmail(), user.getUserName(),
                                user.getPassword(), user.isAccountAdmin(), user.isDataSourceCreator(), user.isInsightCreator());
        }
    }

    public UserStub getUserStub(String userName) {
        UserStub userStub = null;
        Session session = Database.instance().createSession();
        try {
            session.getTransaction().begin();
            List results = session.createQuery("from User where userName = ?").setString(0, userName).list();
            if (results.size() > 0) {
                User user = (User) results.get(0);
                userStub = new UserStub(user.getUserID(), user.getUserName(), user.getEmail(), user.getName());
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
        return userStub;
    }

    public UserServiceResponse authenticateWithEncrypted(String userName, String encryptedPassword) {
        try {
            UserServiceResponse userServiceResponse;
            Session session = Database.instance().createSession();
            List results;
            try {
                session.getTransaction().begin();
                results = session.createQuery("from User where userName = ?").setString(0, userName).list();
                if (results.size() > 0) {
                    User user = (User) results.get(0);
                    String actualPassword = user.getPassword();
                    if (encryptedPassword.equals(actualPassword)) {
                        List accountResults = session.createQuery("from Account where accountID = ?").setLong(0, user.getAccount().getAccountID()).list();
                        Account account = (Account) accountResults.get(0);
                        if (account.getAccountState() == Account.ACTIVE || account.getAccountState() == Account.TRIAL) {
                            userServiceResponse = new UserServiceResponse(true, user.getUserID(), user.getAccount().getAccountID(), user.getName(),
                                user.getAccount().getAccountType(), account.getMaxSize(), user.getEmail(), user.getUserName(), encryptedPassword, user.isAccountAdmin(), user.isDataSourceCreator(), user.isInsightCreator());
                        } else {
                            userServiceResponse = new UserServiceResponse(false, "Your account is not active.");
                        }
                    } else {
                        userServiceResponse = new UserServiceResponse(false, "Incorrect password, please try again.");
                    }
                } else {
                    userServiceResponse = new UserServiceResponse(false, "Incorrect user name, please try again.");
                }
                session.getTransaction().commit();
            } finally {
                session.close();
            }
            return userServiceResponse;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public UserServiceResponse authenticate(String userName, String password) {
        try {
            UserServiceResponse userServiceResponse;
            Session session = Database.instance().createSession();
            List results;
            try {
                session.getTransaction().begin();
                results = session.createQuery("from User where userName = ?").setString(0, userName).list();
                if (results.size() > 0) {
                    User user = (User) results.get(0);
                    String actualPassword = user.getPassword();
                    String encryptedPassword = PasswordService.getInstance().encrypt(password);
                    if (encryptedPassword.equals(actualPassword)) {
                        List accountResults = session.createQuery("from Account where accountID = ?").setLong(0, user.getAccount().getAccountID()).list();
                        Account account = (Account) accountResults.get(0);
                        if (account.getAccountState() == Account.ACTIVE || account.getAccountState() == Account.TRIAL) {
                            userServiceResponse = new UserServiceResponse(true, user.getUserID(), user.getAccount().getAccountID(), user.getName(),
                                user.getAccount().getAccountType(), account.getMaxSize(), user.getEmail(), user.getUserName(), encryptedPassword, user.isAccountAdmin(), user.isDataSourceCreator(), user.isInsightCreator());
                        } else {
                            userServiceResponse = new UserServiceResponse(false, "Your account is not active.");
                        }
                        // FlexContext.getFlexSession().getRemoteCredentials();
                    } else {
                        userServiceResponse = new UserServiceResponse(false, "Incorrect password, please try again.");
                    }
                } else {
                    userServiceResponse = new UserServiceResponse(false, "Incorrect user name, please try again.");
                }
                session.getTransaction().commit();
            } finally {
                session.close();
            }
            return userServiceResponse;
        } catch (Exception e) {
            LogClass.error(e);
            throw new RuntimeException(e);
        }
    }

    public AccountTransferObject retrieveAccount() {
        long accountID = SecurityUtil.getAccountID();
        Session session = Database.instance().createSession();
        try {
            session.beginTransaction();
            List results = session.createQuery("from Account where accountID = ?").setLong(0, accountID).list();
            Account account = (Account) results.get(0);
            AccountTransferObject accountTransferObject = account.toTransferObject();
            session.getTransaction().commit();
            return accountTransferObject;
        } catch (Exception e) {
            LogClass.error(e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }
}
