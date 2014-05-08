package com.xincao.loginserver.network.aion.serverpackets;

import com.xincao.loginserver.network.aion.AionAuthResponse;
import com.xincao.loginserver.network.aion.AionConnection;
import com.xincao.loginserver.network.aion.AionServerPacket;

import java.nio.ByteBuffer;

/**
 * @author KID
 */
public class SM_LOGIN_FAIL extends AionServerPacket {

    /**
     * response - why login fail
     */
    private AionAuthResponse response;

    /**
     * Constructs new instance of <tt>SM_LOGIN_FAIL</tt> packet.
     *
     * @param response auth responce
     */
    public SM_LOGIN_FAIL(AionAuthResponse response) {
        super(0x01);
        this.response = response;
    }

    @Override
    protected void writeImpl(AionConnection con, ByteBuffer buf) {
        writeC(buf, getOpcode());
        writeD(buf, response.getMessageId());
    }
}