package com.infinitehorizons.utils;

import com.infinitehorizons.config.ConfigLoader;
import org.jetbrains.annotations.NotNull;
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

/**
 * A custom {@link ThreadPoolExecutor} that logs unhandled exceptions in task execution.
 * <p>
 * Provides methods to create logged thread pools with configurable settings.
 *
 * @since v0.0.1-SNAPSHOT
 */
public class ThreadPoolExecutorLogged extends ThreadPoolExecutor {

    private final Logger logger;

    /**
     * Constructs a new {@code ThreadPoolExecutorLogged}.
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the pool.
     * @param keepAliveTime   when the number of threads is greater than the core, this is the maximum time
     *                        that excess idle threads will wait for new tasks before terminating.
     * @param unit            the time unit for the {@code keepAliveTime} argument.
     * @param workQueue       the queue to use for holding tasks before they are executed.
     * @param factory         the factory to use when the executor creates a new thread.
     * @param logger          the logger used to log unhandled exceptions in thread execution.
     */
    public ThreadPoolExecutorLogged(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                    TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory factory, Logger logger) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, factory);
        this.logger = logger;
    }

    /**
     * Logs any unhandled exceptions thrown during the execution of a task.
     *
     * @param r the runnable task.
     * @param t the exception thrown during execution, if any.
     */
    @Override
    public void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            try {
                ((Future<?>) r).get();
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

    /**
     * Creates a new cached thread pool with logging of unhandled exceptions.
     *
     * @param threadFactory the factory to use when creating new threads.
     * @param logger        the logger used to log unhandled exceptions in thread execution.
     * @return a new {@link ExecutorService} instance.
     */
    @NotNull
    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory, Logger logger) {
        ConfigLoader configLoader = new ConfigLoader("application.properties");

        int corePoolSize = Integer.parseInt(configLoader.getProperty("thread-pool.core-size", "0"));
        int maxPoolSize = Integer.parseInt(configLoader.getProperty("thread-pool.max-size", String.valueOf(Integer.MAX_VALUE)));
        long keepAliveTime = Long.parseLong(configLoader.getProperty("thread-pool.keep-alive-time", "60"));

        configLoader.setProperty("thread-pool.core-size", String.valueOf(corePoolSize));
        configLoader.setProperty("thread-pool.max-size", String.valueOf(maxPoolSize));
        configLoader.setProperty("thread-pool.keep-alive-time", String.valueOf(keepAliveTime));
        configLoader.saveProperties();

        return new ThreadPoolExecutorLogged(corePoolSize, maxPoolSize,
                keepAliveTime, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                threadFactory, logger);
    }

    /**
     * Creates a new fixed-size thread pool with logging of unhandled exceptions.
     *
     * @param nThreads      the number of threads in the pool.
     * @param threadFactory the factory to use when creating new threads.
     * @param logger        the logger used to log unhandled exceptions in thread execution.
     * @return a new {@link ExecutorService} instance.
     */
    @NotNull
    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory, Logger logger) {
        ConfigLoader configLoader = new ConfigLoader("application.properties");

        configLoader.setProperty("thread-pool.fixed-size", String.valueOf(nThreads));
        configLoader.saveProperties();

        return new ThreadPoolExecutorLogged(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory, logger);
    }
}
