package com.infinitehorizons.utils;

import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

import lombok.experimental.UtilityClass;

/**
 * Utility class providing methods related to Input/Output operations.
 *
 * <p>This class is designed to provide convenience methods for common IO tasks,
 * such as retrieving the appropriate class loader for a given class.</p>
 *
 */
@UtilityClass
public class IoUtils {

    /** MediaType for application/json */
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /** MediaType for application/octet-stream */
    public static final MediaType OCTET = MediaType.parse("application/octet-stream; charset=utf-8");

    /** Empty byte-array, used for {@link #readAllBytes(java.io.InputStream)} */
    public static final byte[] EMPTY_BYTES = new byte[0];

    private static final CompletableFuture<?>[] EMPTY_FUTURES = new CompletableFuture<?>[0];

    /**
     * Reads all bytes from an {@link InputStream}.
     *
     * @param stream The InputStream to read from.
     * @return A byte array containing all bytes of the stream.
     * @throws IOException If an I/O error occurs.
     */
    @NotNull
    public static byte[] readAllBytes(@NotNull InputStream stream) throws IOException {
        int count;
        int pos = 0;
        byte[] output = EMPTY_BYTES;
        byte[] buf = new byte[1024];

        while ((count = stream.read(buf)) > 0) {
            if (pos + count >= output.length) {
                byte[] tmp = output;
                output = new byte[pos + count];
                System.arraycopy(tmp, 0, output, 0, tmp.length);
            }
            System.arraycopy(buf, 0, output, pos, count);
            pos += count;
        }
        return output;
    }

    /**
     * Retrieves the input stream from a response body, handling gzip encoding if necessary.
     *
     * @param response The response to process.
     * @return The input stream of the response body, or null if the body is absent.
     * @throws IOException If an I/O error occurs.
     */
    @Nullable
    public static InputStream getBody(@NotNull okhttp3.Response response) throws IOException {
        List<String> encoding = response.headers("content-encoding");
        ResponseBody body = response.body();
        if (!encoding.isEmpty() && body != null) {
            return new GZIPInputStream(body.byteStream());
        }
        return body != null ? body.byteStream() : null;
    }

    /**
     * Converts an {@link InputStream} to a {@link JSONObject}.
     *
     * @param input The InputStream to convert.
     * @return A JSONObject representing the input data.
     */
    @NotNull
    public static JSONObject toJSON(@NotNull InputStream input) {
        return new JSONObject(new JSONTokener(input));
    }

    /**
     * Converts a list of futures into a future of a list.
     *
     * @param futures The list of futures to convert.
     * @param <T>     The type of the future's result.
     * @return A future that completes with a list of results.
     */
    @NotNull
    public static <T> CompletableFuture<List<T>> flipFuture(@NotNull List<CompletableFuture<T>> futures) {
        List<T> results = new ArrayList<>(futures.size());
        List<CompletableFuture<Void>> stages = new ArrayList<>(futures.size());

        futures.stream()
                .map(future -> future.thenAccept(results::add))
                .forEach(stages::add);

        CompletableFuture<Void> tracker = CompletableFuture.allOf(stages.toArray(EMPTY_FUTURES));
        CompletableFuture<List<T>> future = new CompletableFuture<>();

        tracker.thenRun(() -> future.complete(results)).exceptionally(e -> {
            future.completeExceptionally(e);
            return null;
        });

        return future;
    }

    /**
     * Functional interface for suppliers that can throw checked exceptions.
     *
     * @param <T> The type of result supplied.
     */
    @FunctionalInterface
    public interface SilentSupplier<T> {
        @Nullable
        T get() throws Exception;
    }

    /**
     * Lazy evaluation for logging complex objects.
     *
     * <p>Example: {@code LOG.debug("Suspicious json found", new Lazy(() -> json.toString()));}</p>
     */
    @RequiredArgsConstructor
    public static class Lazy {
        private final SilentSupplier<?> supplier;

        @NotNull
        @Override
        public String toString() {
            try {
                return String.valueOf(supplier.get());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Wrapper for an {@link #OCTET} request body.
     */
    @RequiredArgsConstructor
    public static class OctetBody extends RequestBody {
        private final byte[] data;

        @Override
        public MediaType contentType() {
            return OCTET;
        }

        @Override
        public void writeTo(@NotNull BufferedSink sink) throws IOException {
            sink.write(data);
        }
    }

    /**
     * Retrieves the class loader for a specified class.
     *
     * <p>If the class loader of the given class is null, the method returns the context
     * class loader of the current thread.</p>
     *
     * @param clazz The class for which the class loader is requested.
     * @return The {@link ClassLoader} associated with the provided class, or the
     *         context class loader of the current thread if the class loader is null.
     * @since v1.6
     */
    @NotNull
    public ClassLoader getClassLoaderForClass(@NotNull Class<?> clazz) {
        return clazz.getClassLoader() != null ? clazz.getClassLoader() : Thread.currentThread().getContextClassLoader();
    }
}
