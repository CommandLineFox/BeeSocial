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

        String[] addr = args[0].split(":");
        String ip = addr[0];
        int port = Integer.parseInt(addr[1]);

        int senderId = runtime.getNodeModel().getListenPort();
        Message ping = new Message("PING", senderId, "");
        Sender.sendMessage(ip, port, ping);

        System.out.println("Sent PING to " + ip + ":" + port);
    }
}