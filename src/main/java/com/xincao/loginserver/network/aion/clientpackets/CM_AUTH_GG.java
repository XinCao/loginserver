package com.xincao.loginserver.network.aion.clientpackets;

import com.xincao.loginserver.network.aion.AionAuthResponse;
import com.xincao.loginserver.network.aion.AionClientPacket;
import com.xincao.loginserver.network.aion.AionConnection;
import com.xincao.loginserver.network.aion.AionConnection.State;
import com.xincao.loginserver.network.aion.serverpackets.SM_AUTH_GG;
import com.xincao.loginserver.network.aion.serverpackets.SM_LOGIN_FAIL;

import java.nio.ByteBuffer;

/**
 * @author -Nemesiss-
 */
public class CM_AUTH_GG extends AionClientPacket {

    /**
     * session id - its should match sessionId that was send in Init packet.
     */
    private int sessionId;

    /*
     * private final int data1; private final int data2; private final int data3; private final int data4;
     */
    /**
     * Constructs new instance of <tt>CM_AUTH_GG</tt> packet.
     *
     * @param buf
     * @param client
     */
    public CM_AUTH_GG(ByteBuffer buf, AionConnection client) {
        super(buf, client, 0x07);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        sessionId = readD();
        readD();
        readD();
        readD();
        readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        AionConnection con = getConnection();
        if (con.getSessionId() == sessionId) {
            con.setState(State.AUTHED_GG);
            con.sendPacket(new SM_AUTH_GG(sessionId));
        } else {
            /**
             * Session id is not ok - inform client that smth went wrong - dc
             * client
             */
            con.close(new SM_LOGIN_FAIL(AionAuthResponse.SYSTEM_ERROR), true);
        }
    }
}