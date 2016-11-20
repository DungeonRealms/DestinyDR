package net.dungeonrealms.control.command;

import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.command.proxy.CommandProxyList;
import net.dungeonrealms.control.utils.UtilLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evoltr on 11/20/2016.
 */
public class CommandManager {

    private DRControl control;

    private List<NetworkCommand> commands = new ArrayList<>();

    public CommandManager(DRControl control) {
        this.control = control;

        registerCommand(new CommandProxyList());
    }

    public List<NetworkCommand> getCommands() {
        return commands;
    }

    public NetworkCommand getCommand(String name) {
        for (NetworkCommand command : getCommands()) {
            if (command.getName().equalsIgnoreCase(name)) {
                return command;
            }
        }
        return null;
    }

    public void registerCommand(NetworkCommand command) {
        commands.add(command);
    }

    // Really want to rewrite this some day, but for now this system will suffice.
    public void handle(String str) {

        // Create an array of arguments being passed by the console.
        String[] parts = str.split(" ");

        if (parts.length >= 1) {
            // Get the command being run by the console.
            NetworkCommand command = getCommand(parts[0]);

            if (command == null) {
                UtilLogger.warn("Unknown command. Type 'help' for a list of available commands.");
                return;
            }

            // Create the arguments to be passed to the command.
            List<String> args = new ArrayList<>();

            for (int x = 1; x < parts.length; x++) {
                args.add(parts[x]);
            }

            // Execute the command.
            command.onCommand(args.toArray(new String[args.size()]));
        }
    }

}
