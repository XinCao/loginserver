package com.xincao.loginserver.network.aion;

import com.xincao.common_nio.packet.BaseClientPacket;
import com.xincao.loginserver.model.Account;
import com.xincao.loginserver.utils.AC;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Base class for every Aion -> LS Client Packet
 *
 * @author -Nemesiss-
 */
public abstract class AionClientPacket extends BaseClientPacket<AionConnection> {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected final ApplicationContext ac = AC.getApplicationContext();

    /**
     * Constructs new client packet.
     *
     * @param buf packet data
     * @param client client
     * @param opcode packet id
     */
    protected AionClientPacket(ByteBuffer buf, AionConnection client, int opcode) {
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
            String name;
            Account account = getConnection().getAccount();
            if (account != null) {
                name = account.getName();
            } else {
                name = getConnection().getIP();
            }
            log.error("error handling client (" + name + ") message " + this, e);
        }
    }

    /**
     * Send new AionServerPacket to connection that is owner of this packet.
     * This method is equvalent to: getConnection().sendPacket(msg);
     *
     * @param msg
     */
    protected void sendPacket(AionServerPacket msg) {
        getConnection().sendPacket(msg);
    }
}