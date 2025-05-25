package raf.aleksabuncic.types;

import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Node {
    /**
     * ID used for finding nodes efficiently
     */
    private final String chordId;
    /**
     * Listening IP for incoming messages.
     */
    private final String listenIp;
    /**
     * Port to listen on for messages
     */
    private final short listenPort;
    /**
     * IP of the bootstrap server
     */
    private final String bootstrapIp;
    /**
     * Port of the bootstrap server
     */
    private final short bootstrapPort;
    /**
     * Absolute path to the work path that will be used for all actions
     */
    private final String workPath;
    /**
     * Weak threshold after which a node is considered critical
     */
    private final int weakThreshold;
    /**
     * Strong threshold after which a node is considered down
     */
    private final int strongThreshold;
    /**
     * List of a node's followers that can see private posts
     */
    private final List<String> followers;
    @Setter
    private NodeVisibility visibility;

    public Node(short listenPort, String bootstrapIp, short bootstrapPort, String workPath, int weakThreshold, int strongThreshold) {
        this.chordId = generateId("127.0.0.1", listenPort);
        this.listenIp = "127.0.0.1";
        this.listenPort = listenPort;
        this.bootstrapIp = bootstrapIp;
        this.bootstrapPort = bootstrapPort;
        this.workPath = workPath;
        this.weakThreshold = weakThreshold;
        this.strongThreshold = strongThreshold;
        this.followers = new ArrayList<>();
        this.visibility = NodeVisibility.PUBLIC;
    }

    /**
     * Generates a chord ID for a node from a given IP and port
     *
     * @param ip   IP of the node
     * @param port Port the node listens on
     * @return Chord ID as a string
     */
    private String generateId(String ip, int port) {
        try {
            String input = ip + ":" + port;
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, hash);
            return number.toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 not available", e);
        }
    }
}
