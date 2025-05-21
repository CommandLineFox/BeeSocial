package raf.aleksabuncic.cli;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.cli.command.CommandRegistry;
import raf.aleksabuncic.core.runtime.NodeRuntime;

import java.util.Scanner;

public class CliHandler implements Runnable {
    private final CommandRegistry registry;

    public CliHandler(NodeRuntime runtime) {
        this.registry = new CommandRegistry(runtime);
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] tokens = line.split("\\s+");
            String cmdName = tokens[0];
            String[] args = new String[tokens.length - 1];
            System.arraycopy(tokens, 1, args, 0, args.length);

            Command command = registry.get(cmdName);
            if (command != null) {
                command.execute(args);
            } else {
                System.out.println("Unknown command: " + cmdName);
            }
        }
    }
}