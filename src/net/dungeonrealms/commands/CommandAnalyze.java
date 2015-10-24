package net.dungeonrealms.commands;

import net.dungeonrealms.commands.generic.BasicCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Created by Nick on 9/17/2015.
 */
public class CommandAnalyze extends BasicCommand {

    public CommandAnalyze(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage("FAGGOT");
        return false;
    }
}
