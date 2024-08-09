package com.infinitehorizons.components;

import com.google.errorprone.annotations.CheckReturnValue;
import com.infinitehorizons.SharedWebHook;
import com.infinitehorizons.events.AllowedMentionsEvent;
import com.infinitehorizons.models.ReadonlyMessage;
import com.infinitehorizons.models.send.WebhookMessage;
import com.infinitehorizons.utils.ThreadPoolExecutorLogged;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import lombok.NonNull;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;

/**
 * A component for interacting with Discord webhooks using Discord4J.
 *
 * <p>This class allows sending and editing messages through Discord webhooks, with support for specifying
 * allowed mentions and threading.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * D4JWebhookComponent client = D4JWebhookComponent.withId(123456789L, "webhookToken");
 * client.send(spec -> spec.setContent("Hello, world!")).subscribe();
 * }</pre>
 *
 * @see WebHookComponent
 * @see AllowedMentionsEvent
 */
public class D4JWebHookComponent extends SharedWebHook {

    public D4JWebHookComponent(long id, @NonNull String token, boolean parseMessage, @NonNull OkHttpClient client,
                               @NonNull ScheduledExecutorService pool, @NonNull AllowedMentionsEvent mentions) {
        this(id, token, parseMessage, client, pool, mentions, 0L);
    }

    public D4JWebHookComponent(long id, @NonNull String token, boolean parseMessage, @NonNull OkHttpClient client,
                               @NonNull ScheduledExecutorService pool, @NonNull AllowedMentionsEvent mentions, long threadId) {
        super(id, token, parseMessage, client, pool, mentions, threadId);
    }

    protected D4JWebHookComponent(@NonNull D4JWebHookComponent parent, long threadId) {
        super(parent, threadId);
    }

    /**
     * Creates a D4JWebhookClient from a Discord4J Webhook entity.
     *
     * @param webhook The Discord4J Webhook entity.
     * @return The created D4JWebhookClient.
     * @throws NullPointerException If the webhook is null or doesn't provide a token.
     */
    @NotNull
    public static D4JWebHookComponent from(@NonNull discord4j.core.object.entity.Webhook webhook) {
        return WebHookComponent.fromD4J(webhook).buildD4J();
    }

    /**
     * Creates a D4JWebhookClient with the specified webhook ID and token.
     *
     * @param id    The webhook ID.
     * @param token The webhook token.
     * @return The created D4JWebhookClient.
     * @throws NullPointerException If the token is null.
     */
    @NotNull
    public static D4JWebHookComponent withId(long id, @NonNull String token) {
        ScheduledExecutorService pool = ThreadPoolExecutorLogged.getDefaultPool(id, null, false);
        return new D4JWebHookComponent(id, token, true, new OkHttpClient(), pool, AllowedMentionsEvent.allMentions(), 0L);
    }

    /**
     * Creates a D4JWebhookClient from a webhook URL.
     *
     * @param url The webhook URL.
     * @return The created D4JWebhookClient.
     * @throws NullPointerException     If the URL is null.
     * @throws IllegalArgumentException If the URL is not a valid webhook URL.
     */
    @NotNull
    public static D4JWebHookComponent withUrl(@NonNull String url) {
        Matcher matcher = WebHookComponent.WEBHOOK_PATTERN.matcher(url);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Failed to parse webhook URL");
        }
        return withId(Long.parseUnsignedLong(matcher.group(1)), matcher.group(2));
    }

    @NotNull
    @Override
    public D4JWebHookComponent onThread(long threadId) {
        return new D4JWebHookComponent(this, threadId);
    }

    /**
     * Sends a message using a {@link MessageCreateSpec} to the webhook.
     *
     * @param spec The message creates a specification.
     * @return A {@link Mono} that completes when the message is sent.
     * @throws NullPointerException If the spec is null.
     * @see WebHookMessageComponent#fromD4J(MessageCreateSpec)
     */
    @NotNull
    @CheckReturnValue
    public Mono<ReadonlyMessage> send(@NonNull MessageCreateSpec spec) {
        WebhookMessage message = WebHookMessageComponent.fromD4J(spec).build();
        return Mono.fromFuture(() -> send(message));
    }

    /**
     * Edits a message with the specified ID using a {@link MessageEditSpec}.
     *
     * @param messageId The ID of the message to edit.
     * @param spec      The message edit specification.
     * @return A {@link Mono} that completes when the message is edited.
     * @throws NullPointerException If the spec is null.
     * @see WebHookMessageComponent#fromD4J(MessageEditSpec)
     */
    @NotNull
    @CheckReturnValue
    public Mono<ReadonlyMessage> edit(long messageId, @NonNull MessageEditSpec spec) {
        WebhookMessage message = WebHookMessageComponent.fromD4J(spec).build();
        return Mono.fromFuture(() -> edit(messageId, message));
    }

    /**
     * Edits a message with the specified ID using a {@link MessageEditSpec}.
     *
     * @param messageId The ID of the message to edit.
     * @param spec      The message edit specification.
     * @return A {@link Mono} that completes when the message is edited.
     * @throws NullPointerException If the spec is null.
     * @see WebHookMessageComponent#fromD4J(MessageEditSpec)
     */
    @NotNull
    @CheckReturnValue
    public Mono<ReadonlyMessage> edit(@NonNull String messageId, @NonNull MessageEditSpec spec) {
        WebhookMessage message = WebHookMessageComponent.fromD4J(spec).build();
        return Mono.fromFuture(() -> edit(messageId, message));
    }
}
