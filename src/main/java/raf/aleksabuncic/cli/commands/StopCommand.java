package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.runtime.NodeRuntime;

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
        System.out.println("Shutting down node...");
        System.exit(0);
    }
}
