package net.dungeonrealms.game.command.dungeon;

import net.dungeonrealms.common.game.command.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
		Player p = null;
		
		if(sender instanceof Player) {
			p = (Player) sender;
		}
		
		if(p != null) {
			if(!(p.isOp())) { return true; }
		}
		
		if(args.length != 3) {
			if(p != null) {
				if(!(p.isOp())) { return true; }
				p.sendMessage(ChatColor.RED + "Invalid Syntax. Please use /drlightning <x> <y> <z> to spawn a lightning strike at that location.");
				return true;
			}
		}
		
		if(sender instanceof BlockCommandSender) {
			BlockCommandSender cb = (BlockCommandSender) sender;
			double x;
			double y;
			double z;
			
			x = Double.parseDouble(args[0]);
			y = Double.parseDouble(args[1]);
			z = Double.parseDouble(args[2]);
			
			Location loc = new Location(cb.getBlock().getWorld(), x, y, z);
			loc.getWorld().strikeLightning(loc);
		} else if(sender instanceof Player) {
			double x;
			double y;
			double z;
			
			x = Double.parseDouble(args[0]);
			y = Double.parseDouble(args[1]);
			z = Double.parseDouble(args[2]);
			
			Location loc = new Location(p.getWorld(), x, y, z);
			loc.getWorld().strikeLightning(loc);
		}
		return true;
	}
	
}