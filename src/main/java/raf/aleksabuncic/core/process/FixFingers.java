package raf.aleksabuncic.core.process;

import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Peer;

import java.math.BigInteger;

/**
 * Handles the fix fingers process by periodically updating the finger table.
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
                if (runtime.getSuccessor() == null || runtime.getSuccessorId() == null) {
                    System.out.println("FixFingers: Skipping, successor not yet known.");
                    Thread.sleep(1000);
                    continue;
                }

                String myId = runtime.getNodeModel().getChordId();
                BigInteger myInt = new BigInteger(myId, 16);

                for (int i = 0; i < m; i++) {
                    BigInteger offset = BigInteger.TWO.pow(i);
                    BigInteger target = myInt.add(offset).mod(BigInteger.TWO.pow(m));
                    String targetId = String.format("%040x", target);

                    Peer finger = runtime.findSuccessor(targetId);

                    if (finger == null) {

                        System.out.println("FixFingers: Could not find finger[" + i + "] (null)");
                        continue;
                    }

                    if (runtime.getFingerTable().size() <= i) {
                        runtime.getFingerTable().add(finger);
                        System.out.println("FixFingers: Added finger[" + i + "] → " + finger);
                    } else {
                        Peer current = runtime.getFingerTable().get(i);
                        if (!current.equals(finger)) {
                            runtime.getFingerTable().set(i, finger);
                            System.out.println("FixFingers: Updated finger[" + i + "] → " + finger);
                        }
                    }

                    Thread.sleep(10);
                }

                Thread.sleep(5000);

            } catch (Exception e) {
                System.err.println("FixFingers error: " + e.getMessage());
            }
        }
    }
}