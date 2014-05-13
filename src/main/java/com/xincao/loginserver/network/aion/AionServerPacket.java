package com.xincao.loginserver.network.aion;

import com.xincao.common.nio.packet.BaseServerPacket;
import com.xincao.loginserver.utils.AC;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Base class for every LS -> Aion Server Packet.
 *
 * @author -Nemesiss-
 */
public abstract class AionServerPacket extends BaseServerPacket {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final ApplicationContext ac = AC.getApplicationContext();

    /**
     * Constructs a new server packet with specified id.
     *
     * @param opcode packet opcode.
     */
    protected AionServerPacket(int opcode) {
        super(opcode);
    }

    /**
     * Write and encrypt this packet data for given connection, to given buffer.
     *
     * @param con
     * @param buf
     */
    public final void write(AionConnection con, ByteBuffer buf) {
        buf.putShort((short) 0);
        writeImpl(con, buf);
        buf.flip();
        buf.putShort((short) 0);
        ByteBuffer b = buf.slice();
        short size = (short) (con.encrypt(b) + 2);
        buf.putShort(0, size); // 对称加密包（感觉没有将，协议ID加密进去）
        buf.position(0).limit(size);
    }

    /**
     * Write data that this packet represents to given byte buffer.
     *
     * @param con
     * @param buf
     */
    protected abstract void writeImpl(AionConnection con, ByteBuffer buf);
}