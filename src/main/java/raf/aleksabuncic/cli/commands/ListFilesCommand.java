package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.util.Utils;

import java.io.File;

/**
 * Lists all files stored on this node. Optionally, can send a request to a target node to list files stored there.
 */
public class ListFilesCommand extends Command {
    public ListFilesCommand(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String name() {
        return "list_files";
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            var files = Utils.listFilesInDirectory(runtime.getNodeModel().getWorkPath() + File.separator + "uploads");
            if (files.isEmpty()) {
                System.out.println("No local files.");
            } else {
                System.out.println("Files stored on this node:");
                for (String f : files) {
                    System.out.println(" - " + f);
                }
            }
            return;
        }

        if (args.length != 1) {
            System.out.println("Usage: list_files [<target_id>]");
            return;
        }

        String targetId = args[0];
        String localIp = runtime.getNodeModel().getListenIp();
        int localPort = runtime.getNodeModel().getListenPort();

        Message msg = new Message("LIST_FILES", localIp, localPort, localIp, localPort, "");
        runtime.forwardMessage(targetId, msg);
    }
}