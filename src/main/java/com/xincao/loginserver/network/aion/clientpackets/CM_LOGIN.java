package com.xincao.loginserver.network.aion.clientpackets;

import com.xincao.loginserver.controller.AccountController;
import com.xincao.loginserver.network.aion.AionAuthResponse;
import com.xincao.loginserver.network.aion.AionClientPacket;
import com.xincao.loginserver.network.aion.AionConnection;
import com.xincao.loginserver.network.aion.AionConnection.State;
import com.xincao.loginserver.network.aion.SessionKey;
import com.xincao.loginserver.network.aion.serverpackets.SM_LOGIN_FAIL;
import com.xincao.loginserver.network.aion.serverpackets.SM_LOGIN_OK;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author -Nemesiss-, KID
 */
public class CM_LOGIN extends AionClientPacket {

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(CM_LOGIN.class);
    /**
     * byte array contains encrypted login and password.
     */
    private byte[] data;

    /**
     * Constructs new instance of <tt>CM_LOGIN </tt> packet.
     *
     * @param buf
     * @param client
     */
    public CM_LOGIN(ByteBuffer buf, AionConnection client) {
        super(buf, client, 0x0b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        readD();
        if (getRemainingBytes() >= 128) {
            data = readB(128);
        }
    }

    @Override
    protected void runImpl() {
        if (data == null) {
            return;
        }
        byte[] decrypted;
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
            rsaCipher.init(Cipher.DECRYPT_MODE, getConnection().getRSAPrivateKey());
            decrypted = rsaCipher.doFinal(data, 0, 128);
        } catch (GeneralSecurityException e) {
            log.warn("Error while decripting data on user auth." + e, e);
            sendPacket(new SM_LOGIN_FAIL(AionAuthResponse.SYSTEM_ERROR));
            return;
        }
        String user = new String(decrypted, 64, 32).trim().toLowerCase();
        String password = new String(decrypted, 96, 32).trim();
        int ncotp = decrypted[0x7c];
        ncotp |= decrypted[0x7d] << 8;
        ncotp |= decrypted[0x7e] << 16;
        ncotp |= decrypted[0x7f] << 24;
        log.debug("AuthLogin: " + user + " pass: " + password + " ncotp: " + ncotp);
        AionConnection client = getConnection();
        AionAuthResponse response = ac.getBean(AccountController.class).login(user, password, client);
        switch (response) {
            case AUTHED:
                client.setState(State.AUTHED_LOGIN);
                client.setSessionKey(new SessionKey(client.getAccount()));
                client.sendPacket(new SM_LOGIN_OK(client.getSessionKey()));
                break;
            default:
                client.close(new SM_LOGIN_FAIL(response), true);
                break;
        }
    }
}