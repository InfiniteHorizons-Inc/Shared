package com.infinitehorizons.models;

import com.infinitehorizons.models.send.WebHookEmbed;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Factory class used to convert JSON representations into Java objects for Discord webhooks.
 */
@UtilityClass
public class EntityFactory {

    /**
     * Converts a JSON object into a {@link ReadonlyUser}.
     *
     * @param json The JSON representation of the user.
     * @return A {@link ReadonlyUser} instance.
     */
    @NotNull
    public ReadonlyUser makeUser(@NotNull JSONObject json) {
        long id = Long.parseUnsignedLong(json.getString("id"));
        String name = json.getString("username");
        String avatar = json.optString("avatar", null);
        short discriminator = Short.parseShort(json.getString("discriminator"));
        boolean bot = json.optBoolean("bot", false);

        return new ReadonlyUser(id, discriminator, bot, name, avatar);
    }

    /**
     * Converts a JSON object into a {@link ReadonlyAttachment}.
     *
     * @param json The JSON representation of the attachment.
     * @return A {@link ReadonlyAttachment} instance.
     */
    @NotNull
    public ReadonlyAttachment makeAttachment(@NotNull JSONObject json) {
        String url = json.getString("url");
        String proxy = json.getString("proxy_url");
        String name = json.getString("filename");
        int size = json.getInt("size");
        int width = json.optInt("width", -1);
        int height = json.optInt("height", -1);
        long id = Long.parseUnsignedLong(json.getString("id"));
        return new ReadonlyAttachment(url, proxy, name, width, height, size, id);
    }

    /**
     * Converts a JSON object into a {@link WebHookEmbed.EmbedField}.
     *
     * @param json The JSON representation of the embed field.
     * @return A {@link WebHookEmbed.EmbedField} instance, or null if the input is null.
     */
    @Nullable
    public WebHookEmbed.EmbedField makeEmbedField(@Nullable JSONObject json) {
        if (json == null) {
            return null;
        }
        String name = json.getString("name");
        String value = json.getString("value");
        boolean inline = json.optBoolean("inline", false);
        return new WebHookEmbed.EmbedField(inline, name, value);
    }

    /**
     * Converts a JSON object into a {@link WebHookEmbed.EmbedAuthor}.
     *
     * @param json The JSON representation of the embed author.
     * @return A {@link WebHookEmbed.EmbedAuthor} instance, or null if the input is null.
     */
    @Nullable
    public WebHookEmbed.EmbedAuthor makeEmbedAuthor(@Nullable JSONObject json) {
        if (json == null) {
            return null;
        }
        String name = json.getString("name");
        String url = json.optString("url", null);
        String icon = json.optString("icon", null);
        return new WebHookEmbed.EmbedAuthor(name, icon, Optional.ofNullable(url));
    }

    /**
     * Converts a JSON object into a {@link WebHookEmbed.EmbedFooter}.
     *
     * @param json The JSON representation of the embed footer.
     * @return A {@link WebHookEmbed.EmbedFooter} instance, or null if the input is null.
     */
    @Nullable
    public WebHookEmbed.EmbedFooter makeEmbedFooter(@Nullable JSONObject json) {
        if (json == null) {
            return null;
        }
        String text = json.getString("text");
        String icon = json.optString("icon", null);
        return new WebHookEmbed.EmbedFooter(text, icon);
    }

    /**
     * Converts a JSON object into a {@link WebHookEmbed.EmbedTitle}.
     *
     * @param json The JSON representation of the embed title.
     * @return A {@link WebHookEmbed.EmbedTitle} instance, or null if the input is null.
     */
    @Nullable
    public static WebHookEmbed.EmbedTitle makeEmbedTitle(@NotNull JSONObject json) {
        final String text = json.optString("title", null);
        if (text == null)
            return null;
        final String url = json.optString("url", null);
        return new WebHookEmbed.EmbedTitle(text, url);
    }

    /**
     * Converts a JSON object into a {@link ReadonlyEmbed.EmbedImage}.
     *
     * @param json The JSON representation of the embed image.
     * @return A {@link ReadonlyEmbed.EmbedImage} instance, or null if the input is null.
     */
    @Nullable
    public ReadonlyEmbed.EmbedImage makeEmbedImage(@Nullable JSONObject json) {
        if (json == null) {
            return null;
        }
        String url = json.getString("url");
        String proxyUrl = json.getString("proxyUrl");
        int width = json.getInt("width");
        int height = json.getInt("height");
        return new ReadonlyEmbed.EmbedImage(url, proxyUrl, width, height);
    }

    /**
     * Converts a JSON object into a {@link ReadonlyEmbed.EmbedProvider}.
     *
     * @param json The JSON representation of the embed provider.
     * @return A {@link ReadonlyEmbed.EmbedProvider} instance, or null if the input is null.
     */
    @Nullable
    public ReadonlyEmbed.EmbedProvider makeEmbedProvider(@Nullable JSONObject json) {
        if (json == null) {
            return null;
        }
        String url = json.optString("url", null);
        String name = json.optString("name", null);
        return new ReadonlyEmbed.EmbedProvider(name, url);
    }

    /**
     * Converts a JSON object into a {@link ReadonlyEmbed.EmbedVideo}.
     *
     * @param json The JSON representation of the embed video.
     * @return A {@link ReadonlyEmbed.EmbedVideo} instance, or null if the input is null.
     */
    @Nullable
    public ReadonlyEmbed.EmbedVideo makeEmbedVideo(@Nullable JSONObject json) {
        if (json == null) {
            return null;
        }
        String url = json.getString("url");
        int height = json.getInt("height");
        int width = json.getInt("width");
        return new ReadonlyEmbed.EmbedVideo(url, width, height);
    }

    /**
     * Converts a JSON object into a {@link ReadonlyEmbed}.
     *
     * @param json The JSON representation of the embed.
     * @return A {@link ReadonlyEmbed} instance.
     */
    @NotNull
    public ReadonlyEmbed makeEmbed(@NotNull JSONObject json) {
        String description = json.optString("description", null);
        Integer color = json.isNull("color") ? null : json.getInt("color");
        ReadonlyEmbed.EmbedImage image = makeEmbedImage(json.optJSONObject("image"));
        ReadonlyEmbed.EmbedImage thumbnail = makeEmbedImage(json.optJSONObject("thumbnail"));
        ReadonlyEmbed.EmbedProvider provider = makeEmbedProvider(json.optJSONObject("provider"));
        ReadonlyEmbed.EmbedVideo video = makeEmbedVideo(json.optJSONObject("video"));
        WebHookEmbed.EmbedFooter footer = makeEmbedFooter(json.optJSONObject("footer"));
        WebHookEmbed.EmbedAuthor author = makeEmbedAuthor(json.optJSONObject("author"));
        WebHookEmbed.EmbedTitle title = makeEmbedTitle(json);
        OffsetDateTime timestamp = json.isNull("timestamp") ? null : OffsetDateTime.parse(json.getString("timestamp"));

        JSONArray fieldArray = json.optJSONArray("fields");
        List<WebHookEmbed.EmbedField> fields = new ArrayList<>();
        if (fieldArray != null) {
            for (int i = 0; i < fieldArray.length(); i++) {
                JSONObject obj = fieldArray.getJSONObject(i);
                WebHookEmbed.EmbedField field = makeEmbedField(obj);
                if (field != null) {
                    fields.add(field);
                }
            }
        }
        return new ReadonlyEmbed(timestamp, color, description, thumbnail, image, footer, title, author, fields, provider, video);
    }

    /**
     * Converts a JSON object into a {@link ReadonlyMessage}.
     *
     * @param json The JSON representation of the message.
     * @return A {@link ReadonlyMessage} instance.
     */
    @NotNull
    public ReadonlyMessage makeMessage(@NotNull JSONObject json) {
        long id = Long.parseUnsignedLong(json.getString("id"));
        long channelId = Long.parseUnsignedLong(json.getString("channel_id"));
        ReadonlyUser author = makeUser(json.getJSONObject("author"));
        String content = json.getString("content");
        boolean tts = json.getBoolean("tts");
        boolean mentionEveryone = json.getBoolean("mention_everyone");
        int flags = json.optInt("flags", 0);

        JSONArray usersArray = json.getJSONArray("mentions");
        JSONArray rolesArray = json.getJSONArray("mention_roles");
        JSONArray embedArray = json.getJSONArray("embeds");
        JSONArray attachmentArray = json.getJSONArray("attachments");

        List<ReadonlyUser> mentionedUsers = convertToList(usersArray, EntityFactory::makeUser);
        List<ReadonlyEmbed> embeds = convertToList(embedArray, EntityFactory::makeEmbed);
        List<ReadonlyAttachment> attachments = convertToList(attachmentArray, EntityFactory::makeAttachment);
        List<Long> mentionedRoles = new ArrayList<>();

        for (int i = 0; i < rolesArray.length(); i++) {
            mentionedRoles.add(Long.parseUnsignedLong(rolesArray.getString(i)));
        }

        return new ReadonlyMessage(id, channelId, mentionEveryone, tts, flags, author, content, embeds, attachments, mentionedUsers, mentionedRoles);
    }

    private static <T> List<T> convertToList(JSONArray arr, Function<JSONObject, T> converter) {
        if (arr == null) {
            return Collections.emptyList();
        }
        List<T> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject json = arr.getJSONObject(i);
            T out = converter.apply(json);
            if (out != null) {
                list.add(out);
            }
        }
        return Collections.unmodifiableList(list);
    }
}
