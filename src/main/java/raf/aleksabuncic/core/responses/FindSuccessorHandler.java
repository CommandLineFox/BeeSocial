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
        System.out.println("Received FIND_SUCCESSOR: " + msg.content());

        String[] parts = msg.content().split("::");
        String targetId = parts[0];
        String initiatorId = parts.length > 1 ? parts[1] : runtime.getNodeModel().getChordId() + ":" + runtime.getNodeModel().getListenPort();

        Peer successor = runtime.getSuccessor();
        String successorId = runtime.getSuccessorId();
        String myId = runtime.getNodeModel().getChordId();

        if (successor != null && successorId != null && runtime.isBetween(myId, targetId, successorId)) {
            String responseContent = successor.ip() + ":" + successor.port();

            String initiatorIp;
            int initiatorPort;
            try {
                String[] initiatorParts = initiatorId.split(":");
                initiatorIp = initiatorParts[0];
                initiatorPort = Integer.parseInt(initiatorParts[1]);
            } catch (Exception e) {
                System.err.println("Invalid initiator info in FIND_SUCCESSOR: " + initiatorId);
                return;
            }

            Message response = new Message("FIND_SUCCESSOR_RESPONSE", runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), responseContent);
            runtime.forwardMessage(runtime.hashString(initiatorIp + ":" + initiatorPort), response);

            System.out.println("Handled FIND_SUCCESSOR for ID " + targetId + " → " + successor);
            return;
        }

        Peer nextHop = runtime.closestPrecedingFinger(targetId);
        if (nextHop == null) {
            System.err.println("No next hop found for FIND_SUCCESSOR with targetId: " + targetId);
            return;
        }

        String contentToForward = targetId + "::" + initiatorId;
        Message forwardMsg = new Message("FIND_SUCCESSOR", runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), contentToForward);

        runtime.forwardMessage(runtime.hashPeer(nextHop), forwardMsg);

        System.out.println("Forwarded FIND_SUCCESSOR for ID " + targetId + " to " + nextHop);
    }
}