package com.infinitehorizons.components;

import com.infinitehorizons.constants.SharedConstants;
import com.infinitehorizons.events.AllowedMentionsEvent;
import com.infinitehorizons.models.MessageAttachment;
import com.infinitehorizons.models.send.WebHookEmbed;
import com.infinitehorizons.models.send.WebhookMessage;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import discord4j.discordjson.json.AllowedMentionsData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.discordjson.json.MessageEditRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.MultipartRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.javacord.api.entity.DiscordEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Builder for constructing a {@link WebhookMessage} instance.
 * <p>
 * This builder allows setting various properties of a webhook message,
 * including content, embeds, files, and user appearance.
 */
@Getter
@Setter
public class WebHookMessageComponent {

    private static final int MAX_CONTENT_LENGTH = 2000;

    private final StringBuilder content = new StringBuilder();
    private final List<WebHookEmbed> embeds = new LinkedList<>();
    private final MessageAttachment[] files = new MessageAttachment[WebhookMessage.MAX_FILES];
    private AllowedMentionsEvent allowedMentions = AllowedMentionsEvent.allMentions();
    private String username, avatarUrl;
    private String threadName;
    private boolean isTTS;
    private int flags;
    private int fileIndex = 0;

    /**
     * Checks whether this builder is currently empty.
     *
     * @return True, if this builder is empty.
     */
    public boolean isEmpty() {
        return content.isEmpty() && embeds.isEmpty() && getFileAmount() == 0;
    }

    public int getFileAmount() {
        return fileIndex;
    }

    /**
     * Clears this builder to its default state.
     *
     * @return This builder for chaining convenience.
     */
    @NotNull
    public WebHookMessageComponent reset() {
        content.setLength(0);
        resetEmbeds();
        resetFiles();
        username = null;
        avatarUrl = null;
        isTTS = false;
        threadName = null;
        return this;
    }

    /**
     * Clears all files currently added to this builder.
     */
    public WebHookMessageComponent resetFiles() {
        for (int i = 0; i < WebhookMessage.MAX_FILES; i++) {
            files[i] = null;
        }
        fileIndex = 0;
        return this;
    }

    /**
     * Clears all embeds currently added to this builder.
     */
    public WebHookMessageComponent resetEmbeds() {
        this.embeds.clear();
        return this;
    }

    /**
     * Sets the mention allowlist.
     *
     * @param mentions The mention allowlist.
     * @return
     * @throws NullPointerException If provided with null.
     */
    public WebhookMessage setAllowedMentions(@NotNull AllowedMentionsEvent mentions) {
        this.allowedMentions = Objects.requireNonNull(mentions, "AllowedMentionsEvent");
        return null;
    }

    /**
     * Adds the provided embeds to the builder.
     *
     * @param embeds The embeds to add.
     * @throws NullPointerException  If provided with null.
     * @throws IllegalStateException If more than {@value WebhookMessage#MAX_EMBEDS} are added.
     */
    public WebHookMessageComponent addEmbeds(@NotNull WebHookEmbed... embeds) {
        Objects.requireNonNull(embeds, "Embeds");
        if (this.embeds.size() + embeds.length > WebhookMessage.MAX_EMBEDS)
            throw new IllegalStateException("Cannot add more than 10 embeds to a message");
        for (WebHookEmbed embed : embeds) {
            Objects.requireNonNull(embed, "Embed");
            this.embeds.add(embed);
        }
        return this;
    }

    /**
     * Adds the provided embeds to the builder.
     *
     * @param embeds The embeds to add.
     * @throws NullPointerException  If provided with null.
     * @throws IllegalStateException If more than {@value WebhookMessage#MAX_EMBEDS} are added.
     */
    public WebHookMessageComponent addEmbeds(@NotNull Collection<? extends WebHookEmbed> embeds) {
        Objects.requireNonNull(embeds, "Embeds");
        if (this.embeds.size() + embeds.size() > WebhookMessage.MAX_EMBEDS)
            throw new IllegalStateException("Cannot add more than 10 embeds to a message");
        for (WebHookEmbed embed : embeds) {
            Objects.requireNonNull(embed, "Embed");
            this.embeds.add(embed);
        }
        return this;
    }

    /**
     * Sets the content for this builder.
     *
     * @param content The (nullable) content to use.
     * @return This builder for chaining convenience.
     * @throws IllegalArgumentException If the content is larger than 2000 characters.
     */
    @NotNull
    public WebHookMessageComponent setContent(@Nullable String content) {
        if (content != null && content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Content may not exceed 2000 characters!");
        }
        this.content.setLength(0);
        if (content != null && !content.isEmpty()) {
            this.content.append(content);
        }
        return this;
    }

    /**
     * Appends the provided content to the already present content in this message.
     *
     * @param content The content to append.
     * @return This builder for chaining convenience.
     * @throws NullPointerException     If provided with null.
     * @throws IllegalArgumentException If the content exceeds 2000 characters.
     */
    @NotNull
    public WebHookMessageComponent append(@NotNull String content) {
        Objects.requireNonNull(content, "Content");
        if (this.content.length() + content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Content may not exceed 2000 characters!");
        }
        this.content.append(content);
        return this;
    }

    /**
     * Sets the username to use for this message.
     * Each message by a webhook can have a different user appearance.
     * If this is not set, it will default to the user appearance in the settings of the webhook.
     *
     * @param username The (nullable) username to use.
     */
    public WebHookMessageComponent  setUsername(@Nullable String username) {
        this.username = username == null || username.trim().isEmpty() ? null : username.trim();
        return this;
    }

    /**
     * Sets the avatar URL to use for this message.
     * Each message by a webhook can have a different user appearance.
     * If this is not set, it will default to the user appearance in the settings of the webhook.
     *
     * @param avatarUrl The (nullable) avatar URL to use.
     */
    public WebHookMessageComponent setAvatarUrl(@Nullable String avatarUrl) {
        this.avatarUrl = avatarUrl == null || avatarUrl.trim().isEmpty() ? null : avatarUrl.trim();
        return this;
    }

    private boolean isEmpty(@Nullable String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Sets whether this message should use Text-to-Speech (TTS).
     *
     * @param tts True, if this message should use TTS.
     */
    public WebHookMessageComponent setTTS(boolean tts) {
        isTTS = tts;
        return this;
    }

    /**
     * Sets whether the message should be ephemeral (only works for interaction webhooks).
     *
     * @param ephemeral True if the message should be ephemeral, false otherwise.
     */
    public WebHookMessageComponent setEphemeral(boolean ephemeral) {
        if (ephemeral)
            flags |= SharedConstants.EPHEMERAL;
        else
            flags &= ~SharedConstants.EPHEMERAL;
        return this;
    }

    /**
     * Adds the provided file as an attachment to this message.
     * A single message can have up to {@value WebhookMessage#MAX_FILES} attachments.
     *
     * @param file The file to attach.
     * @return This builder for chaining convenience.
     * @throws NullPointerException If provided with null.
     */
    @NotNull
    public WebHookMessageComponent addFile(@NotNull File file) {
        Objects.requireNonNull(file, "File");
        return addFile(file.getName(), file);
    }

    /**
     * Adds the provided file as an attachment to this message.
     * A single message can have up to {@value WebhookMessage#MAX_FILES} attachments.
     *
     * @param name The alternative name that should be used instead.
     * @param file The file to attach.
     * @return This builder for chaining convenience.
     * @throws NullPointerException If provided with null.
     */
    @NotNull
    public WebHookMessageComponent addFile(@NotNull String name, @NotNull File file) {
        Objects.requireNonNull(file, "File");
        Objects.requireNonNull(name, "Name");
        validateFile(file);
        if (fileIndex >= WebhookMessage.MAX_FILES) {
            throw new IllegalStateException("Cannot add more than " + WebhookMessage.MAX_FILES + " attachments to a message");
        }

        try {
            MessageAttachment attachment = new MessageAttachment(name, file);
            files[fileIndex++] = attachment;
            return this;
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Adds the provided data as a file attachment to this message.
     * A single message can have up to {@value WebhookMessage#MAX_FILES} attachments.
     *
     * @param name The alternative name that should be used.
     * @param data The data to attach as a file.
     * @return This builder for chaining convenience.
     * @throws NullPointerException If provided with null.
     */
    @NotNull
    public WebHookMessageComponent addFile(@NotNull String name, @NotNull byte[] data) {
        Objects.requireNonNull(data, "Data");
        Objects.requireNonNull(name, "Name");
        if (fileIndex >= WebhookMessage.MAX_FILES) {
            throw new IllegalStateException("Cannot add more than " + WebhookMessage.MAX_FILES + " attachments to a message");
        }

        MessageAttachment attachment = new MessageAttachment(name, data);
        files[fileIndex++] = attachment;
        return this;
    }

    /**
     * Adds the provided data as a file attachment to this message.
     * A single message can have up to {@value WebhookMessage#MAX_FILES} attachments.
     *
     * @param name The alternative name that should be used.
     * @param data The data to attach as a file.
     * @throws NullPointerException If provided with null.
     */
    public void addFile(@NotNull String name, @NotNull InputStream data) {
        Objects.requireNonNull(data, "InputStream");
        Objects.requireNonNull(name, "Name");
        if (fileIndex >= WebhookMessage.MAX_FILES) {
            throw new IllegalStateException("Cannot add more than " + WebhookMessage.MAX_FILES + " attachments to a message");
        }

        try {
            MessageAttachment attachment = new MessageAttachment(name, data);
            files[fileIndex++] = attachment;
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Sets the provided name as the name for a newly created thread.
     * This is only valid for forum/media channels.
     *
     * @param name The name that should be used.
     * @return This builder for chaining convenience.
     */
    @NotNull
    public WebHookMessageComponent setThreadName(@Nullable String name) {
        this.threadName = name;
        return this;
    }

    /**
     * Constructs the {@link WebhookMessage} from the current configurations.
     *
     * @return The resulting {@link WebhookMessage}.
     */
    @NotNull
    public WebhookMessage build() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot build an empty message!");
        }
        return new WebhookMessage(username, avatarUrl, content.toString(), embeds, isTTS,
                fileIndex == 0 ? null : Arrays.copyOf(files, fileIndex), allowedMentions, flags, threadName);
    }

    /////////////////////////////////
    /// Third-party compatibility ///
    /////////////////////////////////

    /**
     * Converts a JDA {@link Message} into a compatible WebhookMessageBuilder.
     *
     * @param message The message.
     * @return WebhookMessageBuilder with the converted data.
     * @throws NullPointerException If null is provided.
     */
    @NotNull
    public static WebHookMessageComponent fromJDA(@NotNull net.dv8tion.jda.api.entities.Message message) {
        return fromJDA(MessageCreateData.fromMessage(message));
    }

    /**
     * Converts a JDA {@link Message} into a compatible WebhookMessageBuilder.
     *
     * @param message The message.
     * @return WebhookMessageBuilder with the converted data.
     * @throws NullPointerException If null is provided.
     */
    @NotNull
    public static WebHookMessageComponent fromJDA(@NotNull MessageCreateData message) {
        WebHookMessageComponent builder = new WebHookMessageComponent();
        builder.setTTS(message.isTTS());
        builder.setContent(message.getContent());
        message.getEmbeds().forEach(embed -> builder.addEmbeds(WebHookEmbedComponent.fromJDA(embed).build()));

        EnumSet<Message.MentionType> allowedMentions = message.getAllowedMentions();
        Set<String> mentionedUsers = message.getMentionedUsers();
        Set<String> mentionedRoles = message.getMentionedRoles();
        builder.setAllowedMentions(
                AllowedMentionsEvent.noMentions()
                        .addRoleIds(mentionedUsers)
                        .addRoleIds(mentionedRoles)
                        .allowEveryone(allowedMentions.contains(Message.MentionType.EVERYONE))
                        .allowRoleMentions(allowedMentions.contains(Message.MentionType.ROLE))
                        .allowUserMentions(allowedMentions.contains(Message.MentionType.USER))
        );

        return builder;
    }

    /**
     * Converts a Javacord {@link org.javacord.api.entity.message.Message} into a compatible WebhookMessageBuilder.
     *
     * @param message The message.
     * @return WebhookMessageBuilder with the converted data.
     * @throws NullPointerException If null is provided.
     */
    @NotNull
    public static WebHookMessageComponent fromJavacord(@NotNull org.javacord.api.entity.message.Message message) {
        WebHookMessageComponent builder = new WebHookMessageComponent();
        builder.setTTS(message.isTts());
        builder.setContent(message.getContent());
        message.getEmbeds().forEach(embed -> builder.addEmbeds(WebHookEmbedComponent.fromJavacord(embed).build()));

        AllowedMentionsEvent allowedMentions = AllowedMentionsEvent.noMentions();
        allowedMentions.addUserIds(
                message.getMentionedUsers().stream()
                        .map(DiscordEntity::getIdAsString)
                        .collect(Collectors.toList()));
        allowedMentions.addRoleIds(
                message.getMentionedRoles().stream()
                        .map(DiscordEntity::getIdAsString)
                        .collect(Collectors.toList()));
        allowedMentions.allowEveryone(message.mentionsEveryone());
        builder.setAllowedMentions(allowedMentions);
        return builder;
    }

    /**
     * Converts a Discord4J {@link MessageCreateSpec} into a compatible WebhookMessageBuilder.
     *
     * @return WebhookMessageBuilder with the converted data.
     * @throws NullPointerException If null is provided.
     * @deprecated Replace with {@link #fromD4J(MessageCreateSpec)}.
     */
    @NotNull
    @Deprecated
    public static WebHookMessageComponent fromD4J() {
        throw new UnsupportedOperationException("Cannot build messages via consumers in Discord4J 3.2.0! Please change to fromD4J(spec)");
    }

    /**
     * Converts a Discord4J {@link MessageCreateSpec} into a compatible WebHookMessageComponent.
     *
     * @param spec The message create spec used to specify the desired message settings.
     * @return WebHookMessageComponent with the converted data.
     * @throws NullPointerException If null is provided.
     */
    @NotNull
    public static WebHookMessageComponent fromD4J(@NotNull MessageCreateSpec spec) {
        WebHookMessageComponent builder = new WebHookMessageComponent();
        MultipartRequest<MessageCreateRequest> data = spec.asRequest();
        data.getFiles().forEach(tuple -> builder.addFile(tuple.getT1(), tuple.getT2()));
        MessageCreateRequest jsonPayload = data.getJsonPayload();

        Possible<String> content = jsonPayload.content();
        Possible<List<EmbedData>> embeds = jsonPayload.embeds();
        Possible<Boolean> tts = jsonPayload.tts();
        Possible<AllowedMentionsData> allowedMentions = jsonPayload.allowedMentions();

        if (!content.isAbsent())
            builder.setContent(content.get());
        if (!tts.isAbsent())
            builder.setTTS(tts.get());
        if (!embeds.isAbsent()) {
            builder.addEmbeds(
                    embeds.get().stream()
                            .map(WebHookEmbedComponent::fromD4J)
                            .map(WebHookEmbedComponent::build)
                            .collect(Collectors.toList())
            );
        }

        if (!allowedMentions.isAbsent()) {
            AllowedMentionsData mentions = allowedMentions.get();
            AllowedMentionsEvent whitelist = AllowedMentionsEvent.noMentions();
            if (!mentions.users().isAbsent())
                whitelist.addUserIds(mentions.users().get());
            if (!mentions.roles().isAbsent())
                whitelist.addRoleIds(mentions.roles().get());
            if (!mentions.parse().isAbsent()) {
                List<String> parse = mentions.parse().get();
                whitelist.allowRoleMentions(parse.contains("roles"));
                whitelist.allowEveryone(parse.contains("everyone"));
                whitelist.allowUserMentions(parse.contains("users"));
            }
            builder.setAllowedMentions(whitelist);
        }

        return builder;
    }

    /**
     * Converts a Discord4J {@link MessageEditSpec} into a compatible WebHookMessageComponent.
     *
     * @param spec The message edit spec used to specify the desired message settings.
     * @return WebHookMessageComponent with the converted data.
     * @throws NullPointerException If null is provided.
     */
    @NotNull
    public static WebHookMessageComponent fromD4J(@NotNull MessageEditSpec spec) {
        WebHookMessageComponent builder = new WebHookMessageComponent();
        MultipartRequest<MessageEditRequest> data = spec.asRequest();
        data.getFiles().forEach(tuple -> builder.addFile(tuple.getT1(), tuple.getT2()));
        MessageEditRequest jsonPayload = data.getJsonPayload();

        Possible<Optional<String>> content = jsonPayload.content();
        Possible<Optional<List<EmbedData>>> embeds = jsonPayload.embeds();
        Possible<Optional<AllowedMentionsData>> allowedMentions = jsonPayload.allowedMentions();

        if (!content.isAbsent() && content.get().isPresent())
            builder.setContent(content.get().get());
        if (!embeds.isAbsent() && embeds.get().isPresent()) {
            builder.addEmbeds(
                    embeds.get().get().stream()
                            .map(WebHookEmbedComponent::fromD4J)
                            .map(WebHookEmbedComponent::build)
                            .collect(Collectors.toList())
            );
        }

        if (!allowedMentions.isAbsent() && allowedMentions.get().isPresent()) {
            AllowedMentionsData mentions = allowedMentions.get().get();
            AllowedMentionsEvent whitelist = AllowedMentionsEvent.noMentions();
            if (!mentions.users().isAbsent())
                whitelist.addUserIds(mentions.users().get());
            if (!mentions.roles().isAbsent())
                whitelist.addRoleIds(mentions.roles().get());
            if (!mentions.parse().isAbsent()) {
                List<String> parse = mentions.parse().get();
                whitelist.allowRoleMentions(parse.contains("roles"));
                whitelist.allowEveryone(parse.contains("everyone"));
                whitelist.allowUserMentions(parse.contains("users"));
            }
            builder.setAllowedMentions(whitelist);
        }

        return builder;
    }

    private void validateFile(@NotNull File file) {
        if (!file.exists() || !file.canRead()) {
            throw new IllegalArgumentException("File must exist and be readable");
        }
    }
}
