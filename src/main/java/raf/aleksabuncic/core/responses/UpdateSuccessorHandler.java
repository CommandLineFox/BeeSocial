package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;

/**
 * Handles what happens when a node receives a UPDATE_SUCCESSOR message.
 */
public class UpdateSuccessorHandler extends ResponseHandler {
    public UpdateSuccessorHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "UPDATE_SUCCESSOR";
    }

    @Override
    public void handle(Message msg) {
        String content = msg.content();
        String[] parts = content.split(":");
        if (parts.length != 2) {
            System.err.println("Malformed UPDATE_SUCCESSOR message.");
            return;
        }

        String ip = parts[0];
        int port = Integer.parseInt(parts[1]);
        Peer newSuccessor = new Peer(ip, port);
        runtime.setSuccessor(newSuccessor);
        runtime.setSuccessorId(runtime.hashPeer(newSuccessor));

        System.out.println("Updated successor to: " + newSuccessor);
    }
}