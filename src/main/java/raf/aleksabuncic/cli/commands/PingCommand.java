package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

/**
 * Sends a PING message to a target node.
 */
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
        if (args.length != 1) {
            System.out.println("Usage: ping <target_id>");
            return;
        }

        String targetId = args[0];
        String localIp = runtime.getNodeModel().getListenIp();
        int localPort = runtime.getNodeModel().getListenPort();

        Message ping = new Message("PING", localIp, localPort, localIp, localPort, "");
        runtime.forwardMessage(targetId, ping);
    }
}