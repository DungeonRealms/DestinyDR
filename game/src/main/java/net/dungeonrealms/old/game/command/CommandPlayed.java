package net.dungeonrealms.old.game.command;

import net.dungeonrealms.common.frontend.command.BaseCommand;
import net.dungeonrealms.common.old.game.database.DatabaseAPI;
import net.dungeonrealms.common.old.game.database.data.EnumData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Kieran Quigley (Proxying) on 08-Jul-16.
 */
public class CommandPlayed extends BaseCommand {

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
        return ChatColor.YELLOW.toString() + mins / 24 / 60 + ChatColor.BOLD + "d " + ChatColor.YELLOW + mins / 60 % 24 + ChatColor.BOLD + "h " + ChatColor.YELLOW + mins % 60 + ChatColor.BOLD + "m";
    }
}
