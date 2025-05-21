package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.util.FileUtils;

public class UploadCommand extends Command {
    public UploadCommand(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String name() {
        return "upload";
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: upload <path>");
            return;
        }
        String path = args[0];
        if (!FileUtils.checkIfMediaFile(path)) {
            System.out.println("Unsupported file type.");
            return;
        }
        boolean success = FileUtils.uploadToWorkingRoot(path, runtime.getNodeModel().getImagePath());
        if (success) {
            System.out.println("File uploaded.");
        }
    }
}