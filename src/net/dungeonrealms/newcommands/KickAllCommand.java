package net.dungeonrealms.newcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.AbstractCommand;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;

public class KickAllCommand extends AbstractCommand {

    public KickAllCommand(String command, String usage, String description) {
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
    	Bukkit.getOnlinePlayers().stream().forEach(newPlayer ->{ newPlayer.kickPlayer("This server has been put into maintenance mode!");});
    	return true;
    }

}
