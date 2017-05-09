package net.dungeonrealms.game.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.PlayerToggles.Toggles;

public class CommandToggle extends BaseCommand {

	private Toggles toggle;
	
	public CommandToggle(Toggles t) {
		super(t.getCommand(), "/<command>", t.getDescription());
		this.toggle = t;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return false;
		
		Player p = (Player) sender;
		PlayerWrapper pw = PlayerWrapper.getWrapper(p);
		if (pw == null)
			return false;
		
		if (!pw.getRank().isAtLeast(toggle.getMinRank())) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to use this toggle.");
			return true;
		}
		
		pw.getToggles().toggle(toggle);
		return true;
	}
}
