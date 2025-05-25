package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;


public class FollowCommand extends Command {
    public FollowCommand(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String name() {
        return "follow";
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1 || !args[0].contains(":")) {
            System.out.println("Usage: follow <ip>:<port>");
            return;
        }

        String[] parts = args[0].split(":");
        String targetIp = parts[0];
        int targetPort;
        try {
            targetPort = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port.");
            return;
        }

        String localIp = runtime.getNodeModel().getListenIp();
        int localPort = runtime.getNodeModel().getListenPort();
        Message followRequest = new Message("FOLLOW_REQUEST", localIp, localPort, "");
        Sender.sendMessage(targetIp, targetPort, followRequest);

        System.out.println("Follow request sent to " + targetIp + ":" + targetPort);
    }
}