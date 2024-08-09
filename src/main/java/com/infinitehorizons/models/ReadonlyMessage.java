package com.infinitehorizons.models;

import com.infinitehorizons.SharedWebHook;
import com.infinitehorizons.models.send.WebhookMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.json.JSONString;

import java.util.List;

/**
 * Represents a read-only message used as a response from the {@link SharedWebHook} send methods.
 *
 * <p>This class provides a snapshot of a message with details about its content, author, and metadata.</p>
 *
 * @see #toWebhookMessage()
 */
@Getter
@RequiredArgsConstructor
public class ReadonlyMessage implements JSONString {
    private final long id;
    private final long channelId;
    private final boolean mentionsEveryone;
    private final boolean tts;
    private final int flags;

    @NotNull
    private final ReadonlyUser author;

    @NotNull
    private final String content;

    @NotNull
    private final List<ReadonlyEmbed> embeds;

    @NotNull
    private final List<ReadonlyAttachment> attachments;

    @NotNull
    private final List<ReadonlyUser> mentionedUsers;

    @NotNull
    private final List<Long> mentionedRoles;

    /**
     * Converts this read-only message to a {@link WebhookMessage} for sending.
     *
     * @return A WebhookMessage representation of this read-only message.
     */
    @NotNull
    public WebhookMessage toWebhookMessage() {
        return WebhookMessage.from(this);
    }

    /**
     * Returns the JSON string representation of this message.
     *
     * @return The JSON representation.
     */
    @Override
    public String toJSONString() {
        return new JSONObject()
                .put("content", content)
                .put("embeds", embeds)
                .put("mentions", mentionedUsers)
                .put("mention_roles", mentionedRoles)
                .put("attachments", attachments)
                .put("author", author)
                .put("tts", tts)
                .put("id", Long.toUnsignedString(id))
                .put("channel_id", Long.toUnsignedString(channelId))
                .put("mention_everyone", mentionsEveryone)
                .toString();
    }

    @Override
    public String toString() {
        return toJSONString();
    }
}
