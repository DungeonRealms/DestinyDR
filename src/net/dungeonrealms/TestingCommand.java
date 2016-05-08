package net.dungeonrealms;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.network.NetworkAPI;

public class TestingCommand extends AbstractCommand {

    public TestingCommand(String command, String usage, String description) {
    	super(command, usage, description);
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	Player p = (Player)sender;
        DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$SET, EnumData.ENTERINGREALM, "b7c3e713-e95b-371b-95f9-50b9d08e2d1f", true);
    	API.handleLogout(p.getUniqueId());
    	NetworkAPI.getInstance().sendToServer(p.getName(), "realms1");
    	return true;
    }

}
