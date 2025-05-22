package raf.aleksabuncic.core.runtime;

import lombok.Getter;
import raf.aleksabuncic.core.failure.FailureDetector;
import raf.aleksabuncic.core.net.ConnectionHandler;
import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.response.ResponseRegistry;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Node;
import raf.aleksabuncic.types.Peer;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class NodeRuntime {
    private final Node nodeModel;
    private final Set<Integer> followers;
    private final Set<Integer> pendingRequests;

    private volatile boolean running;

    private final ResponseRegistry responseRegistry;
    private final Set<Integer> recentBackupResponses;

    /**
     * Failure detector
     */
    private final FailureDetector failureDetector;

    /**
     * Known nodes to communicate with
     */
    private final Set<Peer> knownPeers = ConcurrentHashMap.newKeySet();

    /**
     * Chord successor node
     */
    private Peer successor;

    /**
     * Chord ID of the current successor
     */
    private String successorId;

    /**
     * Chord predecessor node (not yet used)
     */
    private Peer predecessor;

    public NodeRuntime(Node nodeModel) {
        this.nodeModel = nodeModel;
        this.followers = new HashSet<>();
        this.pendingRequests = ConcurrentHashMap.newKeySet();
        this.running = true;
        this.responseRegistry = new ResponseRegistry(this);
        this.recentBackupResponses = ConcurrentHashMap.newKeySet();
        this.failureDetector = new FailureDetector(this);
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
     * Register with the bootstrap server and attempt to find successor in the ring
     */
    public void registerWithBootstrap() {
        try {
            String bootstrapIp = nodeModel.getBootstrapIp();
            int bootstrapPort = nodeModel.getBootstrapPort();
            String myPort = String.valueOf(nodeModel.getListenPort());

            Message request = new Message("REGISTER_REQUEST", nodeModel.getListenPort(), myPort);
            Message response = Sender.sendMessageWithResponse(bootstrapIp, bootstrapPort, request);

            if (response != null && "REGISTER_RESPONSE".equals(response.type())) {
                handleMessage(response);
                System.out.println("Successfully registered with bootstrap server.");

                for (Peer peer : knownPeers) {
                    Peer succ = Sender.sendFindSuccessor(peer, nodeModel.getChordId());
                    if (succ != null) {
                        this.successor = succ;
                        this.successorId = nodeModel.getChordId();
                        System.out.println("Set successor to: " + succ);
                        break;
                    }
                }

                if (successor == null) {
                    this.successor = new Peer("127.0.0.1", nodeModel.getListenPort());
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
     * Shuts down the node gracefully.
     */
    public void shutdown() {
        System.out.println("Node shutting down gracefully...");
        running = false;
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
     * Checks whether target lies between start and end (clockwise).
     *
     * @param start  Starting ID
     * @param target Target ID
     * @param end    Ending ID
     * @return True if target is between start and end
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
     * Adds a peer to the known peer set (excluding self)
     *
     * @param peer Peer to add
     */
    public void addPeer(Peer peer) {
        if (peer.port() != nodeModel.getListenPort()) {
            knownPeers.add(peer);
        }
    }
}