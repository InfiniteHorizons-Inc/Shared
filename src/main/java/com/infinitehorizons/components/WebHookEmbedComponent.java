package com.infinitehorizons.components;

import com.infinitehorizons.models.send.WebHookEmbed;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedAuthorData;
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.discordjson.json.EmbedFooterData;
import discord4j.discordjson.json.EmbedImageData;
import discord4j.discordjson.json.EmbedThumbnailData;
import discord4j.discordjson.possible.Possible;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import org.javacord.api.entity.message.embed.EmbedImage;
import org.javacord.api.entity.message.embed.EmbedThumbnail;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Builder for creating a {@link WebHookEmbed} instance.
 * <p>
 * This builder allows for the customization and creation of Discord webhook embeds with various elements such as title, description, author, footer, etc.
 */
@Getter
public class WebHookEmbedComponent {
    private final List<WebHookEmbed.EmbedField> fields;

    private OffsetDateTime timestamp;
    private Integer color;
    private String description;
    private String thumbnailUrl;
    private String imageUrl;
    private WebHookEmbed.EmbedFooter footer;
    private WebHookEmbed.EmbedTitle title;
    private WebHookEmbed.EmbedAuthor author;

    /**
     * Creates a builder with predefined settings from the provided {@link WebHookEmbed} instance.
     *
     * @param embed The embed to copy settings from.
     */
    public WebHookEmbedComponent(@Nullable WebHookEmbed embed) {
        this();
        if (embed != null) {
            timestamp = embed.getTimestamp();
            color = embed.getColor();
            description = embed.getDescription();
            thumbnailUrl = embed.getThumbnailUrl();
            imageUrl = embed.getImageUrl();
            footer = embed.getFooter();
            title = embed.getTitle();
            author = embed.getAuthor();
            fields.addAll(embed.getFields());
        }
    }

    public WebHookEmbedComponent() {
        fields = new ArrayList<>(10);
    }

    /**
     * Resets the builder to its default state.
     */
    public void reset() {
        fields.clear();
        timestamp = null;
        color = null;
        description = null;
        thumbnailUrl = null;
        imageUrl = null;
        footer = null;
        title = null;
        author = null;
    }

    /**
     * Sets the timestamp for the resulting embed.
     *
     * @param timestamp The timestamp.
     * @throws java.time.DateTimeException If unable to convert to an {@link OffsetDateTime}.
     */
    public WebHookEmbedComponent setTimestamp(@Nullable TemporalAccessor timestamp) {
        if (timestamp instanceof Instant) {
            this.timestamp = OffsetDateTime.ofInstant((Instant) timestamp, ZoneId.of("UTC"));
        }
        else {
            this.timestamp = timestamp == null ? null : OffsetDateTime.from(timestamp);
        }
        return this;
    }

    /**
     * Sets the RGB color to use it for the line left of the resulting embed.
     *
     * @param color The color to use.
     */
    public WebHookEmbedComponent setColor(@Nullable Integer color) {
        this.color = color;
        return this;
    }

    /**
     * Sets the description of the embed.
     *
     * @param description The description to use.
     */
    public WebHookEmbedComponent setDescription(@Nullable String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the thumbnail URL for this embed.
     *
     * @param thumbnailUrl The thumbnail URL.
     */
    public WebHookEmbedComponent  setThumbnailUrl(@Nullable String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        return this;
    }

    /**
     * Sets the image URL for this embed.
     *
     * @param imageUrl The image URL.
     */
    public WebHookEmbedComponent setImageUrl(@Nullable String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    /**
     * Sets the footer for this embed.
     *
     * @param footer The {@link WebHookEmbed.EmbedFooter}.
     */
    public WebHookEmbedComponent setFooter(@Nullable WebHookEmbed.EmbedFooter footer) {
        this.footer = footer;
        return this;
    }

    /**
     * Sets the title for this embed.
     *
     * @param title The {@link WebHookEmbed.EmbedTitle}.
     */
    public WebHookEmbedComponent setTitle(@Nullable WebHookEmbed.EmbedTitle title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the author for this embed.
     *
     * @param author The {@link WebHookEmbed.EmbedAuthor}.
     */
    public WebHookEmbedComponent setAuthor(@Nullable WebHookEmbed.EmbedAuthor author) {
        this.author = author;
        return this;
    }

    /**
     * Adds an {@link WebHookEmbed.EmbedField} to this embed.
     *
     * @param field The {@link WebHookEmbed.EmbedField} to add.
     * @throws IllegalStateException If the maximum number of fields has already been reached.
     */
    public WebHookEmbedComponent addField(@NotNull WebHookEmbed.EmbedField field) {
        if (fields.size() == WebHookEmbed.MAX_FIELDS) {
            throw new IllegalStateException("Cannot add more than 25 fields");
        }
        fields.add(Objects.requireNonNull(field));
        return this;
    }

    /**
     * Checks whether this embed is currently empty.
     *
     * @return True if this embed is empty, otherwise false.
     */
    public boolean isEmpty() {
        return isEmpty(description)
                && isEmpty(imageUrl)
                && isEmpty(thumbnailUrl)
                && isFieldsEmpty()
                && isAuthorEmpty()
                && isTitleEmpty()
                && isFooterEmpty()
                && timestamp == null;
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isTitleEmpty() {
        return title == null || isEmpty(title.getText());
    }

    private boolean isFooterEmpty() {
        return footer == null || isEmpty(footer.getText());
    }

    private boolean isAuthorEmpty() {
        return author == null || isEmpty(author.getName());
    }

    private boolean isFieldsEmpty() {
        if (fields.isEmpty()) {
            return true;
        }
        return fields.stream().allMatch(f -> isEmpty(f.getName()) && isEmpty(f.getValue()));
    }

    /**
     * Builds a new {@link WebHookEmbed} instance from the current settings.
     *
     * @return The {@link WebHookEmbed}.
     * @throws IllegalStateException If this embed is currently empty.
     */
    @NotNull
    public WebHookEmbed build() {
        if (isEmpty())
            throw new IllegalStateException("Cannot build an empty embed");
        return new WebHookEmbed(
                timestamp, color,
                description, thumbnailUrl, imageUrl,
                footer, title, author,
                new ArrayList<>(fields)
        );
    }

    /////////////////////////////////
    /// Third-party compatibility ///
    /////////////////////////////////

    /**
     * Converts a JDA {@link MessageEmbed} into a compatible WebhookEmbedBuilder.
     *
     * @param embed The embed to convert.
     * @return WebhookEmbedBuilder with the converted data.
     * @throws NullPointerException If null is provided.
     */
    @NotNull
    @SuppressWarnings("ConstantConditions")
    public static WebHookEmbedComponent fromJDA(@NotNull MessageEmbed embed) {
        WebHookEmbedComponent builder = new WebHookEmbedComponent();
        String url = embed.getUrl();
        String title = embed.getTitle();
        String description = embed.getDescription();
        MessageEmbed.Thumbnail thumbnail = embed.getThumbnail();
        MessageEmbed.AuthorInfo author = embed.getAuthor();
        MessageEmbed.Footer footer = embed.getFooter();
        MessageEmbed.ImageInfo image = embed.getImage();
        List<MessageEmbed.Field> fields = embed.getFields();
        int color = embed.getColorRaw();
        OffsetDateTime timestamp = embed.getTimestamp();

        if (title != null) {
            builder.setTitle(new WebHookEmbed.EmbedTitle(title, url));
        }
        if (description != null) {
            builder.setDescription(description);
        }
        if (thumbnail != null) {
            builder.setThumbnailUrl(thumbnail.getUrl());
        }
        if (author != null) {
            builder.setAuthor(new WebHookEmbed.EmbedAuthor(author.getName(), author.getIconUrl(), Optional.ofNullable(author.getUrl())));
        }
        if (footer != null) {
            builder.setFooter(new WebHookEmbed.EmbedFooter(footer.getText(), footer.getIconUrl()));
        }
        if (image != null) {
            builder.setImageUrl(image.getUrl());
        }
        if (!fields.isEmpty()) {
            fields.forEach(field -> builder.addField(new WebHookEmbed.EmbedField(field.isInline(), field.getName(), field.getValue())));
        }
        if (color != Role.DEFAULT_COLOR_RAW) {
            builder.setColor(color);
        }
        if (timestamp != null) {
            builder.setTimestamp(timestamp);
        }

        return builder;
    }

    /**
     * Converts a Javacord {@link org.javacord.api.entity.message.embed.Embed} into a compatible WebhookEmbedBuilder.
     *
     * @param embed The embed to convert.
     * @return WebHookEmbedComponent with the converted data.
     * @throws NullPointerException If null is provided.
     */
    @NotNull
    public static WebHookEmbedComponent fromJavacord(@NotNull org.javacord.api.entity.message.embed.Embed embed) {
        WebHookEmbedComponent builder = new WebHookEmbedComponent();

        embed.getTitle().ifPresent(title ->
                builder.setTitle(new WebHookEmbed.EmbedTitle(title, embed.getUrl().map(URL::toString).orElse(null))));
        embed.getDescription().ifPresent(builder::setDescription);
        embed.getTimestamp().ifPresent(builder::setTimestamp);
        embed.getColor().map(java.awt.Color::getRGB).ifPresent(builder::setColor);
        embed.getFooter().map(footer -> new WebHookEmbed.EmbedFooter(footer.getText().orElseThrow(NullPointerException::new), footer.getIconUrl().map(URL::toString).orElse(null))).ifPresent(builder::setFooter);
        embed.getImage().map(EmbedImage::getUrl).map(URL::toString).ifPresent(builder::setImageUrl);
        embed.getThumbnail().map(EmbedThumbnail::getUrl).map(URL::toString).ifPresent(builder::setThumbnailUrl);
        embed.getFields().stream()
                .map(field -> new WebHookEmbed.EmbedField(field.isInline(), field.getName(), field.getValue()))
                .forEach(builder::addField);
        return builder;
    }

    /**
     * Converts a Discord4J {@link EmbedCreateSpec} into a compatible WebhookEmbedBuilder.
     *
     * @param spec The embed creates a spec which applies the desired settings.
     * @return WebhookEmbedBuilder with the converted data.
     * @throws NullPointerException If null is provided.
     */
    @NotNull
    public static WebHookEmbedComponent fromD4J(@NotNull EmbedCreateSpec spec) {
        return fromD4J(spec.asRequest());
    }

    /**
     * Converts a Discord4J {@link EmbedData} into a compatible WebhookEmbedBuilder.
     *
     * @param data The embed data.
     * @return WebHookEmbedComponent with the converted data.
     * @throws NullPointerException If null is provided.
     */
    @NotNull
    public static WebHookEmbedComponent fromD4J(@NotNull EmbedData data) {
        WebHookEmbedComponent builder = new WebHookEmbedComponent();

        // there aren't any docs for this, so I'm completely going off of assumptions here
        Possible<String> title = data.title();
        Possible<String> description = data.description();
        Possible<String> url = data.url();
        Possible<String> timestamp = data.timestamp();
        Possible<Integer> color = data.color();
        Possible<EmbedFooterData> footer = data.footer();
        Possible<EmbedImageData> image = data.image();
        Possible<EmbedThumbnailData> thumbnail = data.thumbnail();
        Possible<EmbedAuthorData> author = data.author();
        Possible<List<EmbedFieldData>> fields = data.fields();

        if (!title.isAbsent())
            builder.setTitle(new WebHookEmbed.EmbedTitle(title.get(), url.toOptional().orElse(null)));
        if (!description.isAbsent())
            builder.setDescription(description.get());
        if (!timestamp.isAbsent())
            builder.setTimestamp(OffsetDateTime.parse(timestamp.get()));
        if (!color.isAbsent())
            builder.setColor(color.get());
        if (!footer.isAbsent())
            builder.setFooter(new WebHookEmbed.EmbedFooter(footer.get().text(), footer.get().iconUrl().toOptional().orElse(null)));
        if (!image.isAbsent())
            builder.setImageUrl(image.get().url().get());
        if (!thumbnail.isAbsent())
            builder.setThumbnailUrl(thumbnail.get().url().get());
        if (!author.isAbsent()) {
            EmbedAuthorData authorData = author.get();
            builder.setAuthor(new WebHookEmbed.EmbedAuthor(
                    authorData.name().get(),
                    authorData.iconUrl().toOptional().orElse(null),
                    authorData.url().toOptional().orElse(null)));
        }
        if (!fields.isAbsent()) {
            fields.get()
                    .stream()
                    .map(field -> new WebHookEmbed.EmbedField(field.inline().toOptional().orElse(false), field.name(), field.value()))
                    .forEach(builder::addField);
        }

        return builder;
    }
}
