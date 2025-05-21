package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

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
        System.out.println("Received FOLLOW from Node " + msg.senderId());
        runtime.getPendingRequests().add(msg.senderId());
    }
}
