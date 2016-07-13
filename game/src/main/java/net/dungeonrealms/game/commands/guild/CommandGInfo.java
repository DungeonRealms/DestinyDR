package net.dungeonrealms.game.commands.guild;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.guild.GuildMechanics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

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
        UUID target = player.getUniqueId();


        if (args.length > 0) {
            if (DatabaseAPI.getInstance().getUUIDFromName(args[0]).equals("")) {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + args[0] + ChatColor.RED + " does not exist in our database.");
                return true;
            }

            UUID uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(args[0]));

            if (GuildDatabaseAPI.get().isGuildNull(uuid)) {
                player.sendMessage(ChatColor.RED + args[0] + " is not in a guild.");
                return true;
            }

            target = uuid;
        } else {
            if (GuildDatabaseAPI.get().isGuildNull(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You must be in a " + ChatColor.BOLD + "GUILD" + ChatColor.RED + " to use " + ChatColor.BOLD + "GUILD INFO.");
                return true;
            }
        }

        String guildName = GuildDatabaseAPI.get().getGuildOf(target);
        GuildMechanics.getInstance().showGuildInfo(player, guildName, target.equals(player.getUniqueId()));
        return false;
    }
}
