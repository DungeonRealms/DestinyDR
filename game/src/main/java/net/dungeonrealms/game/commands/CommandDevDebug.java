package net.dungeonrealms.game.commands;

import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.commands.BasicCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * Created by Alan on 7/22/2016.
 */
public class CommandDevDebug extends BasicCommand {

    public CommandDevDebug(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (!(s instanceof ConsoleCommandSender)) return false;

        Constants.debug ^= true;

        return true;
    }
}
