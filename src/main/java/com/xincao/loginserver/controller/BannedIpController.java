package com.xincao.loginserver.controller;

import com.xincao.common_configuration.Util;
import com.xincao.common_util.tool.Validate;
import com.xincao.loginserver.mapper.BannedIpMapper;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import com.xincao.loginserver.model.BannedIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class that controlls all ip banning activity
 *
 * @author SoulKeeper
 */
public class BannedIpController {

    private static final Logger log = LoggerFactory.getLogger(BannedIpController.class);
    @Autowired
    private BannedIpMapper bannedIpMapper;
    /**
     * List of banned ip adresses
     */
    private static Set<BannedIP> banList;

    /**
     * Loads list of banned ips
     */
    public void load() {
        Util.printSection("BannedIP");
        reload();
    }

    /**
     * Loads list of banned ips
     */
    public void reload() {
        // we are not going to make ip ban every minute, so it's ok to simplify a concurrent code a bit
        banList = new CopyOnWriteArraySet<BannedIP>(bannedIpMapper.getAllBans());
        log.info("BannedIpController loaded " + banList.size() + " IP bans.");
    }

    /**
     * Checks if ip (or mask) is banned
     *
     * @param ip ip address to check for ban
     * @return is it banned or not
     */
    public boolean isBanned(String ip) {
        for (BannedIP ipBan : banList) {
            if (ipBan.isActive() && Validate.checkIPMatching(ipBan.getMask(), ip)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Bans ip or mask for infinite period of time
     *
     * @param ip ip to ban
     * @return was ip banned or not
     */
    public boolean banIp(String ip) {
        return banIp(ip, null);
    }

    /**
     * Bans ip (or mask)
     *
     * @param ip ip to ban
     * @param expireTime ban expiration time, null = never expires
     * @return was ip banned or not
     */
    public boolean banIp(String ip, Timestamp expireTime) {
        BannedIP ipBan = new BannedIP();
        ipBan.setMask(ip);
        ipBan.setTimeEnd(expireTime);
        if (bannedIpMapper.insert(ipBan)) {
            banList.add(ipBan);
            return true;
        }
        return false;
    }

    /**
     * Adds or updates ip ban. Changes are reflected in DB
     *
     * @param ipBan banned ip to add or change
     * @return was it updated or not
     */
    public boolean addOrUpdateBan(BannedIP ipBan) {
        if (ipBan.getId() == null) {
            if (bannedIpMapper.insert(ipBan)) {
                banList.add(ipBan);
                return true;
            } else {
                return false;
            }
        } else {
            return bannedIpMapper.update(ipBan);
        }
    }

    /**
     * Removes ip ban.
     *
     * @param ip ip to unban
     * @return returns true if ip was successfully unbanned
     */
    public boolean unbanIp(String ip) {
        Iterator<BannedIP> it = banList.iterator();
        while (it.hasNext()) {
            BannedIP ipBan = it.next();
            if (ipBan.getMask().equals(ip)) {
                if (bannedIpMapper.remove(ipBan)) {
                    it.remove();
                    return true;
                } else {
                    break;
                }
            }
        }
        return false;
    }
}