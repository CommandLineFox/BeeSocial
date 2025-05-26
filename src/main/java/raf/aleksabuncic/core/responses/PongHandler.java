package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

/**
 * Handles what happens when a node responds to a PING request.
 */
public class PongHandler extends ResponseHandler {
    public PongHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "PONG";
    }

    @Override
    public void handle(Message msg) {
        System.out.println("Received PONG from Node " + msg.senderPort());
        runtime.getFailureDetector().notifyPongReceived();
    }
}