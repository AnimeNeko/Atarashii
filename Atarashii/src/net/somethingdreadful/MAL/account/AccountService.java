package net.somethingdreadful.MAL.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;

public class AccountService extends Service {
    private Authenticator mAuthenticator;

    /**
     * Get the provider whose behavior is being controlled.
     *
     * @return String The provider
     */
    public static String getAuth(Context context) throws PackageManager.NameNotFoundException {
        PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        if (TextUtils.isDigitsOnly(pInfo.versionName.replace(".", "")))
            return ".account.Provider";
        else
            return ".beta.account.Provider";
    }

    /**
     * Get the username of an account.
     *
     * @param context The activity context
     * @return String The username
     */
    public static String getUsername(Context context) {
        String username = getAccount(context).name;
        Crashlytics.setUserName(username);
        return username;
    }

    /**
     * Get the password of an account.
     *
     * @param context The activity context
     * @return String The password
     */
    public static String getPassword(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = getAccount(context);
        return accountManager.getPassword(account);
    }

    /**
     * Get an Account on the device.
     *
     * @param context The activity context
     * @return Account The account
     */
    public static Account getAccount(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] account = accountManager.getAccountsByType(".account.SyncAdapter.account");
        return (account.length > 0 ? account[0] : null);
    }

    /**
     * Removes an account from the accountmanager.
     *
     * @param context The activity context
     */
    public static void deleteAccount(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        accountManager.removeAccount(getAccount(context), null, null);
    }

    /**
     * Add an account in the accountmanager.
     *
     * @param context  The activity context
     * @param username The username of the account that will be saved
     * @param password The password of the account that will be saved
     */
    public static void addAccount(Context context, String username, String password) {
        AccountManager accountManager = AccountManager.get(context);
        final Account account = new Account(username, ".account.SyncAdapter.account");
        accountManager.addAccountExplicitly(account, password, null);
    }

    /**
     * Update a password of an account.
     *
     * @param context  The activity context
     * @param password The new password for an account
     */
    public static void updatePassword(Context context, String password) {
        AccountManager accountManager = AccountManager.get(context);
        accountManager.setPassword(getAccount(context), password);
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