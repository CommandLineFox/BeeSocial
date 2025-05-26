package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

/**
 * Handles what happens when a node requests to follow another node.
 */
public class FollowHandler extends ResponseHandler {
    public FollowHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "FOLLOW";
    }

    @Override
    public void handle(Message msg) {
        int senderPort = msg.senderPort();
        runtime.getPendingRequests().add(senderPort);
        //System.out.println("Received FOLLOW from Node " + senderPort);
    }
}