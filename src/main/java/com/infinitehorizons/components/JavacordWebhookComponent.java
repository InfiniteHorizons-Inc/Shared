package com.infinitehorizons.components;

import com.infinitehorizons.SharedWebHook;
import com.infinitehorizons.events.AllowedMentionsEvent;
import com.infinitehorizons.models.ReadonlyMessage;
import com.infinitehorizons.utils.ThreadPoolExecutorLogged;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import okhttp3.OkHttpClient;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.Embed;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;

/**
 * A component for interacting with Discord webhooks using Javacord.
 *
 * <p>This component allows sending and editing messages and embeds through Discord webhooks.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * JavacordWebhookComponent client = JavacordWebhookComponent.withId(123456789L, "webhookToken");
 * client.send(message).thenAccept(System.out::println);
 * }</pre>
 *
 * @see WebHookComponent
 * @see AllowedMentionsEvent
 */
public class JavacordWebhookComponent extends SharedWebHook {

    public JavacordWebhookComponent(long id, @NonNull String token, boolean parseMessage, @NonNull OkHttpClient client,
                                    @NonNull ScheduledExecutorService pool, @NonNull AllowedMentionsEvent mentions) {
        this(id, token, parseMessage, client, pool, mentions, 0L);
    }

    public JavacordWebhookComponent(long id, @NonNull String token, boolean parseMessage, @NonNull OkHttpClient client,
                                    @NonNull ScheduledExecutorService pool, @NonNull AllowedMentionsEvent mentions, long threadId) {
        super(id, token, parseMessage, client, pool, mentions, threadId);
    }

    protected JavacordWebhookComponent(@NonNull JavacordWebhookComponent parent, long threadId) {
        super(parent, threadId);
    }

    /**
     * Creates a JavacordWebhookComponent from a Javacord Webhook entity.
     *
     * @param webhook The Javacord Webhook entity.
     * @return The created JavacordWebhookComponent.
     * @throws NullPointerException If the webhook is null or doesn't provide a token.
     */
    @NotNull
    public static JavacordWebhookComponent from(@NonNull org.javacord.api.entity.webhook.Webhook webhook) {
        return WebHookComponent.fromJavacord(webhook).buildJavacord();
    }

    /**
     * Creates a JavacordWebhookComponent with the specified webhook ID and token.
     *
     * @param id    The webhook ID.
     * @param token The webhook token.
     * @return The created JavacordWebhookComponent.
     * @throws NullPointerException If the token is null.
     */
    @NotNull
    public static JavacordWebhookComponent withId(long id, @NonNull String token) {
        ScheduledExecutorService pool = ThreadPoolExecutorLogged.getDefaultPool(id, null, false);
        return new JavacordWebhookComponent(id, token, true, new OkHttpClient(), pool, AllowedMentionsEvent.allMentions());
    }

    /**
     * Creates a JavacordWebhookComponent from a webhook URL.
     *
     * @param url The webhook URL.
     * @return The created JavacordWebhookComponent.
     * @throws NullPointerException     If the URL is null.
     * @throws IllegalArgumentException If the URL is not a valid webhook URL.
     */
    @NotNull
    public static JavacordWebhookComponent withUrl(@NonNull String url) {
        Matcher matcher = WebHookComponent.WEBHOOK_PATTERN.matcher(url);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Failed to parse webhook URL");
        }
        return withId(Long.parseUnsignedLong(matcher.group(1)), matcher.group(2));
    }

    @NotNull
    @Override
    public JavacordWebhookComponent onThread(long threadId) {
        return new JavacordWebhookComponent(this, threadId);
    }

    /**
     * Sends a {@link Message} to the webhook.
     *
     * @param message The message to send.
     * @return A {@link CompletableFuture} that completes with the scent message.
     * @throws NullPointerException If the message is null.
     * @see WebHookMessageComponent#fromJavacord(Message)
     */
    @NotNull
    public CompletableFuture<ReadonlyMessage> send(@NonNull Message message) {
        return send(WebHookMessageComponent.fromJavacord(message).build());
    }

    /**
     * Sends an {@link Embed} to the webhook.
     *
     * @param embed The embed to send.
     * @return A {@link CompletableFuture} that completes with the scent message.
     * @throws NullPointerException If the embed is null.
     * @see WebHookEmbedComponent#fromJavacord(Embed)
     */
    @NotNull
    public CompletableFuture<ReadonlyMessage> send(@NonNull Embed embed) {
        return send(WebHookEmbedComponent.fromJavacord(embed).build());
    }

    /**
     * Edits a message with the specified ID using a {@link Message}.
     *
     * @param messageId The ID of the message to edit.
     * @param message   The new message content.
     * @return A {@link CompletableFuture} that completes with the edited message.
     * @throws NullPointerException If the message is null.
     * @see WebHookMessageComponent#fromJavacord(Message)
     */
    @NotNull
    public CompletableFuture<ReadonlyMessage> edit(long messageId, @NonNull Message message) {
        return edit(messageId, WebHookMessageComponent.fromJavacord(message).build());
    }

    /**
     * Edits a message with the specified ID using an {@link Embed}.
     *
     * @param messageId The ID of the message to edit.
     * @param embed     The new embed content.
     * @return A {@link CompletableFuture} that completes with the edited message.
     * @throws NullPointerException If the embed is null.
     * @see WebHookEmbedComponent#fromJavacord(Embed)
     */
    @NotNull
    public CompletableFuture<ReadonlyMessage> edit(long messageId, @NonNull Embed embed) {
        return edit(messageId, WebHookEmbedComponent.fromJavacord(embed).build());
    }

    /**
     * Edits a message with the specified ID using a {@link Message}.
     *
     * @param messageId The ID of the message to edit.
     * @param message   The new message content.
     * @return A {@link CompletableFuture} that completes with the edited message.
     * @throws NullPointerException If the message is null.
     * @see WebHookMessageComponent#fromJavacord(Message)
     */
    @NotNull
    public CompletableFuture<ReadonlyMessage> edit(@NonNull String messageId, @NonNull Message message) {
        return edit(messageId, WebHookMessageComponent.fromJavacord(message).build());
    }

    /**
     * Edits a message with the specified ID using an {@link Embed}.
     *
     * @param messageId The ID of the message to edit.
     * @param embed     The new embed content.
     * @return A {@link CompletableFuture} that completes with the edited message.
     * @throws NullPointerException If the embed is null.
     * @see WebHookEmbedComponent#fromJavacord(Embed)
     */
    @NotNull
    public CompletableFuture<ReadonlyMessage> edit(@NonNull String messageId, @NonNull Embed embed) {
        return edit(messageId, WebHookEmbedComponent.fromJavacord(embed).build());
    }
}
