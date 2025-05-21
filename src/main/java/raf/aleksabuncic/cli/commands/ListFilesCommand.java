package raf.aleksabuncic.cli.commands;
import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.util.FileUtils;

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
        int myId = runtime.getNodeModel().getListenPort();
        if (args.length == 0) {
            var files = FileUtils.listFilesInDirectory(runtime.getNodeModel().getImagePath());
            if (files.isEmpty()) {
                System.out.println("No files found.");
            } else {
                System.out.println("Files in working root:");
                for (String f : files) {
                    System.out.println(" - " + f);
                }
            }
        } else if (args.length == 1 && args[0].contains(":")) {
            String[] addr = args[0].split(":");
            String ip = addr[0];
            int port = Integer.parseInt(addr[1]);
            Message msg = new Message("LIST_FILES", myId, "");
            Sender.sendMessage(ip, port, msg);
            System.out.println("Sent LIST_FILES request to " + ip + ":" + port);
        } else {
            System.out.println("Usage: list_files [<ip>:<port>]");
        }
    }
}
