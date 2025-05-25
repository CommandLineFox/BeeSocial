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

        File file = new File(runtime.getNodeModel().getWorkPath(), "uploads" + File.separator + args[0]);
        if (!file.exists()) {
            System.out.println("File not found: " + args[0]);
            return;
        }

        if (file.delete()) {
            System.out.println("File deleted: " + args[0]);
        } else {
            System.out.println("Failed to delete file: " + args[0]);
        }
    }
}