package com.xincao.loginserver.network.aion.clientpackets;

import com.xincao.loginserver.GameServerTable;
import com.xincao.loginserver.network.aion.AionAuthResponse;
import com.xincao.loginserver.network.aion.AionClientPacket;
import com.xincao.loginserver.network.aion.AionConnection;
import com.xincao.loginserver.network.aion.serverpackets.SM_LOGIN_FAIL;
import com.xincao.loginserver.network.aion.serverpackets.SM_SERVER_LIST;

import java.nio.ByteBuffer;

/**
 * @author -Nemesiss-
 */
public class CM_SERVER_LIST extends AionClientPacket {

    /**
     * accountId is part of session key - its used for security purposes
     */
    private int accountId;
    /**
     * loginOk is part of session key - its used for security purposes
     */
    private int loginOk;

    /**
     * Constructs new instance of <tt>CM_SERVER_LIST </tt> packet.
     *
     * @param buf
     * @param client
     */
    public CM_SERVER_LIST(ByteBuffer buf, AionConnection client) {
        super(buf, client, 0x05);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        accountId = readD();
        loginOk = readD();
        readD();// unk
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        AionConnection con = getConnection();
        if (con.getSessionKey().checkLogin(accountId, loginOk)) {
            if (ac.getBean(GameServerTable.class).getGameServers().isEmpty()) {
                con.close(new SM_LOGIN_FAIL(AionAuthResponse.NO_GS_REGISTERED), true);
            } else {
                sendPacket(new SM_SERVER_LIST());
            }
        } else {
            /**
             * Session key is not ok - inform client that smth went wrong - dc
             * client
             */
            con.close(new SM_LOGIN_FAIL(AionAuthResponse.SYSTEM_ERROR), true);
        }
    }
}