package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Created by Chase on Nov 11, 2015
 */
public class CommandModeration extends BaseCommand {
    public CommandModeration(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    public static Map<UUID, UUID> offline_bin_watchers = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player sender = (Player) s;

        if (!Rank.isGM(sender)) return false;

        if (args.length == 0) {
            s.sendMessage(usage);
            return true;
        }

        switch (args[0]) {
            case "tp":
                String playerName = args[1];
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    sender.teleport(player.getLocation());
                    sender.sendMessage("Teleported to " + player.getName());
                } else
                    sender.sendMessage(ChatColor.RED + playerName + " not online");
                break;
            case "armorsee":
            case "banksee":
            case "binsee":
            case "gemsee":
            case "invsee":
                sender.sendMessage(ChatColor.RED + "Please use " + ChatColor.BOLD + ChatColor.UNDERLINE +
                        "/" + args[0].toLowerCase() + ChatColor.RED + " instead.");
                break;
        }
        return false;
    }
}
