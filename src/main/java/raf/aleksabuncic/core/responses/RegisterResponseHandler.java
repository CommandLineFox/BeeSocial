package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;

public class RegisterResponseHandler extends ResponseHandler {
    public RegisterResponseHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "REGISTER_RESPONSE";
    }

    @Override
    public void handle(Message msg) {
        if (msg.content().isBlank()) {
            System.out.println("Bootstrap: no other nodes to connect to.");
            return;
        }

        String[] entries = msg.content().split(",");
        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length != 2) continue;

            String ip = parts[0];
            int port = Integer.parseInt(parts[1]);

            Peer peer = new Peer(ip, port);
            runtime.addPeer(peer);
        }

        System.out.println("Registered peers: " + runtime.getKnownPeers());
    }
}