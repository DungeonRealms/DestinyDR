package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Nick on 11/8/2015.
 */
public class CommandWhois extends BaseCommand {
    public CommandWhois(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player && !Rank.isTrialGM((Player) sender)) return true;

        if (args.length == 0) {
            sender.sendMessage("Syntax. /whois <player>");
            return true;
        }

        String p_name = args[0];

        Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {

            String id = DatabaseAPI.getInstance().getUUIDFromName(p_name);
            if (id.equals("")) {
                sender.sendMessage(ChatColor.RED + "Player " + p_name + " has never logged into DungeonRealms.");
                return;
            }
            UUID uuid = UUID.fromString(id);
            boolean isPlaying = (boolean) DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, uuid);
            String server = DatabaseAPI.getInstance().getFormattedShardName(uuid);

            if (!isPlaying) {
                sender.sendMessage(ChatColor.RED + p_name + ", currently offline.");
            }


            Document banDoc = PunishAPI.getBanDocument(uuid);
            if (banDoc != null) {
                Document bansDoc = banDoc.get("bans", Document.class);
                long banTime = bansDoc.getLong("bannedUntil");
                String reason = bansDoc.getString("reason");
                String by = bansDoc.getString("bannedBy");

                if (banTime != 0) {
                    if (banTime == -1 || banTime > System.currentTimeMillis()) {
                        sender.sendMessage(ChatColor.RED + p_name + " is currently banned for " + reason + " by " + by);

                        if (banTime > 0) {
                            String whenUnbanned = PunishAPI.timeString((int) ((banTime - System.currentTimeMillis()) / 60000));
                            sender.sendMessage(ChatColor.RED + p_name + " will be unbanned in " + whenUnbanned);
                        } else if (banTime == -1) {
                            sender.sendMessage(ChatColor.RED + p_name + " is never set to be unbanned.");
                        }
                    }
                }
            }

            if (isPlaying) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    if (Bukkit.getPlayer(p_name) == null)
                        sender.sendMessage(ChatColor.YELLOW + p_name + ", currently on server " + ChatColor.UNDERLINE + server);
                    else
                        sender.sendMessage(ChatColor.YELLOW + p_name + ", currently on " + ChatColor.UNDERLINE + "YOUR" + ChatColor.YELLOW + " server.");
                });
            }
//        });
        });
        return true;
    }
}
