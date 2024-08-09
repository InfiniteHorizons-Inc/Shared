package com.infinitehorizons.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.json.JSONPropertyName;
import org.json.JSONString;

import java.util.Locale;

/**
 * Represents a read-only Discord user.
 *
 * <p>This class provides a snapshot of a user with details about their ID, discriminator, and other profile information.</p>
 */
@Getter
@RequiredArgsConstructor
public class ReadonlyUser implements JSONString {

    private final long id;
    private final short discriminator;
    private final boolean bot;

    @NotNull
    private final String name;

    @Nullable
    private final String avatar;

    /**
     * Returns the 4-digit discriminator of this user.
     *
     * <p>This is shown in the client after the {@code #} when viewing profiles.</p>
     *
     * @return The formatted discriminator.
     */
    public String getDiscriminator() {
        return String.format(Locale.ROOT, "%04d", discriminator);
    }

    /**
     * Returns the username of this user.
     *
     * <p>This is the user's global username, not a guild-specific nickname.</p>
     *
     * @return The username.
     */
    @NotNull
    @JSONPropertyName("username")
    public String getName() {
        return name;
    }

    /**
     * Returns the avatar ID of this user, or {@code null} if no avatar is set.
     *
     * @return The avatar ID or {@code null}.
     */
    @Nullable
    @JSONPropertyName("avatar_id")
    public String getAvatarId() {
        return avatar;
    }

    /**
     * Returns the JSON string representation of this user.
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
