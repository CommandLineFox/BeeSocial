package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;

/**
 * Handles what happens when a node receives a PREDECESSOR_INFO message.
 */
public class PredecessorInfoHandler extends ResponseHandler {
    public PredecessorInfoHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "PREDECESSOR_INFO";
    }

    @Override
    public void handle(Message msg) {
        String content = msg.content();
        if ("null".equals(content)) {
            System.out.println("Received null predecessor.");
            return;
        }

        String[] parts = content.split(":");
        if (parts.length != 2) {
            System.err.println("Malformed PREDECESSOR_INFO message.");
            return;
        }

        String ip = parts[0];
        int port = Integer.parseInt(parts[1]);
        Peer potentialPredecessor = new Peer(ip, port);
        String id = runtime.hashPeer(potentialPredecessor);

        runtime.considerNewPredecessor(potentialPredecessor, id);
    }
}
