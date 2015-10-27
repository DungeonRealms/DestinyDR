package net.dungeonrealms.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.network.NetworkAPI;
import net.dungeonrealms.party.Party;

/**
 * Created by Nick on 10/15/2015.
 */
public class CommandAccept extends BasicCommand {

    public CommandAccept(String command, String usage, String description) {
        super(command, usage, description);
    }

    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;

        if (args.length > 0) {
            switch (args[0]) {
                case "guild":
                    assert args[1] != null : "arg[1] is null!";
                    if (!Guild.getInstance().isGuildNull(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + "You must leave your current guild to accept invitations to others!");
                        return true;
                    }
                    String guildAccepting = args[1].toUpperCase();
                    List<String> guildInvitations = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.GUILD_INVITES, player.getUniqueId());
                    if (guildInvitations.size() <= 0) {
                        player.sendMessage(ChatColor.RED + "You have no pending invitations!");
                        return true;
                    }
                    String invitation = guildInvitations.stream().filter(rs -> rs.startsWith(guildAccepting)).findFirst().get();

                    if (invitation == null || invitation.equals("")) {
                        player.sendMessage(ChatColor.RED + "You have no pending invitations to a guild with this name!");
                        return true;
                    }

                    String guildName = invitation.split(",")[0];

                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, "notices.guildInvites", guildName + "," + invitation.split(",")[1], true);
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "info.guild", guildName, true);


                    DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, "invitations", player.getUniqueId().toString(), true);
                    DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PUSH, "info.members", player.getUniqueId().toString(), true);


                    NetworkAPI.getInstance().sendNetworkMessage("guild", "update", guildName);
                    NetworkAPI.getInstance().sendNetworkMessage("guild", "message", player.getName() + " has joined the Guild!");
                    player.sendMessage(ChatColor.GREEN + "Congratulations! You have successfully joined " + ChatColor.AQUA + guildName);


                    break;
                case "party":
                    if (!Party.getInstance().isInParty(player) && Party.getInstance().isInParty(Bukkit.getPlayer(args[1]))) {
                        Party.RawParty party = Party.getInstance().getPlayerParty(Bukkit.getPlayer(args[1]));
                        if (party.getInviting().contains(player)) {
                            party.getMembers().add(player);
                            party.getInviting().remove(player);
                            player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.YELLOW + "You've join " + Bukkit.getPlayer(args[1]).getName() + "'s party!");
                        } else {
                            player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "That party doesn't have you in the request!");
                        }
                    } else {
                        player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "You are not in a party!");
                    }

                    break;
            }
        }

        return true;
    }
}
