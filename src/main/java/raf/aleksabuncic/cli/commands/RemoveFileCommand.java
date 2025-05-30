package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

/**
 * Sends a delete request to a responsible node in the Chord ring.
 */
public class RemoveFileCommand extends Command {
    public RemoveFileCommand(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String name() {
        return "remove_file";
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: remove_file <filename>");
            return;
        }

        String fileName = args[0];
        String hash = runtime.hashString(fileName);

        Message deleteMsg = new Message("DELETE_FILE", runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), fileName);

        runtime.forwardMessage(hash, deleteMsg);
        System.out.println("Forwarded delete request for '" + fileName + "' to node responsible for hash " + hash);
    }
}