package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.Peer;

/**
 * Accepts a follow request from a node and notifies that node via routed Chord communication.
 */
public class AcceptCommand extends Command {
    public AcceptCommand(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String name() {
        return "accept";
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: accept <ip:port>");
            return;
        }

        String[] parts = args[0].split(":");
        if (parts.length != 2) {
            System.out.println("Invalid format. Expected format: ip:port");
            return;
        }

        String ip = parts[0];
        int senderPort;
        try {
            senderPort = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number: " + parts[1]);
            return;
        }

        if (runtime.acceptFollow(senderPort)) {
            String localIp = runtime.getNodeModel().getListenIp();
            int localPort = runtime.getNodeModel().getListenPort();

            Peer targetPeer = new Peer(ip, senderPort);
            String targetId = runtime.hashPeer(targetPeer);

            Message msg = new Message("ACCEPT", localIp, localPort, localIp, localPort, "");
            runtime.forwardMessage(targetId, msg);

            System.out.println("Accepted request from Node " + ip + ":" + senderPort + " via Chord route.");
        } else {
            System.out.println("No pending request from Node " + senderPort);
        }
    }
}