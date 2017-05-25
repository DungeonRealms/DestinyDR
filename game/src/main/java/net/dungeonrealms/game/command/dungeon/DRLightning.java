package net.dungeonrealms.game.command.dungeon;

import net.dungeonrealms.common.game.command.BaseCommand;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DRLightning extends BaseCommand {
	public DRLightning(String command, String usage, String description) {
		super(command, usage, description);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.isOp())
			return true;
		
		
		World w = sender instanceof Player ? ((Player)sender).getWorld() : ((BlockCommandSender)sender).getBlock().getWorld();
		
		if(args.length < 3) {
			sender.sendMessage(ChatColor.RED + "Invalid Syntax. Please use /drlightning <x> <y> <z> to spawn a lightning strike at that location.");
			return true;
		}
		
		double x = Double.parseDouble(args[0]);
		double y = Double.parseDouble(args[1]);
		double z = Double.parseDouble(args[2]);
		
		w.strikeLightning(new Location(w, x, y, z));
		return true;
	}
	
}