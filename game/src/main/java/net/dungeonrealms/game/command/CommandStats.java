package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.stats.StatsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Chase on Nov 1, 2015
 */
public class CommandStats extends BaseCommand {

	public CommandStats(String command, String usage, String description, List<String> aliases) {
		super(command, usage, description, aliases);
	}

	@Override
	public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
		if (s instanceof ConsoleCommandSender)
			return false;


		Player player = (Player) s;
		Chat.listenForMessage(player, null, null);
		player.openInventory(StatsManager.getInventory(player));
		return true;
	}
}
