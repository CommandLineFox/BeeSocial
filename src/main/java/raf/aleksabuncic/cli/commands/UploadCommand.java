package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.util.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

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
            System.out.println("Usage: upload <relativePathFromImagePath>");
            return;
        }

        String relativePath = args[0];

        File sourceFile = new File(runtime.getNodeModel().getWorkPath(), relativePath);

        if (!sourceFile.exists()) {
            System.out.println("File not found: " + sourceFile.getAbsolutePath());
            return;
        }

        if (!FileUtils.checkIfMediaFile(sourceFile.getName())) {
            System.out.println("Unsupported file type.");
            return;
        }

        File uploadsDir = new File(runtime.getNodeModel().getWorkPath(), "uploads");
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs();
        }

        File destFile = new File(uploadsDir, sourceFile.getName());
        try {
            Files.copy(sourceFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File uploaded to uploads folder.");
        } catch (Exception e) {
            System.out.println("Failed to upload file: " + e.getMessage());
            return;
        }

        if (runtime.hasBuddy()) {
            try {
                byte[] content = Files.readAllBytes(sourceFile.toPath());
                String encoded = Base64.getEncoder().encodeToString(content);
                String messageContent = sourceFile.getName() + "::" + encoded;

                Message backup = new Message("BACKUP", runtime.getNodeModel().getListenPort(), messageContent);
                Sender.sendMessage(runtime.getBuddyIp(), runtime.getBuddyPort(), backup);

                System.out.println("Backup sent to " + runtime.getBuddyIp() + ":" + runtime.getBuddyPort());
            } catch (Exception e) {
                System.out.println("Failed to send backup: " + e.getMessage());
            }
        }
    }
}