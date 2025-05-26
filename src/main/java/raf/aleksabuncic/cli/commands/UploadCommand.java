package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.util.Utils;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

/**
 * Handles the 'upload' command by hashing the file name and
 * forwarding the file to the responsible node via Chord routing.
 */
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
        if (args.length != 2) {
            System.out.println("Usage: upload [path/to/file]");
            return;
        }

        String filePath = args[1];
        File file = new File(filePath);

        if (!file.exists() || !file.isFile()) {
            System.out.println("File not found: " + filePath);
            return;
        }

        if (!Utils.checkIfMediaFile(filePath)) {
            System.out.println("Unsupported file type. Only media files are allowed.");
            return;
        }

        String fileName = file.getName();
        String hash = runtime.hashString(fileName);

        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String encoded = Base64.getEncoder().encodeToString(fileBytes);
            String content = fileName + "::" + encoded;

            Message uploadMsg = new Message("UPLOAD_TRANSFER", runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), content);
            runtime.forwardMessage(hash, uploadMsg);
            System.out.println("Uploading file '" + fileName + "' via Chord...");

        } catch (Exception e) {
            System.err.println("Failed to upload file: " + e.getMessage());
        }
    }
}