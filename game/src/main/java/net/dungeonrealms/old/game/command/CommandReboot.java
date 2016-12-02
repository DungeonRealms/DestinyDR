package net.dungeonrealms.old.game.command;

import net.dungeonrealms.common.frontend.command.BaseCommand;
import net.dungeonrealms.vgame.old.Game;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


/**
 * Created by Kieran Quigley (Proxying) on 08-Jun-16.
 */
public class CommandReboot extends BaseCommand {
    public CommandReboot(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        String time = DurationFormatUtils.formatDurationWords((Game.getGame().getGameShard().getRebootTime() - System.currentTimeMillis()), true, true);
        commandSender.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + "Next Scheduled Reboot:" + ChatColor.RED + " " + time);
        return true;
    }
}
