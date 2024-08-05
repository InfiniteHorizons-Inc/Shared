import com.infinitehorizons.utils.ThreadPoolExecutorLogged;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ThreadPoolExecutorLoggedTest {

    private Logger logger;
    private ThreadFactory threadFactory;

    @BeforeEach
    void setUp() {
        logger = Mockito.mock(Logger.class);
        threadFactory = Executors.defaultThreadFactory();
    }

    @Test
    void testNewCachedThreadPool() {
        ExecutorService executor = ThreadPoolExecutorLogged.newCachedThreadPool(threadFactory, logger);

        for (int i = 0; i < 10; i++) {
            executor.execute(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executor.shutdown();
        try {
            assertEquals(true, executor.awaitTermination(1, TimeUnit.SECONDS), "Executor should terminate successfully.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testNewFixedThreadPool() {
        int nThreads = 5;
        ExecutorService executor = ThreadPoolExecutorLogged.newFixedThreadPool(nThreads, threadFactory, logger);

        for (int i = 0; i < 10; i++) {
            executor.execute(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executor.shutdown();
        try {
            assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS), "Executor should terminate successfully.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testAfterExecuteWithException() {
        ThreadPoolExecutor executor = new ThreadPoolExecutorLogged(
                1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), threadFactory, logger);

        Runnable task = () -> {
            throw new RuntimeException("Test exception");
        };

        Future<?> future = executor.submit(task);

        executor.shutdown();

        try {
            future.get();
        } catch (ExecutionException | InterruptedException ignored) {
        }

        verify(logger, times(1)).error(anyString(), any(), any(Throwable.class));
    }
}
