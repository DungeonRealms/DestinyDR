package net.dungeonrealms.control.command.util;

import net.dungeonrealms.control.command.NetworkCommand;
import net.dungeonrealms.control.utils.UtilLogger;

import java.util.List;

/**
 * Created by Evoltr on 11/30/2016.
 */
public class CommandHelp extends NetworkCommand {

    public CommandHelp() {
        super("help", "Display a list of available commands.");
    }

    @Override
    public void onCommand(String[] args) {

        //Get a list of commands to display.
        List<NetworkCommand> commands = getDRControl().getCommandManager().getCommands();

        //Display a header.
        UtilLogger.info("------------------- Commands -------------------");

        //Loop through commands and display their info.
        for (int x = 0; x < commands.size(); x++) {

            NetworkCommand cmd = commands.get(x);

            UtilLogger.info((x + 1) + ". " + cmd.getName() + " - " + cmd.getDesc());
        }

    }
}
