package net.dungeonrealms.game.command.guild;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.guild.GuildMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class CommandGDemote extends BaseCommand {

    public CommandGDemote(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if (GuildDatabaseAPI.get().isGuildNull(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must be in a " + ChatColor.BOLD + "GUILD" + ChatColor.RED + " to use " + ChatColor.BOLD + "/gdemote <player>.");
            return true;
        }


        if (args.length == 0) {
            player.sendMessage(usage);
            return true;
        }


        String guildName = GuildDatabaseAPI.get().getGuildOf(player.getUniqueId());
        String displayName = GuildDatabaseAPI.get().getDisplayNameOf(guildName);

        if (!GuildDatabaseAPI.get().isOwner(player.getUniqueId(), guildName) && !Rank.isGM(player)) {
            player.sendMessage(ChatColor.RED + "You must be the " + ChatColor.BOLD + "GUILD OWNER" + ChatColor.RED + " to use " + ChatColor.BOLD + "/gdemote <player>.");
            return true;
        }

        String p_name = args[0];

        if (p_name.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.RED + "You cannot demote yourself in your own guild.");
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

        if (GuildDatabaseAPI.get().isMember(p_uuid, guildName)) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + p_name + ChatColor.RED + " is not yet a " + ChatColor.UNDERLINE + "guild officer");
            player.sendMessage(ChatColor.GRAY + "Use " + ChatColor.RED + "/gpromote " + p_name + ChatColor.GRAY + " to make them a guild officer.");
            return true;
        }

        if (GuildDatabaseAPI.get().isOwner(p_uuid, guildName)) {
            player.sendMessage(ChatColor.RED + "You can't demote the owner of a guild.");
            return true;
        }

        GuildDatabaseAPI.get().demotePlayer(guildName, p_uuid);
        GameAPI.updateGuildData(guildName);

        player.sendMessage(ChatColor.RED + "You have " + ChatColor.UNDERLINE + "demoted" + ChatColor.RED + " " + p_name + " to the rank of " + ChatColor.BOLD + "GUILD MEMBER.");
        GuildMechanics.getInstance().sendAlert(guildName, ChatColor.RED + " " + p_name + " has been " + ChatColor.UNDERLINE + "demoted" + ChatColor.RED + " to the rank of " + ChatColor.BOLD + "GUILD MEMBER.");

        if (p != null) {
            p.sendMessage("");
            p.sendMessage(ChatColor.RED + "You have been " + ChatColor.UNDERLINE + "demoted" + ChatColor.RED + " to the rank of " + ChatColor.BOLD + "GUILD MEMBER" + ChatColor.RED + " in " + displayName);
            p.sendMessage("");
        }

        return false;
    }
}
