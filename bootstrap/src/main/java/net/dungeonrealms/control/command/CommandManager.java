package net.dungeonrealms.control.command;

import net.dungeonrealms.control.DRControl;

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

}
