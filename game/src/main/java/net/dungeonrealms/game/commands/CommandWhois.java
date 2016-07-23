package net.dungeonrealms.game.commands;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.player.Rank;
import net.dungeonrealms.game.database.type.EnumData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

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
        if (DatabaseAPI.getInstance().getUUIDFromName(p_name).equals("")) {
            sender.sendMessage(ChatColor.RED + "Player " + p_name + " has never logged into DungeonRealms.");
            return false;
        }
        UUID uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(p_name));
        boolean isPlaying = (boolean)DatabaseAPI.getInstance().getValue(uuid, EnumData.IS_PLAYING);
        String server = DatabaseAPI.getInstance().getFormattedShardName(uuid);

        if(!isPlaying) {
            sender.sendMessage(ChatColor.RED + p_name + ", currently offline.");
            return true;
        }

        if(Bukkit.getPlayer(p_name) == null) {
            sender.sendMessage(ChatColor.YELLOW + p_name + ", currently on server " + ChatColor.UNDERLINE + server);
        } else {
            sender.sendMessage(ChatColor.YELLOW + p_name + ", currently on " + ChatColor.UNDERLINE + "YOUR" + ChatColor.YELLOW + " server.");
        }
//        });

        return true;
    }
}
