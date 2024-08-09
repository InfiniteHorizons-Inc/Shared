package com.infinitehorizons;

import com.infinitehorizons.components.WebHookComponent;
import com.infinitehorizons.components.WebHookMessageComponent;
import com.infinitehorizons.constants.SharedConstants;
import com.infinitehorizons.events.AllowedMentionsEvent;
import com.infinitehorizons.exceptions.HttpException;
import com.infinitehorizons.handler.WebHookErrorHandler;
import com.infinitehorizons.models.EntityFactory;
import com.infinitehorizons.models.ReadonlyMessage;
import com.infinitehorizons.models.send.WebHookEmbed;
import com.infinitehorizons.models.send.WebhookMessage;
import com.infinitehorizons.utils.IoUtils;
import com.infinitehorizons.utils.ThreadPoolExecutorLogged;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;

/**
 * Client used to execute webhooks. All send methods are asynchronous and return a {@link CompletableFuture}
 * representing the execution. If provided with {@code null}, a {@link NullPointerException} is thrown instead.
 */
@Getter
@Accessors(fluent = true)
public class SharedWebHook implements AutoCloseable {

    public static final String WEBHOOK_URL = "https://discord.com/api/v" + SharedConstants.DISCORD_API_VERSION + "/webhooks/%s/%s";
    public static final String USER_AGENT = "Webhook(https://github.com/MinnDevelopment/discord-webhooks, " + SharedConstants.VERSION + ")";
    private static final Logger LOG = LoggerFactory.getLogger(SharedWebHook.class);
    private static WebHookErrorHandler DEFAULT_ERROR_HANDLER = WebHookErrorHandler.DEFAULT;

    private final String url;
    private final long id;
    private final long threadId;
    private final OkHttpClient client;
    private final ScheduledExecutorService pool;
    private final Bucket bucket;
    private final BlockingQueue<Request> queue;
    private final boolean parseMessage;
    private final AllowedMentionsEvent allowedMentions;
    private SharedWebHook parent;

    @Setter
    private WebHookErrorHandler errorHandler = DEFAULT_ERROR_HANDLER;

    @Setter
    private long defaultTimeout;

    private volatile boolean isQueued;
    private boolean isShutdown;

    public SharedWebHook(long id, String token, boolean parseMessage, OkHttpClient client, ScheduledExecutorService pool, AllowedMentionsEvent mentions, long threadId) {
        this.client = client;
        this.id = id;
        this.threadId = threadId;
        this.parseMessage = parseMessage;
        this.url = String.format(Locale.ROOT, WEBHOOK_URL, Long.toUnsignedString(id), token);
        this.pool = pool;
        this.bucket = new Bucket();
        this.queue = new LinkedBlockingQueue<>();
        this.allowedMentions = mentions;
        this.parent = null;
        this.isQueued = false;
    }

    protected SharedWebHook(SharedWebHook parent, long threadId) {
        this(parent.id, parent.url, parent.parseMessage, parent.client, parent.pool, parent.allowedMentions, threadId);
        this.parent = parent;
    }

    public SharedWebHook(String url, long id, long threadId, OkHttpClient client, ScheduledExecutorService pool, Bucket bucket, BlockingQueue<Request> queue, boolean parseMessage, AllowedMentionsEvent allowedMentions) {

        this.url = url;
        this.id = id;
        this.threadId = threadId;
        this.client = client;
        this.pool = pool;
        this.bucket = bucket;
        this.queue = queue;
        this.parseMessage = parseMessage;
        this.allowedMentions = allowedMentions;
    }

    public static void setDefaultErrorHandler(@NotNull WebHookErrorHandler handler) {
        DEFAULT_ERROR_HANDLER = Objects.requireNonNull(handler, "Error Handler must not be null!");
    }

    @NotNull
    public static SharedWebHook withId(long id, @NotNull String token) {
        Objects.requireNonNull(token, "Token");
        ScheduledExecutorService pool = ThreadPoolExecutorLogged.getDefaultPool(id, null, false);
        return new SharedWebHook(id, token, true, new OkHttpClient(), pool, AllowedMentionsEvent.allMentions(), 0);
    }

    @NotNull
    public static SharedWebHook withUrl(@NotNull String url) {
        Objects.requireNonNull(url, "URL");
        Matcher matcher = WebHookComponent.WEBHOOK_PATTERN.matcher(url);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Failed to parse webhook URL");
        }
        return withId(Long.parseUnsignedLong(matcher.group(1)), matcher.group(2));
    }

    @NotNull
    public SharedWebHook onThread(final long threadId) {
        return new SharedWebHook(this, threadId);
    }

    public boolean isShutdown() {
        return isShutdown;
    }

    @NotNull
    @SuppressWarnings("ConstantConditions")
    public SharedWebHook setTimeout(long millis) {
        if (millis < 0)
            throw new IllegalArgumentException("Cannot set a negative timeout");
        this.defaultTimeout = millis;
        return this;
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> send(@NotNull WebhookMessage message) {
        Objects.requireNonNull(message, "WebhookMessage");
        return execute(message.getBody());
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> send(@NotNull File file) {
        Objects.requireNonNull(file, "File");
        return send(file, file.getName());
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> send(@NotNull File file, @NotNull String fileName) {
        return send(new WebHookMessageComponent()
                .setAllowedMentions(allowedMentions));
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> send(@NotNull byte[] data, @NotNull String fileName) {
        return send(new WebHookMessageComponent()
                .setAllowedMentions(allowedMentions));
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> send(@NotNull InputStream data, @NotNull String fileName) {
        return send(new WebHookMessageComponent()
                .setAllowedMentions(allowedMentions));
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> send(@NotNull WebHookEmbed first, @NotNull WebHookEmbed... embeds) {
        return send(WebhookMessage.embeds(first, embeds));
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> send(@NotNull Collection<WebHookEmbed> embeds) {
        return send(WebhookMessage.embeds(embeds));
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> send(@NotNull String content) {
        Objects.requireNonNull(content, "Content");
        content = content.trim();
        if (content.isEmpty())
            throw new IllegalArgumentException("Cannot send an empty message");
        if (content.length() > 2000)
            throw new IllegalArgumentException("Content may not exceed 2000 characters");
        return execute(newBody(newJson().put("content", content).toString()));
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> edit(long messageId, @NotNull WebhookMessage message) {
        Objects.requireNonNull(message, "WebhookMessage");
        return execute(message.getBody(), Long.toUnsignedString(messageId), RequestType.EDIT);
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> edit(long messageId, @NotNull WebHookEmbed first, @NotNull WebHookEmbed... embeds) {
        return edit(messageId, WebhookMessage.embeds(first, embeds));
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> edit(long messageId, @NotNull Collection<WebHookEmbed> embeds) {
        return edit(messageId, WebhookMessage.embeds(embeds));
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> edit(long messageId, @NotNull String content) {
        Objects.requireNonNull(content, "Content");
        content = content.trim();
        if (content.isEmpty())
            throw new IllegalArgumentException("Cannot send an empty message");
        if (content.length() > 2000)
            throw new IllegalArgumentException("Content may not exceed 2000 characters");
        return edit(messageId, new WebHookMessageComponent().setContent(content).build());
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> edit(@NotNull String messageId, @NotNull WebhookMessage message) {
        Objects.requireNonNull(message, "WebhookMessage");
        return execute(message.getBody(), messageId, RequestType.EDIT);
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> edit(@NotNull String messageId, @NotNull WebHookEmbed first, @NotNull WebHookEmbed... embeds) {
        return edit(messageId, WebhookMessage.embeds(first, embeds));
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> edit(@NotNull String messageId, @NotNull Collection<WebHookEmbed> embeds) {
        return edit(messageId, WebhookMessage.embeds(embeds));
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> edit(@NotNull String messageId, @NotNull String content) {
        Objects.requireNonNull(content, "Content");
        content = content.trim();
        if (content.isEmpty())
            throw new IllegalArgumentException("Cannot send an empty message");
        if (content.length() > 2000)
            throw new IllegalArgumentException("Content may not exceed 2000 characters");
        return edit(messageId, new WebHookMessageComponent().setContent(content).build());
    }

    @NotNull
    public CompletableFuture<Void> delete(long messageId) {
        return execute(null, Long.toUnsignedString(messageId), RequestType.DELETE).thenApply(v -> null);
    }

    @NotNull
    public CompletableFuture<Void> delete(@NotNull String messageId) {
        return execute(null, messageId, RequestType.DELETE).thenApply(v -> null);
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> get(long messageId) {
        return get(Long.toUnsignedString(messageId));
    }

    @NotNull
    public CompletableFuture<ReadonlyMessage> get(@NotNull String messageId) {
        return execute(null, messageId, RequestType.GET);
    }

    private JSONObject newJson() {
        JSONObject json = new JSONObject();
        json.put("allowed_mentions", allowedMentions);
        return json;
    }

    @Override
    public void close() {
        isShutdown = true;
        if (parent != null)
            parent.close();
        if (queue.isEmpty())
            pool.shutdown();
    }

    protected void checkShutdown() {
        if (isShutdown)
            throw new RejectedExecutionException("Cannot send to closed client!");
    }

    @NotNull
    protected static RequestBody newBody(String object) {
        return RequestBody.create(IoUtils.JSON, object);
    }

    @NotNull
    protected CompletableFuture<ReadonlyMessage> execute(RequestBody body, @Nullable String messageId, @NotNull RequestType type) {
        checkShutdown();
        String endpoint = url;
        if (type != RequestType.SEND) {
            Objects.requireNonNull(messageId, "Message ID");
            endpoint += "/messages/" + messageId;
        }
        List<String> query = new ArrayList<>(2);
        if (parseMessage)
            query.add("wait=true");
        if (threadId != 0L)
            query.add("thread_id=" + Long.toUnsignedString(threadId));
        if (!query.isEmpty())
            endpoint += "?" + String.join("&", query);
        return queueRequest(endpoint, type.method, body);
    }

    @NotNull
    protected CompletableFuture<ReadonlyMessage> execute(RequestBody body) {
        return execute(body, null, RequestType.SEND);
    }

    @NotNull
    protected static HttpException failure(Response response) throws IOException {
        final InputStream stream = IoUtils.getBody(response);
        final String responseBody = stream == null ? "" : new String(IoUtils.readAllBytes(stream));

        return new HttpException(response.code(), responseBody, response.headers());
    }

    @NotNull
    protected CompletableFuture<ReadonlyMessage> queueRequest(String url, String method, RequestBody body) {
        CompletableFuture<ReadonlyMessage> callback = new CompletableFuture<>();
        Request req = new Request(callback, body, method, url);
        if (defaultTimeout > 0)
            req.deadline = System.currentTimeMillis() + defaultTimeout;

        return parent == null ? schedule(callback, req) : parent.schedule(callback, req);
    }

    @NotNull
    protected CompletableFuture<ReadonlyMessage> schedule(@NotNull CompletableFuture<ReadonlyMessage> callback, @NotNull Request req) {
        enqueuePair(req);
        if (!isQueued)
            backoffQueue();
        isQueued = true;
        return callback;
    }

    @NotNull
    protected okhttp3.Request newRequest(Request request) {
        return new okhttp3.Request.Builder()
                .url(request.url)
                .method(request.method, request.body)
                .header("accept-encoding", "gzip")
                .header("user-agent", USER_AGENT)
                .build();
    }

    protected void backoffQueue() {
        long delay = bucket.retryAfter();
        if (delay > 0)
            LOG.debug("Backing off queue for {}", delay);
        pool.schedule(this::drainQueue, delay, TimeUnit.MILLISECONDS);
    }

    protected synchronized void drainQueue() {
        boolean graceful = true;
        while (!queue.isEmpty()) {
            final Request pair = queue.peek();
            graceful = executePair(pair);
            if (!graceful)
                break;
        }
        isQueued = !graceful;
        if (isShutdown && graceful)
            pool.shutdown();
    }

    private void enqueuePair(Request pair) {
        queue.add(pair);
    }

    private boolean executePair(Request req) {
        if (req.future.isDone()) {
            queue.poll();
            return true;
        } else if (req.deadline > 0 && req.deadline < System.currentTimeMillis()) {
            req.future.completeExceptionally(new TimeoutException());
            queue.poll();
            return true;
        }

        final okhttp3.Request request = newRequest(req);
        try (Response response = client.newCall(request).execute()) {
            bucket.update(response);
            if (response.code() == Bucket.RATE_LIMIT_CODE) {
                backoffQueue();
                return false;
            } else if (!response.isSuccessful()) {
                final HttpException exception = failure(response);
                errorHandler.handle(this, "Sending a webhook message failed with non-OK http response", exception);
                Objects.requireNonNull(queue.poll()).future.completeExceptionally(exception);
                return true;
            }
            ReadonlyMessage message = null;
            if (parseMessage && !"DELETE".equals(req.method)) {
                InputStream body = IoUtils.getBody(response);
                assert body != null;
                JSONObject json = IoUtils.toJSON(body);
                message = EntityFactory.makeMessage(json);
            }
            Objects.requireNonNull(queue.poll()).future.complete(message);
            if (bucket.isRateLimit()) {
                backoffQueue();
                return false;
            }
        } catch (JSONException | IOException e) {
            errorHandler.handle(this, "There was some error while sending a webhook message", e);
            Objects.requireNonNull(queue.poll()).future.completeExceptionally(e);
        }
        return true;
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    @SuppressWarnings("FieldMayBeFinal")
    private static final class Request {
        private final CompletableFuture<ReadonlyMessage> future;
        private final RequestBody body;
        private final String method, url;
        private long deadline;
    }

    @Getter
    @RequiredArgsConstructor
    @SuppressWarnings("FieldMayBeFinal")
    enum RequestType {
        SEND("POST"), EDIT("PATCH"), DELETE("DELETE"), GET("GET");

        private final String method;
    }

    @Getter
    protected final class Bucket {
        public static final int RATE_LIMIT_CODE = 429;
        private long resetTime;
        private int remainingUses;
        private int limit = Integer.MAX_VALUE;

        public synchronized boolean isRateLimit() {
            if (retryAfter() <= 0)
                remainingUses = limit;
            return remainingUses <= 0;
        }

        public synchronized long retryAfter() {
            return resetTime - System.currentTimeMillis();
        }

        private synchronized void handleRateLimit(Response response, long current) throws IOException {
            final String retryAfter = response.header("Retry-After");
            final String limitHeader = response.header("X-RateLimit-Limit", "5");
            long delay;
            if (retryAfter == null) {
                InputStream stream = IoUtils.getBody(response);
                if (stream == null)
                    delay = 30000;
                else {
                    final JSONObject body = IoUtils.toJSON(stream);
                    delay = (long) Math.ceil(body.getDouble("retry_after")) * 1000;
                }
            } else {
                delay = Long.parseLong(retryAfter) * 1000;
            }
            LOG.error("Encountered 429, retrying after {} ms", delay);
            resetTime = current + delay;
            remainingUses = 0;
            assert limitHeader != null;
            limit = Integer.parseInt(limitHeader);
        }

        private synchronized void update0(Response response) throws IOException {
            final long current = System.currentTimeMillis();
            final boolean is429 = response.code() == RATE_LIMIT_CODE;
            final String remainingHeader = response.header("X-RateLimit-Remaining");
            final String limitHeader = response.header("X-RateLimit-Limit");
            final String resetHeader = response.header("X-RateLimit-Reset-After");
            if (is429) {
                handleRateLimit(response, current);
                return;
            } else if (remainingHeader == null || limitHeader == null || resetHeader == null) {
                LOG.debug("Failed to update buckets due to missing headers in response with code: {} and headers: \n{}",
                        response.code(), response.headers());
                return;
            }
            remainingUses = Integer.parseInt(remainingHeader);
            limit = Integer.parseInt(limitHeader);

            final long reset = (long) Math.ceil(Double.parseDouble(resetHeader));
            final long delay = reset * 1000;
            resetTime = current + delay;
        }

        public void update(Response response) {
            try {
                update0(response);
            } catch (Exception ex) {
                errorHandler.handle(SharedWebHook.this, "Could not read http response", ex);
            }
        }
    }
}









