package com.xincao.loginserver.mapper;

import com.xincao.loginserver.model.AccountTime;

/**
 *
 * @author caoxin
 */
public interface AccountTimeMapper {

    /**
     * Updates @link com.aionemu.loginserver.model.AccountTime data of account
     *
     * @param accountId account id
     * @param accountTime account time set
     * @return was update successfull or not
     */
    public abstract boolean updateAccountTime(int accountId, AccountTime accountTime);

    /**
     * Updates @link com.aionemu.loginserver.model.AccountTime data of account
     *
     * @param accountId
     * @return AccountTime
     */
    public abstract AccountTime getAccountTime(int accountId);
}