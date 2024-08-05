package com.infinitehorizons.utils;

import com.infinitehorizons.config.ConfigLoader;
import org.slf4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorLogged extends ThreadPoolExecutor {

    private final Logger logger;

    // Constructor para inicializar el pool con parámetros específicos
    public ThreadPoolExecutorLogged(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                    TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory factory, Logger logger) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, factory);
        this.logger = logger;
    }

    @Override
    public void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            try {
                Object result = ((Future<?>) r).get();
            } catch (CancellationException e) {
                t = e;
            } catch (ExecutionException e) {
                t = e.getCause();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (t != null) {
            logger.error("Unhandled exception in thread: {}!", Thread.currentThread().getName(), t);
        }
    }

    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory, Logger logger) {
        ConfigLoader configLoader = new ConfigLoader();
        int corePoolSize = Integer.parseInt(configLoader.getProperty("threadpool.core-size", "0"));
        int maxPoolSize = Integer.parseInt(configLoader.getProperty("threadpool.max-size", String.valueOf(Integer.MAX_VALUE)));
        long keepAliveTime = Long.parseLong(configLoader.getProperty("threadpool.keep-alive-time", "60"));

        return new ThreadPoolExecutorLogged(corePoolSize, maxPoolSize,
                keepAliveTime, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                threadFactory, logger);
    }

    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory, Logger logger) {
        ConfigLoader configLoader = new ConfigLoader();

        return new ThreadPoolExecutorLogged(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory, logger);
    }
}
