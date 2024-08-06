package com.infinitehorizons.events;

import com.infinitehorizons.Shared;
import com.infinitehorizons.utils.Logger;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * A generic event that holds the event's name, the {@link Shared} instance, and the follow-up {@link Interaction}.
 *
 * @param <I> The type of follow-up interaction.
 */
@Getter
public abstract class Event<I extends Interaction> {

    private final String eventName;

    private final Shared shared;

    private final I interaction;

    /**
     * Constructs a new DIH4JDAEvent.
     *
     * @param eventName   The name of the event.
     * @param shared     The {@link Shared} instance associated with this event.
     * @param interaction The follow-up interaction.
     */
    protected Event(@NotNull String eventName, @NotNull Shared shared, @NotNull I interaction) {
        this.eventName = eventName;
        this.shared = shared;
        this.interaction = interaction;
    }

    /**
     * Fires an event using the {@link EventListener}.
     *
     * @param event The {@link Event} to fire.
     * @param <I>   The type of follow-up interaction.
     * @since v1.5
     */
    public static <I extends Interaction> void fire(@NotNull Event<I> event) {
        if (event.getShared().getEventListeners().isEmpty()) {
            Logger.warn(Logger.Type.EVENT_MISSING_HANDLER,
                    "%s was fired, but not handled (No listener registered)", event.getEventName());
            if (event instanceof ThrowableEvent && event.getShared().getConfig().isDefaultPrintStacktrace()) {
                ((ThrowableEvent<I>) event).getThrowable().printStackTrace();
            }
        }
        for (EventListener listener : event.getShared().getEventListeners()) {
            invokeEventHandler(listener, event);
        }
    }

    /**
     * Invokes the event handler method on the listener for the given event.
     *
     * @param listener The event listener.
     * @param event    The event to handle.
     * @param <I>      The type of follow-up interaction.
     */
    private static <I extends Interaction> void invokeEventHandler(EventListener listener, Event<I> event) {
        try {
            for (Method method : listener.getClass().getMethods()) {
                if (method.getName().equals(event.getEventName())) {
                    method.invoke(listener, event);
                }
            }
        } catch (ReflectiveOperationException e) {
            Logger.error("Error invoking event handler: %s", e.getMessage());
        }
    }
}
