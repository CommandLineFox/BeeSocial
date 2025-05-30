package raf.aleksabuncic.core.process;

import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Peer;

import java.math.BigInteger;

/**
 * Periodično ažurira finger tabelu koristeći asinhroni hop-by-hop FIND_SUCCESSOR.
 */
public class FixFingers implements Runnable {
    private final NodeRuntime runtime;
    private final int m = 160;

    public FixFingers(NodeRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public void run() {
        while (runtime.isRunning()) {
            try {
                Peer successor = runtime.getSuccessor();
                String myId = runtime.getNodeModel().getChordId();

                if (successor == null || runtime.getSuccessorId() == null) {
                    Thread.sleep(1000);
                    continue;
                }

                if (runtime.getSuccessorId().equals(myId)) {
                    Thread.sleep(5000);
                    continue;
                }

                BigInteger myInt = new BigInteger(myId, 16);

                for (int i = 0; i < Math.min(5, m); i++) {
                    BigInteger offset = BigInteger.TWO.pow(i);
                    BigInteger target = myInt.add(offset).mod(BigInteger.TWO.pow(m));
                    String targetId = String.format("%040x", target);

                    runtime.findSuccessorAsync(targetId);

                    Thread.sleep(100);
                }

                Thread.sleep(5000);

            } catch (Exception e) {
                System.err.println("FixFingers error: " + e.getMessage());
            }
        }
    }
}