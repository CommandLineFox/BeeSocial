package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.runtime.NodeRuntime;

import java.io.File;

public class ListBackupsCommand extends Command {
    public ListBackupsCommand(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String name() {
        return "list_backups";
    }

    @Override
    public void execute(String[] args) {
        String backupDir = runtime.getNodeModel().getImagePath() + File.separator + "backup";
        File folder = new File(backupDir);
        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("No backup files found.");
            return;
        }

        System.out.println("Backup files:");
        for (File file : files) {
            System.out.println(" - " + file.getName());
        }
    }
}