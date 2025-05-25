package raf.aleksabuncic.core.failure;

import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static raf.aleksabuncic.util.FileUtils.extractOriginalFileName;

public class FailureDetector implements Runnable {
    private final NodeRuntime runtime;
    private long lastPongTime = System.currentTimeMillis();
    private final Lock lock = new ReentrantLock();
    private final Condition pongReceived = lock.newCondition();

    public FailureDetector(NodeRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public void run() {
        while (runtime.isRunning()) {
            try {
                Peer target = runtime.getSuccessor();
                if (target == null || target.port() == runtime.getNodeModel().getListenPort()) {
                    Thread.sleep(1000);
                    continue;
                }

                String myIp = runtime.getNodeModel().getListenIp();
                int myPort = runtime.getNodeModel().getListenPort();

                Message ping = new Message("PING", myIp, myPort, "");
                Sender.sendMessage(target.ip(), target.port(), ping);
                System.out.println("Sent PING to successor " + target);

                lock.lock();
                try {
                    int timeout = runtime.getNodeModel().getStrongThreshold();
                    pongReceived.awaitNanos(timeout * 1_000_000L);
                } finally {
                    lock.unlock();
                }

                long now = System.currentTimeMillis();
                long elapsed = now - lastPongTime;

                int weak = runtime.getNodeModel().getWeakThreshold();
                int strong = runtime.getNodeModel().getStrongThreshold();

                if (elapsed >= weak && elapsed < strong) {
                    System.out.println("Weak suspicion: Successor may be unresponsive.");
                }

                if (elapsed >= strong) {
                    System.out.println("Strong suspicion: Successor failed. Attempting recovery...");
                    restoreFromBackup();

                    Peer newSuccessor = null;
                    for (Peer peer : runtime.getKnownPeers()) {
                        if (peer.port() == runtime.getNodeModel().getListenPort()) continue;
                        Peer candidate = Sender.sendFindSuccessor(peer, runtime.getNodeModel().getChordId());
                        if (candidate != null) {
                            newSuccessor = candidate;
                            break;
                        }
                    }

                    if (newSuccessor != null) {
                        runtime.setSuccessor(newSuccessor);
                        System.out.println("Updated successor to: " + newSuccessor);
                        runtime.notifySuccessor();
                    } else {
                        System.out.println("No valid successor found. Acting as own successor.");
                        runtime.setSuccessor(new Peer("127.0.0.1", runtime.getNodeModel().getListenPort()));
                    }

                    lastPongTime = System.currentTimeMillis();
                }

                Thread.sleep(500); // short pause before next PING

            } catch (Exception e) {
                System.out.println("FailureDetector error: " + e.getMessage());
            }
        }
    }

    /**
     * Notifies that a PONG message has been received.
     */
    public void notifyPongReceived() {
        lock.lock();
        try {
            lastPongTime = System.currentTimeMillis();
            pongReceived.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Restores all files from the backup folder to the main folder.
     */
    private void restoreFromBackup() {
        String backupDir = runtime.getNodeModel().getWorkPath() + File.separator + "backup";
        String mainDir = runtime.getNodeModel().getWorkPath() + File.separator + "uploads";

        File folder = new File(backupDir);
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("No backup files found.");
            return;
        }

        for (File file : files) {
            try {
                File dest = new File(mainDir, extractOriginalFileName(file.getName()));
                Files.copy(file.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Restored: " + file.getName());
            } catch (Exception e) {
                System.out.println("Failed to restore " + file.getName() + ": " + e.getMessage());
            }
        }
    }
}