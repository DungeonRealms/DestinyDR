package net.dungeonrealms.newcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.AbstractCommand;
import net.dungeonrealms.game.handlers.TutorialIslandHandler;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.network.NetworkAPI;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.md_5.bungee.api.ChatColor;

public class StarterCommand extends AbstractCommand {

    public StarterCommand(String command, String usage, String description) {
    	super(command, usage, description);
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	Player p = (Player)sender;
    	if(!p.isOp())
    	{
    		p.kickPlayer("No!");
    		return false;
    	}
    	if(args.length != 1)
    	{
    		p.sendMessage(ChatColor.RED + "Invalid Paremeters: /givestarter <name>");
    		return true;
    	}
    	if(Bukkit.getPlayer(args[0]) != null)
    	{
    		TutorialIslandHandler.getInstance().giveStarterKit(Bukkit.getPlayer(args[0]));
    	} else {
    		p.sendMessage(ChatColor.RED + "Player is offline or something.. Use: /givestarter <name>");
    	}
    	return true;
    }

}
