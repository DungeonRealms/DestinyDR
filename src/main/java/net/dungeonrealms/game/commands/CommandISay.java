package net.dungeonrealms.game.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.dungeonrealms.game.player.rank.Rank;

import net.dungeonrealms.game.commands.generic.BasicCommand;

import java.util.Arrays;


/**
 * Created by Chase on Dec 14, 2015
 */
public class CommandISay extends BasicCommand{
    public CommandISay(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        String message = ChatColor.translateAlternateColorCodes('&', String.join(" ", Arrays.asList(args)));

    	if(commandSender instanceof Player){
    		if(!Rank.isGM((Player) commandSender)){
    			return false;
    		}
    		Bukkit.broadcastMessage(message);
    	}else if (commandSender instanceof BlockCommandSender) {
            BlockCommandSender block = (BlockCommandSender) commandSender;
            for(Player p : block.getBlock().getWorld().getPlayers()){
            	p.sendMessage(message);
            }
    	}
		return true;
    }

}
