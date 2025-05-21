package raf.aleksabuncic.core.runtime;

import lombok.Getter;
import raf.aleksabuncic.core.net.ConnectionHandler;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Node;

import java.util.HashSet;
import java.util.Set;

public class NodeRuntime {
    @Getter
    private final Node nodeModel;

    private final Set<Integer> followers = new HashSet<>();
    private final Set<Integer> pendingRequests = new HashSet<>();

    public NodeRuntime(Node nodeModel) {
        this.nodeModel = nodeModel;
    }

    public void start() {
        new Thread(new ConnectionHandler(this, nodeModel.getListenPort())).start();
    }

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

            case "PING" -> {
                System.out.println("Received PING from Node " + senderId);
            }

            case "UPLOAD" -> {
                System.out.println("Received UPLOAD from Node " + senderId + ": " + msg.content());
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

            default -> System.out.println("Unknown message type: " + msg.type());
        }
    }

    public Set<Integer> getFollowers() {
        return followers;
    }

    public Set<Integer> getPendingRequests() {
        return pendingRequests;
    }

    public boolean acceptFollow(int senderId) {
        if (pendingRequests.remove(senderId)) {
            followers.add(senderId);
            return true;
        }
        return false;
    }
}