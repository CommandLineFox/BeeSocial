package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;

public class UpdatePredecessorHandler extends ResponseHandler {
    public UpdatePredecessorHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "UPDATE_PREDECESSOR";
    }

    @Override
    public void handle(Message msg) {
        String content = msg.content();
        String[] parts = content.split(":");
        if (parts.length != 2) {
            System.err.println("Malformed UPDATE_PREDECESSOR message.");
            return;
        }

        String ip = parts[0];
        int port = Integer.parseInt(parts[1]);
        Peer newPredecessor = new Peer(ip, port);
        runtime.setPredecessor(newPredecessor);
        runtime.setPredecessorId(runtime.hashPeer(newPredecessor));

        System.out.println("Updated predecessor to: " + newPredecessor);
    }
}