package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

/**
 * Handles what happens when a node receives a UPLOAD_TRANSFER message.
 */
public class UploadTransferHandler extends ResponseHandler {
    public UploadTransferHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "UPLOAD_TRANSFER";
    }

    @Override
    public void handle(Message msg) {
        runtime.enterCriticalSection();

        try {
            String[] parts = msg.content().split("::", 2);
            if (parts.length != 2) {
                System.err.println("Malformed UPLOAD_TRANSFER message.");
                return;
            }

            String fileName = parts[0];
            byte[] data = Base64.getDecoder().decode(parts[1]);

            String uploadsDir = runtime.getNodeModel().getWorkPath() + File.separator + "uploads";
            File destFile = new File(uploadsDir, fileName);

            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                fos.write(data);
            }

            String backupDir = runtime.getNodeModel().getWorkPath() + File.separator + "backup";
            Files.copy(destFile.toPath(), new File(backupDir, fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Received and stored file: " + fileName);

        } catch (Exception e) {
            System.err.println("Failed to handle UPLOAD_TRANSFER: " + e.getMessage());
        } finally {
            runtime.exitCriticalSection();
        }
    }
}
