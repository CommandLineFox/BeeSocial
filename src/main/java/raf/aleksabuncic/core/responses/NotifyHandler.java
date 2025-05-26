package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;

/**
 * Handles what happens when a node receives a NOTIFY message.
 */
public class NotifyHandler extends ResponseHandler {
    public NotifyHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "NOTIFY";
    }

    @Override
    public void handle(Message message) {
        Peer potentialPredecessor = new Peer(message.senderIp(), message.senderPort());
        runtime.considerNewPredecessor(potentialPredecessor, message.content());
    }
}