package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import net.dungeonrealms.database.PlayerWrapper;
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
        SQLDatabaseAPI.getInstance().getUUIDFromName(p_name, false, (uuid) -> {

            PlayerWrapper.getPlayerWrapper(uuid, false, true, (wrapper) -> {

                if (wrapper == null) {
                    sender.sendMessage("Something went wrong.");
                    return;
                }

                if (!wrapper.isPlaying()) {
                    sender.sendMessage(ChatColor.RED + p_name + ", currently offline.");
                }

                String server = wrapper.getFormattedShardName();

                long banTime = wrapper.getBanExpire();
                String reason = wrapper.getBanReason();
                UUID byUID = wrapper.getWhoBannedMe();
                if (banTime != 0) {
                    if (banTime == -1 || banTime > System.currentTimeMillis()) {
                        String whoBanned = SQLDatabaseAPI.getInstance().getUsernameFromUUID(byUID);

                        if (banTime > 0) {
                            String whenUnbanned = PunishAPI.timeString((int) ((banTime - System.currentTimeMillis()) / 60000));
                            sender.sendMessage(ChatColor.RED + p_name + " will be unbanned in " + whenUnbanned);
                            sender.sendMessage(ChatColor.RED + p_name + " is currently banned for " + reason + " by " + whoBanned);
                        } else if (banTime == -1) {
                            sender.sendMessage(ChatColor.RED + p_name + " is never set to be unbanned.");
                            sender.sendMessage(ChatColor.RED + p_name + " is currently banned for " + reason + " by " + whoBanned);
                        }
                    }
                }


                if (wrapper.isPlaying()) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        if (Bukkit.getPlayer(p_name) == null)
                            sender.sendMessage(ChatColor.YELLOW + p_name + ", currently on server " + ChatColor.UNDERLINE + server);
                        else
                            sender.sendMessage(ChatColor.YELLOW + p_name + ", currently on " + ChatColor.UNDERLINE + "YOUR" + ChatColor.YELLOW + " server.");
                    });
                }
            });
        });
//        });
        return true;
    }
}
