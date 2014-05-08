package com.xincao.loginserver.network.aion.clientpackets;

import com.xincao.loginserver.GameServerInfo;
import com.xincao.loginserver.GameServerTable;
import com.xincao.loginserver.network.aion.AionAuthResponse;
import com.xincao.loginserver.network.aion.AionClientPacket;
import com.xincao.loginserver.network.aion.AionConnection;
import com.xincao.loginserver.network.aion.SessionKey;
import com.xincao.loginserver.network.aion.serverpackets.SM_LOGIN_FAIL;
import com.xincao.loginserver.network.aion.serverpackets.SM_PLAY_FAIL;
import com.xincao.loginserver.network.aion.serverpackets.SM_PLAY_OK;

import java.nio.ByteBuffer;

/**
 * @author -Nemesiss-
 */
public class CM_PLAY extends AionClientPacket {

    /**
     * accountId is part of session key - its used for security purposes
     */
    private int accountId;
    /**
     * loginOk is part of session key - its used for security purposes
     */
    private int loginOk;
    /**
     * Id of game server that this client is trying to play on.
     */
    private byte servId;

    /**
     * Constructs new instance of <tt>SM_PLAY_FAIL</tt> packet.
     *
     * @param buf packet data
     * @param client client
     */
    public CM_PLAY(ByteBuffer buf, AionConnection client) {
        super(buf, client, 0x02);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        accountId = readD();
        loginOk = readD();
        servId = (byte) readC();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        AionConnection con = getConnection();
        SessionKey key = con.getSessionKey();
        if (key.checkLogin(accountId, loginOk)) {
            GameServerInfo gsi = GameServerTable.getGameServerInfo(servId);
            if (gsi == null || !gsi.isOnline()) {
                con.sendPacket(new SM_PLAY_FAIL(AionAuthResponse.SERVER_DOWN));
            } // else if(serv gm only)
            // con.sendPacket(new SM_PLAY_FAIL(AionAuthResponse.GM_ONLY));
            else if (gsi.isFull()) {
                con.sendPacket(new SM_PLAY_FAIL(AionAuthResponse.SERVER_FULL));
            } else {
                con.setJoinedGs();
                sendPacket(new SM_PLAY_OK(key));
            }
        } else {
            con.close(new SM_LOGIN_FAIL(AionAuthResponse.SYSTEM_ERROR), true);
        }
    }
}