package raf.aleksabuncic.cli.command;

import raf.aleksabuncic.cli.commands.*;
import raf.aleksabuncic.core.runtime.NodeRuntime;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();

    public CommandRegistry(NodeRuntime runtime) {
        register(new AcceptCommand(runtime));
        register(new FollowCommand(runtime));
        register(new ListFilesCommand(runtime));
        register(new RemoveFileCommand(runtime));
        register(new StopCommand(runtime));
        register(new UploadCommand(runtime));
        register(new VisibilityCommand(runtime));
        register(new HelpCommand(commands));
    }

    private void register(Command command) {
        commands.put(command.name(), command);
    }

    public Command get(String name) {
        return commands.get(name);
    }
}
