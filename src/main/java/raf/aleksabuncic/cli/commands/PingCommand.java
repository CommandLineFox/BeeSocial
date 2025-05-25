package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

public class PingCommand extends Command {
    public PingCommand(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String name() {
        return "ping";
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1 || !args[0].contains(":")) {
            System.out.println("Usage: ping <ip>:<port>");
            return;
        }

        String[] parts = args[0].split(":");
        String targetIp = parts[0];
        int targetPort = Integer.parseInt(parts[1]);

        String localIp = runtime.getNodeModel().getListenIp();
        int localPort = runtime.getNodeModel().getListenPort();

        Message ping = new Message("PING", localIp, localPort, "");
        Sender.sendMessage(targetIp, targetPort, ping);
    }
}