package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

public class BackupCommand extends Command {
    public BackupCommand(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String name() {
        return "backup";
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1 || !args[0].contains(":")) {
            System.out.println("Usage: backup <ip>:<port>");
            return;
        }

        String[] parts = args[0].split(":");
        String ip = parts[0];
        int port;
        try {
            port = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port.");
            return;
        }

        String myIp = runtime.getNodeModel().getListenIp();
        int myPort = runtime.getNodeModel().getListenPort();

        Message req = new Message("BACKUP_REQUEST", myIp, myPort, "");
        Sender.sendMessage(ip, port, req);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        if (runtime.hasRecentBackupResponse(port)) {
            runtime.setBuddy(ip, port);
            System.out.println("Buddy confirmed and set to " + ip + ":" + port);
        } else {
            System.out.println("Backup node " + ip + ":" + port + " not reachable.");
        }
    }
}