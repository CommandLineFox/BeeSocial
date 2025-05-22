package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

public class DownloadCommand extends Command {
    public DownloadCommand(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String name() {
        return "download";
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 2 || !args[0].contains(":")) {
            System.out.println("Usage: download <ip>:<port> <filename>");
            return;
        }

        String[] addr = args[0].split(":");
        String ip = addr[0];
        int port = Integer.parseInt(addr[1]);
        String filename = args[1];

        Message msg = new Message("DOWNLOAD_REQUEST", runtime.getNodeModel().getListenPort(), filename);
        Sender.sendMessage(ip, port, msg);
        System.out.println("Download request sent to " + ip + ":" + port + " for file: " + filename);
    }
}