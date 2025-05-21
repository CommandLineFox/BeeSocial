package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.runtime.NodeRuntime;

import java.io.File;

public class RemoveFileCommand extends Command {
    public RemoveFileCommand(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String name() {
        return "remove_file";
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: remove_file <filename>");
            return;
        }

        File file = new File(runtime.getNodeModel().getImagePath(), args[0]);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("File removed.");
            } else {
                System.out.println("Failed to remove file.");
            }
        } else {
            System.out.println("File not found.");
        }
    }
}