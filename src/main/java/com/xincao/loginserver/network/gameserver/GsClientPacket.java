package com.xincao.loginserver.network.gameserver;

import com.xincao.common_nio.packet.BaseClientPacket;
import com.xincao.loginserver.utils.AC;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Base class for every GameServer -> LS Client Packet
 *
 * @author -Nemesiss-
 */
public abstract class GsClientPacket extends BaseClientPacket<GsConnection> {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final ApplicationContext ac = AC.getApplicationContext();

    /**
     * Creates new packet instance.
     *
     * @param buf packet data
     * @param client client
     * @param opcode packet id
     */
    protected GsClientPacket(ByteBuffer buf, GsConnection client, int opcode) {
        super(buf, opcode);
        setConnection(client);
    }

    /**
     * run runImpl catching and logging Throwable.
     */
    @Override
    public final void run() {
        try {
            runImpl();
        } catch (Throwable e) {
            log.warn("error handling gs (" + getConnection().getIP() + ") message " + this, e);
        }
    }

    /**
     * Send new GsServerPacket to connection that is owner of this packet. This
     * method is equivalent to: getConnection().sendPacket(msg);
     *
     * @param msg
     */
    protected void sendPacket(GsServerPacket msg) {
        getConnection().sendPacket(msg);
    }
}