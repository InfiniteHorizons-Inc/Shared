package com.infinitehorizons.models.send;

import com.infinitehorizons.SharedWebHook;
import com.infinitehorizons.components.WebHookMessageComponent;
import com.infinitehorizons.constants.SharedConstants;
import com.infinitehorizons.events.AllowedMentionsEvent;
import com.infinitehorizons.models.MessageAttachment;
import com.infinitehorizons.models.ReadonlyMessage;
import com.infinitehorizons.utils.IoUtils;
import lombok.Getter;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Send-only message for a {@link SharedWebHook}.
 * <p>
 * A {@link ReadonlyMessage} can be sent
 * by first converting it to a WebhookMessage with {@link #from(ReadonlyMessage)}.
 */
@Getter
public class WebhookMessage {
    /**
     * Maximum number of files a single message can hold (10).
     */
    public static final int MAX_FILES = 10;
    /**
     * Maximum number of embeds a single message can hold (10).
     */
    public static final int MAX_EMBEDS = 10;

    private final String username;
    private final String avatarUrl;
    private final String content;
    private final List<WebHookEmbed> embeds;
    private final boolean isTTS;
    private final MessageAttachment[] attachments;
    private final AllowedMentionsEvent allowedMentions;
    private final int flags;
    private final String threadName;

    public WebhookMessage(final String username, final String avatarUrl, final String content,
                          final List<WebHookEmbed> embeds, final boolean isTTS,
                          final MessageAttachment[] files, final AllowedMentionsEvent allowedMentions,
                          final int flags, final String threadName) {
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.content = content;
        this.embeds = embeds == null ? Collections.emptyList() : embeds;
        this.isTTS = isTTS;
        this.attachments = files;
        this.allowedMentions = allowedMentions;
        this.flags = flags;
        this.threadName = threadName;
    }

    /**
     * Returns a new WebhookMessage instance with the ephemeral flag turned on/off (true/false).
     * This instance remains unchanged, and a new instance is returned.
     *
     * @param ephemeral Whether to make this message ephemeral.
     * @return New WebhookMessage instance.
     */
    @NotNull
    public WebhookMessage asEphemeral(boolean ephemeral) {
        int flags = this.flags;
        if (ephemeral) {
            flags |= SharedConstants.EPHEMERAL;
        } else {
            flags &= ~SharedConstants.EPHEMERAL;
        }
        return new WebhookMessage(username, avatarUrl, content, embeds, isTTS, attachments, allowedMentions, flags, threadName);
    }

    /**
     * Converts a {@link ReadonlyMessage} to a WebhookMessage.
     * This doesn't convert attachments.
     *
     * @param message The message to convert.
     * @return A WebhookMessage copy.
     * @throws NullPointerException If provided with null.
     */
    @NotNull
    public static WebhookMessage from(@NotNull ReadonlyMessage message) {
        Objects.requireNonNull(message, "Message");
        WebHookMessageComponent builder = new WebHookMessageComponent();
        builder.setAvatarUrl(message.getAuthor().getAvatarId());
        builder.setUsername(message.getAuthor().getName());
        builder.setContent(message.getContent());
        builder.setTTS(message.isTts());
        builder.setEphemeral((message.getFlags() & SharedConstants.EPHEMERAL) != 0);
        builder.addEmbeds(message.getEmbeds());
        return builder.build();
    }

    /**
     * Creates a WebhookMessage from the provided embeds.
     * A message can hold up to {@value #MAX_EMBEDS} embeds.
     *
     * @param first  The first embed.
     * @param embeds Optional additional embeds for the message.
     * @return A WebhookMessage for the embeds.
     * @throws NullPointerException     If provided with null.
     * @throws IllegalArgumentException If more than {@value WebhookMessage#MAX_EMBEDS} are provided.
     */
    @NotNull
    public static WebhookMessage embeds(@NotNull WebHookEmbed first, @NotNull WebHookEmbed... embeds) {
        Objects.requireNonNull(first, "First embed");
        Objects.requireNonNull(embeds, "Embeds");
        if (embeds.length >= WebhookMessage.MAX_EMBEDS) {
            throw new IllegalArgumentException("Cannot add more than 10 embeds to a message");
        }
        List<WebHookEmbed> list = new ArrayList<>(1 + embeds.length);
        list.add(first);
        Collections.addAll(list, embeds);
        return new WebhookMessage(null, null, null, list, false, null, AllowedMentionsEvent.allMentions(), 0, null);
    }

    /**
     * Creates a WebhookMessage from the provided embeds.
     * A message can hold up to {@value #MAX_EMBEDS} embeds.
     *
     * @param embeds Embeds for the message.
     * @return A WebhookMessage for the embeds.
     * @throws NullPointerException     If provided with null.
     * @throws IllegalArgumentException If more than {@value WebhookMessage#MAX_EMBEDS} are provided.
     */
    @NotNull
    public static WebhookMessage embeds(@NotNull Collection<WebHookEmbed> embeds) {
        Objects.requireNonNull(embeds, "Embeds");
        if (embeds.size() > WebhookMessage.MAX_EMBEDS) {
            throw new IllegalArgumentException("Cannot add more than 10 embeds to a message");
        }
        if (embeds.isEmpty()) {
            throw new IllegalArgumentException("Cannot build an empty message");
        }
        embeds.forEach(Objects::requireNonNull);
        return new WebhookMessage(null, null, null, new ArrayList<>(embeds), false, null, AllowedMentionsEvent.allMentions(), 0, null);
    }

    /**
     * Creates a WebhookMessage from the provided attachments.
     * A message can hold up to {@value #MAX_FILES} attachments
     * and a total of 8MiB of data.
     *
     * @param attachments The attachments to add, keys are the alternative names for each attachment.
     * @return A WebhookMessage for the attachments.
     * @throws NullPointerException     If provided with null.
     * @throws IllegalArgumentException If no attachments are provided or more than {@value #MAX_FILES}.
     */
    @NotNull
    public static WebhookMessage files(@NotNull Map<String, ?> attachments) {
        Objects.requireNonNull(attachments, "Attachments");

        int fileAmount = attachments.size();
        if (fileAmount == 0) {
            throw new IllegalArgumentException("Cannot build an empty message");
        }
        if (fileAmount > WebhookMessage.MAX_FILES) {
            throw new IllegalArgumentException("Cannot add more than " + WebhookMessage.MAX_FILES + " files to a message");
        }
        MessageAttachment[] files = new MessageAttachment[fileAmount];
        int i = 0;
        for (Map.Entry<String, ?> attachment : attachments.entrySet()) {
            String name = attachment.getKey();
            Objects.requireNonNull(name, "Name");
            Object data = attachment.getValue();
            files[i++] = convertAttachment(name, data);
        }
        return new WebhookMessage(null, null, null, null, false, files, AllowedMentionsEvent.allMentions(), 0, null);
    }

    /**
     * Creates a WebhookMessage from the provided attachments.
     * A message can hold up to {@value #MAX_FILES} attachments
     * and a total of 8MiB of data.
     * <p>
     * The files are provided in pairs of {@literal Name->Data} similar to the first two arguments.
     * The allowed data types are {@code byte[] | InputStream | File}.
     *
     * @param name1       The alternative name of the first attachment.
     * @param data1       The first attachment, must be of type {@code byte[] | InputStream | File}.
     * @param attachments Optional additional attachments to add, pairs of {@literal String->Data}.
     * @return A WebhookMessage for the attachments.
     * @throws NullPointerException     If provided with null.
     * @throws IllegalArgumentException If no attachments are provided or more than {@value #MAX_FILES} or the additional arguments aren't an even count or an invalid format.
     */
    @NotNull
    public static WebhookMessage files(@NotNull String name1, @NotNull Object data1, @NotNull Object... attachments) {
        Objects.requireNonNull(name1, "Name");
        Objects.requireNonNull(data1, "Data");
        Objects.requireNonNull(attachments, "Attachments");
        if (attachments.length % 2 != 0) {
            throw new IllegalArgumentException("Must provide even number of varargs arguments");
        }
        int fileAmount = 1 + attachments.length / 2;
        if (fileAmount > WebhookMessage.MAX_FILES) {
            throw new IllegalArgumentException("Cannot add more than " + WebhookMessage.MAX_FILES + " files to a message");
        }
        MessageAttachment[] files = new MessageAttachment[fileAmount];
        files[0] = convertAttachment(name1, data1);
        for (int i = 0, j = 1; i < attachments.length; j++, i += 2) {
            Object name = attachments[i];
            Object data = attachments[i + 1];
            if (!(name instanceof String)) {
                throw new IllegalArgumentException("Provided arguments must be pairs for (String, Data). Expected String and found " + (name == null ? null : name.getClass().getName()));
            }
            files[j] = convertAttachment((String) name, data);
        }
        return new WebhookMessage(null, null, null, null, false, files, AllowedMentionsEvent.allMentions(), 0, null);
    }

    /**
     * Whether this message contains files.
     *
     * @return True, if this message contains files.
     */
    public boolean isFile() {
        return attachments != null;
    }

    /**
     * Provides a {@link okhttp3.RequestBody} of this message.
     * This is used internally for executing webhooks through HTTP requests.
     *
     * @return The request body.
     */
    @NotNull
    public RequestBody getBody() {
        final JSONObject payload = new JSONObject();
        payload.put("content", content);
        if (!embeds.isEmpty()) {
            final JSONArray array = new JSONArray();
            for (WebHookEmbed embed : embeds) {
                array.put(embed.reduced());
            }
            payload.put("embeds", array);
        } else {
            payload.put("embeds", new JSONArray());
        }
        if (avatarUrl != null) {
            payload.put("avatar_url", avatarUrl);
        }
        if (username != null) {
            payload.put("username", username);
        }
        payload.put("tts", isTTS);
        payload.put("allowed_mentions", allowedMentions);
        payload.put("flags", flags);
        if (threadName != null) {
            payload.put("thread_name", threadName);
        }
        String json = payload.toString();
        if (isFile()) {
            final MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

            for (int i = 0; i < attachments.length; i++) {
                final MessageAttachment attachment = attachments[i];
                if (attachment == null) {
                    break;
                }
                builder.addFormDataPart("file" + i, attachment.getName(), new IoUtils.OctetBody(attachment.getData()));
            }
            return builder.addFormDataPart("payload_json", json).build();
        }
        return RequestBody.create(IoUtils.JSON, json);
    }

    @NotNull
    private static MessageAttachment convertAttachment(@NotNull String name, @NotNull Object data) {
        Objects.requireNonNull(name, "Name");
        Objects.requireNonNull(data, "Data");
        try {
            if (data instanceof File) {
                return new MessageAttachment(name, (File) data);
            } else if (data instanceof InputStream) {
                return new MessageAttachment(name, (InputStream) data);
            } else if (data instanceof byte[]) {
                return new MessageAttachment(name, (byte[]) data);
            } else {
                throw new IllegalArgumentException("Provided arguments must be pairs for (String, Data). Unexpected data type " + data.getClass().getName());
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
