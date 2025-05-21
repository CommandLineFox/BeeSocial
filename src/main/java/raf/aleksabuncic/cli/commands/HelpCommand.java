package raf.aleksabuncic.cli.commands;

import raf.aleksabuncic.cli.command.Command;

import java.util.Map;

public class HelpCommand extends Command {

    private final Map<String, Command> commandMap;

    public HelpCommand(Map<String, Command> commandMap) {
        super(null);
        this.commandMap = commandMap;
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public void execute(String[] args) {
        System.out.println("Available commands:");
        for (String cmd : commandMap.keySet()) {
            System.out.println(" - " + cmd);
        }
    }
}
