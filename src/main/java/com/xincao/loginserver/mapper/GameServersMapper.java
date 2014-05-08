package com.xincao.loginserver.mapper;

import com.xincao.loginserver.GameServerInfo;
import java.util.Map;

/**
 *
 * @author caoxin
 */
public interface GameServersMapper {

    /**
     * Returns all gameservers from database.
     *
     * @return all gameservers from database.
     */
    public abstract Map<Byte, GameServerInfo> getAllGameServers();
}