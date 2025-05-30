package raf.aleksabuncic.core.process;

import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;

/**
 * Handles the stabilization process by periodically sending GET_PREDECESSOR messages
 * and trying to discover a better successor if the current one is missing or self.
 */
public class Stabilizer implements Runnable {
    private final NodeRuntime runtime;

    public Stabilizer(NodeRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public void run() {
        while (runtime.isRunning()) {
            try {
                Peer successor = runtime.getSuccessor();
                String myId = runtime.getNodeModel().getChordId();
                String successorId = runtime.getSuccessorId();

                if (successor == null || successorId == null || successorId.equals(myId)) {
                    boolean foundPeer = false;
                    for (Peer peer : runtime.getKnownPeers()) {
                        if (peer.port() == runtime.getNodeModel().getListenPort()) {
                            continue;
                        }
                        System.out.println("[Stabilizer] Attempting to find better successor from peer: " + peer);
                        String targetId = runtime.hashPeer(peer);
                        Message msg = new Message("FIND_SUCCESSOR", runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), runtime.getNodeModel().getChordId());
                        Sender.sendMessage(peer.ip(), peer.port(), msg);
                        foundPeer = true;
                        break;
                    }

                    if (!foundPeer) {
                        System.out.println("[Stabilizer] No known peers to find successor from.");
                    }

                    Thread.sleep(3000);
                    continue;
                }

                Message request = new Message("GET_PREDECESSOR", runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), "");
                String targetId = runtime.hashPeer(successor);
                runtime.forwardMessage(targetId, request);

                runtime.notifySuccessor();

                Thread.sleep(3000);

            } catch (Exception e) {
                System.out.println("Stabilizer error: " + e.getMessage());
            }
        }
    }
}