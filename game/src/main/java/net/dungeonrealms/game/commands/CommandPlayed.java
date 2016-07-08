package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Kieran Quigley (Proxying) on 08-Jul-16.
 */
public class CommandPlayed extends BasicCommand {

    public CommandPlayed(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 0) {
                Player player = (Player) sender;
                int minutesPlayed = (int) DatabaseAPI.getInstance().getData(EnumData.TIME_PLAYED, player.getUniqueId());
                player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.UNDERLINE + ChatColor.BOLD + "Time Played:" + ChatColor.YELLOW.toString() + " " + convertMins(minutesPlayed));
                return true;
            }
        }
        return false;
    }

    private String convertMins(int mins) {
        return ChatColor.YELLOW.toString() + mins/24/60 + ChatColor.BOLD + "d " + ChatColor.YELLOW + mins/60%24 + ChatColor.BOLD + "h " + ChatColor.YELLOW + mins%60 + ChatColor.BOLD + "m";
    }
}
