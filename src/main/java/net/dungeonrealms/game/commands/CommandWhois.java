package net.dungeonrealms.game.commands;

import net.dungeonrealms.API;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.mastery.UUIDFetcher;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.StringTokenizer;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created by Nick on 11/8/2015.
 */
public class CommandWhois extends BasicCommand {
    public CommandWhois(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player && !Rank.isGM((Player) sender)) return true;
        String p_name = args[0];

        if(args.length != 1) {
            sender.sendMessage("Syntax. /whois <player>");
            return true;
        }
        String uuidString = DatabaseAPI.getInstance().getUUIDFromName(p_name);
        if (uuidString.equals("")) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
        }
        UUID uuid = UUID.fromString(uuidString);
        boolean isPlaying = (boolean)DatabaseAPI.getInstance().getValue(uuid, EnumData.IS_PLAYING);
        String server = (String)DatabaseAPI.getInstance().getValue(uuid, EnumData.CURRENTSERVER);

        if(!isPlaying) {
            sender.sendMessage(ChatColor.RED + p_name + ", currently offline.");
            return true;
        }

        String server_prefix = server.split("(?=[0-9])", 2)[0];
        int server_num = Integer.parseInt(server.split("(?=[0-9])", 2)[1]);

        if(Bukkit.getPlayer(p_name) == null) {
            sender.sendMessage(ChatColor.YELLOW + p_name + ", currently on server " + ChatColor.UNDERLINE + server_prefix.toUpperCase() + "-" + server_num);
        } else {
            sender.sendMessage(ChatColor.YELLOW + p_name + ", currently on " + ChatColor.UNDERLINE + "YOUR" + ChatColor.YELLOW + " server.");
        }
//        });

        return true;
    }
}
