package raf.aleksabuncic.core.net;

import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Sender {
    /**
     * Sends a message to a remote node.
     *
     * @param host    Remote node hostname or IP address.
     * @param port    Remote node port.
     * @param message Message to send.
     */
    public static void sendMessage(String host, int port, Message message) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.writeObject(message);
            out.flush();
        } catch (Exception e) {
            System.err.println("Failed to send message to " + host + ":" + port);
        }
    }

    /**
     * Sends a message to a remote node and waits for a response.
     *
     * @param host    Remote node hostname or IP address.
     * @param port    Remote node port.
     * @param message Message to send.
     * @return Response message or null if failed to send or receive a response.
     */
    public static Message sendMessageWithResponse(String host, int port, Message message) {
        try (Socket socket = new Socket(host, port)) {
            System.out.println("[SENDER] Connecting to " + host + ":" + port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            System.out.println("[SENDER] Sending message type: " + message.type());
            out.writeObject(message);
            out.flush();

            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            Message response = (Message) in.readObject();
            System.out.println("[SENDER] Received response: " + (response != null ? response.type() : "null"));

            return response;
        } catch (Exception e) {
            System.err.println("[SENDER] Failed to send message to " + host + ":" + port + " → " + e.getMessage());
            return null;
        }
    }

    /**
     * Sends a FIND_SUCCESSOR message and waits for a response.
     *
     * @param targetPeer Target peer to ask.
     * @param targetId   Chord ID for which to find the successor.
     * @param senderIp   IP of the sender node.
     * @param senderPort Port of the sender node.
     * @return Successor Peer or null.
     */
    public static void sendFindSuccessor(Peer targetPeer, String targetId, String senderIp, int senderPort) {
        Message request = new Message("FIND_SUCCESSOR", senderIp, senderPort, targetId);
        sendMessage(targetPeer.ip(), targetPeer.port(), request);
    }
}