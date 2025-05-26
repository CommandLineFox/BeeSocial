package raf.aleksabuncic.core.process;

import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;

/**
 * Handles the stabilization process by periodically sending GET_PREDECESSOR messages
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
                if (successor == null || successor.port() == runtime.getNodeModel().getListenPort()) {
                    Thread.sleep(3000);
                    continue;
                }

                Message request = new Message("GET_PREDECESSOR", runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), "");
                Sender.sendMessage(successor.ip(), successor.port(), request);
                System.out.println("Stabilizer: Sent GET_PREDECESSOR to " + successor);

                runtime.notifySuccessor();

                Thread.sleep(3000);
            } catch (Exception e) {
                System.out.println("Stabilizer error: " + e.getMessage());
            }
        }
    }
}