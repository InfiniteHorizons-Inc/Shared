package com.infinitehorizons.events;

import com.infinitehorizons.components.WebHookComponent;
import com.infinitehorizons.components.WebHookMessageComponent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Constructs an allowlist of allowed mentions for a message.
 * A {@link NullPointerException} will be thrown if any argument in this class is {@code null}.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * AllowedMentionsEvent mentions = new AllowedMentionsEvent()
 *     .addUserIds("86699011792191488", "107562988810027008")
 *     .allowEveryone(false)
 *     .allowRoleMentions(false);
 *
 * // This will only mention the user with the ID 86699011792191488 (Minn#6688)
 * // The @everyone mention will be ignored since it is disabled.
 * client.send(
 *   new WebHookMessageComponent()
 *     .setAllowedMentions(mentions)
 *     .setContent("Hello <@86699011792191488>! And hello @everyone else!")
 *     .build()
 * );
 * }</pre>
 *
 * @see WebHookMessageComponent#setAllowedMentions(AllowedMentionsEvent)
 * @see WebHookComponent#allowedMentions(AllowedMentionsEvent) WebHookComponent#setAllowedMentions(AllowedMentionsEvent)
 * @see #allMentions()
 * @see #noMentions()
 */
@Getter
@Setter
@Accessors(chain = true)
public class AllowedMentionsEvent implements JSONString {

    private boolean allowRoleMentions;
    private boolean allowUserMentions;
    private boolean allowEveryone;
    private final Set<String> userIds = new HashSet<>();
    private final Set<String> roleIds = new HashSet<>();

    /**
     * Allow all mention types: everyone, roles, and users.
     *
     * @return An instance where all mention types are allowed.
     */
    public static AllowedMentionsEvent allMentions() {
        return new AllowedMentionsEvent()
                .allowEveryone(true)
                .allowRoleMentions(true)
                .allowUserMentions(true);
    }

    /**
     * Disable all mention types.
     *
     * @return An instance where no mention types are allowed.
     */
    public static AllowedMentionsEvent noMentions() {
        return new AllowedMentionsEvent()
                .allowEveryone(false)
                .allowRoleMentions(false)
                .allowUserMentions(false);
    }

    /**
     * Add specific user IDs to the mention allowlist.
     * This will disable the parsing of all users.
     *
     * @param userIds The user IDs to allow mentions for.
     * @return This instance with updated user mentions.
     */
    @NotNull
    public AllowedMentionsEvent addUserIds(@NotNull String... userIds) {
        Collections.addAll(this.userIds, userIds);
        allowUserMentions = false;
        return this;
    }

    /**
     * Add specific role IDs to the mention allowlist.
     * This will disable the parsing of all roles.
     *
     * @param roleIds The role IDs to allow mentions for.
     * @return This instance with updated role mentions.
     */
    @NotNull
    public AllowedMentionsEvent addRoleIds(@NotNull String... roleIds) {
        Collections.addAll(this.roleIds, roleIds);
        allowRoleMentions = false;
        return this;
    }

    /**
     * Add specific user IDs to the mention allowlist.
     * This will disable the parsing of all users.
     *
     * @param userIds A collection of user IDs to allow mentions for.
     * @return This instance with updated user mentions.
     */
    @NotNull
    public AllowedMentionsEvent addUserIds(@NotNull Collection<String> userIds) {
        this.userIds.addAll(userIds);
        allowUserMentions = false;
        return this;
    }

    /**
     * Add specific role IDs to the mention allowlist.
     * This will disable the parsing of all roles.
     *
     * @param roleIds A collection of role IDs to allow mentions for.
     * @return This instance with updated role mentions.
     */
    @NotNull
    public AllowedMentionsEvent addRoleIds(@NotNull Collection<String> roleIds) {
        this.roleIds.addAll(roleIds);
        allowRoleMentions = false;
        return this;
    }

    /**
     * Whether to parse {@code @everyone} or {@code @here} mentions.
     *
     * @param allowEveryoneMention True if {@code @everyone} should be parsed.
     * @return This instance with the applied parsing rule.
     */
    @NotNull
    public AllowedMentionsEvent allowEveryone(boolean allowEveryoneMention) {
        allowEveryone = allowEveryoneMention;
        return this;
    }

    /**
     * Whether to parse user mentions.
     * Setting this to {@code true} will clear the allowlist provided by {@link #addUserIds(String...)}.
     *
     * @param allowUserMentions True if all user mentions should be parsed.
     * @return This instance with the applied parsing rule.
     */
    @NotNull
    public AllowedMentionsEvent allowUserMentions(boolean allowUserMentions) {
        this.allowUserMentions = allowUserMentions;
        if (allowUserMentions) {
            userIds.clear();
        }
        return this;
    }

    /**
     * Whether to parse role mentions.
     * Setting this to {@code true} will clear the allowlist provided by {@link #addRoleIds(String...)}.
     *
     * @param allowRoleMentions True if all role mentions should be parsed.
     * @return This instance with the applied parsing rule.
     */
    @NotNull
    public AllowedMentionsEvent allowRoleMentions(boolean allowRoleMentions) {
        this.allowRoleMentions = allowRoleMentions;
        if (allowRoleMentions) {
            roleIds.clear();
        }
        return this;
    }

    @Override
    public String toJSONString() {
        JSONObject json = new JSONObject();
        json.put("parse", new JSONArray());

        if (!userIds.isEmpty()) {
            json.put("users", userIds);
        } else if (allowUserMentions) {
            json.accumulate("parse", "users");
        }

        if (!roleIds.isEmpty()) {
            json.put("roles", roleIds);
        } else if (allowRoleMentions) {
            json.accumulate("parse", "roles");
        }

        if (allowEveryone) {
            json.accumulate("parse", "everyone");
        }
        return json.toString();
    }
}
