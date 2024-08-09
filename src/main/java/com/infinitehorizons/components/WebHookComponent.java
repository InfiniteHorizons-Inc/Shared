package com.infinitehorizons.components;

import com.infinitehorizons.SharedWebHook;
import com.infinitehorizons.events.AllowedMentionsEvent;
import com.infinitehorizons.models.ReadonlyMessage;
import com.infinitehorizons.utils.ThreadPoolExecutorLogged;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.Webhook;
import okhttp3.OkHttpClient;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder for creating {@link SharedWebHook} instances with configurable options.
 *
 * @see SharedWebHook#withId(long, String)
 * @see SharedWebHook#withUrl(String)
 */
@Getter
@Setter
@Accessors(chain = true, fluent = true)
public class WebHookComponent {

    /**
     * Pattern used to validate webhook URLs.
     * {@code (?:https?://)?(?:\w+\.)?discord(?:app)?\.com/api(?:/v\d+)?/webhooks/(\d+)/([\w-]+)(?:/(?:\w+)?)?}
     */
    public static final Pattern WEBHOOK_PATTERN = Pattern.compile(
            "(?:https?://)?(?:\\w+\\.)?discord(?:app)?\\.com/api(?:/v\\d+)?/webhooks/(\\d+)/([\\w-]+)(?:/(?:\\w+)?)?");

    private final long id;
    private final String token;
    private long threadId = 0;
    private ScheduledExecutorService pool;
    private OkHttpClient client;
    private ThreadFactory threadFactory;
    private AllowedMentionsEvent allowedMentions = AllowedMentionsEvent.allMentions();
    private boolean isDaemon;
    private boolean parseMessage = true;

    /**
     * Creates a new WebhookClientBuilder for the specified webhook components.
     *
     * @param id    The webhook id.
     * @param token The webhook token.
     * @throws NullPointerException If the token is null.
     */
    public WebHookComponent(final long id, @NotNull final String token) {
        this.id = id;
        this.token = Objects.requireNonNull(token, "Token cannot be null");
    }

    /**
     * Creates a new WebhookClientBuilder for the specified webhook URL.
     * The URL is verified using {@link #WEBHOOK_PATTERN}.
     *
     * @param url The URL to use.
     * @throws NullPointerException     If the URL is null.
     * @throws IllegalArgumentException If the URL is not valid.
     */
    public WebHookComponent(@NotNull String url) {
        Objects.requireNonNull(url, "URL cannot be null");
        Matcher matcher = WEBHOOK_PATTERN.matcher(url);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Failed to parse webhook URL");
        }

        this.id = Long.parseUnsignedLong(matcher.group(1));
        this.token = matcher.group(2);
    }

    /**
     * Creates a WebhookClientBuilder for the provided JDA webhook.
     *
     * @param webhook The JDA webhook.
     * @return The WebhookClientBuilder.
     * @throws NullPointerException If the webhook is null or doesn't provide a token.
     */
    @NotNull
    public static WebHookComponent fromJDA(@NotNull Webhook webhook) {
        Objects.requireNonNull(webhook, "Webhook cannot be null");
        return new WebHookComponent(webhook.getIdLong(), Objects.requireNonNull(webhook.getToken(), "Webhook Token cannot be null"));
    }

    /**
     * Creates a WebhookClientBuilder for the provided Discord4J webhook.
     *
     * @param webhook The Discord4J webhook.
     * @return The WebhookClientBuilder.
     * @throws NullPointerException If the webhook is null or doesn't provide a token.
     */
    @NotNull
    public static WebHookComponent fromD4J(@NotNull discord4j.core.object.entity.Webhook webhook) {
        Objects.requireNonNull(webhook, "Webhook cannot be null");
        String token = webhook.getToken().orElseThrow(() -> new NullPointerException("Webhook Token is missing"));
        return new WebHookComponent(webhook.getId().asLong(), token);
    }

    /**
     * Creates a WebhookClientBuilder for the provided Java cord webhook.
     *
     * @param webhook The Java cord webhook.
     * @return The WebhookClientBuilder.
     * @throws NullPointerException If the webhook is null or doesn't provide a token.
     */
    @NotNull
    public static WebHookComponent fromJavacord(@NotNull org.javacord.api.entity.webhook.Webhook webhook) {
        Objects.requireNonNull(webhook, "Webhook cannot be null");
        return new WebHookComponent(webhook.getId(),
                webhook.asIncomingWebhook()
                        .map(IncomingWebhook::getToken)
                        .orElseThrow(() -> new NullPointerException("Webhook Token is missing")));
    }

    /**
     * Sets the {@link ScheduledExecutorService} that is used to execute send requests in the resulting {@link SharedWebHook}.
     * This will be closed by a call to {@link SharedWebHook#close()}.
     *
     * @param executorService The executor service to use.
     * @return The current builder, for chaining convenience.
     */
    @NotNull
    public WebHookComponent executorService(@Nullable ScheduledExecutorService executorService) {
        this.pool = executorService;
        return this;
    }

    /**
     * Sets the {@link OkHttpClient} that is used to execute send requests in the resulting {@link SharedWebHook}.
     * It is usually not necessary to use multiple different clients in one application.
     *
     * @param client The HTTP client to use.
     * @return The current builder, for chaining convenience.
     */
    @NotNull
    public WebHookComponent httpClient(@Nullable OkHttpClient client) {
        this.client = client;
        return this;
    }

    /**
     * Sets the {@link ThreadFactory} that is used to initialize the default {@link ScheduledExecutorService} used if
     * {@link #executorService(ScheduledExecutorService)} is not configured.
     *
     * @param factory The factory to use.
     * @return The current builder, for chaining convenience.
     */
    @NotNull
    public WebHookComponent threadFactory(@Nullable ThreadFactory factory) {
        this.threadFactory = factory;
        return this;
    }

    /**
     * Sets the default mention allowlist for every outgoing message.
     * See {@link AllowedMentionsEvent} for more details.
     *
     * @param mentions The mention allowlist.
     * @return The current builder, for chaining convenience.
     */
    @NotNull
    public WebHookComponent allowedMentions(@Nullable AllowedMentionsEvent mentions) {
        this.allowedMentions = mentions == null ? AllowedMentionsEvent.allMentions() : mentions;
        return this;
    }

    /**
     * Sets whether the default executor should use daemon threads.
     * This has no effect if either {@link #executorService(ScheduledExecutorService)} or
     * {@link #threadFactory(ThreadFactory)} are configured to non-null values.
     *
     * @param isDaemon Whether to use daemon threads or not.
     * @return The current builder, for chaining convenience.
     */
    @NotNull
    public WebHookComponent daemon(boolean isDaemon) {
        this.isDaemon = isDaemon;
        return this;
    }

    /**
     * Sets whether resulting messages should be parsed after sending.
     * If this is set to {@code false}, the futures returned by {@link SharedWebHook}
     * will receive {@code null} instead of instances of {@link ReadonlyMessage}.
     *
     * @param waitForMessage True, if the client should parse resulting messages (default behavior).
     * @return The current builder, for chaining convenience.
     */
    @NotNull
    public WebHookComponent waitForMessage(boolean waitForMessage) {
        this.parseMessage = waitForMessage;
        return this;
    }

    /**
     * Sets the ID for the thread you want the messages to be posted to.
     * You can use {@link SharedWebHook#onThread(long)} to send specific messages to threads.
     *
     * @param threadId The target thread id, or 0 to not use threads.
     * @return The current builder, for chaining convenience.
     */
    @NotNull
    public WebHookComponent threadId(long threadId) {
        this.threadId = threadId;
        return this;
    }

    /**
     * Builds the {@link SharedWebHook} with the current settings.
     *
     * @return The {@link SharedWebHook} instance.
     */
    @NotNull
    public SharedWebHook build() {
        OkHttpClient client = this.client == null ? new OkHttpClient() : this.client;
        ScheduledExecutorService pool = this.pool != null ? this.pool : ThreadPoolExecutorLogged.getDefaultPool(id, threadFactory, isDaemon);
        return new SharedWebHook(id, token, parseMessage, client, pool, allowedMentions, threadId);
    }

    /**
     * Builds the {@link D4JWebHookComponent} with the current settings.
     *
     * @return The {@link JDAWebHookComponent} instance.
     */
    @NotNull
    public JDAWebHookComponent buildJDA() {
        return (JDAWebHookComponent) buildClient(new JDAWebHookComponent(id, token, parseMessage, client, pool, allowedMentions, threadId));
    }

    /**
     * Builds the {@link D4JWebHookComponent} with the current settings.
     *
     * @return The {@link D4JWebHookComponent} instance.
     */
    @NotNull
    public D4JWebHookComponent buildD4J() {
        return (D4JWebHookComponent) buildClient(new D4JWebHookComponent(id, token, parseMessage, client, pool, allowedMentions, threadId));
    }

    /**
     * Builds the {@link JavacordWebhookComponent} with the current settings.
     *
     * @return The {@link JavacordWebhookComponent} instance.
     */
    @NotNull
    public JavacordWebhookComponent buildJavacord() {
        return (JavacordWebhookComponent) buildClient(new JavacordWebhookComponent(id, token, parseMessage, client, pool, allowedMentions, threadId));
    }

    private SharedWebHook buildClient(SharedWebHook client) {
        ScheduledExecutorService pool = this.pool != null ? this.pool : ThreadPoolExecutorLogged.getDefaultPool(id, threadFactory, isDaemon);
        return new SharedWebHook(id, token, parseMessage, this.client, pool, allowedMentions, threadId);
    }
}
