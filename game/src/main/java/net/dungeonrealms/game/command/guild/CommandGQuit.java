package net.dungeonrealms.game.command.guild;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.guild.GuildMechanics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/6/2016
 */


public class CommandGQuit extends BaseCommand {

    public CommandGQuit(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;
        
        if (GuildDatabaseAPI.get().isGuildNull(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must be in a " + ChatColor.BOLD + "GUILD" + ChatColor.RED + " to quit.");
            return true;
        }


        GuildMechanics.getInstance().leaveGuild(player);
        return false;
    }
}
