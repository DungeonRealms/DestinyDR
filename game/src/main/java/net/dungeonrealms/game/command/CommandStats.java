package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.chat.Chat;

import net.dungeonrealms.game.player.inventory.menus.guis.PlayerProfileGUI;
import org.bukkit.ChatColor;
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
		player.sendMessage(ChatColor.GRAY + "The stats page has been merged with the profile page!");
		player.sendMessage(ChatColor.GRAY + "Please use /profile to access this menu in the future!");
		new PlayerProfileGUI(player).open(player,null);
		return true;
	}
}
