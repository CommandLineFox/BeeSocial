package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.runtime.NodeRuntime;

import java.io.File;
import java.nio.file.Files;

import static raf.aleksabuncic.util.FileUtils.extractOriginalFileName;

public class RestoreFromBackupCommand extends Command {
    public RestoreFromBackupCommand(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String name() {
        return "restore_from_backup";
    }

    @Override
    public void execute(String[] args) {
        String backupDir = runtime.getNodeModel().getImagePath() + File.separator + "backup";
        String mainDir = runtime.getNodeModel().getImagePath();

        File folder = new File(backupDir);
        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("No backup files found.");
            return;
        }

        for (File file : files) {
            try {
                File dest = new File(mainDir, extractOriginalFileName(file.getName()));
                Files.copy(file.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Restored: " + file.getName());
            } catch (Exception e) {
                System.out.println("Failed to restore " + file.getName() + ": " + e.getMessage());
            }
        }
    }
}
