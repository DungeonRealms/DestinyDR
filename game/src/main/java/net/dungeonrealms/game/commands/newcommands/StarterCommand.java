package net.dungeonrealms.game.commands.newcommands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.database.player.Rank;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StarterCommand extends BasicCommand {

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
