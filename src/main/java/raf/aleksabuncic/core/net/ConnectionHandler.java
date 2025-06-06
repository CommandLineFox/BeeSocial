package raf.aleksabuncic.core.net;

import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Handles incoming connections.
 */
public class ConnectionHandler implements Runnable {
    private final NodeRuntime node;
    private final int port;

    public ConnectionHandler(NodeRuntime node, int port) {
        this.node = node;
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Node " + port + "] Listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handle(socket)).start();
            }
        } catch (Exception e) {
            System.err.println("[Node " + port + "] Server error:");
        }
    }

    /**
     * Handles a single connection.
     *
     * @param socket Socket to handle.
     */
    private void handle(Socket socket) {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            Message msg = (Message) in.readObject();
            node.handleMessage(msg);
        } catch (Exception e) {
            System.out.println("Error during handling message.");
        }
    }
}