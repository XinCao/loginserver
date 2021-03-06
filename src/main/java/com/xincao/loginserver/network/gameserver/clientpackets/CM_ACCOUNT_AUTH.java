package com.xincao.loginserver.network.gameserver.clientpackets;

import com.xincao.loginserver.controller.AccountController;
import com.xincao.loginserver.network.aion.SessionKey;
import com.xincao.loginserver.network.gameserver.GsClientPacket;
import com.xincao.loginserver.network.gameserver.GsConnection;

import java.nio.ByteBuffer;

/**
 * In this packet Gameserver is asking if given account sessionKey is valid at
 * Loginserver side. [if user that is authenticating on Gameserver is already
 * authenticated on Loginserver]
 *
 * @author -Nemesiss-
 *
 */
public class CM_ACCOUNT_AUTH extends GsClientPacket {

    /**
     * SessionKey that GameServer needs to check if is valid at Loginserver
     * side.
     */
    private SessionKey sessionKey;

    /**
     * Constructor.
     *
     * @param buf
     * @param client
     */
    public CM_ACCOUNT_AUTH(ByteBuffer buf, GsConnection client) {
        super(buf, client, 0x01);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        int accountId = readD();
        int loginOk = readD();
        int playOk1 = readD();
        int playOk2 = readD();
        sessionKey = new SessionKey(accountId, loginOk, playOk1, playOk2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        ac.getBean(AccountController.class).checkAuth(sessionKey, getConnection());
    }
}