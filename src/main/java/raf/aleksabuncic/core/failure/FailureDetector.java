package raf.aleksabuncic.core.failure;

import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

import java.io.File;
import java.nio.file.Files;

import static raf.aleksabuncic.util.FileUtils.extractOriginalFileName;

public class FailureDetector implements Runnable {
    private final NodeRuntime runtime;
    private long lastPongTime = System.currentTimeMillis();

    public FailureDetector(NodeRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public void run() {
        while (runtime.isRunning()) {
            try {
                if (!runtime.hasBuddy()) {
                    Thread.sleep(1000);
                    continue;
                }

                Message ping = new Message("PING", runtime.getNodeModel().getListenPort(), "");
                Sender.sendMessage(runtime.getBuddyIp(), runtime.getBuddyPort(), ping);
                System.out.println("Sent PING to buddy");

                Thread.sleep(1000);

                long now = System.currentTimeMillis();
                long elapsed = now - lastPongTime;

                int weak = runtime.getNodeModel().getWeakThreshold();
                int strong = runtime.getNodeModel().getStrongThreshold();

                if (elapsed >= weak && elapsed < strong) {
                    System.out.println("Weak suspicion: Buddy may be unresponsive.");
                }

                if (elapsed >= strong) {
                    System.out.println("Strong suspicion: Buddy failed. Restoring from backup...");
                    restoreFromBackup();
                    lastPongTime = System.currentTimeMillis();
                }

                Thread.sleep(2000);

            } catch (Exception e) {
                System.out.println("FailureDetector error: " + e.getMessage());
            }
        }
    }

    /**
     * Notifies that a PONG message has been received.
     */
    public void notifyPongReceived() {
        lastPongTime = System.currentTimeMillis();
    }

    /**
     * Restores all files from the backup folder to the main folder.
     */
    private void restoreFromBackup() {
        String backupDir = runtime.getNodeModel().getImagePath() + File.separator + "backup";
        String mainDir = runtime.getNodeModel().getImagePath();

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
