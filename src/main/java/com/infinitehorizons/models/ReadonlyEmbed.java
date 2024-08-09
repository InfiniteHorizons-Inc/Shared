package com.infinitehorizons.models;

import com.infinitehorizons.models.send.WebHookEmbed;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.json.JSONString;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * An extension of {@link WebHookEmbed} with additional meta-data for receivable embeds.
 *
 * <p>This class represents an embed with details about its provider, thumbnail, image, and video content.</p>
 */
@Getter
public class ReadonlyEmbed extends WebHookEmbed {

    private final EmbedProvider provider;
    private final EmbedImage thumbnail;
    private final EmbedImage image;
    private final EmbedVideo video;

    public ReadonlyEmbed(
            @Nullable OffsetDateTime timestamp, @Nullable Integer color, @Nullable String description,
            @Nullable EmbedImage thumbnail, @Nullable EmbedImage image, @Nullable EmbedFooter footer,
            @Nullable EmbedTitle title, @Nullable EmbedAuthor author, @NotNull List<EmbedField> fields,
            @Nullable EmbedProvider provider, @Nullable EmbedVideo video) {
        super(timestamp, color, description,
                thumbnail == null ? null : thumbnail.getUrl(),
                image == null ? null : image.getUrl(),
                footer, title, author, fields);
        this.thumbnail = thumbnail;
        this.image = image;
        this.provider = provider;
        this.video = video;
    }

    /**
     * Reduces this embed to a simpler {@link WebHookEmbed} instance for sending.
     *
     * <p>This is done implicitly when sending a readonly embed.</p>
     *
     * @return The reduced embed instance.
     */
    @Override
    @NotNull
    public WebHookEmbed reduced() {
        return new WebHookEmbed(
                getTimestamp(), getColor(), getDescription(),
                thumbnail == null ? null : thumbnail.getUrl(),
                image == null ? null : image.getUrl(),
                getFooter(), getTitle(), getAuthor(), getFields());
    }

    /**
     * Returns the JSON string representation of this embed.
     *
     * <p>Note that received embeds may look different from sent ones.</p>
     *
     * @return The JSON representation.
     */
    @Override
    public String toJSONString() {
        JSONObject base = new JSONObject(super.toJSONString());
        base.put("provider", provider)
                .put("thumbnail", thumbnail)
                .put("video", video)
                .put("image", image);
        if (getTitle() != null) {
            // Serializa el título correctamente en el JSON
            base.put("title", getTitle().getText());  // Serializa solo el texto aquí
            base.put("url", getTitle().getUrl());
        }
        return base.toString();
    }

    @Override
    public String toString() {
        return toJSONString();
    }

    /**
     * Represents meta-data for an embed provider.
     */
    @Getter
    @RequiredArgsConstructor
    public static class EmbedProvider implements JSONString {
        private final String name;
        private final String url;

        /**
         * Returns the JSON string representation of this provider.
         *
         * @return The JSON representation.
         */
        @Override
        public String toJSONString() {
            return new JSONObject(this).toString();
        }

        @Override
        public String toString() {
            return toJSONString();
        }
    }

    /**
     * Represents meta-data about an embed video.
     */
    @Getter
    @RequiredArgsConstructor
    public static class EmbedVideo implements JSONString {
        @NotNull
        private final String url;
        private final int width;
        private final int height;

        /**
         * Returns the JSON string representation of this video.
         *
         * @return The JSON representation.
         */
        @Override
        public String toJSONString() {
            return new JSONObject(this).toString();
        }

        @Override
        public String toString() {
            return toJSONString();
        }
    }

    /**
     * Represents meta-data about an embed image component.
     */
    @Getter
    @RequiredArgsConstructor
    public static class EmbedImage implements JSONString {
        @NotNull
        private final String url;
        @NotNull
        private final String proxyUrl;
        private final int width;
        private final int height;

        /**
         * Returns the JSON string representation of this image.
         *
         * @return The JSON representation.
         */
        @Override
        public String toJSONString() {
            return new JSONObject(this).toString();
        }

        @Override
        public String toString() {
            return toJSONString();
        }
    }
}
