package raf.aleksabuncic.core.runtime;

import lombok.Getter;
import raf.aleksabuncic.core.net.ConnectionHandler;
import raf.aleksabuncic.core.response.ResponseRegistry;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Node;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class NodeRuntime {
    private final Node nodeModel;
    private final Set<Integer> followers;
    private final Set<Integer> pendingRequests;

    private String buddyIp;
    private int buddyPort;

    private volatile boolean running;

    private final ResponseRegistry responseRegistry;
    private final Set<Integer> recentBackupResponses;


    public NodeRuntime(Node nodeModel) {
        this.nodeModel = nodeModel;
        this.followers = new HashSet<>();
        this.pendingRequests = ConcurrentHashMap.newKeySet();
        this.buddyIp = null;
        this.buddyPort = -1;
        this.running = true;
        this.responseRegistry = new ResponseRegistry(this);
        this.recentBackupResponses = ConcurrentHashMap.newKeySet();
    }

    /**
     * Starts the node, and it's ability to listen to incoming messages
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
        boolean handled = responseRegistry.handle(msg);
        if (!handled) {
            System.out.println("Unknown message type: " + msg.type());
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
        return recentBackupResponses.remove(port);
    }
}