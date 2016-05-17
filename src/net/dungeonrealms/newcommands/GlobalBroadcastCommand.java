package net.dungeonrealms.newcommands;

import java.util.Collections;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.AbstractCommand;
import net.dungeonrealms.game.network.NetworkServer;
import net.dungeonrealms.game.network.packets.BroadcastPacket;

public class GlobalBroadcastCommand extends AbstractCommand {

    public GlobalBroadcastCommand(String command, String usage, String description) {
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
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < args.length; i++){
    	sb.append(args[i]).append(" ");
    	}
    	 
    	String allArgs = sb.toString().trim();
    	NetworkServer.getInstance().client.getServerConnection().sendTcp(new BroadcastPacket(Collections.emptyList(), allArgs));
    	return true;
    }

}
