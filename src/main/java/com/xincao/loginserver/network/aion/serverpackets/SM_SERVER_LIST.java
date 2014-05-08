package com.xincao.loginserver.network.aion.serverpackets;

import com.xincao.loginserver.GameServerInfo;
import com.xincao.loginserver.GameServerTable;
import com.xincao.loginserver.network.aion.AionConnection;
import com.xincao.loginserver.network.aion.AionServerPacket;

import java.nio.ByteBuffer;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author -Nemesiss-
 */
public class SM_SERVER_LIST extends AionServerPacket {

    /**
     * Logger for this class.
     */
    protected static Logger log = LoggerFactory.getLogger(SM_SERVER_LIST.class);

    /**
     * Constructs new instance of <tt>SM_SERVER_LIST</tt> packet.
     */
    public SM_SERVER_LIST() {
        super(0x04);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con, ByteBuffer buf) {
        Collection<GameServerInfo> servers = ac.getBean(GameServerTable.class).getGameServers();
        writeC(buf, getOpcode());
        writeC(buf, servers.size());// servers
        writeC(buf, con.getAccount().getLastServer());// last server
        for (GameServerInfo gsi : servers) {
            writeC(buf, gsi.getId());// server id
            writeB(buf, gsi.getIPAddressForPlayer(con.getIP())); // server IP
            writeD(buf, gsi.getPort());// port
            writeC(buf, 0x00); // age limit
            writeC(buf, 0x01);// pvp=1
            writeH(buf, gsi.getCurrentPlayers());// currentPlayers
            writeH(buf, gsi.getMaxPlayers());// maxPlayers
            writeC(buf, gsi.isOnline() ? 1 : 0);// ServerStatus, up=1
            writeD(buf, 1);// bits);
            writeC(buf, 0);// server.brackets ? 0x01 : 0x00);
        }
    }
}