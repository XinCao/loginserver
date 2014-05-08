package com.xincao.loginserver.mapper;

import com.xincao.loginserver.model.Account;

/**
 *
 * @author caoxin
 */
public interface AccountMapper {

    public Account getAccount(String name);

    /**
     * Retuns account id or -1 in case of error
     *
     * @param name name of account
     * @return id or -1 in case of error
     */
    public int getAccountId(String name);

    /**
     * Reruns account count If error occured - returns -1
     *
     * @return account count
     */
    public int getAccountCount();

    /**
     * Inserts new account to database. Sets account ID to id that was generated
     * by DB.
     *
     * @param account account to insert
     * @return true if was inserted, false in other case
     */
    public boolean insertAccount(Account account);

    /**
     * Updates account in database
     *
     * @param account account to update
     * @return true if was updated, false in other case
     */
    public boolean updateAccount(Account account);

    /**
     * Updates lastServer field of account
     *
     * @param accountId account id
     * @param lastServer last accessed server
     * @return was updated successful or not
     */
    public boolean updateLastServer(int accountId, byte lastServer);

    /**
     * Updates last ip that was used to access an account
     *
     * @param accountId account id
     * @param ip ip address
     * @return was update successful or not
     */
    public boolean updateLastIp(int accountId, String ip);

}