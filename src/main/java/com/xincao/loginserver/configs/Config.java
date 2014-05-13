package com.xincao.loginserver.configs;

import com.xincao.common.configuration.ConfigurableProcessor;
import com.xincao.common.configuration.Property;
import com.xincao.common.configuration.Util;
import com.xincao.common.util.tool.PropertiesReader;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author -Nemesiss-
 * @author SoulKeeper
 */
public class Config {

    /**
     * Logger for this class.
     */
    protected static final Logger log = LoggerFactory.getLogger(Config.class);
    /**
     * Login Server port
     */
    @Property(key = "loginserver.network.client.port", defaultValue = "2106")
    public static int LOGIN_PORT;
    /**
     * Login Server bind ip
     */
    @Property(key = "loginserver.network.client.host", defaultValue = "*")
    public static String LOGIN_BIND_ADDRESS;
    /**
     * Login Server port
     */
    @Property(key = "loginserver.network.gameserver.port", defaultValue = "9014")
    public static int GAME_PORT;
    /**
     * Login Server bind ip
     */
    @Property(key = "loginserver.network.gameserver.host", defaultValue = "*")
    public static String GAME_BIND_ADDRESS;
    /**
     * Number of trys of login before ban
     */
    @Property(key = "loginserver.network.client.logintrybeforeban", defaultValue = "5")
    public static int LOGIN_TRY_BEFORE_BAN;
    /**
     * Ban time in minutes
     */
    @Property(key = "loginserver.network.client.bantimeforbruteforcing", defaultValue = "15")
    public static int WRONG_LOGIN_BAN_TIME;
    /**
     * Number of Threads that will handle io read (>= 0)
     */
    @Property(key = "loginserver.network.nio.threads.read", defaultValue = "0")
    public static int NIO_READ_THREADS;
    /**
     * Number of Threads that will handle io write (>= 0)
     */
    @Property(key = "loginserver.network.nio.threads.write", defaultValue = "0")
    public static int NIO_WRITE_THREADS;
    /**
     * Should server automaticly create accounts for users or not?
     */
    @Property(key = "loginserver.accounts.autocreate", defaultValue = "true")
    public static boolean ACCOUNT_AUTO_CREATION;

    /**
     * 加载配置文件（.properties）
     */
    public static void load() {
        try {
            Util.printSection("Network");
            String network = "./config/network";
            Properties[] props = PropertiesReader.loadAllFromDirectory(network);
            ConfigurableProcessor.process(Config.class, props);
            log.info("Loading: " + network + "/network.properties");
        } catch (Exception e) {
            log.error("Can't load loginserver configuration", e);
            throw new Error("Can't load loginserver configuration", e);
        }
    }
}