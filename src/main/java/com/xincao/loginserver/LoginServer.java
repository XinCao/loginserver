package com.xincao.loginserver;

import com.xincao.common.configuration.Util;
import com.xincao.common.util.tool.Infos;
import com.xincao.common.util.constant.ExitCode;
import com.xincao.common.util.tool.DeadLockDetector;
import com.xincao.loginserver.configs.Config;
import com.xincao.loginserver.network.IOServer;
import com.xincao.loginserver.network.ncrypt.KeyGen;
import com.xincao.loginserver.utils.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class LoginServer {

    private static final Logger log = LoggerFactory.getLogger(LoginServer.class);

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Config.load();
        /**
         * Start deadlock detector that will restart server if deadlock happened
         */
        DeadLockDetector.detector(60, DeadLockDetector.Dealt.RESTART); // 检查死锁
        ThreadPoolManager.getInstance();
        /**
         * Initialize Key Generator
         */
        try {
            Util.printSection("KeyGen");
            KeyGen.init();
        } catch (Exception e) {
            log.error("Failed initializing Key Generator. Reason: " + e.getMessage(), e);
            System.exit(ExitCode.CODE_ERROR);
        }
        ApplicationContext ac = new FileSystemXmlApplicationContext("");
        Util.printSection("IOServer");
        IOServer.getInstance().connect();
        Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
        Util.printSection("System");
        Infos.printAllInfos();
        Util.printSection("LoginServerLog");
        log.info("AE Login Server started in " + (System.currentTimeMillis() - start) / 1000 + " seconds.");
    }
}