package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;
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

        Peer successor = runtime.getSuccessor();
        if (successor != null && successor.port() != runtime.getNodeModel().getListenPort()) {
            try {
                byte[] content = Files.readAllBytes(sourceFile.toPath());
                String encoded = Base64.getEncoder().encodeToString(content);
                String messageContent = sourceFile.getName() + "::" + encoded;

                String localIp = runtime.getNodeModel().getListenIp();
                int localPort = runtime.getNodeModel().getListenPort();
                Message backup = new Message("BACKUP", localIp, localPort, messageContent);

                Sender.sendMessage(successor.ip(), successor.port(), backup);
                System.out.println("Backup sent to successor: " + successor);
            } catch (Exception e) {
                System.out.println("Failed to send backup: " + e.getMessage());
            }
        }
    }
}