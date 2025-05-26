package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;

public class GetPredecessorHandler extends ResponseHandler {
    public GetPredecessorHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "GET_PREDECESSOR";
    }

    @Override
    public void handle(Message msg) {
        String targetId = msg.content();
        System.out.println("Received GET_PREDECESSOR for ID " + targetId + " from " + msg.senderIp() + ":" + msg.senderPort());

        Peer predecessor = runtime.getPredecessor();
        String content = (predecessor == null) ? "null" : predecessor.ip() + ":" + predecessor.port();

        Message response = new Message("PREDECESSOR_INFO", runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), content);

        String senderId = runtime.hashString(msg.senderIp() + ":" + msg.senderPort());
        runtime.forwardMessage(senderId, response);

        System.out.println("Sent PREDECESSOR_INFO to " + msg.senderIp() + ":" + msg.senderPort() + " → " + content);
    }
}