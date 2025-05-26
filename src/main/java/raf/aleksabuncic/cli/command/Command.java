package raf.aleksabuncic.cli.command;

import raf.aleksabuncic.core.runtime.NodeRuntime;

/**
 * Represents a command that can be executed by the CLI.
 */
public abstract class Command {
    protected final NodeRuntime runtime;

    public Command(NodeRuntime runtime) {
        this.runtime = runtime;
    }

    /**
     * Returns the name of the command.
     *
     * @return Name of the command.
     */
    public abstract String name();

    /**
     * Executes the specific command using the provided arguments.
     *
     * @param args an array of strings representing the arguments for the command;
     *             the specific format and usage of arguments depend on the implementation.
     */
    public abstract void execute(String[] args);
}
