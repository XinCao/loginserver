package com.xincao.loginserver.network.gameserver.clientpackets;

import java.nio.ByteBuffer;

import com.xincao.loginserver.controller.AccountController;
import com.xincao.loginserver.model.Account;
import com.xincao.loginserver.network.gameserver.GsClientPacket;
import com.xincao.loginserver.network.gameserver.GsConnection;
import com.xincao.loginserver.network.gameserver.serverpackets.SM_REQUEST_KICK_ACCOUNT;
import com.xincao.loginserver.GameServerTable;

/**
 * Reads the list of accoutn id's that are logged to game server
 *
 * @author SoulKeeper
 */
public class CM_ACCOUNT_LIST extends GsClientPacket {

    /**
     * Array with accounts that are logged in
     */
    private String[] accountNames;

    /**
     * Creates new packet instance.
     *
     * @param buf	packet data
     * @param client client
     */
    public CM_ACCOUNT_LIST(ByteBuffer buf, GsConnection client) {
        super(buf, client, 0x04);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        accountNames = new String[readD()];
        for (int i = 0; i < accountNames.length; i++) {
            accountNames[i] = readS();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        for (String s : accountNames) {
            Account a = ac.getBean(AccountController.class).loadAccount(s);
            if (ac.getBean(GameServerTable.class).isAccountOnAnyGameServer(a)) {
                getConnection().sendPacket(new SM_REQUEST_KICK_ACCOUNT(a.getId()));
                continue;
            }
            getConnection().getGameServerInfo().addAccountToGameServer(a);
        }
    }
}