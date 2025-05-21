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
            System.out.println("Usage: upload <path>");
            return;
        }

        String path = args[0];
        if (!FileUtils.checkIfMediaFile(path)) {
            System.out.println("Unsupported file type.");
            return;
        }

        // 1. Sačuvaj lokalno
        boolean success = FileUtils.uploadToWorkingRoot(path, runtime.getNodeModel().getImagePath());
        if (success) {
            System.out.println("File uploaded to working root.");
        } else {
            System.out.println("Failed to upload locally.");
            return;
        }

        // 2. Pošalji kao BACKUP ako postoji buddy
        if (runtime.hasBuddy()) {
            try {
                File file = new File(path);
                byte[] content = Files.readAllBytes(file.toPath());
                String encoded = Base64.getEncoder().encodeToString(content);
                String filename = file.getName();

                String messageContent = filename + "::" + encoded;
                Message backup = new Message("BACKUP", runtime.getNodeModel().getListenPort(), messageContent);

                Sender.sendMessage(runtime.getBuddyIp(), runtime.getBuddyPort(), backup);
                System.out.println("Backup sent to " + runtime.getBuddyIp() + ":" + runtime.getBuddyPort());
            } catch (Exception e) {
                System.out.println("⚠️ Failed to send backup: " + e.getMessage());
            }
        }
    }
}