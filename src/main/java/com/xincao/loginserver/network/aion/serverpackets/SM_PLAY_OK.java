package com.xincao.loginserver.network.aion.serverpackets;

import com.xincao.loginserver.network.aion.AionConnection;
import com.xincao.loginserver.network.aion.AionServerPacket;
import com.xincao.loginserver.network.aion.SessionKey;

import java.nio.ByteBuffer;

/**
 * @author -Nemesiss-
 */
public class SM_PLAY_OK extends AionServerPacket {

    /**
     * playOk1 is part of session key - its used for security purposes [checked
     * at game server side]
     */
    private final int playOk1;
    /**
     * playOk2 is part of session key - its used for security purposes [checked
     * at game server side]
     */
    private final int playOk2;

    /**
     * Constructs new instance of <tt>SM_PLAY_OK </tt> packet.
     *
     * @param key session key
     */
    public SM_PLAY_OK(SessionKey key) {
        super(0x07);

        this.playOk1 = key.playOk1;
        this.playOk2 = key.playOk2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con, ByteBuffer buf) {
        writeC(buf, getOpcode());
        writeD(buf, playOk1);
        writeD(buf, playOk2);
    }
}