package net.dungeonrealms.game.commands.punish;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.common.game.database.player.Rank;
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

public class CommandMute extends BasicCommand {

    public CommandMute(String command, String usage, String description, String... aliases) {
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
            sender.sendMessage(ChatColor.RED + "You cannot mute yourself.");
            return true;
        }


        Player p = Bukkit.getPlayer(p_name);

        if (p == null) {
            sender.sendMessage(ChatColor.RED + p_name + " is not online.");
            return true;
        }

        UUID p_uuid = p.getUniqueId();

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
            duration = Integer.getInteger(args[1]);

        if (duration < 0) {
            sender.sendMessage(ChatColor.RED + args[1] + " is not a valid number.");
            return true;
        }

        if (args.length >= 3) {
            StringBuilder reason = new StringBuilder(args[2]);
            for (int arg = 3; arg < args.length; arg++) reason.append(" ").append(args[arg]);

            PunishAPI.mute(p_uuid, duration, reason.toString(), doAfter -> GameAPI.updatePlayerData(p_uuid));

            sender.sendMessage(ChatColor.RED.toString() + "You have muted " + ChatColor.BOLD + p_name + ChatColor.RED + " until " + PunishAPI.timeString((int) (duration / 60)) + " for " + reason.toString());
            p.sendMessage(ChatColor.RED.toString() + "You have been muted by " + ChatColor.BOLD + sender.getName() + ChatColor.RED + " until " + PunishAPI.timeString((int) (duration / 60)) + " for " + reason.toString());

        } else {
            PunishAPI.mute(p_uuid, duration, "", doAfter -> GameAPI.updatePlayerData(p_uuid));

            sender.sendMessage(ChatColor.RED.toString() + "You have muted " + ChatColor.BOLD + p_name + ChatColor.RED + " until " + PunishAPI.timeString((int) (duration / 60)));
            p.sendMessage(ChatColor.RED.toString() + "You have been muted by " + ChatColor.BOLD + sender.getName() + ChatColor.RED + " until " + PunishAPI.timeString((int) (duration / 60)));
        }

        return false;
    }
}
