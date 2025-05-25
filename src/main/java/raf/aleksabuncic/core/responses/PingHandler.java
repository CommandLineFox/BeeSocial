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
    public void handle(Message message) {
        System.out.println("Received PING from Node " + message.senderPort());

        String localIp = runtime.getNodeModel().getListenIp();
        int localPort = runtime.getNodeModel().getListenPort();

        Message pong = new Message("PONG", localIp, localPort, "");
        Sender.sendMessage(message.senderIp(), message.senderPort(), pong);
    }
}