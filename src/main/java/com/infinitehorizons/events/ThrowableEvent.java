package com.infinitehorizons.events;

import com.infinitehorizons.Shared;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.NotNull;

/**
 * An abstract class representing an event in {@code Shared} that involves a {@link Throwable}.
 * It extends the basic {@link Event} functionality by including error handling.
 *
 * @param <I> The type of interaction associated with this event.
 */
@Getter
public abstract class ThrowableEvent<I extends Interaction> extends Event<I> {

    private final Throwable throwable;

    /**
     * Constructs a new DIH4JDAThrowableEvent.
     *
     * @param eventName   The name of the event.
     * @param shared     The {@link Shared} instance associated with this event.
     * @param interaction The interaction that triggered this event.
     * @param throwable   The {@link Throwable} associated with this event.
     */
    protected ThrowableEvent(@NotNull String eventName, @NotNull Shared shared, @NotNull I interaction,
                                    @NotNull Throwable throwable) {
        super(eventName, shared, interaction);
        this.throwable = throwable;
    }
}
