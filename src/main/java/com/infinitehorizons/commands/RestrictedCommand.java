package com.infinitehorizons.commands;

import com.infinitehorizons.utils.CooldownUtil;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.Permission;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a command with various restrictions on execution.
 * <p>
 * This abstract class defines the structure for commands that can have specific requirements
 * regarding guilds, permissions, users, roles, and cooldowns.
 */
@Getter
@Setter
public abstract class RestrictedCommand {

    private final Map<Long, CooldownUtil> COOLDOWN_CACHE = new HashMap<>();

    private Long[] requiredGuilds = new Long[]{};
    private Permission[] requiredPermissions = new Permission[]{};
    private Long[] requiredUsers = new Long[]{};
    private Long[] requiredRoles = new Long[]{};
    private Duration commandCooldown = Duration.ZERO;

    /**
     * Applies a manual cooldown for a specified user ID.
     * <p>
     * <b>Note:</b> Command cooldowns don't persist between sessions.
     *
     * @param userId  The ID of the user to apply the cooldown to.
     * @param nextUse The {@link Instant} indicating when the user can use the command again.
     */
    public void applyCooldown(long userId, Instant nextUse) {
        COOLDOWN_CACHE.put(userId, new CooldownUtil(Instant.now(), nextUse));
    }

    /**
     * Retrieves the cooldown information for a specified user.
     * <p>
     * If the user has not used the command before, returns a {@link CooldownUtil} with
     * {@code nextUse} and {@code lastUse} both set to {@link Instant#EPOCH}.
     *
     * @param userId The ID of the user to retrieve the cooldown for.
     * @return A {@link CooldownUtil} object containing the cooldown details.
     */
    public CooldownUtil retrieveCooldown(long userId) {
        CooldownUtil cooldown = COOLDOWN_CACHE.get(userId);
        if (cooldown == null) return new CooldownUtil(Instant.EPOCH, Instant.EPOCH);
        return cooldown;
    }

    /**
     * Determines if the specified user is currently under cooldown for this command.
     * <p>
     * <b>Note:</b> Command cooldowns don't persist between sessions.
     *
     * @param userId The ID of the user to check.
     * @return {@code true} if the command is on cooldown for the user, {@code false} otherwise.
     */
    public boolean hasCooldown(long userId) {
        return retrieveCooldown(userId).getNextExecution().isAfter(Instant.now());
    }

}
