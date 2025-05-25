package raf.aleksabuncic.core.runtime;

import lombok.Getter;
import lombok.Setter;
import raf.aleksabuncic.core.failure.FailureDetector;
import raf.aleksabuncic.core.net.ConnectionHandler;
import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.response.ResponseRegistry;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Node;
import raf.aleksabuncic.types.Peer;

import java.math.BigInteger;
import java.security.MessageDigest;
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
    private String successorId;

    /**
     * Chord predecessor node
     */
    @Setter
    private Peer predecessor;

    /**
     * Chord ID of the current predecessor
     */
    private String predecessorId;

    public NodeRuntime(Node nodeModel) {
        this.nodeModel = nodeModel;
    }

    /**
     * Starts the node, and its ability to listen to incoming messages
     */
    public void start() {
        new Thread(new ConnectionHandler(this, nodeModel.getListenPort())).start();
        new Thread(failureDetector).start();
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
            String myPort = String.valueOf(nodeModel.getListenPort());

            Message request = new Message("REGISTER_REQUEST", myIp, Integer.parseInt(myPort), myPort);
            Message response = Sender.sendMessageWithResponse(bootstrapIp, bootstrapPort, request);

            if (response != null && "REGISTER_RESPONSE".equals(response.type())) {
                handleMessage(response);
                System.out.println("Successfully registered with bootstrap server.");

                for (Peer peer : knownPeers) {
                    Peer succ = Sender.sendFindSuccessor(peer, nodeModel.getChordId());
                    if (succ != null) {
                        this.successor = succ;
                        this.successorId = hashPeer(succ);
                        System.out.println("Set successor to: " + succ);
                        break;
                    }
                }

                if (successor == null) {
                    this.successor = new Peer("127.0.0.1", nodeModel.getListenPort());
                    this.successorId = nodeModel.getChordId();
                    System.out.println("No peers found. Acting as own successor.");
                } else {
                    notifySuccessor();
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
        String myId = nodeModel.getChordId();

        if (successor == null || successorId == null || isBetween(myId, targetId, successorId)) {
            return successor != null ? successor : new Peer("127.0.0.1", nodeModel.getListenPort());
        } else {
            return Sender.sendFindSuccessor(successor, targetId);
        }
    }

    /**
     * Checks whether the target lies between start and end (clockwise).
     *
     * @param start  Starting ID
     * @param target Target ID
     * @param end    Ending ID
     * @return True if the target is between start and end
     */
    private boolean isBetween(String start, String target, String end) {
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
            Message notifyMsg = new Message("NOTIFY", nodeModel.getListenPort(), nodeModel.getChordId());
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

    /**
     * Computes the Chord ID for a given peer.
     *
     * @param peer Peer object
     * @return Chord ID (SHA-1 hashed)
     */
    private String hashPeer(Peer peer) {
        String input = peer.ip() + ":" + peer.port();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(input.getBytes());
            return new BigInteger(1, hash).toString(16);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed: " + e.getMessage());
        }
    }
}