package net.somethingdreadful.MAL.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.ContentManager;
import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.database.DatabaseHelper;

import static net.somethingdreadful.MAL.account.AccountType.AniList;
import static net.somethingdreadful.MAL.account.AccountType.MyAnimeList;

public class AccountService extends Service {
    public static AccountType accountType;
    private static Account account;
    private static Context context;
    private Authenticator mAuthenticator;
    /**
     * The account version will be used to peform
     */
    private static final int accountVersion = 3;

    public static void create(Context context) {
        AccountService.context = context;
    }

    /**
     * This is used for Account upgrade purpose
     */
    private static void onUpgrade(int oldVersion) {
        Crashlytics.log(Log.INFO, "Atarashii", "AccountService.onUpgrade(): Upgrading from " + oldVersion + " to " + String.valueOf(accountVersion) + ".");
        setAccountVersion();
        switch (oldVersion + 1) {
            case 1:
            case 2: // We added new base models to make loading easier, the user needs to log out (2.2 beta 1).
                deleteAccount();
                break;
            case 3: // The profile image is now saved in the settings
                ContentManager cManager = new ContentManager(context);
                if (!PrefManager.isCreated())
                    PrefManager.create(context);
                Profile profile = cManager.getProfileFromDB();
                if (profile != null && profile.getImageUrl() != null) {
                    PrefManager.setProfileImage(profile.getImageUrl());
                    PrefManager.commitChanges();
                }
                break;
        }
    }

    /**
     * Get the username of an account.
     *
     * @return String The username
     */
    public static String getUsername() {
        if (getAccount() == null)
            return null;
        String username = getAccount().name;
        Crashlytics.setUserName(username);
        return username;
    }

    /**
     * Get the password of an account.
     *
     * @return String The password
     */
    public static String getPassword() {
        Account account = getAccount();
        if (account == null)
            return null;
        AccountManager accountManager = AccountManager.get(context);
        return accountManager.getPassword(account);
    }

    private static String getAccountType() {
        return context.getPackageName().contains("beta") ? ".beta.account.SyncAdapter.account" : ".account.SyncAdapter.account";
    }

    /**
     * Check if an account exists.
     *
     * @param context The context
     * @return boolean if there is an account
     */
    public static boolean AccountExists(Context context) {
        return AccountManager.get(context).getAccountsByType(getAccountType()).length > 0;
    }

    /**
     * Get an Account on the device.
     *
     * @return Account The account
     */
    public static Account getAccount() {
        if (account == null) {
            AccountManager accountManager = AccountManager.get(context);
            Account[] myaccount = accountManager.getAccountsByType(getAccountType());
            String version = String.valueOf(accountVersion);
            if (myaccount.length > 0) {
                accountType = getAccountType(accountManager.getUserData(myaccount[0], "accountType"));
                version = accountManager.getUserData(myaccount[0], "accountVersion");
                Theme.setCrashData("Site", AccountService.accountType.toString());
                Theme.setCrashData("accountVersion", version);
            }
            account = myaccount.length > 0 ? myaccount[0] : null;
            if (version == null)
                onUpgrade(1);
            else if (Integer.parseInt(version) != accountVersion)
                onUpgrade(Integer.parseInt(version));
        }
        return account;
    }

    public static boolean isMAL() {
        getAccount();
        if (account == null || accountType == null) {
            AccountService.deleteAccount();
            System.exit(0);
        }

        switch (accountType) {
            case MyAnimeList:
                return true;
            case AniList:
                return false;
        }
        return false;
    }

    /**
     * Get the authtoken with the given string.
     *
     * @param type The authToken string
     * @return AccountType The type of account
     */
    private static AccountType getAccountType(String type) {
        if (AccountType.AniList.toString().equals(type))
            return AniList;
        else
            return MyAnimeList;
    }

    /**
     * Removes an account from the accountmanager.
     */
    public static void deleteAccount() {
        AccountManager accountManager = AccountManager.get(context);
        account = null;
        if (getAccount() != null)
            accountManager.removeAccount(getAccount(), null, null);
        accountType = null;
    }

    /**
     * Add an account in the accountmanager.
     *
     * @param username The username of the account that will be saved
     * @param password The password of the account that will be saved
     */
    public static void addAccount(String username, String password, AccountType accountType) {
        AccountManager accountManager = AccountManager.get(context);
        final Account account = new Account(username, getAccountType());
        accountManager.addAccountExplicitly(account, password, null);
        accountManager.setUserData(account, "accountType", accountType.toString());
        accountManager.setUserData(account, "accountVersion", String.valueOf(accountVersion));
        AccountService.accountType = accountType;
    }

    /**
     * Add an accesToken to the Account data.
     *
     * @param token The AccesToken which should be stored
     * @param time  The time till the token will expire
     * @return String The token
     */
    public static String setAccesToken(String token, Long time) {
        AccountManager accountManager = AccountManager.get(context);
        accountManager.setUserData(getAccount(), "accesToken", token);
        accountManager.setUserData(getAccount(), "accesTokenTime", Long.toString((System.currentTimeMillis() / 1000) + (time - 60)));
        return token;
    }

    /**
     * Get the accesToken.
     * <p/>
     * Note: this method will return null if the accesToken is expired!
     *
     * @return String accesToken
     */
    public static String getAccesToken() {
        AccountManager accountManager = AccountManager.get(context);
        String token = accountManager.getUserData(getAccount(), "accesToken");
        try {
            Long expireTime = Long.parseLong(accountManager.getUserData(getAccount(), "accesTokenTime"));
            Long time = System.currentTimeMillis() / 1000;
            Long timeLeft = expireTime - time;
            Crashlytics.log(Log.INFO, "Atarashii", "AccountService: The accestoken will expire in " + Long.toString(timeLeft / 60) + " minutes.");
            return timeLeft >= 0 ? token : null;
        } catch (Exception e) {
            Crashlytics.log(Log.INFO, "Atarashii", "AccountService: The expire time could not be received.");
            return null;
        }
    }

    /**
     * Set an auth token in the accountmanager.
     */
    private static void setAccountVersion() {
        if (account != null) {
            AccountManager accountManager = AccountManager.get(context);
            accountManager.setUserData(account, "accountVersion", String.valueOf(accountVersion));
        }
    }

    /**
     * Set an auth token in the accountmanager.
     *
     * @param refreshToken The auth token of the account that will be saved
     */
    public static void setRefreshToken(String refreshToken) {
        AccountManager accountManager = AccountManager.get(context);
        accountManager.setUserData(getAccount(), "refreshToken", refreshToken);
    }

    /**
     * Set an refresh token in the accountmanager.
     */
    public static String getRefreshToken() {
        AccountManager accountManager = AccountManager.get(context);
        return accountManager.getUserData(getAccount(), "refreshToken");
    }

    /**
     * Removes the userdata
     */
    public static void clearData() {
        DatabaseHelper.deleteDatabase(context);
        PrefManager.clear();
        AccountService.deleteAccount();
    }

    @Override
    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

    public class Authenticator extends AbstractAccountAuthenticator {
        public Authenticator(Context context) {
            super(context);
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String s, String s2, String[] strings, Bundle bundle) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getAuthTokenLabel(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }
    }
}