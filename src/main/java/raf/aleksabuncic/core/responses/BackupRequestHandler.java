package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.core.net.Sender;

public class BackupRequestHandler extends ResponseHandler {
    public BackupRequestHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "BACKUP_REQUEST";
    }

    @Override
    public void handle(Message msg) {
        System.out.println("Received BACKUP_REQUEST from Node " + msg.senderId());
        Message response = new Message("BACKUP_RESPONSE", runtime.getNodeModel().getListenPort(), "");
        Sender.sendMessage("127.0.0.1", msg.senderId(), response);
    }
}