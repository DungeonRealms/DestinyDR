package net.dungeonrealms;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.network.NetworkAPI;
import net.dungeonrealms.game.player.banks.BankMechanics;

public class TestingCommand extends AbstractCommand {

    public TestingCommand(String command, String usage, String description) {
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
    	p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
    	return true;
    }

}
