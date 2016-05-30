package net.dungeonrealms.game.commands.newcommands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.network.NetworkServer;
import net.dungeonrealms.game.network.packets.BroadcastPacket;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class GlobalBroadcastCommand extends BasicCommand {

    public GlobalBroadcastCommand(String command, String usage, String description) {
    	super(command, usage, description);
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	Player p = (Player)sender;
    	if (!p.isOp()) {
    		return false;
    	}
    	StringBuilder sb = new StringBuilder();
		for (String arg : args) {
			sb.append(arg).append(" ");
		}
    	 
    	String allArgs = sb.toString().trim();
    	NetworkServer.getInstance().client.getServerConnection().sendTcp(new BroadcastPacket(Collections.emptyList(), allArgs));
    	return true;
    }

}
