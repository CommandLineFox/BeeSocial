package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;

public class NotifyHandler extends ResponseHandler {
    public NotifyHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "NOTIFY";
    }

    @Override
    public void handle(Message msg) {
        String senderIp = msg.senderId() != -1 ? "127.0.0.1" : "unknown";
        int senderPort = msg.senderId();
        String senderId = msg.content();

        Peer potentialPredecessor = new Peer(senderIp, senderPort);
        runtime.considerNewPredecessor(potentialPredecessor, senderId);
    }
}