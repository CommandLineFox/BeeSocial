package raf.aleksabuncic.core;

import raf.aleksabuncic.cli.CliHandler;
import raf.aleksabuncic.config.ConfigHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Node;

public class Starter {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar BeeSocial.jar <nodeId>");
            return;
        }

        int nodeId;
        try {
            nodeId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid node ID: " + args[0]);
            return;
        }

        Node node = ConfigHandler.loadNodeById("config.json", nodeId);
        if (node == null) {
            System.out.println("Node with ID " + nodeId + " not found in config.");
            return;
        }

        System.out.println("Node started on port " + node.getListenPort());

        NodeRuntime runtime = new NodeRuntime(node);
        runtime.start();

        new Thread(new CliHandler(runtime)).start();
    }
}