package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.NodeVisibility;

/**
 * Sets the visibility of the node.
 */
public class VisibilityCommand extends Command {
    public VisibilityCommand(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String name() {
        return "visibility";
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            System.out.println("Current visibility: " + runtime.getNodeModel().getVisibility());
        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "public" -> {
                    runtime.getNodeModel().setVisibility(NodeVisibility.PUBLIC);
                    System.out.println("Visibility set to PUBLIC.");
                }
                case "private" -> {
                    runtime.getNodeModel().setVisibility(NodeVisibility.PRIVATE);
                    System.out.println("Visibility set to PRIVATE.");
                }
                default -> System.out.println("Invalid value. Use: visibility [public|private]");
            }
        } else {
            System.out.println("Usage: visibility [public|private]");
        }
    }
}