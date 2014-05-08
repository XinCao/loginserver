package com.xincao.loginserver.network.gameserver.clientpackets;

import com.xincao.common_nio.IPRange;
import com.xincao.loginserver.GameServerTable;
import com.xincao.loginserver.network.gameserver.GsAuthResponse;
import com.xincao.loginserver.network.gameserver.GsClientPacket;
import com.xincao.loginserver.network.gameserver.GsConnection;
import com.xincao.loginserver.network.gameserver.GsConnection.State;
import com.xincao.loginserver.network.gameserver.serverpackets.SM_GS_AUTH_RESPONSE;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This is authentication packet that gs will send to login server for
 * registration.
 *
 * @author -Nemesiss-
 */
public class CM_GS_AUTH extends GsClientPacket {

    /**
     * Password for authentication
     */
    private String password;
    /**
     * Id of GameServer
     */
    private byte gameServerId;
    /**
     * Maximum number of players that this Gameserver can accept.
     */
    private int maxPlayers;
    /**
     * Port of this Gameserver.
     */
    private int port;
    /**
     * Default address for server
     */
    private byte[] defaultAddress;
    /**
     * List of IPRanges for this gameServer
     */
    private List<IPRange> ipRanges;

    /**
     * Constructor.
     *
     * @param buf
     * @param client
     */
    public CM_GS_AUTH(ByteBuffer buf, GsConnection client) {
        super(buf, client, 0x00);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        gameServerId = (byte) readC();

        defaultAddress = readB(readC());
        int size = readD();
        ipRanges = new ArrayList<IPRange>(size);
        for (int i = 0; i < size; i++) {
            ipRanges.add(new IPRange(readB(readC()), readB(readC()), readB(readC())));
        }

        port = readH();
        maxPlayers = readD();
        password = readS();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        GsConnection client = getConnection();

        GsAuthResponse resp = ac.getBean(GameServerTable.class).registerGameServer(client, gameServerId, defaultAddress, ipRanges, port, maxPlayers, password);

        switch (resp) {
            case AUTHED:
                getConnection().setState(State.AUTHED);
                sendPacket(new SM_GS_AUTH_RESPONSE(resp));
                break;

            default:
                client.close(new SM_GS_AUTH_RESPONSE(resp), true);
        }
    }
}