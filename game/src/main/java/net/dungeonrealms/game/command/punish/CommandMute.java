package net.dungeonrealms.game.command.punish;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.punishment.PunishAPI;
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

public class CommandMute extends BaseCommand {

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

        int senderID = sender instanceof Player ? PlayerWrapper.getPlayerWrapper((Player)sender).getAccountID() : 0;

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
            duration = Integer.parseInt(args[1]);

        if (duration < 0) {
            sender.sendMessage(ChatColor.RED + args[1] + " is not a valid number.");
            return true;
        }

        // Build the reason string.
        String reasonString = "";
        if (args.length >= 3) {
            StringBuilder reason = new StringBuilder(args[2]);

            for (int arg = 3; arg < args.length; arg++)
                reason.append(" ").append(args[arg]);

            reasonString = reason.toString();
        }

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(p_uuid);
        // Apply the mute against the user.
        PunishAPI.mute(wrapper, duration, reasonString, doAfter -> GameAPI.updatePlayerData(p_uuid, "mute"));

        //
        String punishmentLength = ChatColor.RED + PunishAPI.timeString((int) (duration / 60));
        String friendlyReason = ChatColor.RED + (reasonString != "" ? ".\nReason: " + ChatColor.ITALIC + reasonString : "");

        SQLDatabaseAPI.getInstance().addQuery(QueryType.INSERT_MUTE, wrapper.getAccountID(), "mute", System.currentTimeMillis(), System.currentTimeMillis() + duration * 1000, senderID, reasonString);
        // Distribute the appropriate messages.
        sender.sendMessage(ChatColor.RED.toString() + "You have muted " + ChatColor.BOLD + p_name + ChatColor.RED + " for " + punishmentLength + ".");
        p.sendMessage(ChatColor.RED.toString() + "You have been muted by " + ChatColor.BOLD + sender.getName() + ChatColor.RED + " for " + punishmentLength + friendlyReason + ".");
        GameAPI.sendNetworkMessage("StaffMessage", ChatColor.RED + ChatColor.BOLD.toString() + sender.getName() + ChatColor.RED + " has muted " + ChatColor.BOLD + p_name + ChatColor.RED + " for " + punishmentLength + friendlyReason + ".");
        
        //  BROADCASTS TO DISCORD  //
        String message = "/" + label;
        for(String a : args)
        	message += " " + a;
        GameAPI.sendNetworkMessage("BanMessage", sender.getName() + ": " + message);
        
        return false;
    }
}
