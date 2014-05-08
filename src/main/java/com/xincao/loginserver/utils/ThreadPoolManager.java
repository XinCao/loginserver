package com.xincao.loginserver.utils;

import com.xincao.common_nio.DisconnectionTask;
import com.xincao.common_nio.DisconnectionThreadPool;
import com.xincao.common_util.ScheduledThreadPoolExecutorAE;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author -Nemesiss-
 */
public class ThreadPoolManager implements DisconnectionThreadPool {

    /**
     * Logger for this class
     */
    private static final Logger log = LoggerFactory.getLogger(ThreadPoolManager.class);
    /**
     * Instance of ThreadPoolManager
     */
    private static ThreadPoolManager instance = new ThreadPoolManager();
    /**
     * STPE for normal scheduled tasks
     */
    private ScheduledThreadPoolExecutorAE scheduledThreadPool;
    /**
     * STPE for disconnection tasks
     */
    private ScheduledThreadPoolExecutorAE disconnectionScheduledThreadPool;
    /**
     * TPE for execution of gameserver client packets
     */
    private ThreadPoolExecutor gameServerPacketsThreadPool;

    /**
     * @return ThreadPoolManager instance.
     */
    public static ThreadPoolManager getInstance() {
        return instance;
    }

    /**
     * Constructor.
     */
    private ThreadPoolManager() {
        scheduledThreadPool = new ScheduledThreadPoolExecutorAE(4, new PriorityThreadFactory("ScheduledThreadPool", Thread.NORM_PRIORITY));
        //不知道为什么不存在，这个方法
//        scheduledThreadPool.setRemoveOnCancelPolicy(true);
        disconnectionScheduledThreadPool = new ScheduledThreadPoolExecutorAE(4, new PriorityThreadFactory("ScheduledThreadPool", Thread.NORM_PRIORITY));
//        disconnectionScheduledThreadPool.setRemoveOnCancelPolicy(true);
        gameServerPacketsThreadPool = new ThreadPoolExecutor(4, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("Game Server Packet Pool", Thread.NORM_PRIORITY + 3));
    }

    /**
     * Schedule
     *
     * @param <T>
     * @param r
     * @param delay
     * @return ScheduledFuture
     */
    @SuppressWarnings("unchecked")
    public <T extends Runnable> ScheduledFuture<T> schedule(T r, long delay) {
        try {
            if (delay < 0) {
                delay = 0;
            }
            return (ScheduledFuture<T>) scheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            return null; /* shutdown, ignore */
        }
    }

    /**
     * Schedule at fixed rate
     *
     * @param <T>
     * @param r
     * @param initial
     * @param delay
     * @return ScheduledFuture
     */
    @SuppressWarnings("unchecked")
    public <T extends Runnable> ScheduledFuture<T> scheduleAtFixedRate(T r, long initial, long delay) {
        try {
            if (delay < 0) {
                delay = 0;
            }
            if (initial < 0) {
                initial = 0;
            }
            return (ScheduledFuture<T>) scheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            return null;
        }
    }

    /**
     * Executes Runnable - GameServer Client packet.
     *
     * @param pkt
     */
    public void executeGsPacket(Runnable pkt) {
        gameServerPacketsThreadPool.execute(pkt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void scheduleDisconnection(DisconnectionTask dt, long delay) {
        if (delay < 0) {
            delay = 0;
        }
        scheduledThreadPool.schedule(dt, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForDisconnectionTasks() {
        try {
            disconnectionScheduledThreadPool.shutdown();
            disconnectionScheduledThreadPool.awaitTermination(6, TimeUnit.MINUTES);
        } catch (Exception e) {
        }
    }

    /**
     * PriorityThreadFactory creating new threads for ThreadPoolManager
     *
     */
    private class PriorityThreadFactory implements ThreadFactory {

        /**
         * Priority of new threads
         */
        private int prio;
        /**
         * Thread group name
         */
        private String name;
        /**
         * Number of created threads
         */
        private AtomicInteger threadNumber = new AtomicInteger(1);
        /**
         * ThreadGroup for created threads
         */
        private ThreadGroup group;

        /**
         * Constructor.
         *
         * @param name
         * @param prio
         */
        public PriorityThreadFactory(String name, int prio) {
            this.prio = prio;
            this.name = name;
            group = new ThreadGroup(this.name);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r);
            t.setName(name + "-" + threadNumber.getAndIncrement());
            t.setPriority(prio);
            t.setUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());
            return t;
        }
    }

    /**
     * Shutdown all thread pools.
     */
    public void shutdown() {
        try {
            scheduledThreadPool.shutdown();
            gameServerPacketsThreadPool.shutdown();
            scheduledThreadPool.awaitTermination(2, TimeUnit.SECONDS);
            gameServerPacketsThreadPool.awaitTermination(2, TimeUnit.SECONDS);
            log.info("All ThreadPools are now stopped");
        } catch (InterruptedException e) {
            log.error("Can't shutdown ThreadPoolManager", e);
        }
    }
}