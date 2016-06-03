//package net.dungeonrealms.game.commands;
//
//import net.dungeonrealms.core.Core;
//import net.dungeonrealms.game.commands.generic.BasicCommand;
//import net.dungeonrealms.game.guild.GuildDatabaseAPI;
//import net.dungeonrealms.game.guild.db.GuildDatabase;
//import net.dungeonrealms.game.mongo.DatabaseAPI;
//import net.dungeonrealms.game.mongo.EnumData;
//import net.dungeonrealms.game.player.chat.GameChat;
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandSender;
//import org.bukkit.command.ConsoleCommandSender;
//import org.bukkit.entity.Player;
//import org.json.simple.JSONObject;
//
//import java.util.ArrayList;
//import java.util.UUID;
//
///**
// * Created by Nick on 10/2/2015.
// */
//public class CommandGuild extends BasicCommand {
//
//    public CommandGuild(String command, String usage, String description) {
//        super(command, usage, description);
//    }
//
//    @Override
//    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
//
//        if (s instanceof ConsoleCommandSender) return false;
//
//        Player player = (Player) s;
//
//        if(!player.isOp()) {
//            return true;
//        }
//
//        /*
//        if (Guild.getInstance().isGuildNull(player.getUniqueId())) {
//            player.sendMessage(ChatColor.RED + "You are not in a guild, or we're having trouble finding it.");
//            return true;
//        }
//         */
//
//        String guildName = GuildDatabase.getInstance().getGuildOf(player.getUniqueId());
//
//        if (args.length > 0) {
//
//            if (args[0].equalsIgnoreCase("demote")) {
//                if (args.length == 2) {
//                    if (GuildDatabase.getInstance().isOwner(player.getUniqueId(), guildName)) {
//                       // if (Core.getInstance().hasPlayedBefore(args[1])) {
//                            if (GuildDatabaseAPI.getInstance().isOfficer(Bukkit.getPlayer(args[1]).getUniqueId(), guildName)) {
//
//                                GuildDatabaseAPI.getInstance().demotePlayer(guildName, Bukkit.getPlayer(args[1]).getUniqueId());
//
//                            } else {
//                                player.sendMessage(ChatColor.RED + "You can only demote [OFFICER] to [MEMBER]!");
//                            }
//                       // } else {
//                        //    player.sendMessage(ChatColor.RED + "That player has never played on DungeonRealms!");
//                      //  }
//                    } else {
//                        player.sendMessage(ChatColor.RED + "You must be of the rank [OWNER] to promote players!");
//                    }
//                } else {
//                    player.sendMessage(ChatColor.RED + "Syntax! Try /guild promote <userName>");
//                }
//            }
//        }
//
//        if (args[0].equalsIgnoreCase("promote")) {
//            if (args.length == 2) {
//                if (GuildDatabase.getInstance().isOwner(player.getUniqueId(), guildName)) {
//                    if (Bukkit.getPlayer(args[1]) != null) {
//                        if (GuildDatabase.getInstance().isMember(Bukkit.getPlayer(args[1]).getUniqueId(), guildName)) {
//
//                            GuildDatabase.getInstance().promotePlayer(guildName, Bukkit.getPlayer(args[1]).getUniqueId());
//
//                        } else {
//                            player.sendMessage(ChatColor.RED + "You can only promote [MEMBER] to [OFFICER]!");
//                        }
//                    } else {
//                        player.sendMessage(ChatColor.RED + "The player you're trying to prmote isn't online!");
//                    }
//                } else {
//                    player.sendMessage(ChatColor.RED + "You must be of the rank [OWNER] to promote players!");
//                }
//            } else {
//                player.sendMessage(ChatColor.RED + "Syntax! Try /guild promote <userName>");
//            }
//        } else
//
//            /*
//            Guild chat, /g <message>
//             */
//            if (args[0].equalsIgnoreCase("c") || args[0].equalsIgnoreCase("chat")) {
//                if (args.length > 1) {
//                    StringBuilder message = new StringBuilder();
//
//                    for (int i = 1; i < args.length; i++) {
//                        message.append(args[i]).append(" ");
//                    }
//
//                    GuildDatabase.getInstance().sendAlert(guildName, ChatColor.GREEN.toString() + ChatColor.BOLD + "G" + " " + GameChat.getPreMessage(player) + message.toString());
//                } else {
//                    player.sendMessage(ChatColor.RED + "Make your message longer.");
//                }
//            } else
//
//            /*
//            Guild Set Desc, /guild desc <shit>
//             */
//                if (args[0].equalsIgnoreCase("desc") || args[0].equalsIgnoreCase("d") || args[0].equalsIgnoreCase("motd")) {
//                    //Make sure the person setting the motd is the owner.
//                    if (GuildDatabase.getInstance().isOwner(player.getUniqueId(), DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()).toString())) {
//                        if (GuildDatabase.getInstance().isOwnerOfGuild(player)) {
//                            if (args.length > 1) {
//                                StringBuilder motd = new StringBuilder();
//
//                                for (int i = 1; i < args.length; i++) {
//                                    motd.append(args[i]).append(" ");
//                                }
//
//                                GuildDatabase.getInstance().setMotdOf(DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()).toString(), motd.toString());
//
//                            } else {
//                                player.sendMessage(ChatColor.RED + "Make your description longer!?");
//                            }
//                        }
//                    } else {
//                        player.sendMessage(ChatColor.RED + "You must be of the rank [OWNER] to adjust this!");
//                    }
//                } else
//
//            /*
//            Guild Accept, /guild accept <guildName>
//             */
//                    if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("a")) {
//                        if (args.length == 2) {
//                            if (GuildDatabaseAPI.getInstance().isGuildNull(player.getUniqueId())) {
//                                if (GuildDatabaseAPI.invitations.containsKey(player.getUniqueId())) {
//                                    if (GuildDatabaseAPI.invitations.get(player.getUniqueId()).contains(args[1])) {
//                                        GuildDatabaseAPI.getInstance().setGuild(player.getUniqueId(), args[1]);
//                                        GuildDatabaseAPI.getInstance().sendAlert(args[1], ChatColor.GREEN + player.getName() + " " + ChatColor.YELLOW + "has joined the guild!");
//                                    }
//                                } else {
//                                    player.sendMessage(ChatColor.RED + "You have no pending guild invitations!");
//                                }
//                            } else {
//                                player.sendMessage(ChatColor.RED + "You are already in a guild!");
//                            }
//                        } else {
//                            player.sendMessage(ChatColor.RED + "Syntax! Try /guild accept <guildName>");
//                        }
//                    } else
//
//            /*
//            Guild Invite, /guild invite <playerName>
//             */
//                        if (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("inv")) {
//                            if (GuildDatabaseAPI.getInstance().isOfficer(player.getUniqueId(), guildName) || GuildDatabaseAPI.getInstance().isOwner(player.getUniqueId(), guildName)) {
//                                if (args.length == 2) {
//                                    if (Bukkit.getPlayer(args[1]) != null && !Bukkit.getPlayer(args[1]).getName().equals(player.getName())) {
//                                        if (GuildDatabaseAPI.getInstance().isGuildNull(Bukkit.getPlayer(args[1]).getUniqueId())) {
//
//                                            if (!GuildDatabaseAPI.invitations.containsKey(Bukkit.getPlayer(args[1]).getUniqueId())) {
//                                                GuildDatabaseAPI.invitations.put(Bukkit.getPlayer(args[1]).getUniqueId(), new ArrayList<String>() {{
//                                                    add(guildName);
//                                                }});
//                                                GuildDatabaseAPI.getInstance().sendAlert(guildName, ChatColor.RED + player.getName() + " has invited " + args[1] + " " + "to the guild!");
//                                                Bukkit.getPlayer(args[1]).sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "GUILD INVITATION" + ChatColor.RED + " >> " + ChatColor.GOLD + guildName + " " + ChatColor.GRAY + "Type /guild accept " + guildName);
//                                                //Bukkit.getPlayer(args[1]).sendMessage(ChatColor.GREEN + "You have been invited to: " + ChatColor.GOLD + guildName + " " + ChatColor.GREEN + "type /guild accept " + guildName + " to accept!");
//                                            } else {
//                                                if (!GuildDatabaseAPI.invitations.get(player.getUniqueId()).contains(guildName)) {
//                                                    ArrayList<String> temp = GuildDatabaseAPI.invitations.get(Bukkit.getPlayer(args[1]).getUniqueId());
//                                                    temp.add(guildName);
//                                                    GuildDatabaseAPI.invitations.put(Bukkit.getPlayer(args[1]).getUniqueId(), temp);
//                                                } else {
//                                                    player.sendMessage(ChatColor.RED + "That player already has a pending invitation to " + ChatColor.GOLD + guildName);
//                                                }
//                                            }
//
//                                        } else {
//                                            player.sendMessage(ChatColor.RED + args[1] + " is already in a guild!");
//                                        }
//                                    } else {
//                                        player.sendMessage(ChatColor.RED + args[1] + " isn't online, or you can't invite yourself!");
//                                    }
//                                } else {
//                                    player.sendMessage(ChatColor.RED + "Syntax! Try /g invite <playerName>");
//                                }
//                            }
//                        }
//            /*
//            Guild info, /guild info
//             */
//                        else if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("i")) {
//                            JSONObject guild = GuildDatabaseAPI.getInstance().guilds.get(GuildDatabaseAPI.getInstance().getGuildOf(player.getUniqueId()));
//                            player.sendMessage(new String[]{
//                                    ChatColor.BLUE + "___________________> " + ChatColor.GREEN + guild.get("name").toString() + ChatColor.BLUE + " <___________________",
//                                    ChatColor.WHITE + "Motd: " + ChatColor.GRAY + guild.get("motd").toString(),
//                                    //ChatColor.WHITE + "Level: " + ChatColor.GOLD + guild.get("level").toString(),
//                                    //ChatColor.WHITE + "Experience: " + ChatColor.YELLOW + guild.get("experience").toString(),
//                                    //ChatColor.WHITE + "Allies: " + ChatColor.GREEN + Guild.getInstance().getAlliesOf(guild.get("name").toString()),
//                                    //ChatColor.WHITE + "Enemies: " + ChatColor.RED + Guild.getInstance().getEnemiesOf(guild.get("name").toString()),
//                                    ChatColor.WHITE + "Members Online: " + ChatColor.GREEN + GuildDatabaseAPI.getInstance().getAllOnlineNamesOf(guild.get("name").toString()),
//                                    ChatColor.WHITE + "Members Offline: " + ChatColor.GREEN + GuildDatabaseAPI.getInstance().getAllOfflineOf(guild.get("name").toString()),
//                                    ChatColor.WHITE + "Achievements: " + ChatColor.RED + "None."
//                            });
//                        } else if (args[0].equalsIgnoreCase("kick") || args[0].equalsIgnoreCase("k")) {
//                            if (GuildDatabaseAPI.getInstance().isOwner(player.getUniqueId(), guildName) || GuildDatabaseAPI.getInstance().isOfficer(player.getUniqueId(), guildName)) {
//                                if (args.length == 2) {
//                                    if (Bukkit.getPlayer(args[1]) != null) {
//
//                                        if (GuildDatabaseAPI.getInstance().isOwner(Bukkit.getPlayer(args[1]).getUniqueId(), guildName)) {
//                                            player.sendMessage(ChatColor.RED + "You must be of the rank [ADMIN] to demote a Guild Owner.");
//                                            return true;
//                                        }
//
//                                        if (!GuildDatabaseAPI.getInstance().isGuildNull(Bukkit.getPlayer(args[1]).getUniqueId())) {
//
//                                            GuildDatabaseAPI.getInstance().kickFrom(player.getUniqueId(), guildName, Bukkit.getPlayer(args[1]).getUniqueId());
//
//                                        } else {
//                                            player.sendMessage(ChatColor.RED + "That player isn't in a guild!");
//                                        }
//
//                                    } else {
//                                        //remove player from guild when they're offline.
//
//                                        if (Core.getInstance().hasPlayedBefore(args[1])) {
//                                            UUID uuid = Core.getInstance().getUUIDFromName(args[1]);
//
//                                            GuildDatabaseAPI.getInstance().kickFrom(player.getUniqueId(), guildName, uuid);
//
//                                        } else {
//                                            player.sendMessage(ChatColor.RED + "Not only is that player not in a guild, that player has never played before!");
//                                        }
//
//                                    }
//                                } else {
//                                    player.sendMessage(ChatColor.RED + "Syntax! Try /guild kick <playerName>");
//                                }
//                            } else {
//                                player.sendMessage(ChatColor.RED + "You must be of the rank [[OWNER], [OFFICER]] to kick players!");
//                            }
//                        }
//
//        switch (args[0]) {
//            case "create":
//                GuildDatabaseAPI.getInstance().doesGuildNameExist(args[1], doesExist -> {
//                    if (!doesExist) {
//                        GuildDatabaseAPI.getInstance().doesClanTagExist(args[2], clanTag -> {
//                            if (!clanTag) {
//                                player.sendMessage(ChatColor.GREEN + "That clanTag is available!");
//                                GuildDatabaseAPI.getInstance().createGuild(args[1], args[2], player.getUniqueId(), creation -> player.sendMessage(creation ? ChatColor.GREEN + "Your guild has been created!" : ChatColor.RED + "AN ERROR occurred"));
//                            } else {
//                                player.sendMessage(ChatColor.RED + "That clanTag is already taken!");
//                            }
//                        });
//                    } else {
//                        player.sendMessage(ChatColor.RED + "That guildName is already taken!");
//                    }
//                });
//                break;
//            case "check":
//                player.sendMessage(GuildDatabaseAPI.getInstance().getGuildOf(player.getUniqueId()));
//                break;
//            case "save":
//                GuildDatabaseAPI.getInstance().saveAllGuilds();
//                break;
//        }
//        return true;
//    }
//}
//
