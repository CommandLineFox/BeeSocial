package raf.aleksabuncic.core.runtime;

import lombok.Getter;
import raf.aleksabuncic.core.net.ConnectionHandler;
import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Node;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class NodeRuntime {
    private final Node nodeModel;

    private final Set<Integer> followers = new HashSet<>();
    private final Set<Integer> pendingRequests = new HashSet<>();

    private String buddyIp = null;
    private int buddyPort = -1;

    private volatile boolean running = true;

    private final Set<Integer> recentBackupResponses = ConcurrentHashMap.newKeySet();

    public NodeRuntime(Node nodeModel) {
        this.nodeModel = nodeModel;
    }

    /**
     * Starts the node and it's ability to listen to incoming messages
     */
    public void start() {
        new Thread(new ConnectionHandler(this, nodeModel.getListenPort())).start();
    }

    /**
     * Shuts down the node gracefully.
     */
    public void shutdown() {
        System.out.println("Node shutting down gracefully...");
        running = false;
    }

    /**
     * Handle an incoming message from another node
     *
     * @param msg The message to handle
     */
    public void handleMessage(Message msg) {
        int senderId = msg.senderId();

        switch (msg.type()) {
            case "FOLLOW" -> {
                System.out.println("Received FOLLOW from Node " + senderId);
                pendingRequests.add(senderId);
            }

            case "ACCEPT" -> {
                System.out.println("Received ACCEPT from Node " + senderId);
                followers.add(senderId);
            }

            case "LIST_FILES" -> {
                System.out.println("Received LIST_FILES from Node " + senderId);

                boolean allowed = nodeModel.getVisibility() == raf.aleksabuncic.types.NodeVisibility.PUBLIC ||
                        followers.contains(senderId);

                if (!allowed) {
                    System.out.println("Access denied to Node " + senderId);
                    return;
                }

                var files = raf.aleksabuncic.util.FileUtils.listFilesInDirectory(nodeModel.getImagePath());
                String content = String.join(",", files);
                Message response = new Message("FILES_LIST", nodeModel.getListenPort(), content);

                raf.aleksabuncic.core.net.Sender.sendMessage("127.0.0.1", senderId, response);
                System.out.println("Sent FILES_LIST to Node " + senderId);
            }

            case "FILES_LIST" -> {
                System.out.println("Received FILES_LIST from Node " + senderId);
                String[] files = msg.content().split(",");
                if (files.length == 0 || (files.length == 1 && files[0].isEmpty())) {
                    System.out.println("Remote node has no files.");
                } else {
                    System.out.println("Files on remote node " + senderId + ":");
                    for (String f : files) {
                        System.out.println(" - " + f);
                    }
                }
            }

            case "PING" -> {
                System.out.println("Received PING from Node " + senderId);
                Message pong = new Message("PONG", nodeModel.getListenPort(), "");
                raf.aleksabuncic.core.net.Sender.sendMessage("127.0.0.1", senderId, pong);
            }

            case "PONG" -> System.out.println("Received PONG from Node " + senderId);

            case "BACKUP_REQUEST" -> {
                int requesterPort = msg.senderId();
                System.out.println("Received BACKUP_REQUEST from Node " + requesterPort);
                Message response = new Message("BACKUP_RESPONSE", nodeModel.getListenPort(), "");
                Sender.sendMessage("127.0.0.1", requesterPort, response);
            }

            case "BACKUP_RESPONSE" -> {
                System.out.println("Received BACKUP_RESPONSE from Node " + senderId);
                markBackupResponded(senderId);
            }


            default -> System.out.println("Unknown message type: " + msg.type());
        }
    }

    /**
     * Accepts a follow request
     *
     * @param senderId ID of the sender
     * @return True if accepted, false if not
     */
    public boolean acceptFollow(int senderId) {
        if (pendingRequests.remove(senderId)) {
            followers.add(senderId);
            return true;
        }
        return false;
    }

    /**
     * Sets the buddy of this node.
     *
     * @param ip   IP address of the buddy node. Can be null to remove the buddy.
     * @param port Port of the buddy node. Can be -1 to remove the buddy.
     */
    public void setBuddy(String ip, int port) {
        this.buddyIp = ip;
        this.buddyPort = port;
        System.out.println("Buddy set to " + ip + ":" + port);
    }

    /**
     * Checks if this node has a buddy.
     *
     * @return True if it has a buddy, false if not.
     */
    public boolean hasBuddy() {
        return buddyIp != null && buddyPort != -1;
    }

    /**
     * Marks a backup response as received.
     *
     * @param port Port of the backup node that responded.
     */
    public void markBackupResponded(int port) {
        recentBackupResponses.add(port);
    }

    /**
     * Checks if a backup response has been received from a specific node.
     *
     * @param port Port of the backup node to check.
     * @return True if a response has been received, false if not.
     */
    public boolean hasRecentBackupResponse(int port) {
        return recentBackupResponses.remove(port); // remove da ne ostaje u memoriji
    }
}