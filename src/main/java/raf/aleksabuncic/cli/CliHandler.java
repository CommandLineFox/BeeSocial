package raf.aleksabuncic.cli;

import raf.aleksabuncic.cli.command.Command;
import raf.aleksabuncic.cli.command.CommandRegistry;
import raf.aleksabuncic.core.runtime.NodeRuntime;

import java.util.Scanner;

/**
 * Handles the command line interface (CLI) and handles user input.
 */
public class CliHandler implements Runnable {
    private final NodeRuntime runtime;
    private final CommandRegistry registry;

    public CliHandler(NodeRuntime runtime) {
        this.runtime = runtime;
        this.registry = new CommandRegistry(runtime);
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (runtime.isRunning()) {
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

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

        System.out.println("CLI shut down.");
    }
}