package com.xincao.loginserver.network.aion;

import com.xincao.common_nio.AConnection;
import com.xincao.common_nio.ConnectionFactory;
import com.xincao.common_nio.Dispatcher;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * ConnectionFactory implementation that will be creating AionConnections
 *
 * @author -Nemesiss-
 *
 */
public class AionConnectionFactoryImpl implements ConnectionFactory {

    /**
     * Create a new {@link com.aionemu.commons.network.AConnection AConnection}
     * instance.<br>
     *
     * @param socket that new
     * {@link com.aionemu.commons.network.AConnection AConnection} instance will
     * represent.<br>
     * @param dispatcher to witch new connection will be registered.<br>
     * @return a new instance of
     * {@link com.aionemu.commons.network.AConnection AConnection}<br>
     * @throws IOException
     * @see com.aionemu.commons.network.AConnection
     * @see com.aionemu.commons.network.Dispatcher
     */
    @Override
    public AConnection create(SocketChannel socket, Dispatcher dispatcher) throws IOException {
        return new AionConnection(socket, dispatcher);
    }
}