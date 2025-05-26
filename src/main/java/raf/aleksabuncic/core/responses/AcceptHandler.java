package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

/**
 * Handles what happens when a node accepts a follow request.
 */
public class AcceptHandler extends ResponseHandler {
    public AcceptHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "ACCEPT";
    }

    @Override
    public void handle(Message msg) {
        System.out.println("Received ACCEPT from Node " + msg.senderPort());
        runtime.getFollowers().add(msg.senderPort());
    }
}