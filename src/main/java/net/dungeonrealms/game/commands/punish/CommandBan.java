package net.dungeonrealms.game.commands.punish;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.player.rank.Rank;
import net.dungeonrealms.game.punish.PunishUtils;
import net.dungeonrealms.game.punish.TimeFormat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class CommandBan extends BasicCommand {

    public CommandBan(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(usage);
            return true;
        }

        if ((sender instanceof Player) && !Rank.isPMOD((Player) sender)) return true;


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

        for (TimeFormat d : TimeFormat.values())
            if (d.getKey().equalsIgnoreCase(args[1].substring(args[1].length() - 1))) {
                String n = args[1].substring(0, args[1].length() - 1);
                if (!Utils.isInt(n)) continue;
                duration = Integer.parseInt(n) * d.convert();
            }

        if (duration == 0 && !Utils.isInt(args[1])) {
            sender.sendMessage(ChatColor.RED + args[1] + " is not a valid number.");
            return true;
        }

        if ((sender instanceof Player) && Rank.isPMOD((Player) sender) && duration > 1209600L) {
            sender.sendMessage(ChatColor.RED + "You cannot ban players for more than 14 days.");
            return true;
        }

        if (args.length >= 3) {
            StringBuilder reason = new StringBuilder(args[2]);
            for (int arg = 3; arg < args.length; arg++) reason.append(" ").append(args[arg]);
            sender.sendMessage(ChatColor.RED.toString() + "You have banned " + ChatColor.BOLD + p_name + ChatColor.RED + " until " + Utils.timeString((int) (duration / 60)) + " for " + reason.toString());
            PunishUtils.ban(p_uuid, p_name, duration, reason.toString());
        } else {
            sender.sendMessage(ChatColor.RED.toString() + "You have banned " + ChatColor.BOLD + p_name + ChatColor.RED + " until " + Utils.timeString((int) (duration / 60)));
            PunishUtils.ban(p_uuid, p_name, duration, "");
        }

        return false;
    }
}
