package raf.aleksabuncic.bootstrap;

import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BootstrapServer implements Runnable {

    private final int port;
    private final Set<Peer> registeredNodes = Collections.synchronizedSet(new HashSet<>());

    public BootstrapServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        System.out.println("Bootstrap server started on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket client = serverSocket.accept();
                new Thread(() -> handleConnection(client)).start();
            }
        } catch (Exception e) {
            System.err.println("Bootstrap server error: " + e.getMessage());
        }
    }

    /**
     * Handles a single connection.
     *
     * @param socket Socket to handle
     */
    private void handleConnection(Socket socket) {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            Message msg = (Message) in.readObject();

            if ("REGISTER_REQUEST".equals(msg.type())) {
                String senderIp = socket.getInetAddress().getHostAddress();
                int senderPort = Integer.parseInt(msg.content());

                Peer newNode = new Peer(senderIp, senderPort);
                registeredNodes.add(newNode);

                System.out.println("Registered node: " + newNode);

                String responseContent = registeredNodes.stream().filter(p -> !p.equals(newNode)).findAny().map(Peer::toString).orElse("");
                Message response = new Message("REGISTER_RESPONSE", senderIp, port, senderIp, port, responseContent);
                out.writeObject(response);
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("Error handling bootstrap connection: " + e.getMessage());
        }
    }
}