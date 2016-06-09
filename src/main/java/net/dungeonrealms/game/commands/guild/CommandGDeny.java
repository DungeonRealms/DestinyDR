package net.dungeonrealms.game.commands.guild;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class CommandGDeny extends BasicCommand {

    public CommandGDeny(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        return false;
    }

}