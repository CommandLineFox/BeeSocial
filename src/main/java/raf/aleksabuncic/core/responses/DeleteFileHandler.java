package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

import java.io.File;

/**
 * Handles what happens when a node requests to delete a file.
 */
public class DeleteFileHandler extends ResponseHandler {
    public DeleteFileHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "DELETE_FILE";
    }

    @Override
    public void handle(Message message) {
        String fileName = message.content();

        String uploadsPath = runtime.getNodeModel().getWorkPath() + File.separator + "uploads" + File.separator + fileName;
        String backupPath = runtime.getNodeModel().getWorkPath() + File.separator + "backup" + File.separator + fileName;

        boolean deletedMain = new File(uploadsPath).delete();
        boolean deletedBackup = new File(backupPath).delete();

        System.out.println("DELETE_FILE: '" + fileName + "' → uploads: " + deletedMain + ", backup: " + deletedBackup);
    }
}
