package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;

public class FindSuccessorResponseHandler extends ResponseHandler {

    public FindSuccessorResponseHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "FIND_SUCCESSOR_RESPONSE";
    }

    @Override
    public void handle(Message msg) {
        System.out.println("Received FIND_SUCCESSOR_RESPONSE: " + msg.content());

        String[] parts = msg.content().split(":");
        if (parts.length != 2) {
            System.err.println("Malformed FIND_SUCCESSOR_RESPONSE");
            return;
        }

        String ip = parts[0];
        int port = Integer.parseInt(parts[1]);

        Peer successor = new Peer(ip, port);
        runtime.setSuccessor(successor);

        System.out.println("Updated local successor to: " + ip + ":" + port);

        runtime.notifySuccessor();
    }
}