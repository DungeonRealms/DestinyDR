package net.dungeonrealms.game.commands.punish;

import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import net.dungeonrealms.common.game.punishment.TimeFormat;
import net.dungeonrealms.game.mastery.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class CommandBan extends BasicCommand {

    public CommandBan(String command, String usage, String description, String... aliases) {
        super(command, usage, description, Arrays.asList(aliases));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!Rank.isPMOD(player)) return false;
        }

        if (args.length < 2) {
            sender.sendMessage(usage);
            return true;
        }


        String p_name = args[0];

        if (p_name.equalsIgnoreCase(sender.getName())) {
            sender.sendMessage(ChatColor.RED + "You cannot ban yourself.");
            return true;
        }


        if (DatabaseAPI.getInstance().getUUIDFromName(args[0]).equals("")) {
            sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + p_name + ChatColor.RED + " does not exist in our database.");
            return true;
        }

        UUID p_uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(args[0]));
        long duration = 0;
        boolean isNull = true;

        for (TimeFormat d : TimeFormat.values())
            if (d.getKey().equalsIgnoreCase(args[1].substring(args[1].length() - 1))) {
                String n = args[1].substring(0, args[1].length() - 1);
                if (!Utils.isInt(n)) continue;
                duration = Integer.parseInt(n) * d.convert();
                isNull = false;
                break;
            }

        if (!Utils.isInt(args[1]) && isNull) {
            sender.sendMessage(ChatColor.RED + args[1] + " is not a valid number.");
            return true;
        }

        if (isNull)
            duration = Integer.parseInt(args[1]);

        if (duration < -1) {
            sender.sendMessage(ChatColor.RED + args[1] + " is not a valid number.");
            return true;
        }

        if (sender instanceof Player)
            if (!Rank.isGM((Player) sender) && !Rank.isDev((Player) sender) && Rank.isPMOD((Player) sender)) {
                if (duration > 1209600L) {
                    sender.sendMessage(ChatColor.RED + "You cannot ban players for more than 14 days.");
                    return true;
                }

                if (duration == -1) {
                    sender.sendMessage(ChatColor.RED + "You cannot permanently ban players");
                    return true;
                }
            }

        if (Rank.isPMOD(Bukkit.getOfflinePlayer(p_uuid))) {
            sender.sendMessage(ChatColor.RED + "You cannot ban that player.");
            return true;
        }

        if (args.length >= 3) {
            StringBuilder reason = new StringBuilder(args[2]);
            for (int arg = 3; arg < args.length; arg++) reason.append(" ").append(args[arg]);

            String reasonString = reason.toString() + " [" + sender.getName() + "]";

            if (duration != -1)
                sender.sendMessage(ChatColor.RED.toString() + "You have banned " + ChatColor.BOLD + p_name + ChatColor.RED + " until " + PunishAPI.timeString((int) (duration / 60)) + " for " + reasonString);
            else
                sender.sendMessage(ChatColor.RED.toString() + "You have permanently banned " + ChatColor.BOLD + p_name + ChatColor.RED + " for " + reasonString);

            PunishAPI.ban(p_uuid, p_name, sender.getName(), duration, reasonString, null);
        } else {
            if (duration != -1)
                sender.sendMessage(ChatColor.RED.toString() + "You have banned " + ChatColor.BOLD + p_name + ChatColor.RED + " until " + PunishAPI.timeString((int) (duration / 60)));
            else
                sender.sendMessage(ChatColor.RED.toString() + "You have permanently banned " + ChatColor.BOLD + p_name);

            PunishAPI.ban(p_uuid, p_name, sender.getName(), duration, "", null);
        }

        return false;
    }
}
