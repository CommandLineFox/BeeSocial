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
        String[] addr = args[0].split(":");
        String ip = addr[0];
        int port = Integer.parseInt(addr[1]);

        String myIp = runtime.getNodeModel().getListenIp();
        int myPort = runtime.getNodeModel().getListenPort();
        Message msg = new Message("FOLLOW", myIp, myPort, "");
        Sender.sendMessage(ip, port, msg);
        System.out.println("Sent FOLLOW to " + ip + ":" + port);
    }
}