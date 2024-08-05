package com.infinitehorizons.commands;

/**
 * Interface representing a command that can be executed with a specified event.
 * <p>
 * Implementations of this interface define the logic to be executed when the command is triggered.
 *
 * @param <E> The type of event that triggers the execution of the command.
 */
public interface ExecutableCommand<E> {

    /**
     * Executes the command with the given event.
     *
     * @param event The event that triggers the command execution.
     *              This event provides context and data needed to perform the command's action.
     */
    void execute(E event);

}
