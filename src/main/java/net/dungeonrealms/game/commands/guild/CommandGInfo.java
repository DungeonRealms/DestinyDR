package net.dungeonrealms.game.commands.guild;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.guild.GuildMechanics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */


public class CommandGInfo extends BasicCommand {

    public CommandGInfo(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if (GuildDatabaseAPI.get().isGuildNull(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must be in a " + ChatColor.BOLD + "GUILD" + ChatColor.RED + " to use " + ChatColor.BOLD + "GUILD INFO.");
            return true;
        }

        String guildName = GuildDatabaseAPI.get().getGuildOf(player.getUniqueId());

        GuildMechanics.getInstance().showGuildInfo(player, guildName);
        return false;
    }
}
