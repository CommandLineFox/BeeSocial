package raf.aleksabuncic.core.stabilizer;

import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;

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
                if (successor == null || successor.port() == runtime.getNodeModel().getListenPort()) {
                    Thread.sleep(3000);
                    continue;
                }

                Message request = new Message("GET_PREDECESSOR",
                        runtime.getNodeModel().getListenIp(),
                        runtime.getNodeModel().getListenPort(),
                        "");
                Message response = Sender.sendMessageWithResponse(successor.ip(), successor.port(), request);

                if (response != null && "PREDECESSOR_INFO".equals(response.type())) {
                    String[] parts = response.content().split(":");
                    if (parts.length == 2) {
                        Peer candidate = new Peer(parts[0], Integer.parseInt(parts[1]));
                        String candidateId = runtime.hashPeer(candidate);

                        String selfId = runtime.getNodeModel().getChordId();
                        String succId = runtime.hashPeer(successor);

                        if (runtime.isBetween(selfId, candidateId, succId)) {
                            runtime.setSuccessor(candidate);
                            System.out.println("Stabilizer: Updated successor to " + candidate);
                        }
                    }
                }

                runtime.notifySuccessor();

                Thread.sleep(3000);
            } catch (Exception e) {
                System.out.println("Stabilizer error: " + e.getMessage());
            }
        }
    }
}