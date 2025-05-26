package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.runtime.NodeRuntime;

/**
 * Stops the node by leaving the Chord ring and terminating the runtime.
 */
public class StopCommand extends Command {
    public StopCommand(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String name() {
        return "stop";
    }

    @Override
    public void execute(String[] args) {
        runtime.leaveChordRing();
    }
}
