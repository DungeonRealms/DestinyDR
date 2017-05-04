package net.dungeonrealms.game.command.punish;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.punishment.PunishAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class CommandUnban extends BaseCommand {

    public CommandUnban(String command, String usage, String description, String... aliases) {
        super(command, usage, description, Arrays.asList(aliases));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!Rank.isTrialGM(player)) return false;
        }

        if (args.length == 0) {
            sender.sendMessage(usage);
            return true;
        }

        String p_name = args[0];

        if (p_name.equalsIgnoreCase(sender.getName())) {
            sender.sendMessage(ChatColor.RED + "You cannot ban yourself.");
            return true;
        }

        SQLDatabaseAPI.getInstance().getUUIDFromName(args[0], false, p_uuid -> {
            if (p_uuid == null) {
                sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " does not exist in our database.");
                return;
            }
            PunishAPI.isBanned(p_uuid, banned -> {
                if (!banned) {
                    sender.sendMessage(ChatColor.RED + p_name + " is not banned.");
                    return;
                }
                PunishAPI.unban(p_uuid);
                sender.sendMessage(ChatColor.RED.toString() + "You have unbanned " + ChatColor.BOLD + p_name + ChatColor.RED + ".");
                GameAPI.sendNetworkMessage("StaffMessage", ChatColor.RED + ChatColor.BOLD.toString() + sender.getName() + " has unbanned " + ChatColor.BOLD + p_name + ChatColor.RED + ".");
                GameAPI.sendNetworkMessage("BanMessage", sender.getName() + ": /unban " + p_name);
            });
        });
        return false;
    }
}
