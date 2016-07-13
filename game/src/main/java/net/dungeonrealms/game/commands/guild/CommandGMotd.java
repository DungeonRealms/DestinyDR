package net.dungeonrealms.game.commands.guild;

import net.dungeonrealms.GameAPI;
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

public class CommandGMotd extends BasicCommand {

    public CommandGMotd(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if (GuildDatabaseAPI.get().isGuildNull(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must be in a " + ChatColor.BOLD + "GUILD" + ChatColor.RED + " to view " + ChatColor.BOLD + "/gmotd.");
            return true;
        }
        
        String guildName = GuildDatabaseAPI.get().getGuildOf(player.getUniqueId());

        if (args.length == 0) {
            GuildMechanics.getInstance().showMotd(player, guildName);
            return true;
        }

        if (args.length >= 1) {
            if (!GuildDatabaseAPI.get().isOwner(player.getUniqueId(), guildName)) {
                player.sendMessage(ChatColor.RED + "You must be the " + ChatColor.BOLD + "GUILD OWNER" + ChatColor.RED + " to use " + ChatColor.BOLD + "/gmotd <motd>.");
                return true;
            }

            StringBuilder newMotd = new StringBuilder(args[0]);
            for (int arg = 1; arg < args.length; arg++) newMotd.append(" ").append(args[arg]);

            if (newMotd.toString().contains("$")) {
                player.sendMessage(ChatColor.RED + "MOTD contains illegal character '$'.");
                return true;
            }

            GuildDatabaseAPI.get().setMotdOf(guildName, newMotd.toString());
            GameAPI.updateGuildData(guildName);

            player.sendMessage(ChatColor.GRAY + "You have updated the guild " + ChatColor.BOLD.toString() + ChatColor.DARK_AQUA + "MOTD" + ChatColor.GRAY + " to:");
            GuildMechanics.getInstance().showMotd(player, guildName);
            return true;
        }
        return false;
    }
}
