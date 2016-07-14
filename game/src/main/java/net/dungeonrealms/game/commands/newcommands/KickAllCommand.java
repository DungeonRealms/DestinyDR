package net.dungeonrealms.game.commands.newcommands;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.commands.BasicCommand;
import net.dungeonrealms.game.database.player.Rank;
import net.dungeonrealms.game.world.shops.ShopMechanics;
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
    	if (!Rank.isDev(p)) {
    		return false;
    	}
		ShopMechanics.deleteAllShops(true);
		GameAPI.logoutAllPlayers(true, true);
    	return true;
    }

}
