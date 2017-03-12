package net.dungeonrealms.game.command;

import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.command.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * Created by Alan on 7/22/2016.
 */
public class CommandDevDebug extends BaseCommand {

    public CommandDevDebug() {
        super("devdebug", "/<command>", "Toggle on and off debug.");
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (!(s instanceof ConsoleCommandSender)) return false;

        Constants.debug = !Constants.debug;

        return true;
    }
}
