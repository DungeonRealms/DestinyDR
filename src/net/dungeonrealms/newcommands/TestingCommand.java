package net.dungeonrealms.newcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.AbstractCommand;

public class TestingCommand extends AbstractCommand {

    public TestingCommand(String command, String usage, String description) {
    	super(command, usage, description);
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	Player p = (Player)sender;
    	if(!p.isOp())
    	{
    		return false;
    	}
    	p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
    	return true;
    }

}
