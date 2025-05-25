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
    public void handle(Message message) {
        Peer pred = runtime.getPredecessor();
        String content = (pred == null) ? "" : pred.ip() + ":" + pred.port();
        Message response = new Message("PREDECESSOR_INFO", runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), content);
        Sender.sendMessage(message.senderIp(), message.senderPort(), response);
    }
}
