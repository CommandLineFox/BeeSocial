package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

public class BackupResponseHandler extends ResponseHandler {
    public BackupResponseHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "BACKUP_RESPONSE";
    }

    @Override
    public void handle(Message msg) {
        System.out.println("Received BACKUP_RESPONSE from Node " + msg.senderId());
        runtime.markBackupResponded(msg.senderId());
    }
}
