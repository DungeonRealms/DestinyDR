package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StarterCommand extends BaseCommand {

    public StarterCommand(String command, String usage, String description) {
    	super(command, usage, description);
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	Player p = (Player)sender;
    	if (!Rank.isGM(p)) {
    		return false;
    	}
    	if (args.length != 1) {
    		p.sendMessage(ChatColor.RED + "Invalid Paremeters: /givestarter <name>");
    		return true;
    	}
    	if (Bukkit.getPlayer(args[0]) != null) {
			ItemManager.giveStarter(Bukkit.getPlayer(args[0]));
    	} else {
    		p.sendMessage(ChatColor.RED + "Player is offline or something.. Use: /givestarter <name>");
    	}
    	return true;
    }

}
