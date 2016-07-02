package net.dungeonrealms.game.commands.punish;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.player.rank.Rank;
import net.dungeonrealms.game.punish.PunishUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class CommandUnban extends BasicCommand {

    public CommandUnban(String command, String usage, String description, String... aliases) {
        super(command, usage, description, Arrays.asList(aliases));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ((sender instanceof Player) && !Rank.isPMOD((Player) sender)) return true;

        if (args.length == 0) {
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

        if (!PunishUtils.isBanned(p_uuid, DatabaseAPI.getInstance())) {
            sender.sendMessage(ChatColor.RED + p_name + " is not banned.");
            return true;
        }

        PunishUtils.unban(p_uuid);
        sender.sendMessage(ChatColor.RED.toString() + "You have unbanned " + ChatColor.BOLD + p_name);
        return false;
    }
}
