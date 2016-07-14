package net.dungeonrealms.game.commands.newcommands;

import net.dungeonrealms.game.commands.BasicCommand;
import net.dungeonrealms.game.database.player.Rank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GlobalBroadcastCommand extends BasicCommand {

    public GlobalBroadcastCommand(String command, String usage, String description) {
    	super(command, usage, description);
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	Player p = (Player)sender;
    	if (!Rank.isDev(p)) {
    		return false;
    	}
    	StringBuilder sb = new StringBuilder();
		for (String arg : args) {
			sb.append(arg).append(" ");
		}
    	 
    	String allArgs = sb.toString().trim();
    	//NetworkServer.getInstance().client.getServerConnection().sendTcp(new BroadcastPacket(Collections.emptyList(), allArgs));
    	return true;
    }

}
