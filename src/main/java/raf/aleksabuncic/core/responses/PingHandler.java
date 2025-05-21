package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.core.net.Sender;

public class PingHandler extends ResponseHandler {
    public PingHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "PING";
    }

    @Override
    public void handle(Message msg) {
        System.out.println("Received PING from Node " + msg.senderId());
        Message pong = new Message("PONG", runtime.getNodeModel().getListenPort(), "");
        Sender.sendMessage("127.0.0.1", msg.senderId(), pong);
    }
}
