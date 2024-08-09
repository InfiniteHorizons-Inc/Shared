package com.infinitehorizons.models.send;

import com.infinitehorizons.components.WebHookEmbedComponent;
import com.infinitehorizons.models.ReadonlyEmbed;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.json.JSONString;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A reduced version of an {@link ReadonlyEmbed}
 * used for sending. A webhook can send up to {@value WebhookMessage#MAX_EMBEDS} embeds
 * in a single message.
 *
 * @see WebHookEmbedComponent
 */
@Getter
@Builder
@ToString
public class WebHookEmbed implements JSONString {

    /**
     * Maximum number of fields an embed can hold (25).
     */
    public static final int MAX_FIELDS = 25;

    private final OffsetDateTime timestamp;
    private final Integer color;
    private final String description;
    private final String thumbnailUrl;
    private final String imageUrl;
    private final EmbedFooter footer;
    private final EmbedTitle title;
    private final EmbedAuthor author;
    private final List<EmbedField> fields;

    public WebHookEmbed(
            @Nullable OffsetDateTime timestamp, @Nullable Integer color,
            @Nullable String description, @Nullable String thumbnailUrl, @Nullable String imageUrl,
            @Nullable EmbedFooter footer, @Nullable EmbedTitle title, @Nullable EmbedAuthor author,
            @NotNull List<EmbedField> fields) {
        this.timestamp = timestamp;
        this.color = color;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.imageUrl = imageUrl;
        this.footer = footer;
        this.title = title;
        this.author = author;
        this.fields = Collections.unmodifiableList(fields);
    }

    /**
     * Returns this embed instance, as it's already reduced.
     *
     * @return The current instance
     */
    @NotNull
    public WebHookEmbed reduced() {
        return this;
    }

    /**
     * Returns a JSON representation of this embed.
     *
     * @return A string containing the JSON representation.
     */
    @Override
    public String toJSONString() {
        JSONObject json = new JSONObject();
        if (description != null) json.put("description", description);
        if (timestamp != null) json.put("timestamp", timestamp);
        if (color != null) json.put("color", color & 0xFFFFFF);
        if (author != null) json.put("author", author);
        if (footer != null) json.put("footer", footer);
        if (thumbnailUrl != null) json.put("thumbnail", new JSONObject().put("url", thumbnailUrl));
        if (imageUrl != null) json.put("image", new JSONObject().put("url", imageUrl));
        if (!fields.isEmpty()) json.put("fields", fields);
        if (title != null) {
            if (title.text != null) json.put("url", title.url);
            json.put("title", title.text);
        }
        return json.toString();
    }

    /**
     * POJO for an embed field.
     * <br>An embed can have up to {@value MAX_FIELDS} fields.
     * A row of fields can be up 3 wide or 2 when a thumbnail is configured.
     * To be displayed in the same row as other fields, the field has to be set to {@link #inline () inline}.
     */
    @Getter
    @ToString
    public static class EmbedField implements JSONString {
        private final boolean inline;
        private final String name;
        private final String value;
        /**
         * Creates a new embed field
         *
         * @param inline Whether this should share a row with other fields
         * @param name   The name of the field
         * @param value  The value of the field
         */
        public EmbedField(boolean inline, @NotNull String name, @NotNull String value) {
            this.inline = inline;
            this.name = Objects.requireNonNull(name);
            this.value = Objects.requireNonNull(value);
        }

        /**
         * JSON representation of this field
         *
         * @return The JSON representation
         */
        @Override
        public String toJSONString() {
            return new JSONObject(this).toString();
        }
    }

    /**
     * POJO for an embed author.
     * <br>This can contain an icon (avatar), a name, and an url.
     * Often useful for posts from other platforms such as twitter/GitHub.
     */
    @Getter
    @ToString
    public static class EmbedAuthor implements JSONString {
        private final String name;
        private final String icon;
        private final String url;

        /**
         * Creates a new embed author
         *
         * @param name    The name of the author
         * @param icon The (nullable) icon url of the author
         * @param url     The (nullable) hyperlink of the author
         */
        public EmbedAuthor(@NotNull String name, @Nullable String icon, @Nullable Optional<String> url) {
            this.name = Objects.requireNonNull(name);
            this.icon = icon;
            assert Objects.requireNonNull(url).isPresent();
            this.url = url.orElse(null);
        }

        /**
         * JSON representation of this author
         *
         * @return The JSON representation
         */
        @Override
        public String toJSONString() {
            return new JSONObject(this).toString();
        }
    }

    /**
         * POJO for an embed footer.
         * <br>Useful to display meta-data about context such as
         * for a GitHub comment a repository name/icon.
         */
    @Getter
    @ToString
    public static class EmbedFooter implements JSONString {
        String text;
        String icon;
        /**
         * Creates a new embed footer
         *
         * @param text The visible text of the footer
         * @param icon The (nullable) icon url of the footer
         */
        public EmbedFooter(@NotNull String text, @Nullable String icon) {
            this.text = Objects.requireNonNull(text);
            this.icon = icon;
        }

        /**
         * JSON representation of this footer
         *
         * @return The JSON representation
         */
        @Override
        public String toJSONString() {
            return new JSONObject(this).toString();
        }
    }

    /**
     * POJO for an embed title.
     * <br>This is displayed above description and below the embed author.
     */
    @Getter
    public static class EmbedTitle {
        String text;
        String url;
        /**
         * Creates a new embed title
         *
         * @param text The visible text
         * @param url  The (nullable) hyperlink
         */
        public EmbedTitle(@NotNull String text, @Nullable String url) {
            this.text = Objects.requireNonNull(text);
            this.url = url;
        }
        /**
         * JSON representation of this title
         *
         * @return The JSON representation
         */
        @Override
        public String toString() {
            return new JSONObject(this).toString();
        }
    }
}
