package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Created by Chase on Dec 4, 2015
 */
public class CommandShopClose extends BasicCommand {

	public CommandShopClose(String command, String usage, String description) {
		super(command, usage, description);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//if (sender instanceof Player)
		//NetworkAPI.getInstance().sendNetworkMessage("shop", "close", sender.getName());
		return true;
	}
}
