package raf.aleksabuncic.cli;

import raf.aleksabuncic.types.Node;

import java.util.Scanner;

public class CliHandler implements Runnable {
    private final Node node;

    public CliHandler(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String[] tokens = line.split("\\s+");

            if (tokens.length == 0) continue;

            switch (tokens[0]) {
                case "stop":
                    System.out.println("Shutdown not implemented yet");
                    return;

                case "visibility":
                    System.out.println("Current visibility: " + node.getVisibility());
                    break;

                case "follow":
                    System.out.println("Follow not implemented yet.");
                    break;

                default:
                    System.out.println("Unknown command: " + tokens[0]);
            }
        }
    }
}