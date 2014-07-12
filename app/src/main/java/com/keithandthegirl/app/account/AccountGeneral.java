package com.keithandthegirl.app.account;

import android.accounts.Account;

/**
 * Created by dmfrey on 5/28/14.
 */
public class AccountGeneral {

    /**
     * Account type id
     */
    public static final String ACCOUNT_TYPE = "com.keithandthegirl.app";

    /**
     * Account name
     */
    public static final String ACCOUNT_NAME = "KATG";

    /**
     * Auth token types
     */
    public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to a Keith and the Girl Account";

    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to a Keith and the Girl VIP Account";

    /**
     * KATG Specific Keys
     */
    public static final String KATG_VIP_UID = "KatgVip_uid";
    public static final String KATG_VIP_KEY = "KatgVip_key";

    public static final ServerAuthenticate sServerAuthenticate = new KatgServerAuthenticate();

    public static final Account dummyAccount() {
        return new Account( ACCOUNT_NAME, ACCOUNT_TYPE );
    }

}
