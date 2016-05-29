package net.dungeonrealms.game.commands.newcommands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickAllCommand extends BasicCommand {

    public KickAllCommand(String command, String usage, String description) {
    	super(command, usage, description);
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	Player p = (Player)sender;
    	if (!p.isOp()) {
    		return false;
    	}
    	Bukkit.getOnlinePlayers().stream().forEach(newPlayer -> newPlayer.kickPlayer("This server has been put into maintenance mode!"));
    	return true;
    }

}
