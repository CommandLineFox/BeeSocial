package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

/**
 * Handles what happens when a node receives a PING message.
 */
public class PingHandler extends ResponseHandler {
    public PingHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "PING";
    }

    @Override
    public void handle(Message message) {
        System.out.println("Received PING from Node " + message.senderIp() + ":" + message.senderPort());

        Message pong = new Message("PONG", runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), "");

        String senderId = runtime.hashString(message.senderIp() + ":" + message.senderPort());
        runtime.forwardMessage(senderId, pong);

        System.out.println("Responded with PONG via forwardMessage to " + message.senderIp() + ":" + message.senderPort());
    }
}