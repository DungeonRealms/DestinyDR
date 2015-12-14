package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.guild.Guild;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.util.ArrayList;

/**
 * Created by Nick on 10/2/2015.
 */
public class CommandGuild extends BasicCommand {

    public CommandGuild(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {

        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "[WARNING] " + ChatColor.YELLOW + "You do not have permissions for this!");
            return false;
        }

        /*
        if (Guild.getInstance().isGuildNull(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are not in a guild, or we're having trouble finding it.");
            return true;
        }
         */


        if (args.length > 0) {

            /*
            Guild Accept, /guild accept <guildName>
             */
            if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("a")) {
                if (args.length == 2) {
                    if (Guild.getInstance().isGuildNull(player.getUniqueId())) {

                        if (Guild.invitations.containsKey(player)) {
                            if (Guild.invitations.get(player).contains(args[1])) {
                                DatabaseAPI.getInstance().update(Bukkit.getPlayer(args[1]).getUniqueId(), EnumOperators.$SET, EnumData.GUILD, args[1], true);
                                Guild.getInstance().sendAlert(args[1], ChatColor.GREEN + args[1] + " " + ChatColor.YELLOW + "has accepted the guild invitation!");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You have no pending guild invitations!");
                        }

                    } else {
                        player.sendMessage(ChatColor.RED + "You are already in a guild!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Syntax! Try /guild accept <guildName>");
                }
            }

            /*
            Guild Invite, /guild invite <playerName>
             */
            if (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("inv")) {
                if (args.length == 2) {
                    if (Bukkit.getPlayer(args[1]) != null && !Bukkit.getPlayer(args[1]).getName().equals(player.getName())) {
                        if (Guild.getInstance().isGuildNull(Bukkit.getPlayer(args[1]).getUniqueId())) {

                            String guildName = DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()).toString();

                            if (!Guild.invitations.containsKey(Bukkit.getPlayer(args[1]))) {
                                Guild.invitations.put(Bukkit.getPlayer(args[1]), new ArrayList<String>() {{
                                    add(guildName);
                                }});
                                Bukkit.getPlayer(args[1]).sendMessage(ChatColor.GREEN + "You have been invited to: " + ChatColor.GOLD + guildName + " " + ChatColor.GREEN + "type /guild accept " + guildName + " to accept!");
                            } else {
                                if (!Guild.invitations.get(player).contains(guildName)) {
                                    ArrayList<String> temp = Guild.invitations.get(Bukkit.getPlayer(args[1]));
                                    temp.add(guildName);
                                    Guild.invitations.put(Bukkit.getPlayer(args[1]), temp);
                                } else {
                                    player.sendMessage(ChatColor.RED + "That player already has a pending invitation to " + ChatColor.GOLD + guildName);
                                }
                            }

                        } else {
                            player.sendMessage(ChatColor.RED + args[1] + " is already in a guild!");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + args[1] + " isn't online, or you can't invite yourself!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Syntax! Try /g invite <playerName>");
                }
            }
            /*
            Guild info, /guild info
             */
            else if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("i")) {
                JSONObject guild = Guild.getInstance().guilds.get(DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()).toString());
                player.sendMessage(new String[]{
                        ChatColor.BLUE + "___________________> " + ChatColor.GREEN + guild.get("name").toString() + ChatColor.BLUE + " <___________________",
                        ChatColor.WHITE + "Motd: " + ChatColor.GRAY + guild.get("motd").toString(),
                        ChatColor.WHITE + "Level: " + ChatColor.GOLD + guild.get("level").toString(),
                        ChatColor.WHITE + "Experience: " + ChatColor.YELLOW + guild.get("experience").toString(),
                        ChatColor.WHITE + "Allies: " + ChatColor.GREEN + Guild.getInstance().getAlliesOf(guild.get("name").toString()),
                        ChatColor.WHITE + "Enemies: " + ChatColor.RED + Guild.getInstance().getEnemiesOf(guild.get("name").toString()),
                        ChatColor.WHITE + "Members Online: " + ChatColor.GREEN + Guild.getInstance().getAllOnlineOf(guild.get("name").toString()),
                        ChatColor.WHITE + "Members Offline: " + ChatColor.GREEN + Guild.getInstance().getAllOfflineOf(guild.get("name").toString()),
                        ChatColor.WHITE + "Achievements: " + ChatColor.RED + "None."
                });
            }

            switch (args[0]) {
                case "create":
                    Guild.getInstance().doesGuildNameExist(args[1], guildName -> {
                        if (!guildName) {
                            Guild.getInstance().doesClanTagExist(args[2], clanTag -> {
                                if (!clanTag) {
                                    player.sendMessage(ChatColor.GREEN + "That clanTag is available!");
                                    Guild.getInstance().createGuild(args[1], args[2], player.getUniqueId(), creation -> {
                                        player.sendMessage(creation ? ChatColor.GREEN + "Your guild has been created!" : ChatColor.RED + "AN ERROR occurred");
                                    });
                                } else {
                                    player.sendMessage(ChatColor.RED + "That clanTag is already taken!");
                                }
                            });
                        } else {
                            player.sendMessage(ChatColor.RED + "That guildName is already taken!");
                        }
                    });
                    break;
                case "remove":
                    break;
                case "kick":
                    break;
                case "chat":
                    break;
            }
        }

        return true;
    }
}
