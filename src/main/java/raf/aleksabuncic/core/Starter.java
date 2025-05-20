package raf.aleksabuncic.core;

import raf.aleksabuncic.cli.CliHandler;
import raf.aleksabuncic.config.ConfigHandler;
import raf.aleksabuncic.types.Node;

import java.util.Scanner;

public class Starter {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter node ID to start: ");
        int nodeId = scanner.nextInt();
        scanner.nextLine();

        Node node = ConfigHandler.loadNodeById("config.json", nodeId);
        if (node == null) {
            System.out.println("Node with ID " + nodeId + " not found.");
            return;
        }

        System.out.println("Node started on port " + node.getListenPort());

        new Thread(new CliHandler(node)).start();
    }
}
