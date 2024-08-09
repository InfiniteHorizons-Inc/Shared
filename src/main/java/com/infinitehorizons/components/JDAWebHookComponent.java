package com.infinitehorizons.components;

import com.infinitehorizons.SharedWebHook;
import com.infinitehorizons.events.AllowedMentionsEvent;
import com.infinitehorizons.models.ReadonlyMessage;
import com.infinitehorizons.utils.ThreadPoolExecutorLogged;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;

/**
 * A component for sending and editing messages through Discord webhooks using JDA.
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * JDAWebHookComponent client = JDAWebHookComponent.withId(123456789L, "webhookToken");
 * client.send(message).thenAccept(System.out::println);
 * }</pre>
 *
 * @see WebHookComponent
 * @see AllowedMentionsEvent
 */
public class JDAWebHookComponent extends SharedWebHook {

    public JDAWebHookComponent(long id, @NonNull String token, boolean parseMessage, @NonNull OkHttpClient client,
                               @NonNull ScheduledExecutorService pool, @NonNull AllowedMentionsEvent mentions) {
        this(id, token, parseMessage, client, pool, mentions, 0L);
    }

    public JDAWebHookComponent(long id, @NonNull String token, boolean parseMessage, @NonNull OkHttpClient client,
                            @NonNull ScheduledExecutorService pool, @NonNull AllowedMentionsEvent mentions, long threadId) {
        super(id, token, parseMessage, client, pool, mentions, threadId);
    }

    protected JDAWebHookComponent(@NonNull JDAWebHookComponent parent, long threadId) {
        super(parent, threadId);
    }

    /**
     * Creates a JDAWebhookClient from a JDA Webhook entity.
     *
     * @param webhook The JDA Webhook entity.
     * @return The created JDAWebhookClient.
     * @throws NullPointerException If the webhook is null or does not provide a token.
     */
    @NotNull
    public static JDAWebHookComponent from(@NonNull net.dv8tion.jda.api.entities.Webhook webhook) {
        return WebHookComponent.fromJDA(webhook).buildJDA();
    }

    /**
     * Creates a JDAWebhookClient with the specified webhook ID and token.
     *
     * @param id    The webhook ID.
     * @param token The webhook token.
     * @return The created JDAWebhookClient.
     * @throws NullPointerException If the token is null.
     */
    @NotNull
    public static JDAWebHookComponent withId(long id, @NonNull String token) {
        ScheduledExecutorService pool = ThreadPoolExecutorLogged.getDefaultPool(id, null, false);
        return new JDAWebHookComponent(id, token, true, new OkHttpClient(), pool, AllowedMentionsEvent.allMentions());
    }

    /**
     * Creates a JDAWebhookClient from a webhook URL.
     *
     * @param url The webhook URL.
     * @return The created JDAWebhookClient.
     * @throws NullPointerException     If the URL is null.
     * @throws IllegalArgumentException If the URL is not a valid webhook URL.
     */
    @NotNull
    public static JDAWebHookComponent withUrl(@NonNull String url) {
        Matcher matcher = WebHookComponent.WEBHOOK_PATTERN.matcher(url);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Failed to parse webhook URL");
        }
        return withId(Long.parseUnsignedLong(matcher.group(1)), matcher.group(2));
    }

    @NotNull
    @Override
    public JDAWebHookComponent onThread(long threadId) {
        return new JDAWebHookComponent(this, threadId);
    }

    /**
     * Sends a {@link Message} to the webhook.
     *
     * @param message The message to send.
     * @return A {@link CompletableFuture} that completes with the scent message.
     * @throws NullPointerException If the message is null.
     * @see WebHookMessageComponent#fromJDA(Message)
     */
    @NotNull
    public CompletableFuture<ReadonlyMessage> send(@NonNull Message message) {
        return send(WebHookMessageComponent.fromJDA(message).build());
    }

    /**
     * Sends a {@link MessageEmbed} to the webhook.
     *
     * @param embed The embed to send.
     * @return A {@link CompletableFuture} that completes with the scent message.
     * @throws NullPointerException If the embed is null.
     * @see WebHookEmbedComponent#fromJDA(MessageEmbed)
     */
    @NotNull
    public CompletableFuture<ReadonlyMessage> send(@NonNull MessageEmbed embed) {
        return send(WebHookEmbedComponent.fromJDA(embed).build());
    }

    /**
     * Edits a message with the specified ID using a {@link Message}.
     *
     * @param messageId The ID of the message to edit.
     * @param message   The new message content.
     * @return A {@link CompletableFuture} that completes with the edited message.
     * @throws NullPointerException If the message is null.
     * @see WebHookMessageComponent#fromJDA(Message)
     */
    @NotNull
    public CompletableFuture<ReadonlyMessage> edit(long messageId, @NonNull Message message) {
        return edit(messageId, WebHookMessageComponent.fromJDA(message).build());
    }

    /**
     * Edits a message with the specified ID using a {@link MessageEmbed}.
     *
     * @param messageId The ID of the message to edit.
     * @param embed     The new embed content.
     * @return A {@link CompletableFuture} that completes with the edited message.
     * @throws NullPointerException If the embed is null.
     * @see WebHookEmbedComponent#fromJDA(MessageEmbed)
     */
    @NotNull
    public CompletableFuture<ReadonlyMessage> edit(long messageId, @NonNull MessageEmbed embed) {
        return edit(messageId, WebHookEmbedComponent.fromJDA(embed).build());
    }

    /**
     * Edits a message with the specified ID using a {@link Message}.
     *
     * @param messageId The ID of the message to edit.
     * @param message   The new message content.
     * @return A {@link CompletableFuture} that completes with the edited message.
     * @throws NullPointerException If the message is null.
     * @see WebHookMessageComponent#fromJDA(Message)
     */
    @NotNull
    public CompletableFuture<ReadonlyMessage> edit(@NonNull String messageId, @NonNull Message message) {
        return edit(messageId, WebHookMessageComponent.fromJDA(message).build());
    }

    /**
     * Edits a message with the specified ID using a {@link MessageEmbed}.
     *
     * @param messageId The ID of the message to edit.
     * @param embed     The new embed content.
     * @return A {@link CompletableFuture} that completes with the edited message.
     * @throws NullPointerException If the embed is null.
     * @see WebHookEmbedComponent#fromJDA(MessageEmbed)
     */
    @NotNull
    public CompletableFuture<ReadonlyMessage> edit(@NonNull String messageId, @NonNull MessageEmbed embed) {
        return edit(messageId, WebHookEmbedComponent.fromJDA(embed).build());
    }
}
