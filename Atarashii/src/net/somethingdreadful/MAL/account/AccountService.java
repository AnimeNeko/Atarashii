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
import com.freshdesk.mobihelp.Mobihelp;

import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.database.DatabaseHelper;

public class AccountService extends Service {
    public static AccountType accountType;
    private static Account account;
    private static Context context;
    private Authenticator mAuthenticator;
    /**
     * The account version will be used to peform
     */
    private static final int accountVersion = 2;

    public static void create(Context context) {
        AccountService.context = context;
    }

    /**
     * This is used for Account upgrade purpose
     */
    private static void onUpgrade() {
        Crashlytics.log(Log.INFO, "MALX", "AccountService.onUpgrade(): Upgrading to " + String.valueOf(accountVersion) + ".");
        setAccountVersion(accountVersion);
        switch (accountVersion) {
            case 1:
                // We support now all Anilist scores, the user needs to log out (2.1 beta 3).
                if (!accountType.equals(AccountType.MyAnimeList))
                    deleteAccount();
            case 2:
                // We added new base models to make loading easier, the user needs to log out (2.2 beta 1).
                deleteAccount();
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
        Mobihelp.setUserFullName(context, username);
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

    /**
     * Check if an account exists.
     *
     * @param context The context
     * @return boolean if there is an account
     */
    public static boolean AccountExists(Context context) {
        return AccountManager.get(context).getAccountsByType(".account.SyncAdapter.account").length > 0;
    }

    /**
     * Get an Account on the device.
     *
     * @return Account The account
     */
    public static Account getAccount() {
        if (account == null) {
            AccountManager accountManager = AccountManager.get(context);
            Account[] myaccount = accountManager.getAccountsByType(".account.SyncAdapter.account");
            String version = String.valueOf(accountVersion);
            if (myaccount.length > 0) {
                accountType = getAccountType(accountManager.getUserData(myaccount[0], "accountType"));
                version = accountManager.getUserData(myaccount[0], "accountVersion");
                Theme.setCrashData("Site", AccountService.accountType.toString());
                Theme.setCrashData("accountVersion", version);
            }
            account = myaccount.length > 0 ? myaccount[0] : null;
            if (version == null || accountVersion != Integer.parseInt(version))
                onUpgrade();
        }
        return account;
    }

    public static boolean isMAL() {
        try {
            getAccount();
            return accountType.equals(AccountType.MyAnimeList);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        return accountType.equals(AccountType.MyAnimeList);
    }

    /**
     * Get the authtoken with the given string.
     *
     * @param type The authToken string
     * @return AccountType The type of account
     */
    private static AccountType getAccountType(String type) {
        if (AccountType.AniList.toString().equals(type))
            return AccountType.AniList;
        else
            return AccountType.MyAnimeList;
    }

    /**
     * Removes an account from the accountmanager.
     */
    public static void deleteAccount() {
        AccountManager accountManager = AccountManager.get(context);
        accountManager.removeAccount(getAccount(), null, null);
        account = null;
    }

    /**
     * Add an account in the accountmanager.
     *
     * @param username The username of the account that will be saved
     * @param password The password of the account that will be saved
     */
    public static void addAccount(String username, String password, AccountType accountType) {
        AccountManager accountManager = AccountManager.get(context);
        final Account account = new Account(username, ".account.SyncAdapter.account");
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
            Crashlytics.log(Log.INFO, "MALX", "AccountService: The accestoken will expire in " + Long.toString(timeLeft / 60) + " minutes.");
            return timeLeft >= 0 ? token : null;
        } catch (Exception e) {
            Crashlytics.log(Log.INFO, "MALX", "AccountService: The expire time could not be received.");
            return null;
        }
    }

    /**
     * Set an auth token in the accountmanager.
     *
     * @param accountVersion The new accountversion of the account that will be saved
     */
    private static void setAccountVersion(int accountVersion) {
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
        Mobihelp.clearUserData(context);
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