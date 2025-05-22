package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.runtime.NodeRuntime;

import java.io.File;

public class ListDownloadsCommand extends Command {

    public ListDownloadsCommand(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String name() {
        return "list_downloads";
    }

    @Override
    public void execute(String[] args) {
        String downloadDir = runtime.getNodeModel().getImagePath() + File.separator + "downloads";
        File folder = new File(downloadDir);
        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("No downloaded files found.");
            return;
        }

        System.out.println("Downloaded files:");
        for (File file : files) {
            System.out.println(" - " + file.getName());
        }
    }
}