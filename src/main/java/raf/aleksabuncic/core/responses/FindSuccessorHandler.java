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
        String targetId = msg.content();
        Peer successor = runtime.findSuccessor(targetId);
        Message response = new Message("FIND_SUCCESSOR_RESPONSE", runtime.getNodeModel().getListenPort(), successor.toString());
        response.setSenderIp("127.0.0.1"); // ako nema automatski
        response.setSenderPort(runtime.getNodeModel().getListenPort());
        raf.aleksabuncic.core.net.Sender.sendMessage(msg.senderIp(), msg.senderPort(), response);
    }
}
