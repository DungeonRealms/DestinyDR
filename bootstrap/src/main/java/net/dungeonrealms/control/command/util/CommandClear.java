package net.dungeonrealms.control.command.util;

import net.dungeonrealms.control.command.NetworkCommand;
import net.dungeonrealms.control.utils.UtilLogger;

/**
 * Created by Evoltr on 11/30/2016.
 */
public class CommandClear extends NetworkCommand {

    public CommandClear() {
        super("clear", "Clear the screen.");
    }

    @Override
    public void onCommand(String[] args) {
        for (int i = 0; i < 80; i++) {
            System.out.println("");
        }
        UtilLogger.info("Type 'help' to see the list of commands.");
    }
}
