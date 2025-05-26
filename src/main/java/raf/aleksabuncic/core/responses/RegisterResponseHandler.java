package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;

import java.util.Arrays;

/**
 * Handles what happens when a node responds to a REGISTER request.
 */
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
        if (msg.content() == null || msg.content().isBlank()) {
            System.out.println("Bootstrap server returned no peers.");
            return;
        }

        String[] peerStrings = msg.content().split(",");
        for (String p : peerStrings) {
            String[] parts = p.split(":");
            if (parts.length != 2) continue;

            String ip = parts[0];
            int port = Integer.parseInt(parts[1]);

            if (port == runtime.getNodeModel().getListenPort()) continue;

            runtime.addPeer(new Peer(ip, port));
        }

        System.out.println("Received peers from bootstrap: " + Arrays.toString(peerStrings));
    }
}