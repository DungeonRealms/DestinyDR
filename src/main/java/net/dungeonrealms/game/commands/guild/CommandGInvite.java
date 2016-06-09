package net.dungeonrealms.game.commands.guild;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class CommandGInvite extends BasicCommand {

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

        String guildName = GuildDatabaseAPI.get().getGuildOf(player.getUniqueId());
        String displayName = GuildDatabaseAPI.get().getDisplayNameOf(guildName);

        if (GuildDatabaseAPI.get().isOwner(player.getUniqueId(), guildName) && GuildDatabaseAPI.get().isOfficer(player.getUniqueId(), guildName)) {
            player.sendMessage(ChatColor.RED + "You must be at least a guild " + ChatColor.BOLD + "OFFICER" + ChatColor.RED + " to use " + ChatColor.BOLD + "/ginvite");
            return true;
        }

        String p_name = args[0];

        if (p_name.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.RED + "You cannot invite yourself to your own guild.");
            return true;
        }

        if (Bukkit.getPlayer(p_name) == null) {
            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " is OFFLINE");
            return true;
        }

        Player invitee = Bukkit.getPlayer(p_name);

        DBObject invitation = new BasicDBObject();

        invitation.put("guildName", guildName);
        invitation.put("referrer", sender.getName());
        invitation.put("time", System.currentTimeMillis());

        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.GUILD_INVITE, invitation, true);


        player.sendMessage(ChatColor.GRAY + "You have invited " + ChatColor.BOLD.toString() + ChatColor.DARK_AQUA + p_name + ChatColor.GRAY + " to join your guild.");

        invitee.sendMessage("");
        invitee.sendMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + player + ChatColor.GRAY + " has invited you to join their guild, " + ChatColor.DARK_AQUA + displayName + ChatColor.GRAY + ". To accept, type " + ChatColor.DARK_AQUA.toString() + "/gaccept" + ChatColor.GRAY + " to decline, type " + ChatColor.DARK_AQUA.toString() + "/gdecline");
        invitee.sendMessage("");
        return false;
    }
}
