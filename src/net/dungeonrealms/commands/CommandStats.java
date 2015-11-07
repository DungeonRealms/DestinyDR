package net.dungeonrealms.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.stats.StatsManager;

/**
 * Created by Chase on Nov 1, 2015
 */
public class CommandStats extends BasicCommand {

	public CommandStats(String command, String usage, String description) {
		super(command, usage, description);
	}

	@Override
	public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
		if (s instanceof ConsoleCommandSender)
			return false;
		Player player = (Player) s;
//		DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.STRENGTH, 0, true);
//		DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.DEXTERITY, 0, true);
//		DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.VITALITY, 0, true);
//		DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.INTELLECT, 0, true);
//		DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.BUFFER_POINTS, 6, true);
		player.openInventory(StatsManager.getInventory(player));
		return true;
	}
}
