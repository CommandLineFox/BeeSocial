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
            System.out.println("Usage: accept <senderPort>");
            return;
        }

        int senderPort = Integer.parseInt(args[0]);
        if (runtime.acceptFollow(senderPort)) {
            String localIp = runtime.getNodeModel().getListenIp();
            int localPort = runtime.getNodeModel().getListenPort();

            Peer targetPeer = new Peer("127.0.0.1", senderPort);
            String targetId = runtime.hashPeer(targetPeer);

            Message msg = new Message("ACCEPT", localIp, localPort, "");
            runtime.forwardMessage(targetId, msg);

            System.out.println("Accepted request from Node on port " + senderPort + " via Chord route.");
        } else {
            System.out.println("No pending request from Node " + senderPort);
        }
    }
}