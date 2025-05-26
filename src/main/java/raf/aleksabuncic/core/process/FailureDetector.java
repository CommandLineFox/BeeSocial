package raf.aleksabuncic.core.process;

import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;
import raf.aleksabuncic.util.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Handles the failure detection process by periodically sending PING messages
 */
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
                    Thread.sleep(2000);
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

                    runtime.setSuccessor(null);
                    runtime.setSuccessorId(null);

                    restoreFromBackup();

                    for (Peer peer : runtime.getKnownPeers()) {
                        if (peer.port() == runtime.getNodeModel().getListenPort()) continue;
                        Sender.sendFindSuccessor(peer, runtime.getNodeModel().getChordId(), runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort());
                        System.out.println("Trying to find replacement successor via peer: " + peer);
                        break;
                    }

                    lastPongTime = System.currentTimeMillis();
                }

                Thread.sleep(20000);

            } catch (Exception e) {
                System.out.println("FailureDetector error: " + e.getMessage());
            }
        }
    }

    /**
     * Notifies the failure detector that a PONG message has been received,
     * resetting the timer and unblocking any waiting threads.
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
     * Restores all files from the backup folder to the working upload folder.
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
                File dest = new File(mainDir, FileUtils.extractOriginalFileName(file.getName()));
                Files.copy(file.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Restored: " + file.getName());
            } catch (Exception e) {
                System.out.println("Failed to restore " + file.getName() + ": " + e.getMessage());
            }
        }
    }
}