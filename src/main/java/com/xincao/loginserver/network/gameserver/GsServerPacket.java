package com.xincao.loginserver.network.gameserver;

import com.xincao.common.nio.packet.BaseServerPacket;
import com.xincao.loginserver.utils.AC;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Base class for every LS -> GameServer Server Packet.
 *
 * @author -Nemesiss-
 */
public abstract class GsServerPacket extends BaseServerPacket {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final ApplicationContext ac = AC.getApplicationContext();

    /**
     * Constructs a new server packet with specified id.
     *
     * @param opcode packet opcode.
     */
    protected GsServerPacket(int opcode) {
        super(opcode);
    }

    /**
     * Write this packet data for given connection, to given buffer.
     *
     * @param con
     * @param buf
     */
    public final void write(GsConnection con, ByteBuffer buf) {
        buf.putShort((short) 0);
        writeImpl(con, buf);
        buf.flip();
        buf.putShort((short) buf.limit());
        buf.position(0);
    }

    /**
     * Write data that this packet represents to given byte buffer.
     *
     * @param con
     * @param buf
     */
    protected abstract void writeImpl(GsConnection con, ByteBuffer buf);
}