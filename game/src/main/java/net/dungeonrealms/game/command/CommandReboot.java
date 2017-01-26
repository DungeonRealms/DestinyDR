package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;

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
        if(strings.length > 0 && Rank.isDev(commandSender))//  Modifies shutdown time.  //
        	DungeonRealms.getInstance().setRebootTime(System.currentTimeMillis() + (Integer.parseInt(strings[0]) * 1000));
        	
    	String time = DurationFormatUtils.formatDurationWords((DungeonRealms.getInstance().getRebootTime() - System.currentTimeMillis()), true, true);
        commandSender.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + "Next Scheduled Reboot:" + ChatColor.RED + " " + time);
        return true;
    }
}
