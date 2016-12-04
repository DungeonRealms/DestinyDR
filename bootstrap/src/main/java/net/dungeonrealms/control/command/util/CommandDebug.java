package net.dungeonrealms.control.command.util;

import net.dungeonrealms.control.command.NetworkCommand;
import net.dungeonrealms.control.utils.UtilLogger;
import net.dungeonrealms.control.utils.UtilSlack;

/**
 * Created by Evoltr on 12/4/2016.
 */
public class CommandDebug extends NetworkCommand {

    public CommandDebug() {
        super("debug", "Debug something");
    }

    @Override
    public void onCommand(String[] args) {
        if (args.length == 0) {
            UtilSlack.networkStatus();
        }
    }
}
