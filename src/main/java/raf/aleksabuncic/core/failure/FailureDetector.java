package raf.aleksabuncic.core.failure;

import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

import java.io.File;

public class FailureDetector implements Runnable {
    private final NodeRuntime runtime;

    private long lastPongTime = System.currentTimeMillis();
    private int missedPings = 0;

    public FailureDetector(NodeRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public void run() {
        while (runtime.isRunning()) {
            try {
                if (!runtime.hasBuddy()) {
                    Thread.sleep(2000);
                    continue;
                }

                Message ping = new Message("PING", runtime.getNodeModel().getListenPort(), "");
                Sender.sendMessage(runtime.getBuddyIp(), runtime.getBuddyPort(), ping);

                Thread.sleep(1000);

                long now = System.currentTimeMillis();
                if (now - lastPongTime > 1000) {
                    missedPings++;
                    System.out.println("Missed PING response from buddy (" + missedPings + ")");
                } else {
                    missedPings = 0;
                }

                int weak = runtime.getNodeModel().getWeakThreshold();
                int strong = runtime.getNodeModel().getStrongThreshold();

                if (missedPings == weak) {
                    System.out.println("⚠Weak suspicion: Buddy may be down...");
                }

                if (missedPings >= strong) {
                    System.out.println("Strong suspicion: Buddy is down. Restoring from backup...");
                    restoreFromBackup();
                    missedPings = 0;
                }

                Thread.sleep(2000);

            } catch (Exception e) {
                System.out.println("FailureDetector error: " + e.getMessage());
            }
        }
    }

    /**
     * Notifies the failure detector that a PONG message has been received.
     */
    public void notifyPongReceived() {
        lastPongTime = System.currentTimeMillis();
    }

    private void restoreFromBackup() {
        String backupDir = runtime.getNodeModel().getImagePath() + "/backup";
        String mainDir = runtime.getNodeModel().getImagePath();

        File[] files = new java.io.File(backupDir).listFiles();
        if (files == null || files.length == 0) {
            System.out.println("No backup files found.");
            return;
        }

        for (File file : files) {
            try {
                java.nio.file.Files.copy(file.toPath(), new java.io.File(mainDir + "/" + extractOriginalFileName(file.getName())).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Restored backup: " + file.getName());
            } catch (Exception e) {
                System.out.println("Failed to restore " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Extracts the original file name from the backup file name.
     *
     * @param backupName Name of the backup file. Example: "backup_127.0.0.1_my_image.jpg"
     * @return Original file name. Example: "my_image.jpg"
     */
    private String extractOriginalFileName(String backupName) {
        int underscore = backupName.indexOf('_');
        if (underscore == -1) return backupName;
        return backupName.substring(underscore + 1);
    }
}