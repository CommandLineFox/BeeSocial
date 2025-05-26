package raf.aleksabuncic.core.runtime;

import lombok.Getter;
import lombok.Setter;
import raf.aleksabuncic.core.process.FailureDetector;
import raf.aleksabuncic.core.net.ConnectionHandler;
import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.process.FixFingers;
import raf.aleksabuncic.core.response.ResponseRegistry;
import raf.aleksabuncic.core.process.Stabilizer;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Node;
import raf.aleksabuncic.types.Peer;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class NodeRuntime {
    /**
     * The node that this runtime is tied to
     */
    private final Node nodeModel;

    /**
     * List of followers
     */
    private final Set<Integer> followers = ConcurrentHashMap.newKeySet();

    /**
     * Pending follow request list
     */
    private final Set<Integer> pendingRequests = ConcurrentHashMap.newKeySet();

    /**
     * Check for whether the system is running or not for graceful shutdown
     */
    private volatile boolean running = true;

    /**
     * Registry for handling incoming messages
     */
    private final ResponseRegistry responseRegistry = new ResponseRegistry(this);

    /**
     * Recent backup responses for awaiting reply
     */
    private final Set<Integer> recentBackupResponses = ConcurrentHashMap.newKeySet();

    /**
     * Failure detector
     */
    private final FailureDetector failureDetector = new FailureDetector(this);

    /**
     * Stabilizer thread that periodically checks for node stability and updates successor and predecessor if needed.
     */
    private final Stabilizer stabilizer = new Stabilizer(this);

    /**
     * Fix fingers thread that periodically checks for node stability and updates finger table if needed.
     */
    private final FixFingers fixFingers = new FixFingers(this);

    /**
     * Known nodes to communicate with
     */
    private final Set<Peer> knownPeers = ConcurrentHashMap.newKeySet();

    /**
     * Chord successor node
     */
    @Setter
    private Peer successor;

    /**
     * Chord ID of the current successor
     */
    @Setter
    private String successorId;

    /**
     * Chord predecessor node
     */
    @Setter
    private Peer predecessor;

    /**
     * Chord ID of the current predecessor
     */
    @Setter
    private String predecessorId;

    /**
     * Finger table for efficient O(log N) lookups.
     * Each entry points to a node that succeeds (id + 2^i) mod 2^m
     */
    private final List<Peer> fingerTable = new ArrayList<>();

    public NodeRuntime(Node nodeModel) {
        this.nodeModel = nodeModel;
    }

    /**
     * Starts the node, and its ability to listen to incoming messages
     */
    public void start() {
        new Thread(new ConnectionHandler(this, nodeModel.getListenPort())).start();
        new Thread(failureDetector).start();
        new Thread(stabilizer).start();
        new Thread(fixFingers).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        registerWithBootstrap();
    }

    /**
     * Shuts down the node gracefully.
     */
    public void shutdown() {
        running = false;
        System.out.println("Node shut down.");
    }

    /**
     * Handles incoming messages and dispatches them to the correct response handler.
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
     * Register with the bootstrap server and attempt to find successor in the ring
     */
    public void registerWithBootstrap() {
        try {
            String bootstrapIp = nodeModel.getBootstrapIp();
            int bootstrapPort = nodeModel.getBootstrapPort();

            String myIp = nodeModel.getListenIp();
            int myPort = nodeModel.getListenPort();

            Message request = new Message("REGISTER_REQUEST", myIp, myPort, String.valueOf(myPort));
            Message response = Sender.sendMessageWithResponse(bootstrapIp, bootstrapPort, request);

            if (response != null && "REGISTER_RESPONSE".equals(response.type())) {
                handleMessage(response);
                System.out.println("Successfully registered with bootstrap server.");
                System.out.println("[DEBUG] knownPeers after bootstrap = " + knownPeers);

                for (Peer peer : knownPeers) {
                    System.out.println("Requesting successor info from peer: " + peer);
                    Sender.sendFindSuccessor(peer, nodeModel.getChordId(), myIp, myPort);
                    break;
                }

                if (successor == null) {
                    this.successor = new Peer(myIp, myPort);
                    this.successorId = nodeModel.getChordId();
                    System.out.println("No peers found. Acting as own successor.");
                }

            } else {
                System.err.println("Did not receive valid response from bootstrap.");
            }

        } catch (Exception e) {
            System.err.println("Failed to register with bootstrap server: " + e.getMessage());
        }
    }

    /**
     * Finds the successor of a given Chord ID in the ring.
     *
     * @param targetId Chord ID to resolve
     * @return Peer that is responsible for the target ID
     */
    public Peer findSuccessor(String targetId) {
        if (successor == null || successorId == null) {
            System.err.println("[findSuccessor] No successor info. Returning self.");
            return new Peer(nodeModel.getListenIp(), nodeModel.getListenPort());
        }

        String myId = nodeModel.getChordId();

        System.out.println("Looking for successor of ID: " + targetId);
        System.out.println("My ID: " + myId + ", Successor ID: " + successorId);

        if (isBetween(myId, targetId, successorId)) {
            return successor;
        }

        Peer nextHop = closestPrecedingFinger(targetId);

        return Sender.sendFindSuccessorWithResponse(nextHop, targetId, nodeModel.getListenIp(), nodeModel.getListenPort());
    }

    /**
     * Checks whether the target lies between start and end (clockwise).
     *
     * @param start  Starting ID
     * @param target Target ID
     * @param end    Ending ID
     * @return True if the target is between start and end
     */
    public boolean isBetween(String start, String target, String end) {
        BigInteger s = new BigInteger(start, 16);
        BigInteger t = new BigInteger(target, 16);
        BigInteger e = new BigInteger(end, 16);

        if (s.compareTo(e) < 0) {
            return t.compareTo(s) > 0 && t.compareTo(e) <= 0;
        } else {
            return t.compareTo(s) > 0 || t.compareTo(e) <= 0;
        }
    }

    /**
     * Notifies the current successor that this node might be its predecessor.
     */
    public void notifySuccessor() {
        if (successor != null) {
            String localIp = nodeModel.getListenIp();
            int localPort = nodeModel.getListenPort();

            Message notifyMsg = new Message("NOTIFY", localIp, localPort, nodeModel.getChordId());
            Sender.sendMessage(successor.ip(), successor.port(), notifyMsg);
        }
    }

    /**
     * Adds a peer to the known peer set (excluding self)
     *
     * @param peer Peer to add
     */
    public void addPeer(Peer peer) {
        if (peer.port() != nodeModel.getListenPort()) {
            knownPeers.add(peer);
        }
    }

    /**
     * Sets a predecessor if the sender is a better candidate.
     * Called from NOTIFY handler.
     *
     * @param potentialPredecessor Peer who claims to be our predecessor
     * @param idOfPredecessor      ID of the sender
     */
    public void considerNewPredecessor(Peer potentialPredecessor, String idOfPredecessor) {
        if (predecessor != null && predecessor.ip().equals(potentialPredecessor.ip()) && predecessor.port() == potentialPredecessor.port()) {
            return;
        }

        if (predecessor == null || isBetween(predecessorId, idOfPredecessor, nodeModel.getChordId())) {
            this.predecessor = potentialPredecessor;
            this.predecessorId = idOfPredecessor;
            System.out.println("Updated predecessor to: " + potentialPredecessor);
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
     * Computes the Chord ID for a given peer.
     *
     * @param peer Peer object
     * @return Chord ID (SHA-1 hashed)
     */
    public String hashPeer(Peer peer) {
        String input = peer.ip() + ":" + peer.port();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(input.getBytes());
            return new BigInteger(1, hash).toString(16);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed: " + e.getMessage());
        }
    }

    /**
     * Sends all files from this node's uploads directory to the given peer.
     * Files are encoded as Base64 strings and sent via UPLOAD_TRANSFER messages.
     *
     * @param target Peer to receive the files.
     */
    public void sendAllFilesTo(Peer target) {
        String uploadPath = nodeModel.getWorkPath() + File.separator + "uploads";
        File uploadDir = new File(uploadPath);
        File[] files = uploadDir.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("No files to transfer.");
            return;
        }

        for (File file : files) {
            try {
                byte[] data = Files.readAllBytes(file.toPath());
                String encoded = Base64.getEncoder().encodeToString(data);
                String fileName = file.getName();

                String content = fileName + "::" + encoded;

                Message transfer = new Message("UPLOAD_TRANSFER", nodeModel.getListenIp(), nodeModel.getListenPort(), content);

                Sender.sendMessage(target.ip(), target.port(), transfer);
                System.out.println("Transferred file: " + fileName + " to " + target);

            } catch (IOException e) {
                System.err.println("Failed to send file: " + file.getName() + " → " + e.getMessage());
            }
        }
    }

    /**
     * Gracefully leaves the Chord ring by:
     * 1. Notifying a predecessor to update its successor reference.
     * 2. Notifying successor to update its predecessor reference.
     * 3. Transferring all owned files to the successor.
     * 4. Calling shutdown to stop the node.
     */
    public void leaveChordRing() {
        System.out.println("Leaving Chord ring...");

        if (predecessor != null && successor != null && !successor.equals(predecessor)) {
            Message updateSucc = new Message("UPDATE_SUCCESSOR", nodeModel.getListenIp(), nodeModel.getListenPort(), successor.ip() + ":" + successor.port());
            Sender.sendMessage(predecessor.ip(), predecessor.port(), updateSucc);
        }

        if (successor != null && predecessor != null && !successor.equals(predecessor)) {
            Message updatePred = new Message("UPDATE_PREDECESSOR", nodeModel.getListenIp(), nodeModel.getListenPort(), predecessor.ip() + ":" + predecessor.port());
            Sender.sendMessage(successor.ip(), successor.port(), updatePred);
        }

        sendAllFilesTo(successor);

        shutdown();
    }

    /**
     * Computes the SHA-1 hash of a given string.
     *
     * @param input The string to hash. Must be a valid UTF-8 string.
     * @return The SHA-1 hash of the input string as a hexadecimal string.
     */
    public String hashString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(input.getBytes());
            return new BigInteger(1, hash).toString(16);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed: " + e.getMessage());
        }
    }

    /**
     * Finds the closest preceding node in the finger table for the given ID.
     * Used in recursive/hop-by-hop routing to get closer to the target.
     *
     * @param targetId Target Chord ID we're routing toward
     * @return The closest known peer whose ID is between this node's ID and the target ID
     */
    public Peer closestPrecedingFinger(String targetId) {
        String myId = nodeModel.getChordId();

        for (int i = fingerTable.size() - 1; i >= 0; i--) {
            Peer finger = fingerTable.get(i);
            String fingerId = hashPeer(finger);

            if (isBetween(myId, fingerId, targetId)) {
                return finger;
            }
        }

        return successor;
    }

    /**
     * Routes a message hop-by-hop using the Chord ring toward the given hash.
     * Uses finger table and recursive routing (O(log N)).
     *
     * @param targetId Hashed ID of the destination (e.g., file name, IP:port, etc.)
     * @param msg      Message to send
     */
    public void forwardMessage(String targetId, Message msg) {
        Peer destination = findSuccessor(targetId);
        if (destination == null) {
            System.err.println("Cannot route message, no destination found for ID: " + targetId);
            return;
        }

        if (destination.port() == nodeModel.getListenPort()) {
            handleMessage(msg);
            return;
        }

        Sender.sendMessage(destination.ip(), destination.port(), msg);
    }

}