package net.dungeonrealms.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.mastery.AsyncUtils;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.Database;
import net.dungeonrealms.shops.ShopMechanics;

/**
 * Created by Chase on Nov 6, 2015
 */
public class CommandStop extends BasicCommand {
	public CommandStop(String command, String usage, String description) {
		super(command, usage, description);
	}

	@Override
	public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
		DungeonRealms.getInstance().setFinishedSetup(false);
        DungeonRealms.getInstance().saveConfig();
        ShopMechanics.deleteAllShops();
        API.logoutAllPlayers();
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () ->{
        	DungeonRealms.getInstance().mm.stopInvocation();
        	Utils.log.info("DungeonRealms onDisable() ... SHUTTING DOWN");
        	Database.mongoClient.close();
        	AsyncUtils.pool.shutdown();
        	Bukkit.getWorlds().get(0).save();
        }, 10);
        	Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () ->{
        	Bukkit.shutdown();
        	}, 40);
		return false;
	}
}
