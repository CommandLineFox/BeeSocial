package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

public class BackupHandler extends ResponseHandler {
    public BackupHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "BACKUP";
    }

    @Override
    public void handle(Message msg) {
        String[] parts = msg.content().split("::", 2);
        if (parts.length != 2) {
            System.out.println("Invalid BACKUP message received.");
            return;
        }

        String filename = parts[0];
        String base64Content = parts[1];

        try {
            byte[] content = Base64.getDecoder().decode(base64Content);
            String backupFolder = runtime.getNodeModel().getWorkPath() + File.separator + "backup";
            String savePath = backupFolder + File.separator + msg.senderId() + "_" + filename;

            File saveFile = new File(savePath);
            saveFile.getParentFile().mkdirs();
            Files.write(saveFile.toPath(), content);

            System.out.println("Received backup file \"" + filename + "\" from Node " + msg.senderId());
        } catch (Exception e) {
            System.out.println("Failed to write backup file: " + e.getMessage());
        }
    }
}