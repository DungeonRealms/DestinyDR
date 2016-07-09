package net.dungeonrealms.game.commands.guild;

import net.dungeonrealms.API;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class CommandGKick extends BasicCommand {

    public CommandGKick(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if (GuildDatabaseAPI.get().isGuildNull(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must be in a " + ChatColor.BOLD + "GUILD" + ChatColor.RED + " to use " + ChatColor.BOLD + "/gkick.");
            return true;
        }


        if (args.length == 0) {
            player.sendMessage(usage);
            return true;
        }


        String guildName = GuildDatabaseAPI.get().getGuildOf(player.getUniqueId());
        String displayName = GuildDatabaseAPI.get().getDisplayNameOf(guildName);

        if (!GuildDatabaseAPI.get().isOwner(player.getUniqueId(), guildName) && !GuildDatabaseAPI.get().isOfficer(player.getUniqueId(), guildName)) {
            player.sendMessage(ChatColor.RED + "You must be at least a guild " + ChatColor.BOLD + "OFFICER" + ChatColor.RED + " to use " + ChatColor.BOLD + "/gkick");
            return true;
        }

        String p_name = args[0];

        if (p_name.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.RED + "You cannot kick yourself to your own guild.");
            return true;
        }

        if (DatabaseAPI.getInstance().getUUIDFromName(args[0]).equals("")) {
            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " does not exist in our database.");
            return true;
        }

        Player p = Bukkit.getPlayer(p_name);
        UUID p_uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(args[0]));

        if (!GuildDatabaseAPI.get().getGuildOf(p_uuid).equals(guildName)) {
            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " is not in your guild.");
            return true;
        }
        if (GuildDatabaseAPI.get().isOwner(p_uuid, guildName)) {
            player.sendMessage(ChatColor.RED + "You can't kick the owner of a guild.");
            return true;
        }

        if (GuildDatabaseAPI.get().isOfficer(p_uuid, guildName)) {
            player.sendMessage(ChatColor.RED + "You can't kick an officers.");
            return true;
        }

        GuildMechanics.getInstance().kickFromGuild(player, p_uuid, guildName);
        API.updateGuildData(guildName);

        if (p != null) {
            p.sendMessage("");
            p.sendMessage(ChatColor.RED + "You have been " + ChatColor.UNDERLINE + "kicked" + ChatColor.RED + " from " + displayName);
            p.sendMessage("");
        }

        return false;
    }
}
