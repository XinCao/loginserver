package com.xincao.loginserver.controller;

import com.xincao.common.util.tool.Validate;
import java.util.HashMap;
import java.util.Map;
import com.xincao.loginserver.GameServerInfo;
import com.xincao.loginserver.GameServerTable;
import com.xincao.loginserver.configs.Config;
import com.xincao.loginserver.mapper.AccountMapper;
import com.xincao.loginserver.mapper.AccountTimeMapper;
import com.xincao.loginserver.model.Account;
import com.xincao.loginserver.model.ReconnectingAccount;
import com.xincao.loginserver.network.aion.AionAuthResponse;
import com.xincao.loginserver.network.aion.AionConnection;
import com.xincao.loginserver.network.aion.AionConnection.State;
import com.xincao.loginserver.network.aion.SessionKey;
import com.xincao.loginserver.network.aion.serverpackets.SM_UPDATE_SESSION;
import com.xincao.loginserver.network.gameserver.GsConnection;
import com.xincao.loginserver.network.gameserver.serverpackets.SM_ACCOUNT_AUTH_RESPONSE;
import com.xincao.loginserver.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class is resposible for controlling all account actions
 *
 * @author KID
 * @author SoulKeeper
 */
@Service
public class AccountController {

    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private AccountTimeMapper accountTimeMapper;
    @Autowired
    private BannedIpController bannedIpController;
    @Autowired
    private GameServerTable gameServerTable;
    @Autowired
    private AccountTimeController accountTimeController;

    /**
     * Map with accounts that are active on LoginServer or joined GameServer and
     * are not authenticated yet.
     */
    private Map<Integer, AionConnection> accountsOnLS = new HashMap<Integer, AionConnection>();
    /**
     * Map with accounts that are reconnecting to LoginServer ie was joined
     * GameServer.
     */
    private Map<Integer, ReconnectingAccount> reconnectingAccounts = new HashMap<Integer, ReconnectingAccount>();

    /**
     * Removes account from list of connections
     *
     * @param account account
     */
    public synchronized void removeAccountOnLS(Account account) {
        accountsOnLS.remove(account.getId());
    }

    /**
     * This method is for answering GameServer question about account
     * authentication on GameServer side.
     *
     * @param key
     * @param gsConnection
     */
    public synchronized void checkAuth(SessionKey key, GsConnection gsConnection) {
        AionConnection con = accountsOnLS.get(key.accountId);
        if (con != null) {
            if (con.getSessionKey().checkSessionKey(key)) {
                /**
                 * account is successful logged in on gs remove it from here
                 */
                accountsOnLS.remove(key.accountId);

                GameServerInfo gsi = gsConnection.getGameServerInfo();
                Account acc = con.getAccount();

                /**
                 * Add account to accounts on GameServer list and update
                 * accounts last server
                 */
                gsi.addAccountToGameServer(acc);

                acc.setLastServer(gsi.getId());
                accountMapper.updateAccount(acc);

                /**
                 * Send response to GameServer
                 */
                gsConnection.sendPacket(new SM_ACCOUNT_AUTH_RESPONSE(key.accountId, true, acc.getName(), acc.getAccessLevel(), acc.getMembership()));
            }
        } else {
            gsConnection.sendPacket(new SM_ACCOUNT_AUTH_RESPONSE(key.accountId, false, null, (byte) 0, (byte) 0));
        }
    }

    /**
     * Add account to reconnectionAccount list
     *
     * @param acc
     */
    public synchronized void addReconnectingAccount(ReconnectingAccount acc) {
        reconnectingAccounts.put(acc.getAccount().getId(), acc);
    }

    /**
     * Check if reconnecting account may auth.
     *
     * @param accountId id of account
     * @param loginOk loginOk
     * @param reconnectKey reconnect key
     * @param client aion client
     */
    public synchronized void authReconnectingAccount(int accountId, int loginOk, int reconnectKey, AionConnection client) {
        ReconnectingAccount reconnectingAccount = reconnectingAccounts.remove(accountId);
        if (reconnectingAccount != null && reconnectingAccount.getReconnectionKey() == reconnectKey) {
            Account acc = reconnectingAccount.getAccount();
            client.setAccount(acc);
            accountsOnLS.put(acc.getId(), client);
            client.setState(State.AUTHED_LOGIN);
            client.setSessionKey(new SessionKey(client.getAccount()));
            client.sendPacket(new SM_UPDATE_SESSION(client.getSessionKey()));
        } else {
            client.close( /* new SM_UPDATE_SESSION, */true);
        }
    }

    /**
     * Tries to authentificate account.<br>
     * If success returns {@link AionAuthResponse#AUTHED} and sets account
     * object to connection.<br>
     *
     * If {@link com.aionemu.loginserver.configs.Config#ACCOUNT_AUTO_CREATION}
     * is enabled - creates new account.<br>
     *
     * @param name name of account
     * @param password password of account
     * @param connection connection for account
     * @return Response with error code
     */
    public AionAuthResponse login(String name, String password, AionConnection connection) {
        Account account = loadAccount(name);
        // Try to create new account
        if (account == null && Config.ACCOUNT_AUTO_CREATION) {
            account = createAccount(name, password);
        }
        // If account not found and not created
        if (account == null) {
            return AionAuthResponse.INVALID_PASSWORD;
        }
        // check for paswords beeing equals
        if (!account.getPasswordHash().equals(AccountUtils.encodePassword(password))) {
            return AionAuthResponse.INVALID_PASSWORD;
        }

        // check for paswords beeing equals
        if (account.getActivated() != 1) {
            return AionAuthResponse.INVALID_PASSWORD;
        }

        // If account expired
        if (AccountTimeController.isAccountExpired(account)) {
            return AionAuthResponse.TIME_EXPIRED;
        }

        // if account is banned
        if (AccountTimeController.isAccountPenaltyActive(account)) {
            return AionAuthResponse.BAN_IP;
        }

        // if account is restricted to some ip or mask
        if (account.getIpForce() != null) {
            if (!Validate.checkIPMatching(account.getIpForce(), connection.getIP())) {
                return AionAuthResponse.BAN_IP;
            }
        }

        // if ip is banned
        if (bannedIpController.isBanned(connection.getIP())) {
            return AionAuthResponse.BAN_IP;
        }

        // Do not allow to login two times with same account
        synchronized (AccountController.class) {
            if (gameServerTable.isAccountOnAnyGameServer(account)) {
                gameServerTable.kickAccountFromGameServer(account);
                return AionAuthResponse.ALREADY_LOGGED_IN;
            }

            // If someone is at loginserver, he should be disconnected
            if (accountsOnLS.containsKey(account.getId())) {
                AionConnection aionConnection = accountsOnLS.remove(account.getId());

                aionConnection.close(true);

                return AionAuthResponse.ALREADY_LOGGED_IN;
            } else {
                connection.setAccount(account);
                accountsOnLS.put(account.getId(), connection);
            }
        }

        accountTimeController.updateOnLogin(account);

        // if everything was OK
        accountMapper.updateAccount(account);

        return AionAuthResponse.AUTHED;
    }

    /**
     * Loads account from DB and returns it, or returns null if account was not
     * loaded
     *
     * @param name acccount name
     * @return loaded account or null
     */
    public Account loadAccount(String name) {
        Account account = accountMapper.getAccount(name);
        if (account != null) {
            account.setAccountTime(accountTimeMapper.getAccountTime(account.getId()));
        }
        return account;
    }

    /**
     * Creates new account and stores it in DB. Returns account object in case
     * of success or null if failed
     *
     * @param name account name
     * @param password account password
     * @return account object or null
     */
    public Account createAccount(String name, String password) {
        String passwordHash = AccountUtils.encodePassword(password);
        Account account = new Account();
        account.setName(name);
        account.setPasswordHash(passwordHash);
        account.setAccessLevel((byte) 0);
        account.setMembership((byte) 0);
        account.setActivated((byte) 1);
        if (accountMapper.insertAccount(account)) {
            return account;
        } else {
            return null;
        }
    }
}