package com.xincao.loginserver.network.gameserver.clientpackets;

import com.xincao.common_util.Rnd;
import com.xincao.loginserver.controller.AccountController;
import com.xincao.loginserver.model.Account;
import com.xincao.loginserver.model.ReconnectingAccount;
import com.xincao.loginserver.network.gameserver.GsClientPacket;
import com.xincao.loginserver.network.gameserver.GsConnection;
import com.xincao.loginserver.network.gameserver.serverpackets.SM_ACCOUNT_RECONNECT_KEY;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This packet is sended by GameServer when player is requesting fast reconnect
 * to login server. LoginServer in response will send reconectKey.
 *
 * @author -Nemesiss-
 *
 */
public class CM_ACCOUNT_RECONNECT_KEY extends GsClientPacket {

    /**
     * accoundId of account that will be reconnecting.
     */
    private int accountId;

    /**
     * Constructor.
     *
     * @param buf
     * @param client
     */
    public CM_ACCOUNT_RECONNECT_KEY(ByteBuffer buf, GsConnection client) {
        super(buf, client, 0x02);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        accountId = readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        int reconectKey = Rnd.nextInt();
        Account acc = getConnection().getGameServerInfo().removeAccountFromGameServer(accountId);
        if (acc == null) {
            log.info("This shouldnt happend! [Error]");
        } else {
            ac.getBean(AccountController.class).addReconnectingAccount(new ReconnectingAccount(acc, reconectKey));
        }
        sendPacket(new SM_ACCOUNT_RECONNECT_KEY(accountId, reconectKey));
    }
}