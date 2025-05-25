package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

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
            System.out.println("Usage: accept <senderId>");
            return;
        }
        int senderId = Integer.parseInt(args[0]);
        if (runtime.acceptFollow(senderId)) {
            String myIp = runtime.getNodeModel().getListenIp();
            int myPort = runtime.getNodeModel().getListenPort();

            Message msg = new Message("ACCEPT", myIp, myPort, "");
            Sender.sendMessage("127.0.0.1", senderId, msg);
            System.out.println("Accepted request from Node " + senderId);
        } else {
            System.out.println("No pending request from Node " + senderId);
        }
    }
}