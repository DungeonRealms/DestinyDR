package net.dungeonrealms.control.command.util;

import net.dungeonrealms.control.command.NetworkCommand;

/**
 * Created by Evoltr on 12/4/2016.
 */
public class CommandEnd extends NetworkCommand {

    public CommandEnd() {
        super("end", "Shut down the server.");
    }

    @Override
    public void onCommand(String[] args) {
        getDRControl().shutdown();
    }

}
