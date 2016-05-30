package net.dungeonrealms.game.commands.newcommands;

import net.dungeonrealms.API;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.network.NetworkAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RealmTestCommand extends BasicCommand {

    public RealmTestCommand(String command, String usage, String description) {
    	super(command, usage, description);
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	Player p = (Player)sender;
    	if(!p.isOp()) {
    		return false;
    	}
    	DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$SET, EnumData.ENTERINGREALM, p.getUniqueId().toString(), true);
    	API.handleLogout(p.getUniqueId());
    	NetworkAPI.getInstance().sendToServer(p.getName(), "realms1");
    	return true;
    }

}
