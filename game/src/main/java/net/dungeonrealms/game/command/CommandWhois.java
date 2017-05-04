package net.dungeonrealms.game.command;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.util.TimeUtil;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.punishment.PunishAPI;
import net.dungeonrealms.database.punishment.PunishType;
import net.dungeonrealms.database.punishment.Punishment;
import net.dungeonrealms.game.player.json.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommandWhois extends BaseCommand {
    public CommandWhois(String command, String usage, String description) {
        super(command, usage, description);
    }

    private static SimpleDateFormat format = new SimpleDateFormat("MMM dd, YY hh:mm aa");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player && !Rank.isTrialGM((Player) sender)) return true;

        Rank.PlayerRank rank;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            rank = Rank.getInstance().getPlayerRank(player.getUniqueId());
        } else {
            rank = Rank.PlayerRank.DEV;
        }
        if (args.length == 0) {
            if (sender instanceof Player) {
                sender.sendMessage("Syntax. /whois <player>" + (rank.isAtleast(Rank.PlayerRank.GM) ? "/<ipAddress> -a (alts) -i (ip)" : ""));
                return true;
            }
            sender.sendMessage("Syntax. /whois <player> ");
            return true;
        }

        List<String> argList = Lists.newArrayList(args);

        boolean showAlts = argList.contains("-a") && rank.isAtleast(Rank.PlayerRank.GM);
        boolean showIPs = argList.contains("-i") && rank.isAtleast(Rank.PlayerRank.HEADGM);

        String p_name = args[0];
        Player online = Bukkit.getPlayer(p_name);
        if (p_name.contains(".")) {

            if (showAlts || rank.isAtleast(Rank.PlayerRank.DEV)) {
                //its an ip
                SQLDatabaseAPI.getInstance().executeQuery(QueryType.SELECT_ALTS.getQuery(p_name), true, (set) -> {
                    if (set == null) {
                        sender.sendMessage("No alts found!");
                        return;
                    }

                    List<String> alts = new ArrayList<>();
                    try {
                        while (set.next()) {
                            int accountID = set.getInt("ip_addresses.account_id");
                            long lastUsed = set.getLong("ip_addresses.last_used");
                            String username = set.getString("users.username");

                            alts.add(ChatColor.AQUA + username + " , Account ID: " + accountID + ", Last Used: " + format.format(new Date(lastUsed)));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    if (alts.isEmpty()) {
                        sender.sendMessage(ChatColor.GREEN + "No Alts found for " + p_name);
                    } else {
                        sender.sendMessage(ChatColor.RED.toString() + alts.size() + ChatColor.GREEN.toString() + " alt accounts found for " + p_name);
                        for (String message : alts) {
                            sender.sendMessage(message);
                        }
                    }


                });
            }
        } else {
            SQLDatabaseAPI.getInstance().getUUIDFromName(p_name, false, (uuid) -> {

                PlayerWrapper.getPlayerWrapper(uuid, false, true, (wrapper) -> {

                    if (wrapper == null) {
                        sender.sendMessage("Something went wrong.");
                        return;
                    }

                    if (!wrapper.isPlaying()) {
                        sender.sendMessage(ChatColor.RED + p_name + ", currently offline.");
                    } else {
                        String server = wrapper.getFormattedShardName();
                        if (online == null)
                            sender.sendMessage(ChatColor.YELLOW + p_name + ", currently on server " + ChatColor.UNDERLINE + server);
                        else
                            sender.sendMessage(ChatColor.YELLOW + p_name + ", currently on " + ChatColor.UNDERLINE + "YOUR" + ChatColor.YELLOW + " server.");
                    }

                    if (wrapper.isBanned()) {
                        long banTime = wrapper.getBanExpire();
                        String reason = wrapper.getBanReason();
                        int byUID = wrapper.getWhoBannedMeID();
                        String whoBanned = SQLDatabaseAPI.getInstance().getUsernameFromAccountID(byUID);
                        if (banTime > 0) {
                            String whenUnbanned = PunishAPI.timeString((int) ((banTime - System.currentTimeMillis()) / 60000));
                            sender.sendMessage(ChatColor.RED + p_name + " will be unbanned in " + whenUnbanned);
                            sender.sendMessage(ChatColor.RED + p_name + " is currently banned for " + reason + " by " + whoBanned);
                        } else if (banTime == -1) {
                            sender.sendMessage(ChatColor.RED + p_name + " is never set to be unbanned.");
                            sender.sendMessage(ChatColor.RED + p_name + " is currently banned for " + reason + " by " + whoBanned);
                        }
                    }

                    wrapper.loadAllPunishments(false, punishments -> {

                        List<Punishment> bans = punishments.get(PunishType.BAN);
                        List<Punishment> mutes = punishments.get(PunishType.MUTE);
                        sender.sendMessage("");
                        sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Showing " + bans.size() + ChatColor.RED + " Bans - " + ChatColor.RED + ChatColor.BOLD + mutes.size() + ChatColor.RED + " Mutes");
                        bans.forEach(punishment -> sendPunish(sender, "banned", punishment));

                        if (mutes.size() > 0)
                            sender.sendMessage("");
                        mutes.forEach(punishment -> sendPunish(sender, "muted", punishment));

                        sender.sendMessage("");

                    });

                    if (showAlts) {
                        SQLDatabaseAPI.getInstance().executeQuery(QueryType.SELECT_ALTS.getQuery(wrapper.getLastIP()), true, (set) -> {
                            if (set == null) {
                                sender.sendMessage(ChatColor.RED + "No alts found!");
                                return;
                            }

                            List<String> alts = new ArrayList<>();
                            try {
                                while (set.next()) {
                                    int accountID = set.getInt("ip_addresses.account_id");
                                    long lastUsed = set.getLong("ip_addresses.last_used");

                                    String username = set.getString("users.username");

                                    if (accountID == wrapper.getAccountID()) continue;

                                    alts.add(ChatColor.AQUA + "(" + accountID + ") " + username + ", Last Used: " + format.format(new Date(lastUsed)));
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            if (alts.isEmpty()) {
                                sender.sendMessage(ChatColor.GREEN + "No Alts found for " + p_name);
                            } else {
                                sender.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + alts.size() + ChatColor.GREEN.toString() + " alt account(s) found for: " + p_name);
                                for (String message : alts) {
                                    sender.sendMessage(message);
                                }
                            }

                        });
                    }

                    if (showIPs) {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(ChatColor.RED + "Only players can view ips.");
                            return;
                        }
                        Player player = (Player) sender;

                        long start = System.currentTimeMillis();
                        SQLDatabaseAPI.getInstance().executeQuery(QueryType.SELECT_ALTS_FROM_ACCOUNT_ID.getQuery(wrapper.getAccountID()), true, (set) -> {
                            if (set == null) {
                                sender.sendMessage(ChatColor.RED + "No alts found from account ID: " + wrapper.getAccountID());
                                return;
                            }

                            List<String> alts = new ArrayList<>();
                            try {
                                JSONMessage message = new JSONMessage(ChatColor.YELLOW + "IP History: ", ChatColor.YELLOW);
                                String latestIP = null;
                                long latestTime = 0;

                                boolean first = true;
                                boolean odd = false;
                                while (set.next()) {
                                    String ip = set.getString("ip_addresses.ip_address");
                                    long lastUsed = set.getLong("ip_addresses.last_used");

                                    ChatColor color = odd ? ChatColor.WHITE : ChatColor.GRAY;
                                    message.addRunCommand((!first ? ", " : "") + color + ip, color, "/whois " + ip, ChatColor.GRAY + "Last Used: " + format.format(new Date(lastUsed)) + "\nClick to search this IP!");
//                                    alts.add(ChatColor.AQUA + username + " , A-ID: " + accountID + ", Last Used: " + format.format(new Date(lastUsed)));
                                    odd = !odd;

                                    first = false;
                                    if (lastUsed >= latestTime) {
                                        latestIP = ip;
                                        latestTime = lastUsed;
                                    }
                                }

                                if (latestIP != null) {
                                    JSONMessage recent = new JSONMessage(ChatColor.YELLOW + "Most Recent IP: ", ChatColor.YELLOW);
                                    recent.addRunCommand(ChatColor.WHITE + latestIP, ChatColor.WHITE, "/whois " + latestIP, ChatColor.RED + "Last Used: " + format.format(new Date(latestTime)));
                                    recent.sendToPlayer(player);
                                }
                                message.sendToPlayer(player);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                });
            });
        }
//        });
        return true;
    }


    private void sendPunish(CommandSender sender, String type, Punishment punishment) {
        String name = SQLDatabaseAPI.getInstance().getUsernameFromAccountID(punishment.getPunisherID());
        sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + name + ChatColor.RED + " " + type + " on " + ChatColor.RED + ChatColor.BOLD + format.format(new Date(punishment.getIssued())) + ChatColor.RED + " for " + ChatColor.YELLOW + punishment.getReason() +
                ChatColor.RED + (punishment.getExpiration() == 0 ? " will NEVER expire." : punishment.getExpiration() > System.currentTimeMillis() ? " expires in " + TimeUtil.formatDifference((punishment.getExpiration() - System.currentTimeMillis()) / 1000) : " expired " + TimeUtil.formatDifference((System.currentTimeMillis() - punishment.getExpiration()) / 1000) + " ago.") + (punishment.isQuashed() ? ChatColor.RED.toString() + ChatColor.BOLD + " (UNBANNED)" : ""));
    }
}
