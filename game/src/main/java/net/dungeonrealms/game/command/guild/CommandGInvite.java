package net.dungeonrealms.game.command.guild;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.guild.GuildMechanics;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class CommandGInvite extends BaseCommand {

    public CommandGInvite(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if (GuildDatabaseAPI.get().isGuildNull(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must be in a " + ChatColor.BOLD + "GUILD" + ChatColor.RED + " to use " + ChatColor.BOLD + "/ginvite.");
            return true;
        }


        if (args.length == 0) {
            player.sendMessage(usage);
            return true;
        }


        String guildName = GuildDatabaseAPI.get().getGuildOf(player.getUniqueId());
        String displayName = GuildDatabaseAPI.get().getDisplayNameOf(guildName);

        if (!GuildDatabaseAPI.get().isOwner(player.getUniqueId(), guildName) && !GuildDatabaseAPI.get().isOfficer(player.getUniqueId(), guildName)) {
            player.sendMessage(ChatColor.RED + "You must be at least a guild " + ChatColor.BOLD + "OFFICER" + ChatColor.RED + " to use " + ChatColor.BOLD + "/ginvite");
            return true;
        }

        String p_name = args[0];

        if (p_name.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.RED + "You cannot invite yourself to your own guild.");
            return true;
        }

        if (DatabaseAPI.getInstance().getUUIDFromName(args[0]).equals("")) {
            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " does not exist in our database.");
            return true;
        }

        Player p = Bukkit.getPlayer(p_name);
        UUID p_uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(args[0]));

        if (DatabaseAPI.getInstance().getData(EnumData.GUILD_INVITATION, p_uuid) != null) {
            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " already has a pending guild invitation.");
            return true;
        }

        GuildMechanics.getInstance().checkPlayerGuild(p_uuid);

        if (!GuildDatabaseAPI.get().isGuildNull(p_uuid)) {
            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " is already in a guild.");
            return true;
        }

        Document invitation = new Document();

        invitation.put("guild", guildName);
        invitation.put("referrer", sender.getName());
        invitation.put("time", System.currentTimeMillis());


        DatabaseAPI.getInstance().update(p_uuid, EnumOperators.$SET, EnumData.GUILD_INVITATION, invitation, true, doAfter -> {
            player.sendMessage(ChatColor.GRAY + "You have invited " + ChatColor.BOLD.toString() +
                    ChatColor.DARK_AQUA + p_name + ChatColor.GRAY + " to join your guild.");

            BungeeUtils.sendPlayerMessage(p_name, "");
            BungeeUtils.sendPlayerMessage(p_name, ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + player.getName() + ChatColor.GRAY + " has invited you to join their guild, " + ChatColor.DARK_AQUA + displayName + ChatColor.GRAY + ". To accept, type " + ChatColor.DARK_AQUA.toString() + "/gaccept" + ChatColor.GRAY + " to decline, type " + ChatColor.DARK_AQUA.toString() + "/gdecline");
            BungeeUtils.sendPlayerMessage(p_name, "");
            GameAPI.updatePlayerData(p_uuid);
        });

        return false;
    }
}
