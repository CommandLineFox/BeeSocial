package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;

public class FindSuccessorHandler extends ResponseHandler {
    public FindSuccessorHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "FIND_SUCCESSOR";
    }

    @Override
    public void handle(Message msg) {
        System.out.println("Received FIND_SUCCESSOR for ID " + msg.content());
        String targetId = msg.content();

        Peer successor = runtime.findSuccessor(targetId);
        if (successor == null) {
            System.out.println("Could not resolve successor for ID: " + targetId);
            return;
        }

        String responseContent = successor.ip() + ":" + successor.port();

        Message response = new Message("FIND_SUCCESSOR_RESPONSE", runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), responseContent);

        String senderId = runtime.hashString(msg.senderIp() + ":" + msg.senderPort());
        runtime.forwardMessage(senderId, response);

        System.out.println("Handled FIND_SUCCESSOR for ID " + targetId + " → " + successor);
    }
}