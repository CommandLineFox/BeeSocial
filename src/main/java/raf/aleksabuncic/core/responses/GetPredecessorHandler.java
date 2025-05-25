package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.net.Sender;
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
        Peer predecessor = runtime.getPredecessor();
        String content;

        if (predecessor == null) {
            content = "null";
        } else {
            content = predecessor.ip() + ":" + predecessor.port();
        }

        if (!"null".equals(content)) {
            Message response = new Message("PREDECESSOR_INFO", runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), content);

            Sender.sendMessage(msg.senderIp(), msg.senderPort(), response);
            System.out.println("Sent PREDECESSOR_INFO to " + msg.senderIp() + ":" + msg.senderPort() + " → " + content);
        }
    }
}
